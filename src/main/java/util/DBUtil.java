package util;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * DBM(DB 연결 관리). 모든 DAO가 이 클래스 하나만 거쳐서 Connection을 얻는다.
 *
 * 비밀번호는 소스에 적어두지 않고 환경변수(TRABLE_DB_PASSWORD)에서 읽는다.
 * - 공개 저장소에 올라가는 파일에 실제 비밀번호를 적으면 안 되기 때문이다.
 * - Tomcat을 띄우기 전에 그 환경변수를 설정해두면 된다. 예(PowerShell):
 *     $env:TRABLE_DB_PASSWORD = "본인이 만든 비밀번호"
 *   (Eclipse에서 돌릴 때도 같은 방식으로 OS 환경변수를 설정해두면 된다.)
 *
 * URL/계정명은 본인 Oracle 환경에 맞게 고칠 것 (기본값은 XE, 로그인 아이디 trable).
 */
public class DBUtil {

	public static Connection getInstance() {

		Connection conn = null;
		String driver = "oracle.jdbc.driver.OracleDriver";
		String url = "jdbc:oracle:thin:@localhost:1521:xe";
		String id = "trable";
		String pw = System.getenv("TRABLE_DB_PASSWORD");

		if (pw == null || pw.isEmpty()) {
			System.out.println("[DB 연결 실패] 환경변수 TRABLE_DB_PASSWORD가 설정되지 않았습니다.");
			return null;
		}

		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url, id, pw);
		} catch (Exception e) {
			System.out.println("[DB 연결 실패] " + e.getMessage());
		}

		return conn;
	}
}
