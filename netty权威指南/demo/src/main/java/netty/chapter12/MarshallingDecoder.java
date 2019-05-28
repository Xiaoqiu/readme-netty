package netty.chapter12;

import io.netty.buffer.ByteBuf;
import org.jboss.marshalling.ByteInput;
import org.jboss.marshalling.Unmarshaller;

import java.io.IOException;

/**
 * @author kate
 * @create 2019/5/28
 * @since 1.0.0
 */
public class MarshallingDecoder {

  private final Unmarshaller unmarshaller;

  /**
   * Creates a new decoder whose maximum object size is {@code 1048576} bytes.
   * If the size of the received object is greater than {@code 1048576} bytes,
   * a {@link java.io.StreamCorruptedException} will be raised.
   *
   * @throws IOException
   *
   */
  public MarshallingDecoder() throws IOException {
    unmarshaller = MarshallingCodeCFactory.buildUnMarshalling();
  }

  protected Object decode(ByteBuf in) throws Exception {
    int objectSize = in.readInt();
    ByteBuf buf = in.slice(in.readerIndex(), objectSize);
    ByteInput input = new ChannelBufferByteInput(buf);
    try {
      unmarshaller.start(input);
      Object obj = unmarshaller.readObject();
      unmarshaller.finish();
      in.readerIndex(in.readerIndex() + objectSize);
      return obj;
    } finally {
      unmarshaller.close();
    }
  }
}