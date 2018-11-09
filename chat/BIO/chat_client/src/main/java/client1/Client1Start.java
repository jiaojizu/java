package client1;

import client2.Client2Api;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client1Start {
    static Logger LOG = Logger.getLogger(Client1Start.class);
    static ExecutorService executorService = Executors.newFixedThreadPool(8);
    static int DEFULT_PROT = 8801;

    static ServerSocket serverSocket = null;
    static Socket socket = null;

    //显示聊天记录和信息
    public static JLabel CHAT_RECORD = new JLabel();

    public static void main(String[] args) {

        try {
            serverSocket = new ServerSocket(DEFULT_PROT);//创建一个socket服务端
            openWin();
            LOG.info("客户端启动成功！\n端口号：" + serverSocket.getLocalPort());
        } catch (IOException e) {
            try {
                serverSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            LOG.error("客户端服务启动失败！！！", e);
        }

        try {
            while (true) {
                socket = serverSocket.accept();
                executorService.submit(() -> {
                    DispatcherServlet1.handleRequest(socket);
                });

            }
        } catch (IOException e) {
            LOG.error("消息接受失败！！！", e);
        }

    }

    public static void openWin() {
        JFrame jFrame = new JFrame("client1客户端");
        jFrame.setSize(800, 500);
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jFrame.addWindowListener(new WindowAdapter() {//监听关闭窗口

            @Override
            public void windowOpened(WindowEvent e) {
                Client1Api.regUser();
                super.windowOpened(e);
            }

            @Override
            public void windowClosing(WindowEvent e) {
                Client1Api.loginout();
                if (!executorService.isShutdown()) {
                    executorService.shutdown();
                }
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e1) {
                        LOG.error("客户端关闭失败", e1);
                    }
                }
                if (serverSocket != null) {
                    try {
                        serverSocket.close();
                    } catch (IOException e1) {
                        LOG.error("客户端关闭失败", e1);
                    }
                }
                LOG.info("客户端关闭成功！");
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
        jButton.setBounds(303, 400, 100, 30);
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Client1Api.sendMsg(jTextArea.getText());//监听点击事件发送消息
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
