package com.Main;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @todo: 6/1/21 视频 图片传输使用udp协议，正常检测是否在线使用tcp，发送文本使用tcp
 * @todo: 6/1/21 使用sqlite数据库，存储用户的姓名和密码
 */
public class Server {
    HashSet<Socket> clients;
    HashMap<String, String> users;

    Server(int ip) {
        try {
            ServerSocket serverSocket = new ServerSocket(6666);
            clients = new HashSet<>();
            users = new HashMap<>();
            while (true) {
                try {
                    Socket s = serverSocket.accept();
                    clients.add(s);
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

            try {
                br = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
                bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }

            //  使用数据库对表中数据进行查询，防止未注册账户进入
            String sql = "SELECT user_name FROM users";
            try {
                Statement sm = conn.createStatement();
                ResultSet rs = sm.executeQuery(sql);

                while (rs.next()) {
                    String name = rs.getString("user_name");
                    String passwd = rs.getString("passwd");
                    users.put(name, passwd);
                }


            } catch (SQLException e) {
                e.printStackTrace();
            }

            String user_name = null;
            String user_passwd = null;
            try {
                user_name = br.readLine();
                if (user_name != null) {
                    System.out.println(user_name);
                } else {
                    throw new IOException("no user_name");
                }
                user_passwd = br.readLine();
                if (user_passwd != null) {
                    System.out.println(user_passwd);
                } else {
                    throw new IOException("no_passwd");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (users.containsKey(user_name)) {
                if (user_name == null || user_passwd == null) {

                }
                clients.add(s);
                String insertsql = "insert into users (user_name, passwd) values (?, ?)";
                try {
                    PreparedStatement ps = conn.prepareStatement(insertsql);
                    ps.setString(1, user_name);
                    ps.setString(2, user_passwd);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    bw.write("invalid name! you should register it first!");
                    bw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void run() {
            System.out.println("connected to " + s.getRemoteSocketAddress());

            if (s.isClosed()) {
                System.out.println("why");
            }


            System.out.println("connection is closed");
        }
    }
}