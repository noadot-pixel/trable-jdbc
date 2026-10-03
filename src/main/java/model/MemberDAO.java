package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import util.DBUtil;

/**
 * MEMBERS 테이블을 다루는 DAO. SQL을 실행하는 코드는 전부 여기에 모은다.
 * ID 컬럼 = 로그인 아이디 = 이메일 (MemberDTO 설명 참고).
 */
public class MemberDAO {

	//BIRTHDAY는 날짜 계산을 할 일이 없는 단순 표시용 값이라 DATE가 아니라 VARCHAR2(10)로 저장한다
	//(그대로 "YYYY-MM-DD" 문자열로 넣고 꺼내면 돼서 TO_CHAR/TO_DATE 변환이 필요 없다)
	private static final String COLUMNS =
			"ID, PWD, NICKNAME, IS_ADMIN, TO_CHAR(JOIN_DATE, 'YYYY-MM-DD') JOIN_DATE, "
		  + "BIO, COUNTRY, COUNTRY_OTHER, REAL_NAME, NAME_PUBLIC, "
		  + "BIRTHDAY, BIRTHDAY_PUBLIC, PHOTO_PATH ";

	private MemberDTO toDto(ResultSet rs) throws Exception {

		MemberDTO dto = new MemberDTO();
		dto.setId(rs.getString("ID"));
		dto.setNickname(rs.getString("NICKNAME"));
		dto.setAdmin("Y".equals(rs.getString("IS_ADMIN")));
		dto.setJoinDate(rs.getString("JOIN_DATE"));
		dto.setBio(rs.getString("BIO"));
		dto.setCountry(rs.getString("COUNTRY"));
		dto.setCountryOther(rs.getString("COUNTRY_OTHER"));
		dto.setRealName(rs.getString("REAL_NAME"));
		dto.setNamePublic(!"N".equals(rs.getString("NAME_PUBLIC")));
		dto.setBirthday(rs.getString("BIRTHDAY"));
		dto.setBirthdayPublic(!"N".equals(rs.getString("BIRTHDAY_PUBLIC")));
		dto.setPhotoPath(rs.getString("PHOTO_PATH"));
		// PWD는 세션에 들고 다닐 필요가 없으므로 DTO에 담지 않는다.
		return dto;
	}

	//아이디(이메일) 중복 확인 - 이미 있으면 true
	public boolean idCheck(String id) {

		String sql = "select id from members where id = ?";

		boolean isDuplicate = false;

		try (Connection conn = DBUtil.getInstance();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, id);
			try (ResultSet rs = pstmt.executeQuery()) {
				isDuplicate = rs.next();
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return isDuplicate;
	}

	//회원가입 - 성공하면 1, 실패하면 0
	public int join(MemberDTO dto) {

		String sql = "insert into members(id, pwd, nickname) values(?, ?, ?)";
		//나머지 컬럼은 전부 DEFAULT(IS_ADMIN='N', JOIN_DATE=SYSDATE)거나 NULL 허용이라 안 넣어도 된다.

		int result = 0;

		try (Connection conn = DBUtil.getInstance();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, dto.getId());
			pstmt.setString(2, dto.getPwd());
			pstmt.setString(3, dto.getNickname());
			result = pstmt.executeUpdate();

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return result;
	}

	//로그인 - 아이디/비밀번호가 맞으면 전체 프로필을, 틀리면 null을 돌려준다.
	public MemberDTO login(String id, String pwd) {

		String sql = "select " + COLUMNS + "from members where id = ? and pwd = ?";

		MemberDTO dto = null;

		try (Connection conn = DBUtil.getInstance();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, id);
			pstmt.setString(2, pwd);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					dto = toDto(rs);
				}
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return dto;
	}

	//회원 하나 다시 조회 (프로필 저장 뒤 세션을 최신 값으로 다시 채울 때 사용)
	public MemberDTO getById(String id) {

		String sql = "select " + COLUMNS + "from members where id = ?";

		MemberDTO dto = null;

		try (Connection conn = DBUtil.getInstance();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, id);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					dto = toDto(rs);
				}
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return dto;
	}

	//프로필 수정 - 본인 계정만 수정하므로 where에 id까지 건다 (세션의 id만 써야 함, 화면 값 금지)
	public int update(MemberDTO dto) {

		String sql = "update members set "
				   + "nickname = ?, bio = ?, country = ?, country_other = ?, "
				   + "real_name = ?, name_public = ?, birthday = ?, birthday_public = ?, photo_path = ? "
				   + "where id = ?";

		int result = 0;

		try (Connection conn = DBUtil.getInstance();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, dto.getNickname());
			pstmt.setString(2, dto.getBio());
			pstmt.setString(3, dto.getCountry());
			pstmt.setString(4, dto.getCountryOther());
			pstmt.setString(5, dto.getRealName());
			pstmt.setString(6, dto.isNamePublic() ? "Y" : "N");
			//생일을 비워서 저장하면 null로 들어가도록 빈 문자열은 null로 바꾼다.
			String birthday = (dto.getBirthday() == null || dto.getBirthday().isEmpty()) ? null : dto.getBirthday();
			pstmt.setString(7, birthday);
			pstmt.setString(8, dto.isBirthdayPublic() ? "Y" : "N");
			pstmt.setString(9, dto.getPhotoPath());
			pstmt.setString(10, dto.getId());
			result = pstmt.executeUpdate();

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return result;
	}

}
