package netty.chapter7.messagePack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.msgpack.MessagePack;

import java.util.List;

/**
 * 从接收缓冲区msg中获取byte数组，然后调用MessagePack的read方法，
 * 将其反序列化为Object对象，将解码后的的对象加入到解码列表out。
 * @author kate
 * @create 2019/5/27
 * @since 1.0.0
 */
public class MsgpackDecoder extends MessageToMessageDecoder<ByteBuf> {
  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
    final byte[] array;
    // 获取接收缓冲区的字节长度
    final int length = msg.readableBytes();
    // 创建字节数组
    array = new byte[length];
    // 读取字节缓冲区的字节到字节数组
    msg.getBytes(msg.readerIndex(),array,0,length);
    // 解码字节数组，并写入解码对象列表
    MessagePack messagePack = new MessagePack();
    out.add(messagePack.read(array));

  }
}