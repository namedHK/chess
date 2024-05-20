import java.io.*;
import java.net.*;
import java.awt.event.MouseEvent;

public class ChessClient {
    private Socket socket;
    private OutputStream out;
    private InputStream in;

    public ChessClient(String serverAddress, int port) throws IOException {
        socket = new Socket(serverAddress, port);
        out = socket.getOutputStream();
        in = socket.getInputStream();
    }

    public void sendMove(Message me) {
        byte[] data = null;
        try {
            data = EventUtils.serializeEvent(me);
            out.write(data);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            //System.out.println("客户端 发送操作失败 :" + e.getMessage());
        }

    }

    public void receiveMoves(ChessMainFrame frame) throws IOException, ClassNotFoundException {
        byte[] buffer = new byte[1024];
        int bytesRead = in.read(buffer);
        if (bytesRead != -1) {
            Message msg = (Message) EventUtils.deserializeEvent(buffer);
            frame.move(msg.mouseEvent(), msg.moveType, msg.movePiece);
            // 处理接收到的 MouseEvent
            System.out.println("从服务器接收到的移动: " + msg);
        }
    }
}
