/*
 *�й�����Java��V3.0
 *��ӹ���:ʵ���˵�ǰ��ֵı���
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

//����
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

        ChessMainFrame frame = new ChessMainFrame("java�й��������" + title.get(args[0]) , playType, server);

	}
}

//�������
class ChessMainFrame extends JFrame implements ActionListener,MouseListener,Runnable{
	//���
	JLabel play[] = new JLabel[32];
	//����
	JLabel image;	
	//����
	Container con;
	//������
	JToolBar jmain;	
	//���¿�ʼ
	JButton anew;
	//����
	JButton repent;
    //����
    JButton fail;
    //����
    JButton peace;
    //��¼
    JButton log;
    //����
    JButton help;
	//�˳�
	JButton exit;
	//��ǰ��Ϣ
	JLabel text;
	
	//���浱ǰ����
	Vector Var;
	
	//���������(ʹ�ڵ��÷���)
	ChessRule rule;
	
	/**
	** ��������
	** chessManClick = true ��˸���� �����߳���Ӧ
	** chessManClick = false ������ ֹͣ��˸  �����߳���Ӧ
	*/
	boolean chessManClick;
	
	/**
	** �����������
	** chessPlayClick=1 ��������
	** chessPlayClick=2 �������� Ĭ�Ϻ���
	** chessPlayClick=3 ˫������������
	*/
	public int chessPlayClick=2;
	
	//����������˸���߳�
	Thread tmain;
	//�ѵ�һ�εĵ������Ӹ��߳���Ӧ
	static int Man,i;

    private ChessClient client;
    private ChessServer server;

    /**
     * ����ģʽ 1 ����� 2 �ͻ��� 3 ������ս
     */
    private int playType = 3;

    /**
     * ��ǰ�����ɫ
     */
    int currentChessPlayClick;


    Timer moveTimer;
    Timer gameTimer;
    long moveTimeLimit;  // ÿһ�����ʱ�����ƣ����룩
    long gameTimeLimit;

    private String logStr = "";

    public static final String helpStr = "���壬�ֳ��й����壬��һ�����˶��ĵ�������Ϸ�������й�������Ȧ�����ƾõ���ʷ�������Ļ����塣����������Ļ�������\n" +
            "\n" +
            "���̺Ͳ��֣�����������һ����9�к�10����ɵĸ������磬���ӷ��ڽ�����ϡ����̱�һ��û�и��ߵĺӽ�ֳ����룬�ֱ�������˫����\n" +
            "\n" +
            "���ӣ�ÿ����1������˧����2��ʿ���ˣ���2�����ࣩ��2����2������2���ں�5�������䣩��\n" +
            "\n" +
            "��/˧��ֻ�����Լ��ľŹ����ƶ���ÿ��ֻ����һ������ֻ��ֱ�߻���ߡ�\n" +
            "ʿ/�ˣ�Ҳֻ���ھŹ����ƶ���ÿ����б��һ��\n" +
            "��/�ࣺÿ���ߡ���֣�������֮��ľ������������б�ߣ�����Խ���ӽ硣\n" +
            "���ߡ��ա����Σ�����ֱ��һ����б��һ�����ֱ�ߵĵ�һ�������Ӷ�ס����Ϊ�����ȣ������������������\n" +
            "����ֱ��ǰ�������Ժ���������ƶ������벻�ޣ�������Խ���������ӡ�\n" +
            "�ڣ��ƶ�ʱ�복���ƣ����ڳ���ʱ����������һ�����ӡ�\n" +
            "��/�䣺ֻ��ֱ�ߣ����Ӻ���������ƶ���\n" +
            "����Ŀ�꣺�����Է��Ľ�/˧������Է��Ľ�/˧�޷����ѣ����Ϊ��������������ʤ����\n" +
            "\n" +
            "�������\n" +
            "\n" +
            "�������ظ�������ͬ�Ľ�����������Ϊ��Υ��ġ�\n" +
            "���壺��ĳЩ�ظ������£�˫�����ܻ��ɺ��塣\n" +
            "���֣�һЩ�ض����ظ��������ܻᱻ��Ϊ���֣�����������������͵������졣\n" +
            "����Ĳ��Ժͼ�����·ḻ���������֡��оֺͲоֵĲ�ͬս�������ڳ�ѧ����˵���˽���Щ�������������ŵĵ�һ����������ͨ��ʵս��������ա�";

    private int logCount;

    /**
	** ���캯��
	** ��ʼ��ͼ���û�����
	*/
	ChessMainFrame(String Title, int playType, String serverStr){
        this.playType = playType;
		//�ı�ϵͳĬ������
		Font font = new Font("Dialog", Font.PLAIN, 12);
		java.util.Enumeration keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof javax.swing.plaf.FontUIResource) {
				UIManager.put(key, font);
			}
		}
		//���п͸�����
		con = this.getContentPane();
		con.setLayout(null);
		//ʵ����������
		rule = new ChessRule();
		Var = new Vector();
		
		//����������
		jmain = new JToolBar();
		text = new JLabel("��ӭʹ���������ϵͳ");
		//����������ʾ��Ϣ
		text.setToolTipText("��Ϣ��ʾ");
		anew = new JButton(" �� �� Ϸ ");
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


                // �Ƴ���������
                for (JLabel chessPiece : play) {
                    con.remove(chessPiece);
                }
                con.remove(image);
                con.repaint();

                // ������������Ӵ�������µ�λ�ã��������ݲ�����λ�ü���
                drawChessMan();

                //ע�������ƶ�����
                con.add(image);
                con.revalidate(); // ֪ͨ���ֹ��������²��� Container
                con.repaint(); // �ػ� Container ����ʾ���º�����

                Message move = new Message();
                move.MsgTYpe = 4;
                move.moveTimeLimit = moveTimeLimit;
                move.gameTimeLimit = gameTimeLimit;
                //�����ս
                if(server != null){
                    server.sendMove(move);
                }else if(client != null) {
                    client.sendMove(move);
                }



            }
        });

		anew.setToolTipText("���¿�ʼ�µ�һ��");
		exit = new JButton(" ��  �� ");
		exit.setToolTipText("�˳�����������");
		repent = new JButton(" ��  �� ");
		repent.setToolTipText("���ص��ϴ������λ��");
        fail = new JButton(" �� �� ");
        fail.setToolTipText("�������Ϸ���¿�ʼ");
        peace = new JButton(" �� �� ");
        peace.setToolTipText("�������Ϸ���¿�ʼ");
        log = new JButton(" �� ¼ ");
        log.setToolTipText("�鿴��ּ�¼");
        help = new JButton(" �� �� ");
        help.setToolTipText("������Ϸ����");

        log.addActionListener(e -> {
            JOptionPane.showConfirmDialog(
                    ChessMainFrame.this,logStr,"��¼",
                    JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE);
        });

        help.addActionListener(e -> {
            JOptionPane.showConfirmDialog(
                    ChessMainFrame.this,helpStr,"����",
                    JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE);
        });

		//�������ӵ�������
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
		
		//������ӱ�ǩ
		drawChessMan();

		//ע�ᰴŤ����
		anew.addActionListener(this);
		repent.addActionListener(this);
		exit.addActionListener(this);
        fail.addActionListener(this);
        peace.addActionListener(this);


        //������̱�ǩ
        con.add(image = new JLabel(new ImageIcon("image" + File.separator + "Main.GIF")));
        image.setBounds(0,30,558,620);
        image.addMouseListener(this);
		
		//ע�ᴰ��رռ���
		this.addWindowListener(
			new WindowAdapter() {
				public void windowClosing(WindowEvent we){
					System.exit(0);
				}
			}
		);
		
		//�������
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = this.getSize();
		
		if (frameSize.height > screenSize.height){
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width){
			frameSize.width = screenSize.width;
		}
		
		this.setLocation((screenSize.width - frameSize.width) / 2 - 280 ,(screenSize.height - frameSize.height ) / 2 - 350);
	
		//����
		this.setIconImage(new ImageIcon("image" + File.separator + "�콫.GIF").getImage());
		this.setResizable(false);
		this.setTitle(Title);
		this.setSize(558,670);
		this.show();

        startOnline(playType, serverStr);
	}

    /**
     ** ������ӷ���
     */
    public void drawChessMan(){
        //���̿���
        int i,k;
        //ͼ��
        Icon in;

        //��ɫ����

        //��
        in = new ImageIcon("image" + File.separator + "�ڳ�.GIF");
        for (i=0,k=24;i<2;i++,k+=456){
            play[i] = new JLabel(in);
            play[i].setBounds(k,56,55,55);
            play[i].setName("��1");
        }

        //��
        in = new ImageIcon("image" + File.separator + "����.GIF");
        for (i=4,k=81;i<6;i++,k+=342){
            play[i] = new JLabel(in);
            play[i].setBounds(k,56,55,55);
            play[i].setName("��1");
        }

        //��
        in = new ImageIcon("image" + File.separator + "����.GIF");
        for (i=8,k=138;i<10;i++,k+=228){
            play[i] = new JLabel(in);
            play[i].setBounds(k,56,55,55);
            play[i].setName("��1");
        }

        //ʿ
        in = new ImageIcon("image" + File.separator + "��ʿ.GIF");
        for (i=12,k=195;i<14;i++,k+=114){
            play[i] = new JLabel(in);
            play[i].setBounds(k,56,55,55);
            play[i].setName("ʿ1");
        }

        //��
        in = new ImageIcon("image" + File.separator + "����.GIF");
        for (i=16,k=24;i<21;i++,k+=114){
            play[i] = new JLabel(in);
            play[i].setBounds(k,227,55,55);
            play[i].setName("��1" + i);
        }

        //��
        in = new ImageIcon("image" + File.separator + "����.GIF");
        for (i=26,k=81;i<28;i++,k+=342){
            play[i] = new JLabel(in);
            play[i].setBounds(k,170,55,55);
            play[i].setName("��1" + i);
        }

        //��
        in = new ImageIcon("image" + File.separator + "�ڽ�.GIF");
        play[30] = new JLabel(in);
        play[30].setBounds(252,56,55,55);
        play[30].setName("��1");

        //��ɫ����
        //��
        in = new ImageIcon("image" + File.separator + "�쳵.GIF");
        for (i=2,k=24;i<4;i++,k+=456){
            play[i] = new JLabel(in);
            play[i].setBounds(k,569,55,55);
            play[i].setName("��2");
        }

        //��
        in = new ImageIcon("image" + File.separator + "����.GIF");
        for (i=6,k=81;i<8;i++,k+=342){
            play[i] = new JLabel(in);
            play[i].setBounds(k,569,55,55);
            play[i].setName("��2");
        }

        //��
        in = new ImageIcon("image" + File.separator + "����.GIF");
        for (i=10,k=138;i<12;i++,k+=228){
            play[i] = new JLabel(in);
            play[i].setBounds(k,569,55,55);
            play[i].setName("��2");
        }

        //ʿ
        in = new ImageIcon("image" + File.separator + "��ʿ.GIF");
        for (i=14,k=195;i<16;i++,k+=114){
            play[i] = new JLabel(in);
            play[i].setBounds(k,569,55,55);
            play[i].setName("ʿ2");
        }

        //��
        in = new ImageIcon("image" + File.separator + "����.GIF");
        for (i=21,k=24;i<26;i++,k+=114){
            play[i] = new JLabel(in);
            play[i].setBounds(k,398,55,55);
            play[i].setName("��2" + i);
        }

        //��
        in = new ImageIcon("image" + File.separator + "����.GIF");
        for (i=28,k=81;i<30;i++,k+=342){
            play[i] = new JLabel(in);
            play[i].setBounds(k,455,55,55);
            play[i].setName("��2" + i);
        }

        //˧
        in = new ImageIcon("image" + File.separator + "�콫.GIF");
        play[31] = new JLabel(in);
        play[31].setBounds(252,569,55,55);
        play[31].setName("˧2");

        //ע�������ƶ�����
        for (int j=0;j<32;j++){
            con.add(play[j]);
            play[j].addMouseListener(this);
        }

    }

    // ����������Ϸ�ļ�ʱ��
    public void startGameTimer() {
        gameTimer = new Timer();
        gameTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Game over: Time limit exceeded.");
                gameTimer.cancel();
                JOptionPane.showConfirmDialog(
                        ChessMainFrame.this,"��Ϸʱ�䵽","����",
                        JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE);
                chessPlayClick=3;
                text.setText("����");
            }
        }, gameTimeLimit);
    }


    // ������ǰ��ҵļ�ʱ��
    public void startMoveTimer() {
        moveTimer = new Timer();
        moveTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Game over: Time limit exceeded.");
                moveTimer.cancel();
                if(chessPlayClick == 1){
                    JOptionPane.showConfirmDialog(
                            ChessMainFrame.this,"����ʤ��","����ʤ��",
                            JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE);
                    text.setText("����ʤ��");

                }else if( chessPlayClick == 2){
                    JOptionPane.showConfirmDialog(
                            ChessMainFrame.this,"����ʤ��","����ʤ��",
                            JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE);
                    text.setText("����ʤ��");
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
            //�˻�
        }
    }

	
	/**
	** �̷߳�������������˸
	*/
	public void run(){
		while (true){
			//�������ӵ�һ�¿�ʼ��˸
			if (chessManClick){
				play[Man].setVisible(false);

				//ʱ�����
				try{
					tmain.sleep(200);
				}
				catch(Exception e){
				}
				
				play[Man].setVisible(true);
			}
			
			//��˸��ǰ��ʾ��Ϣ �����û�������
			else {
				text.setVisible(false);
				
				//ʱ�����
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
	** �������ӷ���
	*/
	public void mouseClicked(MouseEvent me){

        if(playType != 3) {

            if (currentChessPlayClick == 0) {
                currentChessPlayClick = chessPlayClick;
            }

            //���߶�սʱ���жϵ�ǰ�����Ƿ����
            if (!currentPlayer()) {
                return;
            }
        }

        Message move = move(me, 0, 0);
        move.MsgTYpe = 1;
        //�����ս
        if(server != null){
            server.sendMove(move);
        }else if(client != null) {
            client.sendMove(move);
        }

	}

    /**
     * �ƶ�����
     * @param me
     */
    public Message move(MouseEvent me, int moveType, int movePiece) {
        System.out.println("Mouse");

        //��ǰ����
        int Ex=0,Ey=0;

        //�����߳�
        if (tmain == null){
            tmain = new Thread(this);
            tmain.start();
        }

        //��������(�ƶ�����)
        if (image.equals(me.getSource()) || moveType == 1){
            moveType = 1;
            //�ú��������ʱ��
            if (chessPlayClick == 2 && play[Man].getName().charAt(1) == '2'){
                Ex = play[Man].getX();
                Ey = play[Man].getY();
                //�ƶ��䡢��
                if (Man > 15 && Man < 26){
                    rule.armsRule(Man,play[Man], me);
                }

                //�ƶ���
                else if (Man > 25 && Man < 30){
                    rule.cannonRule(play[Man],play, me);
                }

                //�ƶ���
                else if (Man >=0 && Man < 4){
                    rule.cannonRule(play[Man],play, me);
                }

                //�ƶ���
                else if (Man > 3 && Man < 8){
                    rule.horseRule(play[Man],play, me);
                }

                //�ƶ��ࡢ��
                else if (Man > 7 && Man < 12){
                    rule.elephantRule(Man,play[Man],play, me);
                }

                //�ƶ��ˡ�ʿ
                else if (Man > 11 && Man < 16){
                    rule.chapRule(Man,play[Man],play, me);
                }

                //�ƶ�����˧
                else if (Man == 30 || Man == 31){
                    rule.willRule(Man,play[Man],play, me);
                }

                //�Ƿ��������(�Ƿ���ԭ��û�ж�)
                if (Ex == play[Man].getX() && Ey == play[Man].getY()){
                    text.setText("               ��������");
                    chessPlayClick=2;
                }

                else {
                    text.setText("               ��������");
                    chessPlayClick=1;
                }



            }//if

            //�ú��������ʱ��
            else if (chessPlayClick == 1 && play[Man].getName().charAt(1) == '1'){
                Ex = play[Man].getX();
                Ey = play[Man].getY();

                //�ƶ��䡢��
                if (Man > 15 && Man < 26){
                    rule.armsRule(Man,play[Man], me);
                }

                //�ƶ���
                else if (Man > 25 && Man < 30){
                    rule.cannonRule(play[Man],play, me);
                }

                //�ƶ���
                else if (Man >=0 && Man < 4){
                    rule.cannonRule(play[Man],play, me);
                }

                //�ƶ���
                else if (Man > 3 && Man < 8){
                    rule.horseRule(play[Man],play, me);
                }

                //�ƶ��ࡢ��
                else if (Man > 7 && Man < 12){
                    rule.elephantRule(Man,play[Man],play, me);
                }

                //�ƶ��ˡ�ʿ
                else if (Man > 11 && Man < 16){
                    rule.chapRule(Man,play[Man],play, me);
                }

                //�ƶ�����˧
                else if (Man == 30 || Man == 31){
                    rule.willRule(Man,play[Man],play, me);
                }

                //�Ƿ��������(�Ƿ���ԭ��û�ж�)
                if (Ex == play[Man].getX() && Ey == play[Man].getY()){
                    text.setText("               ��������");
                    chessPlayClick=1;
                }

                else {
                    text.setText("               ��������");
                    chessPlayClick=2;
                }


            }//else if

            //��ǰû�в���(ֹͣ��˸)
            chessManClick=false;


            log(Ex, Ey);

        }//if

        //��������
        else{
            //��һ�ε�������(��˸����)
            if (!chessManClick){
                if(moveType == 2){
                    Man = movePiece;
                    chessManClick=true;
                }else {
                    for (int i=0;i<32;i++){
                        //������������
                        if (me.getSource().equals(play[i])){
                            //�����߳��ø�������˸
                            Man=i;
                            //��ʼ��˸
                            chessManClick=true;
                            moveType = 2;
                            movePiece = i;
                            break;
                        }
                    }//for
                }
            }//if

            //�ڶ��ε�������(������)
            else if (chessManClick){
                //��ǰû�в���(ֹͣ��˸)
                chessManClick=false;

                for (i=0;i<32;i++){
                    //�ҵ����Ե�����
                    if (play[i].equals(me.getSource()) || (i == movePiece && moveType == 3) ){
                        movePiece = i;
                        moveType = 3;
                        //�ú�������ʱ��
                        if (chessPlayClick == 2 && play[Man].getName().charAt(1) == '2'){
                            Ex = play[Man].getX();
                            Ey = play[Man].getY();

                            //�䡢���Թ���
                            if (Man > 15 && Man < 26){
                                rule.armsRule(play[Man],play[i]);
                            }

                            //�ڳԹ���
                            else if (Man > 25 && Man < 30){
                                rule.cannonRule(0,play[Man],play[i],play, me);
                            }

                            //���Թ���
                            else if (Man >=0 && Man < 4){
                                rule.cannonRule(1,play[Man],play[i],play, me);
                            }

                            //��Թ���
                            else if (Man > 3 && Man < 8){
                                rule.horseRule(play[Man],play[i],play, me);
                            }

                            //�ࡢ��Թ���
                            else if (Man > 7 && Man < 12){
                                rule.elephantRule(play[Man],play[i],play);
                            }

                            //ʿ���˳������
                            else if (Man > 11 && Man < 16){
                                rule.chapRule(Man,play[Man],play[i],play);
                            }

                            //����˧�������
                            else if (Man == 30 || Man == 31){
                                rule.willRule(Man,play[Man],play[i],play);
                                play[Man].setVisible(true);
                            }

                            //�Ƿ��������(�Ƿ���ԭ��û�ж�)
                            if (Ex == play[Man].getX() && Ey == play[Man].getY()){
                                text.setText("               ��������");
                                chessPlayClick=2;
                                break;
                            }

                            else{
                                text.setText("               ��������");
                                chessPlayClick=1;
                                break;
                            }

                        }//if

                        //�ú�������ʱ��
                        else if (chessPlayClick == 1 && play[Man].getName().charAt(1) == '1'){
                            Ex = play[Man].getX();
                            Ey = play[Man].getY();

                            //��Թ���
                            if (Man > 15 && Man < 26){
                                rule.armsRule(play[Man],play[i]);
                            }

                            //�ڳԹ���
                            else if (Man > 25 && Man < 30){
                                rule.cannonRule(0,play[Man],play[i],play, me);
                            }

                            //���Թ���
                            else if (Man >=0 && Man < 4){
                                rule.cannonRule(1,play[Man],play[i],play, me);
                            }

                            //��Թ���
                            else if (Man > 3 && Man < 8){
                                rule.horseRule(play[Man],play[i],play, me);
                            }

                            //�ࡢ��Թ���
                            else if (Man > 7 && Man < 12){
                                rule.elephantRule(play[Man],play[i],play);
                            }

                            //ʿ���˳������
                            else if (Man > 11 && Man < 16){
                                rule.chapRule(Man,play[Man],play[i],play);
                            }

                            //����˧�������
                            else if (Man == 30 || Man == 31){
                                rule.willRule(Man,play[Man],play[i],play);
                                play[Man].setVisible(true);
                            }

                            //�Ƿ��������(�Ƿ���ԭ��û�ж�)
                            if (Ex == play[Man].getX() && Ey == play[Man].getY()){
                                text.setText("               ��������");
                                chessPlayClick=1;
                                break;
                            }

                            else {
                                text.setText("               ��������");
                                chessPlayClick=2;
                                break;
                            }

                        }//else if

                    }//if

                }//for


                //�Ƿ�ʤ��
                if (!play[31].isVisible()){
                    JOptionPane.showConfirmDialog(
                        this,"����ʤ��","���һʤ��",
                        JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE);
                    //˫������������������
                    chessPlayClick=3;
                    text.setText("  ����ʤ��");

                }//if

                else if (!play[30].isVisible()){
                    JOptionPane.showConfirmDialog(
                        this,"����ʤ��","��Ҷ�ʤ��",
                        JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE);
                    chessPlayClick=3;
                    text.setText("  ����ʤ��");
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
	** ���尴ť���¼���Ӧ
	*/
	public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == anew) {
            resetGame();
        } else if (source == fail) {
            int result = JOptionPane.showConfirmDialog(this, "��ȷ��Ҫ������", "����ȷ��", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                if(chessPlayClick == 1){
                    JOptionPane.showConfirmDialog(
                            ChessMainFrame.this,"����ʤ��","����ʤ��",
                            JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE);
                    text.setText("����ʤ��");

                }else if( chessPlayClick == 2){
                    JOptionPane.showConfirmDialog(
                            ChessMainFrame.this,"����ʤ��","����ʤ��",
                            JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE);
                    text.setText("����ʤ��");
                }

                Message msg = new Message();
                msg.MsgTYpe = 2;
                //�����ս
                if(server != null){
                    server.sendMove(msg);
                }else if(client != null) {
                    client.sendMove(msg);
                }


            }
        } else if (source == peace) {
            int result = JOptionPane.showConfirmDialog(this, "��ȷ��Ҫ������", "����ȷ��", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(this, "˫�����壡");
                resetGame();
            }
            Message msg = new Message();
            msg.MsgTYpe = 3;
            //�����ս
            if(server != null){
                server.sendMove(msg);
            }else if(client != null) {
                client.sendMove(msg);
            }
        }  else if (source == exit) {
            System.exit(0);
        }
	}

	/*�����й�����������*/
	class ChessRule implements Serializable {
		/**���ӵ��ƶ�����*/
		public void armsRule(int Man,JLabel play,MouseEvent me){
			//��������
			if (Man < 21){
				//�����ƶ����õ��յ������ģ���ɺϷ�������
				if ((me.getY()-play.getY()) > 27 && (me.getY()-play.getY()) < 86 && (me.getX()-play.getX()) < 55 && (me.getX()-play.getX()) > 0){
					
					//��ǰ��¼��ӵ�����(���ڻ���)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(play.getX(),play.getY()+57,55,55);
				}
				
				//�����ƶ����õ��յ������ģ���ɺϷ������ꡢ�������				
				else if (play.getY() > 284 && (me.getX() - play.getX()) >= 57 && (me.getX() - play.getX()) <= 112){
					play.setBounds(play.getX()+57,play.getY(),55,55);
				}
				
				//�����ƶ����õ��յ������ģ���ɺϷ������ꡢ�������
				else if (play.getY() > 284 && (play.getX() - me.getX()) >= 2 && (play.getX() - me.getX()) <=58){
					//ģ������
					play.setBounds(play.getX()-57,play.getY(),55,55);
				}
			}
			
			//��������
			else{
				//��ǰ��¼��ӵ�����(���ڻ���)
				Var.add(String.valueOf(play.isVisible()));
				Var.add(String.valueOf(play.getX()));
				Var.add(String.valueOf(play.getY()));
				Var.add(String.valueOf(Man));
				
				//�����ƶ����õ��յ������ģ���ɺϷ�������
				if ((me.getX()-play.getX()) >= 0 && (me.getX()-play.getX()) <= 55 && (play.getY()-me.getY()) >27 && play.getY()-me.getY() < 86){
					play.setBounds(play.getX(),play.getY()-57,55,55);
				}
				
				//�����ƶ����õ��յ������ģ���ɺϷ������ꡢ�������
				else if (play.getY() <= 341 && (me.getX() - play.getX()) >= 57 && (me.getX() - play.getX()) <= 112){
					play.setBounds(play.getX()+57,play.getY(),55,55);
				}				
				
				//�����ƶ����õ��յ������ģ���ɺϷ������ꡢ�������
				else if (play.getY() <= 341 && (play.getX() - me.getX()) >= 3 && (play.getX() - me.getX()) <=58){
					play.setBounds(play.getX()-57,play.getY(),55,55);
				}
			}
		}//���ƶ�����

		/**��������*/
		public void armsRule(JLabel play1,JLabel play2){
			//������
			if ((play2.getX() - play1.getX()) <= 112 && (play2.getX() - play1.getX()) >= 57 && (play1.getY() - play2.getY()) < 22 && (play1.getY() - play2.getY()) > -22 && play2.isVisible() && play1.getName().charAt(1)!=play2.getName().charAt(1)){
				//����Ҫ���Ӳ����ҳ���
				if (play1.getName().charAt(1) == '1' && play1.getY() > 284 && play1.getName().charAt(1) != play2.getName().charAt(1)){

					play2.setVisible(false);
					//�ѶԷ���λ�ø��Լ�
					play1.setBounds(play2.getX(),play2.getY(),55,55);
				}
				
				//����Ҫ���Ӳ����ܳ���
				else if (play1.getName().charAt(1) == '2' && play1.getY() < 341 && play1.getName().charAt(1) != play2.getName().charAt(1)){
					play2.setVisible(false);
					//�ѶԷ���λ�ø��Լ�
					play1.setBounds(play2.getX(),play2.getY(),55,55);				
				}
			}
			
			//������
			else if ((play1.getX() - play2.getX()) <= 112 && (play1.getX() - play2.getX()) >= 57 && (play1.getY() - play2.getY()) < 22 && (play1.getY() - play2.getY()) > -22 && play2.isVisible() && play1.getName().charAt(1)!=play2.getName().charAt(1)){
				//����Ҫ���Ӳ��������
				if (play1.getName().charAt(1) == '1' && play1.getY() > 284 && play1.getName().charAt(1) != play2.getName().charAt(1)){
					play2.setVisible(false);
					//�ѶԷ���λ�ø��Լ�
					play1.setBounds(play2.getX(),play2.getY(),55,55);
				}
				
				//����Ҫ���Ӳ����ҳ���
				else if (play1.getName().charAt(1) == '2' && play1.getY() < 341 && play1.getName().charAt(1) != play2.getName().charAt(1)){
					play2.setVisible(false);
					//�ѶԷ���λ�ø��Լ�
					play1.setBounds(play2.getX(),play2.getY(),55,55);				
				}
			}
			
			//������
			else if (play1.getX() - play2.getX() >= -22 && play1.getX() - play2.getX() <= 22 && play1.getY() - play2.getY() >= -112 && play1.getY() - play2.getY() <= 112){
				//���岻�����ϳ���
				if (play1.getName().charAt(1) == '1' && play1.getY() < play2.getY() && play1.getName().charAt(1) != play2.getName().charAt(1)){
					play2.setVisible(false);
					//�ѶԷ���λ�ø��Լ�
					play1.setBounds(play2.getX(),play2.getY(),55,55);
				}
				
				//���岻�����³���
				else if (play1.getName().charAt(1) == '2' && play1.getY() > play2.getY() && play1.getName().charAt(1) != play2.getName().charAt(1)){
					play2.setVisible(false);
					//�ѶԷ���λ�ø��Լ�
					play1.setBounds(play2.getX(),play2.getY(),55,55);
				}			
			}
			
			//��ǰ��¼��ӵ�����(���ڻ���)
			Var.add(String.valueOf(play1.isVisible()));
			Var.add(String.valueOf(play1.getX()));
			Var.add(String.valueOf(play1.getY()));
			Var.add(String.valueOf(Man));
			
			//��ǰ��¼��ӵ�����(���ڻ���)
			Var.add(String.valueOf(play2.isVisible()));
			Var.add(String.valueOf(play2.getX()));
			Var.add(String.valueOf(play2.getY()));
			Var.add(String.valueOf(i));

		}//��Խ���
		
		/**�ڡ����ƶ�����*/
		public void cannonRule(JLabel play,JLabel playQ[],MouseEvent me){
			//�����յ�֮���Ƿ�������
			int Count = 0;
			
			//�ϡ����ƶ�
			if (play.getX() - me.getX() <= 0 && play.getX() - me.getX() >= -55){
				//ָ������ģ��Y����
				for (int i=56;i<=571;i+=57){
					//�ƶ���Y�����Ƿ���ָ�����������
					if (i - me.getY() >= -27 && i - me.getY() <= 27){
						//���е�����
						for (int j=0;j<32;j++){
							//�ҳ���ͬһ�����ߵ��������ӡ����������Լ�
							if (playQ[j].getX() - play.getX() >= -27 && playQ[j].getX() - play.getX() <= 27 && playQ[j].getName()!=play.getName() && playQ[j].isVisible()){
								//����㵽�յ�(������)
								for (int k=play.getY()+57;k<i;k+=57){
									//������㡢С���յ������Ϳ���֪���м��Ƿ�������
									if (playQ[j].getY() < i && playQ[j].getY() > play.getY()){
										//�м���һ�����ӾͲ����Դ��������߹�ȥ
										Count++;
										break;
									}
								}//for
								
								//����㵽�յ�(���ҵ���)
								for (int k=i+57;k<play.getY();k+=57){
									//�������յ������
									if (playQ[j].getY() < play.getY() && playQ[j].getY() > i){
										Count++;
										break;
									}
								}//for
							}//if
						}//for
						
						//�����յ�û�����ӾͿ����ƶ���
						if (Count == 0){
							//��ǰ��¼��ӵ�����(���ڻ���)
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

			//�����ƶ�
			else if (play.getY() - me.getY() >=-27 && play.getY() - me.getY() <= 27){
				//ָ������ģ��X����
				for (int i=24;i<=480;i+=57){
					//�ƶ���X�����Ƿ���ָ�����������
					if (i - me.getX() >= -55 && i-me.getX() <= 0){
						//���е�����
						for (int j=0;j<32;j++){
							//�ҳ���ͬһ�����ߵ��������ӡ����������Լ�
							if (playQ[j].getY() - play.getY() >= -27 && playQ[j].getY() - play.getY() <= 27 && playQ[j].getName()!=play.getName() && playQ[j].isVisible()){
								//����㵽�յ�(���ϵ���)				
								for (int k=play.getX()+57;k<i;k+=57){
									//������㡢С���յ������Ϳ���֪���м��Ƿ�������
									if (playQ[j].getX() < i && playQ[j].getX() > play.getX()){
										//�м���һ�����ӾͲ����Դ��������߹�ȥ
										Count++;
										break;
									}
								}//for
								
								//����㵽�յ�(���µ���)
								for (int k=i+57;k<play.getX();k+=57){
									//�������յ������
									if (playQ[j].getX() < play.getX() && playQ[j].getX() > i){
										Count++;
										break;
									}
								}//for
							}//if
						}//for
						
						//�����յ�û������
						if (Count == 0){
							//��ǰ��¼��ӵ�����(���ڻ���)
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
			
		}//�ڡ����ƶ���������


		/**�ڡ����������*/
		public void cannonRule(int Chess,JLabel play,JLabel playTake,JLabel playQ[],MouseEvent me){
			//�����յ�֮���Ƿ�������
			int Count = 0;


			//���е�����
			for (int j=0;j<32;j++){
				//�ҳ���ͬһ�����ߵ��������ӡ����������Լ�
				if (playQ[j].getX() - play.getX() >= -27 && playQ[j].getX() - play.getX() <= 27 && playQ[j].getName()!=play.getName() && playQ[j].isVisible()){

					//�Լ�����㱻�Ե����յ�(���ϵ���)
					for (int k=play.getY()+57;k<playTake.getY();k+=57){
						//������㡢С���յ������Ϳ���֪���м��Ƿ�������
						if (playQ[j].getY() < playTake.getY() && playQ[j].getY() > play.getY()){
								//���������յ�����Ӹ���
								Count++;			
								break;							
						}
					}//for
								
					//�Լ�����㱻�Ե����յ�(���µ���)
					for (int k=playTake.getY();k<play.getY();k+=57){
						//�������յ������
						if (playQ[j].getY() < play.getY() && playQ[j].getY() > playTake.getY()){
								Count++;	
								break;
						}
					}//for
				}//if
							
				//�ҳ���ͬһ�����ߵ��������ӡ����������Լ�
				else if (playQ[j].getY() - play.getY() >= -10 && playQ[j].getY() - play.getY() <= 10 && playQ[j].getName()!=play.getName() && playQ[j].isVisible()){
					//�Լ�����㱻�Ե����յ�(������)
					for (int k=play.getX()+50;k<playTake.getX();k+=57){
						//������㡢С���յ������Ϳ���֪���м��Ƿ�������						
						if (playQ[j].getX() < playTake.getX() && playQ[j].getX() > play.getX()){
							Count++;			
							break;	
						}
					}//for
								
					//�Լ�����㱻�Ե����յ�(���ҵ���)
					for (int k=playTake.getX();k<play.getX();k+=57){
						//�������յ������
						if (playQ[j].getX() < play.getX() && playQ[j].getX() > playTake.getX()){
								Count++;
								break;
						}
					}//for
				}//if
			}//for
						
			//�����յ�֮��Ҫһ���������ڵĹ��򡢲����ܳ��Լ�������
			if (Count == 1 && Chess == 0 && playTake.getName().charAt(1) != play.getName().charAt(1)){
				//��ǰ��¼��ӵ�����(���ڻ���)
				Var.add(String.valueOf(play.isVisible()));
				Var.add(String.valueOf(play.getX()));
				Var.add(String.valueOf(play.getY()));
				Var.add(String.valueOf(Man));
				
				//��ǰ��¼��ӵ�����(���ڻ���)
				Var.add(String.valueOf(playTake.isVisible()));
				Var.add(String.valueOf(playTake.getX()));									
				Var.add(String.valueOf(playTake.getY()));
				Var.add(String.valueOf(i));
				
				playTake.setVisible(false);
				play.setBounds(playTake.getX(),playTake.getY(),55,55);
			}
			
			//�����յ�֮��û�������ǳ��Ĺ��򡢲����ܳ��Լ�������			
			else if (Count ==0  && Chess == 1 && playTake.getName().charAt(1) != play.getName().charAt(1)){
				
				//��ǰ��¼��ӵ�����(���ڻ���)
				Var.add(String.valueOf(play.isVisible()));
				Var.add(String.valueOf(play.getX()));									
				Var.add(String.valueOf(play.getY()));
				Var.add(String.valueOf(Man));
				
				//��ǰ��¼��ӵ�����(���ڻ���)
				Var.add(String.valueOf(playTake.isVisible()));
				Var.add(String.valueOf(playTake.getX()));									
				Var.add(String.valueOf(playTake.getY()));
				Var.add(String.valueOf(i));
				
				playTake.setVisible(false);
				play.setBounds(playTake.getX(),playTake.getY(),55,55);
			}
			
		}//�ڡ������巽������
		
		/**���ƶ�����*/
		public void horseRule(JLabel play,JLabel playQ[],MouseEvent me){
			//����������ϰ�
			int Ex=0,Ey=0,Move=0;			
			
			//���ơ����
			if (play.getX() - me.getX() >= 2 && play.getX() - me.getX() <= 57 && play.getY() - me.getY() >= 87 && play.getY() - me.getY() <= 141){
				//�Ϸ���Y����
				for (int i=56;i<=571;i+=57){
					//�ƶ���Y�����Ƿ���ָ�����������
					if (i - me.getY() >= -27 && i - me.getY() <= 27){
						Ey = i;
						break;
					}
				}
				
				//�Ϸ���X����
				for (int i=24;i<=480;i+=57){
					//�ƶ���X�����Ƿ���ָ�����������
					if (i - me.getX() >= -55 && i-me.getX() <= 0){
						Ex = i;
						break;
					}
				}
				
				//��ǰ���Ƿ��б������
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getX() - playQ[i].getX() == 0  && play.getY() - playQ[i].getY() == 57 ){
						Move = 1;
						break;
					}	
				}
				
				//�����ƶ�������
				if (Move == 0){
					//��ǰ��¼��ӵ�����(���ڻ���)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
									
					play.setBounds(Ex,Ey,55,55);
				}
				
			}//if
			
			//���ơ��ϱ�
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
				
				//�����Ƿ��б������
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getY() - playQ[i].getY() == 0 && play.getX() - playQ[i].getX() == 57 ){
						Move = 1;
						break;
					}
				}
				
				if (Move == 0){
					//��ǰ��¼��ӵ�����(���ڻ���)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(Ex,Ey,55,55);
				}
			}//else
			
			//���ơ��ұ�
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
				
				//���·��Ƿ��б������
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getX() - playQ[i].getX() == 0  && playQ[i].getY() - play.getY() == 57 ){
						Move = 1;
						break;
					}
				}
				
				if (Move == 0){
					//��ǰ��¼��ӵ�����(���ڻ���)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(Ex,Ey,55,55);
				}
			}//else
			
			//���ơ��ұ�
			else if (play.getY() - me.getY() >= 87 && play.getY() - me.getY() <= 141 && me.getX() - play.getX() <= 87 && me.getX() - play.getX() >= 30 ){
				//�Ϸ���Y����
				for (int i=56;i<=571;i+=57){
					if (i - me.getY() >= -27 && i - me.getY() <= 27){
						Ey = i;
						break;
					}
				}
				
				//�Ϸ���X����
				for (int i=24;i<=480;i+=57){
					if (i - me.getX() >= -55 && i-me.getX() <= 0){
						Ex = i;
						break;
					}
				}
				
				//��ǰ���Ƿ��б������
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
				
				//�����ƶ�������
				if (Move == 0){
					//��ǰ��¼��ӵ�����(���ڻ���)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));	
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(Ex,Ey,55,55);
				}
			}//else 
			
			//���ơ����
			else if (me.getY() - play.getY() >= 87 && me.getY() - play.getY() <= 141 && play.getX() - me.getX() <= 87 && play.getX() - me.getX() >= 10 ){
				//�Ϸ���Y����
				for (int i=56;i<=571;i+=57){
					if (i - me.getY() >= -27 && i - me.getY() <= 27){
						Ey = i;
						break;
					}
				}
				
				//�Ϸ���X����
				for (int i=24;i<=480;i+=57){
					if (i - me.getX() >= -55 && i-me.getX() <= 0){
						Ex = i;
						break;
					}
				}
				
				//���·��Ƿ��б������
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getX() - playQ[i].getX() == 0 && play.getY() - playQ[i].getY() == 57 ){
						Move = 1;
						break;
					}
				}
				
				//�����ƶ�������
				if (Move == 0){
					//��ǰ��¼��ӵ�����(���ڻ���)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(Ex,Ey,55,55);
				}
			}//else
			
			//���ơ��ϱ�
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
				
				//���ҷ��Ƿ��б������
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getY() - playQ[i].getY() == 0 && playQ[i].getX() - play.getX() == 57 ){
						Move = 1;
						break;
					}
				}
				
				if (Move == 0){
					//��ǰ��¼��ӵ�����(���ڻ���)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(Ex,Ey,55,55);
				}
			}//else
			
			//���ơ��±�
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
				
				//���ҷ��Ƿ��б������
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getY() - playQ[i].getY() == 0 && playQ[i].getX() - play.getX() == 57 ){
						Move = 1;
						break;
					}
				}
				
				if (Move == 0){
					//��ǰ��¼��ӵ�����(���ڻ���)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(Ex,Ey,55,55);
				}
			}//else
			
			//���ơ��±�
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
				
				//�����Ƿ��б������
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getY() - playQ[i].getY() == 0 && play.getX() - playQ[i].getX() == 57 ){
						Move = 1;
						break;
					}
				}
				
				if (Move == 0){
					//��ǰ��¼��ӵ�����(���ڻ���)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
				
					play.setBounds(Ex,Ey,55,55);
				}
				
			}//else
			
		}//���ƶ�����

		/**��������*/
		public void horseRule(JLabel play,JLabel playTake ,JLabel playQ[],MouseEvent me){
			//�ϰ�
			int Move=0;
			boolean Chess=false;
			
			//���ơ����
			if (play.getName().charAt(1)!=playTake.getName().charAt(1) && play.getX() - playTake.getX() == 57 && play.getY() - playTake.getY() == 114 ){
				//��ǰ���Ƿ��б������
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getX() - playQ[i].getX() == 0 && play.getY() - playQ[i].getY() == 57){
						Move = 1;
						break;
					}
				}//for
				
				Chess = true;
				
			}//if
			
			//���ơ��ҳ�
			else if (play.getY() - playTake.getY() == 114 && playTake.getX() - play.getX() == 57 ){
				//��ǰ���Ƿ��б������
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getX() - playQ[i].getX() == 0 && play.getY() - playQ[i].getY() == 57){
						Move = 1;
						break;
					}
				}//for		
				
				Chess = true;
				
			}//else
			
			//���ơ��ϳ�
			else if (play.getY() - playTake.getY() == 57 && play.getX() - playTake.getX() == 114 ){
				//�����Ƿ��б������
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getY() - playQ[i].getY() == 0 && play.getX() - playQ[i].getX() == 57){
						Move = 1;
						break;
					}
				}//for
				
				Chess = true;
				
			}//else
			
			//���ơ��³�
			else if (playTake.getY() - play.getY() == 57 && play.getX() - playTake.getX() == 114 ){
				//�����Ƿ��б������
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getY() - playQ[i].getY() == 0 && play.getX() - playQ[i].getX() == 57){
						Move = 1;
						break;
					}
				}//for
				
				Chess = true;
				
			}//else
			
			//���ơ��ϳ�
			else if (play.getY() - playTake.getY() == 57 && playTake.getX() - play.getX() == 114 ){
				//���ҷ��Ƿ��б������
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getY() - playQ[i].getY() == 0 && playQ[i].getX() - play.getX() == 57){
						Move = 1;
						break;
					}
				}//for
				
				Chess = true;
				
			}//else
			
			//���ơ��³�
			else if (playTake.getY() - play.getY() == 57  && playTake.getX() - play.getX() == 114 ){
				//���ҷ��Ƿ��б������
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getY() - playQ[i].getY() == 0 && playQ[i].getX() - play.getX() == 57){
						Move = 1;
						break;
					}
				}//for
				
				Chess = true;
				
			}//else
			
			//���ơ����
			else if (playTake.getY() - play.getY() == 114 && play.getX() - playTake.getX() == 57 ){
				//���·��Ƿ��б������
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getX() - playQ[i].getX() == 0 && play.getY() - playQ[i].getY() == -57 ){
						Move = 1;
						break;
						
					}
				}//for
				
				Chess = true;
				
			}//else 
			
			//���ơ��ҳ�
			else if (playTake.getY() - play.getY() == 114 && playTake.getX() - play.getX() == 57){
				//���·��Ƿ��б������
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getX() - playQ[i].getX() == 0 && play.getY() - playQ[i].getY() == -57 ){
						Move = 1;
						break;
					}
				}//for
				
				Chess = true;
				
			}//else  
			
			//û���ϰ��������Գ��塢���ܳ��Լ���ɫ
			if (Chess && Move == 0 && playTake.getName().charAt(1) != play.getName().charAt(1)){
				//��ǰ��¼��ӵ�����(���ڻ���)
				Var.add(String.valueOf(play.isVisible()));
				Var.add(String.valueOf(play.getX()));
				Var.add(String.valueOf(play.getY()));
				Var.add(String.valueOf(Man));
				
				//��ǰ��¼��ӵ�����(���ڻ���)
				Var.add(String.valueOf(playTake.isVisible()));
				Var.add(String.valueOf(playTake.getX()));
				Var.add(String.valueOf(playTake.getY()));
				Var.add(String.valueOf(i));			
				
				playTake.setVisible(false);
				play.setBounds(playTake.getX(),playTake.getY(),55,55);
			}
		}
		
		/**���ƶ�����*/
		public void elephantRule(int Man,JLabel play,JLabel playQ[],MouseEvent me){
			//������ϰ�
			int Ex=0,Ey=0,Move=0;
			
			//����
			if (play.getX() - me.getX() <= 141 && play.getX() - me.getX() >= 87 && play.getY() - me.getY() <= 141 && play.getY() - me.getY() >= 87){
				//�Ϸ���Y����
				for (int i=56;i<=571;i+=57){
					if (i - me.getY() >= -27 && i - me.getY() <= 27){
						Ey = i;
						break;
					}
				}
				
				//�Ϸ���X����
				for (int i=24;i<=480;i+=57){
					if (i - me.getX() >= -27 && i-me.getX() <= 27){
						Ex = i;
						break;
					}
				}
				
				//���Ϸ��Ƿ�������
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getX() - playQ[i].getX() == 57 && play.getY() - playQ[i].getY() == 57){
						Move++;
						break;
					}
				}
				
				//���첻�ܹ�����
				if (Move == 0 && Ey >= 341 && Man > 9){
					//��ǰ��¼��ӵ�����(���ڻ���)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
								
								System.out.println("Ex="+Ex);
								System.out.println("Ey="+Ey);
					play.setBounds(Ex,Ey,55,55);
				}
				
				//���첻�ܹ�����
				else if (Move == 0 && Ey <= 284 && Man < 10){
					//��ǰ��¼��ӵ�����(���ڻ���)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(Ex,Ey,55,55);	
				}
			}//if
			
			//����
			else if (play.getY() - me.getY() <= 141 && play.getY() - me.getY() >= 87 &&  me.getX() - play.getX() >= 87 && me.getX() - play.getX() <= 141){
				//�Ϸ���Y����
				for (int i=56;i<=571;i+=57){
					if (i - me.getY() >= -27 && i - me.getY() <= 27){
						Ey = i;
						break;
					}
				}
				
				//�Ϸ���X����
				for (int i=24;i<=480;i+=57){
					if (i - me.getX() >= -27 && i-me.getX() <= 27){
						Ex = i;
						break;
					}
				}
				
				//���Ϸ��Ƿ�������
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() &&  playQ[i].getX() - play.getX() == 57 && play.getY() - playQ[i].getY() == 57){
						Move++;
						break;
					}
				}
				
				//�ࡢ�����
				if (Move == 0 && Ey >= 341 && Man > 9){
					//��ǰ��¼��ӵ�����(���ڻ���)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(Ex,Ey,55,55);
				}
				
				else if (Move == 0 && Ey <= 284 && Man < 10){
					//��ǰ��¼��ӵ�����(���ڻ���)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(Ex,Ey,55,55);
				}
				
			}// else if 
			
			//����
			else if (play.getX() - me.getX() <= 141 && play.getX() - me.getX() >= 87 && me.getY() - play.getY() <= 141 && me.getY() - play.getY() >= 87){
				//�Ϸ���Y����
				for (int i=56;i<=571;i+=57){
					if (i - me.getY() >= -27 && i - me.getY() <= 27){
						Ey = i;
						break;
					}
				}
				
				//�Ϸ���X����
				for (int i=24;i<=480;i+=57){
					if (i - me.getX() >= -27 && i-me.getX() <= 27){
						Ex = i;
						break;
					}
				}
				
				//�����Ƿ�������
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getX() - playQ[i].getX() == 57 && play.getY() - playQ[i].getY() == -57){
						Move++;
						break;
					}
				}			
				
				//�ࡢ�����
				
				if (Move == 0 && Ey >= 341 && Man > 9){
					//��ǰ��¼��ӵ�����(���ڻ���)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
									
					play.setBounds(Ex,Ey,55,55);
				}
				
				else if (Move == 0 && Ey <= 284 && Man < 10)
				{
					//��ǰ��¼��ӵ�����(���ڻ���)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(Ex,Ey,55,55);
				}
			}//else if 
			
			//����
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
				
				//���ҷ��Ƿ�������
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && playQ[i].getX() - play.getX() == 57 && playQ[i].getY() - play.getY() == 57){
						Move = 1;
						break;
					}
				}
				
				//�ࡢ�����
				if (Move == 0 && Ey >= 341 && Man > 9){
					//��ǰ��¼��ӵ�����(���ڻ���)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(Ex,Ey,55,55);
				}
				
				else if (Move == 0 && Ey <= 284 && Man < 10){
					//��ǰ��¼��ӵ�����(���ڻ���)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));									
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(Ex,Ey,55,55);
				}
				
			}//else
			
		}//���ƶ�������

		/**�ࡢ��������*/
		public void elephantRule(JLabel play,JLabel playTake,JLabel playQ[]){
			//�ϰ�
			int Move=0;
			boolean Chess=false;
			
			//�����Ϸ�������
			if (play.getX() - playTake.getX() >= 87 && play.getX() - playTake.getX() <= 141 && play.getY() - playTake.getY() >= 87 && play.getY() - playTake.getY() <= 141){
				//���Ϸ��Ƿ�������
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getX() - playQ[i].getX() == 57 && play.getY() - playQ[i].getY() == 57){
						Move++;
						break;
					}
				}//for

				Chess=true;
				
			}//if
			
			//�����Ϸ�������
			else if (playTake.getX() - play.getX() >= 87 && playTake.getX() - play.getX() <= 141 && play.getY() - playTake.getY() >= 87 && play.getY() - playTake.getY() <= 141 ){
				//���Ϸ��Ƿ�������
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() &&  playQ[i].getX() - play.getX() == 57 && play.getY() - playQ[i].getY() == 57 ){
						Move++;
						break;
					}
				}//for	
				
				Chess=true;
			}//else
			
			//�����󷽵�����
			else if (play.getX() - playTake.getX() >= 87 && play.getX() - playTake.getX() <= 141 && playTake.getY() - play.getY() >= 87 && playTake.getY() - play.getY() <= 141){
				//�����Ƿ�������
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && play.getX() - playQ[i].getX() == 57 && play.getY() - playQ[i].getY() == -57 ){
						Move++;
						break;
					}
				}//for
				
				Chess=true;
			}//else
			
			//�����ҷŵ�����
			else if (playTake.getX() - play.getX() >= 87 && playTake.getX() - play.getX() <= 141 && playTake.getY() - play.getY() >= 87 && playTake.getY() - play.getY() <= 141){
				//���ҷ��Ƿ�������
				for (int i=0;i<32;i++){
					if (playQ[i].isVisible() && playQ[i].getX() - play.getX() == 57 && playQ[i].getY() - play.getY() == 57 ){
						Move = 1;
						break;
					}
				}//for		

				Chess=true;
				
			}//else
			
			//û���ϰ��������ܳ��Լ�������
			if (Chess && Move == 0 && playTake.getName().charAt(1) != play.getName().charAt(1)){
				//��ǰ��¼��ӵ�����(���ڻ���)
				Var.add(String.valueOf(play.isVisible()));
				Var.add(String.valueOf(play.getX()));
				Var.add(String.valueOf(play.getY()));
				Var.add(String.valueOf(Man));
				
				//��ǰ��¼��ӵ�����(���ڻ���)
				Var.add(String.valueOf(playTake.isVisible()));
				Var.add(String.valueOf(playTake.getX()));
				Var.add(String.valueOf(playTake.getY()));
				Var.add(String.valueOf(i));
				
				playTake.setVisible(false);
				play.setBounds(playTake.getX(),playTake.getY(),55,55);
			}
			
		}//�ࡢ�����������
		
		/**ʿ�����ƶ�����*/
		public void chapRule(int Man,JLabel play,JLabel playQ[],MouseEvent me){
			//�ϡ���
			if (me.getX() - play.getX() >= 29 && me.getX() - play.getX() <= 114 && play.getY() - me.getY() >= 25 && play.getY() - me.getY() <= 90){
				//ʿ���ܳ����Լ��Ľ���
				if (Man < 14 && (play.getX()+57) >= 195 && (play.getX()+57) <= 309 && (play.getY()-57) <= 170){
					//��ǰ��¼��ӵ�����(���ڻ���)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(play.getX()+57,play.getY()-57,55,55);
				}	
				
				//�˲��ܳ����Լ��Ľ���
				else if (Man > 13 && (play.getY()-57) >= 455 && (play.getX()+57)  >= 195 && (play.getX()+57) <= 309){
					//��ǰ��¼��ӵ�����(���ڻ���)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(play.getX()+57,play.getY()-57,55,55);
				}	
			}// else if 
			
			//�ϡ���
			else if (play.getX() - me.getX() <= 114 && play.getX() - me.getX() >= 25 && play.getY() - me.getY() >= 20 && play.getY() - me.getY() <= 95){
				//ʿ���ܳ����Լ��Ľ���
				if (Man < 14 &&  (play.getX()-57) >= 195 && (play.getX()-57) <= 309 && (play.getY()-57) <= 170  ){
					//��ǰ��¼��ӵ�����(���ڻ���)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(play.getX()-57,play.getY()-57,55,55);
				}	
				
				//�˲��ܳ����Լ��Ľ���
				else if (Man > 13 &&(play.getY()-57) >= 455 && (play.getX()-57)  >= 195 && (play.getX()-57) <= 309){
					//��ǰ��¼��ӵ�����(���ڻ���)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(play.getX()-57,play.getY()-57,55,55);
				}	
			}// else if 
			
			//�¡���
			else if (play.getX() - me.getX() <= 114 && play.getX() - me.getX() >= 20 && me.getY() - play.getY() >= 2 && me.getY() - play.getY() <= 87){
				//ʿ���ܳ����Լ��Ľ���
				if (Man < 14 && (play.getX()-57) >= 195 && (play.getX()-57) <= 309 && (play.getY()+57) <= 170 ){
					//��ǰ��¼��ӵ�����(���ڻ���)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(play.getX()-57,play.getY()+57,55,55);
				}	
				
				//�˲��ܳ����Լ��Ľ���
				else if (Man > 13 && (play.getY()+57) >= 455 && (play.getX()-57)  >= 195 && (play.getX()-57) <= 309){
					//��ǰ��¼��ӵ�����(���ڻ���)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(play.getX()-57,play.getY()+57,55,55);
				}
				
			}// else if 
			
			
			//�¡���
			else if (me.getX() - play.getX() >= 27 && me.getX() - play.getX() <= 114 && me.getY() - play.getY() >= 2 && me.getY() - play.getY() <= 87){
				//ʿ���ܳ����Լ��Ľ���
				if (Man < 14 && (play.getX()+57) >= 195 && (play.getX()+57) <= 309 && (play.getY()+57) <= 170){
					//��ǰ��¼��ӵ�����(���ڻ���)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(play.getX()+57,play.getY()+57,55,55);
				}
				
				//�˲��ܳ����Լ��Ľ���
				else if (Man > 13 &&(play.getY()+57) >= 455 && (play.getX()+57)  >= 195 && (play.getX()+57) <= 309){
					//��ǰ��¼��ӵ�����(���ڻ���)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(play.getX()+57,play.getY()+57,55,55);
				}
			}//else if 
			
		}//ʿ�����ƶ��������


		/**ʿ���˳������*/
		public void chapRule(int Man ,JLabel play,JLabel playTake,JLabel playQ[]){
			//��ǰ״̬
			boolean Chap = false;	
			
			//�ϡ���
			if (playTake.getX() - play.getX() >= 20 && playTake.getX() - play.getX() <= 114 && play.getY() - playTake.getY() >= 2 && play.getY() - playTake.getY() <= 87){
				//���Ե������Ƿ�͵�ǰʿ���
				if (Man < 14 && playTake.getX() >= 195 && playTake.getX() <= 309 && playTake.getY() <= 170 && playTake.isVisible()){
					Chap = true;
				}
				
				//���Ե������Ƿ�͵�ǰ�����
				else if (Man > 13 && playTake.getX() >= 195 && playTake.getX() <= 309 && playTake.getY() >= 455 && playTake.isVisible()){
					Chap = true;
				}
			}//if
			
			//�ϡ���
			else if (play.getX() - playTake.getX() <= 114 && play.getX() - playTake.getX() >= 25 && play.getY() - playTake.getY() >= 2 && play.getY() - playTake.getY() <= 87){
				//���Ե������Ƿ�͵�ǰʿ���
				if (Man < 14 && playTake.getX() >= 195 && playTake.getX() <= 309 && playTake.getY() <= 170 && playTake.isVisible()){
					Chap = true;
				}
				
				//���Ե������Ƿ�͵�ǰ�����
				else if (Man > 13 && playTake.getX() >= 195 && playTake.getX() <= 309 && playTake.getY() >= 455 && playTake.isVisible()){
					Chap = true;
				}
			}// else if 
			
			//�¡���
			else if (play.getX() - playTake.getX() <= 114 && play.getX() - playTake.getX() >= 25 && playTake.getY() - play.getY() >= 2 && playTake.getY() - play.getY() <= 87){
				//���Ե������Ƿ�͵�ǰʿ���
				if (Man < 14 && playTake.getX() >= 195 && playTake.getX() <= 309 && playTake.getY() <= 170 && playTake.isVisible()){
					Chap = true;
				}
				
				//���Ե������Ƿ�͵�ǰ�����
				else if (Man > 13 && playTake.getX() >= 195 && playTake.getX() <= 309 && playTake.getY() >= 455 && playTake.isVisible()){
					Chap = true;
				}
			}// else if 
			
			//�¡���
			else if (playTake.getX() - play.getX() >= 25 && playTake.getX() - play.getX() <= 114 && playTake.getY() - play.getY() >= 2 && playTake.getY() - play.getY() <= 87){
				//���Ե������Ƿ�͵�ǰʿ���
				if (Man < 14 && playTake.getX() >= 195 && playTake.getX() <= 309 && playTake.getY() <= 170 && playTake.isVisible()){
					Chap = true;
				}
				
				//���Ե������Ƿ�͵�ǰ�����
				else if (Man > 13 && playTake.getX() >= 195 && playTake.getX() <= 309 && playTake.getY() >= 455 && playTake.isVisible()){
					Chap = true;
				}
			}//else if 
			
			//���ƶ��������ܳ��Լ�������
			if (Chap && playTake.getName().charAt(1) != play.getName().charAt(1)){
				//��ǰ��¼��ӵ�����(���ڻ���)
				Var.add(String.valueOf(play.isVisible()));
				Var.add(String.valueOf(play.getX()));
				Var.add(String.valueOf(play.getY()));
				Var.add(String.valueOf(Man));
				
				//��ǰ��¼��ӵ�����(���ڻ���)
				Var.add(String.valueOf(playTake.isVisible()));
				Var.add(String.valueOf(playTake.getX()));
				Var.add(String.valueOf(playTake.getY()));
				Var.add(String.valueOf(i));
				
				playTake.setVisible(false);
				play.setBounds(playTake.getX(),playTake.getY(),55,55);
			}
			
		}//ʿ���˳���������
		
		/**���ƶ�����*/
		public void willRule(int Man,JLabel play,JLabel playQ[],MouseEvent me){
			//����
			if ((me.getX()-play.getX()) >= 0 && (me.getX()-play.getX()) <= 55 && (play.getY()-me.getY()) >=2 && play.getY()-me.getY() <= 87){
				//���Ƿ񳬹��Լ��Ľ���
				if (Man == 30 && me.getX() >= 195 && me.getX() <= 359 && me.getY() <= 170){
					//��ǰ��¼��ӵ�����(���ڻ���)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(play.getX(),play.getY()-57,55,55);	
				}	
				
				//˧�Ƿ񳬹��Լ��Ľ���
				else if (Man == 31 && me.getY() >= 455 && me.getX() >= 195 && me.getX() <= 359){
					//��ǰ��¼��ӵ�����(���ڻ���)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(play.getX(),play.getY()-57,55,55);
				}
			}//if
			
			//����
			else if (play.getX() - me.getX() >= 2 && play.getX() - me.getX() <= 57 && me.getY() - play.getY() <= 27 && me.getY() - play.getY() >= -27){
				//���Ƿ񳬹��Լ��Ľ���
				if (Man == 30 && me.getX() >= 195 && me.getX() <= 359 && me.getY() <= 170){
					//��ǰ��¼��ӵ�����(���ڻ���)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(play.getX()-57,play.getY(),55,55);
				}
				
				//˧�Ƿ񳬹��Լ��Ľ���
				else if (Man == 31 && me.getY() >= 455 && me.getX() >= 195 && me.getX() <= 359){
					//��ǰ��¼��ӵ�����(���ڻ���)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(play.getX()-57,play.getY(),55,55);
				}
			}//else if 
			
			//����
			else if (me.getX() - play.getX() >= 57 && me.getX() - play.getX() <= 112 && me.getY() - play.getY() <= 27 && me.getY() - play.getY() >= -27){
				//����˧����
				if (Man == 30 && me.getX() >= 195 && me.getX() <= 359 && me.getY() <= 170){
					//��ǰ��¼��ӵ�����(���ڻ���)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(play.getX()+57,play.getY(),55,55);	
				}	
				
				else if (Man == 31 && me.getY() >= 455 && me.getX() >= 195 && me.getX() <= 359){
					//��ǰ��¼��ӵ�����(���ڻ���)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));	
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(play.getX()+57,play.getY(),55,55);
				}
			}//else if 
			
			//����
			else if (me.getX() - play.getX() >= 0 && me.getX() - play.getX() <= 55 && me.getY() - play.getY() <= 87 && me.getY() - play.getY() >= 27){
				//����˧����
				if (Man == 30 && me.getX() >= 195 && me.getX() <= 359 && me.getY() <= 170){
					//��ǰ��¼��ӵ�����(���ڻ���)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
					
					play.setBounds(play.getX(),play.getY()+57,55,55);
				}
				
				else if (Man == 31 && me.getY() >= 455 && me.getX() >= 195 && me.getX() <= 359){
					//��ǰ��¼��ӵ�����(���ڻ���)
					Var.add(String.valueOf(play.isVisible()));
					Var.add(String.valueOf(play.getX()));
					Var.add(String.valueOf(play.getY()));
					Var.add(String.valueOf(Man));
				
					play.setBounds(play.getX(),play.getY()+57,55,55);
				}

			}//else if
			
		}//����˧�ƶ��������

		public void willRule(int Man ,JLabel play,JLabel playTake ,JLabel playQ[]){
			//��ǰ״̬
			boolean will = false;
			
			//���ϳ�
			if (play.getX() - playTake.getX() >= 0 && play.getX() - playTake.getX() <= 55 && play.getY() - playTake.getY() >= 27 && play.getY() - playTake.getY() <= 87 && playTake.isVisible()){
				//���Ե������Ƿ�͵�ǰ�����
				if (Man == 30 && playTake.getX() >= 195 && playTake.getX() <= 309 && playTake.getY() <= 170){
					will = true;
				}
				
				//���Ե������Ƿ�͵�ǰ˧���
				else if (Man == 31 && playTake.getY() >= 455 && playTake.getX() >= 195 && playTake.getX() <= 309){
					will = true; 
				}
			}
			
			//�����
			else if (play.getX() - playTake.getX() >= 2 && play.getX() - playTake.getX() <= 57 && playTake.getY() - play.getY() <= 27 && playTake.getY() - play.getY() >= -27 && playTake.isVisible()){
				//���Ե������Ƿ�͵�ǰ�����
				if (Man == 30 && playTake.getX() >= 195 && playTake.getX() <= 309 && playTake.getY() <= 170){
					will = true;
				}
				
				//���Ե������Ƿ�͵�ǰ˧���
				else if (Man == 31 && playTake.getY() >= 455 && playTake.getX() >= 195 && playTake.getX() <= 309){
					will = true; 
				}
			}
			
			//���ҳ�
			else if (playTake.getX() - play.getX() >= 2 && playTake.getX() - play.getX() <= 57 && playTake.getY() - play.getY() <= 27 && playTake.getY() - play.getY() >= -27 && playTake.isVisible()){
				//���Ե������Ƿ�͵�ǰ�����
				if (Man == 30 && playTake.getX() >= 195 && playTake.getX() <= 309 && playTake.getY() <= 170){
					will = true;
				}
				
				//���Ե������Ƿ�͵�ǰ˧���
				else if (Man == 31 && playTake.getY() >= 455 && playTake.getX() >= 195 && playTake.getX() <= 309){
					will = true; 
				}
			}
			
			//����
			else if (playTake.getX() - play.getX() >= 0 && playTake.getX() - play.getX() <= 87 && playTake.getY() - play.getY() <= 27 && playTake.getY() - play.getY() >= 40 && playTake.isVisible()){
				//���Ե������Ƿ�͵�ǰ�����
				if (Man == 30 && playTake.getX() >= 195 && playTake.getX() <= 309 && playTake.getY() <= 170){
					will = true;
				}
				
				//���Ե������Ƿ�͵�ǰ˧���
				else if (Man == 31 && playTake.getY() >= 455 && playTake.getX() >= 195 && playTake.getX() <= 309){
					will = true; 
				}
			}
				
			//���ܳ��Լ������ӡ����ϵ�ǰҪ��	
			if (playTake.getName().charAt(1) != play.getName().charAt(1) && will){
				//��ǰ��¼��ӵ�����(���ڻ���)
				Var.add(String.valueOf(play.isVisible()));
				Var.add(String.valueOf(play.getX()));
				Var.add(String.valueOf(play.getY()));
				Var.add(String.valueOf(Man));
				
				//��ǰ��¼��ӵ�����(���ڻ���)
				Var.add(String.valueOf(playTake.isVisible()));
				Var.add(String.valueOf(playTake.getX()));
				Var.add(String.valueOf(playTake.getY()));
				Var.add(String.valueOf(i));

				playTake.setVisible(false);
				play.setBounds(playTake.getX(),playTake.getY(),55,55);
			}			
			
		}//����˧�Թ������
		
	}//������



    private void resetGame() {
        // ��������߼�
        // ���磬���³�ʼ�����̣���������λ�õ�
        rule = new ChessRule(); // ����ʵ����������
        Var.clear(); // ��ղ�����¼
        chessPlayClick = 2; // �췽����
        text.setText("��ӭʹ���������ϵͳ");

        // ���»��ƽ���
        repaint();
    }

    private void log(int x, int y){
        JLabel jLabel = play[Man];
        int moveX = (jLabel.getX() - x) /55;
        int moveY = (jLabel.getY() - y) /55;
        String moveXAction = moveX > 0 ? "����" : "����";
        String moveYAction = moveY > 0 ? "����" : "����";
        if(moveX != 0 || moveY != 0) {
            logStr = String.format("��%s����%s %s %s �� %s %s �� \n",
                    ++logCount, jLabel.getName(), moveYAction, Math.abs(moveY), moveXAction, Math.abs(moveX)) + logStr;
        }

    }
	
}//�������
