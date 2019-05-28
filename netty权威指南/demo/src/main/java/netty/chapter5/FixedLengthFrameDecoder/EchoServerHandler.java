package netty.chapter5.FixedLengthFrameDecoder;

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

    System.out.println("This is " + ++counter + " times receive client : [ " + msg + " ]");
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    cause.printStackTrace();
    ctx.close();
  }
}