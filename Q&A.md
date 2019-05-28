> https://segmentfault.com/a/1190000012976683


### 
1 IO，NIO，AIO区别
IO 阻塞同步通信模式，客户端和服务器连接需要三次握手，使用简单，但吞吐量小
NIO 非阻塞同步通信模式，客户端与服务器通过Channel连接，采用多路复用器轮询注册的Channel。提高吞吐量和可靠性。
AIO 非阻塞异步通信模式，NIO的升级版，采用异步通道实现异步通信，其read和write方法均是异步方法。

2 Stock通信的伪代码实现流程
服务器绑定端口：server = new ServerSocket(PORT)
服务器阻塞监听：socket = server.accept()
服务器开启线程：new Thread(Handle handle)
服务器读写数据：BufferedReader PrintWriter 
客户端绑定IP和PORT：new Socket(IP_ADDRESS, PORT)
客户端传输接收数据：BufferedReader PrintWriter

3 TCP协议与UDP协议有什么区别
TCP : 传输控制协议是基于连接的协议，在正式收发数据前，必须和对方建立可靠的连接。速度慢，合适传输大量数据。
UDP : 用户数据报协议是与TCP相对应的协议。面向非连接的协议，不与对方建立连接，而是直接就把数据包发送过去，速度快，适合传输少量数据。

4 什么是同步阻塞BIO，同步非阻塞NIO，异步非阻塞AIO
同步阻塞IO : 用户进程发起一个IO操作以后，必须等待IO操作的真正完成后，才能继续运行。
同步非阻塞IO: 用户进程发起一个IO操作以后，可做其它事情，但用户进程需要经常询问IO操作是否完成，这样造成不必要的CPU资源浪费。
异步非阻塞IO: 用户进程发起一个IO操作然后，立即返回，等IO操作真正的完成以后，应用程序会得到IO操作完成的通知。类比Future模式。

### 总结
1 BIO模型中通过Socket和ServerSocket完成套接字通道实现。阻塞，同步，连接耗时。
2 NIO模型中通过SocketChannel和ServerSocketChannel完成套接字通道实现。非阻塞/阻塞，同步，避免TCP建立连接使用三次握手带来的开销。
3 AIO模型中通过AsynchronousSocketChannel和AsynchronousServerSocketChannel完成套接字通道实现。非阻塞，异步。

## AIO
AIO 也叫NIO2.0 是一种非阻塞异步的通信模式。在NIO的基础上引入了新的异步通道的概念，并提供了异步文件通道和异步套接字通道的实现。
AIO 并没有采用NIO的多路复用器，而是使用异步通道的概念。其read，write方法的返回类型都是Future对象。而Future模型是异步的，其核心思想是：去主函数等待时间。

小结：AIO模型中通过AsynchronousSocketChannel和AsynchronousServerSocketChannel完成套接字通道的实现。非阻塞，异步。

AIO服务端代码，负责创建服务器通道，绑定端口，等待请求。

## NIO
NIO 全称New IO，也叫Non-Block IO 是一种非阻塞同步的通信模式。
NIO 设计原理：
NIO 相对于BIO来说一大进步。客户端和服务器之间通过Channel通信。NIO可以在Channel进行读写操作。这些Channel都会被注册在Selector多路复用器上。Selector通过一个线程不停的轮询这些Channel。找出已经准备就绪的Channel执行IO操作。
NIO 通过一个线程轮询，实现千万个客户端的请求，这就是非阻塞NIO的特点。
1）缓冲区Buffer：它是NIO与BIO的一个重要区别。BIO是将数据直接写入或读取到Stream对象中。而NIO的数据操作都是在缓冲区中进行的。缓冲区实际上是一个数组。Buffer最常见的类型是ByteBuffer，另外还有CharBuffer，ShortBuffer，IntBuffer，LongBuffer，FloatBuffer，DoubleBuffer。
2）通道Channel：和流不同，通道是双向的。NIO可以通过Channel进行数据的读，写和同时读写操作。通道分为两大类：一类是网络读写（SelectableChannel），一类是用于文件操作（FileChannel），我们使用的SocketChannel和ServerSocketChannel都是SelectableChannel的子类。
3）多路复用器Selector：NIO编程的基础。多路复用器提供选择已经就绪的任务的能力。就是Selector会不断地轮询注册在其上的通道（Channel），如果某个通道处于就绪状态，会被Selector轮询出来，然后通过SelectionKey可以取得就绪的Channel集合，从而进行后续的IO操作。服务器端只要提供一个线程负责Selector的轮询，就可以接入成千上万个客户端，这就是JDK NIO库的巨大进步。

说明：这里的代码只实现了客户端发送请求，服务端接收数据的功能。其目的是简化代码，方便理解。github源码中有完整代码。
小结：NIO模型中通过SocketChannel和ServerSocketChannel完成套接字通道的实现。非阻塞/阻塞，同步，避免TCP建立连接使用三次握手带来的开销。


## BIO
BIO 全称Block-IO 是一种阻塞同步的通信模式。我们常说的Stock IO 一般指的是BIO。是一个比较传统的通信方式，模式简单，使用方便。但并发处理能力低，通信耗时，依赖网速。
BIO 设计原理：
服务器通过一个Acceptor线程负责监听客户端请求和为每个客户端创建一个新的线程进行链路处理。典型的一请求一应答模式。若客户端数量增多，频繁地创建和销毁线程会给服务器打开很大的压力。后改良为用线程池的方式代替新增线程，被称为伪异步IO。

服务器提供IP地址和监听的端口，客户端通过TCP的三次握手与服务器连接，连接成功后，双放才能通过套接字(Stock)通信。
小结：BIO模型中通过Socket和ServerSocket完成套接字通道的实现。阻塞，同步，建立连接耗时。


## netty用到的设计模式
                                     
## netty设计缺陷
                                              