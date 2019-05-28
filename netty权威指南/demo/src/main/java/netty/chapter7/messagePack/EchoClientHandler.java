package netty.chapter7.messagePack;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.jboss.marshalling.TraceInformation;

/**
 * @author kate
 * @create 2019/5/27
 * @since 1.0.0
 */
public class EchoClientHandler extends ChannelHandlerAdapter {
  private final int sendNumer;

  public EchoClientHandler(int sendNumer) {
    this.sendNumer = sendNumer;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    System.out.println("channelActive");
    UserInfo[] infos = getUserInfo();
    System.out.println("infos :" +  infos.length);
    for (UserInfo infoE : infos) {

      System.out.println("infos :" +   infoE.toString());
      ctx.write(infoE);
    }
    ctx.flush();
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    System.out.println("channelRead");
    System.out.println("Client receive the msgpack message : " + msg);
    ctx.write(msg);
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    System.out.println("channelReadComplete");
    ctx.flush();
  }

  private UserInfo[] getUserInfo(){
    UserInfo[] userInfos = new UserInfo[sendNumer];
    UserInfo userInfo = null;
    for (int i = 0; i < sendNumer; i++) {
      userInfo = new UserInfo();
      userInfo.setAge(i);
      userInfo.setName("kate" + i);
      userInfos[i] = userInfo;
    }
    System.out.println("DONE");
    return userInfos;
  }
}