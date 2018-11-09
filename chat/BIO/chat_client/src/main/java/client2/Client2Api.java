package client2;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;

import static client2.Client2Start.CHAT_RECORD;

public class Client2Api {
    static Logger LOG = Logger.getLogger(Client2Api.class);

    public static Socket serverScoket = null;
    public static BufferedReader serverReader = null;
    public static BufferedWriter serverWriter = null;

    static int SERVER_PROT = 8888;

    static String SERVER_IP = "127.0.0.1";

    /**
     * 注册用户
     */
    public static void regUser() {
        try {
            getConnection();
            String msg = "{\"method\":\"regUser\",\"userName\":\"client_2\",\"ip\":\"127.0.0.1\",\"prot\":\"8802\"}";
            serverWriter.write(msg);//写入消息到服务端
            serverWriter.flush();
            serverScoket.shutdownOutput();
            //显示客户端返回消息
            LOG.info(getMessage(serverReader));
            serverScoket.shutdownInput();
        } catch (IOException e) {
            LOG.error("链接服务器出现异常", e);
        } finally {
            try {
                disConnection();
            } catch (IOException e) {
                LOG.error("关闭socket失败", e);
            }
        }

    }

    /**
     * 输入流转换为字符串
     *
     * @param serverReader
     * @return
     * @throws IOException
     */
    public static String getMessage(BufferedReader serverReader) throws IOException {
        StringBuffer serverMsg = new StringBuffer();
        String line = null;
        while ((line = serverReader.readLine()) != null) {
            serverMsg.append(line + "\n");
        }
        return serverMsg.toString();// 显示收到消息
    }

    /**
     * 建立socket链接
     *
     * @throws IOException
     */
    public static void getConnection() throws IOException {
        serverScoket = new Socket(SERVER_IP, SERVER_PROT);
        serverReader = new BufferedReader(new InputStreamReader(serverScoket.getInputStream()));
        serverWriter = new BufferedWriter(new OutputStreamWriter(serverScoket.getOutputStream()));
//        LOG.info("链接服务器成功!");
    }

    /**
     * 关闭socket链接
     *
     * @throws IOException
     */
    public static void disConnection() throws IOException {
        if (serverReader != null) serverReader.close();
        if (serverWriter != null) serverWriter.close();
        if (serverScoket != null) serverScoket.close();
//        LOG.info("本次回话完成，关闭链接!");
    }


    /**
     * 用户登出
     *
     * @throws IOException
     */
    public static void loginout() {
        try {
            getConnection();
            String msg = "{\"method\":\"loginout\",\"userName\":\"client_2\"}";
            serverWriter.write(msg);//写入消息到服务端
            serverWriter.flush();
            serverScoket.shutdownOutput();
            //显示客户端返回消息
            LOG.info(getMessage(serverReader));
            serverScoket.shutdownInput();
        } catch (IOException e) {
            LOG.error("", e);
        } finally {
            try {
                disConnection();
            } catch (IOException e) {
                LOG.error("关闭socket失败", e);
            }
        }
    }

    public static void sendMsg(String message) {
        try {
            getConnection();
            CHAT_RECORD.setText(CHAT_RECORD.getText().replace("</html>","<p>【client2】:"+message+"</p></html>"));
            String msg = "{\"method\":\"sendMsg\",\"sourceName\":\"client_2\",\"userName\":\"client_1\",\"msg\":\"" + message + "\"}";
            serverWriter.write(msg);//写入消息到服务端
            serverWriter.flush();
            serverScoket.shutdownOutput();
            //显示客户端返回消息
            //显示客户端返回消息
            CHAT_RECORD.setText(CHAT_RECORD.getText().replace("</html>","<p>"+getMessage(serverReader)+"</p></html>"));
            serverScoket.shutdownInput();
        } catch (IOException e) {
            LOG.error("", e);
        } finally {
            try {
                disConnection();
            } catch (IOException e) {
                LOG.error("关闭socket失败", e);
            }
        }
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
