import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import console.Tracking;

public class MainGUI extends JFrame {
	// DB 연동 정보
	static final String dbID = "testuser";
	static final String dbPW = "testpw";
	static final String dbName = "mindlink";
	static final String header = "jdbc:mysql://localhost:3306/";
	static final String encoding = "useUnicode=true&characterEncoding=UTF-8";
	static final String url = header + dbName + "?" + encoding;

	// 상태 변수
	public static Scanner input = new Scanner(System.in);
	static int userId = -1;
	static int menuNum = -1;

	// GUI 컴포넌트
	private JPanel CP;
	TextArea ta = new TextArea();
	JButton[] buttons = new JButton[7];
	JFrame frame;
	JButton btn1 = new JButton();
	JButton btn2 = new JButton();
	JButton btn3 = new JButton();
	JButton btn4 = new JButton();
	JButton btn5 = new JButton();
	JButton btn6 = new JButton();
	JButton btn7 = new JButton();
	JLabel titleLabel = new JLabel();
	JTable table;
	JScrollPane resultScroll;
	DefaultTableModel tableModel;
	JScrollPane typeScroll = new JScrollPane();
	private JScrollPane districtScroll;
	private JList<String> districtList;
	

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				MainGUI window = new MainGUI();
				window.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public MainGUI() {
		setTitle("Mindlink 시스템");
		setBounds(100, 100, 1100, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame = this;
		CP = new JPanel();
		CP.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(CP);
		CP.setLayout(null);
		
		tableModel = new DefaultTableModel(new Object[][] {}, new String[] {});
		table = new JTable(tableModel);
		resultScroll = new JScrollPane(table);
		resultScroll.setBounds(0, 100, 800, 400);
		CP.add(resultScroll);

		for (int i = 0; i < buttons.length; i++) {
			buttons[i] = new JButton();
			buttons[i].setFont(new Font("KoPubWorld돋움체 Medium", Font.PLAIN, 14));
			CP.add(buttons[i]);
		}

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		setupMenus(menuBar);
		
		districtList = new JList<>(new String[] {"(select)", "마포구", "서대문구", "은평구", "종로구"});
		districtScroll = new JScrollPane(districtList);
		districtScroll.setBounds(820, 100, 150, 100);
		districtScroll.setVisible(false);
		CP.add(districtScroll);

		// 이벤트 리스너 (한 번만 추가)
		districtList.addListSelectionListener(ev -> {
			if (!ev.getValueIsAdjusting()) {
				String selected = districtList.getSelectedValue();
				if (selected != null && !selected.equals("(select)")) {
					tableModel.setRowCount(0);
					Object[][] data = SearchGUI.getInstitutionByDistrict(selected);
					for (Object[] row : data) {
						tableModel.addRow(row);
					}
					districtScroll.setVisible(false); // 선택 후 숨기기
				}
			}
		});

		ta.setBounds(0, 100, 800, 400);
		CP.add(ta);
		
		CP.add(titleLabel);
		CP.setVisible(true);
		
		updateUI();
	}

	private void setupMenus(JMenuBar menuBar) {
		JMenu sysMenu = new JMenu("시스템");
		sysMenu.setFont(new Font("KoPubWorld돋움체 Medium", Font.PLAIN, 14));

		JMenuItem login = new JMenuItem("로그인");
		login.setFont(sysMenu.getFont());
		login.addActionListener(e -> login());
		sysMenu.add(login);

		JMenuItem logout = new JMenuItem("로그아웃");
		logout.setFont(sysMenu.getFont());
		logout.addActionListener(e -> logout());
		sysMenu.add(logout);

		JMenuItem exit = new JMenuItem("종료");
		exit.setFont(sysMenu.getFont());
		exit.addActionListener(e -> {
			JOptionPane.showMessageDialog(frame, "[SYSTEM] Mindlink를 종료합니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
			System.exit(0);
		});
		sysMenu.add(exit);
		menuBar.add(sysMenu);
		
		typeScroll.setBounds(0, 100, 100, 25);
		typeScroll.setVisible(false);
		CP.add(typeScroll);

		JMenu menu = new JMenu("메뉴");
		menu.setFont(sysMenu.getFont());
		menuBar.add(menu);

		JMenuItem search = new JMenuItem("검색");
		search.setFont(sysMenu.getFont());
		search.addActionListener(e -> showSearchMenu());
		menu.add(search);
		
		JMenuItem review = new JMenuItem("리뷰");
		review.setFont(sysMenu.getFont());
		review.addActionListener(e -> showReviewMenu());
		menu.add(review);
		
		JMenuItem appointment = new JMenuItem("상담");
		appointment.setFont(sysMenu.getFont());
		appointment.addActionListener(e -> showAppointmentMenu());
		menu.add(appointment);
		
		JMenuItem tracking = new JMenuItem("트래킹");
		tracking.setFont(sysMenu.getFont());
		tracking.addActionListener(e -> showTrackingMenu());
		menu.add(tracking);
	}

	private void login() {
		if (userId > 10000) {
			JOptionPane.showMessageDialog(frame, "이미 로그인되어 있습니다. (" + userId + ")", "알림", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		String inputId = JOptionPane.showInputDialog(frame, "아이디를 입력하세요 (0 입력 시 종료)");
		if (inputId == null) return;

		try {
			int id = Integer.parseInt(inputId);
			if (id == 0) {
				JOptionPane.showMessageDialog(frame, "로그인을 종료합니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
			} else if (id > 10000) {
				userId = id;
				JOptionPane.showMessageDialog(frame, "로그인 성공! (" + userId + ")", "로그인", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(frame, "유효하지 않은 ID입니다.", "오류", JOptionPane.WARNING_MESSAGE);
			}
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(frame, "숫자만 입력해주세요.", "입력 오류", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void logout() {
		if (userId > 10000) {
			JOptionPane.showMessageDialog(frame, "로그아웃합니다. (" + userId + ")", "알림", JOptionPane.INFORMATION_MESSAGE);
			userId = -1;
		} else {
			JOptionPane.showMessageDialog(frame, "로그인 상태가 아닙니다.", "오류", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void clearButtonListeners() {
	    JButton[] buttons = {btn1, btn2, btn3, btn4, btn5, btn6, btn7};
	    for (JButton btn : buttons) {
	        for (ActionListener al : btn.getActionListeners()) {
	            btn.removeActionListener(al);
	        }
	        btn.setVisible(true); // 필요 시 false로 개별 설정
	    }
	}
	private void showSearchMenu() {
		clearUI();
		showUI();
		menuNum = 1;
		String[] labels = { "전체 기관 조회", "기관 타입별 조회", "지역별 기관 조회", "평균 평점 이상 기관 조회", "뒤로 가기", "", "" };
		titleLabel.setText("기관 검색");
		clearButtonListeners();
		for (int i = 0; i < buttons.length; i++) {
			buttons[i].setVisible(true);
			buttons[i].setText(labels[i]);
			for (ActionListener al : buttons[i].getActionListeners())
				buttons[i].removeActionListener(al);
		}
		buttons[0].addActionListener(e -> {
			tableModel.setRowCount(0);
			tableModel.setColumnIdentifiers(new String[] { "기관명", "주소", "전화번호" });
			Object[][] data = SearchGUI.getAllInstitution();
			for (Object[] row : data) {
				tableModel.addRow(row);
			}
		});
		buttons[1].addActionListener(e -> {
			String[] types = { "공공", "민간" };
			String selected = (String) JOptionPane.showInputDialog(frame, "기관 타입을 선택하세요:", "기관 타입별 조회",
					JOptionPane.QUESTION_MESSAGE, null, types, types[0]);

			if (selected != null) {
				tableModel.setRowCount(0);
				tableModel.setColumnIdentifiers(new String[] { "기관명", "주소", "전화번호" });
				Object[][] data = SearchGUI.getInstitutionByType(selected);
				for (Object[] row : data) {
					tableModel.addRow(row);
				}
			}
		});
		buttons[2].addActionListener(e -> {
			String[] districts = { "마포구", "서대문구", "은평구", "종로구" };
			String selected = (String) JOptionPane.showInputDialog(frame, "지역을 선택하세요:", "지역별 기관 조회",
					JOptionPane.QUESTION_MESSAGE, null, districts, districts[0]);

			if (selected != null) {
				tableModel.setRowCount(0);
				tableModel.setColumnIdentifiers(new String[] { "기관명", "주소", "전화번호" });
				Object[][] data = SearchGUI.getInstitutionByDistrict(selected);
				for (Object[] row : data) {
					tableModel.addRow(row);
				}
			}
		});

		buttons[3].addActionListener(e -> {
			typeScroll.setVisible(false);
			districtScroll.setVisible(false);

			tableModel.setColumnIdentifiers(new String[] { "기관명", "주소", "전화번호", "평점" });
			tableModel.setRowCount(0);
			table.getColumnModel().getColumn(0).setPreferredWidth(100);
			table.getColumnModel().getColumn(1).setPreferredWidth(250);
			table.getColumnModel().getColumn(2).setPreferredWidth(150);
			table.getColumnModel().getColumn(3).setPreferredWidth(50);
			Object[][] data = SearchGUI.getInstitutionByRating();
			for (Object[] row : data) {
				tableModel.addRow(row);
			}
		});
		buttons[4].addActionListener(e -> {
			menuNum = -1;
			clearUI();
		});
		buttons[5].setVisible(false);
		buttons[6].setVisible(false);
		updateUI();
	}

	private void showReviewMenu() {
		clearUI();
		showUI();
		menuNum = 2;
		titleLabel.setText("리뷰");
		clearButtonListeners();
		String[] labels = { "리뷰 전체 조회", "리뷰 등록", "리뷰 수정", "리뷰 삭제", "기관별 리뷰 통계 조회", "사용자별 리뷰 통계 조회", "뒤로 가기" };
		for (int i = 0; i < buttons.length; i++) {
			buttons[i].setVisible(true);
			buttons[i].setText(labels[i]);
			for (ActionListener al : buttons[i].getActionListeners())
				buttons[i].removeActionListener(al);
		}
		buttons[0].addActionListener(e -> {
			clearTable();
			tableModel.setColumnIdentifiers(new String[] { "리뷰ID", "유저ID", "기관ID", "내용", "평점", "작성일", "기관평균", "기관순위" });
			Object[][] data = ReviewGUI.getAllReviews();
			for (Object[] row : data) {
				tableModel.addRow(row);
			}
		});
		buttons[1].addActionListener(e -> {
			clearTable();
			tableModel.setColumnIdentifiers(new String[] { "리뷰ID", "기관ID", "평점", "내용" });
			updateTable(ReviewGUI.getUserReview(userId));
			if (userId == -1) {
				JOptionPane.showMessageDialog(frame, "로그인 상태가 아닙니다.", "오류", JOptionPane.ERROR_MESSAGE);
			} else {
				JTextField institutionIdField = new JTextField();
				JTextField contentField = new JTextField();
				JTextField ratingField = new JTextField();
				Object[] message = { "기관ID:", institutionIdField, "리뷰 내용:", contentField, "평점(1~5):", ratingField };
				int option = JOptionPane.showConfirmDialog(this, message, "리뷰 등록", JOptionPane.OK_CANCEL_OPTION);
				if (option == JOptionPane.OK_OPTION) {
					try {
						int institutionId = Integer.parseInt(institutionIdField.getText());
						String content = contentField.getText();
						int rating = Integer.parseInt(ratingField.getText());
						boolean result = ReviewGUI.createReview(userId, institutionId, content, rating);
						JOptionPane.showMessageDialog(this, result ? "등록 완료!" : "등록 실패!");
						clearTable();
						tableModel.setColumnIdentifiers(new String[] { "리뷰ID", "기관ID", "평점", "내용" });
						Object[][] data = ReviewGUI.getUserReview(userId);
						updateTable(data);
						;
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(this, "입력 오류: " + ex.getMessage());
					}
				}
			}
		});
		buttons[2].addActionListener(e -> {
			clearTable();
			if (userId == -1) {
				JOptionPane.showMessageDialog(frame, "로그인 상태가 아닙니다.", "오류", JOptionPane.ERROR_MESSAGE);
			} else {
				JTextField reviewIdField = new JTextField();
				JTextField contentField = new JTextField();
				JTextField ratingField = new JTextField();
				tableModel.setColumnIdentifiers(new String[] { "리뷰ID", "기관ID", "평점", "내용" });
				Object[][] data = ReviewGUI.getUserReview(userId);
				updateTable(data);
				Object[] message = { "리뷰ID:", reviewIdField, "새 내용:", contentField, "새 평점(1~5):", ratingField };
				int option = JOptionPane.showConfirmDialog(this, message, "리뷰 수정", JOptionPane.OK_CANCEL_OPTION);
				if (option == JOptionPane.OK_OPTION) {
					try {
						int reviewId = Integer.parseInt((reviewIdField.getText()));
						String content = contentField.getText();
						int rating = Integer.parseInt(ratingField.getText());
						boolean result = ReviewGUI.updateReview(reviewId, content, rating);
						JOptionPane.showMessageDialog(this, result ? "수정 완료!" : "수정 실패!");
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(this, "입력 오류: " + ex.getMessage());
					}
				}
				clearTable();
				tableModel.setColumnIdentifiers(new String[] { "리뷰ID", "기관ID", "평점", "내용" });
				updateTable(ReviewGUI.getUserReview(userId));
			}
		});
		buttons[3].addActionListener(e -> {
			clearTable();
			if (userId == -1) {
				JOptionPane.showMessageDialog(frame, "로그인 상태가 아닙니다.", "오류", JOptionPane.ERROR_MESSAGE);
			} else {
				tableModel.setColumnIdentifiers(new String[] { "리뷰ID", "기관ID", "평점", "내용" });
				Object[][] data = ReviewGUI.getUserReview(userId);
				updateTable(data);
				JTextField ratingField = new JTextField();
				String reviewIdStr = JOptionPane.showInputDialog(this, "삭제할 리뷰ID:");
				if (reviewIdStr != null) {
					try {
						int reviewId = Integer.parseInt(reviewIdStr);
						boolean result = ReviewGUI.deleteReview(reviewId);
						JOptionPane.showMessageDialog(this, result ? "삭제 완료!" : "삭제 실패!");
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(this, "입력 오류: " + ex.getMessage());
					}
				}
				clearTable();
				tableModel.setColumnIdentifiers(new String[] { "리뷰ID", "기관ID", "평점", "내용" });
				updateTable(ReviewGUI.getUserReview(userId));
			}

		});
		buttons[4].addActionListener(e -> {

			String instIdStr = JOptionPane.showInputDialog(this, "기관ID 입력:");
			int institutionId = -1;
			if (instIdStr != null) {
				try {
					institutionId = Integer.parseInt(instIdStr);
					tableModel.setColumnIdentifiers(
							new String[] { "기관명", "기관평균", "리뷰수", "기관ID", "유저ID", "리뷰ID", "평점", "작성일", "내용" });
					Object[][] data = ReviewGUI.getInstitutionStats(institutionId);
					for (Object[] row : data) {
						tableModel.addRow(row);
					}
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(this, "입력 오류: " + ex.getMessage());
				}
				clearTable();
				tableModel.setColumnIdentifiers(
						new String[] { "기관명", "기관평균", "리뷰수", "기관ID", "유저ID", "리뷰ID", "평점", "작성일", "내용" });
				updateTable(ReviewGUI.getInstitutionStats(institutionId));
			}
		});
		buttons[5].addActionListener(e -> {
			clearTable();
			String userIdStr = JOptionPane.showInputDialog(this, "사용자ID 입력:");
			int userId = -1;
			if (userIdStr != null) {
				try {
					userId = Integer.parseInt(userIdStr);
					tableModel.setColumnIdentifiers(new String[] { "유저ID", "기관ID", "평점", "내용", "누적 리뷰수", "누적 평균 평점" });
					Object[][] data = ReviewGUI.getUserStats(userId);
					updateTable(data);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(this, "입력 오류: " + ex.getMessage());
				}
			}
		});
		buttons[6].addActionListener(e -> {
			menuNum = -1;
			clearUI();
		});
		updateUI();
	}

	private void showAppointmentMenu() {
		clearTable();
		if (userId == -1) {
			JOptionPane.showMessageDialog(frame, "로그인 상태가 아닙니다.", "오류", JOptionPane.ERROR_MESSAGE);
		} else {
			showUI();
			menuNum = 3;
			titleLabel.setText("상담 관리 시스템");
			for (int i = 0; i < buttons.length; i++) {
				buttons[i].setVisible(false);
			}
			updateUI();
			if (userId < 20000) { // 환자
				titleLabel.setText("상담 조회 시스템");
				Object[][] data = AppointmentRecordGUI.getRecordsByUser(userId);
				tableModel.setColumnIdentifiers(new String[] { "기록ID", "환자ID", "기관ID", "날짜", "처방", "진단", "내용" });
				updateTable(data);
				buttons[0].setVisible(true);
				buttons[0].setText("뒤로 가기");
				buttons[0].addActionListener(e -> {
					menuNum = -1;
					clearUI();
				});
				updateUI();
			} else {
				clearButtonListeners();
				String[] labels = { "상담 기록 조회", "상담 기록 등록", "상담 기록 수정", "상담 기록 삭제", "내원자 트래킹 조회", "뒤로 가기", "" };
				for (int i = 0; i < buttons.length; i++) {
					buttons[i].setVisible(true);
					buttons[i].setText(labels[i]);
					for (ActionListener al : buttons[i].getActionListeners())
						buttons[i].removeActionListener(al);
				}

				buttons[6].setVisible(false);
				buttons[0].addActionListener(e -> {
					clearTable();
					Object[][] data = AppointmentRecordGUI.getRecordsByUser(userId);
					tableModel.setColumnIdentifiers(new String[] { "기록ID", "환자ID", "기관ID", "날짜", "처방", "진단", "내용" });
					updateTable(data);
				});
				buttons[1].addActionListener(e -> {
					clearTable();
					Object[][] data = AppointmentRecordGUI.getRecordsByUser(userId);
					tableModel.setColumnIdentifiers(new String[] { "기록ID", "환자ID", "기관ID", "날짜", "처방", "진단", "내용" });
					updateTable(data);
					JTextField pidField = new JTextField();
					JTextField diagField = new JTextField();
					JTextField prescField = new JTextField();
					JTextField recordField = new JTextField();

					Object[] message = { "환자 ID:", pidField, "진단명:", diagField, "처방:", prescField, "상담 내용:",
							recordField };

					int option = JOptionPane.showConfirmDialog(this, message, "상담 기록 등록", JOptionPane.OK_CANCEL_OPTION);
					if (option == JOptionPane.OK_OPTION) {
						int pid = Integer.parseInt(pidField.getText());
						String diag = diagField.getText().trim();
						String presc = prescField.getText().trim();
						String rec = recordField.getText().trim();

						boolean result = AppointmentRecordGUI.insertRecord(userId, pid, presc, diag, rec);
						JOptionPane.showMessageDialog(this, result ? "등록 완료!" : "등록 실패 - 알레르기 충돌 확인");
					}
					clearTable();
					data = AppointmentRecordGUI.getRecordsByUser(userId);
					tableModel.setColumnIdentifiers(new String[] { "기록ID", "환자ID", "기관ID", "날짜", "처방", "진단", "내용" });
					updateTable(data);
				});
				buttons[2].addActionListener(e -> {
					clearTable();
					Object[][] data = AppointmentRecordGUI.getRecordsByUser(userId);
					tableModel.setColumnIdentifiers(new String[] { "기록ID", "환자ID", "기관ID", "날짜", "처방", "진단", "내용" });
					updateTable(data);
					JTextField idField = new JTextField();
					JTextField diagField = new JTextField();
					JTextField prescField = new JTextField();
					JTextField recordField = new JTextField();

					Object[] message = { "기록ID:", idField, "진단명:", diagField, "처방:", prescField, "상담 내용:",
							recordField };

					int option = JOptionPane.showConfirmDialog(this, message, "기록 수정", JOptionPane.OK_CANCEL_OPTION);
					if (option == JOptionPane.OK_OPTION) {
						int id = Integer.parseInt(idField.getText());
						String diag = diagField.getText();
						String presc = prescField.getText();
						String rec = recordField.getText();

						boolean result = AppointmentRecordGUI.updateRecord(id, diag, presc, rec);
						JOptionPane.showMessageDialog(this, result ? "수정 완료!" : "수정 실패 - 알레르기 충돌 확인");
					}
					data = AppointmentRecordGUI.getRecordsByUser(userId);
					clearTable();
					tableModel.setColumnIdentifiers(new String[] { "기록ID", "환자ID", "기관ID", "날짜", "처방", "진단", "내용" });
					updateTable(data);
				});
				buttons[3].addActionListener(e -> {
					clearTable();
					Object[][] data = AppointmentRecordGUI.getRecordsByUser(userId);
					tableModel.setColumnIdentifiers(new String[] { "기록ID", "환자ID", "기관ID", "날짜", "처방", "진단", "내용" });
					updateTable(data);
					String idStr = JOptionPane.showInputDialog(this, "삭제할 상담 기록 ID:");
					if (idStr != null) {
						int id = Integer.parseInt(idStr);
						boolean result = AppointmentRecordGUI.deleteRecord(id);
						JOptionPane.showMessageDialog(this, result ? "삭제 완료!" : "삭제 실패");
					}
					data = AppointmentRecordGUI.getRecordsByUser(userId);
					clearTable();
					tableModel.setColumnIdentifiers(new String[] { "기록ID", "환자ID", "기관ID", "날짜", "처방", "진단", "내용" });
					updateTable(data);
				});
				buttons[4].addActionListener(e -> {
					clearTable();
					String input = JOptionPane.showInputDialog(this, "조회할 내원자 ID를 입력하세요.: ");
					if (input == null || input.isEmpty())
						return;

					int patientId = Integer.parseInt(input);
					Object[][] data = AppointmentRecordGUI.getTrackingRecords(userId, patientId);

					if (data == null) {
						JOptionPane.showMessageDialog(this, "해당 내원자 조회 권한이 없거나 회원 정보가 없습니다.");
					} else if (data.length == 0) {
						JOptionPane.showMessageDialog(this, "해당 사용자의 트래킹 기록이 없습니다.");
					} else {
						tableModel.setColumnIdentifiers(
								new String[] { "날짜", "기관ID", "감정점수", "수면시간", "운동", "운동시간", "한 마디" });
						updateTable(data);
					}
				});
				buttons[5].addActionListener(e -> {
					menuNum = -1;
					clearUI();
				});
				updateUI();
			}

		}

	}

	private void showTrackingMenu() {
		clearTable();
		showUI();
		menuNum = 4;
		titleLabel.setText("트래킹 (일일 습관)");
		clearButtonListeners();
		String[] labels = { "전체 트래킹 조회", "트래킹 등록", "트래킹 수정", "트래킹 삭제", "트래킹 통계", "뒤로 가기", "" };
		for (int i = 0; i < buttons.length; i++) {
			buttons[i].setVisible(true);
			buttons[i].setText(labels[i]);
			for (ActionListener al : buttons[i].getActionListeners())
				buttons[i].removeActionListener(al);
		}
		buttons[0].addActionListener(e -> {
			clearTable();
			if (userId == -1) {
				JOptionPane.showMessageDialog(frame, "로그인 상태가 아닙니다.", "오류", JOptionPane.ERROR_MESSAGE);
			} else {
				tableModel.setColumnIdentifiers(
						new String[] { "유저 아이디", "기관 아이디", "날짜", "감정", "수면시간", "운동 이름", "운동 시간", "코멘트" });
				Object[][] data = TrackingGUI.getTracking(userId);
				updateTable(data);
			}
		});

		buttons[1].addActionListener(e -> {
			clearTable();
			tableModel.setColumnIdentifiers(
			new String[] { "유저 아이디", "기관 아이디", "날짜", "감정", "수면시간", "운동 이름", "운동 시간", "코멘트" });
			Object[][] data = TrackingGUI.getTracking(userId);
			updateTable(data);
			if (userId == -1) {
				JOptionPane.showMessageDialog(frame, "로그인 상태가 아닙니다.", "오류", JOptionPane.ERROR_MESSAGE);
				return;
			}

			// 입력 필드 생성
			JTextField institutionIdField = new JTextField();
			JTextField feelingField = new JTextField();
			JTextField sleepingField = new JTextField();
			JTextField exerciseNameField = new JTextField();
			JTextField exerciseTimeField = new JTextField();
			JTextField commentField = new JTextField();

			Object[] message = { "기관 ID*:", institutionIdField, "감정 점수 (1-100)*:", feelingField, "수면 시간*:",
					sleepingField, "운동 이름:", exerciseNameField, "운동 시간:", exerciseTimeField, "한 문장 메모:", commentField };

			int option = JOptionPane.showConfirmDialog(frame, message, "트래킹 등록", JOptionPane.OK_CANCEL_OPTION);

			if (option == JOptionPane.OK_OPTION) {
				try {
					int institutionId = Integer.parseInt(institutionIdField.getText().trim());
					int feeling = Integer.parseInt(feelingField.getText().trim());
					int sleeping = Integer.parseInt(sleepingField.getText().trim());

					String exerciseName = exerciseNameField.getText().trim();
					if (exerciseName.isEmpty())
						exerciseName = null;

					String exerciseTimeStr = exerciseTimeField.getText().trim();
					Double exerciseTime = (exerciseTimeStr.isEmpty()) ? null : Double.parseDouble(exerciseTimeStr);

					String comment = commentField.getText().trim();
					if (comment.isEmpty())
						comment = null;

					boolean result = TrackingGUI.registerTracking(userId, institutionId, sleeping, feeling,
							exerciseName, exerciseTime, comment);

					JOptionPane.showMessageDialog(frame, result ? "등록 완료!" : "등록 실패!");

					clearTable();
					data = TrackingGUI.getTracking(userId);
					tableModel.setColumnIdentifiers(
							new String[] { "유저 아이디", "기관 아이디", "날짜", "감정", "수면시간", "운동 이름", "운동 시간", "코멘트" });
					updateTable(data);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(frame, "입력 오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		buttons[2].addActionListener(e -> {
			clearTable();
			tableModel.setColumnIdentifiers(
			new String[] { "유저 아이디", "기관 아이디", "날짜", "감정", "수면시간", "운동 이름", "운동 시간", "코멘트" });
			Object[][] data = TrackingGUI.getTracking(userId);
			updateTable(data);
			if (userId == -1) {
				JOptionPane.showMessageDialog(frame, "로그인 상태가 아닙니다.", "오류", JOptionPane.ERROR_MESSAGE);
				return;
			}

			try {
				JTextField institutionIdField = new JTextField();
				JTextField dateField = new JTextField();
				JTextField feelingField = new JTextField();
				JTextField sleepingField = new JTextField();
				JTextField exerciseNameField = new JTextField();
				JTextField exerciseTimeField = new JTextField();
				JTextField commentField = new JTextField();

				Object[] message = { "기관 ID*:", institutionIdField, "수정할 날짜 (yyyy-MM-dd HH:mm:ss)*:", dateField,
						"감정 점수 (1-100)*:", feelingField, "수면 시간*:", sleepingField, "운동 이름:", exerciseNameField,
						"운동 시간:", exerciseTimeField, "한 문장 메모:", commentField };

				int option = JOptionPane.showConfirmDialog(frame, message, "트래킹 수정", JOptionPane.OK_CANCEL_OPTION);

				if (option == JOptionPane.OK_OPTION) {
					int institutionId = Integer.parseInt(institutionIdField.getText());
					LocalDateTime date = LocalDateTime.parse(dateField.getText().replace(" ", "T"));
					int feeling = Integer.parseInt(feelingField.getText());
					int sleeping = Integer.parseInt(sleepingField.getText());

					// trim으로 공백 제거 후 null 처리
					String exerciseNameRaw = exerciseNameField.getText().trim();
					String exerciseTimeRaw = exerciseTimeField.getText().trim();
					String commentRaw = commentField.getText().trim();

					String exerciseName = exerciseNameRaw.isEmpty() ? null : exerciseNameRaw;

					// exerciseTime은 Double, 빈칸이면 null (또는 기본값 사용 가능)
					Double exerciseTime = exerciseTimeRaw.isEmpty() ? null : Double.parseDouble(exerciseTimeRaw);

					String comment = commentRaw.isEmpty() ? null : commentRaw;

					boolean result = TrackingGUI.updateTracking(userId, institutionId, date, feeling, sleeping,
							exerciseName, exerciseTime, comment);
					JOptionPane.showMessageDialog(frame, result ? "수정 완료!" : "수정 실패!");

					// 테이블 갱신
					clearTable();
					tableModel.setColumnIdentifiers(
							new String[] { "유저 아이디", "기관 아이디", "날짜", "감정", "수면시간", "운동 이름", "운동 시간", "코멘트" });
					data = TrackingGUI.getTracking(userId);
					updateTable(data);
				}

			} catch (Exception ex) {
				JOptionPane.showMessageDialog(frame, "입력 오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
			}
		});

		buttons[3].addActionListener(e -> {
		    clearTable();
		    tableModel.setColumnIdentifiers(
		        new String[] { "유저 아이디", "기관 아이디", "날짜", "감정", "수면시간", "운동 이름", "운동 시간", "코멘트" });
		    Object[][] data = TrackingGUI.getTracking(userId);
		    updateTable(data);

		    // 날짜 & 기관 ID 입력받기
		    JTextField dateField = new JTextField();
		    JTextField institutionIdField = new JTextField();

		    Object[] message = {
		        "삭제할 날짜를 입력하세요 (예: 2025-06-08 14:00:00):", dateField,
		        "삭제할 기관 ID를 입력하세요:", institutionIdField
		    };

		    int option = JOptionPane.showConfirmDialog(frame, message, "트래킹 삭제", JOptionPane.OK_CANCEL_OPTION);
		    if (option != JOptionPane.OK_OPTION) return;

		    try {
		        String dateStr = dateField.getText().trim();
		        int institutionId = Integer.parseInt(institutionIdField.getText().trim());
		        LocalDateTime date = LocalDateTime.parse(dateStr.replace(" ", "T")); // 날짜 변환

		        int confirm = JOptionPane.showConfirmDialog(frame, "정말 삭제하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
		        if (confirm != JOptionPane.YES_OPTION) return;

		        boolean result = TrackingGUI.deleteTracking(userId, institutionId, date);

		        if (result) {
		            JOptionPane.showMessageDialog(frame, "삭제 완료!");
		            clearTable();
		            tableModel.setColumnIdentifiers(new String[] {
		                "유저 아이디", "기관 아이디", "날짜", "감정", "수면시간", "운동 이름", "운동 시간", "코멘트"
		            });
		            data = TrackingGUI.getTracking(userId);
		            updateTable(data);
		        } else {
		            JOptionPane.showMessageDialog(frame, "삭제 실패: 일치하는 데이터가 없습니다.", "오류", JOptionPane.ERROR_MESSAGE);
		        }
		    } catch (Exception ex) {
		        JOptionPane.showMessageDialog(frame, "입력 오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
		    }
		});


		buttons[4].addActionListener(e -> {
			clearTable();
			tableModel.setColumnIdentifiers(new String[] { "사용자ID", "평균 수면시간", "평균 운동시간", "총 수면시간", "총 운동시간" });
			Object[][] data = TrackingGUI.analyzeTracking();
			updateTable(data);
		});
		buttons[5].addActionListener(e -> {
			menuNum = -1;
			clearUI();
		});
		buttons[6].setVisible(false);
		updateUI();
	}

	private void updateUI() { 
		CP.removeAll();
		CP.setLayout(null);

		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		titleLabel.setFont(new Font("한컴 말랑말랑 Bold", Font.PLAIN, 20));
		titleLabel.setBounds(0, 0, 800, 70);
		CP.add(titleLabel);

		resultScroll.setBounds(0, 70, 800, 450);
		CP.add(resultScroll);

		CP.add(districtScroll); 
		districtScroll.setVisible(false);

		if (menuNum != -1) {
			int y = 70;
			for (int i = 0; i < buttons.length; i++) {
				buttons[i].setBounds(830, y, 200, 30);
				CP.add(buttons[i]);
				y += 50;
			}
		}
		CP.revalidate();
		CP.repaint();
	}
	


	private void showUI() {
		for (JButton button : buttons) {
			button.setVisible(true);
		}

		resultScroll.setVisible(true);

		districtScroll.setVisible(true);
		typeScroll.setVisible(true);

		ta.setVisible(true);

		titleLabel.setText("");
	}
	
	private void clearUI() {
		clearTable();
		for (JButton button : buttons) {
			button.setVisible(false);
		}

		resultScroll.setVisible(false);

		districtScroll.setVisible(false);
		typeScroll.setVisible(false);

		ta.setVisible(false);

		titleLabel.setText("");
	}


	private void clearTable() {
		tableModel.setRowCount(0);
		tableModel.setColumnIdentifiers(new String[] {});
	}
	
	private void updateTable(Object[][] data) {
		for (Object[] row : data) {
			tableModel.addRow(row);
		}
	}

}
