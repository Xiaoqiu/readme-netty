package netty.chapter12;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import sun.nio.ch.Net;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 握手成功之后，由客户端主动发送心跳消息，服务端接收到心跳消息之后，
 * 返回响应消息。由于心跳消息的目的是为了检测链路的可用性，因此不需要携带消息体。
 *
 * 心跳超时实现非常简单，直接利用Netty的ReadTimeoutHandler机制。
 * 当一定周期内（50s）没有读取到对方任何消息时，需要主动关闭链路。
 *
 * 客户端，重新发起链接。
 * 服务端，释放资源，清楚客户端登录缓存消息，等待服务端重连。
 * @author kate
 * @create 2019/5/24
 * @since 1.0.0
 */
public class HeartBeatReqHandler extends ChannelHandlerAdapter {
  // 接收线程返回消息
  private volatile ScheduledFuture<?> heatBeat;

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    NettyMessage message = (NettyMessage)msg;
    // 握手成功，主动发送心跳消息
    // 客户端收到服务端的登录返回消息。
    if (message.getHeader() != null
    && message.getHeader().getType() == MessageType.LOGIN_RESP.value()) {
      //  启动无限循环定时器用于定期发送心跳消息。
      // NioEventLoop是一个schedule，因此支持定时器的执行。
      // 每5秒发送一条心跳消息
      heatBeat = ctx.executor().scheduleAtFixedRate(
          new HeartBeatTask(ctx),0,5000, TimeUnit.MILLISECONDS
      );
    } else if (message.getHeader() != null
    && message.getHeader().getType() == MessageType.HEARTBEAT_RESP.value()) {
      // 客户端收到了服务端的返回心跳响应。打印消息
      System.out.println("Client receive server heart beat message: ===> " + message);
    } else {
      // 发送到下一个channelHandler
      ctx.fireChannelRead(msg);
    }
  }
  private class HeartBeatTask implements Runnable {
    private final ChannelHandlerContext ctx;

    public HeartBeatTask(ChannelHandlerContext ctx) {
      this.ctx = ctx;
    }

    @Override
    public void run() {
      NettyMessage heartBeat = buildHeartBeat();
      System.out.println("Client send beat message to server : ===> " + heartBeat);
      ctx.writeAndFlush(heartBeat);
    }

    private NettyMessage buildHeartBeat() {
      NettyMessage message = new NettyMessage();
      Header header = new Header();
      header.setType(MessageType.HEARTBEAT_REQ.value());
      message.setHeader(header);
      return message;
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    // 如果已经执行了异步线程发送心跳消息，就取消这个任务。
    if (heatBeat != null) {
      heatBeat.cancel(true);
      heatBeat = null;
    }
    cause.printStackTrace();
    ctx.close();
  }
}

