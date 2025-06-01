import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.Scanner;

public class InstitutionReview {
	static final String dbID = "testuser";
	static final String dbPW = "testpw";
	static final String dbName = "mindlink";
	static final String header = "jdbc:mysql://localhost:3306/";
	static final String encoding = "useUnicode=true&characterEncoding=UTF-8";
	static final String url = header + dbName + "?" + encoding;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n==== 리뷰 관리 시스템 ====");
            System.out.println("1. 리뷰 전체 조회 (기관 평균 평점 및 순위 포함)");
            System.out.println("2. 리뷰 등록");
            System.out.println("3. 리뷰 수정");
            System.out.println("4. 리뷰 삭제");
            System.out.println("5. 기관별 리뷰 통계 조회 ");
            System.out.println("6. 사용자별 리뷰 통계 조회");
            System.out.println("0. 종료");
            System.out.print("선택: ");
            int menuChoice = scanner.nextInt();
            scanner.nextLine();

            switch (menuChoice) {
                case 1: selectAllReviews(); break;
                case 2: createReview(scanner); break;
                case 3: updateReview(scanner); break;
                case 4: deleteReview(scanner); break;
                case 5: selectByInstitutionGroup(scanner); break;
                case 6: selectByUserGroup(scanner); break;
                case 0:
                    System.out.println("종료합니다.");
                    scanner.close();
                    return;
                default:
                    System.out.println("잘못된 선택입니다.");
            }
        }
    }

    // 평점 검증
  //평점은 정수로만 받았습니다
    private static boolean validateRating(int rating) {
        return rating >= 1 && rating <= 5;
    }

    // 1. 전체 조회 랭크 넣는건 여기가 나을 거 같아서 윈도우 OLAP 사용했습니다.
    static void selectAllReviews() {
        String sql =
            "WITH reviewWithAvg AS (" +
            "    SELECT reviewId, userId, institutionId, content, rating, " +
            "           DATE_FORMAT(date, '%Y-%m-%d') AS formattedDate, " +
            "           AVG(rating) OVER (PARTITION BY institutionId) AS institutionAvg " +
            "      FROM Reviews" +
            ") " +
            "SELECT reviewId, userId, institutionId, content, rating, formattedDate, " +
            "       institutionAvg, " +
            "       RANK() OVER (ORDER BY institutionAvg DESC) AS institutionRank " +
            "  FROM reviewWithAvg " +
            "ORDER BY institutionAvg DESC, formattedDate DESC";

        try (Connection conn = DriverManager.getConnection(url, dbID, dbPW);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            printReviewResultsWithWindow(rs, "[전체 리뷰 + 기관평균평점/순위]");
        } catch (SQLException e) {
            handleSQLException("조회", e);
        }
    }

    // 2. 등록
    static void createReview(Scanner scanner) {
        try {
            System.out.print("유저ID: ");
            BigDecimal userId = scanner.nextBigDecimal();
            System.out.print("기관ID: ");
            int institutionId = scanner.nextInt();
            scanner.nextLine();
            System.out.print("리뷰 내용: ");
            String content = scanner.nextLine().trim();
            System.out.print("평점(1~5): ");
            int rating = scanner.nextInt();

            if (!validateRating(rating)) {
                System.out.println("평점은 1~5 사이 정수로 골라주세요.");
                return;
            }
            if (content.isEmpty()) {
                System.out.println("리뷰 내용을 입력해주세요.");
                return;
            }

            String sql = "INSERT INTO Reviews (userId, institutionId, content, rating) VALUES (?, ?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(url, dbID, dbPW);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setBigDecimal(1, userId);
                pstmt.setInt(2, institutionId);
                pstmt.setString(3, content);
                pstmt.setInt(4, rating);
                int result = pstmt.executeUpdate();
                System.out.println(result + "건 등록 완료!");
            } catch (SQLException e) {
                handleSQLException("등록", e);
            }
        } catch (Exception e) {
            handleInputException(e);
        }
    }

    // 3. 수정(UPDATE)
    static void updateReview(Scanner scanner) {
        try {
            System.out.print("수정할 리뷰번호(reviewId): ");
            BigDecimal reviewId = scanner.nextBigDecimal();
            scanner.nextLine();
            System.out.print("새 내용: ");
            String newContent = scanner.nextLine().trim();
            System.out.print("새 평점(1~5): ");
            int newRating = scanner.nextInt();
            if (!validateRating(newRating)) {
                System.out.println("평점은 1~5 사이여야 합니다.");
                return;
            }
            if (newContent.isEmpty()) {
                System.out.println("리뷰 내용을 입력해주세요.");
                return;
            }
            String sql = "UPDATE Reviews SET content=?, rating=? WHERE reviewId=?";
            try (Connection conn = DriverManager.getConnection(url, dbID, dbPW);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, newContent);
                pstmt.setInt(2, newRating);
                pstmt.setBigDecimal(3, reviewId);
                int result = pstmt.executeUpdate();
                System.out.println(result > 0 ? "수정이 완료되었습니다!" : "해당 리뷰가 없습니다.");
            } catch (SQLException e) {
                handleSQLException("수정", e);
            }
        } catch (Exception e) {
            handleInputException(e);
        }
    }

    // 4. 삭제
    static void deleteReview(Scanner scanner) {
        try {
            System.out.print("삭제할 리뷰번호(reviewId): ");
            BigDecimal reviewId = scanner.nextBigDecimal();

            String sql = "DELETE FROM Reviews WHERE reviewId=?";
            try (Connection conn = DriverManager.getConnection(url, dbID, dbPW);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setBigDecimal(1, reviewId);
                int result = pstmt.executeUpdate();
                System.out.println(result > 0 ? "삭제가 정상적으로 처리되었습니다." : "해당 리뷰가 없습니다.");
            } catch (SQLException e) {
                handleSQLException("삭제", e);
            }
        } catch (Exception e) {
            handleInputException(e);
        }
    }

    // 5. 기관별 리뷰 통계 조회 (GROUP BY/HAVING)
    static void selectByInstitutionGroup(Scanner scanner) {
        System.out.print("기관 ID를 입력하세요: ");
        int institutionId = scanner.nextInt();

        String sql = 
            "SELECT i.name, " +
            "AVG(r.rating) OVER (PARTITION BY r.institutionId) AS institutionAvgRating, " +
            "COUNT(*) OVER (PARTITION BY r.institutionId) AS reviewCount, " +
            "r.institutionId, r.userId, r.reviewId, r.rating, r.date, r.content " +
            "FROM Reviews r " +
            "JOIN Institution i ON r.institutionId = i.institutionId " +
            "WHERE r.institutionId = ? " +
            "ORDER BY r.userId";

        try (Connection conn = DriverManager.getConnection(url, dbID, dbPW);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, institutionId);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n[기관별 사용자 리뷰 통계]");

            boolean hasData = false;

            if (rs.next()) {
                hasData = true;

                System.out.printf("기관명: %s\t| 기관 ID: %d\t| 리뷰 수: %d개\t| 평균평점: %.2f\n", 
                        rs.getString("name"),
                        rs.getInt("institutionId"),
                        rs.getInt("reviewCount"),
                        rs.getDouble("institutionAvgRating"));

                System.out.printf("→ 사용자:%d\t| 평점:%d\t\t| 내용: %s\n",
                        rs.getInt("userId"),
                        rs.getInt("rating"),
                        rs.getString("content"));

                while (rs.next()) {
                    System.out.printf("→ 사용자:%d\t| 평점:%d\t\t| 내용: %s\n",
                            rs.getInt("userId"),
                            rs.getInt("rating"),
                            rs.getString("content"));
                }
            }

            if (!hasData) {
                System.out.println("해당 기관에 대한 리뷰가 없습니다.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            handleInputException(e);
        }
    }


    // 6. 사용자별 리뷰 통계 조회 (GROUP BY/HAVING)
    static void selectByUserGroup(Scanner scanner) {
        try {
            System.out.print("사용자ID: ");
            BigDecimal userId = scanner.nextBigDecimal();
            String sql =
            	    "SELECT r.userId, r.institutionId, (SELECT COUNT(*) FROM Reviews r3 WHERE r3.userId = r.userId) AS ReviewCount, AVG(rating), " +
            	    "       (SELECT AVG(r2.rating) FROM Reviews r2 WHERE r2.userId = r.userId) AS userAvgRating " +
            	    " FROM Reviews r " +
            	    "WHERE r.userId = ? " +
            	    "GROUP BY r.userId, r.institutionId " +
            	    "HAVING COUNT(*) >= 1 ";
            try (Connection conn = DriverManager.getConnection(url, dbID, dbPW);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setBigDecimal(1, userId);
                ResultSet rs = pstmt.executeQuery();
                System.out.println("\n[사용자별 기관 리뷰 통계]");

        	    boolean hasData = false;

        	    if (rs.next()) {
        	        hasData = true;

        	        System.out.printf("사용자:%d\t| 리뷰수:%d\t| 평균평점:%.2f\n",
        	                rs.getInt("userId"),
        	                rs.getInt("reviewCount"),
        	                rs.getDouble("userAvgRating"));
        	        System.out.printf("→ 기관:%d\t\t| 사용자 평점:%d\n",
    	                    rs.getInt("institutionId"),
    	                    rs.getInt("AVG(rating)"));
        	        while (rs.next()) {
        	            System.out.printf("→ 기관:%d\t\t| 사용자 평점:%d\n",
        	            		rs.getInt("institutionId"),
        	                    rs.getInt("AVG(rating)"));
        	        }
        	    }

        	    if (!hasData) {
        	        System.out.println("해당 사용자가 등록한 리뷰가 없습니다.");
        	    }
            } catch (SQLException e) {
                handleSQLException("사용자별 통계 조회", e);
            }
        } catch (Exception e) {
            handleInputException(e);
        }
    }


    // 공통 출력 부분
    private static void printReviewResultsWithWindow(ResultSet rs, String title) throws SQLException {
        System.out.println("\n" + title);
        boolean hasData = false;
        while (rs.next()) {
            hasData = true;
            System.out.printf("번호:%d | 유저:%d | 기관:%d | 평점:%d | 내용:%s | 작성일:%s | 기관평균:%.2f | 기관순위:%d\n",
                rs.getBigDecimal("reviewId").intValue(),
                rs.getBigDecimal("userId").intValue(),
                rs.getInt("institutionId"),
                rs.getInt("rating"),
                rs.getString("content"),
                rs.getString("formattedDate"),
                rs.getDouble("institutionAvg"),
                rs.getInt("institutionRank"));
        }
        if (!hasData) {
            System.out.println("조회 결과가 없습니다.");
        }
    }

    // 예외 처리 메소드들
    private static void handleSQLException(String operation, SQLException e) {
        System.out.println(operation + " 작업 실패: " + e.getMessage());
    }

    private static void handleInputException(Exception e) {
        System.out.println("입력 오류: " + e.getMessage());
    }
}
