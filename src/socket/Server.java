package socket;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicReference;

public class Server {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) { // �����˿�12345
            System.out.println("Server is listening on port 12345");
            
            while (true) {
                Socket socket = serverSocket.accept(); // �ȴ��ͻ�������
                System.out.println("New client connected");
                
                new ServerThread(socket).start(); // �����µ��̴߳���ͻ�������
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

            // ����һ���߳�����ȡ�ͻ�����Ϣ
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

            // ���߳����ڷ�����Ϣ���ͻ���
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
