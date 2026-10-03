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
import service.JoinCommand;
import service.LoginCommand;
import service.LogoutCommand;
import service.MypageCommand;
import service.ProfileEditCommand;

/**
 * 회원 관련 요청을 한 서블릿이 받아서 getServletPath()로 구분해 처리한다.
 */
@WebServlet({"/member/join", "/member/login", "/member/logout", "/member/mypage", "/member/mypageEdit"})
@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 10 * 1024 * 1024)
//프로필 수정 폼이 사진을 같이 보내는 multipart/form-data라서 이 설정이 필요하다.
public class MemberController extends HttpServlet {

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

		//로그인해야만 볼 수 있는 주소 → 로그인 안 했으면 로그인 화면으로
		boolean needsLogin = "/member/mypage".equals(path) || "/member/mypageEdit".equals(path);

		if (needsLogin && loginMember == null) {
			response.sendRedirect(request.getContextPath() + "/member/login");
			return;
		}

		switch (path) {
			case "/member/join": {
				page = "/auth/signup.jsp";               // GET: 가입 화면 / POST 실패 시: 다시 가입 화면
				if ("POST".equals(method)) {
					cmd = new JoinCommand();
				}
				break;
			}
			case "/member/login": {
				page = "/auth/login.jsp";
				if ("POST".equals(method)) {
					cmd = new LoginCommand();
				}
				break;
			}
			case "/member/logout": {
				cmd = new LogoutCommand();
				break;
			}
			case "/member/mypage": {
				cmd = new MypageCommand();
				page = "/mypage.jsp";
				break;
			}
			case "/member/mypageEdit": {
				page = "/mypage/edit.jsp";
				if ("POST".equals(method)) {
					cmd = new ProfileEditCommand();      // 저장
				}
				//GET은 Command 없이 바로 forward: 세션의 loginMember로 폼을 채우면 된다.
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
