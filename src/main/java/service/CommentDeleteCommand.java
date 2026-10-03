package service;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.CommentDAO;
import model.CommentDTO;
import model.MemberDTO;
import model.PostDAO;
import model.PostDTO;
import util.ParamUtil;

/**
 * 댓글 삭제(Ajax, POST /comment/delete) - JSON으로 응답.
 * 삭제 권한: 댓글 작성자 본인 / 게시글 작성자 / 관리자 중 하나.
 * COMMENTS와 POSTS는 FK만으로는 "이 댓글이 달린 글의 작성자"까지 검사할 수 없어서
 * (좋아요처럼 where 조건 하나로 끝나지 않음) 이 Command에서 직접 Java로 비교한다.
 */
public class CommentDeleteCommand implements Command {

	@Override
	public boolean doCommand(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("application/json; charset=utf-8");
		PrintWriter out = response.getWriter();

		MemberDTO loginMember = (MemberDTO) request.getSession().getAttribute("loginMember");

		if (loginMember == null) {
			out.print("{\"result\":\"login\"}");
			return false;
		}

		int cno = ParamUtil.toInt(request.getParameter("cno"), 0);

		CommentDAO commentDAO = new CommentDAO();
		CommentDTO comment = commentDAO.getById(cno);

		if (comment == null) {
			out.print("{\"result\":\"fail\"}");
			return false;
		}

		PostDTO post = new PostDAO().getPost(comment.getBno());

		boolean isCommentWriter = loginMember.getId().equals(comment.getWriter());
		boolean isPostWriter = post != null && loginMember.getId().equals(post.getWriter());

		if (!isCommentWriter && !isPostWriter && !loginMember.isAdmin()) {
			out.print("{\"result\":\"forbidden\"}");
			return false;
		}

		commentDAO.delete(cno);

		int commentCnt = commentDAO.countByPost(comment.getBno());

		out.print("{\"result\":\"ok\",\"commentCnt\":" + commentCnt + "}");
		return false;
	}

}
