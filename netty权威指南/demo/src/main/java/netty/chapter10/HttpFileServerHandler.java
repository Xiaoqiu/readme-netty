package netty.chapter10;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpResponseStatus.*;

/**
 * HTTP 文件服务器 处理类
 * @author kate
 * @create 2019/5/22
 * @since 1.0.0
 */
public class HttpFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
  private final String url;
  private static final Pattern ALLOWED_FILE_NAME = Pattern
      .compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");
  private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");

  public HttpFileServerHandler(String url) {
    this.url = url;
  }

  @Override
  protected void messageReceived(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {

    if (!request.decoderResult().isSuccess()) {
      // 请求消息解码失败，返回400
      sendError(ctx,BAD_REQUEST);
      return;
    }
    if (request.method() != HttpMethod.GET) {
      // 构造405错误
      sendError(ctx,METHOD_NOT_ALLOWED);
      return;
    }
    final String uri = request.uri();
    final String path = sanitizeUri(uri);

    // 如果构造的uri不合法，则返回HTTP 403
    if (path == null) {
      sendError(ctx,FORBIDDEN);
      return;
    }
    // 使用新组装的uri路径构造File对象，
    File file = new File(path);
    // 如果文件不存在或者系统隐藏文件，在构建404异常返回
    if (file.isHidden() || !file.exists()) {
      sendError(ctx,NOT_FOUND);
      return;
    }
    // 如果文件是目录，则发送目录链接给客户端浏览器。
    if (file.isDirectory()) {
      if (uri.endsWith("/")) {
        sendListing(ctx,file);
      } else {
        sendRedirect(ctx,uri + '/');
      }
      return;
    }
    if (!file.isFile()) {
      sendError(ctx,FORBIDDEN);
      return;
    }
    //
    RandomAccessFile randomAccessFile = null;
    try {
      // 以只读的方式打开文件
      randomAccessFile = new RandomAccessFile(file,"r");
    } catch (FileNotFoundException fnfe) {
      sendError(ctx,NOT_FOUND);
      return;
    }
    long fileLength = randomAccessFile.length();
    HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,OK);
    // 获取文件长度
    HttpHeaderUtil.setContentLength(response,fileLength);
    setContentTypeHeader(response,file);
    // 如果是keep-alive，则在应答消息中设置Connection为keep-alive
    if (HttpHeaderUtil.isKeepAlive(request)) {
      response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
    }
    ctx.write(response);
    // 通过netty的ChunkedFile对象直接将文件写入到发送缓冲区中。
    ChannelFuture sendFileFuture;
    sendFileFuture = ctx.write(new ChunkedFile(randomAccessFile,0,fileLength,8192),ctx.newProgressivePromise());
    sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
      @Override
      public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) throws Exception {
        if (total < 0) {
          System.err.println("Transfer Progress: " + progress);
        } else {
          System.err.println("Transfer Progress: " + progress + "/" + total);
        }
      }

      @Override
      public void operationComplete(ChannelProgressiveFuture future) throws Exception {
          System.out.println("Transfer complete");
      }
    });
    // 如果使用chunked编码，最后需要发送一个编码结束的空消息体。
    //将LastHttpContent.EMPTY_LAST_CONTENT发送到缓冲区中，标识所有的消息体已经发送完成。
    // 同时调用flush方法将之前发送到缓冲区的消息刷新到SocketChannel中发送诶对方。
    ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
    // 如果是非KeepAlive，最后一包消息发送完成之后，服务端要主动关闭连接。
    if (!HttpHeaderUtil.isKeepAlive(request)) {
      lastContentFuture.addListener(ChannelFutureListener.CLOSE);
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    cause.printStackTrace();
    if (ctx.channel().isActive()) {
      sendError(ctx,INTERNAL_SERVER_ERROR);
    }
  }

  private String sanitizeUri(String uri) {
    try {
      // 对URL进行解码，使用UTF-8字符集，解码成功之后对URI进行合法性判断。
      uri = URLDecoder.decode(uri,"UTF-8");
    } catch (UnsupportedEncodingException e) {
      try {
        uri = URLDecoder.decode(uri,"ISO-8859-1");
      } catch (UnsupportedEncodingException e1) {
        throw  new Error();
      }
    }
    // 如果uri与允许的url一直，或者是其子目录（文件），则校验通过。否则返回空
    if (!uri.startsWith(url)) {
      return null;
    }
    if (!uri.startsWith("/")) {
      return null;
    }
    // 将硬编码的文件路径分隔符替换为本地操作系统的文件分隔符。
    uri = uri.replace('/',File.separatorChar);

    // 对新额uri做第二次合法性校验，如果校验失败则直接返回空。
    if (uri.contains(File.separator + '.')
      || uri.contains('.' + File.separator) || uri.startsWith(".")
      || uri.endsWith(".") || INSECURE_URI.matcher(uri).matches()) {
      return null;
    }
    System.out.println("user.dir : +" + System.getProperty("user.dir"));
    System.out.println("uri : +" + uri);
    System.out.println(" : +" + System.getProperty("user.dir") + File.separator + uri);
    // 最后对文件进行拼接，使用运行程序所在工程目录+uri构造绝对路径返回。
    return System.getProperty("user.dir") + File.separator + uri;
  }

  // 返回文件链接响应给客户端
  private static void sendListing(ChannelHandlerContext ctx, File dir) {
    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,OK);
    // 需要将响应结果显示在浏览器上，采用HTML的格式。
    response.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/html;charset=UTF-8");
    StringBuilder buf = new StringBuilder();
    String dirPath = dir.getPath();
    buf.append("<!DOCTYPE html>\r\n");
    buf.append("<html><head><title>");
    buf.append(dirPath);
    buf.append(" 目录： ");
    buf.append("</title></head><body>\r\n");
    buf.append("<h3>");
    buf.append(dirPath).append(" 目录：");
    buf.append("</h3>\r\n");
    buf.append("ul");
    buf.append("<url>链接： <a href=\"../\">..</a></li>\r\n");
    for (File f: dir.listFiles()) {
      if (f.isHidden() || !f.canRead()) {
        continue;
      }
      String name = f.getName();

      if (!ALLOWED_FILE_NAME.matcher(name).matches()) {
        continue;
      }
      buf.append("<li>链接：<a href=\"");
      buf.append(name);
      buf.append("\">");
      buf.append(name);
      buf.append("</a></li>\r\n");
    }
    buf.append("</ul></body></html>\r\n");
    // 写入缓冲区
    ByteBuf  buffer = Unpooled.copiedBuffer(buf,CharsetUtil.UTF_8);
    response.content().writeBytes(buffer);
    // 释放缓冲区
    buffer.release();
    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
  }

  private static void sendRedirect(ChannelHandlerContext ctx, String newUri) {
    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,FOUND);
    response.headers().set(HttpHeaderNames.LOCATION,newUri);
    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
  }

  private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,status, Unpooled.copiedBuffer("Failure: " + status.toString() +
        "\r\n", CharsetUtil.UTF_8));
    response.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/plain; charset=UTF-8");
    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
  }

  private static void setContentTypeHeader(HttpResponse response, File file) {
    MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();
    response.headers().set(HttpHeaderNames.CONTENT_TYPE,mimetypesFileTypeMap.getContentType(file.getPath()));
  }

}