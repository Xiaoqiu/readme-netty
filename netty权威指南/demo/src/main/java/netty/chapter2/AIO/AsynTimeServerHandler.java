package netty.chapter2.AIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.CountDownLatch;

/**
 * AIO时间服务器，服务端
 **/
public class AsynTimeServerHandler implements Runnable {
  private int port;
  CountDownLatch latch;
  // 异步的服务端通道
  AsynchronousServerSocketChannel asynchronousServerSocketChannel;

  public AsynTimeServerHandler(int port) {
    this.port = port;

    try {
      // 创建一个异步的服务端通道
      asynchronousServerSocketChannel = AsynchronousServerSocketChannel.open();
      // 这个异步的通道绑定监听端口。
      asynchronousServerSocketChannel.bind(new InetSocketAddress(port));
      System.out.println("The time server is start in port : " + port);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  @Override
  public void run() {
    // 完成一组操作之前，允许当前线程一直阻塞
    latch = new CountDownLatch(1);
    doAccept();
    try {
      // 让这个线程阻塞，防止服务器端退出。
      // 实际中，不需要启动独立的线程处理AsynchronousServerSocketChannel
      latch.await();
    }catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void doAccept() {
    // 通过CompletionHandler接收accept操作的通知消息。
    // 本例通过AcceptCompletionHandler作为handler
    asynchronousServerSocketChannel.accept(this,
        new AcceptCompletionHandler());
  }
}