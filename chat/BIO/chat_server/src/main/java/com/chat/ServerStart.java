package com.chat;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 聊天服务器启动程序
 */
public class ServerStart {
    static Logger LOG = Logger.getLogger(ServerStart.class);
    static ExecutorService executorService = Executors.newFixedThreadPool(8);
    static int DEFULT_PROT = 8888;


    public static void main(String[] args) {

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(DEFULT_PROT);//创建一个socket服务端
            LOG.info("服务器启动成功！\n端口号：" + serverSocket.getLocalPort());
        } catch (IOException e) {
            LOG.error("服务启动失败！！！", e);
        }

        try {
            while (true) {
                Socket socket = serverSocket.accept();
                executorService.submit(() -> {
                    DispatcherServlet.handleRequest(socket);
                });

            }
        } catch (IOException e) {
            LOG.error("消息接受失败！！！", e);
        }

    }
}
