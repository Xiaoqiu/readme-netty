## NIO时间服务器
```java
// 启动类
public class TimeServer {
	public static void main(String[] args) throws IOException {
			int port = 8080;
			if (args != null && args.length > 0) {
				try {
					port = Integer.valueOf(args[0]);
				} catch (NumberFormatException e) {

				}
			}

			MultiplexerTimeServer timeServer = new MultiplexerTimeServer(port);
			New Thread(timeServer, "NIO-MultiplexerTimeServer-001").start();
	}
}

// 主要业务类
public class MultiplexerTimeServer implements Runnable {
	private Selector selector;
	private ServerSocketChannel servChannel;
	private volatile boolean stop;

	//初始化多路复用器，绑定监听端口
	public MultiplexerTimeServer(int port) {
		try {
			selector = Selector.open();
			servChannel = ServerSocketChannel.open();
			servChannel.configureBlocking(false);
			servChannel.socket().bind

		}
	}
}

```
