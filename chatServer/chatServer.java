import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.Structure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonGenerator.Feature;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.regex.*;

import java.net.*;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;
import java.awt.*;

public class chatServer {
    public static void main(String[] args) {
        boolean com = false;
        if (args.length == 1) {
            if (args[0].equals("-c")) {
                com = true;
            }
        }
        new ServerWindow("Chat Server", "Config", com);
    }
}

class ServerWindow implements ActionListener,WindowListener {
    private JFrame frame, frame2;
    private Share share;
    private JTextField PortText;
    private JTextArea netInfo;
    private Timer timer;
    int port;
    private final int ARRAY_SIZE = 2048;
    
    public interface Socket extends Library {
    	// loadLibraryの第一引数はあとで作成するlib***.soの***と一致させる。
        Socket INSTANCE = (Socket) Native.load("socket", Socket.class);
        
        // Cの関数名と一致させる
        boolean getNB(byte[] buff, int buffsize);
        void Sleep_(int us);
    }
    
    ServerWindow(String MainTitle, String SubTitle, boolean com) {
        if (com) {
            byte[] input = new byte[ARRAY_SIZE];
            Socket soc = Socket.INSTANCE;
            do {
                System.out.print("PORT => ");
                while (!soc.getNB(input, ARRAY_SIZE)) {
                    soc.Sleep_(10000);
                }
                String str = Native.toString(input);
                try {
                    port = Integer.parseInt(str);
                } catch (Exception e) {
                    port = -1;
                }
            } while(port < 0 || port > 65535);
            Server server = new Server(port, true);
            try {
                server.runServer();
            } catch (Exception e) {
                System.out.println(e);
            }
        } else {
            share = new Share();
            share.quit = false;
            share.closed = false;
            timer = new Timer(1 , this);
        
            SubWindow(SubTitle);
            MainWindow(MainTitle);
 		    timer.setActionCommand("Timer");
            timer.start();
        }
    }
    
    public void MainWindow(String title) {
        frame = new JFrame();
 		frame.setTitle(title);
 		frame.setBounds( 10, 10, 640, 480);
 		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		JPanel p1 = new JPanel();
 		p1.setLayout(new BoxLayout(p1, BoxLayout.LINE_AXIS));
 		netInfo = new JTextArea();
 		netInfo.setEditable(false);
 		p1.add(netInfo);
 		JButton btn = new JButton("Quit");
 		btn.addActionListener(this);
 		btn.setActionCommand("Quit");
 		frame.addWindowListener(this);
 		frame.getContentPane().add(p1, BorderLayout.CENTER);
 		frame.getContentPane().add(btn, BorderLayout.SOUTH);
    	frame.setVisible(false);
    }
    
    public void SubWindow(String title) {
        frame2 = new JFrame();
 		frame2.setTitle(title);
 		frame2.setBounds( 10, 10, 350, 200);
 		frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		JLabel label = new JLabel("Port : ");
 		PortText = new JTextField("50000", 5);
 		PortText.addActionListener(this);
 		PortText.setActionCommand("Enter");
 		JPanel p1 = new JPanel();
 		p1.setLayout(new FlowLayout(FlowLayout.LEFT));
        p1.add(label);
        p1.add(PortText);
 		JButton btn = new JButton("Enter");
 		btn.addActionListener(this);
 		btn.setActionCommand("Enter");
 		frame2.addWindowListener(this);
 		frame2.getContentPane().add(p1, BorderLayout.CENTER);
 		frame2.getContentPane().add(btn, BorderLayout.SOUTH);
    	frame2.setVisible(true);
    }
    
    public String makeNetInfo() {
        StringBuilder sb = new StringBuilder();
        Net net = new Net();
        ArrayList<String> info = new ArrayList<>();
        sb.append("port: \n\t");
        sb.append(String.valueOf(port));
        sb.append("\n");
        try {
            info = net.netInfo();
        } catch (Exception e) {
            System.out.println(e);
        }
        sb.append("PC name: \n\t");
        sb.append(info.get(0));
        sb.append("\n");
        if (info.size() > 1) {
            sb.append("IPv4 address: \n");
            for (int i = 1; i < info.size(); i++) {
                sb.append("\t");
                sb.append(info.get(i));
                sb.append("\n");
            }
        }
        return sb.toString();
    }
    
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals("Enter")) {
		    try {
		        port = Integer.parseInt(PortText.getText());
		    } catch (NumberFormatException ex) {
		        port = -1;
		    }
		    try {
		        if (port >= 0 && port <= 65535) {
			        frame2.dispose();
			        netInfo.setText(makeNetInfo());
			        frame.setVisible(true);
        
                    Server server = new Server(port, false);
                    Thread t = new Thread(server);
                    t.start();
		        }
		    } catch (Exception ex) {
                System.out.println(ex);
            }
        } else if (cmd.equals("Quit")) {
			frame.dispose();
        } else if (cmd.equals("Timer")) {
			if (share.closed) {
			    frame.dispose();
			}
        }
    }
	
	public void windowClosing(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
	    if (e.getWindow().getName().equals(frame.getName())) {
            share.quit = true;
            try {
                while (!share.closed) {
                    Thread.sleep(10);
                 }
            } catch (Exception ex) {
		        System.out.println(ex);
            }
	        System.exit(0);
        } else {
            frame.setVisible(true);
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

class Net {
    public ArrayList<String> netInfo() throws Exception {
        ArrayList<String> info = new ArrayList<>();
        if (PlatformUtils.isMac()) {
            String lo = InetAddress.getLoopbackAddress().getHostAddress();
            InetAddress inet  = InetAddress.getLocalHost();
            info.add(inet.getHostName());
            String com = "ifconfig | grep inet | grep netmask | awk -F'[ ]' '{ print $2 }'";
            ProcessBuilder p = new ProcessBuilder("sh", "-c", com);
            p.redirectErrorStream(true);
            Process process = p.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.defaultCharset()));
            String line;
            while ((line = r.readLine()) != null) {
                info.add(line + (line.equals(lo) ? " (lo)" : ""));
            }
            int result = process.waitFor();
        } else if (PlatformUtils.isLinux()) {
            String lo = InetAddress.getLoopbackAddress().getHostAddress();
            InetAddress inet  = InetAddress.getLocalHost();
            info.add(inet.getHostName());
            String com = "ip a | grep inet | egrep \"([0-9]+\\.){3}[0-9]+\" | awk '{ gsub(/^[ \t]+/,\"\"); print $0 }' | awk -F'[ ]' '{ print $2 }' | awk -F'[/]' '{ print $1 }'";
            ProcessBuilder p = new ProcessBuilder("sh", "-c", com);
            p.redirectErrorStream(true);
            Process process = p.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.defaultCharset()));
            String line;
            while ((line = r.readLine()) != null) {
                info.add(line + (line.equals(lo) ? " (lo)" : ""));
            }
            int result = process.waitFor();
        } else if (PlatformUtils.isWindows()) {
            InetAddress inet  = InetAddress.getLocalHost();
            info.add(inet.getHostName());
            info.add(InetAddress.getLoopbackAddress().getHostAddress() + " (lo)");
            String com = "ipconfig | findstr \"IP\" | findstr \"[0-9][0-9]*\\.[0-9][0-9]*\\.[0-9][0-9]*\\.[0-9][0-9]*\"";
            ProcessBuilder p = new ProcessBuilder("cmd", "/c", com);
            p.redirectErrorStream(true);
            Process process = p.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.defaultCharset()));
            String line;
            Pattern pattern = Pattern.compile("^((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])$");
            while ((line = r.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    info.add(matcher.group());
                }
            }
            int result = process.waitFor();
        } else {
            InetAddress inet  = InetAddress.getLocalHost();
            info.add(inet.getHostName());
            InetAddress []adrs = InetAddress.getAllByName(info.get(0));
            for(int i = 0; i < adrs.length; i++) {
                String str = adrs[i].getHostAddress();
                if (is_ip(str)) {
                    info.add(str + (adrs[i].isLoopbackAddress() ? " (lo)" : ""));
                }
            }
        }
        return info;
    }
    
	public boolean is_ip(String str) {
		String regex = "^((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])$";
		return str.matches(regex);
	}
}

class Share {
    public static boolean quit;
    public static boolean closed;
}

class Server implements Runnable {
    private int PORT;
    private ArrayList<Integer> socklist;
    private ArrayList<String> acList;
    private ArrayList<MemberSTokenObj> memberList;
    private Socket soc;
    Buffer<PLMessageObj> pastLog;
    MemberListObj list;
    private final int ARRAY_SIZE = 2048;
    private Share share;
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
    
    Server(int port,boolean com) {
        socklist = new ArrayList<>();
        acList = new ArrayList<>();
        this.com = com;
        PORT = port;
        soc = Socket.INSTANCE;
        memberList = new ArrayList<>();
        list = new MemberListObj();
        list.list = new ArrayList<>();
        share = new Share();
    }
    
    public void run() {
        try {
            runServer();
        } catch (Exception e) {
            System.out.println(e);
        }
        share.closed=true;
    }
    
    public void runServer() throws Exception {
        int sock = createSocket();
        fd_set fds = new fd_set();
        pastLog = new Buffer<>(100);
        byte[] buf = new byte[ARRAY_SIZE];
        byte[] input = new byte[ARRAY_SIZE];
        
        soc.getflag();
        String name;
        while (true) {
            select(sock, fds);
            if (soc.FD_ISSET_(sock, fds)) {
                accept(sock);
            }
            for (int i = 0; i < socklist.size(); i++) {
                int recv = soc.recvS(socklist.get(i), buf, ARRAY_SIZE);
                if (recv < 0) {
                    if (!soc.notRecv() && !soc.getflag()) {
                        System.out.println("Failed to recieve.");
                    }
                } else {
                    String str = new String(buf, "UTF-8");
                    ObjectMapper mapper = new ObjectMapper();
                    MessageObj mobj = new MessageObj();
                    MemberSTokenObj mstobj = new MemberSTokenObj();
                    try {
                        mobj = mapper.readValue(str, MessageObj.class);
                    } catch (Exception excep) {
                        continue;
                    }
                    if (mobj.type.equals("Quit")) {
                        mobj.type = "Delete";
                        int token = mstobj.getToken(String.valueOf(mobj.token), memberList);
                        mobj.message = String.valueOf(token);
                        mobj.token = (Object) 0;
                        str = mapper.writeValueAsString(mobj);
                        for (int j = 0; j < socklist.size(); j++) {
                            if(i == j) continue;
                            soc.sendS(socklist.get(j), str, ARRAY_SIZE);
                        }
                        for(int j = 0; j < list.list.size(); j++) {
                            if(list.list.get(j).token == token) {
                                PLMessageObj pastLogObj = new PLMessageObj();
                                pastLogObj.message = "* " + list.list.get(j).name + "さんが退室しました *";
                                pastLogObj.id = 0;
                                pastLogObj.type = "Quit";
                                pastLog.add(pastLogObj);
                                list.list.remove(j);
                                break;
                            }
                        }
                        for(int j = 0; j < memberList.size(); j++) {
                            if(memberList.get(j).token == token) {
                                memberList.remove(j);
                                break;
                            }
                        }
                        String pat = "^token: " + token + ", IP: .+$";
                        Pattern pattern = Pattern.compile(pat);
                        Pattern p = Pattern.compile("IP:.+$");
                        for (int j = 0; j < acList.size(); j++) {
                            Matcher matcher = pattern.matcher(acList.get(j));
                            if (matcher.find()) {
                                Matcher m = p.matcher(acList.get(j));
                                if (m.find()) {
                                    //System.out.println("Quit. (" + m.group() + ")");
                                }
                                acList.remove(j);
                                break;
                            }
                        }
                        soc.closeSocket(socklist.get(i));
                        socklist.remove(i);
                    } else {
                        if (mobj.getType(mobj.token).equals("String")) {
                            int token = mstobj.getToken(String.valueOf(mobj.token), memberList);
                            if (token < 0) {
                                System.out.println("Token is wrong.");
                            } else {
                                mobj.token = (Object) token;
                                str = mapper.writeValueAsString(mobj);
                                for (int j = 0; j < socklist.size(); j++) {
                                    soc.sendS(socklist.get(j), str, ARRAY_SIZE);
                                }
                                for (int j = 0; j < list.list.size(); j++) {
                                    if(list.list.get(j).token == token) {
                                        PLMessageObj pastLogObj = new PLMessageObj();
                                        pastLogObj.message = list.list.get(j).name + " : " + mobj.message;
                                        pastLogObj.id = token;
                                        pastLogObj.type = "Message";
                                        pastLog.add(pastLogObj);
                                    }
                                }
                            }
                        } else {
                            System.out.println("Format is wrong.");
                        }
                    }
                }
            }
            if (com) {
                if (soc.getNB(input, ARRAY_SIZE)) {
                    String str = Native.toString(input);
                    if (str.equals("quit")) {
                        serverClose();
                        break;
                    } else if (str.equals("IP")) {
                        Net net = new Net();
                        ArrayList<String> list = net.netInfo();
                        for (int i = 0; i < list.size(); i++) {
                            System.out.println(list.get(i));
                        } 
                    } else if (str.equals("Port")) {
                        System.out.println(PORT);
                    } else if (str.equals("Connect")) {
                        for (int i = 0; i < acList.size(); i++) {
                            System.out.println(acList.get(i));
                        } 
                    } else if (str.equals("List")) {
                        for (int i = 0; i < memberList.size(); i++) {
                            String s = "name: " + memberList.get(i).name + ", token: " + memberList.get(i).token + ", stoken: " + memberList.get(i).stoken;
                            System.out.println(s);
                        } 
                    }
                    for (int i = 0; i < ARRAY_SIZE; i++) {
                        input[i] = '\0';
                    }
                }
            } else {
                if (share.quit) {
                    serverClose();
                    break;
                }
            }
            soc.Sleep_(10000);
        }
    }
    
    private void serverClose() throws JsonProcessingException {
        MessageObj mes = new MessageObj();
        ObjectMapper mapper = new ObjectMapper();
        mes.type = "Down";
        mes.message = "";
        mes.token = (Object) 0;
        String str = mapper.writeValueAsString(mes);
        for (int i = 0; i < socklist.size(); i++) {
            if (soc.sendS(socklist.get(i), str, ARRAY_SIZE) < 0) {
                System.out.println("Failed to send.");
            }
        }
        while (socklist.size() != 0) {
            soc.closeSocket(socklist.get(0));
            socklist.remove(0);
        }
    }
    
    private void accept(int sock) throws JsonProcessingException {
        MemberSTokenObj mstobj = new MemberSTokenObj();
        MemberObj memobj = new MemberObj();
        byte[] buf = new byte[ARRAY_SIZE];
        IntByReference acPort = new IntByReference();
        byte[] acIP = new byte[16];
        int acceptSock = soc.acceptInfo(sock, acPort, acIP);
        String acIPStr = Native.toString(acIP);
        for(int i = 0; i < 16; i++) {
            acIP[i] = '\0';
        }
        int acPortVal = acPort.getValue();
        MessageObj mes = new MessageObj();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(Feature.ESCAPE_NON_ASCII, true);
        String str;
        if (acceptSock < 0) {
            System.out.println("Failed to accept.");
        } else {
            soc.nonBlocking(acceptSock);
            while (true) {
                int recv = soc.recvS(acceptSock, buf, ARRAY_SIZE);
                if (recv < 0) {
                    if (!soc.notRecv() && !soc.getflag()) {
                        System.out.println("Failed to recieve.");
                    }
                } else {
                    try {
                        str = new String(buf, "UTF-8");
                        mes = mapper.readValue(str, MessageObj.class);
                    } catch (Exception excep) {
                        continue;
                    }
                    if (mes.type.equals("Send Name")) {
                        break;
                    }
                }
                soc.Sleep_(10000);
            }
            //System.out.println("Accept. (IP: " + acIPStr + ", port: " + acPortVal + ", Name: " + mes.message + ")");
            mstobj.name = mes.message;
            mstobj.stoken = mstobj.makeSToken(memberList);
            mes.message = mstobj.stoken;
            mes.type = "Send StrToken";
            mes.token = (Object) 0;
            str = mapper.writeValueAsString(mes);
            if (soc.sendS(acceptSock, str, ARRAY_SIZE) < 0) {
                System.out.println("Failed to send.");
            }
            soc.Sleep_(30000);
            mstobj.token = mstobj.makeToken(memberList);
            acList.add("token: " + mstobj.token + ", IP: " + acIPStr + ", port: " + acPortVal + ", Name: " + mstobj.name);
            mes.message = String.valueOf(mstobj.token);
            mes.type = "Send IntToken";
            mes.token = (Object) 0;
            str = mapper.writeValueAsString(mes);
            if (soc.sendS(acceptSock, str, ARRAY_SIZE) < 0) {
                System.out.println("Failed to send.");
            }
            memberList.add(mstobj);
            MemberObj memberobj = new MemberObj();
            memberobj.token = mstobj.token;
            memberobj.name = mstobj.name;
            list.list.add(memberobj);
            soc.Sleep_(30000);
            String strlist = mapper.writeValueAsString(list);
            mes.type = "Send List";
            mes.token = (Object) 0;
            mes.message = strlist;
            str = mapper.writeValueAsString(mes);
            if (soc.sendS(acceptSock, str, ARRAY_SIZE) < 0) {
                System.out.println("Failed to send.");
            }
            soc.Sleep_(30000);
            if (soc.sendS(acceptSock, str, ARRAY_SIZE) < 0) {
                System.out.println("Failed to send.");
            }
            memobj.token = mstobj.token;
            memobj.name = mstobj.name;
            String strmem = mapper.writeValueAsString(memobj);
            mes.type = "Add";
            mes.token = (Object) 0;
            mes.message = strmem;
            str = mapper.writeValueAsString(mes);
            for (int i = 0; i < socklist.size(); i++) {
                if (soc.sendS(socklist.get(i), str, ARRAY_SIZE) < 0) {
                    System.out.println("Failed to send.");
                }
            }
            soc.Sleep_(10000);
            mes.type = "Past log";
            mes.token = (Object) 0;
            try {
                for (int i = 0; i < pastLog.size(); i++) {
                    String strpast = mapper.writeValueAsString(pastLog.get(i));
                    mes.message = strpast;
                    str = mapper.writeValueAsString(mes);
                    if (soc.sendS(acceptSock, str, ARRAY_SIZE) < 0) {
                        System.out.println("Failed to send.");
                    }
                }
            } catch (Exception ex) {
                
            }
            mes.message = null;
            str = mapper.writeValueAsString(mes);
            if (soc.sendS(acceptSock, str, ARRAY_SIZE) < 0) {
                System.out.println("Failed to send.");
            }
            socklist.add(acceptSock);
            PLMessageObj pastLogObj = new PLMessageObj();
            pastLogObj.message = "* " + memobj.name + "さんが入室しました *";
            pastLogObj.id = 0;
            pastLogObj.type = "Add";
            pastLog.add(pastLogObj);
        }
    }
    
    private void select(int sock, fd_set fds) throws Exception {
        soc.FD_ZERO_(fds);
        soc.FD_SET_(sock, fds);
        if (soc.selectNT(sock, fds) < 0) {
            soc.closeSocket(sock);
            throw new Exception("Failed to select.");
        }
    }
    
    private int createSocket() throws Exception {
        int sock = soc.TCPSocket();
        if (sock < 0) {
            throw new Exception("Failed to create a socket.");
        }
        if (soc.bindNIP(sock, PORT) < 0) {
            soc.closeSocket(sock);
            throw new Exception("Failed to bind.");
        }
        if (soc.listenNB(sock) < 0) {
            soc.closeSocket(sock);
            throw new Exception("Failed to listen.");
        }
        if (com) {
            System.out.println("Server is up.");
        }
        return sock;
    }
}

class PLMessageObj {
    public int id;
    public String type;
    public String message;
}

class MessageObj {
    public Object token;
    public String type;
    public String message;
    
    public String getType(Object obj) {
        return obj.getClass().getSimpleName();
    }
}

class MemberObj {
    public int token;
    public String name;
}

class MemberListObj {
    public ArrayList<MemberObj> list;
}

class MemberSTokenObj extends MemberObj {
    public String stoken;
    private Rand rand;
    
    public interface Rand extends Library {
    	// loadLibraryの第一引数はあとで作成するlib***.soの***と一致させる。
        Rand INSTANCE = (Rand) Native.load("socket", Rand.class);
        
        void initRand();
        double random();
    }
    
    MemberSTokenObj() {
        rand = Rand.INSTANCE;
        rand.initRand();
    }
    
    public String makeSToken(ArrayList<MemberSTokenObj> list) {
        boolean f;
        String str;
        do {
            f = false;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                double rnd = rand.random();
                double num = rnd * 52;
                if (num < 26) {
                    sb.append((char) ('a' + (char) num));
                } else {
                    sb.append((char) ('A' + (char) (num - 26)));
                }
            }
            str = sb.toString();
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).equals(str)) {
                    f = true;
                    break;
                }
            }
        } while(f);
        return str;
    }
    
    public int makeToken(ArrayList<MemberSTokenObj> list) {
        int rtn = -1;
        for (int j = 1; j <= list.size() + 1; j++) {
            boolean f = false;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).token == j) {
                    f = true;
                }
            }
            if (f == false) {
                rtn = j;
                break;
            }
        }
        return rtn;
    }
    
    public int getToken(String str, ArrayList<MemberSTokenObj> list) {
        int rtn = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).stoken.equals(str)) {
                rtn = list.get(i).token;
                break;
            }
        }
        return rtn;
    }
}

class Buffer<T> {
	private List<T> queue;
	private int length = 0;
	private int maxLen;
	
	Buffer(int len) {
		maxLen = len;
	    queue = new ArrayList<>();
	}
	
	public void add(T t) {
		if ((length >= maxLen) && (maxLen > 0)) {
			queue.remove(0);
			queue.add(t);
		} else {
			queue.add(t);
			length++;
		}
	}
	
	public T poll() {
		length--;
		T t = queue.get(0);
		queue.remove(0);
		return t;
	}
	
	public T remove() {
		return queue.remove(0);
	}
	
	public T peek() {
		return queue.get(0);
	}
	
	public T get(int i) {
		return queue.get(i);
	}
	
	public int size() {
		return length;
	}
}