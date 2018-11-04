package com.chat;

import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;

import static com.chat.DispatcherServlet.ONLINE_USER;

/**
 * 处理请求
 */
public class HandleProgram {
    static Logger LOG = Logger.getLogger(HandleProgram.class);

    /**
     * 注册用户
     *
     * @param msg
     * @param sourceWriter
     */
    public void regUser(Map<String, Object> msg, BufferedWriter sourceWriter) throws IOException {
        User user = new User(msg.get("userName").toString(), Integer.valueOf(msg.get("prot").toString()), msg.get("ip").toString(), 1);
        synchronized (user.getUserName().intern()) {
            ONLINE_USER.put(user.getUserName(), user);
        }
        sourceWriter.write("用户注册成功！");
    }

    /**
     * 发送消息
     *
     * @param message
     * @param sourceWriter
     */
    public void sendMsg(Map<String, Object> message, BufferedWriter sourceWriter) throws IOException {
        User user = ONLINE_USER.get(message.get("userName"));
        if (user == null || user.getStatus() == 0) {
            sourceWriter.write("消息发送失败，用户未注册或未在线");
        }
        SendMsgServer.sendMsg(user, message, sourceWriter);
    }

    /**
     * 用户登出
     *
     * @param message
     * @param sourceWriter
     */
    public void loginout(Map<String, Object> message, BufferedWriter sourceWriter) throws IOException {
        User user = ONLINE_USER.get(message.get("userName"));
        if (user == null || user.getStatus() == 0) {
            sourceWriter.write("用户登出失败，用户未注册或未在线");
        }
        user.setStatus(0);
        sourceWriter.write("用户登出成功！");
    }
}
