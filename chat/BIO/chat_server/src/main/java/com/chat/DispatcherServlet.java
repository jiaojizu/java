package com.chat;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求分发器
 */
public class DispatcherServlet {
    static Logger LOG = Logger.getLogger(DispatcherServlet.class);

    public static Map<String, User> ONLINE_USER = new HashMap<>();//在线用户

    public static void handleRequest(Socket socket) {
        BufferedReader reader = null;
        BufferedWriter sourceWriter = null;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));//接受客户端消息
            sourceWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));//回复客户端

            //处理客户端消息
            StringBuffer clientMsg = new StringBuffer();
            String line = null;
            while ((line = reader.readLine()) != null) {
                clientMsg.append(line + "\n");
            }

            LOG.info(clientMsg);

            Map<String, Object> message = new JSONObject(clientMsg.toString()).toMap();

            HandleProgram handleProgram = new HandleProgram();

            if (!message.containsKey("method")) {
                sourceWriter.write("非法的请求！");
                sourceWriter.flush();//刷新buffer，让客户端立马收到消息
                return;
            }

            if ("regUser".equals(message.get("method"))) {//注册用户
                handleProgram.regUser(message, sourceWriter);
            } else if ("sendMsg".equals(message.get("method"))) {//发送消息
                handleProgram.sendMsg(message, sourceWriter);
            } else if ("loginout".equals(message.get("method"))){//用户登出
                handleProgram.loginout(message, sourceWriter);
            }else {
                sourceWriter.write("请求的方法不存在！");
                sourceWriter.flush();//刷新buffer，让客户端立马收到消息
                return;
            }
            sourceWriter.flush();//刷新buffer，让客户端立马收到消息
            socket.shutdownOutput();//关闭输出流
        } catch (IOException e) {
            LOG.error("读取消息失败", e);
        } finally {
            if (sourceWriter != null) {
                try {
                    sourceWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (reader != null) {
                try {
                    reader.close();
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
