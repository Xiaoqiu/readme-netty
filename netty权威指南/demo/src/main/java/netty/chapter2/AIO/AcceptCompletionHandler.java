package netty.chapter2.AIO;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * @ClassName AcceptCompletionHandler
 * @Description AIO时间服务器服务端
 **/
public class AcceptCompletionHandler implements CompletionHandler<AsynchronousSocketChannel,AsynTimeServerHandler> {

  @Override
  public void completed(AsynchronousSocketChannel result, AsynTimeServerHandler attachment) {
    // 获取这个异步channel,继续调用accept方法，
    // 当有新的客户端连接接入，系统回调我们传入的CompletionHandler实例的，completed方法。
    // 表示客户端以及接入成功。
    attachment.asynchronousServerSocketChannel.accept(attachment,this);
    // 创建缓冲区。
    ByteBuffer buffer = ByteBuffer.allocate(1024);
    // 异步读取数据。
    /**
     * AsynchronousSocketChannel:异步channel的read()方法参数
     * ByteBuffer dst: 接收缓冲区，用于从异步channel中读取数据包
     * A attachment : 异步channel携带的附件，通知回调的时候作为入参使用。
     * CompletionHandler<Integer,? super A> handler : 接收通知回调业务handler,
     * 本程序为ReadCompletionHandler
     */
    result.read(buffer,buffer,new ReadCompletionHandler(result));
  }

  @Override
  public void failed(Throwable exc, AsynTimeServerHandler attachment) {
      exc.printStackTrace();
      attachment.latch.countDown();
  }

}