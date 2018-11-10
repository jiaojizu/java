package chat;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

import static chat.DispatcherServlet.ONLINE_USER;


/**
 * 处理请求
 */
public class HandleProgram {
    static Logger LOG = Logger.getLogger(HandleProgram.class);

    /**
     * 注册用户
     *
     * @param msg
     */
    public void regUser(Map<String, Object> msg, ByteBuffer writeBuffer) throws IOException {
        User user = new User(msg.get("userName").toString(), Integer.valueOf(msg.get("prot").toString()), msg.get("ip").toString(), 1);
        synchronized (user.getUserName().intern()) {
            ONLINE_USER.put(user.getUserName(), user);
        }
        writeBuffer.put("用户注册成功！".getBytes("UTF-8"));
    }

    /**
     * 发送消息
     *
     * @param message
     */
    public void sendMsg(Map<String, Object> message, ByteBuffer writeBuffer) throws IOException {
        User user = ONLINE_USER.get(message.get("userName"));
        if (user == null || user.getStatus() == 0) {
            writeBuffer.put("消息发送失败，用户未注册或未在线".getBytes("UTF-8"));

        }
        SendMsgServer.sendMsg(user, message, writeBuffer);
    }

    /**
     * 用户登出
     *
     * @param message
     */
    public void loginout(Map<String, Object> message, ByteBuffer writeBuffer) throws IOException {
        User user = ONLINE_USER.get(message.get("userName"));
        if (user == null || user.getStatus() == 0) {
            writeBuffer.put("用户登出失败，用户未注册或未在线".getBytes("UTF-8"));
        }
        user.setStatus(0);
        writeBuffer.put("用户登出成功！".getBytes("UTF-8"));
    }
}
