package netty.chapter4.badTest;
/**
 * @author kate
 * @create 2019/5/20
 * @since 1.0.0
 */

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.Date;

/**
 * @ClassName TimeServerHandler
 * @Description Netty时间服务器，服务端
 * 继承ChannelHandlerAdapter对网络事件进行读写操作。
 * @Author Huang Xiaoqiu
 * @Date 2019/5/20 15:00
 * @Version 1.0.0
 **/
public class TimeServerHandler extends ChannelHandlerAdapter {
  private  int counter;
  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

    String body = (String) msg;
    System.out.println("The time server receive order: " + body + "； the counter is : " + ++counter);
    String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ?
        new Date(System.currentTimeMillis()).toString()
        : "BAD ORDER";
    currentTime = currentTime +  System.getProperty("line.separator");
    // 缓冲区，字符串编码为字节，并复制到缓冲区
    ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
    // 只是写入缓冲区，并没有发送到channel，后面ctx.flush()再发送到channel
    // 不直接把消息写入socketChannel，防止频繁唤醒selector
    ctx.writeAndFlush(resp);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    ctx.close();
  }
}