package netty.chapter2;
/**
 * @author kate
 * @create 2019/5/17
 * @since 1.0.0
 */

import java.io.IOException;

/**
 * @ClassName TimeServer
 * @Description NIO服务端启动类
 * @Author Huang Xiaoqiu
 * @Date 2019/5/17 15:07
 * @Version 1.0.0
 **/
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
    Thread thread = new Thread(timeServer,"NIO-MultiplexerTimeServer-001");
    thread.start();
  }
}