package netty.chapter12;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务端的握手接入和安全认证代码，
 * @author kate
 * @create 2019/5/24
 * @since 1.0.0
 */
public class LoginAuthRespHandler extends ChannelHandlerAdapter {
    // 存储登录成功的IP，IP地址作为key,
    private Map<String,Boolean> nodeCheck = new ConcurrentHashMap<>();

    private String[] whiteList = {"127.0.0.1","192.168.1.104"};

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyMessage message = (NettyMessage)msg;

        // 如果是握手请求消息，处理，其他消息传递给其他ChannelHandler
        if (message.getHeader() != null
        && message.getHeader().getType() == MessageType.LOGIN_REQ.value()) {
            // 获取登录的IP转化为字符串
            String nodeIndex = ctx.channel().remoteAddress().toString();
            NettyMessage loginResp = null;
            // 重复登录，拒绝
            if (nodeCheck.containsKey(nodeIndex)) {
                loginResp = buildResponse((byte) -1);
            } else {
                // 如果没有重复登录，需要检查这个
                InetSocketAddress address = (InetSocketAddress) ctx.channel()
                    .remoteAddress();
                String ip = address.getAddress().getHostAddress();
                boolean isOk = false;
                // 判断IP在白名单
                for (String WIP : whiteList) {
                    if (WIP.equals(ip)) {
                        isOk = true;
                        break;
                    }
                }
                // 如果在白名单里面返回0， 否则返回-1
                loginResp = isOk ? buildResponse((byte)0) : buildResponse((byte)-1);
                // 如果在白名单里面，登录成功，把IP加入到内存里面，用来判断重复登录。
                if (isOk) {
                    nodeCheck.put(nodeIndex,true);
                }
                System.out.println("The login response is : " + loginResp
                + "body[ " + loginResp.getBody() + " ]");
                // 返回登录响应
                ctx.writeAndFlush(loginResp);
            }
        } else {
            // 如果不是登录请求，把请求发送到下一个handler
            ctx.fireChannelRead(msg);
        }
    }

    private NettyMessage buildResponse(byte result) {
        NettyMessage message = new NettyMessage();
        Header header = new Header();
        header.setType(MessageType.LOGIN_RESP.value());
        message.setHeader(header);
        message.setBody(result);
        return message;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 删除这个登录IP的缓存。
        nodeCheck.remove(ctx.channel().remoteAddress().toString());
        ctx.close();
        ctx.fireExceptionCaught(cause);
    }
}