package netty.chapter4.badTest;
/**
 * @author kate
 * @create 2019/5/20
 * @since 1.0.0
 */

/**
 * @ClassName Test
 * @Description
 * @Author Huang Xiaoqiu
 * @Date 2019/5/20 17:06
 * @Version 1.0.0
 **/
public class Test {
  public static void main(String[] args) {
   // req = ("QUERY TIME ORDER" + System.getProperty("line.separator")).getBytes();
    System.out.println("QUERY TIME ORDER" + System.getProperty("line.separator"));
    System.out.println(System.getProperty("line.separator").length());
    //String body = new String(req,"UTF-8").substring(0,req.length - System.getProperty("line.separator").length());

  }
}