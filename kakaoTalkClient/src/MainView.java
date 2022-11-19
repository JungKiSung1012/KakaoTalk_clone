import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;


// 친구 목록, 채팅 목록 나오는 화면
public class MainView extends JFrame {
	private static final long serialVersionUID = 1L;

	private ImageIcon profile_default = new ImageIcon("./img/profile_default.png");
	private Socket socket; // 연결소켓
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	private String userName = "";
	private String userStatus = "상태메시지";
	private ImageIcon profileImg = profile_default;
	private List<JLabel> FriendLabelList = new ArrayList<JLabel>();
	private List<JLabel> RoomLabelList = new ArrayList<JLabel>();
	private JTextPane friendTextPane;
	
	private JFrame mainView;
	
	
	public MainView(String username, String ip_addr, String port_no) {
		userName = username;
		setBounds(100, 100, 390, 630);
		getContentPane().setLayout(null);
		setTitle("카카오톡");
		setResizable(false);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		mainView = this;
		
		
		FriendPanel friendPanel = new FriendPanel(this);
		ChatroomPanel chatroomPanel = new ChatroomPanel(this);
		new MenuPanel(this, friendPanel, chatroomPanel);
		
		try {
			socket = new Socket(ip_addr, Integer.parseInt(port_no));
			
			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.flush();
			ois = new ObjectInputStream(socket.getInputStream());
			
			// 접속 시 0번으로 내 정보 전송
			ChatMsg obcm = new ChatMsg(userName, "0", userStatus);
			obcm.img = profileImg;
			SendObject(obcm);
			
			ListenNetwork net = new ListenNetwork();
			net.start();

		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
	}
	
	// Server Message를 수신해서 화면에 표시
	class ListenNetwork extends Thread {
		public void run() {
			while (true) {
				try {
					Object obcm = null;
					String msg = null;
					ChatMsg cm;
					try {
						obcm = ois.readObject();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
						break;
					}
					if (obcm == null)
						break;
					if (obcm instanceof ChatMsg) {
						cm = (ChatMsg) obcm;
						msg = String.format("[%s] [%s] %s", cm.getCode(), cm.getId(), cm.getData());
						System.out.println(msg);
					} else
						continue;
					
					switch (cm.getCode()) {
				    // 프로필 재로딩
					case "0":
						// "방이름|상태메시지"
						String data = cm.getData();
						if (data.equals("### 시작 ###")) { // 시작 신호가 오면 List 비우기
							FriendLabelList.removeAll(FriendLabelList);
							// textpane 비우기
							friendTextPane.setText("");
						}
						else if (data.equals("### 끝 ###")) {
							//끝 신호 받으면 전체 그리기
							for (JLabel label : FriendLabelList)
								addComponent(friendTextPane, label);
						}
						else {  // 끝 신호가 오기 전까지 계속 add
							String profile[] = data.split("\\|");
							FriendLabelList.add(new FriendLabel(cm.img, profile[0], profile[1]));
						}
						break;
					
					// 31 : 프로필 요청 결과
					// 
					case "31":
						ImageIcon myProfileImg = cm.img;
						// appendProfile 함수를 만들어야 하나?
						// 일단 append로 붙이라고 했으니까. 틀을 만들어야 되잖아.
						break;
					}
				} catch (IOException e) {
					try {
						ois.close();
						oos.close();
						socket.close();

						break;
					} catch (Exception ee) {
						break;
					} // catch문 끝
				} // 바깥 catch문끝

			}
		}
	}
	

	public void SendObject(Object ob) { // 서버로 메세지를 보내는 메소드
		try {
			oos.writeObject(ob);
		} catch (IOException e) {
			//textArea.append("메세지 송신 에러!!\n");
			//AppendText("SendObject Error");
			System.out.println("\"메세지 송신 에러!!");
		}
	}

	class MenuPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		private ImageIcon main_friend = new ImageIcon("./img/main_friend.png");
		private ImageIcon main_friend_clicked = new ImageIcon("./img/main_friend_clicked.png");
		private ImageIcon main_chatroom = new ImageIcon("./img/main_chatroom.png");
		private ImageIcon main_chatroom_clicked = new ImageIcon("./img/main_chatroom_clicked.png");
		private ButtonGroup btnGroup = new ButtonGroup();
		
		public MenuPanel(JFrame frame, FriendPanel friendPanel, ChatroomPanel chatroomPanel) {
			frame.add(this);
			setBackground(new Color(236, 236, 237));
			setBounds(0, 0, 70, 591);
			setLayout(null);

						
			JToggleButton friendBtn = new JToggleButton(main_friend);
			add(friendBtn);
			friendBtn.setSelected(true);
			btnGroup.add(friendBtn);
			friendBtn.setSelectedIcon(main_friend_clicked);
			friendBtn.setRolloverIcon(main_friend_clicked);
			friendBtn.setBounds(0, 35, 70, 70);
			friendBtn.setBorderPainted(false);
			friendBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
			
			JToggleButton chatroomBtn = new JToggleButton(main_chatroom);
			add(chatroomBtn);
			btnGroup.add(chatroomBtn);
			chatroomBtn.setSelectedIcon(main_chatroom_clicked);
			chatroomBtn.setRolloverIcon(main_chatroom_clicked);
			chatroomBtn.setBounds(0, 103, 70, 70);
			chatroomBtn.setBorderPainted(false);
			chatroomBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
			
			// 친구, 채팅 버튼 토글 이벤트
			friendBtn.addItemListener(new ItemListener() {
				   public void itemStateChanged(ItemEvent ev) {
				      if(ev.getStateChange()==ItemEvent.SELECTED){
			            	friendPanel.setVisible(true);
			            	chatroomPanel.setVisible(false);
				      } else if(ev.getStateChange()==ItemEvent.DESELECTED){
			            	friendPanel.setVisible(false);
			            	chatroomPanel.setVisible(true);
				      }
				   }
				});
			
		}
	}
	
	// JTextPane에 컴포넌트 추가하는 함수
	public void addComponent(JTextPane textPane, Component component) {
		StyledDocument doc = (StyledDocument) textPane.getDocument();
	    Style style = doc.addStyle("StyleName", null);
	    StyleConstants.setComponent(style, component);
	    try {
			doc.insertString(doc.getLength(), "ignored text\n", style);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	// 친구 목록 나오는 패널
	class FriendPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		
		private ImageIcon main_search = new ImageIcon("./img/main_search.png");
		private ImageIcon main_addFriend = new ImageIcon("./img/main_addFriend.png");
		private ImageIcon profile_default = new ImageIcon("./img/profile_default.png");
		private JTextField textField;
		private JScrollPane scrollPane;
		
		public FriendPanel(JFrame frame) {
			frame.add(this);
			setBounds(70, 0, 304, 591);
			setBackground(Color.WHITE);
			setLayout(null);
			
			JPanel topPanel = new JPanel();
			topPanel.setBounds(0, 0, 374, 61);
			add(topPanel);
			topPanel.setBackground(Color.WHITE);
			topPanel.setLayout(null);
			
			textField = new JTextField("친구");
			topPanel.add(textField);
			textField.setBorder(javax.swing.BorderFactory.createEmptyBorder());
			textField.setEditable(false);
			textField.setFont(new Font("맑은 고딕", Font.BOLD, 16));
			textField.setBackground(Color.WHITE);
			textField.setBounds(12, 22, 87, 29);
			textField.setColumns(10);
			
			JButton searchBtn = new JButton(main_search);
			topPanel.add(searchBtn);
			searchBtn.setBounds(202, 10, 41, 41);
			searchBtn.setBorderPainted(false);
			searchBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
			
			
			JButton addFriendBtn = new JButton(main_addFriend);
			topPanel.add(addFriendBtn);
			addFriendBtn.setBounds(251, 10, 41, 41);
			addFriendBtn.setBorderPainted(false);
			addFriendBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
			
			
			scrollPane = new JScrollPane();
			add(scrollPane);
			scrollPane.setBounds(0, 70, 304, 520);
			scrollPane.setBorder(null);
			
			
			friendTextPane = new JTextPane();
			scrollPane.add(friendTextPane);
			friendTextPane.setBackground(Color.WHITE);
			friendTextPane.setEditable(false);
			friendTextPane.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
			scrollPane.setViewportView(friendTextPane);
			
			
			// 스크롤 맨 위로 올리기
			friendTextPane.setSelectionStart(0);
			friendTextPane.setSelectionEnd(0);
			
		}		
	}
	
	
	
	// 채팅 목록 나오는 패널
	class ChatroomPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		
		private ImageIcon main_search = new ImageIcon("./img/main_search.png");
		private ImageIcon main_addChat = new ImageIcon("./img/main_addChat.png");
		private ImageIcon profile_default = new ImageIcon("./img/profile_default.png");
		private JTextField textField;
		private JScrollPane scrollPane;

		public ChatroomPanel(JFrame frame) {
			frame.add(this);
			setBounds(70, 0, 304, 591);
			setBackground(Color.WHITE);
			setLayout(null);
			setVisible(false);
			
			JPanel topPanel = new JPanel();
			topPanel.setBounds(0, 0, 374, 61);
			add(topPanel);
			topPanel.setBackground(Color.WHITE);
			topPanel.setLayout(null);
			
			textField = new JTextField("채팅");
			topPanel.add(textField);
			textField.setBorder(javax.swing.BorderFactory.createEmptyBorder());
			textField.setEditable(false);
			textField.setFont(new Font("맑은 고딕", Font.BOLD, 16));
			textField.setBackground(Color.WHITE);
			textField.setBounds(12, 22, 87, 29);
			textField.setColumns(10);
			
			JButton searchBtn = new JButton(main_search);
			topPanel.add(searchBtn);
			searchBtn.setBounds(202, 10, 41, 41);
			searchBtn.setBorderPainted(false);
			searchBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
			
			JButton addChatBtn = new JButton(main_addChat);
			topPanel.add(addChatBtn);
			addChatBtn.setBounds(251, 10, 41, 41);
			addChatBtn.setBorderPainted(false);
			addChatBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
			
			scrollPane = new JScrollPane();
			add(scrollPane);
			scrollPane.setBounds(0, 70, 304, 520);
			scrollPane.setBorder(null);
			
			JTextPane textPane = new JTextPane();
			scrollPane.add(textPane);
			textPane.setBackground(Color.WHITE);
			textPane.setEditable(false);
			textPane.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
			scrollPane.setViewportView(textPane);

			RoomLabelList.add(new RoomLabel(profile_default, "홍길동", "아버지!", mainView.getLocationOnScreen(), "1"));
			for (int i=0; i<20; i++)
				RoomLabelList.add(new RoomLabel(profile_default, "채팅방 이름", "마지막 대화 내용", mainView.getLocationOnScreen(), "1"));
			for (JLabel label : RoomLabelList)
				addComponent(textPane, label);

			// 스크롤 맨 위로 올리기
			textPane.setSelectionStart(0);
			textPane.setSelectionEnd(0);
		}

	}
}