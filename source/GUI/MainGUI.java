import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class MainGUI extends JFrame {
	// 상태 변수
	public static Scanner input = new Scanner(System.in);
	static int userId = -1;
	static int menuNum = -1;

	// GUI 컴포넌트
	JPanel CP;
	JButton[] buttons = new JButton[7];
	JFrame frame;
	JLabel titleLabel = new JLabel();
	JTable table;
	JScrollPane resultScroll;
	DefaultTableModel tableModel;
	JScrollPane typeScroll = new JScrollPane();
	JScrollPane districtScroll;
	JList<String> districtList;

	private Font kopubFont() {
		return new Font("KoPubWorld돋움체 Medium", Font.PLAIN, 14);
	}

	// GUI 창을 띄울 main 함수
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

		// ContentPanel 설정
		CP = new JPanel();
		CP.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(CP);
		CP.setLayout(null);

		// 결과가 출력될 Table 설정
		tableModel = new DefaultTableModel(new Object[][] {}, new String[] {});
		table = new JTable(tableModel);
		resultScroll = new JScrollPane(table);
		resultScroll.setBounds(0, 100, 800, 400);
		CP.add(resultScroll);

		// Button 설정
		for (int i = 0; i < buttons.length; i++) {
			buttons[i] = new JButton();
			buttons[i].setFont(kopubFont());
			CP.add(buttons[i]);
		}

		// menuBar 설정
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		setupMenus(menuBar);

		districtList = new JList<>(new String[] { "(select)", "마포구", "서대문구", "은평구", "종로구" });
		districtScroll = new JScrollPane(districtList);
		districtScroll.setBounds(820, 100, 150, 100);
		districtScroll.setVisible(false);
		CP.add(districtScroll);

		CP.add(titleLabel);
		CP.setVisible(true);
		updateUI();
	}

	private void setupMenus(JMenuBar menuBar) {
		JMenu sysMenu = new JMenu("시스템");
		sysMenu.setFont(kopubFont());

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
	}

	private void clearUI() {
		clearTable(new String[] {});
		for (JButton button : buttons) {
			button.setVisible(false);
		}

		resultScroll.setVisible(false);
		districtScroll.setVisible(false);
		typeScroll.setVisible(false);
		titleLabel.setText("");
	}

	private void setColumnWidths(int[] widths) {
		for (int i = 0; i < widths.length; i++) {
			table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
		}
	}

	private void clearTable(String[] columnName) {
		tableModel.setRowCount(0);
		tableModel.setColumnIdentifiers(columnName);
	}

	private void updateTable(Object[][] data) {
		for (Object[] row : data) {
			tableModel.addRow(row);
		}
	}

	private void login() {
		if (userId > 10000) {
			JOptionPane.showMessageDialog(frame, "이미 로그인되어 있습니다. (" + userId + ")", "알림",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		String inputId = JOptionPane.showInputDialog(frame, "아이디를 입력하세요 (0 입력 시 종료)");
		if (inputId == null)
			return;

		try {
			int id = Integer.parseInt(inputId);
			if (id == 0) {
				JOptionPane.showMessageDialog(frame, "로그인을 종료합니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
			} else if (id > 10000) {
				userId = id;
				JOptionPane.showMessageDialog(frame, "로그인 성공! (" + userId + ")", "로그인",
						JOptionPane.INFORMATION_MESSAGE);
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
		clearUI();
		clearTable(new String[] {});
	}

	private void clearButtonListeners() {
		for (JButton btn : buttons) {
			for (ActionListener al : btn.getActionListeners()) {
				btn.removeActionListener(al);
			}
			btn.setVisible(true);
		}
	}

	private void showSearchMenu() {
		clearTable(new String[] {});
		showUI();
		menuNum = 1;
		titleLabel.setText("기관 검색");
		clearButtonListeners();

		String[] labels = { "전체 기관 조회", "기관 타입별 조회", "지역별 기관 조회", "평균 평점 이상 기관 조회", "뒤로 가기", "", "" };

		for (int i = 0; i < buttons.length; i++) {
			JButton button = buttons[i];
			button.setText(labels[i]);
			button.setVisible(!labels[i].isEmpty());
		}

		setSearchButtonActions();
		updateUI();
	}

	private void setSearchButtonActions() {
		buttons[0].addActionListener(e -> showAllInstitutions());
		buttons[1].addActionListener(e -> showInstitutionByType());
		buttons[2].addActionListener(e -> showInstitutionByDistrict());
		buttons[3].addActionListener(e -> showInstitutionByRating());
		buttons[4].addActionListener(e -> {
			menuNum = -1;
			clearUI();
		});
	}

	private void showAllInstitutions() {
		clearTable(new String[] { "기관명", "주소", "전화번호" });
		Object[][] data = SearchGUI.getAllInstitution();
		updateTable(data);
	}

	private void showInstitutionByType() {
		String[] types = { "공공", "민간" };
		String selected = (String) JOptionPane.showInputDialog(this, "기관 타입을 선택하세요:", "기관 타입별 조회",
				JOptionPane.QUESTION_MESSAGE, null, types, types[0]);

		if (selected != null) {
			clearTable(new String[] { "기관명", "주소", "전화번호" });
			Object[][] data = SearchGUI.getInstitutionByType(selected);
			updateTable(data);
		}
	}

	private void showInstitutionByDistrict() {
		String[] districts = { "마포구", "서대문구", "은평구", "종로구" };
		String selected = (String) JOptionPane.showInputDialog(this, "지역을 선택하세요:", "지역별 기관 조회",
				JOptionPane.QUESTION_MESSAGE, null, districts, districts[0]);

		if (selected != null) {
			clearTable(new String[] { "기관명", "주소", "전화번호" });
			Object[][] data = SearchGUI.getInstitutionByDistrict(selected);
			updateTable(data);
		}
	}

	private void showInstitutionByRating() {
		typeScroll.setVisible(false);
		districtScroll.setVisible(false);

		clearTable(new String[] { "기관명", "주소", "전화번호", "평점" });
		Object[][] data = SearchGUI.getInstitutionByRating();
		updateTable(data);
	}

	private void showReviewMenu() {
		clearTable(new String[] {});
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

		setReviewButtonActions();
		updateUI();
	}

	private void setReviewButtonActions() {
		buttons[0].addActionListener(e -> showAllReviews());
		buttons[1].addActionListener(e -> createReview());
		buttons[2].addActionListener(e -> updateReview());
		buttons[3].addActionListener(e -> deleteReview());
		buttons[4].addActionListener(e -> showInstitutionReviewStats());
		buttons[5].addActionListener(e -> showUserReviewStats());
		buttons[6].addActionListener(e -> {
			menuNum = -1;
			clearUI();
		});
	}

	private void showAllReviews() {
		clearTable(new String[] { "리뷰ID", "유저ID", "기관ID", "내용", "평점", "작성일", "기관평균", "기관순위" });
		updateTable(ReviewGUI.getAllReviews());
	}

	private void createReview() {
		clearTable(new String[] { "리뷰ID", "기관ID", "평점", "내용" });
		updateTable(ReviewGUI.getUserReview(userId));

		if (userId == -1) {
			JOptionPane.showMessageDialog(frame, "로그인 상태가 아닙니다.", "오류", JOptionPane.ERROR_MESSAGE);
			return;
		}

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
				updateUserReviews();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, "입력 오류: " + ex.getMessage());
			}
		}
	}

	private void updateReview() {
		clearTable(new String[] { "리뷰ID", "기관ID", "평점", "내용" });

		if (userId == -1) {
			JOptionPane.showMessageDialog(frame, "로그인 상태가 아닙니다.", "오류", JOptionPane.ERROR_MESSAGE);
			return;
		}

		updateTable(ReviewGUI.getUserReview(userId));

		JTextField reviewIdField = new JTextField();
		JTextField contentField = new JTextField();
		JTextField ratingField = new JTextField();
		Object[] message = { "리뷰ID:", reviewIdField, "새 내용:", contentField, "새 평점(1~5):", ratingField };

		int option = JOptionPane.showConfirmDialog(this, message, "리뷰 수정", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) {
			try {
				int reviewId = Integer.parseInt(reviewIdField.getText());
				String content = contentField.getText();
				int rating = Integer.parseInt(ratingField.getText());
				boolean result = ReviewGUI.updateReview(reviewId, content, rating);
				JOptionPane.showMessageDialog(this, result ? "수정 완료!" : "수정 실패!");
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, "입력 오류: " + ex.getMessage());
			}
			updateUserReviews();
		}
	}

	private void deleteReview() {
		clearTable(new String[] { "리뷰ID", "기관ID", "평점", "내용" });

		if (userId == -1) {
			JOptionPane.showMessageDialog(frame, "로그인 상태가 아닙니다.", "오류", JOptionPane.ERROR_MESSAGE);
			return;
		}

		updateTable(ReviewGUI.getUserReview(userId));
		String reviewIdStr = JOptionPane.showInputDialog(this, "삭제할 리뷰ID:");

		if (reviewIdStr != null) {
			try {
				int reviewId = Integer.parseInt(reviewIdStr);
				boolean result = ReviewGUI.deleteReview(reviewId);
				JOptionPane.showMessageDialog(this, result ? "삭제 완료!" : "삭제 실패!");
				updateUserReviews();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, "입력 오류: " + ex.getMessage());
			}
		}
	}

	private void showInstitutionReviewStats() {
		String instIdStr = JOptionPane.showInputDialog(this, "기관ID 입력:");

		if (instIdStr != null) {
			try {
				int institutionId = Integer.parseInt(instIdStr);
				clearTable(new String[] { "기관명", "기관평균", "리뷰수", "기관ID", "유저ID", "리뷰ID", "평점", "작성일", "내용" });
				updateTable(ReviewGUI.getInstitutionStats(institutionId));
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, "입력 오류: " + ex.getMessage());
			}
		}
	}

	private void showUserReviewStats() {
		clearTable(new String[] {});
		String userIdStr = JOptionPane.showInputDialog(this, "사용자ID 입력:");

		if (userIdStr != null) {
			try {
				int userId = Integer.parseInt(userIdStr);
				tableModel.setColumnIdentifiers(new String[] { "유저ID", "기관ID", "평점", "내용", "누적 리뷰수", "누적 평균 평점" });
				updateTable(ReviewGUI.getUserStats(userId));
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, "입력 오류: " + ex.getMessage());
			}
		}
	}

	private void updateUserReviews() {
		clearTable(new String[] { "리뷰ID", "기관ID", "평점", "내용" });
		updateTable(ReviewGUI.getUserReview(userId));
	}


	private void showAppointmentMenu() {
		clearTable(new String[] {});
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
					clearTable(new String[] {});
					Object[][] data = AppointmentRecordGUI.getRecordsByUser(userId);
					tableModel.setColumnIdentifiers(new String[] { "기록ID", "환자ID", "기관ID", "날짜", "처방", "진단", "내용" });
					updateTable(data);
				});
				buttons[1].addActionListener(e -> {
					clearTable(new String[] {});
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
					clearTable(new String[] {});
					data = AppointmentRecordGUI.getRecordsByUser(userId);
					tableModel.setColumnIdentifiers(new String[] { "기록ID", "환자ID", "기관ID", "날짜", "처방", "진단", "내용" });
					updateTable(data);
				});
				buttons[2].addActionListener(e -> {
					clearTable(new String[] {});
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
					clearTable(new String[] {});
					tableModel.setColumnIdentifiers(new String[] { "기록ID", "환자ID", "기관ID", "날짜", "처방", "진단", "내용" });
					updateTable(data);
				});
				buttons[3].addActionListener(e -> {
					clearTable(new String[] {});
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
					clearTable(new String[] {});
					tableModel.setColumnIdentifiers(new String[] { "기록ID", "환자ID", "기관ID", "날짜", "처방", "진단", "내용" });
					updateTable(data);
				});
				buttons[4].addActionListener(e -> {
					clearTable(new String[] {});
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
		clearTable(new String[] {});
		showUI();
		menuNum = 4;
		titleLabel.setText("트래킹 (일일 습관)");
		clearButtonListeners();

		String[] labels = { "전체 트래킹 조회", "트래킹 등록", "트래킹 수정", "트래킹 삭제", "트래킹 통계", "뒤로 가기", "" };

		for (int i = 0; i < buttons.length; i++) {
			buttons[i].setText(labels[i]);
			buttons[i].setVisible(!labels[i].isEmpty());
		}

		setTrackingButtonActions();
		updateUI();
	}

	private void setTrackingButtonActions() {
		buttons[0].addActionListener(e -> showAllTrackings());
		buttons[1].addActionListener(e -> createTracking());
		buttons[2].addActionListener(e -> updateTracking());
		buttons[3].addActionListener(e -> deleteTracking());
		buttons[4].addActionListener(e -> showTrackingStats());
		buttons[5].addActionListener(e -> {
			menuNum = -1;
			clearUI();
		});
	}

	private void showAllTrackings() {
		clearTable(new String[] {});
		if (userId == -1) {
			JOptionPane.showMessageDialog(frame, "로그인 상태가 아닙니다.", "오류", JOptionPane.ERROR_MESSAGE);
			return;
		}

		tableModel.setColumnIdentifiers(
				new String[] { "트래킹 아이디", "유저 아이디", "기관 아이디", "날짜", "감정", "수면시간", "운동 이름", "운동 시간", "코멘트" });
		Object[][] data = TrackingGUI.getTracking(userId);
		updateTable(data);
	}

	private void createTracking() {
		if (userId == -1) {
			JOptionPane.showMessageDialog(frame, "로그인 상태가 아닙니다.", "오류", JOptionPane.ERROR_MESSAGE);
			return;
		}

		showAllTrackings(); // 현재 트래킹 목록 표시

		// 입력 필드 생성
		JTextField institutionIdField = new JTextField();
		JTextField feelingField = new JTextField();
		JTextField sleepingField = new JTextField();
		JTextField exerciseNameField = new JTextField();
		JTextField exerciseTimeField = new JTextField();
		JTextField commentField = new JTextField();

		Object[] message = { "기관 ID*:", institutionIdField, "감정 점수 (1-100)*:", feelingField, "수면 시간*:", sleepingField,
				"운동 이름:", exerciseNameField, "운동 시간:", exerciseTimeField, "한 문장 메모:", commentField };

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

				boolean result = TrackingGUI.registerTracking(userId, institutionId, sleeping, feeling, exerciseName,
						exerciseTime, comment);

				JOptionPane.showMessageDialog(frame, result ? "등록 완료!" : "등록 실패!");
				showAllTrackings(); // 테이블 갱신

			} catch (Exception ex) {
				JOptionPane.showMessageDialog(frame, "입력 오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void updateTracking() {
		if (userId == -1) {
			JOptionPane.showMessageDialog(frame, "로그인 상태가 아닙니다.", "오류", JOptionPane.ERROR_MESSAGE);
			return;
		}

		showAllTrackings(); // 현재 트래킹 목록 표시

		try {
			JTextField tIdField = new JTextField();
			JTextField feelingField = new JTextField();
			JTextField sleepingField = new JTextField();
			JTextField exerciseNameField = new JTextField();
			JTextField exerciseTimeField = new JTextField();
			JTextField commentField = new JTextField();

			Object[] message = { "트래킹 ID*:", tIdField, "감정 점수 (1-100)*:", feelingField, "수면 시간*:", sleepingField,
					"운동 이름:", exerciseNameField, "운동 시간:", exerciseTimeField, "한 문장 메모:", commentField };

			int option = JOptionPane.showConfirmDialog(frame, message, "트래킹 수정", JOptionPane.OK_CANCEL_OPTION);

			if (option == JOptionPane.OK_OPTION) {
				int tId = Integer.parseInt(tIdField.getText());
				int feeling = Integer.parseInt(feelingField.getText());
				int sleeping = Integer.parseInt(sleepingField.getText());

				String exerciseNameRaw = exerciseNameField.getText().trim();
				String exerciseTimeRaw = exerciseTimeField.getText().trim();
				String commentRaw = commentField.getText().trim();

				String exerciseName = exerciseNameRaw.isEmpty() ? null : exerciseNameRaw;
				Double exerciseTime = exerciseTimeRaw.isEmpty() ? null : Double.parseDouble(exerciseTimeRaw);
				String comment = commentRaw.isEmpty() ? null : commentRaw;

				boolean result = TrackingGUI.updateTracking(userId, tId, feeling, sleeping, exerciseName, exerciseTime,
						comment);
				JOptionPane.showMessageDialog(frame, result ? "수정 완료!" : "수정 실패!");

				showAllTrackings(); // 테이블 갱신
			}

		} catch (Exception ex) {
			JOptionPane.showMessageDialog(frame, "입력 오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void deleteTracking() {
		if (userId == -1) {
			JOptionPane.showMessageDialog(frame, "로그인 상태가 아닙니다.", "오류", JOptionPane.ERROR_MESSAGE);
			return;
		}

		showAllTrackings(); // 현재 트래킹 목록 표시

		JTextField tIdField = new JTextField();
		Object[] message = { "삭제하고자 하는 트래킹의 아이디를 입력하세요:", tIdField };

		int option = JOptionPane.showConfirmDialog(frame, message, "트래킹 삭제", JOptionPane.OK_CANCEL_OPTION);
		if (option != JOptionPane.OK_OPTION)
			return;

		try {
			int tId = Integer.parseInt(tIdField.getText());

			int confirm = JOptionPane.showConfirmDialog(frame, "정말 삭제하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
			if (confirm != JOptionPane.YES_OPTION)
				return;

			boolean result = TrackingGUI.deleteTracking(userId, tId);

			if (result) {
				JOptionPane.showMessageDialog(frame, "삭제 완료!");
				showAllTrackings(); // 테이블 갱신
			} else {
				JOptionPane.showMessageDialog(frame, "삭제 실패: 일치하는 데이터가 없습니다.", "오류", JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(frame, "입력 오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void showTrackingStats() {
		clearTable(new String[] {});
		tableModel.setColumnIdentifiers(new String[] { "기관 ID", "사용자 ID", "평균 수면시간", "평균 운동시간", "총 수면시간", "총 운동시간" });
		Object[][] data = TrackingGUI.analyzeTracking();
		updateTable(data);
	}
}
