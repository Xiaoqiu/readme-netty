package netty.chapter12;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author kate
 * @create 2019/5/24
 * @since 1.0.0
 */
public class NettyMessageDecoder extends LengthFieldBasedFrameDecoder {
  MarshallingDecoder marshallingDecoder;
  public NettyMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) throws IOException {
    super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
    marshallingDecoder = new MarshallingDecoder();


  }

  @Override
  protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
    ByteBuf frame = (ByteBuf) super.decode(ctx,in);
    if (frame == null) {
      return null;
    }

    NettyMessage message = new NettyMessage();
    Header header = new Header();
    header.setCrcCode(in.readInt());
    header.setLength(in.readInt());
    header.setSessionID(in.readLong());
    header.setType(in.readByte());
    header.setPriority(in.readByte());

    int size = in.readInt();
    if (size > 0) {
      Map<String,Object> attch = new HashMap<String,Object>(size);
      int keySize = 0;
      byte[] keyArray = null;
      String key = null;
      for (int i = 0; i < size; i++) {

      }
    }
    return super.decode(ctx, in);
  }
}