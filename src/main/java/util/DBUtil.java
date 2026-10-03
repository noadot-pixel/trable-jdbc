package util;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * DBM(DB 연결 관리). 모든 DAO가 이 클래스 하나만 거쳐서 Connection을 얻는다.
 *
 * TODO: URL/ID/PASSWORD를 본인 Oracle 환경에 맞게 수정할 것.
 * (이 값들을 실제로 외부에 공개된 계정/비밀번호로 채우지 말 것 — 로컬 전용 값만 사용)
 */
public class DBUtil {

	public static Connection getInstance() {

		Connection conn = null;
		String driver = "oracle.jdbc.driver.OracleDriver";
		String url = "jdbc:oracle:thin:@localhost:1521:xe";
		String id = "trable";
		String pw = "CHANGE_ME";

		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url, id, pw);
		} catch (Exception e) {
			System.out.println("[DB 연결 실패] " + e.getMessage());
		}

		return conn;
	}
}
