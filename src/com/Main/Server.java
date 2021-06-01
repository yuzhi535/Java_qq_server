package com.Main;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.sql.*;

/**
 * @todo: 6/1/21 视频 图片传输使用udp协议，正常检测是否在线使用tcp，发送文本使用tcp
 * @todo: 6/1/21 使用sqlite数据库，存储用户的姓名和密码
 */
public class Server {
    Server(int ip) {
        try {
            ServerSocket serverSocket = new ServerSocket(6666);

            while (true) {
                try {
                    Socket s = serverSocket.accept();
                    Thread thread = new MyServer(s);
                    thread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class MyServer extends Thread {
    Socket s;
    String url = "jdbc:sqlite:users.db";
    String jdbc = "org.sqlite.JDBC";
    Connection conn;

    BufferedWriter bw;
    BufferedReader br;

    MyServer(Socket _socket) {
        s = _socket;

        try {
            Class.forName(jdbc).newInstance();

            conn = DriverManager.getConnection(url);

        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("数据库加载错误，即将退出!");
            //------------------------

            //------------------------
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("数据库连接错误!,即将退出");
            //    退出
        }
    }

    @Override
    public void run() {
        try {
            br = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
            bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8));

            System.out.println("connected to " + s.getRemoteSocketAddress());

            if (s.isClosed()) {
                System.out.println("why");
            }

            String str;
            while (true) {
                try {
                    str = br.readLine();
                    if (str != null)
                        System.out.println(str);
                    else {
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("connection is closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
