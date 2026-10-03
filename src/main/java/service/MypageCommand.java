package service;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.MemberDTO;
import model.PostDAO;
import model.PostDTO;

/**
 * 마이페이지(보기 전용, GET /member/mypage).
 * 세션에 이미 있는 프로필(loginMember)에 더해 내가 쓴 글 / 스크랩한 글 목록을 준비한다.
 */
public class MypageCommand implements Command {

	@Override
	public boolean doCommand(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		MemberDTO loginMember = (MemberDTO) request.getSession().getAttribute("loginMember");

		PostDAO postDAO = new PostDAO();

		List<PostDTO> myPosts = postDAO.getListByWriter(loginMember.getId());
		List<PostDTO> scrapPosts = postDAO.getScrappedList(loginMember.getId());

		request.setAttribute("myPosts", myPosts);
		request.setAttribute("scrapPosts", scrapPosts);

		return true;   // mypage.jsp로 forward
	}

}
