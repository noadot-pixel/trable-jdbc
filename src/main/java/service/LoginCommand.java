package service;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.MemberDAO;
import model.MemberDTO;

/**
 * 로그인 폼 제출(POST /member/login) 처리.
 */
public class LoginCommand implements Command {

	@Override
	public boolean doCommand(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String id = request.getParameter("loginEmail");
		String pwd = request.getParameter("loginPassword");

		if (id == null || pwd == null || id.trim().isEmpty() || pwd.trim().isEmpty()) {
			request.setAttribute("msg", "이메일과 비밀번호를 입력해 주세요.");
			return true;   // login.jsp로 forward
		}

		MemberDTO loginMember = new MemberDAO().login(id.trim(), pwd);

		if (loginMember == null) {
			request.setAttribute("msg", "이메일 또는 비밀번호가 올바르지 않습니다.");
			request.setAttribute("inputId", id);   // 이메일 칸은 다시 채워준다
			return true;
		}

		//세션(session): 브라우저 하나당 서버에 하나씩 생기는 보관함.
		//request는 요청 한 번이 끝나면 사라지지만, session은 로그아웃/브라우저 종료 전까지 유지된다.
		//→ 여기에 로그인 정보를 넣어두면 다른 페이지에서도 "누가 로그인했는지" 알 수 있다.
		HttpSession session = request.getSession();
		session.setAttribute("loginMember", loginMember);

		response.sendRedirect(request.getContextPath() + "/main");
		return false;
	}

}
