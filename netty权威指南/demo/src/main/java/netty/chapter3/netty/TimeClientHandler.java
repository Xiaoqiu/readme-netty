package netty.chapter3.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.logging.Logger;

/**
 * NIO客户端，读写操作处理
 */
public class TimeClientHandler extends ChannelHandlerAdapter {
  private static final Logger logger = Logger.getLogger(TimeClientHandler.class.getName());
  private final ByteBuf firstMessage;

  public TimeClientHandler() {
    // 客户端发送的一条信息字符串，编码为字节数组
    byte[] req = "QUERY TIME ORDER".getBytes();
    // 初始化ByteBuf
    firstMessage = Unpooled.buffer(req.length);
    // 字节数组复制（写入）ByteBuf缓冲区
    firstMessage.writeBytes(req);
  }

  // 当客户端和服务端的TCP链路建立成功后，netty的nio线程就会调用，channelActive方法。
  //发送查询指令给服务端。
  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    // 写入channel发送。
    ctx.writeAndFlush(firstMessage);
  }

  // 当服务端返回应答消息，channelRead被调用，
  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    // 读取到缓冲区
    ByteBuf buf = (ByteBuf) msg;
    byte[] req = new byte[buf.readableBytes()];
    // 从缓冲区复制（读取）到字节数组。
    buf.readBytes(req);
    // 解码为字符串
    String body = new String(req, "UTF-8");
    System.out.println("Now is: " + body);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    logger.warning("Unexpected exception from downstream : " + cause.getMessage());
    ctx.close();
  }
}