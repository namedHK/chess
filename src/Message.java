import java.awt.event.MouseEvent;
import java.io.Serializable;

/**
 * ������Ϣ
 *
 * @author hk
 * @version 1.0
 **/
public class Message implements Serializable {

    public Message(MouseEvent mouseEvent, int moveType, int movePiece) {
        this.mouseEvent = mouseEvent;
        this.moveType = moveType;
        this.movePiece = movePiece;
    }

    public Message() {
    }

    MouseEvent mouseEvent;

    /**
     * 1. ��һ�ε������
     * 2. �ƶ�ָ������
     * 3. �Ե�����
     */
    int moveType;

    /**
     * �ƶ�������
     */
    int movePiece;

    /**
     * 1. ��������
     * 2. ����
     * 3. Ͷ��
     * 4. ������Ϸʱ��
     */
    int MsgTYpe;

    /**
     * ÿһ�����ʱ�����ƣ����룩
     *
     */
    long moveTimeLimit;

    /**
     * ÿһ�����ʱ�����ƣ����룩
     */
    long gameTimeLimit;

    /**
     * �������
     */
    int chessPlayClick;

    public MouseEvent mouseEvent(){
        return mouseEvent;
    }

}
