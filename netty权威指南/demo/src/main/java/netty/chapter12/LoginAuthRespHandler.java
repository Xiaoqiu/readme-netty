package netty.chapter12;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务端的握手接入和安全认证代码，
 * @author kate
 * @create 2019/5/24
 * @since 1.0.0
 */
public class LoginAuthRespHandler extends ChannelHandlerAdapter {
    private Map<String,Boolean> nodeCheck = new ConcurrentHashMap<>();

    private String[] whiteList = {"127.0.0.1","192.168.1.104"};

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyMessage message = (NettyMessage)msg;

        // 如果是握手请求消息，处理，其他消息传递给其他ChannelHandler
        if(message.getHeader() != null
        && message.getHeader().getType() == MessageType.LOGIN_RESP.value()) {
            String nodeIndex = ctx.channel().remoteAddress().toString();
            NettyMessage loginResp = null;
            // 重复登录，拒绝
            
        }
    }
}