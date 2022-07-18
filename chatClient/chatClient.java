import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.Structure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonGenerator.Feature;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;
import java.awt.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class chatClient {
    public static void main(String[] args) {
        boolean com = false;
        if (args.length == 1) {
            if (args[0].equals("-c")) {
                com = true;
            }
        }
        new Client("Chat", "Config", com);
    }
}

class Share {
    public static String Message;
    public static boolean SendFlag = false;
    public static boolean QuitFlag = false;
    public static boolean RecvFlag = false;
    public static boolean RecvPastFlag = false;
    public static boolean Exception = false;
    public static boolean ThreadEndFlag = false;
    public static boolean NameListFlag = false;
    public static boolean isMe = false;
    public static boolean isServer = false;
    public static ArrayList<String> PastMessage;
    public static ArrayList<Boolean> isServerPast;
    public static ArrayList<MemberObj> Member;
    public static int myToken;
    
    public static boolean flag() {
        return SendFlag | QuitFlag | RecvFlag | RecvPastFlag | Exception | ThreadEndFlag | NameListFlag;
    }
    
    public static void change(ShareF s) {
        Message = s.Message;
        SendFlag = s.SendFlag;
        QuitFlag = s.QuitFlag;
        RecvFlag = s.RecvFlag;
        RecvPastFlag = s.RecvPastFlag;
        Exception = s.Exception;
        ThreadEndFlag = s.ThreadEndFlag;
        NameListFlag = s.NameListFlag;
        isMe = s.isMe;
        isServer = s.isServer;
    }
}

class ShareF {
    public String Message;
    public boolean SendFlag;
    public boolean QuitFlag;
    public boolean RecvFlag;
    public boolean RecvPastFlag;
    public boolean Exception;
    public boolean ThreadEndFlag;
    public boolean NameListFlag;
    public boolean isMe;
    public boolean isServer;
    
    ShareF() {
        reset();
    }
    
    public void reset() {
        Message = "";
        SendFlag = false;
        QuitFlag = false;
        RecvFlag = false;
        RecvPastFlag = false;
        Exception = false;
        ThreadEndFlag = false;
        NameListFlag = false;
        isMe = false;
        isServer = false;
    }
}

class ShareList {
    public static ArrayList<ShareF> list;
    
    public static int size() {
        return list.size();
    }
    
    public static ShareF get(int i) {
        return list.get(i);
    }
    
    public static ShareF peek() {
        return list.get(0);
    }
    
    public static ShareF poll() {
        ShareF s = list.get(0);
        list.remove(0);
        return s;
    }
    
    public static void remove() {
        list.remove(0);
    }
    
    public static void add(ShareF s) {
        list.add(s);
    }
}

class Client implements ActionListener,WindowListener {
    private int PORT;
    private String IP;
    private String name;
    private JFrame frame, frame2;
    private JTextField IPText, PortText, NameText;
    private JTextField SendText;
    private IPv4 ip;
    private Timer timer;
    private Share share;
    private ShareF shareflag;
    private ShareList list;
    private JTextPane RecvText, NameListText;
    private DefaultStyledDocument docN, docR;
    private StyleContext scN, scR;
    private boolean com;
    private final int ARRAY_SIZE = 2048;
    
    public interface Socket extends Library {
    	// loadLibraryの第一引数はあとで作成するlib***.soの***と一致させる。
        Socket INSTANCE = (Socket) Native.load("socket", Socket.class);
        
        // Cの関数名と一致させる
        boolean getNB(byte[] buff, int buffsize);
        void Sleep_(int us);
    }
    
    Client(String mainTitle, String subTitle, boolean com) {
        share = new Share();
        shareflag = new ShareF();
        list = new ShareList();
        list.list = new ArrayList<>();
        share.Member = new ArrayList<>();
        timer = new Timer(1 , this);
        
        ip = new IPv4();
        
        if (com) {
            byte[] input = new byte[ARRAY_SIZE];
            Socket soc = Socket.INSTANCE;
            do {
                System.out.print("IP => ");
                while (!soc.getNB(input, ARRAY_SIZE)) {
                    soc.Sleep_(10000);
                }
                IP = Native.toString(input);
            } while(!ip.is_ip(IP));
            for (int i = 0; i < ARRAY_SIZE; i++) {
                input[i] = '\0';
            }
            do {
                System.out.print("PORT => ");
                while (!soc.getNB(input, ARRAY_SIZE)) {
                    soc.Sleep_(10000);
                }
                String str = Native.toString(input);
                try {
                    PORT = Integer.parseInt(str);
                } catch (Exception e) {
                    PORT = -1;
                }
            } while(PORT < 0 || PORT > 65535);
            for (int i = 0; i < ARRAY_SIZE; i++) {
                input[i] = '\0';
            }
            do {
                System.out.print("name => ");
                while (!soc.getNB(input, ARRAY_SIZE)) {
                    soc.Sleep_(10000);
                }
                name = Native.toString(input);
            } while(name.equals(""));
            ClientTransmission client = new ClientTransmission(IP, PORT, name, true);
            try {
                client.runClient();
            } catch (Exception e) {
                System.out.println(e);
            }
        } else {
            MainWindow(mainTitle);
            SubWindow(subTitle);
 		    timer.setActionCommand("Timer");
            timer.start();
        }
    }
    
    public void MainWindow(String Title) {
    	frame = new JFrame();
 		frame.setTitle(Title);
 		frame.setBounds( 10, 10, 960, 720);
 		JButton btn = new JButton("Window Close");
 		btn.addActionListener(this);
 		btn.setActionCommand("Exit Button");
 		JPanel p1 = new JPanel();
 		p1.setLayout(new FlowLayout(FlowLayout.LEFT));
 		JPanel p2 = new JPanel();
 		p2.setLayout(new BoxLayout(p2, BoxLayout.LINE_AXIS));
 		p2.setPreferredSize(new Dimension((int)(frame.getWidth() * 0.8), frame.getHeight()));
 		JPanel p3 = new JPanel();
 		p3.setLayout(new BoxLayout(p3, BoxLayout.LINE_AXIS));
 		p3.setPreferredSize(new Dimension((int)(frame.getWidth() * 0.2), frame.getHeight()));
 		JLabel label1 = new JLabel("Message : ");
 		SendText = new JTextField(50);
 		SendText.setToolTipText("送信メッセージを入力してください");
 		JButton btn2 = new JButton("Enter");
 		SendText.addActionListener(this);
 		SendText.setActionCommand("SendText");
 		btn2.addActionListener(this);
 		btn2.setActionCommand("Send");
 		RecvText = new JTextPane();
 		RecvText.setEditable(false);
 		JScrollPane RecvScroll = new JScrollPane(RecvText, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		NameListText = new JTextPane();
 		NameListText.setEditable(false);
 		JScrollPane NameListScroll = new JScrollPane(NameListText, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        p1.add(label1);
        p1.add(SendText);
        p1.add(btn2);
        p2.add(RecvScroll);
        p3.add(NameListScroll);
        frame.getContentPane().add(p1, BorderLayout.NORTH);
 		frame.addWindowListener(this);
 		frame.getContentPane().add(p2, BorderLayout.WEST);
 		frame.getContentPane().add(p3, BorderLayout.EAST);
 		frame.getContentPane().add(btn, BorderLayout.SOUTH);
 		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    	frame.setVisible(false);
    	scR = new StyleContext();
    	docR = new DefaultStyledDocument(scR);
    	RecvText.setDocument(docR);
    	scN = new StyleContext();
    	docN = new DefaultStyledDocument(scN);
    	NameListText.setDocument(docN);
    }
    
    public void SubWindow(String Title) {
    	frame2 = new JFrame();
 		frame2.setTitle(Title);
 		frame2.setBounds( 10, 10, 350, 200);
 		JPanel p = new JPanel();
 		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
 		JPanel p1 = new JPanel();
 		p1.setLayout(new FlowLayout(FlowLayout.LEFT));
 		JPanel p2 = new JPanel();
 		p2.setLayout(new FlowLayout(FlowLayout.LEFT));
 		JPanel p3 = new JPanel();
 		p3.setLayout(new FlowLayout(FlowLayout.LEFT));
 		JLabel label1 = new JLabel("IP : ");
 		JLabel label2 = new JLabel("Port : ");
 		JLabel label3 = new JLabel("Name : ");
 		IPText = new JTextField("127.0.0.1", 15);
 		PortText = new JTextField("50000", 5);
 		NameText = new JTextField(20);
 		IPText.addActionListener(this);
 		IPText.setActionCommand("IPText");
 		PortText.addActionListener(this);
 		PortText.setActionCommand("PortText");
 		NameText.addActionListener(this);
 		NameText.setActionCommand("NameText");
        p1.add(label1);
        p2.add(label2);
        p3.add(label3);
        p1.add(IPText);
        p2.add(PortText);
        p3.add(NameText);
        p.add(p1);
        p.add(p2);
        p.add(p3);
 		JButton btn2 = new JButton("Enter");
 		btn2.addActionListener(this);
 		btn2.setActionCommand("Enter");
 		frame2.addWindowListener(this);
 		frame2.getContentPane().add(p, BorderLayout.CENTER);
 		frame2.getContentPane().add(btn2, BorderLayout.SOUTH);
 		frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame2.setVisible(true);
    }
	
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.equals("Timer")) {
		    if (share.Exception) {
		        JOptionPane.showMessageDialog(frame, share.Message, "Error", 
  JOptionPane.ERROR_MESSAGE);
		        frame.dispose();
		    } else if (share.ThreadEndFlag) {
		        JOptionPane.showMessageDialog(frame, share.Message, "Information", 
  JOptionPane.INFORMATION_MESSAGE);
		        frame.dispose();
		    } else if (share.NameListFlag) {
		        share.NameListFlag = false;
		        int[] rtn = new int[2];
		        rtn = makeNameList();
		        MutableAttributeSet attr = new SimpleAttributeSet();
		        StyleConstants.setForeground(attr, Color.blue);
		        StyleConstants.setBold(attr, true);
		        docN.setCharacterAttributes(rtn[0], rtn[1], attr, false);
		    } else if (share.RecvPastFlag) {
		        initMessageList();
		        int[] rtn = new int[2];
		        MutableAttributeSet attr = new SimpleAttributeSet();
		        for (int i = 0; i < share.PastMessage.size(); i++) {
		            share.Message = share.PastMessage.get(i);
		            rtn = makeMessageList();
		            if (share.isServerPast.get(i)) {
		                StyleConstants.setForeground(attr, Color.red);
		                docR.setCharacterAttributes(rtn[0], rtn[1], attr, false);
		            }
		        }
		        share.RecvPastFlag = false;
		    }
		    if (share.RecvFlag) {
		        int[] rtn = new int[2];
		        rtn = makeMessageList();
		        MutableAttributeSet attr = new SimpleAttributeSet();
		        if (share.isMe) {
		            StyleConstants.setForeground(attr, Color.blue);
		            docR.setCharacterAttributes(rtn[0], rtn[1], attr, false);
		        } else if (share.isServer) {
		            StyleConstants.setForeground(attr, Color.red);
		            docR.setCharacterAttributes(rtn[0], rtn[1], attr, false);
		        }
		        share.RecvFlag = false;
		        share.isMe = false;
		        share.isServer = false;
		    }
		} else if (cmd.equals("Exit Button")){
		    if (share.flag()) {
		        shareflag.SendFlag = true;
		        shareflag.QuitFlag = true;
		        list.add(shareflag);
		        shareflag.reset();
		    } else {
		        share.SendFlag = true;
		        share.QuitFlag = true;
		    }
			frame.dispose();
		} else if (cmd.equals("Send") || cmd.equals("SendText")) {
		    if (!SendText.getText().equals("")) {
		        if (share.flag()) {
		            shareflag.SendFlag = true;
		            shareflag.Message = SendText.getText();
		            list.add(shareflag);
		            shareflag.reset();
		        } else {
		            share.SendFlag = true;
		            share.Message = SendText.getText();
		        }
		        SendText.setText("");
		    }
		} else if (cmd.equals("Enter") || cmd.equals("NameText") || cmd.equals("IPText") || cmd.equals("PortText")) {
		    IP = IPText.getText();
		    try {
		        PORT = Integer.parseInt(PortText.getText());
		    } catch (NumberFormatException ex) {
		        PORT = -1;
		    }
		    name = NameText.getText();
            try {
                if (!name.equals("") && PORT >= 0 && PORT <= 65535 && ip.is_ip(IP)) {
			        frame2.dispose();
			        frame.setVisible(true);
			        ClientTransmission r = new ClientTransmission(IP, PORT, name, false);
                    Thread t = new Thread(r);
                    t.start();
                }
            } catch (Exception ex) {
                System.out.println(ex);
            }
		}
		if (!share.flag() && list.size() > 0) {
		    share.change(list.poll());
		}
	}
	
	void initMessageList() {
	    try {
	        docR.insertString(0, "", scR.getStyle(StyleContext.DEFAULT_STYLE));
	    } catch (Exception e) {
	        System.err.println("Failed to read string.");
	    }
	}
	
	int[] makeMessageList() {
	    int[] rtn = new int[2];
	    rtn[0] = docR.getLength();
	    try {
	        docR.insertString(docR.getLength(), share.Message, scR.getStyle(StyleContext.DEFAULT_STYLE));
	        docR.insertString(docR.getLength(), "\n", scR.getStyle(StyleContext.DEFAULT_STYLE));
	    } catch (Exception e) {
	        System.err.println("Failed to read string.");
	    }
	    rtn[1] = share.Message.length();
	    return rtn;
	}
	
	int[] makeNameList() {
	    StringBuffer sb = new StringBuffer();
	    int[] rtn = new int[2];
	    int docLength = 0, strlen;
	    for (int i = 0; i < share.Member.size(); i++) {
	        sb.append(share.Member.get(i).name);
	        strlen = share.Member.get(i).name.length();
	        if (share.Member.get(i).token == share.myToken) {
	            rtn[0] = docLength;
	            rtn[1] = strlen;
	        }
	        sb.append("\n");
	        docLength += strlen + 1;
	    }
	    try {
	        docN.remove(0, docN.getLength());
	        docN.insertString(0, new String(sb), scN.getStyle(StyleContext.DEFAULT_STYLE));
	    } catch (Exception e) {
	        System.err.println("Failed to read string.");
	    }
	    return rtn;
	}
	
	public void windowClosing(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
		if (e.getWindow().getName().equals(frame.getName())) {
		    share.QuitFlag = true;
		    share.SendFlag = true;
		    try {
		        join();
		    } catch (Exception ex) {
		        System.out.println(ex);
		    }
		    System.exit(0);
    	} else {
    		frame.setVisible(true);
    	}
	}
	
	private void join() throws Exception {
        while (!share.ThreadEndFlag) {
            Thread.sleep(10);
        }
	}
	
	public void windowOpened(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}
}
 
class ClientTransmission implements Runnable {
    private int PORT;
    private String IP;
    private String name;
    private Socket soc;
    private final int ARRAY_SIZE = 2048;
    private Share share;
    private ShareF shareflag;
    private ShareList list;
    private boolean com;
    
    public interface Socket extends Library {
    	// loadLibraryの第一引数はあとで作成するlib***.soの***と一致させる。
        Socket INSTANCE = (Socket) Native.load("socket", Socket.class);
        
        // Cの関数名と一致させる
        boolean getflag();
        int connectS(int sock, String ip, int port);
        void Sleep_(int us);
        void nonBlocking(int sock);
        boolean notRecv();
        int errorNum();
        int TCPSocket();
        int UDPSocket();
        int bindNIP(int sock, int port);
        int bindIP(int sock, int port, String ip);
        int listenNB(int sock);
        int acceptNI(int sock);
        int acceptInfo(int sock,IntByReference port, byte[] ip);
        void closeSocket(int sock);
        int selectNT(int sock, fd_set fds);
        int selectT(int sock, int s, int us, fd_set fds);
        int recvS(int sock, byte[] buf, int size);
        int recvfromS(int sock, byte[] buf, int size, byte[] ip, IntByReference port);
        int sendS(int sock, String buf, int size);
        int recvI(int sock, IntByReference buf, int size);
        int recvfromI(int sock, IntByReference buf, int size, byte[] ip, IntByReference port);
        int sendI(int sock, int[] buf, int size);
        int recvD(int sock, DoubleByReference buf, int size);
        int recvfromD(int sock, DoubleByReference buf, int size, byte[] ip, IntByReference port);
        int sendD(int sock, double[] buf, int size);
        void FD_ZERO_(fd_set fds);
        void FD_SET_(int sock, fd_set fds);
        boolean FD_ISSET_(int sock, fd_set fds);
        long inetAddr(String cp);
        long htonLong(long hostlong);
        short htonShort(short hostshort);
        long ntohLong(long netlong);
        short ntohShort(short netshort);
        boolean ReadNB(int fd, byte[] buff, int buffSize);
        boolean getNB(byte[] buff, int buffsize);
        int strLength(String str);
    }
    
    public static int[] convertInt(int n) {
        int rtn[] = new int[1];
        rtn[0] = n;
        return rtn;
    }
    
    public static double[] convertDouble(double n) {
        double rtn[] = new double[1];
        rtn[0] = n;
        return rtn;
    }
    
    public static class fd_set extends Structure {
        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("fds_bits");
        }
        public long fds_bits[] = new long[16];
    }
    
    ClientTransmission(String ip, int port, String name, boolean com) {
        this.name = name;
        PORT = port;
        IP = ip;
        this.com = com;
        soc = Socket.INSTANCE;
        share = new Share();
        shareflag = new ShareF();
        list = new ShareList();
    }
    
    public void run() {
        try {
            runClient();
        } catch (Exception e) {
            share.Exception = true;
            share.Message = String.valueOf(e);
            System.out.println(e);
        }
        share.ThreadEndFlag = true;
    }
    
    public void runClient() throws Exception {
        int sock = createSocket();
        byte[] buf = new byte[ARRAY_SIZE];
        byte[] input = new byte[ARRAY_SIZE];
        MessageObj mobj = new MessageObj();
        MemberObj memObj = new MemberObj();
        MemberSTokenObj myobj = new MemberSTokenObj();
        ArrayList<MemberObj> memberList = new ArrayList<>();
        share.isServerPast = new ArrayList<>();
        share.PastMessage = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(Feature.ESCAPE_NON_ASCII, true);
        myobj.name = name;
        connect(sock);
        
        mobj.message = myobj.name;
        mobj.token = (Object) (-1);
        mobj.type = "Send Name";
        String sendStr = mapper.writeValueAsString(mobj);
         if (soc.sendS(sock, sendStr, ARRAY_SIZE) < 0) {
            System.out.println("Failed to send.");
        }
        String recvStr;
        while (true) {
            int recv = soc.recvS(sock, buf, ARRAY_SIZE);
            if (recv < 0) {
                if (!soc.notRecv() && !soc.getflag()) {
                    System.out.println("Failed to recieve.");
                }
            } else {
                recvStr = new String(buf, "UTF-8");
                try {
                    mobj = mapper.readValue(recvStr, MessageObj.class);
                } catch (Exception excep) {
                    continue;
                }
                if (mobj.type.equals("Send StrToken")) {
                    break;
                }
            }
            soc.Sleep_(10000);
        }
        myobj.stoken = mobj.message;
        while (true) {
            int recv = soc.recvS(sock, buf, ARRAY_SIZE);
            if (recv < 0) {
                if (!soc.notRecv() && !soc.getflag()) {
                    System.out.println("Failed to recieve.");
                }
            } else {
                recvStr = new String(buf, "UTF-8");
                try {
                    mobj = mapper.readValue(recvStr, MessageObj.class);
                } catch (Exception excep) {
                    continue;
                }
                if (mobj.type.equals("Send IntToken")) {
                    break;
                }
            }
            soc.Sleep_(10000);
        }
        myobj.token = Integer.parseInt(mobj.message);
        share.myToken = Integer.parseInt(mobj.message);
        soc.getflag();
        
        while (true) {
            int recv = soc.recvS(sock, buf, ARRAY_SIZE);
            if (recv < 0) {
                if (!soc.notRecv() && !soc.getflag()) {
                    System.out.println("Failed to recieve.");
                }
            } else {
                recvStr = new String(buf, "UTF-8");
                try {
                    mobj = mapper.readValue(recvStr, MessageObj.class);
                } catch (Exception excep) {
                    continue;
                }
                if (mobj.type.equals("Send List")) {
                    break;
                }
            }
            soc.Sleep_(10000);
        }
        JsonNode node = mapper.readValue(mobj.message, JsonNode.class);
        MemberObj[] listValue = mapper.readValue(String.valueOf(node.get("list")), MemberObj[].class);
        for (MemberObj listValueData : listValue) {
            memberList.add(listValueData);
            share.Member.add(listValueData);
        }
        if (com) {
            System.out.println("Member:");
            for (int i = 0; i < memberList.size(); i++) {
                System.out.println(memberList.get(i).name);
            }
        } else {
            share.NameListFlag = true;
        }
        
        while (true) {
            int recv = soc.recvS(sock, buf, ARRAY_SIZE);
            if (recv < 0) {
                if (!soc.notRecv() && !soc.getflag()) {
                    System.out.println("Failed to recieve.");
                }
            } else {
                String str = new String(buf, "UTF-8");
                try {
                    mobj = mapper.readValue(str, MessageObj.class);
                } catch (Exception excep) {
                    continue;
                }
                if (mobj.type.equals("Down")) {
                    if (com) {
                        System.out.println("* サーバが終了しました *");
                    } else {
                        share.Message = "* サーバが終了しました *";
                    }
                    soc.closeSocket(sock);
                    break;
                } else if (mobj.type.equals("Add")) {
                    node = mapper.readValue(str, JsonNode.class);
                    node = node.get("message");
                    memObj = mapper.readValue(node.asText(), MemberObj.class);
                    if (com) {
                        System.out.println("* " + memObj.name + "さんが入室しました *");
                    } else {
                        if (share.flag()) {
                            shareflag.isServer = true;
                            if (memObj.name != null) {
                                shareflag.Message = "* " + memObj.name + "さんが入室しました *";
                            }
                            shareflag.RecvFlag = true;
                            list.add(shareflag);
		                    shareflag.reset();
                        } else {
                            share.isServer = true;
                            if (memObj.name != null) {
                                share.Message = "* " + memObj.name + "さんが入室しました *";
                            }
                            share.RecvFlag = true;
                        }
                    }
                    memberList.add(memObj);
                    if (!com) {
                        share.Member.add(memObj);
                        share.NameListFlag = true;
                    }
                } else if (mobj.type.equals("Past log")) {
                    if (mobj.message != null) {
                        PLMessageObj mesObj = mapper.readValue(mobj.message, PLMessageObj.class);
                        if (com) {
                            System.out.println(mesObj.message);
                        } else {
                            share.PastMessage.add(mesObj.message);
                            share.isServerPast.add(mesObj.id == 0);
                            share.Message = mesObj.message;
                        }
                    } else {
                        if (com) {
                            System.out.println("* ようこそ、" + myobj.name + "さん *");
                        } else {
                            share.isServerPast.add(true);
                            share.PastMessage.add("* ようこそ、" + myobj.name + "さん *");
                            share.RecvPastFlag = true;
                        }
                        //System.out.println(share.PastMessage.size());
                    }
                } else if (mobj.type.equals("Delete")) {
                    node = mapper.readValue(str, JsonNode.class);
                    node = node.get("message");
                    int token = Integer.parseInt(node.asText());
                    int num = -1;
                    for (int i = 0; i < memberList.size(); i++) {
                        if (memberList.get(i).token == token) {
                            num = i;
                            break;
                        }
                    }
                    if (num >= 0) {
                        if (com) {
                            System.out.println("* " + memberList.get(num).name + "さんが退室しました *");
                        } else {
                            if (share.flag()) {
                                shareflag.isServer = true;
                                shareflag.Message = "* " + memberList.get(num).name + "さんが退室しました *";
                                shareflag.RecvFlag = true;
                                list.add(shareflag);
		                        shareflag.reset();
                            } else {
                                share.isServer = true;
                                share.Message = "* " + memberList.get(num).name + "さんが退室しました *";
                                share.RecvFlag = true;
                            }
                        }
                        memberList.remove(num);
                        if (!com) {
                            share.Member.remove(num);
                            share.NameListFlag = true;
                        }
                    }
                } else {
                    String name = serchName(Integer.parseInt(String.valueOf(mobj.token)), memberList);
                    if (name != null) {
                        if (com) {
                            System.out.println(name + " : " + mobj.message);
                        } else {
                            if (share.flag()) {
                                if (Integer.parseInt(String.valueOf(mobj.token)) == share.myToken) {
                                    shareflag.isMe = true;
                                }
                                shareflag.Message = name + " : " + mobj.message;
                                shareflag.RecvFlag = true;
                                list.add(shareflag);
		                        shareflag.reset();
                            } else {
                                if (Integer.parseInt(String.valueOf(mobj.token)) == share.myToken) {
                                    share.isMe = true;
                                }
                                share.Message = name + " : " + mobj.message;
                                share.RecvFlag = true;
                            }
                        }
                    }
                }
            }
            if (com) {
                if (soc.getNB(input, ARRAY_SIZE)) {
                    String str = Native.toString(input);
                    if (str.equals("\\q")) {
                        mobj.message = "";
                        mobj.token = (Object) myobj.stoken;
                        mobj.type = "Quit";
                        String s = mapper.writeValueAsString(mobj);
                        if (soc.sendS(sock, s, ARRAY_SIZE) < 0) {
                            System.out.println("Failed to send.");
                        } else {
                            soc.closeSocket(sock);
                            System.out.println("quit");
                            break;
                        }
                    } else {
                        if (str.startsWith("\\\\")) {
                            mobj.message = str.substring(1);
                        } else {
                            mobj.message = str;
                        }
                        mobj.token = (Object) myobj.stoken;
                        mobj.type = "Message";
                        String s = mapper.writeValueAsString(mobj);
                        if (soc.sendS(sock, s, ARRAY_SIZE) < 0) {
                            System.out.println("Failed to send.");
                        }
                    }
                    for (int i = 0; i < ARRAY_SIZE; i++) {
                        input[i] = '\0';
                    }
                }
            } else {
                if (share.SendFlag) {
                    share.SendFlag = false;
                    mobj.message = share.Message;
                    mobj.token = (Object) myobj.stoken;
                    mobj.type = (share.QuitFlag ? "Quit" : "Message");
                    String str = mapper.writeValueAsString(mobj);
                    if (soc.sendS(sock, str, ARRAY_SIZE) < 0) {
                        System.out.println("Failed to send.");
                    } else {
                        if (share.QuitFlag) {
                            soc.closeSocket(sock);
                            break;
                        }
                    }
                    for (int i = 0; i < ARRAY_SIZE; i++) {
                        input[i] = '\0';
                    }
                }
                soc.Sleep_(1000);
            }
		    if (!share.flag() && list.size() > 0) {
		        share.change(list.poll());
		    }
		}
    }
    
    private void connect(int sock) throws Exception {
        if (soc.connectS(sock, IP, PORT) < 0) {
            soc.closeSocket(sock);
            throw new Exception("Failed to connect.");
        }
        soc.nonBlocking(sock);
    }
    
    private void select(int sock, fd_set fds) throws Exception {
        soc.FD_ZERO_(fds);
        soc.FD_SET_(sock, fds);
        if (soc.selectNT(sock, fds) < 0) {
            soc.closeSocket(sock);
            throw new Exception("Failed to select.");
        }
    }
    
    private String serchName(int token, ArrayList<MemberObj> memberList) {
        String rtn = null;
        for (int i = 0; i < memberList.size(); i++) {
            if (memberList.get(i).token == token) {
                rtn = memberList.get(i).name;
                break;
            }
        }
        return rtn;
    }
    
    private int createSocket() throws Exception {
        int sock = soc.TCPSocket();
        if (sock < 0) {
            throw new Exception("Failed to create a socket.");
        }
        return sock;
    }
    
    private String inputData(String mes) {
        String rtn;
        byte[] input = new byte[ARRAY_SIZE];
        if (mes != null) {
            System.out.print(mes);
        }
        while (true) {
            if (soc.getNB(input, ARRAY_SIZE)) {
                rtn = Native.toString(input);
                if (!rtn.equals("")) {
                    break;
                } else {
                    if (mes != null) {
                        System.out.print(mes);
                    }
                }
            }
            soc.Sleep_(10000);
        }
        return rtn;
    }
}

class MessageObj {
    public Object token;
    public String type;
    public String message;
    
    public String getType(Object obj) {
        return obj.getClass().getSimpleName();
    }
}

@JsonIgnoreProperties(ignoreUnknown=true)
class MemberObj {
    int token;
    String name;
    
    public int getToken() {
        return this.token;
    }
    public String getName() {
        return this.name;
    }
    
    public void getToken(int token) {
        this.token = token;
    }
    public void getName(String name) {
        this.name = name;
    }
}

class PLMessageObj {
    public int id;
    public String type;
    public String message;
}

class MemberSTokenObj extends MemberObj {
    public String stoken;
}

class IPv4 {
	public boolean is_ip(String str) {
		String regex = "^((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])$";
		return str.matches(regex);
	}
}