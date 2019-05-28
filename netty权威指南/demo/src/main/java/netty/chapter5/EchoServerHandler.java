package netty.chapter5;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author kate
 * @create 2019/5/21
 * @since 1.0.0
 */
public class EchoServerHandler extends ChannelHandlerAdapter {
  int counter = 0;

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

    String body = (String)msg;
    System.out.println("This is " + ++counter + " times receive client : [ " + body + " ]");
    // 客户端也是通过这个分隔符处理消息，所以返回去的时候也要加这个分隔符。
    body += "$_";
    ByteBuf echo = Unpooled.copiedBuffer(body.getBytes());
    ctx.writeAndFlush(echo);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    cause.printStackTrace();
    ctx.close();
  }
}