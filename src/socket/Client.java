package socket;

import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        String hostname = "localhost"; // ��������ַ
        int port = 12345; // �������˿�

        try (Socket socket = new Socket(hostname, port)) {
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            String text;

            do {
                System.out.print("Enter message: ");
                text = consoleReader.readLine();

                writer.println(text); // ������Ϣ��������

                String response = reader.readLine();
                System.out.println(response); // �ӷ��������ջ�Ӧ

            } while (!text.equals("bye"));

        } catch (UnknownHostException e) {
            System.out.println("Server not found: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
        }
    }
}
