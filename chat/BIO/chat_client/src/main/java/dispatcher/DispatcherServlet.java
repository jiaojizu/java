package dispatcher;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;

/**
 * 请求分发器
 */
public class DispatcherServlet {
    static Logger LOG = Logger.getLogger(DispatcherServlet.class);

    public static void handleRequest(Socket socket) {
        BufferedReader clientReader = null;
        BufferedWriter clientWriter = null;
        try {
            clientReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));//接受服务端消息
            clientWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));//回复服务端

            //处理客户端消息
            StringBuffer clientMsg = new StringBuffer();
            String line = null;
            while ((line = clientReader.readLine()) != null) {
                clientMsg.append(line + "\n");
            }
            LOG.info(clientMsg.toString());
            String result = "{\"code\":\"200\"}";
            clientWriter.write(result);//收到信息回执
            clientWriter.flush();
            socket.shutdownOutput();
        } catch (IOException e) {
            LOG.error("读取消息失败", e);
        } finally {
            if (clientWriter != null) {
                try {
                    clientWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (clientReader != null) {
                try {
                    clientReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
