package service;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.MemberDTO;
import model.PostDAO;
import model.PostDTO;
import model.PostLogDAO;
import util.ParamUtil;
import util.UploadUtil;

/**
 * 게시글 삭제(POST /post/delete).
 * 댓글/좋아요/스크랩은 POSTS를 지우면 FK의 ON DELETE CASCADE로 Oracle이 알아서 함께 지운다
 * (DB-SCHEMA.md 참고). 업로드했던 사진 파일만 따로 지워준다.
 */
public class PostDeleteCommand implements Command {

	@Override
	public boolean doCommand(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		MemberDTO loginMember = (MemberDTO) request.getSession().getAttribute("loginMember");

		int bno = ParamUtil.toInt(request.getParameter("bno"), 0);

		PostDAO postDAO = new PostDAO();
		PostDTO post = postDAO.getPost(bno);

		if (post == null) {
			response.sendRedirect(request.getContextPath() + "/post/list");
			return false;
		}

		boolean isAdmin = loginMember.isAdmin();

		int deletedRows = isAdmin
			? postDAO.deleteByAdmin(bno)
			: postDAO.delete(bno, loginMember.getId());

		if (deletedRows == 0) {
			request.getSession().setAttribute("flashMsg", "권한이 없거나 이미 삭제된 게시글입니다.");
			response.sendRedirect(request.getContextPath() + "/post/list");
			return false;
		}

		UploadUtil.delete(post.getImgPath(), request.getServletContext());

		new PostLogDAO().insert("DELETE", bno, post.getTitle(), loginMember.getId());

		response.sendRedirect(request.getContextPath() + "/post/list");
		return false;
	}

}
