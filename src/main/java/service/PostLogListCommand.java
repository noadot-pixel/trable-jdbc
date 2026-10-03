package service;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.PostLogDAO;

/**
 * 게시글 로그 목록(GET /admin/postLogs). 관리자만 접근 가능 (AdminController에서 검사).
 */
public class PostLogListCommand implements Command {

	@Override
	public boolean doCommand(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setAttribute("logList", new PostLogDAO().getList());

		return true;   // admin/post-logs.jsp로 forward
	}

}
