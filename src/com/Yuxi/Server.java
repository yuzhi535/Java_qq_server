package com.Yuxi;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @todo 发送图片
 * @todo 有空了写发送视频
 */
public class Server extends Thread {
    HashSet<ObjectOutputStream> clients;
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

    public synchronized void sendToAll() {

    }

    class MyServer extends Thread {
        Socket s;
        String url = "jdbc:sqlite:users.db";
        String jdbc = "org.sqlite.JDBC";
        Connection conn;

        String user_name = null;
        String user_passwd = null;

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
                        clients.add(out);
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
                        clients.add(out);
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
            int index = 0;
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
                    dataSize = info.getData_size();
                    if (totalSize - dataSize < 1024) {
                        if (type == 1) {
                            infoString.append(Arrays.toString(data));
                            System.out.println(infoString);

                            sendMsg(info);

                            infoString = new StringBuilder();
                        } else if (type == 2) {
//                            rawDat += new String(data);
                            sendMsg(info);

//                            rawDat = "";
                        } else {
                            String name = "";
                            for (String userName :
                                    user_to_clients.keySet()) {
                                name += "\r" + userName;
                            }
                            name += "\r";
                            info = new User(user_name, user_passwd, 1, 3, name.length(), name,
                                    name.getBytes(StandardCharsets.UTF_8), name.length());
                            sendMsg(info);
                        }
                        info = null;
                    } else {
                        if (type == 1) {
                            infoString.append(Arrays.toString(data));
                        } else if (type == 2) {
                            sendMsg(info);
                        } else {

                        }
                    }
                } catch (EOFException | SocketException e) {
                    break;
                } catch (StreamCorruptedException e) {
                    sendMsg(new User(user_name, user_passwd, 1, 1, 1, "asd", "传输失败".getBytes(), 8));
                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("connection closed");
            clients.remove(out);
            user_to_clients.remove(user_name);
            String close_info = "下线";
            sendMsg(new User(user_name, user_passwd, 1, 1, close_info.length(), "asd", close_info.getBytes(StandardCharsets.UTF_8), close_info.length()));
        }

        synchronized void sendMsg(User info) {
            try {
                for (ObjectOutputStream os :
                        clients) {
                    os.writeObject(info);
                    os.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}