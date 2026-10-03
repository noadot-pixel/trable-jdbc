package service;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.CommentDAO;
import model.MemberDTO;
import util.EscapeUtil;
import util.ParamUtil;

/**
 * 댓글 등록(Ajax, POST /comment/write) - JSON으로 응답.
 * 성공 응답 예: {"result":"ok","cno":12,"writer":"a@b.com","writerNick":"닉네임","content":"...","regDate":"2026-10-03 10:20"}
 */
public class CommentWriteCommand implements Command {

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
		String content = request.getParameter("content");

		if (content == null || content.trim().isEmpty()) {
			out.print("{\"result\":\"empty\"}");
			return false;
		}

		if (content.length() > 500) {
			out.print("{\"result\":\"toolong\"}");
			return false;
		}

		CommentDAO dao = new CommentDAO();
		int cno = dao.insert(bno, loginMember.getId(), content.trim());

		if (cno == 0) {
			out.print("{\"result\":\"fail\"}");
			return false;
		}

		String json = "{\"result\":\"ok\","
				+ "\"cno\":" + cno + ","
				+ "\"writer\":\"" + EscapeUtil.json(loginMember.getId()) + "\","
				+ "\"writerNick\":\"" + EscapeUtil.json(loginMember.getNickname()) + "\","
				+ "\"content\":\"" + EscapeUtil.json(content.trim()) + "\","
				+ "\"commentCnt\":" + dao.countByPost(bno)
				+ "}";

		out.print(json);
		return false;
	}

}
