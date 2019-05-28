package netty.chapter2.AIO;

import java.io.IOException;

/**
 * 异步套接字通道的真正异步非阻塞I/O
 * 不需要通过多路复用器（Selector）对注册的通道进行轮询操作，即可实现异步读写。
 * 简化了NIO的编程模型。
 * AIO时间服务器，服务端
 */
public class TimeServer {
  public static void main (String[] args) throws IOException {
    int port = 8080;
    if (args != null && args.length > 0) {
      try {
        port = Integer.valueOf(args[0]);
      } catch (NumberFormatException e) {
         // 采用默认值
      }
    }
    AsynTimeServerHandler timeServer = new AsynTimeServerHandler(port);
    Thread thread = new Thread(timeServer,"AIO-AsynTimeServerHandler-001");
    thread.start();
  }
}