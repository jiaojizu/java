package chat;

import com.buffer.Buffers;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 聊天服务器启动程序
 */
public class ServerStart {
    static Logger LOG = Logger.getLogger(ServerStart.class);
    static ExecutorService executorService = Executors.newFixedThreadPool(8);
    static int DEFULT_PROT = 8888;
    static Charset utf8 = Charset.forName("UTF-8");


    public static void main(String[] args) {

        Selector selector = null;
        ServerSocketChannel serverSocket = null;
        InetSocketAddress socketAddress = new InetSocketAddress(DEFULT_PROT);
        try {
            //创建一个选择器
            selector = Selector.open();
            //创建一个服务通道
            serverSocket = ServerSocketChannel.open();
            serverSocket.configureBlocking(false);//设置通道阻塞模式，false为非阻塞
            serverSocket.bind(socketAddress, 100);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);//注册通道到selector
            LOG.info("服务器启动成功！\n端口号：" + serverSocket.getLocalAddress());
        } catch (IOException e) {
            LOG.error("服务启动失败！！！", e);
        }

        try {
            while (true) {
                int select = selector.select();
                if (select < 1) continue;
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {//处理新接入的客户端
                    SelectionKey key = iterator.next();
                    try {
                        iterator.remove();//删除处理过的channel
                        LOG.info(key.isReadable()+"111111111111111"+key.isWritable()+"www2222"+key.isAcceptable());
                        //处理tcp链接事件
                        if (key.isAcceptable()) {
                            //已完成TCP三次握手，获取一个socket通道，socket不再阻塞等待
                            SocketChannel socketChannel = serverSocket.accept();
                            socketChannel.configureBlocking(false);
                            //注册客户端的socket到selector，设置事件类型为读事件类型,并且提供缓冲区
                            socketChannel.register(selector, SelectionKey.OP_READ , new Buffers(256, 256));
                            LOG.info("来自" + socketChannel.getRemoteAddress() + "的请求");
                        }

                        //处理读事件，并且有可读数据
                        if (key.isReadable()) {
                            DispatcherServlet.handleRequest(key);
                            key.interestOps(key.interestOps()&(~SelectionKey.OP_READ));
                        }
                        //处理
                        if (key.isWritable()){
                            DispatcherServlet.handleResponse(key);
                        }
                    } catch (Exception e) {
                        LOG.error("客户端连接出现错误",e);
                        key.cancel();
                        key.channel().close();
                    }

                }

            }
        } catch (IOException e) {
            LOG.error("消息接受失败！！！", e);
        }

    }


}
