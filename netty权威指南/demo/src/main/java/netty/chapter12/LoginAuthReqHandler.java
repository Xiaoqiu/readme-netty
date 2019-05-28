package netty.chapter12;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.awt.*;

/**
 *  客户端握手和安全认证
 *  握手消息的接入，和安全认证在服务端处理，
 *  握手认证的客户端channelHandler,用于在通道激活时，发起握手请求。
 * @author kate
 * @create 2019/5/24
 * @since 1.0.0
 */
public class LoginAuthReqHandler extends ChannelHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(buildLoginReq());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // TCP三次握手成功后，客户端构造登录请求
        NettyMessage message = (NettyMessage)msg;

        // 如果握手消息得到服务端的应答，需要判断是否认证成功
        if (message.getHeader() != null
                && message.getHeader().getType() == MessageType.LOGIN_RESP.value()) {
            byte loginResult = (byte)message.getBody();
            if (loginResult != (byte)0) {
                //  握手失败关闭连接，重新发起连接。
                ctx.close();
            } else {
                System.out.println("Login is ok : " + message);
                ctx.fireChannelRead(msg);
            }
        } else {
            // 如果不是握手的应答消息，直接传递给后面的channelHandler进行处理。
                ctx.fireChannelRead(msg);
        }
    }

    /**
     * 构造登录请求，由于采用IP白名单认证机制，因此，不需要携带消息体，消息体为空。
     * 消息类型为：3， 握手请求消息。
     * @return
     */
    private NettyMessage buildLoginReq() {
        NettyMessage message = new NettyMessage();
        Header header = new Header();
        header.setType(MessageType.LOGIN_REQ.value());
        message.setHeader(header);
        return message;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.fireExceptionCaught(cause);
    }
}
