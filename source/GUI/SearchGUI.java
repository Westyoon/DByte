import java.sql.*;
import java.util.ArrayList;


public class SearchGUI {
	static final String userID = "testuser";
	static final String userPW = "testpw";
	static final String dbName = "mindlink";
	static final String header = "jdbc:mysql://localhost:3306/";
	static final String encoding = "useUnicode=true&characterEncoding=UTF-8";
	static final String url = header + dbName + "?" + encoding;
	
	private static Object[][] getResult(String sql, Object[] params, int colCount) {
		try (Connection conn = DriverManager.getConnection(url, userID, userPW)) {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			
			if (params != null) {
				for (int i = 0; i < params.length; i++) {
					pstmt.setObject(i+1, params[i]);
				}
			}
			ResultSet rs = pstmt.executeQuery();
			
			ArrayList<Object[]> rows = new ArrayList<>();
			while(rs.next()) {
				Object[] row = new Object[colCount];
				for (int i = 0; i < colCount; i++) {
					if (colCount == 4 && i == 3) {
						row[i] = String.format("%.1f", rs.getDouble(i+1));
					} else {
						row[i] = rs.getObject(i+1);
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
	
	//전체 기관 목록 조회 | 사용자는 기관 이름, 주소, 전화번호만 조회 가능
	public static Object[][] getAllInstitution() {
		String sql = "select name, address, tel from institution";
		return getResult(sql, null, 3);
	}
	
	//지역별 기관 목록 조회
	public static Object[][] getInstitutionByDistrict(String district) {
		String sql = "select name, address, tel from institution where district = ?";
		return getResult(sql, new Object[] {district}, 3);
	}
	
	//기관 타입별 기관 목록 조회
	public static Object[][] getInstitutionByType(String type) {
		String sql = "select name, address, tel from institution where institutionType = ?";
		return getResult(sql, new Object[] {type}, 3);
	}
	
	//기관의 평균 평점이 전체 기관의 평균 평점보다 높은 기관 목록 조회	
	public static Object[][] getInstitutionByRating() {
		String sql = "select i.name, i.address, i.tel, avg(r.rating) as avg_rating "
				+ "from institution i join reviews r on i.institutionId = r.institutionId "
				+ "group by i.institutionId, i.name, i.institutionId, i.city, i.district, i.address, i.tel "
				+ "having avg(r.rating) > (select avg(rating) from reviews) "
				+ "order by avg_rating desc";
		return getResult(sql, null, 4);
	}
	
}
