package compont;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TimeSettingDialog extends JDialog {
    private JTextField moveTimeField;
    private JTextField gameTimeField;
    private JButton okButton;
    private JButton cancelButton;
    private long moveTimeLimit;
    private long gameTimeLimit;

    public TimeSettingDialog(Frame owner) {
        super(owner, "����ʱ��", true);
        setLayout(new GridLayout(3, 2));
        
        moveTimeField = new JTextField(5);
        gameTimeField = new JTextField(5);

        add(new JLabel("ÿ����ʱ�����ƣ��룩��"));
        add(moveTimeField);
        add(new JLabel("������Ϸʱ�����ƣ����ӣ���"));
        add(gameTimeField);

        okButton = new JButton("ȷ��");
        cancelButton = new JButton("ȡ��");

        add(okButton);
        add(cancelButton);

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    moveTimeLimit = Long.parseLong(moveTimeField.getText()) * 1000; // ת��Ϊ����
                    gameTimeLimit = Long.parseLong(gameTimeField.getText()) * 60000; // ת��Ϊ����
                    setVisible(false);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(TimeSettingDialog.this, "��������Ч������", "����", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveTimeLimit = -1; // ��ʾȡ��
                gameTimeLimit = -1;
                setVisible(false);
            }
        });

        pack();
        setLocationRelativeTo(owner);
    }

    public long[] getTimeLimits() {
        return new long[]{moveTimeLimit, gameTimeLimit};
    }
}
