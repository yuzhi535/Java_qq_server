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
            String sql = "SELECT * FROM users";
            try {
                Statement sm = conn.createStatement();
                ResultSet rs = sm.executeQuery(sql);

                while (rs.next()) {
                    String name = rs.getString("user_name");
                    String passwd = rs.getString("passwd");
                    users.put(name, passwd);
                    System.out.println("database " + name + " " + passwd);
                }


            } catch (SQLException e) {
                e.printStackTrace();
            }


            String user_name = null;
            String user_passwd = null;
            try {
                // register
                if (!br.readLine().equals("login")) {
                    user_name = br.readLine();
                    user_passwd = br.readLine();

                    System.out.println("user_name " + user_name + " passwd " + user_passwd);

                    if (users.containsKey(user_name)) {
                        System.out.println("the name is owned by the database!\n Please select another one!");
                        bw.write("not valid\n");
                        bw.flush();
                    } else {
                        String insertsql = "insert into users (user_name, passwd) values (?, ?)";
                        try {
                            PreparedStatement ps = conn.prepareStatement(insertsql);
                            ps.setString(1, user_name);
                            ps.setString(2, user_passwd);
                            ps.executeUpdate();
                        } catch (SQLException e) {

                        }
                        clients.add(s);
                    }
                } else {
                    user_name = br.readLine();
                    if (user_name != null) {
                        System.out.println("username = " + user_name);
                    } else {
                        throw new IOException("no user_name");
                    }
                    user_passwd = br.readLine();
                    if (user_passwd != null) {
                        System.out.println("user passwd=" + user_passwd);
                    } else {
                        throw new IOException("no_passwd");
                    }

                    if (users.containsKey(user_name)) {
                        clients.add(s);
                        bw.write("valid\n");
                        bw.flush();
                    } else {
                        bw.write("invalid\n");
                        bw.flush();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        @Override
        public void run() {
            System.out.println("connected to " + s.getRemoteSocketAddress());

            if (s.isClosed()) {
                System.out.println("why");
            }

            while (true) {
                try {
                    String readContent = br.readLine();
                    if (readContent == null) {
                        break;
                    }
                    System.out.println(readContent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
                System.out.println("connection is closed");

//            System.out.println("connection is closed");
        }
    }
}