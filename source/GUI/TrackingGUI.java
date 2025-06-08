import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;

public class TrackingGUI {
	static final String dbID = "testuser";
	static final String dbPW = "testpw";
	static final String dbName = "mindlink";
	static final String header = "jdbc:mysql://localhost:3306/";
	static final String encoding = "useUnicode=true&characterEncoding=UTF-8";
	static final String url = header + dbName + "?" + encoding;

	public static Scanner input = new Scanner(System.in);

	private static Object[][] getResult(String sql, Object[] params, int colCount) {
		try (Connection conn = DriverManager.getConnection(url, dbID, dbPW)) {
			PreparedStatement pstmt = conn.prepareStatement(sql);

			if (params != null) {
				for (int i = 0; i < params.length; i++) {
					pstmt.setObject(i + 1, params[i]);
				}
			}
			ResultSet rs = pstmt.executeQuery();

			ArrayList<Object[]> rows = new ArrayList<>();
			while (rs.next()) {
				Object[] row = new Object[colCount];
				for (int i = 0; i < colCount; i++) {
					if (colCount == 4 && i == 3) {
						row[i] = String.format("%.1f", rs.getDouble(i + 1));
					} else {
						row[i] = rs.getObject(i + 1);
					}
				}
				rows.add(row);
			}

			return rows.toArray(new Object[0][0]);

		} catch (SQLException e) {
			e.printStackTrace();
			return new Object[0][0];
		}
	}

	public static Object[][] getTracking(int userId) {
		String sql = "SELECT * FROM tracking WHERE userId = ? ORDER BY date ASC";
		return getResult(sql, new Object[] { userId }, 9);
	}

	public static boolean registerTracking(int userId, int institutionId, int sleeping, int feeling,
			String exerciseName, Double exerciseTime, String comment) {
		LocalDateTime now = LocalDateTime.now();
		String sql = "INSERT INTO tracking (userId, institutionId, date, feeling, sleeping, exerciseName, exerciseTime, comment) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

		try (Connection conn = DriverManager.getConnection(url, dbID, dbPW);
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, userId);
			pstmt.setInt(2, institutionId);
			pstmt.setTimestamp(3, Timestamp.valueOf(now));
			pstmt.setInt(4, feeling);
			pstmt.setInt(5, sleeping);

			if (exerciseName == null || exerciseName.isEmpty()) {
				pstmt.setNull(6, Types.VARCHAR);
			} else {
				pstmt.setString(6, exerciseName);
			}

			if (exerciseTime == null) {
				pstmt.setNull(7, Types.DOUBLE);
			} else {
				pstmt.setDouble(7, exerciseTime);
			}

			if (comment == null || comment.isEmpty()) {
				pstmt.setNull(8, Types.VARCHAR);
			} else {
				pstmt.setString(8, comment);
			}

			int result = pstmt.executeUpdate();
			return result > 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean updateTracking(int userId, int tId, int feeling, int sleeping,
			String exerciseName, Double exerciseTime, String comment) {
		String sql = "UPDATE tracking SET feeling = ?, sleeping = ?, exerciseName = ?, exerciseTime = ?, comment = ? "
				+ "WHERE userId = ? AND trackingId = ?";

		try (Connection conn = DriverManager.getConnection(url, dbID, dbPW);
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, feeling);
			pstmt.setInt(2, sleeping);

			if (exerciseName == null || exerciseName.isEmpty()) {
				pstmt.setNull(3, Types.VARCHAR);
			} else {
				pstmt.setString(3, exerciseName);
			}

			if (exerciseTime == null) {
				pstmt.setNull(4, Types.DOUBLE);
			} else {
				pstmt.setDouble(4, exerciseTime);
			}

			if (comment == null || comment.isEmpty()) {
				pstmt.setNull(5, Types.VARCHAR);
			} else {
				pstmt.setString(5, comment);
			}

			pstmt.setInt(6, userId);
			pstmt.setInt(7, tId);
			
			return pstmt.executeUpdate() > 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean deleteTracking(int userId, int tId) {
		String sql = "DELETE FROM tracking WHERE userId = ? AND trackingId = ?";

		try (Connection conn = DriverManager.getConnection(url, dbID, dbPW);
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, userId);
			pstmt.setInt(2, tId);

			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static Object[][] analyzeTracking() {
	    String sql =
	        "SELECT " +
	        "  CASE WHEN GROUPING(userId) = 1 THEN '전체' ELSE CAST(userId AS CHAR) END AS userLabel, " +
	        "  ROUND(AVG(sleeping), 2) AS avgSleeping, " +
	        "  ROUND(AVG(exerciseTime), 2) AS avgExerciseTime, " +
	        "  SUM(sleeping) AS totalSleeping, " +
	        "  SUM(exerciseTime) AS totalExerciseTime, " +
	        "  GROUPING(userId) AS isTotal " +  // 정렬용 컬럼
	        "FROM tracking " +
	        "GROUP BY userId WITH ROLLUP " +
	        "ORDER BY isTotal ASC";  // 일반 사용자 → 전체 (isTotal=1)

	    // 열 수가 5개니까 count = 5
	    return getResult(sql, null, 5);
	}


}
