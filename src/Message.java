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

    public Message() {
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

    /**
     * 1. 关于棋子
     * 2. 认输
     * 3. 投降
     * 4. 设置游戏时间
     */
    int MsgTYpe;

    /**
     * 每一步棋的时间限制（毫秒）
     *
     */
    long moveTimeLimit;

    /**
     * 每一局棋的时间限制（毫秒）
     */
    long gameTimeLimit;

    /**
     * 认输玩家
     */
    int chessPlayClick;

    public MouseEvent mouseEvent(){
        return mouseEvent;
    }

}
