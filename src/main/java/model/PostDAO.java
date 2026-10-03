package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import util.DBUtil;

/**
 * POSTS, POST_LIKES, SCRAPS 테이블을 다루는 DAO.
 * SQL을 실행하는 코드는 전부 여기에 모으고, Command는 이 메서드들을 호출만 한다.
 */
public class PostDAO {

	//목록/상세에서 공통으로 쓰는 컬럼. writer_nick은 members와 join해서 가져온다.
	private static final String COLUMNS =
			"p.bno, p.writer, m.nickname writer_nick, p.title, p.country, p.style, p.img_path, "
		  + "p.view_cnt, p.like_cnt, to_char(p.reg_date, 'YYYY-MM-DD') reg_date, "
		  + "to_char(p.mod_date, 'YYYY-MM-DD') mod_date ";

	private PostDTO toDto(ResultSet rs) throws Exception {

		PostDTO dto = new PostDTO();
		dto.setBno(rs.getInt("bno"));
		dto.setWriter(rs.getString("writer"));
		dto.setWriterNick(rs.getString("writer_nick"));
		dto.setTitle(rs.getString("title"));
		dto.setCountry(rs.getString("country"));
		dto.setStyle(rs.getString("style"));
		dto.setImgPath(rs.getString("img_path"));
		dto.setViewCnt(rs.getInt("view_cnt"));
		dto.setLikeCnt(rs.getInt("like_cnt"));
		dto.setRegDate(rs.getString("reg_date"));
		dto.setModDate(rs.getString("mod_date"));
		return dto;
	}

	//목록 화면 검색/필터 조건. 값이 "ALL"이거나 비어있으면 그 조건은 걸지 않는다.
	private String makeWhere(String country, String style, String keyword) {

		List<String> conditions = new ArrayList<String>();

		if (country != null && !country.isEmpty() && !"ALL".equals(country)) {
			conditions.add("p.country = ?");
		}
		if (style != null && !style.isEmpty() && !"ALL".equals(style)) {
			conditions.add("p.style = ?");
		}
		if (keyword != null && !keyword.trim().isEmpty()) {
			conditions.add("p.title like ?");
		}

		if (conditions.isEmpty()) {
			return "";
		}

		return "where " + String.join(" and ", conditions) + " ";
	}

	private int bindWhere(PreparedStatement pstmt, int idx, String country, String style, String keyword) throws Exception {

		if (country != null && !country.isEmpty() && !"ALL".equals(country)) {
			pstmt.setString(idx++, country);
		}
		if (style != null && !style.isEmpty() && !"ALL".equals(style)) {
			pstmt.setString(idx++, style);
		}
		if (keyword != null && !keyword.trim().isEmpty()) {
			pstmt.setString(idx++, "%" + keyword.trim() + "%");
		}

		return idx;
	}

	//게시글 목록 (국가/성향 필터 + 제목 검색, 정렬은 호출하는 쪽에서 orderBy로 지정)
	private List<PostDTO> getListOrderBy(String country, String style, String keyword, String orderBy) {

		String where = makeWhere(country, style, keyword);

		String sql = "select " + COLUMNS
				   + "from posts p join members m on p.writer = m.id "
				   + where
				   + "order by " + orderBy;

		List<PostDTO> list = new ArrayList<PostDTO>();

		try (Connection conn = DBUtil.getInstance();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {

			bindWhere(pstmt, 1, country, style, keyword);

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					list.add(toDto(rs));
				}
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return list;
	}

	//최신순 목록
	public List<PostDTO> getList(String country, String style, String keyword) {
		return getListOrderBy(country, style, keyword, "p.bno desc");
	}

	//인기순 목록 - 좋아요 많은 순, 같으면 조회수 많은 순, 같으면 최신순
	public List<PostDTO> getPopularList(String country, String style, String keyword) {
		return getListOrderBy(country, style, keyword, "p.like_cnt desc, p.view_cnt desc, p.bno desc");
	}

	//국가별 최신 n개 (메인 화면)
	public List<PostDTO> getLatestByCountry(String country, int n) {

		String sql = "select * from ( "
				   + "  select " + COLUMNS
				   + "  from posts p join members m on p.writer = m.id "
				   + "  where p.country = ? "
				   + "  order by p.bno desc "
				   + ") where rownum <= ?";

		List<PostDTO> list = new ArrayList<PostDTO>();

		try (Connection conn = DBUtil.getInstance();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, country);
			pstmt.setInt(2, n);

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					list.add(toDto(rs));
				}
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return list;
	}

	//특정 회원이 쓴 글 (마이페이지 - 내 글)
	public List<PostDTO> getListByWriter(String writer) {

		String sql = "select " + COLUMNS
				   + "from posts p join members m on p.writer = m.id "
				   + "where p.writer = ? order by p.bno desc";

		List<PostDTO> list = new ArrayList<PostDTO>();

		try (Connection conn = DBUtil.getInstance();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, writer);

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					list.add(toDto(rs));
				}
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return list;
	}

	//특정 회원이 스크랩한 글 (마이페이지 - 스크랩한 글, 최근 스크랩한 순)
	public List<PostDTO> getScrappedList(String memberId) {

		String sql = "select " + COLUMNS
				   + "from posts p join members m on p.writer = m.id "
				   + "join scraps s on p.bno = s.bno "
				   + "where s.member_id = ? order by s.scrap_date desc";

		List<PostDTO> list = new ArrayList<PostDTO>();

		try (Connection conn = DBUtil.getInstance();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, memberId);

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					list.add(toDto(rs));
				}
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return list;
	}

	//게시글 하나 (상세보기, 수정 화면) - 없는 번호면 null
	public PostDTO getPost(int bno) {

		String sql = "select " + COLUMNS + ", p.content "
				   + "from posts p join members m on p.writer = m.id "
				   + "where p.bno = ?";

		PostDTO dto = null;

		try (Connection conn = DBUtil.getInstance();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, bno);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					dto = toDto(rs);
					dto.setContent(rs.getString("content"));
				}
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return dto;
	}

	//조회수 1 증가
	public void increaseViewCnt(int bno) {

		String sql = "update posts set view_cnt = view_cnt + 1 where bno = ?";

		try (Connection conn = DBUtil.getInstance();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, bno);
			pstmt.executeUpdate();

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	//글쓰기 - 성공하면 새 글 번호, 실패하면 0
	//등록 직후 상세화면으로 이동하려면 번호를 알아야 하므로, 시퀀스에서 번호를 먼저 뽑고 그 번호로 insert 한다.
	public int insert(PostDTO dto) {

		String seqSql = "select seq_posts.nextval from dual";
		String sql = "insert into posts(bno, writer, title, content, country, style, img_path) "
				   + "values(?, ?, ?, ?, ?, ?, ?)";

		int bno = 0;

		try (Connection conn = DBUtil.getInstance();
			 PreparedStatement seqPstmt = conn.prepareStatement(seqSql);
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {

			try (ResultSet rs = seqPstmt.executeQuery()) {
				if (rs.next()) {
					bno = rs.getInt(1);
				}
			}

			pstmt.setInt(1, bno);
			pstmt.setString(2, dto.getWriter());   // 화면 값이 아니라 세션의 로그인 id여야 한다!
			pstmt.setString(3, dto.getTitle());
			pstmt.setString(4, dto.getContent());
			pstmt.setString(5, dto.getCountry());
			pstmt.setString(6, dto.getStyle());
			pstmt.setString(7, dto.getImgPath());

			if (pstmt.executeUpdate() == 0) {
				bno = 0;
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
			bno = 0;
		}

		return bno;
	}

	//글수정 - where에 writer까지 걸어서 "본인 글일 때만" 수정되게 한다 (관리자는 Command에서 별도 처리)
	public int update(PostDTO dto) {

		String sql = "update posts set title = ?, content = ?, country = ?, style = ?, img_path = ?, mod_date = sysdate "
				   + "where bno = ? and writer = ?";

		int result = 0;

		try (Connection conn = DBUtil.getInstance();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, dto.getTitle());
			pstmt.setString(2, dto.getContent());
			pstmt.setString(3, dto.getCountry());
			pstmt.setString(4, dto.getStyle());
			pstmt.setString(5, dto.getImgPath());
			pstmt.setInt(6, dto.getBno());
			pstmt.setString(7, dto.getWriter());
			result = pstmt.executeUpdate();

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return result;
	}

	//관리자가 남의 글을 수정할 때 쓰는 버전 - writer 조건 없이 bno만으로 수정한다.
	public int updateByAdmin(PostDTO dto) {

		String sql = "update posts set title = ?, content = ?, country = ?, style = ?, img_path = ?, mod_date = sysdate "
				   + "where bno = ?";

		int result = 0;

		try (Connection conn = DBUtil.getInstance();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, dto.getTitle());
			pstmt.setString(2, dto.getContent());
			pstmt.setString(3, dto.getCountry());
			pstmt.setString(4, dto.getStyle());
			pstmt.setString(5, dto.getImgPath());
			pstmt.setInt(6, dto.getBno());
			result = pstmt.executeUpdate();

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return result;
	}

	//글삭제 - 본인 글일 때만. 댓글/좋아요/스크랩은 FK의 ON DELETE CASCADE로 함께 지워진다.
	public int delete(int bno, String writer) {

		String sql = "delete from posts where bno = ? and writer = ?";

		int result = 0;

		try (Connection conn = DBUtil.getInstance();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, bno);
			pstmt.setString(2, writer);
			result = pstmt.executeUpdate();

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return result;
	}

	//관리자가 남의 글을 삭제할 때 쓰는 버전
	public int deleteByAdmin(int bno) {

		String sql = "delete from posts where bno = ?";

		int result = 0;

		try (Connection conn = DBUtil.getInstance();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, bno);
			result = pstmt.executeUpdate();

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return result;
	}


	/* ===================== 좋아요 (POST_LIKES) ===================== */

	//이 회원이 이 글에 좋아요를 눌렀는지
	public boolean isLiked(int bno, String memberId) {

		String sql = "select 1 from post_likes where bno = ? and member_id = ?";

		boolean liked = false;

		try (Connection conn = DBUtil.getInstance();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, bno);
			pstmt.setString(2, memberId);
			try (ResultSet rs = pstmt.executeQuery()) {
				liked = rs.next();
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return liked;
	}

	//좋아요 누르기/취소하기(토글) - 처리에 성공했으면 true
	//post_likes 변경 + posts.like_cnt 갱신, 두 작업이 반드시 같이 성공/실패해야 하므로 트랜잭션으로 묶는다.
	public boolean toggleLike(int bno, String memberId) {

		String checkSql  = "select 1 from post_likes where bno = ? and member_id = ?";
		String deleteSql = "delete from post_likes where bno = ? and member_id = ?";
		String insertSql = "insert into post_likes(bno, member_id) values(?, ?)";
		//like_cnt를 +1/-1 하지 않고 실제 개수를 다시 세서 넣는다 → 값이 어긋나 있어도 자동으로 맞춰진다.
		String cntSql    = "update posts set like_cnt = (select count(*) from post_likes where bno = ?) where bno = ?";

		boolean success = false;

		try (Connection conn = DBUtil.getInstance()) {

			conn.setAutoCommit(false);

			try {
				boolean exists;
				try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
					pstmt.setInt(1, bno);
					pstmt.setString(2, memberId);
					try (ResultSet rs = pstmt.executeQuery()) {
						exists = rs.next();
					}
				}

				try (PreparedStatement pstmt = conn.prepareStatement(exists ? deleteSql : insertSql)) {
					pstmt.setInt(1, bno);
					pstmt.setString(2, memberId);
					pstmt.executeUpdate();
				}

				try (PreparedStatement pstmt = conn.prepareStatement(cntSql)) {
					pstmt.setInt(1, bno);
					pstmt.setInt(2, bno);
					pstmt.executeUpdate();
				}

				conn.commit();
				success = true;

			} catch (Exception e) {
				conn.rollback();
				throw e;
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return success;
	}

	public int getLikeCnt(int bno) {

		String sql = "select like_cnt from posts where bno = ?";

		int cnt = 0;

		try (Connection conn = DBUtil.getInstance();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, bno);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					cnt = rs.getInt(1);
				}
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return cnt;
	}


	/* ===================== 스크랩 (SCRAPS) ===================== */

	public boolean isScrapped(int bno, String memberId) {

		String sql = "select 1 from scraps where bno = ? and member_id = ?";

		boolean scrapped = false;

		try (Connection conn = DBUtil.getInstance();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, bno);
			pstmt.setString(2, memberId);
			try (ResultSet rs = pstmt.executeQuery()) {
				scrapped = rs.next();
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return scrapped;
	}

	//스크랩 추가/해제(토글) - 좋아요와 달리 카운터 컬럼이 없어서 트랜잭션이 필요 없다.
	public boolean toggleScrap(int bno, String memberId) {

		boolean scrapped = isScrapped(bno, memberId);

		String sql = scrapped
			? "delete from scraps where bno = ? and member_id = ?"
			: "insert into scraps(bno, member_id) values(?, ?)";

		try (Connection conn = DBUtil.getInstance();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, bno);
			pstmt.setString(2, memberId);
			pstmt.executeUpdate();

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return !scrapped;
	}

}
