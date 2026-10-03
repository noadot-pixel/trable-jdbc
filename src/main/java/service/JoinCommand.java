package service;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.MemberDAO;
import model.MemberDTO;

/**
 * 회원가입 폼 제출(POST /member/join) 처리.
 * 화면(auth/signup.jsp)의 "이메일" 입력값을 그대로 로그인 아이디로 쓴다.
 */
public class JoinCommand implements Command {

	@Override
	public boolean doCommand(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String email = request.getParameter("signupEmail");
		String nickname = request.getParameter("signupNickname");
		String pwd = request.getParameter("signupPassword");
		String pwdConfirm = request.getParameter("signupPasswordConfirm");

		//JS에서 이미 검사했더라도 서버에서 한 번 더 검사해야 한다
		//(JS는 브라우저에서 끄거나 조작할 수 있어서 믿을 수 없다)
		if (isBlank(email) || isBlank(nickname) || isBlank(pwd) || isBlank(pwdConfirm)) {
			request.setAttribute("msg", "모든 항목을 입력해 주세요.");
			return true;   // signup.jsp로 다시 forward
		}

		if (!pwd.equals(pwdConfirm)) {
			request.setAttribute("msg", "비밀번호가 서로 일치하지 않습니다.");
			return true;
		}

		if (pwd.length() < 6) {
			request.setAttribute("msg", "비밀번호는 6자 이상이어야 합니다.");
			return true;
		}

		email = email.trim();

		if (!email.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$")) {
			request.setAttribute("msg", "이메일 형식이 올바르지 않습니다.");
			return true;
		}

		MemberDAO dao = new MemberDAO();

		if (dao.idCheck(email)) {
			request.setAttribute("msg", "이미 가입된 이메일입니다.");
			return true;
		}

		MemberDTO dto = new MemberDTO();
		dto.setId(email);
		dto.setPwd(pwd);
		dto.setNickname(nickname.trim());

		if (dao.join(dto) == 1) {

			//성공 → 로그인 화면으로 이동. forward가 아니라 redirect여야
			//새로고침해도 가입이 두 번 되지 않는다.
			response.sendRedirect(request.getContextPath() + "/member/login?joined=1");
			return false;
		}

		request.setAttribute("msg", "회원가입에 실패했습니다. 잠시 후 다시 시도하세요.");
		return true;
	}

	private boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}

}
