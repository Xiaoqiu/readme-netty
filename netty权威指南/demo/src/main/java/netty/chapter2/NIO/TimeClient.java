package netty.chapter2;
/**
 * @author kate
 * @create 2019/5/20
 * @since 1.0.0
 */

import java.io.IOException;

/**
 *  NIO创建的TimeClient
 * @ClassName TimeClient
 * @Description
 * @Author Huang Xiaoqiu
 * @Date 2019/5/20 09:01
 * @Version 1.0.0
 **/
public class TimeClient {
  public static void main(String[] args) throws IOException {
    int port = 8080;
    if (args != null && args.length > 0) {
      try {
        port = Integer.valueOf(args[0]);
      } catch (NumberFormatException e) {
        // 采用默认值
      }
    }
    // 创建线程来处理异步链接和读写操作
    Thread thread = new Thread(new TimeClientHandler("127.0.0.1",port),"TimeClient-001");
    thread.start();
  }

}