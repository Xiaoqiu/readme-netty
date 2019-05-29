package netty.chapter12;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import sun.plugin2.message.Message;

/**
 * 服务端接收心跳消息，制造返回消息。
 * @author kate
 * @create 2019/5/29
 * @since 1.0.0
 */
public class HeartBeatRespHandler extends ChannelHandlerAdapter {
  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    NettyMessage message = (NettyMessage)msg;
    // 判断是心跳请求消息,发送心跳响应。
    if (message.getHeader() != null
    && message.getHeader().getType() == MessageType.HEARTBEAT_REQ.value()) {
      System.out.println("Receive client heart beat message : ====> " + message);
      NettyMessage heartBeat = buildHeartBeat();
      System.out.println("Send heart beat response message to client : ===> " + heartBeat);
      ctx.writeAndFlush(heartBeat);
    } else {
      // 如果不是心跳请求，就传递到下一个channelHandler
      ctx.fireChannelRead(msg);
    }
  }

  private NettyMessage buildHeartBeat() {
    NettyMessage message = new NettyMessage();
    Header header = new Header();
    header.setType(MessageType.HEARTBEAT_RESP.value());
    message.setHeader(header);
    return message;
  }
}