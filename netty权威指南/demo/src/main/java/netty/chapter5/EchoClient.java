package netty.chapter5;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * @author kate
 * @create 2019/5/21
 * @since 1.0.0
 */
public class EchoClient {

  public void connect(int port,String host) throws Exception {
    EventLoopGroup group = new NioEventLoopGroup();
    try {
      Bootstrap b = new Bootstrap();
      b.group(group).channel(NioSocketChannel.class)
          .handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
              ByteBuf delimiter = Unpooled.copiedBuffer("$_".getBytes());
              // 分隔符解码
              ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1024,delimiter));
              // 字节转字符解码
              ch.pipeline().addLast(new StringDecoder());
              // 自定义处理器
              ch.pipeline().addLast(new EchoClientHandler());
            }
          });
      ChannelFuture f = b.connect(host,port).sync();
      f.channel().closeFuture().sync();

    } finally {
      group.shutdownGracefully();
    }
  }

  public static void main(String[] args) throws Exception {
    int port = 8080;
    if (args != null && args.length > 0) {
      try {
        port = Integer.valueOf(args[0]);
      } catch (NumberFormatException e) {
        // 使用默认值
      }

    }
    new EchoClient().connect(port,"127.0.0.1");
  }
}