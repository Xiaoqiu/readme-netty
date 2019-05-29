package netty.chapter12;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * 服务端
 * @author kate
 * @create 2019/5/24
 * @since 1.0.0
 */
public class NettyServer {
  public void bind() throws Exception {
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workGroup = new NioEventLoopGroup();
    ServerBootstrap b = new ServerBootstrap();
    b.group(bossGroup,workGroup)
        .channel(NioServerSocketChannel.class)
        .option(ChannelOption.SO_BACKLOG,100)
        .handler(new LoggingHandler(LogLevel.INFO))
        .childHandler(new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addLast(new NettyMessageDecoder(1024 * 1024, 4, 4));
            ch.pipeline().addLast(new NettyMessageEncoder());
            // 发送登录响应
            ch.pipeline().addLast(new LoginAuthRespHandler());
            // 发送心跳响应
            ch.pipeline().addLast("HeartBeatHandler",new HeartBeatRespHandler());
          }
        });
    // 绑定端口，同步等待成功
    System.out.println("Netty server start ok : "
        + (NettyConstant.REMOTEIP + ":" + NettyConstant.PORT));
    ChannelFuture channelFuture = b.bind(NettyConstant.REMOTEIP,NettyConstant.PORT).sync();
    channelFuture.channel().closeFuture().sync();
  }
  public static void main(String[] args) throws Exception {
    new NettyServer().bind();
  }
}