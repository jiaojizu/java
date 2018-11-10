package chat;

import com.buffer.Buffers;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class SendMsgServer {
    static Logger LOG = Logger.getLogger(SendMsgServer.class);

    public static void sendMsg(User user, Map<String, Object> message, ByteBuffer sourceWriter) {
        Selector selector = null;
        Charset utf8 = Charset.forName("UTF-8");
        try {
            selector = Selector.open();
            SocketChannel socket = SocketChannel.open();
            socket.configureBlocking(false);
            socket.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, new Buffers(256, 256));
            socket.connect(new InetSocketAddress(user.getIp(), user.getProt()));
            while (!socket.finishConnect()) {
            }
            LOG.info("连接目标客户端成功！");
            while (true){
                selector.select();//阻塞等待处理任务
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();//当前处理通道
                    try {
                        iterator.remove();//删除已处理通道

                        /*获取注册通道时候时候定义的缓冲区*/
                        Buffers buffers = (Buffers) key.attachment();
                        ByteBuffer readBuffer = buffers.getReadBuffer();
                        ByteBuffer writeBuffer = buffers.gerWriteBuffer();
                        SocketChannel socketChannel = (SocketChannel) key.channel();

                        //如果写缓冲区可写
                        if (key.isWritable()) {
                            writeBuffer.put(("【" + message.get("sourceName") + "】:" + message.get("msg")).getBytes("UTF-8"));
                            writeBuffer.flip();
                            socketChannel.write(writeBuffer);
                            writeBuffer.clear();
                            key.interestOps(key.interestOps()&(~SelectionKey.OP_WRITE));
                        }

                        //如果是可读channel，并且读缓存区后可读数据
                        if (key.isReadable()) {
                            socket.read(readBuffer);
                            readBuffer.flip();
                            CharBuffer decode = utf8.decode(readBuffer);
                            Map<String, Object> result = new JSONObject(decode.toString()).toMap();
                            readBuffer.clear();//清空读缓冲区
                            key.interestOps(key.interestOps()&(~SelectionKey.OP_READ));
                            //处理客户端回执消息
                            if (result.containsKey("code") && "200".equals(result.get("code"))) {
                                sourceWriter.put((user.getUserName() + "消息接受成功！").getBytes("UTF-8"));
                            } else {
                                sourceWriter.put((user.getUserName() + "消息接受异常！").getBytes("UTF-8"));
                            }
                            return;
                        }
                    } catch (Exception e) {
                        key.cancel();
                        key.channel().close();
                        LOG.error("本次消息处理失败",e);
                    }

                }
            }



        } catch (IOException e) {
            try {
                sourceWriter.put((user.getUserName() + "目标客户端异常！").getBytes("UTF-8"));
            } catch (IOException e1) {
                LOG.error("链接客户端异常！" + user.getProt(), e);
            }
            LOG.error("链接客户端异常！", e);
        } finally {
            try {
                selector.close();
            } catch (IOException e) {
                LOG.error("关闭本次请求失败", e);
            }
        }
    }
}
