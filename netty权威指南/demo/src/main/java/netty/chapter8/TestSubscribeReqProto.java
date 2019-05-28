package netty.chapter8;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kate
 * @create 2019/5/28
 * @since 1.0.0
 */
public class TestSubscribeReqProto {
  private static byte[] encode(SubscribeReqProto.SubscribeReq req) {
    return req.toByteArray();
  }
  private static SubscribeReqProto.SubscribeReq decode (byte[] body) throws InvalidProtocolBufferException {
    return SubscribeReqProto.SubscribeReq.parseFrom(body);
  }

  private static SubscribeReqProto.SubscribeReq createSubscribeReq() {
    SubscribeReqProto.SubscribeReq.Builder builder = SubscribeReqProto.SubscribeReq
        .newBuilder();
    builder.setSubReqID(1);
    builder.setUserName("kate");
    builder.setProductName("book");
    builder.setAddress("shenzhen");
    return builder.build();
  }

  public static void main(String[] args) throws InvalidProtocolBufferException{
    SubscribeReqProto.SubscribeReq req = createSubscribeReq();
    System.out.println("Before encode : " + req.toString());
    SubscribeReqProto.SubscribeReq req2 = decode(encode(req));
    System.out.println("After decode : " + req2.toString());
    System.out.println("Assert equal : ---> " + req2.equals(req));
  }
}