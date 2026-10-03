package service;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.MemberDTO;
import model.PostDAO;
import util.ParamUtil;

/**
 * 스크랩 추가/해제(Ajax, POST /post/scrap) - JSON으로 응답.
 * 응답 예: {"result":"ok","scrapped":true}
 */
public class ScrapCommand implements Command {

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

		int bno = ParamUtil.toInt(request.getParameter("bno"), 0);

		boolean scrapped = new PostDAO().toggleScrap(bno, loginMember.getId());

		out.print("{\"result\":\"ok\",\"scrapped\":" + scrapped + "}");
		return false;
	}

}
