package client2;

import buffer.Buffers;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import static client2.Client2Start.CHAT_RECORD;

/**
 * 请求分发器
 */
public class DispatcherServlet2 {
    static Logger LOG = Logger.getLogger(DispatcherServlet2.class);
    static Charset utf8 = Charset.forName("UTF-8");

    public static void handleRequest(SelectionKey key) {
        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            Buffers buffers = (Buffers) key.attachment();
            ByteBuffer clientReader = buffers.getReadBuffer();
            ByteBuffer clientWriter = buffers.getReadBuffer();
            //处理客户端消息
            socketChannel.read(clientReader);
            clientReader.flip();
            CharBuffer decode = utf8.decode(clientReader);
            String clientMsg = decode.toString();
            clientReader.clear();
            CHAT_RECORD.setText(CHAT_RECORD.getText().replace("</html>","<p>"+clientMsg.toString()+"</p></html>"));

            String result = "{\"code\":\"200\"}";
            clientWriter.put(result.getBytes("UTF-8"));
            clientWriter.flip();
            int len = 0;
            while (clientWriter.hasRemaining()){
                socketChannel.write(clientWriter);
                /*说明底层的socket写缓冲已满*/
                if(len == 0){
                    break;
                }
            }
            clientWriter.clear();
            if(len != 0){
                /*取消通道的写事件*/
                key.interestOps(key.interestOps() & (~SelectionKey.OP_WRITE));
            }
        } catch (IOException e) {
            key.cancel();
            try {
                key.channel().close();
            } catch (IOException e1) {
                LOG.error("关闭通道失败",e1);
            }
            LOG.error("读取消息失败", e);
        }
    }
}
