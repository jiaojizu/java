package chat;

import java.net.Socket;

public class User {
    public User() {
    }

    public User(String userName, int prot, String ip, int status) {
        this.userName = userName;
        this.prot = prot;
        this.ip = ip;
        this.setStatus(status);//设置用户状态
    }

    public User(String userName, int prot, String ip) {
        this.userName = userName;
        this.prot = prot;
        this.ip = ip;
    }


    String userName = "";
    int prot = 0;
    String ip = "";
    int status = 0;//在线状态：1在线，0下线
    Socket socket = null;//用户链接

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getProt() {
        return prot;
    }

    public void setProt(int prot) {
        this.prot = prot;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
