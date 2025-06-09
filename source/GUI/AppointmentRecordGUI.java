import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class AppointmentRecordGUI {
    static final String dbID = "testuser";
    static final String dbPW = "testpw";
    static final String dbName = "mindlink";
    static final String url = "jdbc:mysql://localhost:3306/" + dbName + "?useUnicode=true&characterEncoding=UTF-8";
    

    public static Object[][] getRecordsByUser(int userId) {
        String sql;
        boolean isDoctor = userId >= 20000;
        if (isDoctor) {
            sql = "SELECT DISTINCT * FROM DoctorView WHERE institutionId = (SELECT institutionId FROM Medical WHERE userId = ?) ORDER BY recordDate ASC;";
        } else {
            sql = "SELECT * FROM PatientView WHERE userId = ? ORDER BY recordDate ASC";
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
        	if (hasAllergyConflict(patientId, prescription)) {
                return false;
            }
        	
        	int instId = getInstitutionId(doctorId);
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
            int patientId = getPatientIdFromAppointment (appointmentId);
        	if (patientId == -1 || hasAllergyConflict(patientId, prescription)) return false;

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
            int doctorInstId = getInstitutionId(doctorId);
            int patientInstId = getPatientInstitutionId(patientId);

            if (doctorInstId != patientInstId || patientInstId == -1) return null;

            String sql = "SELECT * FROM Tracking WHERE userId = ? ORDER BY date DESC";
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
                            (exerciseName == null || exerciseName != null) ? "" : exerciseName,
                            (exerciseName == null || exerciseName != null) ? "" : rs.getDouble("exerciseTime"),
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

    private static boolean hasAllergyConflict(int patientId, String prescription) throws SQLException {
        String sql = "SELECT allergy FROM Patient WHERE userId = ?";
        Connection conn = DriverManager.getConnection(url, dbID, dbPW);
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

    private static int getInstitutionId(int doctorId) throws SQLException {
        String sql = "SELECT institutionId FROM Medical WHERE userId = ?";
        Connection conn = DriverManager.getConnection(url, dbID, dbPW);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, doctorId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("institutionId");
        }
        return -1;
    }

    private static int getPatientInstitutionId(int patientId) throws SQLException {
        String sql = "SELECT institutionId FROM Tracking WHERE userId = ? ORDER BY date DESC LIMIT 1";
        Connection conn = DriverManager.getConnection(url, dbID, dbPW);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("institutionId");
        }
        return -1;
    }
    
    private static int getPatientIdFromAppointment(int appointmentId) throws SQLException {
    	Connection conn = DriverManager.getConnection(url, dbID, dbPW);
    	String sql = "SELECT userId FROM AppointmentRecord WHERE appointmentId = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, appointmentId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt("userId") : -1;
        }
    }
}
