package netty.chapter3.netty;
/**
 * @author kate
 * @create 2019/5/20
 * @since 1.0.0
 */

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

// Netty时间服务器客户端
public class TimeClient {
  public void connet(int port, String host) throws Exception {
    // 配置客户端NIO线程组
    EventLoopGroup group = new NioEventLoopGroup();
    try {
      Bootstrap b = new Bootstrap();
      b.group(group).channel(NioSocketChannel.class)
          .option(ChannelOption.TCP_NODELAY, true)
          .handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
              ch.pipeline().addLast(new TimeClientHandler());
            }
          });
      // 发起异步链接操作
      // 阻塞，等待链接建立就会返回
      ChannelFuture f = b.connect(host,port).sync();

      // 阻塞，等待客户端链路关闭就会返回
      f.channel().closeFuture().sync();
    } finally {
      // 优雅退出，释放NIO线程组
      group.shutdownGracefully();
    }
  }

  public static void main(String[] args) throws Exception {
    int port = 8080;
    if (args != null && args.length > 0) {
      try {
        port = Integer.valueOf(args[0]);
      } catch (NumberFormatException e) {
        // 默认值
      }
    }

    new TimeClient().connet(port,"127.0.0.1");
  }

}