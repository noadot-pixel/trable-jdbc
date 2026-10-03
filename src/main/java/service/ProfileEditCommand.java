package service;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import model.MemberDAO;
import model.MemberDTO;
import util.UploadUtil;

/**
 * 회원정보 수정(POST /member/mypageEdit). GET은 컨트롤러가 바로 mypage/edit.jsp로
 * forward하므로(세션의 loginMember로 폼을 채움) 이 Command는 저장(POST)만 처리한다.
 */
public class ProfileEditCommand implements Command {

	@Override
	public boolean doCommand(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession();
		MemberDTO loginMember = (MemberDTO) session.getAttribute("loginMember");

		String nickname = request.getParameter("profileNickname");

		if (nickname == null || nickname.trim().isEmpty()) {
			request.setAttribute("msg", "닉네임을 입력해 주세요.");
			return true;   // mypage/edit.jsp로 다시 forward
		}

		String country = request.getParameter("profileCountry");

		if ("ETC".equals(country)
				&& (request.getParameter("profileCountryOther") == null
					|| request.getParameter("profileCountryOther").trim().isEmpty())) {
			request.setAttribute("msg", "국가를 직접 입력해 주세요.");
			return true;
		}

		MemberDTO dto = new MemberDTO();
		dto.setId(loginMember.getId());   // 화면 값이 아니라 세션의 로그인 id! (남의 정보를 못 바꾸게)
		dto.setNickname(nickname.trim());
		dto.setBio(request.getParameter("profileBio"));
		dto.setCountry(country);
		dto.setCountryOther(request.getParameter("profileCountryOther"));
		dto.setRealName(request.getParameter("profileName"));
		dto.setNamePublic(!"private".equals(request.getParameter("nameVisibility")));
		dto.setBirthday(request.getParameter("profileBirthday"));
		dto.setBirthdayPublic(!"private".equals(request.getParameter("birthdayVisibility")));

		//새 사진을 고르지 않았으면 기존 사진 경로를 그대로 유지한다.
		String photoPath = loginMember.getPhotoPath();

		Part photoPart = request.getPart("profilePhoto");

		if (photoPart != null && photoPart.getSize() > 0) {

			String newPath = UploadUtil.save(photoPart, request.getServletContext(), "profile");

			if (newPath == null) {
				request.setAttribute("msg", "이미지 파일(5MB 이하)만 업로드할 수 있습니다.");
				return true;
			}

			//업로드에 성공했으면 예전 프로필 사진 파일은 지운다.
			UploadUtil.delete(photoPath, request.getServletContext());
			photoPath = newPath;
		}

		dto.setPhotoPath(photoPath);

		if (new MemberDAO().update(dto) == 0) {
			request.setAttribute("msg", "저장에 실패했습니다. 잠시 후 다시 시도하세요.");
			return true;
		}

		//화면(헤더 닉네임 등)에 바로 반영되도록 세션의 로그인 정보도 최신 값으로 다시 채운다.
		session.setAttribute("loginMember", new MemberDAO().getById(loginMember.getId()));

		response.sendRedirect(request.getContextPath() + "/member/mypage");
		return false;
	}

}
