package service;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.PostDAO;

/**
 * 메인 화면(GET /main). 국가별 최신 글 3개씩을 준비한다.
 */
public class MainCommand implements Command {

	@Override
	public boolean doCommand(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		PostDAO dao = new PostDAO();

		request.setAttribute("koreaPosts", dao.getLatestByCountry("KR", 3));
		request.setAttribute("japanPosts", dao.getLatestByCountry("JP", 3));
		request.setAttribute("worldPosts", dao.getLatestByCountry("ETC", 3));

		return true;   // index.jsp로 forward
	}

}
