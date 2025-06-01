import java.util.Scanner;

public class Main {
	static final String dbID = "testuser";
	static final String dbPW = "testpw";
	static final String dbName = "mindlink";
	static final String header = "jdbc:mysql://localhost:3306/";
	static final String encoding = "useUnicode=true&characterEncoding=UTF-8";
	static final String url = header + dbName + "?" + encoding;

	public static Scanner input = new Scanner(System.in);
	static int userId = -1;

	public static void login() {
		while (true) {
			System.out.print("[SYSTEM] 당신의 userId를 입력하여 로그인하세요. (종료 시 0) : ");
			userId = input.nextInt();
			input.nextLine();

			if (userId > 10000)
				break;
			else if (userId == 0) {
				System.out.println("[SYSTEM] 로그인을 종료합니다.");
				break;
			}
		}
	}

	public static void main(String[] args) {
		login();
		while (userId != -1) {
			int console = -1;
			while (true) {
				try {
					console = -1;
					System.out.println("\n[SYSTEM] 안녕하세요, 여러분의 마음 지킴을 위한 Mindlink 입니다.");
					System.out.println("[SYSTEM] 어떤 메뉴를 사용하실 건가요?");
					System.out.println("===============================================================");
					System.out.println("[SYSTEM] 1. 검색 2. 리뷰 3. 상담 기록 4. 일일 습관 기록 (트래킹) 0. 로그아웃");
					console = input.nextInt();

					switch (console) {
					case 0:
						System.out.printf("[SYSTEM] 해당 계정(%d)을 로그아웃합니다....\n\n", userId);
						userId = -1;
						break;
					case 1:
						System.out.println("[SYSTEM] 검색을 실행합니다.");
						Search.startSearch();
						continue;
					case 2:
						System.out.println("[SYSTEM] 리뷰를 실행합니다.");
						InstitutionReview.startInstitutionReview(userId);
						continue;
					case 3:
						System.out.println("[SYSTEM] 상담 기록을 실행합니다.");
						AppointmentRecord.startAppointmentRecord(userId);
						continue;
					case 4:
						System.out.println("[SYSTEM] 일일 습관 기록 (트래킹)을 실행합니다.");
						Tracking.startTracking(userId);
						continue;
					}

					if (console > 4 || console < 0) {
						throw new IllegalArgumentException("[ERROR] 잘못된 값입니다. 다시 시도해주세요.");
					}
					break;

				} catch (IllegalArgumentException e) {
					System.out.println(e.getMessage());
				} catch (Exception e) {
					System.out.println("[ERROR] 숫자를 입력해주세요.");
					input.nextLine();
				}

			}
			int loginCheck = 0;
			while (true) {
				try {
					System.out.print("[SYSTEM] 다른 계정으로 로그인하시겠습니까? 1. 예 2. 아니오: ");
					loginCheck = input.nextInt();

					if (loginCheck != 1 && loginCheck != 2) {
						throw new IllegalArgumentException("[ERROR] 잘못된 값입니다. 다시 시도해주세요.");
					}
					break;

				} catch (IllegalArgumentException e) {
					System.out.println(e.getMessage());
				} catch (Exception e) {
					System.out.println("[ERROR] 숫자를 입력해주세요.");
					input.nextLine();
				}
				System.out.println("[SYSTEM] 다른 계정으로 로그인하실 건가요? 1. 예 2. 아니오");

			}
			if (loginCheck == 1) {
				userId = -1;
				login();
				continue;
			} else {
				System.out.println("====================================");
				System.out.println("[SYSTEM] Mindlink 시스템을 종료합니다....");
				System.out.println("[SYSTEM] Mindlink를 이용해주셔서 감사합니다!");
				break;
			}
		}

	}
}
