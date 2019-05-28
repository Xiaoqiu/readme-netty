package netty.chapter12;

import io.netty.buffer.ByteBuf;
import org.jboss.marshalling.Marshaller;

import java.io.IOException;

/**
 * @author kate
 * @create 2019/5/28
 * @since 1.0.0
 */
public class MarshallingEncoder {

  private static final byte[] LENGTH_PLACEHOLDER = new byte[4];
  Marshaller marshaller;

  public MarshallingEncoder() throws IOException {
    marshaller = MarshallingCodeCFactory.buildMarshalling();
  }

  protected void encode(Object msg, ByteBuf out) throws Exception {
    try {
      int lengthPos = out.writerIndex();
      out.writeBytes(LENGTH_PLACEHOLDER);
      ChannelBufferByteOutput output = new ChannelBufferByteOutput(out);
      marshaller.start(output);
      marshaller.writeObject(msg);
      marshaller.finish();
      out.setInt(lengthPos, out.writerIndex() - lengthPos - 4);
    } finally {
      marshaller.close();
    }
  }
}