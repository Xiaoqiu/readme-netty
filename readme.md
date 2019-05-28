## 传输
### 不通过netty使用OIO和NIO(使用JDK)

### 通过netty使用OIO和NIO
- 每种传输实现都暴露相同的API，
- 传输实现依赖于interface Channel, ChannelPipeline, ChannelHandler

### 传输API
- 传输API的核心是interface Channel, 被所有I/O操作，
- Channel类的层次结果
- Channel接口（父接口）
    - ServerChannel接口（子接口）
    - ChannelPipeline接口 (每个Channel都会被分配一个)
    - ChannelConfig接口 (每个Channel都会被分配一个，包含了所有配置，支持热更新)
    - AbstractChannel接口（子接口）
    
- ChannelPipeline持有所有将应用于入站和出站数据以及事件的ChannelHandler实例。
    - ChannelPipeline实现了一种常见的设计模式---拦截过滤器
- ChannelHandler实现了应用程序用于处理状态变化以及数据处理的逻辑。
    - 1 将数据从一种格式转换为另一种格式
    - 2 提供异常的通知
    - 3 提供Channel变为活动的或者非活动的通知
    - 4 提供当Channel注册到EventLoop或者从EventLoop注销时的通知
    - 5 提供有关用户自定义事件的通知
    
- Channel的方法
    - eventLoop : 返回分配非Channel的EventLoop
    - pipleline : 返回分配给Channel的ChannelPipeline
    - isActive : 如果Channel是活动的，返回true。活动的意义可依赖于底层的传输。例如：一个Socket传输一旦链接到远程节点便是活动的。
        一个Datagram传输一旦被打开便是活动的
    -  localAddress : 返回本地的SocketAddress
    - remoteAddress : 返回远程的SocketAddress
    - write : 将数据写到远程节点。这个数据将被传递给ChannelPipeline,并且排队直到他被冲刷
    - flush : 将之前已写的数据冲刷到底层传输，如一个Socket
    - writeAndFlush : 一个简单的方法，等于调用write()并接着调用flush()
    
- 实例： 写数据并将其冲刷到远程节点这样的常规任务。使用Channel.writeAndFlush()实现

```java

```
    
    
## 第五章 ByteBuf
- 网络数据的基本单位总是字节
- Java NIO提供了ByteBuffer作为它的字节容器，这个类使用过于复杂。
- Netty的ByteBuffer的替代品是ByteBuf,一个强大的实现。

### 5.1 ByteBuf的API
Netty 的数据处理API通过两个组件暴露
- abstract class ByteBuf
- interface ByteBufHolder
- ByteBuf的API的优点：
    - 可以被用户自定义的缓冲区类型扩展
    - 通过内置的复合缓冲区类型实现了透明的零拷贝
    - 容量可以按需增长（类似于JDK的StringBuilder）
    - 在读和写这两种模式之前不需要调用ByteBuffer的flip()方法；
    - 读和写使用了不同索引
    - 支持方法的链式调用
    - 支持引用计数
    - 支持池化
    
### 5.2 ByteBuf类-----Netty的数据容器
- 所有网络通信都涉及字节序列的移动，高效的数据结构必不可少
- 使用不同索引简化数据访问
#### 5.2.1 如何工作的
- ByteBu维护两个不同的索引，一个用于读取，一个用于写入
- 

### 5.3 字节级的操作
 

### 5.4 ByteHolder接口
### 5.5 ByteBuf分配
### 5.6 引用计数    
    
    
## 编码，解码框架
### 解码器:入站消息转换格式，实现ChannelInboundHandler
#### 字节解码为消息：ByteToMessageDecoder, ReplayingDecoder;
        - 抽象方法：
```java

 decode(ChannelHandlerContext ctx,ByteBuf in, List<Object> out);
 //List不为空就会传到下一个ChannelInboundHandler
 //这个方法会被重复调用，直到ByteBuf没有可读字节为止。
 
 decodeLast(ChannelHandlerContext ctx,ByteBuf in, List<Object> out)
 //Channel状态为非活动时，这个方法被调用一次.
 
```
        - 例子
```java

/**
 * @ClassName ToIntegerDecoder
 * @Description 将字节解码为特定的格式，int字节流，从入站ByteBuf中读取每个int
 * @Author Huang Xiaoqiu
 * @Date 2019/2/25 15:28
 * @Version 1.0.0
 **/
public class ToIntegerDecoder extends ByteToMessageDecoder {

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    //检测是否至少4字节（一个int的字节长度）刻度，
    if (in.readableBytes() >= 4) {
      //ByteBuf中读取一个int,并添加到解码消息List中
        out.add(in.readInt());
    }
  }
}

```        
#### ReplayingDecoder 解码类
        - 1 如果没有足够字节可用，readInt()抛出Error,并在基类中捕获并处理
        - 2 当有更多的数据可供读取时，该decode()方法会被再次调用
        - 3 不是所有的ByteBuf操作可用
        - 4 ReplayingDecoder慢于ByteToMessageDecoder
        
```java

/**
 * @ClassName ToIntegerDecoder2
 * @Description 字节解码为消息
 * @Author Huang Xiaoqiu
 * @Date 2019/2/25 15:41
 * @Version 1.0.0
 **/
public class ToIntegerDecoder2 extends ReplayingDecoder {

  //ByteBuf 为 ReplayingDecoderByteBuf
  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    //从入站ByteBuf读取一个int,并添加到List
    // 1 如果没有足够字节可用，readInt()抛出Error,并在基类中捕获并处理
    // 2 当有更多的数据可供读取时，该decode()方法会被再次调用
    // 3 不是所有的ByteBuf操作可用
    // 4 ReplayingDecoder慢于ByteToMessageDecoder
    out.add(in.readInt());
  }
}

```        
    - 消息被编码或者解码后，会被自动释放
        - ReferenceCountUtil.release(message);
    - 保留引用以便稍后使用，防止释放
        - ReferenceCountUtil.retain(message)

#### io.netty.handler.codec.LineBasedFrameDecoder解码类
        - 内部netty使用，使用了行尾控制符（\n 或者 \r\n）解析消息数据
#### io.netty.handler.codec.http.HttpObjectDecoder解码类
        - http数据的解码器
         
    - io.netty.handler.codec子包下，更多的解码，编码器。
    
#### 消息类型转另外一个消息类型：MessageToMessageDecoder 解码类  
```java

public abstract class MessageToMessageDecoder<I> extends ChannelInboundHandlerAdapter {
  //...
}

```  
    - 例子
```java

/**
 * @ClassName IntegerToStringDecoder
 * @Description POJO类型转换为另一种POJO类型的解码器
 * @Author Huang Xiaoqiu
 * @Date 2019/2/25 15:59
 * @Version 1.0.0
 **/
public class IntegerToStringDecoder extends MessageToMessageDecoder {
  @Override
  protected void decode(ChannelHandlerContext ctx, Object msg, List out) throws Exception {
    out.add(String.valueOf(msg));
  }
}

```          

#### 更复杂的解码器：io.netty.handler.codec.http.HttpObjectAggregator类，
        扩展了MessageToMessageDecoder<HttpObject>
        
```java

public class HttpObjectAggregator
        extends MessageAggregator<HttpObject, HttpMessage, HttpContent, FullHttpMessage> {
  
        }
        
```        
               
 
#### TooLongFrameException类
- netty是异步框架，字节在解码之前要在内存缓冲，帧超出指定大小限制抛出异常
- 处理：1 http协议返回一个特殊响应，2 其他情况，关闭对应链接

```java

/**
 * @ClassName SafeByteToMessageDecoder
 * @Description 安全的解码器, 字节解码
 * @Author Huang Xiaoqiu
 * @Date 2019/2/25 16:16
 * @Version 1.0.0
 **/
public class SafeByteToMessageDecoder extends ByteToMessageDecoder {
  private static final int MAX_FRAME_SIZE = 1024;

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    int readable = in.readableBytes();
    //检查缓冲区是否有超过MAX_FRAME_SIZE个字节
    //帧大小溢出，可变帧大小的协议，这种保护措施特别重要。
    if (readable > MAX_FRAME_SIZE) {
      //挑过所有的可读字节，抛出TooLongFrameException，并通知ChnnalHandler
      in.skipBytes(readable);
      throw new TooLongFrameException("Frame too big");
    }
  }
}

```


### 编码器
#### 消息转字节: MessageToByteEncoder抽象类
- 消息转其他格式消息
- encode()方法，把输入的类型转为ByteBuf转发给ChannelPipeline的下一个ChannelOutboundHandler
```java

/**
 * @ClassName ShortToByteEncoder
 * @Description 解码器
 * @Author Huang Xiaoqiu
 * @Date 2019/2/26 08:04
 * @Version 1.0.0
 **/
public class ShortToByteEncoder extends MessageToByteEncoder<Short> {

  @Override
  public void encode(ChannelHandlerContext ctx, Short msg, ByteBuf out) throws Exception {
    out.writeShort(msg);
  }
}

```

#### 专门的编码器在，import io.netty.handler.codec.http.websocketx包中找到
#### MessageToMessageEncoder抽象类
- encode()方法

### 编解码器
#### ByteToMessageCodec抽象类同时处理ByteToMessageDecoder和MessageToByteEncoder
- 同一个类中管理出站和入站数据转换
- 编解码器同时实现ChannelInboundHandler和ChannelOutboundHandler接口
- decode(),decodeLast(),encode()

#### MessageToMessageCodec抽象类

```java
public abstract class MessageToMessageCodec<INBOUND_IN, OUTBOUND_IN> extends ChannelDuplexHandler {
  
}
```

## WebSocket协议
- 示例引用一个新的WebSocket协议，实现浏览器和服务器之前的
双向通信

```java

/**
 * @ClassName WebSocketConvertHandler
 * @Description
 * @Author Huang Xiaoqiu
 * @Date 2019/2/26 10:11
 * @Version 1.0.0
 **/
public class WebSocketConvertHandler extends MessageToMessageCodec<WebSocketFrame,
    WebSocketConvertHandler.MyWebSocketFrame> {


  //将MyWebSocketFrame转为指定的WebSocketFrame子类类型
  @Override
  protected void encode(ChannelHandlerContext ctx, MyWebSocketFrame msg, List<Object> out) throws Exception {
    ByteBuf payload = msg.getData().duplicate().retain();
    //实例化为WebSocketFrame的子类
    switch (msg.getType()) {
      case BINARY:
        out.add(new BinaryWebSocketFrame(payload));
        break;
      case TEXT:
        out.add(new TextWebSocketFrame(payload));
        break;
      case CLOASE:
        out.add(new CloseWebSocketFrame(true,0,payload));
        break;
      case CONTINUATION:
        out.add(new ContinuationWebSocketFrame(payload));
        break;
      case PONG:
        out.add(new PongWebSocketFrame(payload));
        break;
      case PING:
        out.add(new PingWebSocketFrame(payload));
        break;
      default:
        throw new IllegalAccessException("Unsuppoted websocket msg" + msg);

    }
  }

  // 将WebSocketFrame解码为MyWebSocketFrame，被设置FrameType
  @Override
  protected void decode(ChannelHandlerContext ctx, WebSocketFrame msg, List<Object> out) throws Exception {
    //保证引用不会被release
    ByteBuf payload = msg.content().duplicate().retain();
    Boolean flag = false;
    if (msg instanceof BinaryWebSocketFrame) {
      flag = true;
      out.add(new MyWebSocketFrame (MyWebSocketFrame.FrameType.BINARY,payload));
    }
    if (msg instanceof CloseWebSocketFrame) {
      flag = true;
      out.add(new MyWebSocketFrame (MyWebSocketFrame.FrameType.CLOASE,payload));
    }
    if (msg instanceof PingWebSocketFrame) {
      flag = true;
      out.add(new MyWebSocketFrame (MyWebSocketFrame.FrameType.PING,payload));
    }
    if (msg instanceof PongWebSocketFrame) {
      flag = true;
      out.add(new MyWebSocketFrame (MyWebSocketFrame.FrameType.PONG,payload));
    }
    if (msg instanceof TextWebSocketFrame) {
      flag = true;
      out.add(new MyWebSocketFrame (MyWebSocketFrame.FrameType.TEXT,payload));
    }
    if (msg instanceof ContinuationWebSocketFrame) {
      flag = true;
      out.add(new MyWebSocketFrame (MyWebSocketFrame.FrameType.CONTINUATION,payload));
    }
    if (flag == false) {
      throw new IllegalStateException("Unsupported web socket msg" + msg);
    }

  }

  public static final class MyWebSocketFrame {
    public enum FrameType {
      BINARY,
      CLOASE,
      PING,
      PONG,
      TEXT,
      CONTINUATION
    }
    private final FrameType type;
    private final ByteBuf data;

    public MyWebSocketFrame(FrameType type, ByteBuf data){
      this.type = type;
      this.data = data;
    }

    public FrameType getType() {
      return type;
    }

    public ByteBuf getData() {
      return data;
    }
  }
}

```

#### CombinedChannelDuplexHandler
- 充当容器，结合已经实现的编码器类，解码器类
- 不影响独立的编码器类， 解码器类的重用性

```java
public class CombinedChannelDuplexHandler<I extends ChannelInboundHandler, O extends ChannelOutboundHandler>
        extends ChannelDuplexHandler {
  
        }
```
- 例子

```java

/**
 * @ClassName CharToByteEncoder
 * @Description 字符到字节编码器
 * @Author Huang Xiaoqiu
 * @Date 2019/2/26 16:06
 * @Version 1.0.0
 **/
public class CharToByteEncoder extends MessageToByteEncoder<Character> {

  //将Character解码为char，并将其写入到出站ByteBuf中
  @Override
  protected void encode(ChannelHandlerContext ctx, Character msg, ByteBuf out) throws Exception {
    out.writeChar(msg);
  }

}

/**
 * @ClassName ByteToCharDecoder
 * @Description 字节到字符解码器
 * @Author Huang Xiaoqiu
 * @Date 2019/2/26 16:05
 * @Version 1.0.0
 **/
public class ByteToCharDecoder extends ByteToMessageDecoder {

  //将一个或多个Character对象添加到传出的List中
  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
      out.add(in.readChar());
  }
}

/**
 * @ClassName CombinedByteCharCodec
 * @Description 编码器，解码器的容器.灵活使用编码器和解码器
 * 通过该解码器和编码器实现参数化CombinedByteCharCodec
 * @Author Huang Xiaoqiu
 * @Date 2019/2/26 15:47
 * @Version 1.0.0
 **/
public class CombinedByteCharCodec extends CombinedChannelDuplexHandler<
    ByteToCharDecoder,CharToByteEncoder> {

  /**
   * 通过该解码器和编码器实现参数化CombinedByteCharCodec
    */
  public CombinedByteCharCodec(){
    super(new ByteToCharDecoder(),new CharToByteEncoder());
  }
}
```

## 预置的ChannelHandler和编码器
- 为许多协议提供了默认的编码器和解码器

### SSL/TLS保护Netty应用程序
- netty提供的是OpenSslEngine(更好，默认使用)
    - 通过SslHandler的ChannelHandler调用这个API
- JDK提供的是SSLEngine(上一个不可用，可以回退到调用这个)
- ChannelInitializer来将SslHandler添加到ChannelPipeline
- ChannelInitializer用于在Channel注册好时设置ChannelPipeline

### 构建基于Netty的HTTP/HTTPS的应用程序
- http/https
    - 请求结构 : HttpRequest, HttpContent, lastHttpContent
    - 响应结构 ： HttpResponse, HttpContent, lastHttpContent
####  编码解码器
    - HttpRequestEncoder
    - HttpResponseEncoder 
    - HttpRequestDecoder
    - HttpResponseDecoder
 
- 例子
```java

/**
 * @ClassName HttpPipelineInitializer
 * @Description 将ChannelHandler添加到ChannelPipeline
 * @Author Huang Xiaoqiu
 * @Date 2019/2/28 08:43
 * @Version 1.0.0
 **/
public class HttpPipelineInitializer extends ChannelInitializer<Channel> {
  private final boolean client;

  public HttpPipelineInitializer(boolean client) {
    this.client = client;
  }

  @Override
  protected void initChannel(Channel ch) throws Exception {
    ChannelPipeline pipeline = ch.pipeline();


    if (client) {
      //如果是客户端，加HttpResponseDecoder处理来自服务器的响应。
      pipeline.addLast("decoder",new HttpResponseDecoder());

      //如果是客户端，加HttpRequestEncoder以向服务器发送请求。
      pipeline.addLast("encoder",new HttpRequestEncoder());
    } else {
      //如果是服务端，加HttpRequestDecoder以接收来自客户端的请求。
      pipeline.addLast("decoder", new HttpRequestDecoder());
      //如果是服务端，加HttpResponseEncoder以向客户端发送响应。
      pipeline.addLast("encoder",new HttpResponseEncoder());
    }
  }
}

```   

####  聚合Http消息
- 消息是分段缓冲，直到可以转发一个完整的消息到下一个ChannelInboundHandler.
- 有一定开销，不用担心消息碎片
- 实现：在ChannelPipeline添加一个ChannelHandler

#### HTTP压缩
- 减少传输数据的大小，有一些开销
- 特别对于文本数据
- 提供了ChannelHandler实现，支持gzip, deflate编码
- 客户端可以通过提供头部信息指示服务器它所支持的压缩格式
GET /encrypted-area HTTP/1.1
Host: www.example.com
Accept-Encoding: gzip, delate
- 服务器并没有义务压缩它所发生的数据

- 例子：自动压缩HTTP消息
```java

/**
 * @ClassName HttpConpressionInitializer
 * @Description 添加自动压缩HTTP消息ChannelHandler
 * @Author Huang Xiaoqiu
 * @Date 2019/2/28 09:28
 * @Version 1.0.0
 **/
public class HttpConpressionInitializer extends ChannelInitializer<Channel> {
  private final boolean isClient;

  public HttpConpressionInitializer(boolean isClient) {
    this.isClient = isClient;
  }

  @Override
  protected void initChannel(Channel ch) throws Exception {
    ChannelPipeline pipeline = ch.pipeline();
    if (isClient) {
      //客户端，解压
      pipeline.addLast("codec", new HttpClientCodec());
      pipeline.addLast("decompressor",new HttpContentDecompressor());
    } else {
      //服务端，压缩数据
      pipeline.addLast("codex",new HttpServerCodec());
      pipeline.addLast("compressor",new HttpContentCompressor());
    }
  }
}

```
#### 使用httpsd
- 只需要将SslHandler添加ChannelPipeline的ChannelHandler组合中

- 例子
```java
/**
 * @ClassName HttpsCodecInitializer
 * @Description 使用https
 * @Author Huang Xiaoqiu
 * @Date 2019/2/28 09:38
 * @Version 1.0.0
 **/
public class HttpsCodecInitializer extends ChannelInitializer<Channel> {
  private final SslContext context;
  private final boolean isClient;

  public HttpsCodecInitializer(SslContext context, boolean isClient) {
    this.context = context;
    this.isClient = isClient;
  }


  @Override
  protected void initChannel(Channel ch) throws Exception {
    ChannelPipeline pipeline = ch.pipeline();
    SSLEngine engine = context.newEngine(ch.alloc());
    //将SslHandler添加到ChannelPipeline中使用HTTPS
    pipeline.addFirst("ssl", new SslHandler(engine));
    if (isClient) {
      pipeline.addLast("codec", new HttpClientCodec());
    } else {
      pipeline.addLast("codec", new HttpServerCodec());
    }
  }
}

```
#### webSocket
- HTTP是一直请求/响应的模式交互
- webSocket是实时发布消息，在一个单一的TCP链接上提供双向的通信，为
网页和远程服务器之间的双向通信提供了一种替代HTTP轮询的方案。
- 需要将适当的客户算或者服务器webSocket ChannelHandler添加到ChannelPipeline中
- handler处理的是由webSocket定义的称为帧的特殊消息类型
    
#####  WebSocketFrame类型
- BinaryWebSocketFrame : 数据帧：二进制数据
- TextWebSocketFrame : 数据帧：文本数据
- ContinuationWebSocketFrame : 数据帧：属于上一个BinaryWebSocketFrame或者TextWebSocketFrame的文本或者二进制数据
- CloseWebSocketFrame : 控制帧：一个CLOSE请求，关闭的状态码以及关闭的原因
- PingWebSocketFrame  控制帧：请求一个PongWebSocketFrame
- PongWebSocketFrame  控制帧：对PingWebSocketFrame请求的响应

- 例子：处理协议升级握手，以及3种控制帧Close, Ping, Pong.

```java

/**
 * @ClassName WebSocketServerInitializer
 * @Description webSocket协议
 * @Author Huang Xiaoqiu
 * @Date 2019/2/28 10:39
 * @Version 1.0.0
 **/
public class WebSocketServerInitializer extends ChannelInitializer<Channel> {

  @Override
  protected void initChannel(Channel ch) throws Exception {
    //为握手提供聚合的HttpRequest
    ch.pipeline().addLast(new HttpServerCodec(), new HttpObjectAggregator(655536),
        //如果请求的端点是"/websocket",则处理该升级握手
        new WebSocketServerProtocolHandler("/websocket"),
        //处理TextWebSocketFrame
        new TextFrameHandler(),
        //处理BinaryWebSocketFrame
        new BinaryFrameHandler(),
        //处理ContinuationWebSocketFrame
        new ContinuationFrameHandler());
  }

  /**
   *
   */
  public static final class TextFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
      //Handler text frame
    }
  }

  /**
   *
   */
  public static final class BinaryFrameHandler extends SimpleChannelInboundHandler<BinaryWebSocketFrame> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BinaryWebSocketFrame msg) throws Exception {
      //Handler Binary frame
    }
  }

  /**
   *
   */
  public static final class ContinuationFrameHandler extends SimpleChannelInboundHandler<ContinuationWebSocketFrame> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ContinuationWebSocketFrame msg) throws Exception {
      //Handle continuation frame
    }
  }
}

```

#### 保护WebSocket
- 为了 WebSocket添加安全性，只需要将SslHandler 作为第一个ChannelHandler添加到ChannelPipeline中

### 处理空闲的连接和超时
- 专门的ChannelHandler实现
- IdleStateHandler,使用最多。
    - 当链接空闲时间太长，将触发一个IdleStateEvent事件，然后通过你的ChannelInboundHandler中重写userEventTriggered()方法来处理
    该IdleStateEvent事件。
    
- ReadTimeoutHandler，在指定时间间隔内没有收到任何入站数据，则抛出一个ReadTimeoutException并关闭对应的Channel。可以重写ChannelHandler中的，exceptionCaught()方法，检测该异常。
- WriteTimeoutHandler，，在指定时间间隔内没有任何出站数据写入，则抛出一个WriteTimeoutException并关闭对应的Channel。可以重写ChannelHandler中的，exceptionCaught()方法，检测该异常。

```java

/**
 * @ClassName IdleStateHandlerInitializer
 * @Description 发送心跳消息到远程节点的方法，如果在60秒之内没有接收或者发送任何任何的数据，我们将得到通知，
 * 如果没有响应，则链接会被关闭
 * @Author Huang Xiaoqiu
 * @Date 2019/3/3 19:27
 * @Version 1.0.0
 **/
public class IdleStateHandlerInitializer extends ChannelInitializer<Channel> {

  @Override
  protected void initChannel(Channel ch) throws Exception {
    ChannelPipeline pipeline = ch.pipeline();
    //IdleStateHandler 将被触发时发送一个IdleStateEvent事件
    pipeline.addLast(new IdleStateHandler(0,0,60, TimeUnit.SECONDS));
    pipeline.addLast(new HeartbeatHandler());
  }

  /**
   * 实现userEventTriggered()方法以发送心跳消息,
   * 演示如何使用IdleStateHandler来测试远程节点是否仍然还活着，并在它失活时通过关闭连接来释放资源。
   */
  public static final class HeartbeatHandler extends ChannelInboundHandlerAdapter {
    //发送到远程节点的心跳消息
    private static final ByteBuf HEARTBEAT_SEQUENCE = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer(
        "HEARTBEAT", CharsetUtil.ISO_8859_1
    ));

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
      if (evt instanceof IdleStateEvent) {
        //发送心跳消息，并发送失败时关闭该链接
        ctx.writeAndFlush(HEARTBEAT_SEQUENCE.duplicate()).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
      } else {
        // 不是IdleStateEvent事件，所以将它传递给下一个ChannelInboundHandler
        super.userEventTriggered(ctx,evt);
      }
    }
  }
}

```

### 解码基于分隔符的协议和基于长度的协议
#### 基于分隔符的协议

#### 基于长度的协议

### 写大型数据
- 写操作是非阻塞的，所以即使没有写出所有的数据，写操作也会在完成时返回并通知ChannelFuture.
- 如果仍然不停写入，就有内存耗尽的风险。
- 所以在写大数据时，需要准备好处理到远程节点的链接是慢速链接的情况，这种情况会导致内存释放的延迟。
- 考虑下将一个文件内容写出到网络的情况

- 



           