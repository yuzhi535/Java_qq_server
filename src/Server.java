import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    Server(int ip) {
        try {
            ServerSocket serverSocket = new ServerSocket(7777);
            try (Socket s = serverSocket.accept()) {

            } catch (IOException e) {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
