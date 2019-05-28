package netty.chapter8;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author kate
 * @create 2019/5/28
 * @since 1.0.0
 */
public class SubReqClientHandler extends ChannelHandlerAdapter {
  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    for ( int i = 0; i < 10; i++) {
      ctx.write(subReq(i));
    }
    ctx.flush();
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    System.out.println("receive server response : [ " + msg + " ]");
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
  private static SubscribeReqProto.SubscribeReq subReq(int i) {
    SubscribeReqProto.SubscribeReq.Builder builder = SubscribeReqProto.SubscribeReq
        .newBuilder();
    builder.setSubReqID(i);
    builder.setUserName("kate");
    builder.setProductName("book");
    builder.setAddress("shenzhen");
    return builder.build();
  }
}