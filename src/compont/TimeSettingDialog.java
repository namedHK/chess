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
        super(owner, "设置时间", true);
        setLayout(new GridLayout(3, 2));
        
        moveTimeField = new JTextField(5);
        gameTimeField = new JTextField(5);

        add(new JLabel("每步棋时间限制（秒）："));
        add(moveTimeField);
        add(new JLabel("整局游戏时间限制（分钟）："));
        add(gameTimeField);

        okButton = new JButton("确定");
        cancelButton = new JButton("取消");

        add(okButton);
        add(cancelButton);

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    moveTimeLimit = Long.parseLong(moveTimeField.getText()) * 1000; // 转换为毫秒
                    gameTimeLimit = Long.parseLong(gameTimeField.getText()) * 60000; // 转换为毫秒
                    setVisible(false);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(TimeSettingDialog.this, "请输入有效的数字", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveTimeLimit = -1; // 表示取消
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
