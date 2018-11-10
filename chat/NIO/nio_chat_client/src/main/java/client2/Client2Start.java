package client2;

import buffer.Buffers;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Client2Start {
    static Logger LOG = Logger.getLogger(Client2Start.class);
    static int DEFULT_PROT = 8802;
    static Selector selector = null;
    static ServerSocketChannel serverSocket = null;


    //显示聊天记录和信息
    public static JLabel CHAT_RECORD = new JLabel();



    public static void main(String[] args) {


        try {

            selector = Selector.open();
            serverSocket = ServerSocketChannel.open();//创建一个socket服务chanenl
            serverSocket.configureBlocking(false);//设置为非阻塞
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);//设置接受TCP链接
            serverSocket.bind(new InetSocketAddress(DEFULT_PROT),100);
            openWin();
            LOG.info("客户端启动成功！\n端口号：" + serverSocket.getLocalAddress());

        } catch (IOException e) {
            LOG.error("客户端服务启动失败！！！", e);
        }

        try {
            while (true) {
                selector.select();//阻塞等待请求链接
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                if (iterator.hasNext()){
                    SelectionKey key = iterator.next();//需要处理的请求
                    iterator.remove();//删除已处理的请求
                    if (key.isAcceptable()){
                        //已完成TCP三次握手，获取一个socket通道，socket不再阻塞等待
                        SocketChannel socketChannel = serverSocket.accept();
                        socketChannel.configureBlocking(false);
                        //注册客户端的socket到selector，设置事件类型为读事件类型,并且提供缓冲区
                        socketChannel.register(selector, SelectionKey.OP_READ|SelectionKey.OP_WRITE , new Buffers(256, 256));
                        LOG.info("来自" + socketChannel.getRemoteAddress() + "的请求");
                    }
                    if (key.isReadable()){
                        DispatcherServlet2.handleRequest(key);
                    }
                }

            }
        } catch (IOException e) {
            LOG.error("消息接受失败！！！", e);
        }

    }

    public static void openWin() {
        JFrame jFrame = new JFrame("client2客户端");
        jFrame.setSize(800, 500);
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jFrame.addWindowListener(new WindowAdapter() {//监听关闭窗口

            @Override
            public void windowOpened(WindowEvent e) {
                Client2Api.regUser();
                super.windowOpened(e);
            }

            @Override
            public void windowClosing(WindowEvent e) {
                Client2Api.loginout();
                try {
                    selector.close();
                } catch (IOException e1) {
                    LOG.error("客户端关闭失败！",e1);
                }finally{
                    LOG.info("客户端关闭成功！");
                }
                super.windowClosing(e);

            }
        });

        //显示聊天记录和信息
        CHAT_RECORD = new JLabel("<html><p>这里显示聊天记录:</p></html>");
        CHAT_RECORD.setBounds(40, 10, 700, 300);
        CHAT_RECORD.setOpaque(true);
        CHAT_RECORD.setBackground(Color.white);
        CHAT_RECORD.setHorizontalAlignment(SwingConstants.LEFT);
        CHAT_RECORD.setVerticalAlignment(SwingConstants.TOP);

        JTextArea jTextArea = new JTextArea();
        jTextArea.setBounds(40, 330, 700, 50);


        JButton jButton = new JButton("发送给client_1");
        jButton.setBounds(303, 400, 150, 30);
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Client2Api.sendMsg(jTextArea.getText());//监听点击事件发送消息
                jTextArea.setText("");
            }
        });

        Container contentPane = jFrame.getContentPane();
        contentPane.setLayout(null);
        contentPane.add(CHAT_RECORD);
        contentPane.add(jTextArea);
        contentPane.add(jButton);

        jFrame.setVisible(true);
    }
}
