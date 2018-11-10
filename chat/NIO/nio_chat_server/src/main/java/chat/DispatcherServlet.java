package chat;

import com.buffer.Buffers;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求分发器
 */
public class DispatcherServlet {
    static Logger LOG = Logger.getLogger(DispatcherServlet.class);

    public static Map<String, User> ONLINE_USER = new HashMap<>();//在线用户
    static Charset utf8 = Charset.forName("UTF-8");

    public static void handleRequest(SelectionKey key) throws IOException {
        /*获取注册通道时提供的缓冲区*/
        Buffers buffers = (Buffers) key.attachment();
        ByteBuffer readBuffer = buffers.getReadBuffer();
        ByteBuffer writeBuffer = buffers.gerWriteBuffer();

        //获取读通道
        SocketChannel channel = (SocketChannel) key.channel();
        //将通道中的数据读入到缓冲区
        channel.read(readBuffer);
        readBuffer.flip();
        //消息解码
        CharBuffer msgBuffer = utf8.decode(readBuffer);
        readBuffer.clear();
        Map<String, Object> message = new JSONObject(msgBuffer.toString()).toMap();
//                        readBuffer.rewind();//改变缓冲区指针，让其可以重新读取
        HandleProgram handleProgram = new HandleProgram();

        if (!message.containsKey("method")) {
            writeBuffer.put("非法的请求！".getBytes("UTF-8"));
            return;
        }

        if ("regUser".equals(message.get("method"))) {//注册用户
            handleProgram.regUser(message, writeBuffer);
        } else if ("sendMsg".equals(message.get("method"))) {//发送消息
            handleProgram.sendMsg(message, writeBuffer);
        } else if ("loginout".equals(message.get("method"))){//用户登出
            handleProgram.loginout(message, writeBuffer);
        }else {
            writeBuffer.put("请求的方法不存在！".getBytes("UTF-8"));
            return;
        }


        /*设置通道写事件*/
        key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
    }

    public static void handleResponse(SelectionKey key) throws IOException {
        //获取注册通道时提供的缓冲区
        Buffers  buffers = (Buffers)key.attachment();
        ByteBuffer writeBuffer = buffers.gerWriteBuffer();
        writeBuffer.flip();
        SocketChannel socketChannel = (SocketChannel) key.channel();
        int len = 0;
        //若缓冲区中无可读字节，则说明成功发送给服务器消息
        while(writeBuffer.hasRemaining()){
            len = socketChannel.write(writeBuffer);
            /*说明底层的socket写缓冲已满*/
            if(len == 0){
                break;
            }
        }
        writeBuffer.compact();
        /*说明数据全部写入到底层的socket写缓冲区*/
        if(len != 0){
            /*取消通道的写事件*/
            key.interestOps(key.interestOps() & (~SelectionKey.OP_WRITE));
        }
    }
}
