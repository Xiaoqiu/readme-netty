chapter 4 
# TCP粘包/拆包问题的解决
- TCP粘包、拆包基础知识
- 
- 使用netty解决读半包问题

### 4.3.4 LineBaseFrameDecoder 和 StringDecoder原理分析
- LineBaseFrameDecoder
    - 以换行符为结束标志的解码器：\n \r\n
    - 支持配置单行的最大长度。
- StringDecoder
    - 接到的对象转换位字符串

- LineBaseFrameDecoder + StringDecoder
    - 是安装行切换的文本解码器，设计来支持TCP粘包和拆包
    
- chapter5 ： 分隔符解码器

    
