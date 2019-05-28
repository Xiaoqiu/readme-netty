package netty.chapter2;
/**
 * @author kate
 * @create 2019/5/17
 * @since 1.0.0
 */

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * @ClassName MultiplexerTimeServer
 * @Description NIO时间服务器，多路复用器
 * @Author Huang Xiaoqiu
 * @Date 2019/5/17 15:07
 * @Version 1.0.0
 **/
public class MultiplexerTimeServer implements Runnable{
  private Selector selector;
  private ServerSocketChannel servChannel;
  private volatile boolean stop;

  //初始化多路复用器，绑定监听端口
  public MultiplexerTimeServer(int port) {
    try {
      selector = Selector.open();
      servChannel = ServerSocketChannel.open();
      // ServerSocketChannel设置为异步非阻塞
      servChannel.configureBlocking(false);
      // backlog设置为1024
      servChannel.socket().bind(new InetSocketAddress(port), 1024);
      //注意register()方法的第二个参数。这是一个“interest集合”，意思是在通过Selector监听Channel时对什么事件感兴趣
      /**
       * 可以监听四种不同类型的事件：
       * Connect ：某个channel成功连接到另一个服务器称为"连接就绪"
       * Accept： 一个server socket channel准备好接收新进入的连接，"接收就绪"
       * Read：一个有数据可读的通道，"读就绪"
       * Write： 等待写数据的通道，写就绪"
       * 通道触发一个事件的意思的该事件已经就绪了。
       *
       *这四种事件用SelectionKey的四个常量来表示：
       * SelectionKey.OP_CONNECT
       * SelectionKey.OP_ACCEPT
       * SelectionKey.OP_READ
       * SelectionKey.OP_WRITE
       * 对多种事件感兴趣：
       * 	int interestSet = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
       **/
      servChannel.register(selector, SelectionKey.OP_ACCEPT);
      System.out.println("The time server is start in port: " + port);
    } catch (IOException e) {
        e.printStackTrace();
        // 端口如果被占用，则退出。
        System.exit(1);
    }
  }
  public void stop(){
    this.stop = true;
  }

  public void run() {
    // 循环遍历selector,
    //  selector.select(1000); selector的休眠时间为1s, 无论是否有读写时间发送，selector每个1s都被唤醒一次。
    // selector.select(); 当有处于就绪状态的channel时，selector将返回就绪状态的channel selectionKey集合，
    // 通过对就绪的channel集合进行迭代，可以进行网络的异步读写操作。
      while(!stop) {
        try {
          /**
           * 通过Selector选择通道:
           *一旦向Selector注册了一或多个通道，就可以调用几个重载的select()方法。这些方法返回你所感兴趣的事件（如连接、接受、读或写）已经准备就绪的那些通道。
           * 换句话说，如果你对“读就绪”的通道感兴趣，select()方法会返回读事件已经就绪的那些通道。
           * 下面是select()方法：
           * select()方法返回的int值表示有多少通道已经就绪
           * int select() : 阻塞到至少有一个通道在你注册的事件上就绪了。
           * int select(long timeout) : 和select()一样，除了最长会阻塞timeout毫秒(参数)。
           * int selectNow(): 不会阻塞，不管什么通道就绪都立刻返回（译者注：此方法执行非阻塞的选择操作。如果自从前一次选择操作后，
           * 没有通道变成可选择的，则此方法直接返回零。）。
           *
           * select()方法返回的int值表示有多少通道已经就绪。亦即，自上次调用select()方法后有多少通道变成就绪状态。如果调用select()方法，因为有一个通道变成就绪状态，返回了1，若再次调用select()方法，如果另一个通道就绪了，它会再次返回1。如果对第一个就绪的channel没有做任何操作，现在就有两个就绪的通道，但在每次select()方法调用之间，只有一个通道就绪了。
           */
          selector.select(1000);
          /**
           * selectedKeys():
           * ，并且返回值表明有一个或更多个通道就绪了，然后可以通过调用selector的selectedKeys()方法，
           *   访问“已选择键集（selected key set）”中的就绪通道。如下所示：
           */
          Set<SelectionKey> selectionKeySet = selector.selectedKeys();
          Iterator<SelectionKey> it = selectionKeySet.iterator();
          SelectionKey key = null;
          while (it.hasNext()) {
            key = it.next();
            it.remove(); // 删除next()返回的元素。
            try {
              handleInput(key);
            } catch (Exception e) {

            }
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

      // 多路复用器关闭后，所有注册在上面的Channel 和pipe等资源都会被自动去
    //注册并关闭，所以不需要重复释放资源
      if (selector != null) {
        try {
          selector.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
  }

  private void handleInput(SelectionKey key) throws IOException {
      if (key.isValid()) {
        // 根据SelectionKey的操作位判断，即可获知网络事件的类型。
        // 处理新的接入的请求消息
        if (key.isAcceptable()) {
          // 接收到一个新的连接就绪请求
          ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
          // 接收客户端请求，并创建SocketChannel实例，（相当于完TCP的三次握手，TCP物理链路正式建立）
          SocketChannel sc = ssc.accept();
          // 新创建的socketChannel，设置为异步非阻塞。
          // 也可以设置其他TCP参数，例子没有进行额外设置。
          sc.configureBlocking(false);
          // 添加一个新的链接到selector, 这个channel对读就绪的请求感兴趣
          sc.register(selector, SelectionKey.OP_READ);
        }

        if (key.isReadable()) {
          // 接收到一个读就绪请求
          SocketChannel sc = (SocketChannel) key.channel();
          // 申请一块缓冲区，1K大小。
          ByteBuffer readBuffer = ByteBuffer.allocate(1024);
          // 把channel里面的数据写入缓冲区，并返回写入的字节数。
          /**
           * readBytes > 0 : 读到了字节，对字节进行了解吗
           * readBytes = 0 ： 没有读到字节，属于正常场景，忽略
           * readBytes = -1 ：链路已经关闭，需要关闭socketChannel,释放资源。
           */
          int readBytes = sc.read(readBuffer);
          // 把缓冲区的数据读取出来。
          if (readBytes > 0) {
            // 读之前要把缓冲区设置为可读
            /**
             * readBuffer.flip(); 当前缓冲区的limit设置为position，position设置为0
             * 用于后续对缓冲区读取操作。
             */
            readBuffer.flip();
            // 写入一个字节数据，
            byte[] bytes = new byte[readBuffer.remaining()];
            // 把缓冲区可读的字节数组复制到新创建的字节数组中。
            readBuffer.get(bytes);
            // 把字节数据转化为字符串
            String body = new String(bytes,"UTF-8");
            System.out.println("The time server receive order: " + body);
            String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ?
                new Date(System.currentTimeMillis()).toString()
                : "BAD ORDER";
            doWrite(sc,currentTime);
          } else if (readBytes < 0) {
            //对链路关闭
            key.cancel();
            sc.close();
          } else {
            ; // 读到0字节，忽略
          }
        }
      }
  }

  private void doWrite(SocketChannel channel, String response) throws IOException{
    if (response != null && response.trim().length() > 0) {
      // 字符串编码为字节数组
      byte[] bytes = response.getBytes();
      // 创建缓冲区
      ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
      // 字节复制到缓冲区
      writeBuffer.put(bytes);
      // 把缓冲区设置为可读
      writeBuffer.flip();
      // 把缓冲区字节写入channel
      /**
       * 由于socketChannel是异步非阻塞的，所以不能保证一次就能把需要发送的字节数组发送完，
       * 出现"写半包"问题。
       * 需要注册写操作，不断轮询selector,将没有发送完的ByteBuffer发送完毕，
       * 可以通过ByteBuffer的hasRemaining()方法，判断消息是否发送完成。
       * 这个例子没有演示写半包场景。
       */
      channel.write(writeBuffer);
    }
  }

}