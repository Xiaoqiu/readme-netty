package netty.chapter9;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.marshalling.*;
import org.jboss.marshalling.MarshallerFactory;
import org.jboss.marshalling.Marshalling;
import org.jboss.marshalling.MarshallingConfiguration;

/**
 * @author kate
 * @create 2019/5/27
 * @since 1.0.0
 */
public class MarshallingCodeCFactory {

  public static ChannelHandler buildMarshallingDecoder() {
    final MarshallerFactory marshallerFactory = Marshalling.getProvidedMarshallerFactory("serial");
    final MarshallingConfiguration configuration = new MarshallingConfiguration();
    configuration.setVersion(5);

    UnmarshallerProvider provider = new DefaultUnmarshallerProvider(
        marshallerFactory, configuration
    );
    // 1024: 单个消息序列化后的最大长度
    MarshallingDecoder decoder = new MarshallingDecoder(provider,1024);
    return decoder;
  }

  public static ChannelHandler buildMarshallingEncoder() {
    final MarshallerFactory marshallerFactory = Marshalling.getProvidedMarshallerFactory("serial");
    final MarshallingConfiguration configuration = new MarshallingConfiguration();
    configuration.setVersion(5);
    MarshallerProvider provider = new DefaultMarshallerProvider(
        marshallerFactory, configuration
    );
    MarshallingEncoder encoder = new MarshallingEncoder(provider);
    return encoder;
  }
}