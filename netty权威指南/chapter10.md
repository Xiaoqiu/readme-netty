高级编
chapter10 http协议开发应用
chapter11 webSocket协议开发
chapter12 UDP协议开发
chapter13 文件传输
chapter14 私有协议栈开发



chapter10 http协议开发应用
- HTTP协议介绍
- Netty HTT服务端入门开发
- HTTP + XML 应用开发
- HTTP 附件处理

## 10.1 HTTP协议介绍
- 应用层，面向对象的协议
- 无状态
### 10.1.2 HTTP请求消息
- 组成：
    - HTTP请求行: 第一行
    - HTTP消息头
    - HTTP请求正文
    
- HTTP请求行: 第一行
    - 格式：Method RequesCRLFt-URI HTTP-Version CRLF
    - CRLF ： 表示回车和换行
        
```bash
GET /netty5.0 HTTP/1.1
```

- HTTP消息头
    - Accept : 指定客户端接收哪些类型的消息
    - Accept-Charset ： 客户端接收的字符集
    - Accept-Encoding ： 客户端可接收的内容编码（与Accept类似）
    - Accept-Language ： 客户端可以接受的自然语言，如中文 （与Accept类似）
    - Authorization ： 客户端有权查看某个资源，如果服务端响应为401，可以发送一个包含Authorization的请求报告域的请求。要求服务器对其进行认证。
    - Host ： 必须的，指定被请求的资源的主机和端口。通常从HTTP的URL中提取。
    - User-Agent ： 客户端的操作系统，浏览器，其他属性告诉服务器 
    - Content-Length ： 请求消息体的长度
    - Content-Type ： 表示后面的文档属于什么类型MIME类型
    - Connection ： 连接类型
    
 ### 10.1.3 HTTP消息响应消息
 - 组成：
    - 状态行
    - 消息报头
    - 响应正文   
- 状态行：
    - 格式： HTTP-Version Status-Code Reason-Phrase CRLF

- 消息报头  
    - Location ： 响应报头域用来重定向接受者到一个新的位置。
    - Server ： 服务器用来处理请求的软件信息
    - WWW-Authenticate ：客户端发送Authorizatoin报头，服务器响应报头就包含这个域。
    
## 10.2 Netty HTTP 服务端入门开发
- 异步事件驱动
- 异步非阻塞

## 10.2.1 Netty HTTP 服务端例程场景描述
- 文件服务器使用http协议提供服务
- 当客户端通过浏览器访问文件服务器，对访问路径进行检查，
- 检查失败，返回403，
- 校验通过，以链接的方式打开当前文件目录，每个目录或者文件都是一个超链接，可以递归访问。
- 如果是目录，可以递归访问子目录，或文件
- 如果是文件且可读，则可以在浏览器直接打开。或者下载。

```java
// todo : HTTP文件服务器 启动类HttpFileServer
// todo:  HTTP文件服务器 处理类HttpFileServerHandler
```

## 10.3 Netty Http + XML 协议栈开发
- 利用netty提供的基础HTTP协议栈功能，扩展开发http+xml协议栈

### 10.3.1 开发场景介绍

























