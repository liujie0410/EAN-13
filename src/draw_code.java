import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

class Show_UI extends JPanel{
    /**绘图*/
    public void paint(Graphics g) {
        super.paint(g);
        int x1,y1,x2,y2;
        Graphics2D g_2d = (Graphics2D)g;
        g_2d.setColor(Color.black);
        g_2d.setStroke(new BasicStroke(2,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL));
        g_2d.drawLine(100,50,100,150);//画线，左右边界横纵坐标
        g_2d.drawString(String.valueOf(draw_code.eancode[0]),92,150);//写字
        g_2d.drawLine(104,50,104,150);
        x1 = 106;		x2 = 106;
        y1 = 50;		y2 = 140;
        /**依次访问条码二进制数组中的每一个*/
        for(int i=1;i<7;i++) {//左侧数据符
            g_2d.drawString(String.valueOf(draw_code.eancode[i]),x1,y2+10);
            for(int j=0;j<7;j++) {
                if(draw_code.Bcode[i][j]==0) {
                    x1+=2;
                    x2+=2;
                }
                else if(draw_code.Bcode[i][j]==1) {
                    g_2d.drawLine(x1,y1,x2,y2);
                    x1+=2;
                    x2+=2;
                }
            }
        }
        g_2d.drawLine(x1+2,y1,x2+2,150);//中间间隔符
        g_2d.drawLine(x1+6,y1,x2+6,150);
        x1+=10;
        x2+=10;
        for(int i=7;i<13;i++) {//右侧数据符
            g_2d.drawString(String.valueOf(draw_code.eancode[i]),x1,y2+10);
            for(int j=0;j<7;j++) {
                if(draw_code.Bcode[i][j]==0) {
                    x1+=2;
                    x2+=2;
                }
                else if(draw_code.Bcode[i][j]==1) {
                    g_2d.drawLine(x1,y1,x2,y2);
                    x1+=2;
                    x2+=2;
                }
            }

        }
        g_2d.drawLine(x1,y1,x2,150);
        g_2d.drawLine(x1+4,y1,x2+4,150);
    }
}

public class draw_code extends JFrame implements ActionListener{
    /**SHOW UI*/
    JButton input_btn = new JButton("Get Code");
    Show_UI panel = new Show_UI();
    JPanel p2 = new JPanel();
    JPanel p4 = new JPanel();
    JPanel p3 = new JPanel();
    JPanel p5 = new JPanel();
    JTextField seq = new JTextField(15);
    JLabel lb = new JLabel("Please input your numbers：");

    /**B、C字符集分别按照0～9的顺序，将二进制字符串存为数组形式*/
    int[][] A = {{0,0,0,1,1,0,1},{0,0,1,1,0,0,1},{0,0,1,0,0,1,1},{0,1,1,1,1,0,1},{0,1,0,0,0,1,1,}, {0,1,1,0,0,0,1},{0,1,0,1,1,1,1},{0,1,1,1,0,1,1},{0,1,1,0,1,1,1},{0,0,0,1,0,1,1}};
    int[][] B = {{0,1,0,0,1,1,1},{0,1,1,0,0,1,1},{0,0,1,1,0,1,1},{0,1,0,0,0,0,1},{0,0,1,1,1,0,1}, {0,1,1,1,0,0,1},{0,0,0,0,1,0,1},{0,0,1,0,0,0,1},{0,0,0,1,0,0,1},{0,0,1,0,1,1,1}};
    int[][] C = {{1,1,1,0,0,1,0},{1,1,0,0,1,1,0},{1,1,0,1,1,0,0},{1,0,0,0,0,1,0},{1,0,1,1,1,0,0}, {1,0,0,1,1,1,0},{1,0,1,0,0,0,0},{1,0,0,0,1,0,0},{1,0,0,1,0,0,0},{1,1,1,0,1,0,0}};

    /**初始化条码：6+ABBBAA+CCCCC+C*/
    static int[][] Bcode = {{0,0,0,0,0,0,0},{0,0,0,1,0,1,1},{0,1,0,0,1,1,1},{0,1,0,0,1,1,1},{0,1,0,0,1,1,1},{0,0,0,1,1,0,1},{0,0,0,1,1,0,1}, {1,1,1,0,0,1,0},{1,1,1,0,0,1,0},{1,1,1,0,0,1,0},{1,1,1,0,0,1,0},{1,1,1,0,0,1,0},{1,0,0,0,1,0,0}};
    static int[] eancode = {6,9,0,0,0,0,0,0,0,0,0,0,7};

    /**画条码*/
    public draw_code() {
        this.setSize(400,400);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new GridLayout(2,1,1,1));
        this.add(panel);
        this.add(p4);
        this.getContentPane().setBackground(Color.white);
        panel.setBackground(Color.white);
        p4.setBackground(Color.white);
        p2.setBackground(Color.white);
        p3.setBackground(Color.white);
        p5.setBackground(Color.white);
        p4.setLayout(new GridLayout(3,1));
        p4.add(p5);
        p4.add(p2);
        p4.add(p3);
        p2.setLayout(new FlowLayout());
        p2.add(lb);
        p2.add(seq);
        p3.add(input_btn);
        input_btn.addActionListener(this);
        input_btn.setActionCommand("Get Code");
    }

    /**鼠标点击事件：唤醒条码二进制化*/
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals("Get Code")) {
            for(int i=0,j=0;i<12;i++,j++) {//更新eancode，即获取输入的代码
                eancode[i] = Integer.parseInt(seq.getText().substring(j,j+1));
            }
            /**计算校验符：通过计算三倍的偶数位和加上奇数位和，然后所得数与大于该值且最接近其的整十数值的差值，得到校验和*/
            int sum =0;
            sum = eancode[11]+eancode[9]+eancode[7]+eancode[5]+eancode[3]+eancode[1];
            sum = sum*3;
            sum += eancode[10]+eancode[8]+eancode[6]+eancode[4]+eancode[2]+eancode[0];
            eancode[12] = 10-sum%10;
            /**根据前置码，判断字符集，将左侧数据符转化为0、1*/
            switch(eancode[0]) {
                case 0: Bcode[1] = A[eancode[1]];Bcode[2] = A[eancode[2]];Bcode[3] = A[eancode[3]];Bcode[4] = A[eancode[4]];Bcode[5] = A[eancode[5]];Bcode[6] = A[eancode[6]];break;
                case 1: Bcode[1] = A[eancode[1]];Bcode[2] = A[eancode[2]];Bcode[3] = B[eancode[3]];Bcode[4] = A[eancode[4]];Bcode[5] = B[eancode[5]];Bcode[6] = B[eancode[6]];break;
                case 2: Bcode[1] = A[eancode[1]];Bcode[2] = A[eancode[2]];Bcode[3] = B[eancode[3]];Bcode[4] = B[eancode[4]];Bcode[5] = A[eancode[5]];Bcode[6] = B[eancode[6]];break;
                case 3: Bcode[1] = A[eancode[1]];Bcode[2] = A[eancode[2]];Bcode[3] = B[eancode[3]];Bcode[4] = B[eancode[4]];Bcode[5] = B[eancode[5]];Bcode[6] = A[eancode[6]];break;
                case 4:Bcode[1] = A[eancode[1]];Bcode[2] = B[eancode[2]];Bcode[3] = A[eancode[3]];Bcode[4] = A[eancode[4]];Bcode[5] = B[eancode[5]];Bcode[6] = B[eancode[6]];break;
                case 5:Bcode[1] = A[eancode[1]];Bcode[2] = B[eancode[2]];Bcode[3] = B[eancode[3]];Bcode[4] = A[eancode[4]];Bcode[5] = A[eancode[5]];Bcode[6] = B[eancode[6]];break;
                case 6:Bcode[1] = A[eancode[1]];Bcode[2] = B[eancode[2]];Bcode[3] = B[eancode[3]];Bcode[4] = B[eancode[4]];Bcode[5] = A[eancode[5]];Bcode[6] = A[eancode[6]];break;
                case 7:Bcode[1] = A[eancode[1]];Bcode[2] = B[eancode[2]];Bcode[3] = A[eancode[3]];Bcode[4] = B[eancode[4]];Bcode[5] = A[eancode[5]];Bcode[6] = B[eancode[6]];break;
                case 8:Bcode[1] = A[eancode[1]];Bcode[2] = B[eancode[2]];Bcode[3] = A[eancode[3]];Bcode[4] = B[eancode[4]];Bcode[5] = B[eancode[5]];Bcode[6] = A[eancode[6]];break;
                case 9:Bcode[1] = A[eancode[1]];Bcode[2] = B[eancode[2]];Bcode[3] = B[eancode[3]];Bcode[4] = A[eancode[4]];Bcode[5] = B[eancode[5]];Bcode[6] = A[eancode[6]];break;
            }
            /**右侧数据符按照C字符集转换为0、1*/
            for(int i=7;i<13;i++) {
                Bcode[i] = C[eancode[i]];
            }
        }
        //更新条码图片
        panel = new Show_UI();
        this.remove(panel);
        this.repaint();
    }

    public static void main(String[] args) {
        new draw_code().setVisible(true);
    }
}

