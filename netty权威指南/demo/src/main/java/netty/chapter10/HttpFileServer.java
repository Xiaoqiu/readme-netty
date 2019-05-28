package netty.chapter10;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * 文件服务器，启动类
 * @author kate
 * @create 2019/5/22
 * @since 1.0.0
 */
public class HttpFileServer {
  private static final String DEFAULT_URL = "/";

  public void run (final int port, final String url) throws Exception {
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workGroup = new NioEventLoopGroup();
    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup,workGroup)
          .channel(NioServerSocketChannel.class)
          .childHandler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
              // http解码器
              // http消息会生成的对象
              // 1) HttpRequest 2)HttpContent 3)LastHttpContent
              ch.pipeline().addLast("http-decoder",new HttpRequestDecoder());
              // http解码器在每个http消息种都会生成多个对象，这个解码器作用就是准换为单一的FullHttpRequest对象
              ch.pipeline().addLast("http-aggregator",new HttpObjectAggregator(65536));
              // 对http响应消息编码
              ch.pipeline().addLast("http-encoder",new HttpResponseEncoder());
              // 支持异步发送打的码流（如大文件传输），但不占用过多的内存，防止发送JAVA内存溢出。
              ch.pipeline().addLast("http-chunked",new ChunkedWriteHandler());
              // 自定义，用于文件服务器的业务处理
              ch.pipeline().addLast("fileServerHandler",new HttpFileServerHandler(url));
            }
          });

      ChannelFuture future = b.bind("127.0.0.1",port).sync();
      System.out.println("HTTP 文件目录服务器启动，网址： http://127.0.0.1:" + port + url );
      future.channel().closeFuture().sync();
    } finally {
      bossGroup.shutdownGracefully();
      workGroup.shutdownGracefully();
    }
  }

  public static void main (String[] args) throws Exception {
    int port = 8080;
    if (args != null && args.length > 0) {
      try {
        port = Integer.valueOf(args[0]);
      } catch (NumberFormatException e) {
        // 默认
      }
    }
    String url = DEFAULT_URL;
    if (args.length > 1) {
      url = args[1];
    }

    new HttpFileServer().run(port,url);
  }
}