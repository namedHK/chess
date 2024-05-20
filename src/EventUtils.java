import java.awt.event.MouseEvent;
import java.io.*;

public class EventUtils {

    public static byte[] serializeEvent(Object event) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(event);
        oos.flush();
        return bos.toByteArray();
    }

    public static Object deserializeEvent(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bis);
        return  ois.readObject();
    }
}
