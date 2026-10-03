package controller;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.MemberDTO;
import service.Command;
import service.CommentDeleteCommand;
import service.CommentWriteCommand;
import service.LikeCommand;
import service.MainCommand;
import service.PostDeleteCommand;
import service.PostListCommand;
import service.PostModifyCommand;
import service.PostModifyFormCommand;
import service.PostViewCommand;
import service.PostWriteCommand;
import service.ScrapCommand;

/**
 * 게시글/댓글/좋아요/스크랩 관련 요청을 한 서블릿이 받아서 getServletPath()로 구분해 처리한다.
 * "/post/*"로 하면 /posts.jsp 같은 화면 파일로 forward하는 요청까지 다시 가로채므로
 * 주소를 하나씩 지정한다.
 */
@WebServlet({"/main",
			 "/post/list", "/post/view", "/post/write", "/post/modify", "/post/delete",
			 "/post/like", "/post/scrap", "/comment/write", "/comment/delete"})
@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 10 * 1024 * 1024)
//글쓰기/수정 폼이 파일을 보내는 multipart/form-data 형식이라 이 설정이 있어야 getPart()/getParameter()를 쓸 수 있다.
//파일 하나 최대 5MB, 요청 전체 최대 10MB.
public class PostController extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doAction(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doAction(request, response);
	}

	protected void doAction(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");

		String path = request.getServletPath();
		String method = request.getMethod();
		String page = null;
		Command cmd = null;

		MemberDTO loginMember = (MemberDTO) request.getSession().getAttribute("loginMember");

		//로그인해야만 쓸 수 있는 화면(글쓰기/수정/삭제)은 로그인 화면으로 돌려보낸다.
		//좋아요/스크랩/댓글은 Ajax라 redirect가 소용없어서, 각 Command 안에서 JSON으로 알려준다.
		boolean needsLogin = "/post/write".equals(path) || "/post/modify".equals(path) || "/post/delete".equals(path);

		if (needsLogin && loginMember == null) {
			response.sendRedirect(request.getContextPath() + "/member/login");
			return;
		}

		switch (path) {
			case "/main": {
				cmd = new MainCommand();
				page = "/index.jsp";
				break;
			}
			case "/post/list": {
				cmd = new PostListCommand();
				page = "/posts.jsp";
				break;
			}
			case "/post/view": {
				cmd = new PostViewCommand();
				page = "/posts/detail.jsp";
				break;
			}
			case "/post/write": {
				page = "/posts/write.jsp";               // GET: 글쓰기 화면 / POST 실패 시: 다시 글쓰기 화면
				if ("POST".equals(method)) {
					cmd = new PostWriteCommand();
				}
				break;
			}
			case "/post/modify": {
				page = "/posts/edit.jsp";
				if ("POST".equals(method)) {
					cmd = new PostModifyCommand();       // 수정 저장
				} else {
					cmd = new PostModifyFormCommand();   // 기존 글 불러와서 수정 화면
				}
				break;
			}
			case "/post/delete": {
				//삭제는 주소창 입력/링크만으로 실행되지 않도록 POST일 때만 처리
				if ("POST".equals(method)) {
					cmd = new PostDeleteCommand();
				} else {
					response.sendRedirect(request.getContextPath() + "/post/list");
					return;
				}
				break;
			}
			case "/post/like": {                         // Ajax - JSON 응답
				cmd = new LikeCommand();
				break;
			}
			case "/post/scrap": {                         // Ajax - JSON 응답
				cmd = new ScrapCommand();
				break;
			}
			case "/comment/write": {                      // Ajax - JSON 응답
				cmd = new CommentWriteCommand();
				break;
			}
			case "/comment/delete": {                     // Ajax - JSON 응답
				cmd = new CommentDeleteCommand();
				break;
			}
		}

		boolean isForward = true;
		if (cmd != null) {
			isForward = cmd.doCommand(request, response);
		}

		if (isForward && page != null) {
			request.getRequestDispatcher(page).forward(request, response);
		}
	}

}
