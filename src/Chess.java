/*
 *中国象棋Java版V3.0
 *添加功能:实现了当前棋局的保存
 */

import compont.TimeSettingDialog;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;

//主类
public class Chess{

    public static Map<String,String> title = new HashMap<>();

    static {
        title.put("1", "server");
        title.put("2", "client");
        title.put("3", "single");
    }

	public static void main(String args[]){
        int playType = args.length > 0  ? Integer.parseInt(args[0]) : 3;
        String server = null;
        if(playType == 2){
            server = args[1];
        }

        ChessMainFrame frame = new ChessMainFrame("java中国象棋对弈" + title.get(args[0]) , playType, server);

	}
}

//主框架类
class ChessMainFrame extends JFrame implements ActionListener,MouseListener,Runnable{
	//玩家
	JLabel play[] = new JLabel[32];
	//棋盘
	JLabel image;	
	//窗格
	Container con;
	//工具栏
	JToolBar jmain;	
	//重新开始
	JButton anew;
	//悔棋
	JButton repent;
    //认输
    JButton fail;
    //和棋
    JButton peace;
    //记录
    JButton log;
    //帮助
    JButton help;
	//退出
	JButton exit;
	//当前信息
	JLabel text;
	
	//保存当前操作
	Vector Var;
	
	//规则类对象(使于调用方法)
	ChessRule rule;
	
	/**
	** 单击棋子
	** chessManClick = true 闪烁棋子 并给线程响应
	** chessManClick = false 吃棋子 停止闪烁  并给线程响应
	*/
	boolean chessManClick;
	
	/**
	** 控制玩家走棋
	** chessPlayClick=1 黑棋走棋
	** chessPlayClick=2 红棋走棋 默认红棋
	** chessPlayClick=3 双方都不能走棋
	*/
	public int chessPlayClick=2;
	
	//控制棋子闪烁的线程
	Thread tmain;
	//把第一次的单击棋子给线程响应
	static int Man,i;

    private ChessClient client;
    private ChessServer server;

    /**
     * 游玩模式 1 服务端 2 客户端 3 本机对战
     */
    private int playType = 3;

    /**
     * 当前玩家颜色
     */
    int currentChessPlayClick;


    Timer moveTimer;
    Timer gameTimer;
    long moveTimeLimit;  // 每一步棋的时间限制（毫秒）
    long gameTimeLimit;

    private String logStr = "";

    public static final String helpStr = "象棋，又称中国象棋，是一种两人对弈的棋类游戏。它在中国和中文圈有着悠久的历史和深厚的文化意义。下面是象棋的基本规则：\n" +
            "\n" +
            "棋盘和布局：象棋棋盘是一个由9列和10行组成的格线网络，棋子放在交叉点上。棋盘被一条没有格线的河界分成两半，分别代表对弈双方。\n" +
            "\n" +
            "棋子：每方有1个将（帅）、2个士（仕）、2个象（相）、2个马、2个车、2个炮和5个兵（卒）。\n" +
            "\n" +
            "将/帅：只能在自己的九宫内移动，每次只能走一步，且只能直走或横走。\n" +
            "士/仕：也只能在九宫内移动，每次走斜线一格。\n" +
            "象/相：每次走“田”字，即两点之间的距离是两个格的斜线，不能越过河界。\n" +
            "马：走“日”字形，即先直走一格再斜走一格，如果直走的第一格有棋子堵住（称为蹩马腿），则马不能走这个方向。\n" +
            "车：直线前进，可以横向或纵向移动，距离不限，但不能越过其他棋子。\n" +
            "炮：移动时与车类似，但在吃子时，必须跳过一个棋子。\n" +
            "兵/卒：只能直走，过河后可以左右移动。\n" +
            "对弈目标：将军对方的将/帅。如果对方的将/帅无法逃脱，则称为“将死”，对弈胜利。\n" +
            "\n" +
            "特殊规则：\n" +
            "\n" +
            "长将：重复进行相同的将军动作被认为是违规的。\n" +
            "和棋：在某些重复局面下，双方可能会达成和棋。\n" +
            "禁手：一些特定的重复动作可能会被视为禁手，具体规则可能因比赛和地区而异。\n" +
            "象棋的策略和技巧深奥丰富，包括开局、中局和残局的不同战术。对于初学者来说，了解这些基本规则是入门的第一步，随后可以通过实战来提高棋艺。";

    private int logCount;

    /**
	** 构造函数
	** 初始化图形用户界面
	*/
	ChessMainFrame(String Title, int playType, String serverStr){
        this.playType = playType;
		//改变系统默认字体
		Font font = new Font("Dialog", Font.PLAIN, 12);
		java.util.Enumeration keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof javax.swing.plaf.FontUIResource) {
				UIManager.put(key, font);
			}
		}
		//获行客格引用
		con = this.getContentPane();
		con.setLayout(null);
		//实例化规则类
		rule = new ChessRule();
		Var = new Vector();
		
		//创建工具栏
		jmain = new JToolBar();
		text = new JLabel("欢迎使用象棋对弈系统");
		//当鼠标放上显示信息
		text.setToolTipText("信息提示");
		anew = new JButton(" 新 游 戏 ");
        anew.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chessPlayClick = 2;
                TimeSettingDialog dialog = new TimeSettingDialog(ChessMainFrame.this);
                dialog.setVisible(true);
                long[] timeLimits = dialog.getTimeLimits();
                if (timeLimits[0] > 0) {
                    moveTimeLimit = timeLimits[0];
                    startMoveTimer();
                }
                if (timeLimits[1] > 0) {
                    gameTimeLimit = timeLimits[1];
                    startGameTimer();
                }


                // 移除所有棋子
                for (JLabel chessPiece : play) {
                    con.remove(chessPiece);
                }
                con.remove(image);
                con.repaint();

                // 可以在这里添加代码计算新的位置，本例中暂不处理位置计算
                drawChessMan();

                //注册棋子移动监听
                con.add(image);
                con.revalidate(); // 通知布局管理器重新布局 Container
                con.repaint(); // 重绘 Container 以显示更新后的组件

                Message move = new Message();
                move.MsgTYpe = 4;
                move.moveTimeLimit = moveTimeLimit;
                move.gameTimeLimit = gameTimeLimit;
                //网络对战
                if(server != null){
                    server.sendMove(move);
                }else if(client != null) {
                    client.sendMove(move);
                }



            }
        });

		anew.setToolTipText("重新开始新的一局");
		exit = new JButton(" 退  出 ");
		exit.setToolTipText("退出象棋程序程序");
		repent = new JButton(" 悔  棋 ");
		repent.setToolTipText("返回到上次走棋的位置");
        fail = new JButton(" 认 输 ");
        fail.setToolTipText("认输后游戏重新开始");
        peace = new JButton(" 和 棋 ");
        peace.setToolTipText("和棋后游戏重新开始");
        log = new JButton(" 记 录 ");
        log.setToolTipText("查看棋局记录");
        help = new JButton(" 帮 助 ");
        help.setToolTipText("介绍游戏规则");

        log.addActionListener(e -> {
            JOptionPane.showConfirmDialog(
                    ChessMainFrame.this,logStr,"记录",
                    JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE);
        });

        help.addActionListener(e -> {
            JOptionPane.showConfirmDialog(
                    ChessMainFrame.this,helpStr,"帮助",
                    JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE);
        });

		//把组件添加到工具栏
		jmain.setLayout(new GridLayout(0,4));
		jmain.add(anew);
		jmain.add(repent);
        jmain.add(fail);
        jmain.add(peace);
        jmain.add(log);
        jmain.add(help);
		jmain.add(exit);
		jmain.add(text);
		jmain.setBounds(0,0,558,30);
		con.add(jmain);
		
		//添加棋子标签
		drawChessMan();

		//注册按扭监听
		anew.addActionListener(this);
		repent.addActionListener(this);
		exit.addActionListener(this);
        fail.addActionListener(this);
        peace.addActionListener(this);


        //添加棋盘标签
        con.add(image = new JLabel(new ImageIcon("image" + File.separator + "Main.GIF")));
        image.setBounds(0,30,558,620);
        image.addMouseListener(this);
		
		//注册窗体关闭监听
		this.addWindowListener(
			new WindowAdapter() {
				public void windowClosing(WindowEvent we){
					System.exit(0);
				}
			}
		);
		
		//窗体居中
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = this.getSize();
		
		if (frameSize.height > screenSize.height){
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width){
			frameSize.width = screenSize.width;
		}
		
		this.setLocation((screenSize.width - frameSize.width) / 2 - 280 ,(screenSize.height - frameSize.height ) / 2 - 350);
	
		//设置
		this.setIconImage(new ImageIcon("image" + File.separator + "红将.GIF").getImage());
		this.setResizable(false);
		this.setTitle(Title);
		this.setSize(558,670);
		this.show();

        startOnline(playType, serverStr);
	}

    /**
     ** 添加棋子方法
     */
    public void drawChessMan(){
        //流程控制
        int i,k;
        //图标
        Icon in;

        //黑色棋子

        //车
        in = new ImageIcon("image" + File.separator + "黑车.GIF");
        for (i=0,k=24;i<2;i++,k+=456){
            play[i] = new JLabel(in);
            play[i].setBounds(k,56,55,55);
            play[i].setName("车1");
        }

        //马
        in = new ImageIcon("image" + File.separator + "黑马.GIF");
        for (i=4,k=81;i<6;i++,k+=342){
            play[i] = new JLabel(in);
            play[i].setBounds(k,56,55,55);
            play[i].setName("马1");
        }

        //相
        in = new ImageIcon("image" + File.separator + "黑象.GIF");
        for (i=8,k=138;i<10;i++,k+=228){
            play[i] = new JLabel(in);
            play[i].setBounds(k,56,55,55);
            play[i].setName("象1");
        }

        //士
        in = new ImageIcon("image" + File.separator + "黑士.GIF");
        for (i=12,k=195;i<14;i++,k+=114){
            play[i] = new JLabel(in);
            play[i].setBounds(k,56,55,55);
            play[i].setName("士1");
        }

        //卒
        in = new ImageIcon("image" + File.separator + "黑卒.GIF");
        for (i=16,k=24;i<21;i++,k+=114){
            play[i] = new JLabel(in);
            play[i].setBounds(k,227,55,55);
            play[i].setName("卒1" + i);
        }

        //炮
        in = new ImageIcon("image" + File.separator + "黑炮.GIF");
        for (i=26,k=81;i<28;i++,k+=342){
            play[i] = new JLabel(in);
            play[i].setBounds(k,170,55,55);
            play[i].setName("炮1" + i);
        }

        //将
        in = new ImageIcon("image" + File.separator + "黑将.GIF");
        play[30] = new JLabel(in);
        play[30].setBounds(252,56,55,55);
        play[30].setName("将1");

        //红色棋子
        //车
        in = new ImageIcon("image" + File.separator + "红车.GIF");
        for (i=2,k=24;i<4;i++,k+=456){
            play[i] = new JLabel(in);
            play[i].setBounds(k,569,55,55);
            play[i].setName("车2");
        }

        //马
        in = new ImageIcon("image" + File.separator + "红马.GIF");
        for (i=6,k=81;i<8;i++,k+=342){
            play[i] = new JLabel(in);
            play[i].setBounds(k,569,55,55);
            play[i].setName("马2");
        }

        //相
        in = new ImageIcon("image" + File.separator + "红象.GIF");
        for (i=10,k=138;i<12;i++,k+=228){
            play[i] = new JLabel(in);
            play[i].setBounds(k,569,55,55);
            play[i].setName("象2");
        }

        //士
        in = new ImageIcon("image" + File.separator + "红士.GIF");
        for (i=14,k=195;i<16;i++,k+=114){
            play[i] = new JLabel(in);
            play[i].setBounds(k,569,55,55);
            play[i].setName("士2");
        }

        //兵
        in = new ImageIcon("image" + File.separator + "红卒.GIF");
        for (i=21,k=24;i<26;i++,k+=114){
            play[i] = new JLabel(in);
            play[i].setBounds(k,398,55,55);
            play[i].setName("卒2" + i);
        }

        //炮
        in = new ImageIcon("image" + File.separator + "红炮.GIF");
        for (i=28,k=81;i<30;i++,k+=342){
            play[i] = new JLabel(in);
            play[i].setBounds(k,455,55,55);
            play[i].setName("炮2" + i);
        }

        //帅
        in = new ImageIcon("image" + File.separator + "红将.GIF");
        play[31] = new JLabel(in);
        play[31].setBounds(252,569,55,55);
        play[31].setName("帅2");

        //注册棋子移动监听
        for (int j=0;j<32;j++){
            con.add(play[j]);
            play[j].addMouseListener(this);
        }

    }

    // 启动整局游戏的计时器
    public void startGameTimer() {
        gameTimer = new Timer();
        gameTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Game over: Time limit exceeded.");
                gameTimer.cancel();
                JOptionPane.showConfirmDialog(
                        ChessMainFrame.this,"游戏时间到","和棋",
                        JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE);
                chessPlayClick=3;
                text.setText("和棋");
            }
        }, gameTimeLimit);
    }


    // 启动当前玩家的计时器
    public void startMoveTimer() {
        moveTimer = new Timer();
        moveTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Game over: Time limit exceeded.");
                moveTimer.cancel();
                if(chessPlayClick == 1){
                    JOptionPane.showConfirmDialog(
                            ChessMainFrame.this,"红旗胜利","红旗胜利",
                            JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE);
                    text.setText("红棋胜利");

                }else if( chessPlayClick == 2){
                    JOptionPane.showConfirmDialog(
                            ChessMainFrame.this,"黑旗胜利","黑旗胜利",
                            JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE);
                    text.setText("黑旗胜利");
                }

                chessPlayClick=3;
            }
        }, moveTimeLimit);
    }



    public void startOnline(int playType, String serverStr) {
        if (playType == 1) {
            try {
                server = new ChessServer(12345, this);
                new Thread(() -> {
                    try {
                        server.start();
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if(playType == 2){
            try {
                client = new ChessClient(serverStr, 12345);
                new Thread(() -> {
                    try {
                        while (true) {
                            client.receiveMoves(this);
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if(playType == 3){
            //人机
        }
    }

	
	/**
	** 线程方法控制棋子闪烁
	*/
	public void run(){
		while (true){
			//单击棋子第一下开始闪烁
			if (chessManClick){
				play[Man].setVisible(false);

				//时间控制
				try{
					tmain.sleep(200);
				}
				catch(Exception e){
				}
				
				play[Man].setVisible(true);
			}
			
			//闪烁当前提示信息 以免用户看不见
			else {
				text.setVisible(false);
				
				//时间控制
				try{
					tmain.sleep(250);
				}
				catch(Exception e){
				}
				
				text.setVisible(true);
			}
			
			try{
				tmain.sleep(350);
			}
			catch (Exception e){
			}
		}
	}


	/**
	** 单击棋子方法
	*/
	public void mouseClicked(MouseEvent me){

        if(playType != 3) {

            if (currentChessPlayClick == 0) {
                currentChessPlayClick = chessPlayClick;
            }

            //在线对战时，判断当前棋子是否可走
            if (!currentPlayer()) {
                return;
            }
        }

        Message move = move(me, 0, 0);
        move.MsgTYpe = 1;
        //网络对战
        if(server != null){
            server.sendMove(move);
        }else if(client != null) {
            client.sendMove(move);
        }

	}

    /**
     * 移动棋子
     * @param me
     */
    public Message move(MouseEvent me, int moveType, int movePiece) {
        System.out.println("Mouse");

        //当前坐标
        int Ex=0,Ey=0;

        //启动线程
        if (tmain == null){
            tmain = new Thread(this);
            tmain.start();
        }

        //单击棋盘(移动棋子)
        if (image.equals(me.getSource()) || moveType == 1){
            moveType = 1;
            //该红棋走棋的时候
            if (chessPlayClick == 2 && play[Man].getName().charAt(1) == '2'){
                Ex = play[Man].getX();
                Ey = play[Man].getY();
                //移动卒、兵
                if (Man > 15 && Man < 26){
                    rule.armsRule(Man,play[Man], me);
                }

                //移动炮
                else if (Man > 25 && Man < 30){
                    rule.cannonRule(play[Man],play, me);
                }

                //移动车
                else if (Man >=0 && Man < 4){
                    rule.cannonRule(play[Man],play, me);
                }

                //移动马
                else if (Man > 3 && Man < 8){
                    rule.horseRule(play[Man],play, me);
                }

                //移动相、象
                else if (Man > 7 && Man < 12){
                    rule.elephantRule(Man,play[Man],play, me);
                }

                //移动仕、士
                else if (Man > 11 && Man < 16){
                    rule.chapRule(Man,play[Man],play, me);
                }

                //移动将、帅
                else if (Man == 30 || Man == 31){
                    rule.willRule(Man,play[Man],play, me);
                }

                //是否走棋错误(是否在原地没有动)
                if (Ex == play[Man].getX() && Ey == play[Man].getY()){
                    text.setText("               红棋走棋");
                    chessPlayClick=2;
                }

                else {
                    text.setText("               黑棋走棋");
                    chessPlayClick=1;
                }



            }//if

            //该黑棋走棋的时候
            else if (chessPlayClick == 1 && play[Man].getName().charAt(1) == '1'){
                Ex = play[Man].getX();
                Ey = play[Man].getY();

                //移动卒、兵
                if (Man > 15 && Man < 26){
                    rule.armsRule(Man,play[Man], me);
                }

                //移动炮
                else if (Man > 25 && Man < 30){
                    rule.cannonRule(play[Man],play, me);
                }

                //移动车
                else if (Man >=0 && Man < 4){
                    rule.cannonRule(play[Man],play, me);
                }

                //移动马
                else if (Man > 3 && Man < 8){
                    rule.horseRule(play[Man],play, me);
                }

                //移动相、象
                else if (Man > 7 && Man < 12){
                    rule.elephantRule(Man,play[Man],play, me);
                }

                //移动仕、士
                else if (Man > 11 && Man < 16){
                    rule.chapRule(Man,play[Man],play, me);
                }

                //移动将、帅
                else if (Man == 30 || Man == 31){
                    rule.willRule(Man,play[Man],play, me);
                }

                //是否走棋错误(是否在原地没有动)
                if (Ex == play[Man].getX() && Ey == play[Man].getY()){
                    text.setText("               黑棋走棋");
                    chessPlayClick=1;
                }

                else {
                    text.setText("               红棋走棋");
                    chessPlayClick=2;
                }


            }//else if

            //当前没有操作(停止闪烁)
            chessManClick=false;


            log(Ex, Ey);

        }//if

        //单击棋子
        else{
            //第一次单击棋子(闪烁棋子)
            if (!chessManClick){
                if(moveType == 2){
                    Man = movePiece;
                    chessManClick=true;
                }else {
                    for (int i=0;i<32;i++){
                        //被单击的棋子
                        if (me.getSource().equals(play[i])){
                            //告诉线程让该棋子闪烁
                            Man=i;
                            //开始闪烁
                            chessManClick=true;
                            moveType = 2;
                            movePiece = i;
                            break;
                        }
                    }//for
                }
            }//if

            //第二次单击棋子(吃棋子)
            else if (chessManClick){
                //当前没有操作(停止闪烁)
                chessManClick=false;

                for (i=0;i<32;i++){
                    //找到被吃的棋子
                    if (play[i].equals(me.getSource()) || (i == movePiece && moveType == 3) ){
                        movePiece = i;
                        moveType = 3;
                        //该红棋吃棋的时候
                        if (chessPlayClick == 2 && play[Man].getName().charAt(1) == '2'){
                            Ex = play[Man].getX();
                            Ey = play[Man].getY();

                            //卒、兵吃规则
                            if (Man > 15 && Man < 26){
                                rule.armsRule(play[Man],play[i]);
                            }

                            //炮吃规则
                            else if (Man > 25 && Man < 30){
                                rule.cannonRule(0,play[Man],play[i],play, me);
                            }

                            //车吃规则
                            else if (Man >=0 && Man < 4){
                                rule.cannonRule(1,play[Man],play[i],play, me);
                            }

                            //马吃规则
                            else if (Man > 3 && Man < 8){
                                rule.horseRule(play[Man],play[i],play, me);
                            }

                            //相、象吃规则
                            else if (Man > 7 && Man < 12){
                                rule.elephantRule(play[Man],play[i],play);
                            }

                            //士、仕吃棋规则
                            else if (Man > 11 && Man < 16){
                                rule.chapRule(Man,play[Man],play[i],play);
                            }

                            //将、帅吃棋规则
                            else if (Man == 30 || Man == 31){
                                rule.willRule(Man,play[Man],play[i],play);
                                play[Man].setVisible(true);
                            }

                            //是否走棋错误(是否在原地没有动)
                            if (Ex == play[Man].getX() && Ey == play[Man].getY()){
                                text.setText("               红棋走棋");
                                chessPlayClick=2;
                                break;
                            }

                            else{
                                text.setText("               黑棋走棋");
                                chessPlayClick=1;
                                break;
                            }

                        }//if

                        //该黑棋吃棋的时候
                        else if (chessPlayClick == 1 && play[Man].getName().charAt(1) == '1'){
                            Ex = play[Man].getX();
                            Ey = play[Man].getY();

                            //卒吃规则
                            if (Man > 15 && Man < 26){
                                rule.armsRule(play[Man],play[i]);
                            }

                            //炮吃规则
                            else if (Man > 25 && Man < 30){
                                rule.cannonRule(0,play[Man],play[i],play, me);
                            }

                            //车吃规则
                            else if (Man >=0 && Man < 4){
                                rule.cannonRule(1,play[Man],play[i],play, me);
                            }

                            //马吃规则
                            else if (Man > 3 && Man < 8){
                                rule.horseRule(play[Man],play[i],play, me);
                            }

                            //相、象吃规则
                            else if (Man > 7 && Man < 12){
                                rule.elephantRule(play[Man],play[i],play);
                            }

                            //士、仕吃棋规则
                            else if (Man > 11 && Man < 16){
                                rule.chapRule(Man,play[Man],play[i],play);
                            }

                            //将、帅吃棋规则
                            else if (Man == 30 || Man == 31){
                                rule.willRule(Man,play[Man],play[i],play);
                                play[Man].setVisible(true);
                            }

                            //是否走棋错误(是否在原地没有动)
                            if (Ex == play[Man].getX() && Ey == play[Man].getY()){
                                text.setText("               黑棋走棋");
                                chessPlayClick=1;
                                break;
                            }

                            else {
                                text.setText("               红棋走棋");
                                chessPlayClick=2;
                                break;
                            }

                        }//else if

                    }//if

                }//for


                //是否胜利
                if (!play[31].isVisible()){
                    JOptionPane.showConfirmDialog(
                        this,"黑棋胜利","玩家一胜利",
                        JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE);
                    //双方都不可以在走棋了
                    chessPlayClick=3;
                    text.setText("  黑棋胜利");

                }//if

                else if (!play[30].isVisible()){
                    JOptionPane.showConfirmDialog(
                        this,"红棋胜利","玩家二胜利",
                        JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE);
                    chessPlayClick=3;
                    text.setText("  红棋胜利");
                }//else if

            }//else

        }//else

        return new Message(me, moveType, movePiece);
    }

    private boolean currentPlayer() {
        return currentChessPlayClick == 0 || currentChessPlayClick == chessPlayClick;
    }

    public void mousePressed(MouseEvent me){
	}
	public void mouseReleased(MouseEvent me){
	}
	public void mouseEntered(MouseEvent me){
	}
	public void mouseExited(MouseEvent me){
	}
	
	/**
	** 定义按钮的事件响应
	*/
	public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == anew) {
            resetGame();
        } else if (source == fail) {
            int result = JOptionPane.showConfirmDialog(this, "你确定要认输吗？", "认输确认", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                if(chessPlayClick == 1){
                    JOptionPane.showConfirmDialog(
                            ChessMainFrame.this,"红旗胜利","红旗胜利",
                            JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE);
                    text.setText("红棋胜利");

                }else if( chessPlayClick == 2){
                    JOptionPane.showConfirmDialog(
                            ChessMainFrame.this,"黑旗胜利","黑旗胜利",
                            JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE);
                    text.setText("黑旗胜利");
                }

                Message msg = new Message();
                msg.MsgTYpe = 2;
                //网络对战
                if(server != null){
                    server.sendMove(msg);
                }else if(client != null) {
                    client.sendMove(msg);
                }


            }
        } else if (source == peace) {
            int result = JOptionPane.showConfirmDialog(this, "你确定要和棋吗？", "和棋确认", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(this, "双方和棋！");
                resetGame();
            }
            Message msg = new Message();
            msg.MsgTYpe = 3;
            //网络对战
            if(server != null){
                server.sendMove(msg);
            }else if(client != null) {
                client.sendMove(msg);
            }
        }  else if (source == exit) {
            System.exit(0);
        }
	}

	/*定义中国象棋规则的类*/
	class ChessRule implements Serializable {
		/**卒子的移动规则*/
		public void armsRule(int Man,JLabel play,MouseEvent me){
			//黑卒向下
			if (Man < 21){
				//向下移动、得到终点的坐标模糊成合法的坐标
				if ((me.getY()-play.getY()) > 27 && (me.getY()-play.getY()) < 86 && (me.getX()-play.getX()) < 55 && (me.getX()-play.getX()) > 0){
					
					//当前记录添加到集合(用于悔棋)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(play.getX(),play.getY()+57,55,55);
				}
				
				//向右移动、得到终点的坐标模糊成合法的坐标、必须过河				
				else if (play.getY() > 284 && (me.getX() - play.getX()) >= 57 && (me.getX() - play.getX()) <= 112){
					play.setBounds(play.getX()+57,play.getY(),55,55);
				}
				
				//向左移动、得到终点的坐标模糊成合法的坐标、必须过河
				else if (play.getY() > 284 && (play.getX() - me.getX()) >= 2 && (play.getX() - me.getX()) <=58){
					//模糊坐标
					play.setBounds(play.getX()-57,play.getY(),55,55);
				}
			}
			
			//红卒向上
			else{
				//当前记录添加到集合(用于悔棋)
				Var.add(String.valueOf(play.isVisible()));
				Var.add(String.valueOf(play.getX()));
				Var.add(String.valueOf(play.getY()));
				Var.add(String.valueOf(Man));
				
				//向上移动、得到终点的坐标模糊成合法的坐标
				if ((me.getX()-play.getX()) >= 0 && (me.getX()-play.getX()) <= 55 && (play.getY()-me.getY()) >27 && play.getY()-me.getY() < 86){
					play.setBounds(play.getX(),play.getY()-57,55,55);
				}
				
				//向右移动、得到终点的坐标模糊成合法的坐标、必须过河
				else if (play.getY() <= 341 && (me.getX() - play.getX()) >= 57 && (me.getX() - play.getX()) <= 112){
					play.setBounds(play.getX()+57,play.getY(),55,55);
				}				
				
				//向左移动、得到终点的坐标模糊成合法的坐标、必须过河
				else if (play.getY() <= 341 && (play.getX() - me.getX()) >= 3 && (play.getX() - me.getX()) <=58){
					play.setBounds(play.getX()-57,play.getY(),55,55);
				}
			}
		}//卒移动结束

		/**卒吃棋规则*/
		public void armsRule(JLabel play1,JLabel play2){
			//向右走
			if ((play2.getX() - play1.getX()) <= 112 && (play2.getX() - play1.getX()) >= 57 && (play1.getY() - play2.getY()) < 22 && (play1.getY() - play2.getY()) > -22 && play2.isVisible() && play1.getName().charAt(1)!=play2.getName().charAt(1)){
				//黑棋要过河才能右吃棋
				if (play1.getName().charAt(1) == '1' && play1.getY() > 284 && play1.getName().charAt(1) != play2.getName().charAt(1)){

					play2.setVisible(false);
					//把对方的位置给自己
					play1.setBounds(play2.getX(),play2.getY(),55,55);
				}
				
				//红棋要过河才左能吃棋
				else if (play1.getName().charAt(1) == '2' && play1.getY() < 341 && play1.getName().charAt(1) != play2.getName().charAt(1)){
					play2.setVisible(false);
					//把对方的位置给自己
					play1.setBounds(play2.getX(),play2.getY(),55,55);				
				}
			}
			
			//向左走
			else if ((play1.getX() - play2.getX()) <= 112 && (play1.getX() - play2.getX()) >= 57 && (play1.getY() - play2.getY()) < 22 && (play1.getY() - play2.getY()) > -22 && play2.isVisible() && play1.getName().charAt(1)!=play2.getName().charAt(1)){
				//黑棋要过河才能左吃棋
				if (play1.getName().charAt(1) == '1' && play1.getY() > 284 && play1.getName().charAt(1) != play2.getName().charAt(1)){
					play2.setVisible(false);
					//把对方的位置给自己
					play1.setBounds(play2.getX(),play2.getY(),55,55);
				}
				
				//红棋要过河才能右吃棋
				else if (play1.getName().charAt(1) == '2' && play1.getY() < 341 && play1.getName().charAt(1) != play2.getName().charAt(1)){
					play2.setVisible(false);
					//把对方的位置给自己
					play1.setBounds(play2.getX(),play2.getY(),55,55);				
				}
			}
			
			//向上走
			else if (play1.getX() - play2.getX() >= -22 && play1.getX() - play2.getX() <= 22 && play1.getY() - play2.getY() >= -112 && play1.getY() - play2.getY() <= 112){
				//黑棋不能向上吃棋
				if (play1.getName().charAt(1) == '1' && play1.getY() < play2.getY() && play1.getName().charAt(1) != play2.getName().charAt(1)){
					play2.setVisible(false);
					//把对方的位置给自己
					play1.setBounds(play2.getX(),play2.getY(),55,55);
				}
				
				//红棋不能向下吃棋
				else if (play1.getName().charAt(1) == '2' && play1.getY() > play2.getY() && play1.getName().charAt(1) != play2.getName().charAt(1)){
					play2.setVisible(false);
					//把对方的位置给自己
					play1.setBounds(play2.getX(),play2.getY(),55,55);
				}			
			}
			
			//当前记录添加到集合(用于悔棋)
			Var.add(String.valueOf(play1.isVisible()));
			Var.add(String.valueOf(play1.getX()));
			Var.add(String.valueOf(play1.getY()));
			Var.add(String.valueOf(Man));
			
			//当前记录添加到集合(用于悔棋)
			Var.add(String.valueOf(play2.isVisible()));
			Var.add(String.valueOf(play2.getX()));
			Var.add(String.valueOf(play2.getY()));
			Var.add(String.valueOf(i));

		}//卒吃结束
		
		/**炮、车移动规则*/
		public void cannonRule(JLabel play,JLabel playQ[],MouseEvent me){
			//起点和终点之间是否有棋子
			int Count = 0;
			
			//上、下移动
			if (play.getX() - me.getX() <= 0 && play.getX() - me.getX() >= -55){
				//指定所有模糊Y坐标
				for (int i=56;i<=571;i+=57){
					//移动的Y坐标是否有指定坐标相近的
					if (i - me.getY() >= -27 && i - me.getY() <= 27){
						//所有的棋子
						for (int j=0;j<32;j++){
							//找出在同一条竖线的所有棋子、并不包括自己
							if (playQ[j].getX() - play.getX() >= -27 && playQ[j].getX() - play.getX() <= 27 && playQ[j].getName()!=play.getName() && playQ[j].isVisible()){
								//从起点到终点(从左到右)
								for (int k=play.getY()+57;k<i;k+=57){
									//大于起点、小于终点的坐标就可以知道中间是否有棋子
									if (playQ[j].getY() < i && playQ[j].getY() > play.getY()){
										//中间有一个棋子就不可以从这条竖线过去
										Count++;
										break;
									}
								}//for
								
								//从起点到终点(从右到左)
								for (int k=i+57;k<play.getY();k+=57){
									//找起点和终点的棋子
									if (playQ[j].getY() < play.getY() && playQ[j].getY() > i){
										Count++;
										break;
									}
								}//for
							}//if
						}//for
						
						//起点和终点没有棋子就可以移动了
						if (Count == 0){
							//当前记录添加到集合(用于悔棋)
							Var.add(String.valueOf(play.isVisible()));
							Var.add(String.valueOf(play.getX()));
							Var.add(String.valueOf(play.getY()));
							Var.add(String.valueOf(Man));
							play.setBounds(play.getX(),i,55,55);
							break;
						}
					}//if
				}//for
			}//if

			//左、右移动
			else if (play.getY() - me.getY() >=-27 && play.getY() - me.getY() <= 27){
				//指定所有模糊X坐标
				for (int i=24;i<=480;i+=57){
					//移动的X坐标是否有指定坐标相近的
					if (i - me.getX() >= -55 && i-me.getX() <= 0){
						//所有的棋子
						for (int j=0;j<32;j++){
							//找出在同一条横线的所有棋子、并不包括自己
							if (playQ[j].getY() - play.getY() >= -27 && playQ[j].getY() - play.getY() <= 27 && playQ[j].getName()!=play.getName() && playQ[j].isVisible()){
								//从起点到终点(从上到下)				
								for (int k=play.getX()+57;k<i;k+=57){
									//大于起点、小于终点的坐标就可以知道中间是否有棋子
									if (playQ[j].getX() < i && playQ[j].getX() > play.getX()){
										//中间有一个棋子就不可以从这条横线过去
										Count++;
										break;
									}
								}//for
								
								//从起点到终点(从下到上)
								for (int k=i+57;k<play.getX();k+=57){
									//找起点和终点的棋子
									if (playQ[j].getX() < play.getX() && playQ[j].getX() > i){
										Count++;
										break;
									}
								}//for
							}//if
						}//for
						
						//起点和终点没有棋子
						if (Count == 0){
							//当前记录添加到集合(用于悔棋)
							Var.add(String.valueOf(play.isVisible()));
							Var.add(String.valueOf(play.getX()));
							Var.add(String.valueOf(play.getY()));
							Var.add(String.valueOf(Man));
							
							play.setBounds(i,play.getY(),55,55);
							break;
						}
					}//if
				}//for
			}//else
			
		}//炮、车移动方法结束


		/**炮、车吃棋规则*/
		public void cannonRule(int Chess,JLabel play,JLabel playTake,JLabel playQ[],MouseEvent me){
			//起点和终点之间是否有棋子
			int Count = 0;


			//所有的棋子
			for (int j=0;j<32;j++){
				//找出在同一条竖线的所有棋子、并不包括自己
				if (playQ[j].getX() - play.getX() >= -27 && playQ[j].getX() - play.getX() <= 27 && playQ[j].getName()!=play.getName() && playQ[j].isVisible()){

					//自己是起点被吃的是终点(从上到下)
					for (int k=play.getY()+57;k<playTake.getY();k+=57){
						//大于起点、小于终点的坐标就可以知道中间是否有棋子
						if (playQ[j].getY() < playTake.getY() && playQ[j].getY() > play.getY()){
								//计算起点和终点的棋子个数
								Count++;			
								break;							
						}
					}//for
								
					//自己是起点被吃的是终点(从下到上)
					for (int k=playTake.getY();k<play.getY();k+=57){
						//找起点和终点的棋子
						if (playQ[j].getY() < play.getY() && playQ[j].getY() > playTake.getY()){
								Count++;	
								break;
						}
					}//for
				}//if
							
				//找出在同一条竖线的所有棋子、并不包括自己
				else if (playQ[j].getY() - play.getY() >= -10 && playQ[j].getY() - play.getY() <= 10 && playQ[j].getName()!=play.getName() && playQ[j].isVisible()){
					//自己是起点被吃的是终点(从左到右)
					for (int k=play.getX()+50;k<playTake.getX();k+=57){
						//大于起点、小于终点的坐标就可以知道中间是否有棋子						
						if (playQ[j].getX() < playTake.getX() && playQ[j].getX() > play.getX()){
							Count++;			
							break;	
						}
					}//for
								
					//自己是起点被吃的是终点(从右到左)
					for (int k=playTake.getX();k<play.getX();k+=57){
						//找起点和终点的棋子
						if (playQ[j].getX() < play.getX() && playQ[j].getX() > playTake.getX()){
								Count++;
								break;
						}
					}//for
				}//if
			}//for
						
			//起点和终点之间要一个棋子是炮的规则、并不能吃自己的棋子
			if (Count == 1 && Chess == 0 && playTake.getName().charAt(1) != play.getName().charAt(1)){
				//当前记录添加到集合(用于悔棋)
				Var.add(String.valueOf(play.isVisible()));
				Var.add(String.valueOf(play.getX()));
				Var.add(String.valueOf(play.getY()));
				Var.add(String.valueOf(Man));
				
				//当前记录添加到集合(用于悔棋)
				Var.add(String.valueOf(playTake.isVisible()));
				Var.add(String.valueOf(playTake.getX()));									
				Var.add(String.valueOf(playTake.getY()));
				Var.add(String.valueOf(i));
				
				playTake.setVisible(false);
				play.setBounds(playTake.getX(),playTake.getY(),55,55);
			}
			
			//起点和终点之间没有棋子是车的规则、并不能吃自己的棋子			
			else if (Count ==0  && Chess == 1 && playTake.getName().charAt(1) != play.getName().charAt(1)){
				
				//当前记录添加到集合(用于悔棋)
				Var.add(String.valueOf(play.isVisible()));
				Var.add(String.valueOf(play.getX()));									
				Var.add(String.valueOf(play.getY()));
				Var.add(String.valueOf(Man));
				
				//当前记录添加到集合(用于悔棋)
				Var.add(String.valueOf(playTake.isVisible()));
				Var.add(String.valueOf(playTake.getX()));									
				Var.add(String.valueOf(playTake.getY()));
				Var.add(String.valueOf(i));
				
				playTake.setVisible(false);
				play.setBounds(playTake.getX(),playTake.getY(),55,55);
			}
			
		}//炮、车吃棋方法结束
		
		/**马移动规则*/
		public void horseRule(JLabel play,JLabel playQ[],MouseEvent me){
			//保存坐标和障碍
			int Ex=0,Ey=0,Move=0;			
			
			//上移、左边
			if (play.getX() - me.getX() >= 2 && play.getX() - me.getX() <= 57 && play.getY() - me.getY() >= 87 && play.getY() - me.getY() <= 141){
				//合法的Y坐标
				for (int i=56;i<=571;i+=57){
					//移动的Y坐标是否有指定坐标相近的
					if (i - me.getY() >= -27 && i - me.getY() <= 27){
						Ey = i;
						break;
					}
				}
				
				//合法的X坐标
				for (int i=24;i<=480;i+=57){
					//移动的X坐标是否有指定坐标相近的
					if (i - me.getX() >= -55 && i-me.getX() <= 0){
						Ex = i;
						break;
					}
				}
				
				//正前方是否有别的棋子
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getX() - playQ[i].getX() == 0  && play.getY() - playQ[i].getY() == 57 ){
						Move = 1;
						break;
					}	
				}
				
				//可以移动该棋子
				if (Move == 0){
					//当前记录添加到集合(用于悔棋)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
									
					play.setBounds(Ex,Ey,55,55);
				}
				
			}//if
			
			//左移、上边
			else if (play.getY() - me.getY() >= 27 && play.getY() - me.getY() <= 86 && play.getX() - me.getX() >= 70 && play.getX() - me.getX() <= 130){
				//Y
				for (int i=56;i<=571;i+=57){
					if (i - me.getY() >= -27 && i - me.getY() <= 27){
						Ey = i;
					}
				}
				
				//X
				for (int i=24;i<=480;i+=57){
					if (i - me.getX() >= -55 && i-me.getX() <= 0){
						Ex = i;
					}
				}
				
				//正左方是否有别的棋子
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getY() - playQ[i].getY() == 0 && play.getX() - playQ[i].getX() == 57 ){
						Move = 1;
						break;
					}
				}
				
				if (Move == 0){
					//当前记录添加到集合(用于悔棋)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(Ex,Ey,55,55);
				}
			}//else
			
			//下移、右边
			else if (me.getY() - play.getY() >= 87 && me.getY() - play.getY() <= 141 && me.getX() - play.getX() <= 87 && me.getX() - play.getX() >= 2 ){	
				//Y		
				for (int i=56;i<=571;i+=57){
					if (i - me.getY() >= -27 && i - me.getY() <= 27){
						Ey = i;
					}
				}
				
				//X
				for (int i=24;i<=480;i+=57){
					if (i - me.getX() >= -55 && i-me.getX() <= 0){
						Ex = i;
					}
				}
				
				//正下方是否有别的棋子
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getX() - playQ[i].getX() == 0  && playQ[i].getY() - play.getY() == 57 ){
						Move = 1;
						break;
					}
				}
				
				if (Move == 0){
					//当前记录添加到集合(用于悔棋)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(Ex,Ey,55,55);
				}
			}//else
			
			//上移、右边
			else if (play.getY() - me.getY() >= 87 && play.getY() - me.getY() <= 141 && me.getX() - play.getX() <= 87 && me.getX() - play.getX() >= 30 ){
				//合法的Y坐标
				for (int i=56;i<=571;i+=57){
					if (i - me.getY() >= -27 && i - me.getY() <= 27){
						Ey = i;
						break;
					}
				}
				
				//合法的X坐标
				for (int i=24;i<=480;i+=57){
					if (i - me.getX() >= -55 && i-me.getX() <= 0){
						Ex = i;
						break;
					}
				}
				
				//正前方是否有别的棋子
				for (int i=0;i<32;i++){
					System.out.println(i+"playQ[i].getX()="+playQ[i].getX());
					//System.out.println("play.getX()="+play.getX());
					if (playQ[i].isVisible() && play.getX() - playQ[i].getX() == 0 && play.getY() - playQ[i].getY() == 57 ){
						Move = 1;
						//System.out.println("play.getY()="+play.getY());
						//System.out.println("playQ[i].getY()="+playQ[i].getY());
						break;
					}
				}
				
				//可以移动该棋子
				if (Move == 0){
					//当前记录添加到集合(用于悔棋)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));	
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(Ex,Ey,55,55);
				}
			}//else 
			
			//下移、左边
			else if (me.getY() - play.getY() >= 87 && me.getY() - play.getY() <= 141 && play.getX() - me.getX() <= 87 && play.getX() - me.getX() >= 10 ){
				//合法的Y坐标
				for (int i=56;i<=571;i+=57){
					if (i - me.getY() >= -27 && i - me.getY() <= 27){
						Ey = i;
						break;
					}
				}
				
				//合法的X坐标
				for (int i=24;i<=480;i+=57){
					if (i - me.getX() >= -55 && i-me.getX() <= 0){
						Ex = i;
						break;
					}
				}
				
				//正下方是否有别的棋子
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getX() - playQ[i].getX() == 0 && play.getY() - playQ[i].getY() == 57 ){
						Move = 1;
						break;
					}
				}
				
				//可以移动该棋子
				if (Move == 0){
					//当前记录添加到集合(用于悔棋)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(Ex,Ey,55,55);
				}
			}//else
			
			//右移、上边
			else if (play.getY() - me.getY() >= 30 && play.getY() - me.getY() <= 87 && me.getX() - play.getX() <= 141 && me.getX() - play.getX() >= 87 ){
				//Y		
				for (int i=56;i<=571;i+=57){
					if (i - me.getY() >= -27 && i - me.getY() <= 27){
						Ey = i;
					}
				}
				
				//X
				for (int i=24;i<=480;i+=57){
					if (i - me.getX() >= -55 && i-me.getX() <= 0){
						Ex = i;
					}
				}
				
				//正右方是否有别的棋子
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getY() - playQ[i].getY() == 0 && playQ[i].getX() - play.getX() == 57 ){
						Move = 1;
						break;
					}
				}
				
				if (Move == 0){
					//当前记录添加到集合(用于悔棋)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(Ex,Ey,55,55);
				}
			}//else
			
			//右移、下边
			else if (me.getY() - play.getY() >= 30 && me.getY() - play.getY() <= 87 && me.getX() - play.getX() <= 141 && me.getX() - play.getX() >= 87 ){
				//Y		
				for (int i=56;i<=571;i+=57){
					if (i - me.getY() >= -27 && i - me.getY() <= 27){
						Ey = i;
					}
				}
				
				//X
				for (int i=24;i<=480;i+=57){
					if (i - me.getX() >= -55 && i-me.getX() <= 0){
						Ex = i;
					}
				}
				
				//正右方是否有别的棋子
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getY() - playQ[i].getY() == 0 && playQ[i].getX() - play.getX() == 57 ){
						Move = 1;
						break;
					}
				}
				
				if (Move == 0){
					//当前记录添加到集合(用于悔棋)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(Ex,Ey,55,55);
				}
			}//else
			
			//左移、下边
			else if (me.getY() - play.getY() >= 30 && me.getY() - play.getY() <= 87 && play.getX() - me.getX() <= 141 && play.getX() - me.getX() >= 87 ){
				//Y		
				for (int i=56;i<=571;i+=57){
					if (i - me.getY() >= -27 && i - me.getY() <= 27){
						Ey = i;
					}
				}
				
				//X
				for (int i=24;i<=480;i+=57){
					if (i - me.getX() >= -55 && i-me.getX() <= 0){
						Ex = i;
					}
				}
				
				//正左方是否有别的棋子
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getY() - playQ[i].getY() == 0 && play.getX() - playQ[i].getX() == 57 ){
						Move = 1;
						break;
					}
				}
				
				if (Move == 0){
					//当前记录添加到集合(用于悔棋)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
				
					play.setBounds(Ex,Ey,55,55);
				}
				
			}//else
			
		}//马移动结束

		/**马吃棋规则*/
		public void horseRule(JLabel play,JLabel playTake ,JLabel playQ[],MouseEvent me){
			//障碍
			int Move=0;
			boolean Chess=false;
			
			//上移、左吃
			if (play.getName().charAt(1)!=playTake.getName().charAt(1) && play.getX() - playTake.getX() == 57 && play.getY() - playTake.getY() == 114 ){
				//正前方是否有别的棋子
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getX() - playQ[i].getX() == 0 && play.getY() - playQ[i].getY() == 57){
						Move = 1;
						break;
					}
				}//for
				
				Chess = true;
				
			}//if
			
			//上移、右吃
			else if (play.getY() - playTake.getY() == 114 && playTake.getX() - play.getX() == 57 ){
				//正前方是否有别的棋子
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getX() - playQ[i].getX() == 0 && play.getY() - playQ[i].getY() == 57){
						Move = 1;
						break;
					}
				}//for		
				
				Chess = true;
				
			}//else
			
			//左移、上吃
			else if (play.getY() - playTake.getY() == 57 && play.getX() - playTake.getX() == 114 ){
				//正左方是否有别的棋子
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getY() - playQ[i].getY() == 0 && play.getX() - playQ[i].getX() == 57){
						Move = 1;
						break;
					}
				}//for
				
				Chess = true;
				
			}//else
			
			//左移、下吃
			else if (playTake.getY() - play.getY() == 57 && play.getX() - playTake.getX() == 114 ){
				//正左方是否有别的棋子
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getY() - playQ[i].getY() == 0 && play.getX() - playQ[i].getX() == 57){
						Move = 1;
						break;
					}
				}//for
				
				Chess = true;
				
			}//else
			
			//右移、上吃
			else if (play.getY() - playTake.getY() == 57 && playTake.getX() - play.getX() == 114 ){
				//正右方是否有别的棋子
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getY() - playQ[i].getY() == 0 && playQ[i].getX() - play.getX() == 57){
						Move = 1;
						break;
					}
				}//for
				
				Chess = true;
				
			}//else
			
			//右移、下吃
			else if (playTake.getY() - play.getY() == 57  && playTake.getX() - play.getX() == 114 ){
				//正右方是否有别的棋子
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getY() - playQ[i].getY() == 0 && playQ[i].getX() - play.getX() == 57){
						Move = 1;
						break;
					}
				}//for
				
				Chess = true;
				
			}//else
			
			//下移、左吃
			else if (playTake.getY() - play.getY() == 114 && play.getX() - playTake.getX() == 57 ){
				//正下方是否有别的棋子
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getX() - playQ[i].getX() == 0 && play.getY() - playQ[i].getY() == -57 ){
						Move = 1;
						break;
						
					}
				}//for
				
				Chess = true;
				
			}//else 
			
			//下移、右吃
			else if (playTake.getY() - play.getY() == 114 && playTake.getX() - play.getX() == 57){
				//正下方是否有别的棋子
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getX() - playQ[i].getX() == 0 && play.getY() - playQ[i].getY() == -57 ){
						Move = 1;
						break;
					}
				}//for
				
				Chess = true;
				
			}//else  
			
			//没有障碍、并可以吃棋、不能吃自己颜色
			if (Chess && Move == 0 && playTake.getName().charAt(1) != play.getName().charAt(1)){
				//当前记录添加到集合(用于悔棋)
				Var.add(String.valueOf(play.isVisible()));
				Var.add(String.valueOf(play.getX()));
				Var.add(String.valueOf(play.getY()));
				Var.add(String.valueOf(Man));
				
				//当前记录添加到集合(用于悔棋)
				Var.add(String.valueOf(playTake.isVisible()));
				Var.add(String.valueOf(playTake.getX()));
				Var.add(String.valueOf(playTake.getY()));
				Var.add(String.valueOf(i));			
				
				playTake.setVisible(false);
				play.setBounds(playTake.getX(),playTake.getY(),55,55);
			}
		}
		
		/**相移动规则*/
		public void elephantRule(int Man,JLabel play,JLabel playQ[],MouseEvent me){
			//坐标和障碍
			int Ex=0,Ey=0,Move=0;
			
			//上左
			if (play.getX() - me.getX() <= 141 && play.getX() - me.getX() >= 87 && play.getY() - me.getY() <= 141 && play.getY() - me.getY() >= 87){
				//合法的Y坐标
				for (int i=56;i<=571;i+=57){
					if (i - me.getY() >= -27 && i - me.getY() <= 27){
						Ey = i;
						break;
					}
				}
				
				//合法的X坐标
				for (int i=24;i<=480;i+=57){
					if (i - me.getX() >= -27 && i-me.getX() <= 27){
						Ex = i;
						break;
					}
				}
				
				//左上方是否有棋子
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getX() - playQ[i].getX() == 57 && play.getY() - playQ[i].getY() == 57){
						Move++;
						break;
					}
				}
				
				//红旗不能过楚河
				if (Move == 0 && Ey >= 341 && Man > 9){
					//当前记录添加到集合(用于悔棋)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
								
								System.out.println("Ex="+Ex);
								System.out.println("Ey="+Ey);
					play.setBounds(Ex,Ey,55,55);
				}
				
				//黑旗不能过汉界
				else if (Move == 0 && Ey <= 284 && Man < 10){
					//当前记录添加到集合(用于悔棋)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(Ex,Ey,55,55);	
				}
			}//if
			
			//上右
			else if (play.getY() - me.getY() <= 141 && play.getY() - me.getY() >= 87 &&  me.getX() - play.getX() >= 87 && me.getX() - play.getX() <= 141){
				//合法的Y坐标
				for (int i=56;i<=571;i+=57){
					if (i - me.getY() >= -27 && i - me.getY() <= 27){
						Ey = i;
						break;
					}
				}
				
				//合法的X坐标
				for (int i=24;i<=480;i+=57){
					if (i - me.getX() >= -27 && i-me.getX() <= 27){
						Ex = i;
						break;
					}
				}
				
				//右上方是否有棋子
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() &&  playQ[i].getX() - play.getX() == 57 && play.getY() - playQ[i].getY() == 57){
						Move++;
						break;
					}
				}
				
				//相、象规则
				if (Move == 0 && Ey >= 341 && Man > 9){
					//当前记录添加到集合(用于悔棋)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(Ex,Ey,55,55);
				}
				
				else if (Move == 0 && Ey <= 284 && Man < 10){
					//当前记录添加到集合(用于悔棋)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(Ex,Ey,55,55);
				}
				
			}// else if 
			
			//下左
			else if (play.getX() - me.getX() <= 141 && play.getX() - me.getX() >= 87 && me.getY() - play.getY() <= 141 && me.getY() - play.getY() >= 87){
				//合法的Y坐标
				for (int i=56;i<=571;i+=57){
					if (i - me.getY() >= -27 && i - me.getY() <= 27){
						Ey = i;
						break;
					}
				}
				
				//合法的X坐标
				for (int i=24;i<=480;i+=57){
					if (i - me.getX() >= -27 && i-me.getX() <= 27){
						Ex = i;
						break;
					}
				}
				
				//下左方是否有棋子
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getX() - playQ[i].getX() == 57 && play.getY() - playQ[i].getY() == -57){
						Move++;
						break;
					}
				}			
				
				//相、象规则
				
				if (Move == 0 && Ey >= 341 && Man > 9){
					//当前记录添加到集合(用于悔棋)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
									
					play.setBounds(Ex,Ey,55,55);
				}
				
				else if (Move == 0 && Ey <= 284 && Man < 10)
				{
					//当前记录添加到集合(用于悔棋)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(Ex,Ey,55,55);
				}
			}//else if 
			
			//下右
			else if (me.getX() - play.getX() >= 87 &&  me.getX() - play.getX() <= 141 && me.getY() - play.getY() >= 87 && me.getY() - play.getY() <= 141){
				//Y		
				for (int i=56;i<=571;i+=57){
					if (i - me.getY() >= -27 && i - me.getY() <= 27){
						Ey = i;
					}
				}
				
				//X
				for (int i=24;i<=480;i+=57){
					if (i - me.getX() >= -27 && i-me.getX() <= 27){
						Ex = i;
					}
				}
				
				//下右方是否有棋子
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && playQ[i].getX() - play.getX() == 57 && playQ[i].getY() - play.getY() == 57){
						Move = 1;
						break;
					}
				}
				
				//相、象规则
				if (Move == 0 && Ey >= 341 && Man > 9){
					//当前记录添加到集合(用于悔棋)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(Ex,Ey,55,55);
				}
				
				else if (Move == 0 && Ey <= 284 && Man < 10){
					//当前记录添加到集合(用于悔棋)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));									
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(Ex,Ey,55,55);
				}
				
			}//else
			
		}//相移动规则吉束

		/**相、象吃棋规则*/
		public void elephantRule(JLabel play,JLabel playTake,JLabel playQ[]){
			//障碍
			int Move=0;
			boolean Chess=false;
			
			//吃左上方的棋子
			if (play.getX() - playTake.getX() >= 87 && play.getX() - playTake.getX() <= 141 && play.getY() - playTake.getY() >= 87 && play.getY() - playTake.getY() <= 141){
				//左上方是否有棋子
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getX() - playQ[i].getX() == 57 && play.getY() - playQ[i].getY() == 57){
						Move++;
						break;
					}
				}//for

				Chess=true;
				
			}//if
			
			//吃右上方的棋子
			else if (playTake.getX() - play.getX() >= 87 && playTake.getX() - play.getX() <= 141 && play.getY() - playTake.getY() >= 87 && play.getY() - playTake.getY() <= 141 ){
				//右上方是否有棋子
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() &&  playQ[i].getX() - play.getX() == 57 && play.getY() - playQ[i].getY() == 57 ){
						Move++;
						break;
					}
				}//for	
				
				Chess=true;
			}//else
			
			//吃下左方的棋子
			else if (play.getX() - playTake.getX() >= 87 && play.getX() - playTake.getX() <= 141 && playTake.getY() - play.getY() >= 87 && playTake.getY() - play.getY() <= 141){
				//下左方是否有棋子
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getX() - playQ[i].getX() == 57 && play.getY() - playQ[i].getY() == -57 ){
						Move++;
						break;
					}
				}//for
				
				Chess=true;
			}//else
			
			//吃下右放的棋子
			else if (playTake.getX() - play.getX() >= 87 && playTake.getX() - play.getX() <= 141 && playTake.getY() - play.getY() >= 87 && playTake.getY() - play.getY() <= 141){
				//下右方是否有棋子
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && playQ[i].getX() - play.getX() == 57 && playQ[i].getY() - play.getY() == 57 ){
						Move = 1;
						break;
					}
				}//for		

				Chess=true;
				
			}//else
			
			//没有障碍、并不能吃自己的棋子
			if (Chess && Move == 0 && playTake.getName().charAt(1) != play.getName().charAt(1)){
				//当前记录添加到集合(用于悔棋)
				Var.add(String.valueOf(play.isVisible()));
				Var.add(String.valueOf(play.getX()));
				Var.add(String.valueOf(play.getY()));
				Var.add(String.valueOf(Man));
				
				//当前记录添加到集合(用于悔棋)
				Var.add(String.valueOf(playTake.isVisible()));
				Var.add(String.valueOf(playTake.getX()));
				Var.add(String.valueOf(playTake.getY()));
				Var.add(String.valueOf(i));
				
				playTake.setVisible(false);
				play.setBounds(playTake.getX(),playTake.getY(),55,55);
			}
			
		}//相、象吃棋规则结束
		
		/**士、仕移动方法*/
		public void chapRule(int Man,JLabel play,JLabel playQ[],MouseEvent me){
			//上、右
			if (me.getX() - play.getX() >= 29 && me.getX() - play.getX() <= 114 && play.getY() - me.getY() >= 25 && play.getY() - me.getY() <= 90){
				//士不能超过自己的界限
				if (Man < 14 && (play.getX()+57) >= 195 && (play.getX()+57) <= 309 && (play.getY()-57) <= 170){
					//当前记录添加到集合(用于悔棋)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(play.getX()+57,play.getY()-57,55,55);
				}	
				
				//仕不能超过自己的界限
				else if (Man > 13 && (play.getY()-57) >= 455 && (play.getX()+57)  >= 195 && (play.getX()+57) <= 309){
					//当前记录添加到集合(用于悔棋)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(play.getX()+57,play.getY()-57,55,55);
				}	
			}// else if 
			
			//上、左
			else if (play.getX() - me.getX() <= 114 && play.getX() - me.getX() >= 25 && play.getY() - me.getY() >= 20 && play.getY() - me.getY() <= 95){
				//士不能超过自己的界限
				if (Man < 14 &&  (play.getX()-57) >= 195 && (play.getX()-57) <= 309 && (play.getY()-57) <= 170  ){
					//当前记录添加到集合(用于悔棋)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(play.getX()-57,play.getY()-57,55,55);
				}	
				
				//仕不能超过自己的界限
				else if (Man > 13 &&(play.getY()-57) >= 455 && (play.getX()-57)  >= 195 && (play.getX()-57) <= 309){
					//当前记录添加到集合(用于悔棋)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(play.getX()-57,play.getY()-57,55,55);
				}	
			}// else if 
			
			//下、左
			else if (play.getX() - me.getX() <= 114 && play.getX() - me.getX() >= 20 && me.getY() - play.getY() >= 2 && me.getY() - play.getY() <= 87){
				//士不能超过自己的界限
				if (Man < 14 && (play.getX()-57) >= 195 && (play.getX()-57) <= 309 && (play.getY()+57) <= 170 ){
					//当前记录添加到集合(用于悔棋)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(play.getX()-57,play.getY()+57,55,55);
				}	
				
				//仕不能超过自己的界限
				else if (Man > 13 && (play.getY()+57) >= 455 && (play.getX()-57)  >= 195 && (play.getX()-57) <= 309){
					//当前记录添加到集合(用于悔棋)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(play.getX()-57,play.getY()+57,55,55);
				}
				
			}// else if 
			
			
			//下、右
			else if (me.getX() - play.getX() >= 27 && me.getX() - play.getX() <= 114 && me.getY() - play.getY() >= 2 && me.getY() - play.getY() <= 87){
				//士不能超过自己的界限
				if (Man < 14 && (play.getX()+57) >= 195 && (play.getX()+57) <= 309 && (play.getY()+57) <= 170){
					//当前记录添加到集合(用于悔棋)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(play.getX()+57,play.getY()+57,55,55);
				}
				
				//仕不能超过自己的界限
				else if (Man > 13 &&(play.getY()+57) >= 455 && (play.getX()+57)  >= 195 && (play.getX()+57) <= 309){
					//当前记录添加到集合(用于悔棋)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(play.getX()+57,play.getY()+57,55,55);
				}
			}//else if 
			
		}//士、仕移动规则结束


		/**士、仕吃棋规则*/
		public void chapRule(int Man ,JLabel play,JLabel playTake,JLabel playQ[]){
			//当前状态
			boolean Chap = false;	
			
			//上、右
			if (playTake.getX() - play.getX() >= 20 && playTake.getX() - play.getX() <= 114 && play.getY() - playTake.getY() >= 2 && play.getY() - playTake.getY() <= 87){
				//被吃的棋子是否和当前士相近
				if (Man < 14 && playTake.getX() >= 195 && playTake.getX() <= 309 && playTake.getY() <= 170 && playTake.isVisible()){
					Chap = true;
				}
				
				//被吃的棋子是否和当前仕相近
				else if (Man > 13 && playTake.getX() >= 195 && playTake.getX() <= 309 && playTake.getY() >= 455 && playTake.isVisible()){
					Chap = true;
				}
			}//if
			
			//上、左
			else if (play.getX() - playTake.getX() <= 114 && play.getX() - playTake.getX() >= 25 && play.getY() - playTake.getY() >= 2 && play.getY() - playTake.getY() <= 87){
				//被吃的棋子是否和当前士相近
				if (Man < 14 && playTake.getX() >= 195 && playTake.getX() <= 309 && playTake.getY() <= 170 && playTake.isVisible()){
					Chap = true;
				}
				
				//被吃的棋子是否和当前仕相近
				else if (Man > 13 && playTake.getX() >= 195 && playTake.getX() <= 309 && playTake.getY() >= 455 && playTake.isVisible()){
					Chap = true;
				}
			}// else if 
			
			//下、左
			else if (play.getX() - playTake.getX() <= 114 && play.getX() - playTake.getX() >= 25 && playTake.getY() - play.getY() >= 2 && playTake.getY() - play.getY() <= 87){
				//被吃的棋子是否和当前士相近
				if (Man < 14 && playTake.getX() >= 195 && playTake.getX() <= 309 && playTake.getY() <= 170 && playTake.isVisible()){
					Chap = true;
				}
				
				//被吃的棋子是否和当前仕相近
				else if (Man > 13 && playTake.getX() >= 195 && playTake.getX() <= 309 && playTake.getY() >= 455 && playTake.isVisible()){
					Chap = true;
				}
			}// else if 
			
			//下、右
			else if (playTake.getX() - play.getX() >= 25 && playTake.getX() - play.getX() <= 114 && playTake.getY() - play.getY() >= 2 && playTake.getY() - play.getY() <= 87){
				//被吃的棋子是否和当前士相近
				if (Man < 14 && playTake.getX() >= 195 && playTake.getX() <= 309 && playTake.getY() <= 170 && playTake.isVisible()){
					Chap = true;
				}
				
				//被吃的棋子是否和当前仕相近
				else if (Man > 13 && playTake.getX() >= 195 && playTake.getX() <= 309 && playTake.getY() >= 455 && playTake.isVisible()){
					Chap = true;
				}
			}//else if 
			
			//可移动、并不能吃自己的棋子
			if (Chap && playTake.getName().charAt(1) != play.getName().charAt(1)){
				//当前记录添加到集合(用于悔棋)
				Var.add(String.valueOf(play.isVisible()));
				Var.add(String.valueOf(play.getX()));
				Var.add(String.valueOf(play.getY()));
				Var.add(String.valueOf(Man));
				
				//当前记录添加到集合(用于悔棋)
				Var.add(String.valueOf(playTake.isVisible()));
				Var.add(String.valueOf(playTake.getX()));
				Var.add(String.valueOf(playTake.getY()));
				Var.add(String.valueOf(i));
				
				playTake.setVisible(false);
				play.setBounds(playTake.getX(),playTake.getY(),55,55);
			}
			
		}//士、仕吃棋规则结束
		
		/**将移动规则*/
		public void willRule(int Man,JLabel play,JLabel playQ[],MouseEvent me){
			//向上
			if ((me.getX()-play.getX()) >= 0 && (me.getX()-play.getX()) <= 55 && (play.getY()-me.getY()) >=2 && play.getY()-me.getY() <= 87){
				//将是否超过自己的界限
				if (Man == 30 && me.getX() >= 195 && me.getX() <= 359 && me.getY() <= 170){
					//当前记录添加到集合(用于悔棋)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(play.getX(),play.getY()-57,55,55);	
				}	
				
				//帅是否超过自己的界限
				else if (Man == 31 && me.getY() >= 455 && me.getX() >= 195 && me.getX() <= 359){
					//当前记录添加到集合(用于悔棋)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(play.getX(),play.getY()-57,55,55);
				}
			}//if
			
			//向左
			else if (play.getX() - me.getX() >= 2 && play.getX() - me.getX() <= 57 && me.getY() - play.getY() <= 27 && me.getY() - play.getY() >= -27){
				//将是否超过自己的界限
				if (Man == 30 && me.getX() >= 195 && me.getX() <= 359 && me.getY() <= 170){
					//当前记录添加到集合(用于悔棋)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(play.getX()-57,play.getY(),55,55);
				}
				
				//帅是否超过自己的界限
				else if (Man == 31 && me.getY() >= 455 && me.getX() >= 195 && me.getX() <= 359){
					//当前记录添加到集合(用于悔棋)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(play.getX()-57,play.getY(),55,55);
				}
			}//else if 
			
			//向右
			else if (me.getX() - play.getX() >= 57 && me.getX() - play.getX() <= 112 && me.getY() - play.getY() <= 27 && me.getY() - play.getY() >= -27){
				//将、帅规则
				if (Man == 30 && me.getX() >= 195 && me.getX() <= 359 && me.getY() <= 170){
					//当前记录添加到集合(用于悔棋)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(play.getX()+57,play.getY(),55,55);	
				}	
				
				else if (Man == 31 && me.getY() >= 455 && me.getX() >= 195 && me.getX() <= 359){
					//当前记录添加到集合(用于悔棋)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));	
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(play.getX()+57,play.getY(),55,55);
				}
			}//else if 
			
			//向下
			else if (me.getX() - play.getX() >= 0 && me.getX() - play.getX() <= 55 && me.getY() - play.getY() <= 87 && me.getY() - play.getY() >= 27){
				//将、帅规则
				if (Man == 30 && me.getX() >= 195 && me.getX() <= 359 && me.getY() <= 170){
					//当前记录添加到集合(用于悔棋)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(play.getX(),play.getY()+57,55,55);
				}
				
				else if (Man == 31 && me.getY() >= 455 && me.getX() >= 195 && me.getX() <= 359){
					//当前记录添加到集合(用于悔棋)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
				
					play.setBounds(play.getX(),play.getY()+57,55,55);
				}

			}//else if
			
		}//将、帅移动规则结束

		public void willRule(int Man ,JLabel play,JLabel playTake ,JLabel playQ[]){
			//当前状态
			boolean will = false;
			
			//向上吃
			if (play.getX() - playTake.getX() >= 0 && play.getX() - playTake.getX() <= 55 && play.getY() - playTake.getY() >= 27 && play.getY() - playTake.getY() <= 87 && playTake.isVisible()){
				//被吃的棋子是否和当前将相近
				if (Man == 30 && playTake.getX() >= 195 && playTake.getX() <= 309 && playTake.getY() <= 170){
					will = true;
				}
				
				//被吃的棋子是否和当前帅相近
				else if (Man == 31 && playTake.getY() >= 455 && playTake.getX() >= 195 && playTake.getX() <= 309){
					will = true; 
				}
			}
			
			//向左吃
			else if (play.getX() - playTake.getX() >= 2 && play.getX() - playTake.getX() <= 57 && playTake.getY() - play.getY() <= 27 && playTake.getY() - play.getY() >= -27 && playTake.isVisible()){
				//被吃的棋子是否和当前将相近
				if (Man == 30 && playTake.getX() >= 195 && playTake.getX() <= 309 && playTake.getY() <= 170){
					will = true;
				}
				
				//被吃的棋子是否和当前帅相近
				else if (Man == 31 && playTake.getY() >= 455 && playTake.getX() >= 195 && playTake.getX() <= 309){
					will = true; 
				}
			}
			
			//向右吃
			else if (playTake.getX() - play.getX() >= 2 && playTake.getX() - play.getX() <= 57 && playTake.getY() - play.getY() <= 27 && playTake.getY() - play.getY() >= -27 && playTake.isVisible()){
				//被吃的棋子是否和当前将相近
				if (Man == 30 && playTake.getX() >= 195 && playTake.getX() <= 309 && playTake.getY() <= 170){
					will = true;
				}
				
				//被吃的棋子是否和当前帅相近
				else if (Man == 31 && playTake.getY() >= 455 && playTake.getX() >= 195 && playTake.getX() <= 309){
					will = true; 
				}
			}
			
			//向下
			else if (playTake.getX() - play.getX() >= 0 && playTake.getX() - play.getX() <= 87 && playTake.getY() - play.getY() <= 27 && playTake.getY() - play.getY() >= 40 && playTake.isVisible()){
				//被吃的棋子是否和当前将相近
				if (Man == 30 && playTake.getX() >= 195 && playTake.getX() <= 309 && playTake.getY() <= 170){
					will = true;
				}
				
				//被吃的棋子是否和当前帅相近
				else if (Man == 31 && playTake.getY() >= 455 && playTake.getX() >= 195 && playTake.getX() <= 309){
					will = true; 
				}
			}
				
			//不能吃自己的棋子、符合当前要求	
			if (playTake.getName().charAt(1) != play.getName().charAt(1) && will){
				//当前记录添加到集合(用于悔棋)
				Var.add(String.valueOf(play.isVisible()));
				Var.add(String.valueOf(play.getX()));
				Var.add(String.valueOf(play.getY()));
				Var.add(String.valueOf(Man));
				
				//当前记录添加到集合(用于悔棋)
				Var.add(String.valueOf(playTake.isVisible()));
				Var.add(String.valueOf(playTake.getX()));
				Var.add(String.valueOf(playTake.getY()));
				Var.add(String.valueOf(i));

				playTake.setVisible(false);
				play.setBounds(playTake.getX(),playTake.getY(),55,55);
			}			
			
		}//将、帅吃规则结束
		
	}//规则类



    private void resetGame() {
        // 重置棋局逻辑
        // 例如，重新初始化棋盘，重置棋子位置等
        rule = new ChessRule(); // 重新实例化规则类
        Var.clear(); // 清空操作记录
        chessPlayClick = 2; // 红方先行
        text.setText("欢迎使用象棋对弈系统");

        // 重新绘制界面
        repaint();
    }

    private void log(int x, int y){
        JLabel jLabel = play[Man];
        int moveX = (jLabel.getX() - x) /55;
        int moveY = (jLabel.getY() - y) /55;
        String moveXAction = moveX > 0 ? "左移" : "右移";
        String moveYAction = moveY > 0 ? "上移" : "下移";
        if(moveX != 0 || moveY != 0) {
            logStr = String.format("第%s步：%s %s %s 格， %s %s 格 \n",
                    ++logCount, jLabel.getName(), moveYAction, Math.abs(moveY), moveXAction, Math.abs(moveX)) + logStr;
        }

    }
	
}//主框架类
