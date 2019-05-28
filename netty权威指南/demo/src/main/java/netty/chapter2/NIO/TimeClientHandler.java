package netty.chapter2;
/**
 * @author kate
 * @create 2019/5/20
 * @since 1.0.0
 */

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @ClassName TimeClientHandler
 * @Description NIO时间服务器客户端
 * @Author Huang Xiaoqiu
 * @Date 2019/5/20 09:06
 * @Version 1.0.0
 **/
public class TimeClientHandler implements Runnable {
  private String host;
  private int port;
  private Selector selector;
  private SocketChannel socketChannel;
  private volatile boolean stop;

  public TimeClientHandler(String host, int port) {
    this.host = host == null ? "127.0.0.1" : host;
    this.port = port;

    try {
      selector = Selector.open();
      socketChannel = SocketChannel.open();
      // 设置客户端连接的TCP参数
      socketChannel.configureBlocking(false);

    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public void run() {
    try {
      doConnect();
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }

    while (!stop) {
      try {
        //等1s唤醒一次selector,即返回一次selectionKey的集合
        // selector每次都会获取一批就绪的key,后续对相应的channel来做处理。
        selector.select(1000);
        Set<SelectionKey> selectionKeySet = selector.selectedKeys();
        Iterator<SelectionKey> it = selectionKeySet.iterator();
        SelectionKey key = null;
        while (it.hasNext()) {
          key = it.next();
          it.remove();
          try {
            handlerInput(key);
          } catch (Exception e) {
            if (key != null) {
              // 如果这个key发生了异常，就取消这个key,并关闭对应的channel
              key.cancel();
              if (key.channel() != null) {
                key.channel().close();
              }
            }
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
        System.exit(1);
      }
    }

    // 多路复用器关闭后，所有注册在上面的channel和pipe等资源都会自动去注册并关闭，所以不需要重复释放资源
    if (selector != null) {
      try {
          selector.close();
      } catch (IOException e) {
        e.printStackTrace();
      }

    }
  }

  // 传力selector返回的SelectionKey，根据不同的类型处理。
  private void handlerInput(SelectionKey key) throws IOException {
    if (key.isValid()) {
      SocketChannel sc = (SocketChannel)key.channel();
      // 判断是否连接成功,判断是否是链接类型的channel
      if (key.isConnectable()) {
        // 如果已经完成链接，那么组织读就绪的channel，告诉selector，后续对读就绪的key感兴趣
        if (sc.finishConnect()) {
          sc.register(selector, SelectionKey.OP_READ);
          doWrite(sc);
        } else {
          //链接失败，进程退出
          System.exit(1);
        }
      }
      // 如果是读就绪的key
      if (key.isReadable()) {
        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        int readBytes = sc.read(readBuffer);
        if (readBytes > 0) {
          // position设置为0
          readBuffer.flip();
          // readBuffer.remaining() " position到limit之间的字节数
          byte[] bytes = new byte[readBuffer.remaining()];
          // 缓冲区复制到字节数组
          readBuffer.get(bytes);
          // 从字节数据组解码到字符串
          String body = new String(bytes,"UTF-8");
          System.out.println("Now is : " + body);
          // 已经读取到了服务器的返回数据，可以停止调用selector.select()方法
          this.stop = true;
        } else if (readBytes < 0 ) {
          // 对链路关闭
          key.cancel();
          sc.close();
        } else {
          ;// 读取到0字节忽略
        }
      }
    }
  }

  // 做一次连接，后续通过遍历SelectionKey来处理对应类型的channel
  private void doConnect() throws IOException {
    // 如果直接连接成功，则注册到多路复用器上，发送请求消息，读应答
    if (socketChannel.connect(new InetSocketAddress(host,port))) {
      // 已经连接成功，告诉一下selector，后面对读取的channel感兴趣。
      socketChannel.register(selector, SelectionKey.OP_READ);
      doWrite(socketChannel);
    } else {
      // 没有连接成功，后续继续对连接的channel感兴趣
      socketChannel.register(selector, SelectionKey.OP_CONNECT);
    }
  }

  private void doWrite(SocketChannel sc) throws IOException {
    // 字符串转为字节编码
    byte[] req = "QUERY TIME ORDER".getBytes();
    // 创建缓冲区
    ByteBuffer writeBuffer = ByteBuffer.allocate(req.length);
    // 字节数组复制到缓冲区
    writeBuffer.put(req);
    // 缓冲器设置为可读，position设置为0
    writeBuffer.flip();
    // socketChannel发送缓冲区数据
    sc.write(writeBuffer);
    /**
     *  socketChannel为异步非阻塞，所以一次性不确定是否全部发送完所有缓冲区字节。
     *  通过判断writeBuffer.hasRemaining()判断是否发送完成.
     *
     */
    if (!writeBuffer.hasRemaining()) {
      System.out.println("Send order 2 server succeed.");
    }
  }

}