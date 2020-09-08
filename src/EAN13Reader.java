import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class EAN13Reader {
    static int HEIGHT = 6; //截取的样本图像像素高度
    static int BW_MID = 100; //判断黑白的阀值

   /** ean13是一种（7,2）码，即每个字符的总宽度为7个模块宽，交替的两条两空，而每个条空的宽度不超过4个模块*/
   //基准数组，A和B的子集转换为同色连续字符集
    static int [][] LEFT_TABLE_INT = new int[] []{
            {3,2,1,1},{2,2,2,1},{2,1,2,2},{1,4,1,1},{1,1,3,2},
            {1,2,3,1},{1,1,1,4},{1,3,1,2},{1,2,1,3},{3,1,1,2},
            {1,1,2,3},{1,2,2,2},{2,2,1,2},{1,1,4,1},{2,3,1,1},
            {1,3,2,1},{4,1,1,1},{3,1,2,1},{3,1,2,1},{2,1,1,3}
    };//左端，二维数组，[20][4]
    //A=（a1,a2,a3,a4） 表示一个条码字符，1<=ai<=4,a1+a2+a3+a4=7，每位a表示条码的宽度
    //前十组为A子集，后十组为B子集
    //例如，第一个元素 {3,2,1,1}即为A子集中的0001101
    static int [][] RIGHT_TABLE_INT = new int[] []{
            {3,2,1,1},{2,2,2,1},{2,1,2,2},{1,4,1,1},{1,1,3,2},
            {1,2,3,1},{1,1,1,4},{1,3,1,2},{1,2,1,3},{3,1,1,2}
    };//右端[10][4]，C字符集


    public static void main(String[] args) {

        /************以下为UI*************/
        int gap = 10;
        JFrame f = new JFrame("条码识别系统");
        f.setSize(410, 400);
        f.setLocation(200, 200);
        f.setLayout(null);

        JPanel pInput = new JPanel();
        pInput.setBounds(gap, gap,260,120);
        pInput.setLayout(new GridLayout(4,3,gap,gap));


        JLabel warning = new JLabel("请输入图片地址或点击'浏览'选择文件位置");
        JLabel location = new JLabel("图片地址:");
        JTextField locationText = new JTextField();
        JButton b = new JButton("生成");
        JButton btn = new JButton("浏览文件");

        pInput.add(warning);
        pInput.add(location);
        pInput.add(locationText);

        //文本域
        JTextArea ta = new JTextArea();
        ta.setLineWrap(true);
        b.setBounds(150, 130, 80, 30);
        btn.setBounds(280,70,80,30);
        ta.setBounds(gap, 170, 375, 170);


        f.add(pInput);
        f.add(b);
        f.add(ta);
        f.add(btn);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        f.setVisible(true);
        //鼠标监听
        b.addActionListener(new ActionListener() {
            boolean checkedpass = true;
            public void actionPerformed(ActionEvent e) {
                checkedpass = true;
                checkEmpty(locationText,"图片地址");
                String src = locationText.getText();//获得选取的图片地址
                try{
                    BufferedImage img = getImg(src);  /**读取图片*/
                    String model =getRealValue(img); /**根据条码图像区域返回条码数据*/
                    if(checkedpass){
                        String result= String.format(model, location);
                        ta.setText("");
                        ta.append(result); //显示结果
                    }
                }catch(Exception f){
                    f.printStackTrace();
                }

            }

            //检验图片地址是否为空
            private void checkEmpty(JTextField tf, String msg){
                if(!checkedpass)
                    return;
                String value = tf.getText();
                if(value.length()==0){
                    JOptionPane.showMessageDialog(f, msg + " 不能为空");
                    tf.grabFocus();
                    checkedpass = false;
                }
            }

        });
        btn.addActionListener(new ActionListener() {  /**选择图片地址*/
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                chooser.showDialog(new JLabel(), "选择");
                File file = chooser.getSelectedFile();
                locationText.setText(file.getAbsoluteFile().toString());

            }
        });

    }
    /**读取图片*/
    public static BufferedImage getImg(String srcImg){
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(srcImg));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }
    /**根据条码图像区域返回条码数据*/
    public static String getRealValue(BufferedImage img){
        int w = img.getWidth(); //宽度
        int h = img.getHeight();//高度
        int y = (int)(h/2); //高度的一半
        if(y<HEIGHT){//图片太小无法识别
            return "无法识别的条码";
        }
        if(h>HEIGHT*2)
            y= (int)(h/2)-(HEIGHT/2); //确定一个取像素点的高度位置，尽可能在中间，保留足够的高度取多个点求像素的平均值
        else y = 1;
        StringBuffer valuestr = new StringBuffer(""); //记录识别结果的字符串缓存区
        int chg = 0; //记录颜色变化次数
        int gvn = 0; //记录实际取值次数（每变化一次取值一次）
        int ln = 0; //记录同色连续像素的数量
        int uprv= -1; //记录上次的像素取值
        int xdv = 0; //参考底色，取前8个像素的颜色作为底色参考值
        int[] gvalue = new int[4]; //临时记录一组数字

        for(int i=0;i<w;i++){
            int v = 0; //记录取值为深色的次数
            //高度取HEIGHT个像素,并求平均值，如果平均值为浅色，那么取0，否则取1
            for(int j=y;j<y+HEIGHT;j++){
                int prgb = getValue(img.getRGB(i,j));
                xdv += prgb;   //参考底色，取前8个像素的颜色作为底色参考值
                if(prgb<BW_MID-80) v+=1; //累加该列为深色的像素个数
            }
            int rv = 0; //标记0或1
            if(i<8){
               //取参考颜色样本，默认都为0
                rv = 0;
                BW_MID = xdv/((i+1)*(HEIGHT)); //不断变换参考样本值
            }else{
                rv = v>HEIGHT/2?1:0; //n个以上的像素为深色，就表示该列为深色
            }
            if(rv==uprv){  //若颜色与上一个像素点一样，则同色连续像素数量+1
                ln++;
            }else{
                chg++; //若颜色与上一个像素点相比改变，变化次数+1
                if(chg>1 && chg<6){//起始符号
                    gvalue[(chg-2)%4] = ln; //识别图像后，以每次像素改变的位置作为数组序号，记录同色连续像素取值
                    if((chg-1)%4==0){
                        //将gvalue作为读取到的样本值，与基准值，通过方差和的方式去噪求值
                        valuestr.append(getSTDValue(gvalue, LEFT_TABLE_INT));//valuestr：记录识别结果的字符串缓存区
                        //System.out.println("1:   "+getSTDValue(gvalue, LEFT_TABLE_INT));
                    }
                }
                if((chg>5 && chg<30)|| (chg>34 && chg<59)){//跳过中间间隔符，左侧数据符和右侧数据符
                    gvn++; //实际取值次数
                    if(gvn%4==0){
                        gvalue[(gvn-1)%4] = ln;
                        if(gvn<=24){
                            valuestr.append(getSTDValue(gvalue, LEFT_TABLE_INT));
                           // System.out.println("2:   "+getSTDValue(gvalue, LEFT_TABLE_INT));
                        }else{
                            valuestr.append(getSTDValue(gvalue, RIGHT_TABLE_INT));
                           // System.out.println("3:   "+getSTDValue(gvalue, RIGHT_TABLE_INT));
                        }
                    }else {
                        gvalue[(gvn-1)%4] = ln;
                    }
                }
                ln = 1;
            }
            uprv = rv;
        }
        String results=valuestr.toString()+"\n(其中最后一位为校验位)";
        return results;
    }

    /** 由一组样本与基准表，通过方差和的方式去噪求值*/
    public static int getSTDValue(final int[] mvalues,int[][] STDTABLE){
        int rvalue = 0;
        int fx = 0;
        System.out.println("step1");
        for(int i=0;i<STDTABLE.length;i++){//i为数组某元素的位置序号，与A或B(-10)对应的数字字符一样
            System.out.println("step2");
            int[] svalues = LEFT_TABLE_INT[i]; //基准值
            System.out.println("i="+i+", LEFT_TABLE_INT[i]="+LEFT_TABLE_INT[i].toString());
            int[] xvalue = new int[4];
            for(int j=0;j<mvalues.length;j++){
                xvalue[j] = (int)((double)mvalues[j]/svalues[j]+0.5);
                System.out.println("xvalue"+j+": "+xvalue[j]);
            }
            int tmp = getFX(xvalue);//方差和
            System.out.println("tmp"+i+":"+tmp);
            System.out.println("fx"+i+":"+fx);
            if(i==0){
                fx = tmp;
            }else{
                if(tmp<fx){
                   fx = tmp;
                    rvalue = i>9?i-10:i;//大于9即为B子集
                    System.out.println("rvalue"+i+":"+rvalue);
                }
            }
        }
        return rvalue;
    }

    /**求一组样本数的方差和(针对所有样本数都相同的情况简化算法)*/
    public static int getFX(int[] numlist){
        if(numlist.length<1) return 0;
        int fx = 0;
        double avg = 0;

        for(int i=0;i<numlist.length;i++){
            avg += numlist[i];
        }
        avg = (int)((double)avg/numlist.length+0.5);

        for(int i=0;i<numlist.length;i++){
            fx += (numlist[i]-avg)*(numlist[i]-avg);
        }
        return fx;
    }

    /**判断黑白，由色彩值返回单色信号 1 或 0*/
    public static int getValue(int rgb){
        int r = (rgb>>16)&255;
        int g = (rgb>>8)&255;
        int b = rgb&255;
        double avg = 0.299*r+0.587*g+0.114*b; //判断黑白
        return (int)avg;
    }
}
