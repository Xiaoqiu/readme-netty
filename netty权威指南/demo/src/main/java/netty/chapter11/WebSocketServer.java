package netty.chapter11;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.io.IOException;

/**
 * @author kate
 * @create 2019/5/23
 * @since 1.0.0
 */
public class WebSocketServer {
  public void run(int port) throws Exception {
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup,workerGroup)
          .channel(NioServerSocketChannel.class)
          .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
              // http消息解码和编码器
              ch.pipeline().addLast("http-codec",new HttpServerCodec());
              // http解码处理的多个对象聚合为一个FullHttpRequest对象
              // 将http消息的多个部分组合成一条完整的http消息
              ch.pipeline().addLast("aggregator",new HttpObjectAggregator(65536));
              // http返回消息需要写入大量数据的处理器,
              // 向客户端发送HTML5文件
              ch.pipeline().addLast("http-chunked",new ChunkedWriteHandler());
              // 自定义的处理器，对接收消息和发送消息做加工。
              ch.pipeline().addLast("handler",new WebSocketServerHandler());
            }
          });
      // todo:
       Channel ch = b.bind(port).sync().channel();

      System.out.println("Web socket server started at port: " + port );
      System.out.println("open your browser and nagigate to http://localhost:" + port + '/');
      ch.closeFuture().sync();

    } finally {
      bossGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
    }
  }

  public static void main(String[] args) throws Exception {
    int port = 8080;
    if (args != null && args.length > 0) {
      try {
        port = Integer.valueOf(args[0]);
      } catch (NumberFormatException e) {
        //
      }
    }

    new WebSocketServer().run(port);
  }
}