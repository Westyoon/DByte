//등록할 때 알레르기 검사가 적용이 안되어서 알레르기 검사를 기존에는 기록id로 했는데 환자id로 변경해서 검사하도록 했습니다!!
package view;

import java.sql.*;
import java.util.ArrayList;

public class AppointmentRecordsql {
    static final String dbID = "testuser";
    static final String dbPW = "testpw";
    static final String dbName = "mindlink";
    static final String url = "jdbc:mysql://localhost:3306/" + dbName + "?useUnicode=true&characterEncoding=UTF-8";

    public static Object[][] getRecordsByUser(int userId) {
        String sql;
        boolean isDoctor = userId >= 20000;
        if (isDoctor) {
            sql = "SELECT * FROM DoctorView WHERE institutionId = (SELECT institutionId FROM Medical WHERE userId = ?) ORDER BY recordDate DESC";
        } else {
            sql = "SELECT * FROM PatientView WHERE userId = ? ORDER BY recordDate DESC";
        }
        try (Connection conn = DriverManager.getConnection(url, dbID, dbPW);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            ArrayList<Object[]> list = new ArrayList<>();
            while (rs.next()) {
                list.add(new Object[]{
                        rs.getInt("appointmentId"),
                        rs.getInt("userId"),
                        rs.getInt("institutionId"),
                        rs.getDate("recordDate"),
                        rs.getString("prescription"),
                        rs.getString("diagnosis"),
                        rs.getString("record")
                });
            }
            return list.toArray(new Object[0][]);
        } catch (SQLException e) {
            e.printStackTrace();
            return new Object[0][0];
        }
    }

    public static boolean insertRecord(int doctorId, int patientId, String prescription, String diagnosis, String record) {
        try (Connection conn = DriverManager.getConnection(url, dbID, dbPW)) {
        	
        	if (hasAllergyConflict(conn, patientId, prescription)) { // <-- 알레르기 충돌 검사 추가
                return false; // 충돌 발생 시 false 반환
            }
        	
        	int instId = getInstitutionId(conn, doctorId);
            if (instId == -1) return false;

            String sql = "INSERT INTO AppointmentRecord (userId, institutionId, prescription, diagnosis, recordDate, record) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, patientId);
                pstmt.setInt(2, instId);
                pstmt.setString(3, prescription);
                pstmt.setString(4, diagnosis);
                pstmt.setDate(5, new java.sql.Date(System.currentTimeMillis()));
                pstmt.setString(6, record);
                return pstmt.executeUpdate() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateRecord(int appointmentId, String diagnosis, String prescription, String record) {
        try (Connection conn = DriverManager.getConnection(url, dbID, dbPW)) {
            int patientId = getPatientIdFromAppointment (conn, appointmentId); // <-- 기록id로 유저id 가져오도록
        	if (patientId == -1 || hasAllergyConflict(conn, patientId, prescription)) return false; // <-- 알레르기 충돌 검사 수정

            String sql = "UPDATE AppointmentRecord SET diagnosis = ?, prescription = ?, record = ? WHERE appointmentId = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, diagnosis);
                pstmt.setString(2, prescription);
                pstmt.setString(3, record);
                pstmt.setInt(4, appointmentId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteRecord(int appointmentId) {
        try (Connection conn = DriverManager.getConnection(url, dbID, dbPW)) {
            String sql = "DELETE FROM AppointmentRecord WHERE appointmentId = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, appointmentId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Object[][] getTrackingRecords(int doctorId, int patientId) {
        try (Connection conn = DriverManager.getConnection(url, dbID, dbPW)) {
            int doctorInstId = getInstitutionId(conn, doctorId);
            int patientInstId = getPatientInstitutionId(conn, patientId);

            if (doctorInstId != patientInstId || patientInstId == -1) return null;

            String sql = "SELECT * FROM Tracking WHERE userId = ? ORDER BY date ASC";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, patientId);
                ResultSet rs = pstmt.executeQuery();
                ArrayList<Object[]> list = new ArrayList<>();
                while (rs.next()) {
                    String exerciseName = rs.getString("exerciseName");
                    list.add(new Object[]{
                            rs.getTimestamp("date"),
                            rs.getInt("institutionId"),
                            rs.getInt("feeling"),
                            rs.getInt("sleeping"),
                            (exerciseName == null || exerciseName.isBlank()) ? "" : exerciseName,
                            (exerciseName == null || exerciseName.isBlank()) ? "" : rs.getDouble("exerciseTime"),
                            rs.getString("comment")
                    });
                }
                return list.toArray(new Object[0][]);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static boolean hasAllergyConflict(Connection conn, int patientId, String prescription) throws SQLException { // <-- 알레르기 충돌 검사 수정
        String sql = "SELECT allergy FROM Patient WHERE userId = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String allergy = rs.getString("allergy");
                return allergy != null 
                        && !allergy.trim().isEmpty()
                        && prescription != null
                        && allergy.toLowerCase().contains(prescription.toLowerCase().trim());
            }
        }
        return false;
    }

    private static int getInstitutionId(Connection conn, int doctorId) throws SQLException {
        String sql = "SELECT institutionId FROM Medical WHERE userId = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, doctorId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("institutionId");
        }
        return -1;
    }

    private static int getPatientInstitutionId(Connection conn, int patientId) throws SQLException {
        String sql = "SELECT institutionId FROM Tracking WHERE userId = ? LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("institutionId");
        }
        return -1;
    }
    
    private static int getPatientIdFromAppointment(Connection conn, int appointmentId) throws SQLException { // <-- 기록id로 환자id 가져오는 메소드 추가
        String sql = "SELECT userId FROM AppointmentRecord WHERE appointmentId = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, appointmentId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt("userId") : -1;
        }
    }
}
