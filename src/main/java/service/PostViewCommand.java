package service;

import java.io.IOException;
import java.util.List;

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
 * 게시글 상세(GET /post/view).
 */
public class PostViewCommand implements Command {

	@Override
	public boolean doCommand(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		int bno = ParamUtil.toInt(request.getParameter("bno"), 0);

		PostDAO postDAO = new PostDAO();
		PostDTO post = postDAO.getPost(bno);

		if (post == null) {
			response.sendRedirect(request.getContextPath() + "/post/list");
			return false;
		}

		postDAO.increaseViewCnt(bno);
		post.setViewCnt(post.getViewCnt() + 1);   // 화면에 바로 +1 된 값이 보이도록

		MemberDTO loginMember = (MemberDTO) request.getSession().getAttribute("loginMember");

		boolean liked = false;
		boolean scrapped = false;

		if (loginMember != null) {
			liked = postDAO.isLiked(bno, loginMember.getId());
			scrapped = postDAO.isScrapped(bno, loginMember.getId());
		}

		List<CommentDTO> commentList = new CommentDAO().getList(bno);

		request.setAttribute("post", post);
		request.setAttribute("liked", liked);
		request.setAttribute("scrapped", scrapped);
		request.setAttribute("commentList", commentList);

		return true;   // posts/detail.jsp로 forward
	}

}
