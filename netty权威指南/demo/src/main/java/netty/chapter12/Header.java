package netty.chapter12;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kate
 * @create 2019/5/24
 * @since 1.0.0
 */
@Data
public final class Header {
  private int crcCode = 0xabef0101;
  // 消息长度
  private int length;
  // 会话ID
  private long sessionID;
  // 消息类型
  private byte type;
  //消息优先级
  private byte priority;
  // 附件
  private Map<String,Object> attachment = new HashMap<>();

  @Override
  public String toString(){
    return "Header [crcCode=" + crcCode + ", length=" + length
        + ", sessionID=" + sessionID + ", type" + type + ", priority="
        + priority + ", attachment=" + attachment + "]";
  }

}