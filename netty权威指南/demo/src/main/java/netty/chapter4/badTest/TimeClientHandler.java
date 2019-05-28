package netty.chapter4.badTest;

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

  private int counter;
  private  byte[] req;

  public TimeClientHandler() {
    req = ("QUERY TIME ORDER" + System.getProperty("line.separator")).getBytes();
  }

  // 当客户端和服务端的TCP链路建立成功后，netty的nio线程就会调用，channelActive方法。
  //发送查询指令给服务端。
  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    ByteBuf message = null;
    for ( int i = 0; i < 100 ; i++) {
      message = Unpooled.buffer(req.length);
      message.writeBytes(req);
      // 写入channel发送。
      ctx.writeAndFlush(message);
    }
  }

  // 当服务端返回应答消息，channelRead被调用，
  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    // 读取到缓冲区
    String body = (String) msg;
    System.out.println("Now is: " + body + "; the counter is :" + ++counter);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    logger.warning("Unexpected exception from downstream : " + cause.getMessage());
    ctx.close();
  }
}