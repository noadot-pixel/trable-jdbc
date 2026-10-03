package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import util.DBUtil;

/**
 * POST_LOGS 테이블을 다루는 DAO. 게시글 작성/수정/삭제 때마다 한 건씩 쌓인다.
 */
public class PostLogDAO {

	public int insert(String logType, int bno, String postTitle, String actorId) {

		String seqSql = "select seq_post_logs.nextval from dual";
		String sql = "insert into post_logs(log_id, log_type, bno, post_title, actor_id) values(?, ?, ?, ?, ?)";

		int logId = 0;

		try (Connection conn = DBUtil.getInstance();
			 PreparedStatement seqPstmt = conn.prepareStatement(seqSql);
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {

			try (ResultSet rs = seqPstmt.executeQuery()) {
				if (rs.next()) {
					logId = rs.getInt(1);
				}
			}

			pstmt.setInt(1, logId);
			pstmt.setString(2, logType);
			pstmt.setInt(3, bno);
			pstmt.setString(4, postTitle);
			pstmt.setString(5, actorId);

			if (pstmt.executeUpdate() == 0) {
				logId = 0;
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
			logId = 0;
		}

		return logId;
	}

	//최신순 전체 목록 (관리자 화면). 검색/필터는 지금은 화면에서 자바스크립트 없이
	//서버가 넘겨준 전체 목록을 그대로 보여주고, 필터 적용은 다음 단계에서 쿼리 파라미터로 확장 가능.
	public List<PostLogDTO> getList() {

		String sql = "select l.log_id, l.log_type, l.bno, l.post_title, l.actor_id, m.nickname actor_nick, "
				   + "to_char(l.log_date, 'YYYY-MM-DD HH24:MI') log_date "
				   + "from post_logs l join members m on l.actor_id = m.id "
				   + "order by l.log_id desc";

		List<PostLogDTO> list = new ArrayList<PostLogDTO>();

		try (Connection conn = DBUtil.getInstance();
			 PreparedStatement pstmt = conn.prepareStatement(sql);
			 ResultSet rs = pstmt.executeQuery()) {

			while (rs.next()) {

				PostLogDTO dto = new PostLogDTO();
				dto.setLogId(rs.getInt("log_id"));
				dto.setLogType(rs.getString("log_type"));
				dto.setBno(rs.getInt("bno"));
				dto.setPostTitle(rs.getString("post_title"));
				dto.setActorId(rs.getString("actor_id"));
				dto.setActorNick(rs.getString("actor_nick"));
				dto.setLogDate(rs.getString("log_date"));
				list.add(dto);
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return list;
	}

}
