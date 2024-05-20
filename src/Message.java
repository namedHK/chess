import java.awt.event.MouseEvent;
import java.io.Serializable;

/**
 * 传递消息
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

    MouseEvent mouseEvent;

    /**
     * 1. 第一次点击棋子
     * 2. 移动指定棋子
     * 3. 吃掉棋子
     */
    int moveType;

    /**
     * 移动的棋子
     */
    int movePiece;


    public MouseEvent mouseEvent(){
        return mouseEvent;
    }

}
