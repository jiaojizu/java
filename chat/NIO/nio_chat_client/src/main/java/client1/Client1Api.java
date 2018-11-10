package client1;

import buffer.Buffers;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

import static client1.Client1Start.CHAT_RECORD;

public class Client1Api {
    static Logger LOG = Logger.getLogger(Client1Api.class);

    static Selector selector = null;
    static SocketChannel scoketChannel = null;
    static ByteBuffer readBuffer = null;
    static ByteBuffer writeBuffer = null;

    static int SERVER_PROT = 8888;
    static String SERVER_IP = "127.0.0.1";
    static Charset utf8 = Charset.forName("UTF-8");

    /**
     * 注册用户
     */
    public static void regUser() {
        String msg = "{\"method\":\"regUser\",\"userName\":\"client_1\",\"ip\":\"127.0.0.1\",\"prot\":\"8801\"}";
        sendMsgToServer(msg, "log");
    }

    /**
     * 发送给服务端数据
     *
     * @return
     * @throws IOException
     */
    public static void sendData(String msg) throws IOException {
        writeBuffer.put(msg.getBytes("UTF-8"));
        writeBuffer.flip();
        int len = 0;
        while (writeBuffer.hasRemaining()) {
            len = scoketChannel.write(writeBuffer);
            if (len < 1) break;
        }
        writeBuffer.clear();
    }

    /**
     * 获取服务端返回数据
     *
     * @return
     * @throws IOException
     */
    public static String getMessage() throws IOException {
        scoketChannel.read(readBuffer);
        readBuffer.flip();
        String msg = "";
        while (readBuffer.hasRemaining()) {
            CharBuffer decode = utf8.decode(readBuffer);
            msg += decode.toString();
        }
        readBuffer.clear();
        return msg;// 显示收到消息
    }

    /**
     * 建立socket链接，发送消息给服务器
     *
     * @throws IOException
     */
    public static void sendMsgToServer(String msg, String model) {
        try {
            selector = Selector.open();
            SocketChannel sc = SocketChannel.open();
            sc.configureBlocking(false);
            sc.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ, new Buffers(256, 256));
            sc.connect(new InetSocketAddress(SERVER_IP, SERVER_PROT));
            while (!sc.finishConnect()) {//阻塞等待三次握手完成

            }
            LOG.info("链接服务器成功!");
            while (true) {
                selector.select();//阻塞等待请求链接
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    try {//处理失败可以继续处理其他channal
                        iterator.remove();//删除已处理请求
                        Buffers buffers = (Buffers) key.attachment();
                        readBuffer = buffers.getReadBuffer();
                        writeBuffer = buffers.getWriteBuffer();
                        scoketChannel = (SocketChannel) key.channel();
                        if (key.isWritable()) {
                            sendData(msg);
                            key.interestOps(key.interestOps() & (~SelectionKey.OP_WRITE));
                        }
                        if (key.isReadable()) {
                            //显示客户端返回消息
                            if ("chat".equals(model)) {//聊天记录
                                CHAT_RECORD.setText(CHAT_RECORD.getText().replace("</html>", "<p><font size='2' color='green'>" + getMessage() + "</font></p></html>"));
                            } else {
                                LOG.info(getMessage());
                            }
                            key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));
                            return;
                        }
                    } catch (IOException e) {
                        key.cancel();
                        key.channel().close();
                        LOG.error("本次消息处理失败", e);
                    }
                }
            }
        } catch (IOException e) {
            LOG.error("服务器链接失败！", e);
        } finally {
            disConnection();
        }

    }

    /**
     * 关闭socket链接
     *
     * @throws IOException
     */
    public static void disConnection() {
        try {
            if (selector != null) selector.close();
        } catch (IOException e) {
            LOG.error("链接关闭失败！", e);
        }
        LOG.info("本次回话完成!");
    }


    /**
     * 用户登出
     *
     * @throws IOException
     */
    public static void loginout() {
        String msg = "{\"method\":\"loginout\",\"userName\":\"client_1\"}";
        sendMsgToServer(msg, "log");
    }

    public static void sendMsg(String message) {
        CHAT_RECORD.setText(CHAT_RECORD.getText().replace("</html>", "<p>【client1】:" + message + "</p></html>"));
        String msg = "{\"method\":\"sendMsg\",\"sourceName\":\"client_1\",\"userName\":\"client_2\",\"msg\":\"" + message + "\"}";
        sendMsgToServer(msg, "chat");
    }

    public static void main(String[] args) {
        if ("loginout".equals(args[0])) {
            loginout();
        } else if ("login".equals(args[0])) {
            regUser();
        } else if ("send".equals(args[0])) {
            sendMsg(args[1]);
        }
    }


}
