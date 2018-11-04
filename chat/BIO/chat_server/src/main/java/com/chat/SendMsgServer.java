package com.chat;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.Map;

public class SendMsgServer {
    static Logger LOG = Logger.getLogger(SendMsgServer.class);
    public static void sendMsg(User user, Map<String, Object> message, BufferedWriter sourceWriter) {
        Socket socket = null;//目标客户端
        BufferedWriter targetWrite = null;//目标客户端
        BufferedReader targetRead = null;//目标客户端
        try {
            socket = new Socket(user.getIp(),user.getProt());
            targetWrite = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            targetRead = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            targetWrite.write("【"+message.get("sourceName")+"】:"+message.get("msg"));//发送消息
            targetWrite.flush();
            socket.shutdownOutput();

            //处理客户端接受消息回执
            StringBuffer clientMsg = new StringBuffer();
            String line = null;
            while ((line = targetRead.readLine()) != null) {
                clientMsg.append(line + "\n");
            }
            Map<String, Object> result = new JSONObject(clientMsg.toString()).toMap();
            if (result.containsKey("code")&&"200".equals(result.get("code"))){
                sourceWriter.write(user.getUserName()+"消息接受成功！");
            }else{
                sourceWriter.write(user.getUserName()+"消息接受异常！");
            }
        } catch (IOException e) {
            try {
                sourceWriter.write("目标客户端异常！");
            } catch (IOException e1) {
                LOG.error("链接客户端异常！"+user.getProt(),e);
            }
            LOG.error("链接客户端异常！",e);
        }finally {
            if (targetRead != null) {
                try {
                    targetRead.close();
                } catch (IOException e) {
                    LOG.error(e);
                }
            }
            if (targetWrite != null) {
                try {
                    targetWrite.close();
                } catch (IOException e) {
                    LOG.error(e);
                }
            }
            if (socket!=null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    LOG.error("目标客户端关闭失败！"+user.getProt(),e);
                }
            }
        }
    }
}
