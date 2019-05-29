package netty.chapter12;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 一单链路关闭，多了5s后重新链接的代码。
 * @author kate
 * @create 2019/5/24
 * @since 1.0.0
 */
public class NettyClient {
  // 创建一个5s重新链接的定时器。线程池
  private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
  EventLoopGroup group = new NioEventLoopGroup();

  public void connect(int port, String host) throws Exception {
    try {
      Bootstrap b = new Bootstrap();
      b.group(group)
          .channel(NioSocketChannel.class)
          .handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
              ch.pipeline().addLast(new NettyMessageDecoder(1024 * 1024, 4, 4));
              ch.pipeline().addLast("MessageEncoder", new NettyMessageEncoder());
              ch.pipeline().addLast("readTimeoutHandler", new ReadTimeoutHandler(50));
              ch.pipeline().addLast("LoginAuthHandler", new LoginAuthReqHandler());
              ch.pipeline().addLast("HeartBeatHandler", new HeartBeatReqHandler());
            }
          });
      // 发起异步链接操作
      ChannelFuture f = b.connect(host,port).sync();

      f.channel().closeFuture().sync();

    } finally {
      // 释放所有资源之后，清空资源，造次发起链接重连。
      executor.execute(new Runnable() {
        @Override
        public void run() {
          try {
            // 5s后重连
            TimeUnit.SECONDS.sleep(5);
            // 发起重连
            connect(NettyConstant.PORT,NettyConstant.REMOTEIP);
          } catch (InterruptedException e) {
            e.printStackTrace();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      });
    }
  }

  public static void main (String[] args) throws Exception {
    new NettyClient().connect(NettyConstant.PORT,NettyConstant.REMOTEIP);
  }
}