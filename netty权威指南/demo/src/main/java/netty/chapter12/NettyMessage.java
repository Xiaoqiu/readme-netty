package netty.chapter12;

import lombok.Data;

/**
 * @author kate
 * @create 2019/5/24
 * @since 1.0.0
 */
@Data
public final class NettyMessage {
  // 消息头
  private Header header;
  // 消息体
  private Object body;

  @Override
  public String toString() {
    return "NettyMessage [header=" + header + "]" ;
  }
}