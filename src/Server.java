import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;

/**
 * @todo: 6/1/21 视频 图片传输使用udp协议，正常检测是否在线使用tcp，发送文本使用tcp
 * @todo: 6/1/21 使用sqlite数据库，存储用户的姓名和密码
 */
public class Server {
    Server(int ip) {
        try {
            ServerSocket serverSocket = new ServerSocket(7777);

            while (true) {
                try (Socket s = serverSocket.accept()) {
                    new MyServer(s).start();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Server(7777);
    }
}

class MyServer extends Thread {
    Socket s;
    String url = "jdbc:sqlite:identifier.sqlite";
    String jdbc = "org.sqlite.JDBC";
    Connection conn;

    MyServer(Socket aim) {
        s = aim;
    }

    @Override
    public void run() {
        try {
            Class.forName(jdbc).newInstance();

            conn = DriverManager.getConnection(url);

        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("数据库加载错误，即将退出!");
            //------------------------

            //------------------------
        } catch (SQLException e) {
            System.out.println("数据库连接错误!,即将退出");
            //    退出
        }
    }
}
