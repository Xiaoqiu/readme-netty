package netty.chapter11;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpResponseStatus.*;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author kate
 * @create 2019/5/23
 * @since 1.0.0
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {
 private static final Logger logger = Logger.getLogger(WebSocketServerHandler.class.getName());

 private WebSocketServerHandshaker handshaker;

  @Override
  protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
    // 传统的http接入 ： 第一次握手请求由http协议承载，所以是一个http消息。
    //执行handleHttpRequest方法来处理Websocket握手请求。
    if (msg instanceof FullHttpRequest) {
      handleHttpRequest(ctx,(FullHttpRequest)msg);
      // webSocket接入
    } else if (msg instanceof WebSocketFrame) {
      handleWebSocketFrame(ctx,(WebSocketFrame)msg);
    }


  }

  // http请求处理方法： 第一次握手请求由http协议承载，所以是一个http消息。
  // 执行handleHttpRequest方法来处理Websocket握手请求
  private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
    // 解码失败
    // 或者如果头部没有包含Upgrade字段，或者它的值不是websocket，则返回http 400
    if (!request.decoderResult().isSuccess() ||
        (!"websocket".equals(request.headers().get("Upgrade")))) {
      sendHttpResponse(ctx,request,new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,BAD_REQUEST));
      return;
    }
    // 构造握手工程WebSocketServerHandshakerFactory
    WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
        "ws://localhost:8080/websocket",null,false
    );
    // 创建握手处理类WebSocketServerHandshaker
    // 通过握手处理类，构造握手响应消息返回给客户端。
    handshaker = wsFactory.newHandshaker(request);
    if (handshaker == null) {
      WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
    } else {
      handshaker.handshake(ctx.channel(),request);
    }


  }

  // webSocket请求处理方法
  // 第一次http协议承载的握手建立后。服务端对WebSocket消息进行处理
  private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
    // 判断是否是关闭链路的指令
    if (frame instanceof CloseWebSocketFrame) {
      System.out.println("CloseWebSocketFrame， 结束");
      // WebSocketServerHandshaker关闭链路
      handshaker.close(ctx.channel(),((CloseWebSocketFrame) frame).retain());
      return;
    }
    // 判断是否是Ping消息
    if (frame instanceof PingWebSocketFrame) {
      System.out.println("PingWebSocketFrame， 结束");
      // 维持链路的ping消息，构造pong消息返回。
      ctx.channel().write(
          new PongWebSocketFrame(frame.content().retain())
      );
      return;
    }
    //本例子仅仅支持文本消息，不支持二进制消息
    if (!(frame instanceof TextWebSocketFrame)) {
      System.out.println("！TextWebSocketFrame， 结束");
      throw new UnsupportedOperationException(String.format(
          "%s frame type not supported", frame.getClass().getName()
      ));
    }
    // 返回应答消息
    String request = ((TextWebSocketFrame)frame).text();
    System.out.println("received data: " + request);
    ctx.channel().write(
        new TextWebSocketFrame(request
            + " , 欢迎使用Netty WebSocket服务，现在时刻："
            + new java.util.Date().toString()));
  }


  // 请求响应处理方法
  private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse response) {
  // 非200 ，返回应答给客户端
    if (response.status().code() != 200) {
      ByteBuf byteBuf = Unpooled.copiedBuffer(response.status().toString(), CharsetUtil.UTF_8);
      response.content().writeBytes(byteBuf);
      byteBuf.release();
      HttpHeaderUtil.setContentLength(response, response.content().readableBytes());
    }

    // 200的时候， 如果是非Keep-Alive,关闭连接。
    ChannelFuture f = ctx.channel().writeAndFlush(response);

    // 非Keep-Alive,或者返回码不是200
    if (!HttpHeaderUtil.isKeepAlive(request) || response.status().code() != 200) {
      // 会发送一个通知，关闭channel
      f.addListener(ChannelFutureListener.CLOSE);
    }

  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    ctx.flush();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    cause.printStackTrace();
    ctx.close();
  }
}