import java.sql.*;

import java.util.ArrayList;

public class ReviewGUI {
	static final String dbID = "testuser";
	static final String dbPW = "testpw";
	static final String dbName = "mindlink";
	static final String header = "jdbc:mysql://localhost:3306/";
	static final String encoding = "useUnicode=true&characterEncoding=UTF-8";
	static final String url = header + dbName + "?" + encoding;

	// 1. 전체 리뷰 조회 (기관 평균 평점 및 순위 포함)
	public static Object[][] getAllReviews() {
		String sql = "WITH reviewWithAvg AS (" + "    SELECT reviewId, userId, institutionId, content, rating, "
				+ "           DATE_FORMAT(date, '%Y-%m-%d') AS formattedDate, "
				+ "           AVG(rating) OVER (PARTITION BY institutionId) AS institutionAvg " + "      FROM Reviews"
				+ ") " + "SELECT reviewId, userId, institutionId, content, rating, formattedDate, "
				+ "       institutionAvg, "
				+ "       DENSE_RANK() OVER (ORDER BY institutionAvg DESC) AS institutionRank "
				+ "  FROM reviewWithAvg " + "ORDER BY institutionAvg DESC, formattedDate DESC";
		try (Connection conn = DriverManager.getConnection(url, dbID, dbPW);
				PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {
			return resultSetToArray(rs);
		} catch (SQLException e) {
			handleSQLException("전체 리뷰 조회", e);
			return new Object[0][0];
		}
	}

	// 2. 리뷰 등록
	public static boolean createReview(int userId, int institutionId, String content, int rating) {
		if (!validateRating(rating) || content == null || content.trim().isEmpty()) {
			return false;
		}
		String sql = "INSERT INTO Reviews (userId, institutionId, content, rating, date) VALUES (?, ?, ?, ?, ?)";
		try (Connection conn = DriverManager.getConnection(url, dbID, dbPW);
				PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			pstmt.setInt(2, institutionId);
			pstmt.setString(3, content.trim());
			pstmt.setInt(4, rating);
			pstmt.setTimestamp(5, new java.sql.Timestamp(System.currentTimeMillis()));
			int result = pstmt.executeUpdate();
			return result > 0;
		} catch (SQLException e) {
			handleSQLException("리뷰 등록", e);
			return false;
		}
	}

	// 3. 리뷰 수정
	public static boolean updateReview(int reviewId, String newContent, int newRating) {
		if (!validateRating(newRating) || newContent == null || newContent.trim().isEmpty()) {
			return false;
		}
		String sql = "UPDATE Reviews SET content=?, rating=? WHERE reviewId=?";
		try (Connection conn = DriverManager.getConnection(url, dbID, dbPW);
				PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, newContent.trim());
			pstmt.setInt(2, newRating);
			pstmt.setInt(3, reviewId);
			int result = pstmt.executeUpdate();
			return result > 0;
		} catch (SQLException e) {
			handleSQLException("리뷰 수정", e);
			return false;
		}
	}

	// 4. 리뷰 삭제
	public static boolean deleteReview(int reviewId) {
		String sql = "DELETE FROM Reviews WHERE reviewId=?";
		try (Connection conn = DriverManager.getConnection(url, dbID, dbPW);
				PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, reviewId);
			int result = pstmt.executeUpdate();
			return result > 0;
		} catch (SQLException e) {
			handleSQLException("리뷰 삭제", e);
			return false;
		}
	}

	// 5. 기관별 리뷰 통계 조회
	public static Object[][] getInstitutionStats(int institutionId) {
		String sql = "SELECT i.name, " + "AVG(r.rating) OVER (PARTITION BY r.institutionId) AS institutionAvgRating, "
				+ "COUNT(*) OVER (PARTITION BY r.institutionId) AS reviewCount, "
				+ "r.institutionId, r.userId, r.reviewId, r.rating, r.date, r.content " + "FROM Reviews r "
				+ "JOIN Institution i ON r.institutionId = i.institutionId " + "WHERE r.institutionId = ? "
				+ "ORDER BY r.userId";
		try (Connection conn = DriverManager.getConnection(url, dbID, dbPW);
				PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, institutionId);
			try (ResultSet rs = pstmt.executeQuery()) {
				return resultSetToArray(rs);
			}
		} catch (SQLException e) {
			handleSQLException("기관별 리뷰 통계 조회", e);
			return new Object[0][0];
		}
	}

	// 6. 사용자별 리뷰 통계 조회
	public static Object[][] getUserStats(int userId) {
		String sql = "SELECT " + "  userId, institutionId, rating, content, " + "  COUNT(*) OVER ( "
				+ "    PARTITION BY userId " + "    ORDER BY institutionId, reviewId "
				+ "    ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW " + "  ) AS cumulative_count, "
				+ "  AVG(rating) OVER ( " + "    PARTITION BY userId " + "    ORDER BY institutionId, reviewId "
				+ "    ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW " + "  ) AS cumulative_avg " + "FROM Reviews "
				+ "WHERE userId = ? " + "ORDER BY institutionId ASC, reviewId ASC";

		try (Connection conn = DriverManager.getConnection(url, dbID, dbPW);
				PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			try (ResultSet rs = pstmt.executeQuery()) {
				return resultSetToArray(rs); // Now includes cumulative_count
			}
		} catch (SQLException e) {
			handleSQLException("사용자 리뷰 누적 통계 조회", e);
			return new Object[0][0];
		}
	}

	public static Object[][] getUserReview(int userId) {
		String sql = "SELECT r.reviewId, r.institutionId, r.rating, r.content " 
				+ "FROM Reviews r "
				+ "WHERE r.userId = ?";
		try (Connection conn = DriverManager.getConnection(url, dbID, dbPW);
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			try (ResultSet rs = pstmt.executeQuery()) {
				return resultSetToArray(rs);
			}
		} catch (SQLException e) {
			handleSQLException("사용자별 리뷰 조회", e);
			return new Object[0][0];
		}
	}

	// 평점 검증 (1~5)
	private static boolean validateRating(int rating) {
		return rating >= 1 && rating <= 5;
	}

	// ResultSet을 2차원 배열로 변환
	private static Object[][] resultSetToArray(ResultSet rs) throws SQLException {
		ArrayList<Object[]> rows = new ArrayList<>();
		ResultSetMetaData meta = rs.getMetaData();
		int colCount = meta.getColumnCount();
		while (rs.next()) {
			Object[] row = new Object[colCount];
			for (int i = 0; i < colCount; i++) {
				row[i] = rs.getObject(i + 1);
			}
			rows.add(row);
		}
		return rows.toArray(new Object[0][0]);
	}

	// 예외 처리
	private static void handleSQLException(String operation, SQLException e) {
		System.err.println(operation + " 실패: " + e.getMessage());
	}
}
