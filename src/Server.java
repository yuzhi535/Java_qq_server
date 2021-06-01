import java.io.IOException;
import java.net.ServerSocket;

public class Server {
    Server(int ip) {
        try {
            ServerSocket serverSocket = new ServerSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
