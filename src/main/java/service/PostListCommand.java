package service;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.PostDAO;
import model.PostDTO;

/**
 * 게시글 목록(GET /post/list). 국가/성향 필터 + 제목 검색 + 정렬(최신순/인기순)을
 * 전부 쿼리 파라미터로 받아 SQL에서 처리한다.
 */
public class PostListCommand implements Command {

	@Override
	public boolean doCommand(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String country = request.getParameter("country");
		String style = request.getParameter("style");
		String keyword = request.getParameter("keyword");
		String sort = request.getParameter("sort");   // "latest"(기본) 또는 "popular"

		if (country == null) country = "ALL";
		if (style == null) style = "ALL";
		if (keyword == null) keyword = "";

		PostDAO dao = new PostDAO();

		List<PostDTO> list = "popular".equals(sort)
			? dao.getPopularList(country, style, keyword)
			: dao.getList(country, style, keyword);

		request.setAttribute("list", list);
		request.setAttribute("country", country);
		request.setAttribute("style", style);
		request.setAttribute("keyword", keyword);
		request.setAttribute("sort", sort == null ? "latest" : sort);

		return true;   // posts.jsp로 forward
	}

}
