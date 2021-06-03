package com.Yuxi;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @todo: 6/1/21 视频 图片传输使用udp协议，正常检测是否在线使用tcp，发送文本使用tcp
 */
public class Server {
    HashSet<Socket> clients;
    HashMap<String, String> users;
    HashMap<String, Socket> user_to_clients;

    Server(int ip) {
        try {
            ServerSocket serverSocket = new ServerSocket(6666);
            clients = new HashSet<>();
            users = new HashMap<>();
            user_to_clients = new HashMap<>();
            while (true) {
                try {
                    Socket ss = serverSocket.accept();
                    clients.add(ss);
                    Thread thread = new MyServer(ss);
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

        ObjectOutputStream out;
        ObjectInputStream in;

        boolean isValid = false;

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
                in = new ObjectInputStream(s.getInputStream());
                out = new ObjectOutputStream(s.getOutputStream());
                out.flush();
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
                String read = in.readUTF();
                System.out.println(read);
                if (read.equals("register")) {
                    user_name = in.readUTF();
                    user_passwd = in.readUTF();

                    System.out.println("user_name " + user_name + " passwd " + user_passwd);

                    if (users.containsKey(user_name)) {
                        System.out.println("the name is owned by the database!\n Please select another one!");
                        out.writeUTF("not valid");
                        out.flush();
                    } else {
                        String insertsql = "insert into users (user_name, passwd) values (?, ?)";
                        try {
                            PreparedStatement ps = conn.prepareStatement(insertsql);
                            ps.setString(1, user_name);
                            ps.setString(2, user_passwd);
                            ps.executeUpdate();
                        } catch (SQLException e) {

                        }
                        out.writeUTF("valid");
                        out.flush();
                        clients.add(s);
                        isValid = true;
                    }
                } else if (read.equals("login")) {
                    user_name = in.readUTF();
                    System.out.println("username = " + user_name);
                    user_passwd = in.readUTF();
                    System.out.println("user passwd = " + user_passwd);

                    if (users.containsKey(user_name) && users.get(user_name).equals(user_passwd) && !user_to_clients.containsKey(user_name)) {
                        out.writeUTF("valid");
                        out.flush();
                        clients.add(s);
                        user_to_clients.put(user_name, s);
                        isValid = true;
                    } else {
                        out.writeUTF("invalid");
                        out.flush();
                    }
                } else {
                    /*
                      impossible
                     */
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        @Override
        public void run() {
            if (!isValid) {
                return;
            }
            System.out.println("connected to " + s.getRemoteSocketAddress());

            User info;
            int index;
            byte[] data;
            int totalSize;
            int type;
            int dataSize = 0;
            String users = "";
            StringBuilder infoString = new StringBuilder();

            while (true) {
                try {
                    info = (User) in.readObject();
                    index = info.getIndex();
                    data = info.getData();
                    users = info.getGroup();
                    totalSize = info.getTotal_size();
                    type = info.getType();
                    dataSize += info.getData_size();
                    if (dataSize == totalSize) {
                        if (type == 1) {
                            infoString.append(Arrays.toString(data));
                            System.out.println(infoString);

                            sendMsg(String.valueOf(infoString));

                            infoString = new StringBuilder();
                        } else {
                            /**
                             *
                             */
                        }
                        dataSize = 0;
                        users = "";
                        info = null;
                    } else {
                        if (type == 1) {
                            infoString.append(Arrays.toString(data));
                        } else {
                            /**
                             *
                             */
                        }
                    }
                } catch (EOFException | SocketException e) {
                    break;
                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("connection closed");
            clients.remove(s);
            user_to_clients.remove(users);
        }

        void sendMsg(String msg) {
            try {
                out.writeObject(msg);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}