chapter5 分隔符和定长解码器的应用
上次协议为了对消息进行区分4中方式：
- 消息长度固定
- 回车换行符作为消息结束符，文本协议使用广泛
- 将特殊的分隔符作为消息的结束标志。
- 通过消息头中定义长度字段来标识消息的总长度

- DelimiterBasedFrameDecoder
    - 分隔符做结束标志的消息解码
- FixedLengthFrameDecoder
    - 定长消息的解码

- 两者都可以解决TCP粘包/拆包导致的读半包问题

- DelimiterBaseFrameDecoder服务端开发
- DelimiterBaseFrameDecoder客户端开发
- 运行DelimiterBaseFrameDecoder服务端和客户端
- FixedLengthFrameDecoder服务端开发
- 通过telnet命令行调试FixedLengthFrameDecoder服务端


## 5.1DelimiterBaseFrameDecoder应用开发
- Echo服务例子，
- EchoServer打印消息，原始消息返回客户端
- 消息以$_作为分隔符

```java
 protected void initChannel(SocketChannel ch) throws Exception {
              ByteBuf delimiter = Unpooled.copiedBuffer("$_".getBytes());
              // 已经经过DelimiterBaseFrameDecoder解码，是一个完整的消息包。
              ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1024,delimiter));
              // 经过StringDecoder解码，是字符串对象。
              ch.pipeline().addLast(new StringDecoder());
              // 这个自定义处理器，接收到的msg消息是解码后的字符串对象。
              ch.pipeline().addLast(new EchoServerHandler());
            }
```

## 5.2 FixedLengthFrameDecoder应用开发
- 服务端直接打印消息
- 无论一次接收到多少数据报，它都会按照构造函数中设置的固定
长度进行解码。如果半包消息，会缓存半包消息并等待下个包到达后进行拼包，直到读取一个完整的包。
- telnet 测试
```bash
telnet localhsot 8080
hi, kate, welcom to heeeeee


```
- 后续
- 编解码技术
- 内置的编解码框架
- Java序列化
- 二进制编解码
- 谷歌protobuf编解码
- JBoss的Marshalling编解码





















