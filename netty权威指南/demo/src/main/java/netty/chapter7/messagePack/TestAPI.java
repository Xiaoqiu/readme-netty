package netty.chapter7.messagePack;

import org.msgpack.MessagePack;

import javax.xml.transform.Templates;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kate
 * @create 2019/5/27
 * @since 1.0.0
 */
public class TestAPI {
  public static void mian(String[] args) throws IOException {
    // 创建被序列化的对象
    List<String> src = new ArrayList<>();
    src.add("kate");
    src.add("hello");
    src.add("me");
    MessagePack messagePack = new MessagePack();
    // 序列化
    byte[] raw = messagePack.write(src);
    // 反序列化，使用一个template
//    List<String> dst1 = messagePack.read(raw, Templates.tList(Templates.TString));
//    System.out.println(dst1.get(0));
//    System.out.println(dst1.get(1));
//    System.out.println(dst1.get(2));
  }
}