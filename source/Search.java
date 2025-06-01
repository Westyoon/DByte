import java.sql.*;
import java.util.Scanner;

public class Search {
	static final String dbID = "testuser";
	static final String dbPW = "testpw";
	static final String dbName = "mindlink";
	static final String header = "jdbc:mysql://localhost:3306/";
	static final String encoding = "useUnicode=true&characterEncoding=UTF-8";
	static final String url = header + dbName + "?" + encoding;
	
	//전체 기관 목록 조회 | 사용자는 기관 이름, 주소, 전화번호만 조회 가능
	static void showInstitution() {		
		String sql = "select name, address, tel from institution";

		try (Connection conn = DriverManager.getConnection(url, dbID, dbPW)) {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet myResSet = pstmt.executeQuery();
			
			System.out.println("전체 기관 목록");
			while (myResSet.next()) {
				String name = myResSet.getString("name");
				String address = myResSet.getString("address");
				String tel = myResSet.getString("tel");
			
				System.out.println(name + " | " + address + " | " + tel);
			}
		} catch (SQLException e) {
			System.out.println("기관 조회 실패 " + e.getMessage());
		}
	}

	//지역별 기관 목록 조회
	static void showInstitutionByDistrict() {
		Scanner scan = new Scanner(System.in);
		System.out.print("조회하고 싶은 구를 입력 (마포구, 서대문구, 은평구, 종로구): ");
		String district = scan.nextLine();
		
		String sql = "select name, address, tel from institution where district = ?";

		try (Connection conn = DriverManager.getConnection(url, dbID, dbPW)) {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, district);
			ResultSet myResSet = pstmt.executeQuery();
			
			System.out.println(district + " 기관 목록");
			while (myResSet.next()) {
				String name = myResSet.getString("name");
				String address = myResSet.getString("address");
				String tel = myResSet.getString("tel");
			
				System.out.println(name + " | " + address + " | " + tel);
			}
		} catch (SQLException e) {
			System.out.println(district + " 기관 조회 실패 " + e.getMessage());
		}
	}
	
	//기관 타입별 기관 목록 조회
	static void showInstitutionByType() {
		Scanner scan = new Scanner(System.in);
		System.out.print("기관 타입(공공/민간): ");
		String type = scan.nextLine();
		
		String sql = "select name, address, tel from institution where institutionType = ?";

		try (Connection conn = DriverManager.getConnection(url, dbID, dbPW)) {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, type);
			ResultSet myResSet = pstmt.executeQuery();
			
			System.out.println(type + " 기관 목록");
			while (myResSet.next()) {
				String name = myResSet.getString("name");
				String address = myResSet.getString("address");
				String tel = myResSet.getString("tel");
			
				System.out.println(name + " | " + address + " | " + tel);
			}
		} catch (SQLException e) {
			System.out.println("기관 조회 실패 " + e.getMessage());
		}
	}
	
	//기관의 평균 평점이 전체 기관의 평균 평점보다 높은 기관 목록 조회
	//평점이 있는 기관만 조회 가능한 쿼리라 데이터셋이 조금 애매함..
	//현실에서도 평점이 있는 기관만 조회 가능한 걸 고려해서 쿼리를 짰기 때문에 데이터셋에 추가 데이터 필요
	static void showInstitutionByRating() {		
		String sql = "select i.name, i.address, i.tel, avg(r.rating) as avgRating "
				+ "from institution i join reviews r on i.institutionId = r.institutionId "
				+ "group by i.institutionId, i.name, i.institutionType, i.city, i.district, i.address, i.tel "
				+ "having avg(r.rating) > (select avg(rating) from reviews) "
				+ "order by avgRating desc";

		try (Connection conn = DriverManager.getConnection(url, dbID, dbPW)) {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet myResSet = pstmt.executeQuery();
			
			System.out.println("평균 평점이 전체 평균 평점보다 높은 기관 목록");
			while (myResSet.next()) {
				String name = myResSet.getString("name");
				String address = myResSet.getString("address");
				String tel = myResSet.getString("tel");
				double rating = myResSet.getDouble("avgRating");
			
				System.out.printf("%s | %s | %s | %.1f\n", name, address, tel, rating);
			}
		} catch (SQLException e) {
			System.out.println("평점으로 기관 조회 실패" + e.getMessage());
		}
	}
	
	static void showMenu() {		
		System.out.println("==== 기관 조회 ====");
		System.out.println("1. 전체 기관 조회");
		System.out.println("2. 기관 타입별 조회");
		System.out.println("3. 지역별 기관 조회");
		System.out.println("4. 평균 평점보다 높은 기관 조회");
		System.out.println("0. 시스템 종료");
	}
	
	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);
				
		while(true) {
			showMenu();
			System.out.print("메뉴 선택: ");
			int choice = scan.nextInt();
			scan.nextLine();
			
			switch(choice) {
				case 1: showInstitution(); break;
				case 2: showInstitutionByType(); break;
				case 3: showInstitutionByDistrict(); break;
				case 4: showInstitutionByRating(); break;
				case 0:
					System.out.println("시스템 종료");
					scan.close();
					System.exit(0);
				default: System.out.println("없는 메뉴"); continue;
			}
		}
	}
}
