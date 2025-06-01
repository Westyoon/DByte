
// 오류 수정 및 구조 정비한 코드
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Scanner;

public class Tracking {
	static final String dbID = "testuser";
	static final String dbPW = "testpw";
	static final String dbName = "mindlink";
	static final String header = "jdbc:mysql://localhost:3306/";
	static final String encoding = "useUnicode=true&characterEncoding=UTF-8";
	static final String url = header + dbName + "?" + encoding;

	public static Scanner input = new Scanner(System.in);
	static int userId = -1;

	public static void startTracking(int userId) {
		while (true) {
			System.out.println("\n==== 트래킹 관리 시스템 ====");
			System.out.println("1. 트래킹 등록");
			System.out.println("2. 전체 트래킹 조회");
			System.out.println("3. 트래킹 정보 수정");
			System.out.println("4. 트래킹 정보 삭제");
			System.out.println("5. 사용자별 평균 트래킹 정보 조회");
			System.out.println("0. 종료");
			System.out.print("선택: ");

			int select;
			try {
				select = input.nextInt();
				input.nextLine();
			} catch (Exception e) {
				System.out.println("숫자를 입력해주세요.");
				input.nextLine();
				continue;
			}

			switch (select) {
			case 1:
				registerTracking(userId);
				break;
			case 2:
				getTracking(userId);
				break;
			case 3:
				updateTracking(userId);
				break;
			case 4:
				System.out.print("삭제할 기관 번호를 입력하세요: ");
				int instId = input.nextInt();
				input.nextLine();
				System.out.print("삭제할 날짜를 입력하세요 (예: 2025-06-01 00:00:00): ");
				String dateStr = input.nextLine();
				LocalDateTime date = LocalDateTime.parse(dateStr.replace(" ", "T"));
				deleteTracking(userId, instId, date);
				break;
			case 5:
				analyzeTracking();
				break;
			case 0:
				System.out.println("종료합니다.");
				return;
			default:
				System.out.println("잘못된 선택입니다.");
			}
		}
	}

	public static void getTracking(int userId) {
		String sql = "SELECT * FROM tracking WHERE userId = ? ORDER BY date ASC";

		try (Connection conn = DriverManager.getConnection(url, dbID, dbPW);
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, userId);
			ResultSet rs = pstmt.executeQuery();

			boolean hasData = false;
			System.out.println("\n===== 사용자 트래킹 전체 기록 =====");
			while (rs.next()) {
				hasData = true;
				System.out.printf("[날짜: %s] 기관ID: %d | 감정점수: %d | 수면시간: %d시간\n", rs.getTimestamp("date"),
						rs.getInt("institutionId"), rs.getInt("feeling"), rs.getInt("sleeping"));

				String exerciseName = rs.getString("exerciseName");
				double exerciseTime = rs.getDouble("exerciseTime");
				if (exerciseName != null && !exerciseName.isEmpty()) {
					System.out.printf("  → 운동: %s (%.1f시간)\n", exerciseName, exerciseTime);
				}

				String comment = rs.getString("comment");
				if (comment != null && !comment.isEmpty()) {
					System.out.printf("  → 한 마디: %s\n", comment);
				}
				System.out.println("-------------------------------------");
			}

			if (!hasData) {
				System.out.println("해당 사용자의 트래킹 기록이 없습니다.");
			}
		} catch (SQLException e) {
			System.out.println("DB 오류: " + e.getMessage());
		}
	}

	public static void registerTracking(int userId) {
		int sleeping;
		int feeling;
		String exerciseName = null;
		double exerciseTime = 0;
		String comment = null;
		LocalDateTime now = LocalDateTime.now();

		try {
			System.out.print("기관 번호를 입력하세요: ");
			int institutionId = input.nextInt();

			System.out.print("오늘의 수면시간을 적어주세요: ");
			sleeping = input.nextInt();

			System.out.print("오늘의 감정은 몇 점인가요 (1-100): ");
			feeling = input.nextInt();
			input.nextLine();

			System.out.print("오늘 운동을 하셨나요? (y/n): ");
			String check = input.nextLine();

			if (check.equalsIgnoreCase("y")) {
				System.out.print("하신 운동을 적어주세요: ");
				exerciseName = input.nextLine();
				System.out.print("운동 시간을 적어주세요: ");
				exerciseTime = input.nextDouble();
				input.nextLine();
			}

			System.out.print("등록하고 싶은 한 문장이 있으신가요? (y/n): ");
			check = input.nextLine();

			if (check.equalsIgnoreCase("y")) {
				System.out.print("오늘의 한 문장을 적어주세요: ");
				comment = input.nextLine();
			}

			String sql = "INSERT INTO tracking (userId, institutionId, date, feeling, sleeping, exerciseName, exerciseTime, comment) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

			try (Connection conn = DriverManager.getConnection(url, dbID, dbPW);
					PreparedStatement pstmt = conn.prepareStatement(sql)) {

				pstmt.setInt(1, userId);
				pstmt.setInt(2, institutionId);
				pstmt.setTimestamp(3, Timestamp.valueOf(now));
				pstmt.setInt(4, feeling);
				pstmt.setInt(5, sleeping);
				pstmt.setString(6, exerciseName);
				pstmt.setDouble(7, exerciseTime);
				pstmt.setString(8, comment);

				int result = pstmt.executeUpdate();
				System.out.println(result + "건의 트래킹 정보가 등록되었습니다.");
			}

		} catch (Exception e) {
			System.out.println("오류: " + e.getMessage());
		}
	}

	public static void updateTracking(int userId) {
		try {
			getTracking(userId);
			System.out.print("기관 번호를 입력하세요: ");
			int institutionId = input.nextInt();
			input.nextLine();

			System.out.print("변경할 날짜를 입력하세요 (예: 2025-06-01 00:00:00): ");
			String dateStr = input.nextLine();
			LocalDateTime date = LocalDateTime.parse(dateStr.replace(" ", "T"));

			System.out.print("새 감정 점수 (1-100): ");
			int feeling = input.nextInt();

			System.out.print("새 수면 시간: ");
			int sleeping = input.nextInt();
			input.nextLine();

			System.out.print("운동 이름 (없을 시, 엔터): ");
			String exerciseName = input.nextLine();
			double exerciseTime = 0;
			if (!(exerciseName.isEmpty())) {
				System.out.print("운동 시간: ");
				exerciseTime = input.nextDouble();
				input.nextLine();
			}

			System.out.print("한 문장 메모 (없을 시, 엔터): ");
			String comment = input.nextLine();

			String sql = "UPDATE tracking SET feeling = ?, sleeping = ?, exerciseName = ?, exerciseTime = ?, comment = ? "
					+ "WHERE userId = ? AND institutionId = ? AND date = ?";

			try (Connection conn = DriverManager.getConnection(url, dbID, dbPW);
					PreparedStatement pstmt = conn.prepareStatement(sql)) {

				pstmt.setInt(1, feeling);
				pstmt.setInt(2, sleeping);
				pstmt.setString(3, exerciseName);
				pstmt.setDouble(4, exerciseTime);
				pstmt.setString(5, comment);
				pstmt.setInt(6, userId);
				pstmt.setInt(7, institutionId);
				pstmt.setTimestamp(8, Timestamp.valueOf(date));

				int rows = pstmt.executeUpdate();
				if (rows > 0) {
					System.out.println("트래킹 정보가 성공적으로 수정되었습니다.");
				} else {
					System.out.println("수정할 트래킹 정보가 없습니다.");
				}
			}

		} catch (Exception e) {
			System.out.println("오류: " + e.getMessage());
		}
	}

	public static void deleteTracking(int userId, int institutionId, LocalDateTime date) {
		String sql = "DELETE FROM tracking WHERE userId = ? AND institutionId = ? AND date = ?";

		try (Connection conn = DriverManager.getConnection(url, dbID, dbPW);
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, userId);
			pstmt.setInt(2, institutionId);
			pstmt.setTimestamp(3, Timestamp.valueOf(date));

			int rows = pstmt.executeUpdate();
			if (rows > 0) {
				System.out.println("트래킹 정보가 성공적으로 삭제되었습니다.");
			} else {
				System.out.println("삭제할 트래킹 정보가 없습니다.");
			}

		} catch (SQLException e) {
			System.out.println("오류: " + e.getMessage());
		}
	}

	public static void analyzeTracking() {
		String sql = "SELECT userId, " +
	             "AVG(sleeping) AS avgSleeping, " +
	             "AVG(exerciseTime) AS avgExerciseTime " +
	             "FROM tracking " +
	             "GROUP BY userId WITH ROLLUP;";

	    try (Connection conn = DriverManager.getConnection(url, dbID, dbPW);
	         PreparedStatement pstmt = conn.prepareStatement(sql);
	         ResultSet rs = pstmt.executeQuery()) {

	        System.out.println("\n===== 사용자별 평균 수면시간 및 운동시간 (전체 평균 포함) =====");
	        System.out.printf("%-12s | %-15s | %-15s\n", "userId", "avgSleeping", "avgExerciseTime");
	        System.out.println("--------------------------------------------------------");

	        while (rs.next()) {
	            Object objUserId = rs.getObject("userId");
	            double avgSleep = rs.getDouble("avgSleeping");
	            double avgExercise = rs.getDouble("avgExerciseTime");

	            if (objUserId == null) {
	                System.out.println("========================================================");
	                System.out.printf("%-12s | %-15.2f | %-15.2f\n", "전체 평균", avgSleep, avgExercise);
	            } else {
	                int uid = (Integer) objUserId;
	                System.out.printf("%-12d | %-15.2f | %-15.2f\n", uid, avgSleep, avgExercise);
	            }
	        }

	    } catch (SQLException e) {
	        System.out.println("[ERROR] 데이터베이스 오류: " + e.getMessage());
	    }
	}

}
