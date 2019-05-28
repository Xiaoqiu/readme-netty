package netty.chapter7.messagePack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.msgpack.MessagePack;

/**
 * @author kate
 * @create 2019/5/27
 * @since 1.0.0
 */
// 负责把Object类型的POJO对象编码为byte数组，然后写入ByteBuf中
public class MsgpackEncoder extends MessageToByteEncoder<Object> {
  @Override
  protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
    MessagePack messagePack = new MessagePack();
    // 对象被编码之后
    byte[] raw = messagePack.write(msg);
   // 写入发送缓冲区（发送缓冲区，数据要使用字节传输，所以先编码。）
    out.writeBytes(raw);
  }
}