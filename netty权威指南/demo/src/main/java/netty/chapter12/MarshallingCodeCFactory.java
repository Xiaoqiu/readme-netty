package netty.chapter12;

import io.netty.handler.codec.marshalling.*;
import org.jboss.marshalling.*;

import java.io.IOException;

/**
 * MarshallingCodeCFactory 工厂类创建了MarshallingDecoder解码器，并将其
 * 加入到ChannelPipeline中，
 * @author kate
 * @create 2019/5/24
 * @since 1.0.0
 */
public final class MarshallingCodeCFactory {

  /**
   * 创建Jboss Marshaller
   *
   * @return
   * @throws IOException
   */
  protected static Marshaller buildMarshalling() throws IOException {
    final MarshallerFactory marshallerFactory = Marshalling
        .getProvidedMarshallerFactory("serial");
    final MarshallingConfiguration configuration = new MarshallingConfiguration();
    configuration.setVersion(5);
    Marshaller marshaller = marshallerFactory
        .createMarshaller(configuration);
    return marshaller;
  }

  /**
   * 创建Jboss Unmarshaller
   *
   * @return
   * @throws IOException
   */
  protected static Unmarshaller buildUnMarshalling() throws IOException {
    final MarshallerFactory marshallerFactory = Marshalling
        .getProvidedMarshallerFactory("serial");
    final MarshallingConfiguration configuration = new MarshallingConfiguration();
    configuration.setVersion(5);
    final Unmarshaller unmarshaller = marshallerFactory
        .createUnmarshaller(configuration);
    return unmarshaller;
  }

}