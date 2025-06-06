// 상담 기록 기능 구현을 위한 class 파일입니다.
//날짜 형식 출력과 내원자의 트래킹 정보를 가져올 수 있도록 기능 추가했습니다!(트래킹 함수 사용)
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class AppointmentRecord {
	static final String dbID = "testuser";
	static final String dbPW = "testpw";
	static final String dbName = "mindlink";
	static final String header = "jdbc:mysql://localhost:3306/";
	static final String encoding = "useUnicode=true&characterEncoding=UTF-8";
	static final String url = header + dbName + "?" + encoding;

	public static Scanner input = new Scanner(System.in);

	public static void startAppointmentRecord(int userId) {

		if (userId < 20000) {
			showPatientConsultations(userId);
		} else {
			showDoctorMenu(userId, input);
		}
	}

	// 환자: 본인 상담 기록 조회
	static void showPatientConsultations(int patientId) {
		String sql = "SELECT * FROM PatientView WHERE userId = ? ORDER BY recordDate DESC";
		try (Connection conn = DriverManager.getConnection(url, dbID, dbPW);
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, patientId);
			ResultSet rs = pstmt.executeQuery();

			System.out.println("\n[나의 상담 기록]");
			boolean hasData = false;
			while (rs.next()) {
				hasData = true;
				printConsultation(rs);
			}
			if (!hasData)
				System.out.println("상담 기록이 없습니다.");

		} catch (SQLException e) {
			System.out.println("오류: " + e.getMessage());
		}
	}

	// 의료인: DoctorView 사용해 기관 기록 관리
	static void showDoctorMenu(int doctorId, Scanner scanner) {
		try (Connection conn = DriverManager.getConnection(url, dbID, dbPW)) {
			int institutionId = getInstitutionId(conn, doctorId);

			if (institutionId == -1) {
				System.out.println("의료인 정보가 없습니다.");
				return;
			}

			while (true) {
				System.out.println("\n[의료진 상담 관리 메뉴]");
				System.out.println("1. 상담 기록 전체 조회");
				System.out.println("2. 상담 기록 등록");
				System.out.println("3. 상담 기록 수정");
				System.out.println("4. 상담 기록 삭제");
				System.out.println("5. 내원자 트래킹 전체 조회");
				System.out.println("0. 이전으로");
				System.out.print("선택: ");
				int choice = scanner.nextInt();
				scanner.nextLine();

				switch (choice) {
				case 1:
					showConsultationsByInstitution(conn, institutionId);
					break;
				case 2:
					createAppointmentRecord(doctorId, scanner);
					break; 
				case 3:
					updateConsultation(conn, scanner);
					break;
				case 4:
					deleteConsultation(conn, scanner);
					break;
				case 5: // ← 추가됨
					System.out.print("조회할 내원자 ID를 입력하세요: ");
					try {
						int patientId = scanner.nextInt();
						scanner.nextLine(); // 버퍼 비우기

						// 환자 기관 ID와 의료인 기관 ID 비교
						int patientInstitutionId = getPatientInstitutionId(conn, patientId);

						if (patientInstitutionId == -1) {
							System.out.println("해당 내원자의 정보가 존재하지 않습니다.");
						} else if (patientInstitutionId != institutionId) {
							System.out.println("[권한 없음] 해당 내원자는 소속 기관의 환자가 아닙니다.");
						} else {
							// 권한이 있는 경우에만 트래킹 기록을 조회
							Tracking.getTracking(patientId);
						}
					} catch (Exception e) {
						System.out.println("유효한 숫자 ID를 입력해주세요.");
						scanner.nextLine(); // 잘못된 입력 버퍼 비우기
					}
					break;
				case 0:
					return;
				default:
					System.out.println("잘못된 입력입니다.");
				}

			}

		} catch (SQLException e) {
			System.out.println("DB 오류: " + e.getMessage());
		}
	}

	// 의사 userId로 기관 id 조회
	static int getInstitutionId(Connection conn, int doctorId) throws SQLException {
		String sql = "SELECT institutionId FROM Medical WHERE userId = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, doctorId);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getInt("institutionId");
			}
		}
		return -1;
	}
	
	// 환자 userId로 기관 id 조회
	static int getPatientInstitutionId(Connection conn, int patientId) throws SQLException {
		String sql = "SELECT institutionId FROM Tracking WHERE userId = ? LIMIT 1"; // 트래킹 기록 중 하나에서 기관 ID 가져오기
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, patientId);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getInt("institutionId");
			}
		}
		return -1; // 해당 환자 ID가 없거나 트래킹 기록이 없는 경우
	}

	// 기관 내 모든 상담 기록 조회(DoctorView로 제한)
	static void showConsultationsByInstitution(Connection conn, int institutionId) throws SQLException {
		String sql = "SELECT * FROM DoctorView WHERE institutionId = ? ORDER BY recordDate DESC";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, institutionId);
			ResultSet rs = pstmt.executeQuery();
			System.out.println("\n[기관 상담 기록]");
			boolean hasData = false;
			while (rs.next()) {
				hasData = true;
				printConsultation(rs);
			}
			if (!hasData)
				System.out.println("상담 기록이 없습니다.");
		}
	}

	static void createAppointmentRecord(int userId, Scanner scanner) {
		try (Connection conn = DriverManager.getConnection(url, dbID, dbPW)) {

			// 사용자 입력
			System.out.print("내원자 아이디: ");
			int pId = scanner.nextInt();
			scanner.nextLine();

			System.out.print("진단명: ");
			String diagnosis = scanner.nextLine().trim();
			
			System.out.print("처방 내용: ");
			String prescription = scanner.nextLine().trim();

			System.out.print("상담/진료 기록 내용: ");
			String record = scanner.nextLine().trim();


			if (prescription.isEmpty() || diagnosis.isEmpty() || record.isEmpty()) {
				System.out.println("모든 내용을 입력해주세요.");
				return;
			}

			String sql = "INSERT INTO AppointmentRecord (userId, institutionId, prescription, diagnosis, recordDate, record) "
					+ "VALUES (?, ?, ?, ?, ?, ?)";

			try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
				int institutionId = getInstitutionId(conn, userId);

				if (institutionId == -1) {
					System.out.println("의료인 정보가 없습니다.");
					return;
				}
				pstmt.setInt(1, pId);
				pstmt.setInt(2, institutionId);
				pstmt.setString(3, prescription);
				pstmt.setString(4, diagnosis);
				java.sql.Date now = new java.sql.Date(System.currentTimeMillis());
				pstmt.setDate(5, now);
				pstmt.setString(6, record);

				int result = pstmt.executeUpdate();
				System.out.println(result + "건 등록 완료!");
			} catch (SQLException e) {
				handleSQLException("등록", e);
			}
		} catch (Exception e) {
			handleInputException(e);
		}
	}

	// 상담 기록 수정(처방할 때 알레르기 충돌 검사 포함)
	static void updateConsultation(Connection conn, Scanner scanner) throws SQLException {
		System.out.print("수정할 상담 아이디 (9____): ");
		int id = scanner.nextInt();
		scanner.nextLine();
		System.out.print("새 진단명: ");
		String diagnosis = scanner.nextLine();
		System.out.print("새 처방 약물: ");
		String prescription = scanner.nextLine();
		System.out.print("새 상담 내용: ");
		String record = scanner.nextLine();

		// 알레르기 확인
		if (hasAllergyConflict(conn, id, prescription)) {
			System.out.println("[경고] 환자가 해당 약물에 알레르기가 있어 처방할 수 없습니다.");
			return;
		}

		String sql = "UPDATE AppointmentRecord SET diagnosis = ?, prescription = ?, record = ? WHERE appointmentId = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, diagnosis);
			pstmt.setString(2, prescription);
			pstmt.setString(3, record);
			pstmt.setInt(4, id);
			int result = pstmt.executeUpdate();
			System.out.println(result > 0 ? "수정 완료" : "해당 상담 기록이 없습니다.");
		}
	}

	// 처방 약물 - 알레르기 충돌 검사
	static boolean hasAllergyConflict(Connection conn, int appointmentId, String prescription) throws SQLException {
		String sql = "SELECT p.allergies FROM Patient p JOIN AppointmentRecord ar ON p.userId = ar.userId WHERE ar.appointmentId = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, appointmentId);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				String allergies = rs.getString("allergies");
				return allergies != null && allergies.toLowerCase().contains(prescription.toLowerCase());
			}
		}
		return false;
	}

	// 상담 기록 삭제
	static void deleteConsultation(Connection conn, Scanner scanner) throws SQLException {
		System.out.print("삭제할 상담 아이디 (9____): ");
		int id = scanner.nextInt();
		String sql = "DELETE FROM AppointmentRecord WHERE appointmentId = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, id);
			int result = pstmt.executeUpdate();
			System.out.println(result > 0 ? "삭제 완료" : "해당 상담 기록이 없습니다.");
		}
	}

	// 상담 기록 출력
	static void printConsultation(ResultSet rs) throws SQLException {
		System.out.printf("(%d) 환자ID: %d\t| 기관ID: %d\t| 날짜: %s\n처방: %s\t\t| 진단: %s\n내용: %s\n\n", rs.getInt("appointmentId"),
				rs.getInt("userId"), rs.getInt("institutionId"), rs.getDate("recordDate"), rs.getString("prescription"),
				rs.getString("diagnosis"), rs.getString("record"));
	}
	
	
	
	
	

	// 예외 처리 메소드들
	private static void handleSQLException(String operation, SQLException e) {
		System.out.println(operation + " 작업 실패: " + e.getMessage());
	}

	private static void handleInputException(Exception e) {
		System.out.println("입력 오류: " + e.getMessage());
	}
}
