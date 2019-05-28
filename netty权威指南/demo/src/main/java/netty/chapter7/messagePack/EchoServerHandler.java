package netty.chapter7.messagePack;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author kate
 * @create 2019/5/27
 * @since 1.0.0
 */
public class EchoServerHandler extends ChannelHandlerAdapter {
  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    System.out.println("server receive the msgpack message : "+msg+"");
    // 原路返回给客户端
    ctx.writeAndFlush(msg);
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    ctx.flush();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    System.out.println("exceptionCaught");
    cause.printStackTrace();
    ctx.close();
  }
}