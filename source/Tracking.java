// 수정된 Tracking.java 콘솔 프로그램
package console;

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
                select = Integer.parseInt(input.nextLine());
            } catch (Exception e) {
                System.out.println("숫자를 입력해주세요.");
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
                    deleteTracking(userId);
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
                System.out.printf("[ID: %d | 날짜: %s] 기관ID: %d | 감정점수: %d | 수면시간: %d시간\n",
                        rs.getInt("trackingId"), rs.getTimestamp("date"),
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
        try {
            System.out.print("기관 번호를 입력하세요: ");
            int institutionId = Integer.parseInt(input.nextLine());

            System.out.print("오늘의 수면시간을 적어주세요: ");
            int sleeping = Integer.parseInt(input.nextLine());

            System.out.print("오늘의 감정은 몇 점인가요 (1-100): ");
            int feeling = Integer.parseInt(input.nextLine());

            String exerciseName = null;
            Double exerciseTime = null;
            String comment = null;

            System.out.print("오늘 운동을 하셨나요? (y/n): ");
            if (input.nextLine().equalsIgnoreCase("y")) {
                System.out.print("하신 운동을 적어주세요: ");
                exerciseName = input.nextLine();
                System.out.print("운동 시간을 적어주세요: ");
                exerciseTime = Double.parseDouble(input.nextLine());
            }

            System.out.print("등록하고 싶은 한 문장이 있으신가요? (y/n): ");
            if (input.nextLine().equalsIgnoreCase("y")) {
                System.out.print("오늘의 한 문장을 적어주세요: ");
                comment = input.nextLine();
            }

            String sql = "INSERT INTO tracking (userId, institutionId, date, feeling, sleeping, exerciseName, exerciseTime, comment) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = DriverManager.getConnection(url, dbID, dbPW);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, userId);
                pstmt.setInt(2, institutionId);
                pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setInt(4, feeling);
                pstmt.setInt(5, sleeping);
                pstmt.setString(6, exerciseName);
                if (exerciseTime != null) pstmt.setDouble(7, exerciseTime); else pstmt.setNull(7, Types.DOUBLE);
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
            System.out.print("변경할 트래킹 ID를 입력하세요: ");
            int trackingId = Integer.parseInt(input.nextLine());

            System.out.print("새 감정 점수 (1-100): ");
            int feeling = Integer.parseInt(input.nextLine());

            System.out.print("새 수면 시간: ");
            int sleeping = Integer.parseInt(input.nextLine());

            System.out.print("운동 이름 (없으면 엔터): ");
            String exerciseName = input.nextLine();

            Double exerciseTime = null;
            if (!exerciseName.isEmpty()) {
                System.out.print("운동 시간: ");
                exerciseTime = Double.parseDouble(input.nextLine());
            }

            System.out.print("한 문장 메모 (없으면 엔터): ");
            String comment = input.nextLine();

            String sql = "UPDATE tracking SET feeling = ?, sleeping = ?, exerciseName = ?, exerciseTime = ?, comment = ? " +
                    "WHERE userId = ? AND trackingId = ?";

            try (Connection conn = DriverManager.getConnection(url, dbID, dbPW);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, feeling);
                pstmt.setInt(2, sleeping);
                pstmt.setString(3, exerciseName);
                if (exerciseTime != null) pstmt.setDouble(4, exerciseTime); else pstmt.setNull(4, Types.DOUBLE);
                pstmt.setString(5, comment);
                pstmt.setInt(6, userId);
                pstmt.setInt(7, trackingId);

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

    public static void deleteTracking(int userId) {
        try {
            getTracking(userId);
            System.out.print("삭제할 트래킹 ID를 입력하세요: ");
            int trackingId = Integer.parseInt(input.nextLine());

            String sql = "DELETE FROM tracking WHERE userId = ? AND trackingId = ?";

            try (Connection conn = DriverManager.getConnection(url, dbID, dbPW);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, userId);
                pstmt.setInt(2, trackingId);

                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    System.out.println("트래킹 정보가 성공적으로 삭제되었습니다.");
                } else {
                    System.out.println("삭제할 트래킹 정보가 없습니다.");
                }
            }
        } catch (Exception e) {
            System.out.println("오류: " + e.getMessage());
        }
    }

    public static void analyzeTracking() {
        String sql = "SELECT " +
                "  CASE WHEN GROUPING(institutionId) = 1 THEN '전체' ELSE CAST(institutionId AS CHAR) END AS institutionLabel, " +
                "  CASE WHEN GROUPING(userId) = 1 THEN '--' ELSE CAST(userId AS CHAR) END AS userLabel, " +
                "  ROUND(AVG(sleeping), 2) AS avgSleeping, " +
                "  ROUND(AVG(exerciseTime), 2) AS avgExerciseTime, " +
                "  SUM(sleeping) AS totalSleeping, " +
                "  SUM(exerciseTime) AS totalExerciseTime " +
                "FROM tracking " +
                "GROUP BY ROLLUP(institutionId, userId) " +
                "ORDER BY institutionLabel ASC, userLabel ASC";

        try (Connection conn = DriverManager.getConnection(url, dbID, dbPW);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("\n===== 기관 및 사용자별 평균 수면시간 및 운동시간 =====");
            System.out.printf("%-12s | %-12s | %-15s | %-15s\n", "기관", "사용자", "평균 수면시간", "평균 운동시간");
            System.out.println("--------------------------------------------------------------------------");

            while (rs.next()) {
                String institutionLabel = rs.getString("institutionLabel");
                String userLabel = rs.getString("userLabel");
                double avgSleep = rs.getDouble("avgSleeping");
                double avgExercise = rs.getDouble("avgExerciseTime");

                System.out.printf("%-12s | %-12s | %-15.2f | %-15.2f\n", institutionLabel, userLabel, avgSleep, avgExercise);
            }

        } catch (SQLException e) {
            System.out.println("[ERROR] 데이터베이스 오류: " + e.getMessage());
        }
    }
}
