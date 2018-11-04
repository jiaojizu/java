package client2;

import client1.Client1Api;
import dispatcher.DispatcherServlet;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client2Start {
    static Logger LOG = Logger.getLogger(Client2Start.class);
    static ExecutorService executorService = Executors.newFixedThreadPool(8);
    static int DEFULT_PROT = 8802;


    public static void main(String[] args) {

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(DEFULT_PROT);//创建一个socket服务端
            LOG.info("客户端启动成功！\n端口号：" + serverSocket.getLocalPort());
            Client2Api.regUser();
        } catch (IOException e) {
            LOG.error("客户端服务启动失败！！！", e);
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