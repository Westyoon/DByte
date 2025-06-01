// 상담 기록 기능 구현을 위한 class 파일입니다. 
import java.sql.*;
import java.util.Scanner;

/*통합 DB 완성하면 변수 수정해야 할 것 같습니다 ㅜ DB에는 user_id라고 저장했어요..
 * 
 * 90000 그리고 date 추가
 * */

public class AppointmentRecord {
	static final String dbID = "testuser";
	static final String dbPW = "testpw";
	static final String dbName = "mindlink";
	static final String header = "jdbc:mysql://localhost:3306/";
	static final String encoding = "useUnicode=true&characterEncoding=UTF-8";
	static final String url = header + dbName + "?" + encoding;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("\n당신의 userId를 입력하세요 (종료하려면 0 입력): ");
            int userId = scanner.nextInt();
            scanner.nextLine();

            if (userId == 0) {
                System.out.println("종료합니다.");
                break;
            }

            if (userId < 20000) {
                showPatientConsultations(userId);
            } else {
                showDoctorMenu(userId, scanner);
            }
        }

        scanner.close();
    }

    // 환자: 본인 상담 기록 조회 (PatientView: CREATE VIEW PatientView AS SELECT * FROM Appointment_record; 활용)
    static void showPatientConsultations(int patientId) {
        String sql = "SELECT * FROM PatientView WHERE userId = ?";
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
            if (!hasData) System.out.println("상담 기록이 없습니다.");

        } catch (SQLException e) {
            System.out.println("오류: " + e.getMessage());
        }
    }

    // 의료인: DoctorView 사용해 기관 기록 관리
    /*CREATE VIEW DoctorView AS
	SELECT ar.*
	FROM Appointment_record ar
	JOIN Medical m ON ar.institution_id = m.institution_id;*/
    static void showDoctorMenu(int doctorId, Scanner scanner) {
        try (Connection conn = DriverManager.getConnection(url, dbID, dbPW)) {
            int institutionId = getInstitutionIdByDoctor(conn, doctorId);

            if (institutionId == -1) {
                System.out.println("의료인 정보가 없습니다.");
                return;
            }

            while (true) {
                System.out.println("\n[의료진 상담 관리 메뉴]");
                System.out.println("1. 상담 기록 전체 조회");
                System.out.println("2. 상담 기록 수정");
                System.out.println("3. 상담 기록 삭제");
                System.out.println("0. 이전으로");
                System.out.print("선택: ");
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1: showConsultationsByInstitution(conn, institutionId); break;
                    case 2: updateConsultation(conn, scanner); break;
                    case 3: deleteConsultation(conn, scanner); break;
                    case 0: return;
                    default: System.out.println("잘못된 입력입니다.");
                }
            }

        } catch (SQLException e) {
            System.out.println("DB 오류: " + e.getMessage());
        }
    }
    //의사 user_id로 기관 id 조회
    static int getInstitutionIdByDoctor(Connection conn, int doctorId) throws SQLException {
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

    // 기관 내 모든 상담 기록 조회(DoctorView로 제한)
    static void showConsultationsByInstitution(Connection conn, int institutionId) throws SQLException {
        String sql = "SELECT * FROM DoctorView WHERE institutionId = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, institutionId);
            ResultSet rs = pstmt.executeQuery();
            System.out.println("\n[기관 상담 기록]");
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                printConsultation(rs);
            }
            if (!hasData) System.out.println("상담 기록이 없습니다.");
        }
    }
    //상담 기록 수정(처방할 때 알레르기 충돌 검사 포함)
    static void updateConsultation(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("수정할 appointmentId: ");
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
    //처방 약물 - 알레르기 충돌 검사
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
    //상담 기록 삭제
    static void deleteConsultation(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("삭제할 appointmentId: ");
        int id = scanner.nextInt();
        String sql = "DELETE FROM Appointment_record WHERE appointmentId = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int result = pstmt.executeUpdate();
            System.out.println(result > 0 ? "삭제 완료" : "해당 상담 기록이 없습니다.");
        }
    }

    //상담 기록 출력
    static void printConsultation(ResultSet rs) throws SQLException {
        System.out.printf("(%d) 환자ID: %d\t| 기관ID: %d\n처방: %s\t\t| 진단: %s\n내용: %s\n\n",
                rs.getInt("appointmentId"),
                rs.getInt("userId"),
                rs.getInt("institutionId"),
                rs.getString("prescription"),
                rs.getString("diagnosis"),
                rs.getString("record")
        );
    }
}
