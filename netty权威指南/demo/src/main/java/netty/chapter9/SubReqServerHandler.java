package netty.chapter9;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import netty.chapter8.SubscribeReqProto;
import netty.chapter8.SubscribeRespProto;

/**
 * @author kate
 * @create 2019/5/28
 * @since 1.0.0
 */
public class SubReqServerHandler extends ChannelHandlerAdapter {
  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    SubscribeReqProto.SubscribeReq req = (SubscribeReqProto.SubscribeReq)msg;
    if ("kate".equalsIgnoreCase(req.getUserName())) {
      System.out.println("Service accept client subscribe req: [ " + req.toString() + " ]");
      ctx.writeAndFlush(resp(req.getSubReqID()));
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    cause.printStackTrace();
    ctx.close();
  }

  private SubscribeRespProto.SubscribeResp resp (int subReqID) {
    SubscribeRespProto.SubscribeResp.Builder builder = SubscribeRespProto
        .SubscribeResp.newBuilder();
    builder.setSubReqID(subReqID);
    builder.setRespCode(0);
    builder.setDesc("good book!");
    return builder.build();
  }
}