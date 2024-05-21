import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

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
            Message me = (Message) EventUtils.deserializeEvent(buffer);
            EventUtils.dealMsg(me, frame);
        }
    }
}
