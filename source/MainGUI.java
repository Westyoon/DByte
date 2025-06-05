import java.awt.EventQueue;
import java.util.Scanner;

import javax.swing.*;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class MainGUI {
	static final String dbID = "testuser";
	static final String dbPW = "testpw";
	static final String dbName = "mindlink";
	static final String header = "jdbc:mysql://localhost:3306/";
	static final String encoding = "useUnicode=true&characterEncoding=UTF-8";
	static final String url = header + dbName + "?" + encoding;

	public static Scanner input = new Scanner(System.in);
	static int userId = -1;
	static int menuNum = -1;

	private JFrame frame;
	TextArea ta = new TextArea();
	JButton btn1 = new JButton();
	JButton btn2 = new JButton();
	JButton btn3 = new JButton();
	JButton btn4 = new JButton();
	JButton btn5 = new JButton();
	JButton btn6 = new JButton();
	JButton btn7 = new JButton();

	private void login() {
		if (userId > 10000) {
			JOptionPane.showMessageDialog(frame, "[SYSTEM] 이미 로그인되어 있습니다. (" + userId + ")", "안내",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		JDialog loginDialog = new JDialog(frame, "로그인", true);
		loginDialog.setSize(320, 200);
		loginDialog.setLocationRelativeTo(frame);
		loginDialog.getContentPane().setLayout(null);

		JLabel idLabel = new JLabel("아이디 (종료 시 0):");
		idLabel.setBounds(20, 20, 120, 25);
		loginDialog.getContentPane().add(idLabel);

		JTextField idField = new JTextField();
		idField.setBounds(150, 20, 130, 25);
		loginDialog.getContentPane().add(idField);

		JButton confirmButton = new JButton("확인");
		confirmButton.setBounds(50, 80, 80, 30);
		loginDialog.getContentPane().add(confirmButton);

		JButton cancelButton = new JButton("취소");
		cancelButton.setBounds(170, 80, 80, 30);
		loginDialog.getContentPane().add(cancelButton);

		confirmButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					String ipt = idField.getText().trim();
					userId = Integer.parseInt(ipt);

					if (userId == 0) {
						JOptionPane.showMessageDialog(loginDialog, "[SYSTEM] 로그인을 종료합니다.", "알림",
								JOptionPane.INFORMATION_MESSAGE);
					} else if (userId > 10000) {
						JOptionPane.showMessageDialog(loginDialog, "[SYSTEM] 로그인 성공! (" + userId + ")", "로그인 성공",
								JOptionPane.INFORMATION_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(loginDialog, "[SYSTEM] 유효하지 않은 ID입니다.", "경고",
								JOptionPane.WARNING_MESSAGE);
						return;
					}

					loginDialog.dispose();

				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(loginDialog, "[SYSTEM] 숫자만 입력해주세요.", "입력 오류",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loginDialog.dispose();
			}
		});

		loginDialog.setVisible(true);
	}
	
	private void updateUI() {
	    frame.getContentPane().removeAll();
	    frame.getContentPane().setLayout(null);

	    ta.setBounds(0, 0, 800, 536);
	    frame.getContentPane().add(ta);

	    if (menuNum != -1) {
	        JButton[] buttons = {btn1, btn2, btn3, btn4, btn5, btn6, btn7};
	        int y = 10;
	        for (int i = 0; i < buttons.length; i++) {
	            buttons[i].setFont(new Font("KoPubWorld돋움체 Medium", Font.PLAIN, 14));
	            buttons[i].setBounds(824, y, 300, 50);
	            // 기존 리스너 제거
	            for (ActionListener al : buttons[i].getActionListeners()) {
	                buttons[i].removeActionListener(al);
	            }
	            frame.getContentPane().add(buttons[i]);
	            y += 70;
	        }
	    }

	    frame.revalidate();
	    frame.repaint();
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainGUI window = new MainGUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public MainGUI() {
		frame = new JFrame();
		frame.setTitle("MindLink");
		frame.setBounds(100, 100, 1200, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu logMenu = new JMenu("시스템");
		logMenu.setFont(new Font("KoPubWorld돋움체 Medium", Font.PLAIN, 14));
		menuBar.add(logMenu);
		
		JMenuItem logInBtn = new JMenuItem("로그인");
		logInBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				login();
			}
		});
		logInBtn.setFont(new Font("KoPubWorld돋움체 Medium", Font.PLAIN, 14));
		logMenu.add(logInBtn);
		
		JMenuItem logOutBtn = new JMenuItem("로그아웃");
		logOutBtn.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        if (userId > 10000 && userId < 30000) {
		            JOptionPane.showMessageDialog(frame,
		                    "[SYSTEM] 로그아웃합니다. (" + userId + ")",
		                    "알림",
		                    JOptionPane.INFORMATION_MESSAGE);
		            userId = -1;
		        } else {
		            JOptionPane.showMessageDialog(frame,
		                    "[SYSTEM] 로그인 상태가 아닙니다.",
		                    "오류",
		                    JOptionPane.ERROR_MESSAGE);
		        }
		    }
		});

		
		logOutBtn.setFont(new Font("KoPubWorld돋움체 Medium", Font.PLAIN, 14));
		logMenu.add(logOutBtn);
		
		JMenuItem sysBtn = new JMenuItem("종료");
		sysBtn.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        JOptionPane.showMessageDialog(
		            frame,
		            "[SYSTEM] Mindlink를 종료합니다.",
		            "알림",
		            JOptionPane.INFORMATION_MESSAGE
		        );
		        System.exit(0);
		    }
		});
		sysBtn.setFont(new Font("KoPubWorld돋움체 Medium", Font.PLAIN, 14));

		logInBtn.setFont(new Font("KoPubWorld돋움체 Medium", Font.PLAIN, 14));
		logMenu.add(sysBtn);
		
		JMenu menu = new JMenu("메뉴");
		menu.setFont(new Font("KoPubWorld돋움체 Medium", Font.PLAIN, 14));
		menuBar.add(menu);
		
		JMenuItem searchBtn = new JMenuItem("검색");
		searchBtn.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        menuNum = 1;
		        
		        for (ActionListener al : btn1.getActionListeners()) btn1.removeActionListener(al);
		        for (ActionListener al : btn2.getActionListeners()) btn2.removeActionListener(al);
		        for (ActionListener al : btn3.getActionListeners()) btn3.removeActionListener(al);
		        for (ActionListener al : btn4.getActionListeners()) btn4.removeActionListener(al);
		        for (ActionListener al : btn5.getActionListeners()) btn5.removeActionListener(al);
		        for (ActionListener al : btn6.getActionListeners()) btn6.removeActionListener(al);
		        for (ActionListener al : btn7.getActionListeners()) btn7.removeActionListener(al);


		        btn1.setLabel("전체 기관 조회");
		        btn1.addActionListener(e1 -> {
		            // TODO: 전체 기관 조회 기능
		        });

		        btn2.setLabel("기관 타입별 조회");
		        btn2.addActionListener(e1 -> {
		            // TODO: 기관 타입별 조회 기능
		        });

		        btn3.setLabel("지역별 기관 조회");
		        btn3.addActionListener(e1 -> {
		            // TODO: 지역별 조회 기능
		        });

		        btn4.setLabel("평균 평점보다 높은 기관 조회");
		        btn4.addActionListener(e1 -> {
		            // TODO: 평균 평점보다 높은 기관 조회
		        });

		        btn5.setLabel("뒤로 가기");
		        btn5.addActionListener(e1 -> {
		            menuNum = -1;
		            updateUI();
		        });

		        btn6.setLabel(""); // 필요 없는 경우 label 제거
		        btn6.setVisible(false);

		        btn7.setLabel("");
		        btn7.setVisible(false);

		        updateUI();
		    }
		});

		searchBtn.setFont(new Font("KoPubWorld돋움체 Medium", Font.PLAIN, 14));
		menu.add(searchBtn);
		
		JMenuItem reviewBtn = new JMenuItem("리뷰");
		reviewBtn.setFont(new Font("KoPubWorld돋움체 Medium", Font.PLAIN, 14));
		reviewBtn.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        menuNum = 2;
		        
		        for (ActionListener al : btn1.getActionListeners()) btn1.removeActionListener(al);
		        for (ActionListener al : btn2.getActionListeners()) btn2.removeActionListener(al);
		        for (ActionListener al : btn3.getActionListeners()) btn3.removeActionListener(al);
		        for (ActionListener al : btn4.getActionListeners()) btn4.removeActionListener(al);
		        for (ActionListener al : btn5.getActionListeners()) btn5.removeActionListener(al);
		        for (ActionListener al : btn6.getActionListeners()) btn6.removeActionListener(al);
		        for (ActionListener al : btn7.getActionListeners()) btn7.removeActionListener(al);


		        btn1.setLabel("리뷰 전체 조회 (기관 평균 평점 및 순위)");
		        btn1.addActionListener(e1 -> {
		            // TODO: 전체 리뷰 조회
		        });

		        btn2.setLabel("리뷰 등록");
		        btn2.addActionListener(e1 -> {
		            // TODO: 리뷰 등록
		        });

		        btn3.setLabel("리뷰 수정");
		        btn3.addActionListener(e1 -> {
		            // TODO: 리뷰 수정
		        });

		        btn4.setLabel("리뷰 삭제");
		        btn4.addActionListener(e1 -> {
		            // TODO: 리뷰 삭제
		        });

		        btn5.setLabel("기관별 리뷰 통계 조회");
		        btn5.addActionListener(e1 -> {
		            // TODO: 기관별 통계
		        });

		        btn6.setLabel("사용자별 리뷰 통계 조회");
		        btn6.setVisible(true);
		        btn6.addActionListener(e1 -> {
		            // TODO: 사용자별 통계
		        });

		        btn7.setLabel("뒤로 가기");
		        btn7.setVisible(true);
		        btn7.addActionListener(e1 -> {
		            menuNum = -1;
		            updateUI();
		        });

		        updateUI();
		    }
		});
		menu.add(reviewBtn);
		
		JMenuItem appointementBtn = new JMenuItem("상담");
		appointementBtn.setFont(new Font("KoPubWorld돋움체 Medium", Font.PLAIN, 14));
		appointementBtn.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        menuNum = 3;
		        
		        for (ActionListener al : btn1.getActionListeners()) btn1.removeActionListener(al);
		        for (ActionListener al : btn2.getActionListeners()) btn2.removeActionListener(al);
		        for (ActionListener al : btn3.getActionListeners()) btn3.removeActionListener(al);
		        for (ActionListener al : btn4.getActionListeners()) btn4.removeActionListener(al);
		        for (ActionListener al : btn5.getActionListeners()) btn5.removeActionListener(al);
		        for (ActionListener al : btn6.getActionListeners()) btn6.removeActionListener(al);
		        for (ActionListener al : btn7.getActionListeners()) btn7.removeActionListener(al);


		        btn1.setLabel("");
		        btn1.setVisible(false);

		        btn2.setLabel("");
		        btn2.setVisible(false);

		        btn3.setLabel("");
		        btn3.setVisible(false);

		        btn4.setLabel("");
		        btn4.setVisible(false);

		        btn5.setLabel("");
		        btn5.setVisible(false);

		        btn6.setLabel(""); // 필요 없는 경우 label 제거
		        btn6.setVisible(false);

		        btn7.setLabel("");
		        btn7.setVisible(false);

		        updateUI();
		    }
		});
		menu.add(appointementBtn);
	
		JMenuItem trackingBtn = new JMenuItem("트래킹");
		trackingBtn.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        menuNum = 4;

		        for (ActionListener al : btn1.getActionListeners()) btn1.removeActionListener(al);
		        for (ActionListener al : btn2.getActionListeners()) btn2.removeActionListener(al);
		        for (ActionListener al : btn3.getActionListeners()) btn3.removeActionListener(al);
		        for (ActionListener al : btn4.getActionListeners()) btn4.removeActionListener(al);
		        for (ActionListener al : btn5.getActionListeners()) btn5.removeActionListener(al);
		        for (ActionListener al : btn6.getActionListeners()) btn6.removeActionListener(al);
		        for (ActionListener al : btn7.getActionListeners()) btn7.removeActionListener(al);

		        btn1.setLabel("트래킹 등록");
		        btn1.setVisible(true);
		        btn1.addActionListener(e1 -> {
		        });

		        btn2.setLabel("전체 트래킹 조회");
		        btn2.setVisible(true);
		        btn2.addActionListener(e1 -> {
		        });

		        btn3.setLabel("트래킹 정보 수정");
		        btn3.setVisible(true);
		        btn3.addActionListener(e1 -> {
		        });

		        btn4.setLabel("트래킹 정보 삭제");
		        btn4.setVisible(true);
		        btn4.addActionListener(e1 -> {
		        });

		        btn5.setLabel("사용자별 평균 트래킹 정보 조회");
		        btn5.setVisible(true);
		        btn5.addActionListener(e1 -> {
		        });

		        btn6.setLabel("뒤로 가기");
		        btn6.setVisible(true);
		        btn6.addActionListener(e1 -> {
		            menuNum = -1;
		            updateUI();
		        });

		        btn7.setLabel("");
		        btn7.setVisible(false);

		        updateUI();
		    }
		});

		
		trackingBtn.setFont(new Font("KoPubWorld돋움체 Medium", Font.PLAIN, 14));
		menu.add(trackingBtn);
		
		frame.getContentPane().setLayout(null);
		
		ta.setBounds(0, 0, 800, 536);
		frame.getContentPane().add(ta);
		frame.setVisible(true);

	}
}
