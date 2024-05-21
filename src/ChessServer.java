import javax.swing.*;
import java.io.*;
import java.net.*;

public class ChessServer implements Serializable {
    private ServerSocket serverSocket;

    private ChessMainFrame frame;

    private ClientHandler clientHandler;

    public ChessServer(int port, ChessMainFrame frame) throws IOException {
        serverSocket = new ServerSocket(port);
        this.frame = frame;
    }

    public void start() throws IOException, ClassNotFoundException {
        clientHandler =  new ClientHandler(serverSocket.accept(), frame);
        clientHandler.start();
    }

    public void sendMove(Message msg) {
        try {
            clientHandler.send(msg);
        } catch (IOException e) {
            e.printStackTrace();
           // System.out.println("服务端 发送操作失败" );
        }
    }

    private static class ClientHandler extends Thread implements Serializable {
        private Socket clientSocket;
        private InputStream in;
        private OutputStream out;
        private ChessMainFrame frame;

        public ClientHandler(Socket socket, ChessMainFrame frame) throws IOException {
            this.clientSocket = socket;
            this.in = socket.getInputStream();
            this.out = socket.getOutputStream();
            this.frame = frame;
        }

        public void send(Object o) throws IOException {
            out.write(EventUtils.serializeEvent(o));
            out.flush();
        }

        public void run() {
            while (true) {
                try {
                    byte[] buffer = new byte[1024];
                    int bytesRead = in.read(buffer);
                    if (bytesRead != -1) {
                        Message me = (Message) EventUtils.deserializeEvent(buffer);
                        EventUtils.dealMsg(me, frame);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }


    }

}
