package socket;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicReference;

public class Server {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) { // 监听端口12345
            System.out.println("Server is listening on port 12345");
            
            while (true) {
                Socket socket = serverSocket.accept(); // 等待客户端连接
                System.out.println("New client connected");
                
                new ServerThread(socket).start(); // 创建新的线程处理客户端请求
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ServerThread extends Thread {
    private Socket socket;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try (InputStream input = socket.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(input));
             OutputStream output = socket.getOutputStream();
             PrintWriter writer = new PrintWriter(output, true);
             BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))) {
            
            AtomicReference<String> messageFromClient = new AtomicReference<>();
            String messageFromServer;

            // 创建一个线程来读取客户端消息
            Thread readThread = new Thread(() -> {
                try {
                    while (( reader.readLine() != null)) {
                        messageFromClient.set(reader.readLine());
                        System.out.println("Received from client: " + messageFromClient);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            readThread.start();

            // 主线程用于发送消息到客户端
            while (true) {
                System.out.print("Enter message to client: ");
                messageFromServer = consoleReader.readLine();
                writer.println(messageFromServer);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
