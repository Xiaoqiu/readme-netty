package netty.chapter3.netty;
/**
 * @author kate
 * @create 2019/5/20
 * @since 1.0.0
 */

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @ClassName TimeServer
 * @Description Netty时间服务器，服务端
 * @Author Huang Xiaoqiu
 * @Date 2019/5/20 14:15
 * @Version 1.0.0
 **/
public class TimeServer {
  public void bind(int port) throws Exception {
    //配置服务端的NIO的线程组
    // 一个用于服务端接收客户端的链接
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    //一个用于SocketChannel的网络读写。
    EventLoopGroup workerGroup = new NioEventLoopGroup();

    try {
      // netty启动NIO服务端的辅助启动类
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup,workerGroup)
          // 设置创建的Channel为NioServerSocketChannel，对应于NIO库的ServerSocketChannel
          .channel(NioServerSocketChannel.class)
          // 为NioServerSocketChannel的TCP参数，
          .option(ChannelOption.SO_BACKLOG,1024)
          // 绑定IO事件的处理类，类似于Reactor模式中的handler类。
          // 用于处理网络IO事件，记录日志，消息编解码等。
          .childHandler(new ChildChannelHandler());
      // 绑定端口，同步等待成功。
      // sync();等待绑定操作完成。
      // ChannelFuture: 用于异步操作的通知回调。
      ChannelFuture f = b.bind(port).sync();
      // 等待服务端监听端口关闭，回调
      // 进行阻塞，等待服务端链路关闭之后main()函数才退出。
      f.channel().closeFuture().sync();
    } finally {
      // 优雅退出，释放线程池资源
      bossGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
    }
  }

  private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
      // TimeServerHandler继承ChannelHandlerAdapter对网络事件进行读写操作。
      socketChannel.pipeline().addLast(new TimeServerHandler());
    }
  }

  public static void main(String[] args) throws Exception{
    int port = 8080;
    if (args != null && args.length > 0) {
      try {
        port = Integer.valueOf(args[0]);
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }
    }
    new TimeServer().bind(port);
  }

}