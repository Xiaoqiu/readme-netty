package netty.chapter7.messagePack;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

/**
 * @author kate
 * @create 2019/5/27
 * @since 1.0.0
 */
public class EchoClient {
  private final String host;
  private final int port;
  private final int sendNumber;

  public EchoClient(String host, int port, int sendNumber) {
    this.host = host;
    this.port = port;
    this.sendNumber = sendNumber;
  }

  public void run() throws Exception {
    //配置客户端
    EventLoopGroup group = new NioEventLoopGroup();
    try {
      Bootstrap b = new Bootstrap();
      b.group(group).channel(NioSocketChannel.class)
          .option(ChannelOption.TCP_NODELAY,true)
          // 客户端连接超时时间30秒
          .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,3000)
          .handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
              ch.pipeline().addLast("frameDecoder",new LengthFieldBasedFrameDecoder(65535,0,2,0,2));
              ch.pipeline().addLast("msgpack decoder", new MsgpackDecoder());
              ch.pipeline().addLast("frameEncoder",new LengthFieldPrepender(2));
              ch.pipeline().addLast("msgpack encoder",new MsgpackEncoder());
              ch.pipeline().addLast(new EchoClientHandler(sendNumber));
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
    new EchoClient("127.0.0.1",port,3).run();
  }
}