import javax.swing.*;
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

    public static void dealMsg(Message me, ChessMainFrame frame) {
        if(me.MsgTYpe == 1) {
            frame.move(me.mouseEvent(), me.moveType, me.movePiece);
        }else if(me.MsgTYpe == 2){
            if(me.chessPlayClick == 1){
                JOptionPane.showConfirmDialog(
                        frame,"ºìÆìÊ¤Àû","ºìÆìÊ¤Àû",
                        JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE);
            }else if( me.chessPlayClick == 2){
                JOptionPane.showConfirmDialog(
                        frame,"ºÚÆìÊ¤Àû","ºÚÆìÊ¤Àû",
                        JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE);
            }
        }else if(me.MsgTYpe == 3){
            JOptionPane.showConfirmDialog(
                    frame,"Ë«·½ºÍÆå£¡","ºÍÆå",
                    JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE);
        }else if(me.MsgTYpe == 4){
            frame.moveTimeLimit = me.moveTimeLimit;
            frame.gameTimeLimit = me.gameTimeLimit;
            frame.panelRepaint();
        }
    }
}
