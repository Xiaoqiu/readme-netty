package netty.chapter2.AIO;
/**
 * @author kate
 * @create 2019/5/20
 * @since 1.0.0
 */

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Date;

/**
 * @ClassName ReadCompletionHandler
 * @Description AIO时间服务器服务端
 * @Author Huang Xiaoqiu
 * @Date 2019/5/20 11:04
 * @Version 1.0.0
 **/
public class ReadCompletionHandler implements CompletionHandler<Integer, ByteBuffer> {
  private AsynchronousSocketChannel channel;

  public ReadCompletionHandler(AsynchronousSocketChannel channel) {
    if (this.channel == null) {
      this.channel = channel;
    }
  }

  @Override
  public void completed(Integer result, ByteBuffer attachment) {
    attachment.flip();
    byte[] body = new byte[attachment.remaining()];
    attachment.get(body);
    try {
      String req = new String(body,"UTF-8");
      System.out.println("The time server receive order : " + req);
      String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(req)
          ? new Date(System.currentTimeMillis()).toString() : "BAD ORDER";
      doWrite(currentTime);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }

  private void doWrite(String currentTime){
    if (currentTime != null && currentTime.trim().length() > 0) {
      byte[] bytes = (currentTime).getBytes();
      ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
      writeBuffer.put(bytes);
      writeBuffer.flip();
      // 异步回调接口CompletionHandler
      channel.write(writeBuffer, writeBuffer, new CompletionHandler<Integer, ByteBuffer>() {
        @Override
        public void completed(Integer result, ByteBuffer buffer) {
          // 如果没有发送完成，继续发送
          if (buffer.hasRemaining()) {
            channel.write(buffer,buffer,this);
          }
        }

        @Override
        public void failed(Throwable exc, ByteBuffer attachment) {
          try {
            channel.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      });
    }
  }

  @Override
  public void failed(Throwable exc, ByteBuffer attachment) {
    try {
      this.channel.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}