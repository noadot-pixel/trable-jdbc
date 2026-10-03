package service;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.MemberDTO;
import model.PostDAO;
import model.PostDTO;
import util.ParamUtil;

/**
 * 수정 화면 띄우기(GET /post/modify). 기존 글을 불러와 폼에 채운다.
 * 본인 글(또는 관리자)이 아니면 상세화면으로 돌려보낸다.
 */
public class PostModifyFormCommand implements Command {

	@Override
	public boolean doCommand(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		int bno = ParamUtil.toInt(request.getParameter("bno"), 0);

		PostDTO post = new PostDAO().getPost(bno);

		if (post == null) {
			response.sendRedirect(request.getContextPath() + "/post/list");
			return false;
		}

		MemberDTO loginMember = (MemberDTO) request.getSession().getAttribute("loginMember");

		if (!loginMember.getId().equals(post.getWriter()) && !loginMember.isAdmin()) {

			request.getSession().setAttribute("flashMsg", "본인이 작성한 게시글만 수정할 수 있습니다.");
			response.sendRedirect(request.getContextPath() + "/post/view?bno=" + bno);
			return false;
		}

		request.setAttribute("post", post);

		return true;   // posts/edit.jsp로 forward
	}

}
