package service;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import model.MemberDTO;
import model.PostDAO;
import model.PostDTO;
import model.PostLogDAO;
import util.ParamUtil;
import util.UploadUtil;

/**
 * 게시글 수정 저장(POST /post/modify).
 */
public class PostModifyCommand implements Command {

	@Override
	public boolean doCommand(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		MemberDTO loginMember = (MemberDTO) request.getSession().getAttribute("loginMember");

		int bno = ParamUtil.toInt(request.getParameter("bno"), 0);

		PostDAO postDAO = new PostDAO();
		PostDTO original = postDAO.getPost(bno);

		if (original == null) {
			response.sendRedirect(request.getContextPath() + "/post/list");
			return false;
		}

		boolean isAdmin = loginMember.isAdmin();

		if (!loginMember.getId().equals(original.getWriter()) && !isAdmin) {
			request.getSession().setAttribute("flashMsg", "본인이 작성한 게시글만 수정할 수 있습니다.");
			response.sendRedirect(request.getContextPath() + "/post/view?bno=" + bno);
			return false;
		}

		String title = request.getParameter("writeTitle");
		String content = request.getParameter("writeBody");
		String country = request.getParameter("country");
		String style = request.getParameter("style");

		if (isBlank(title) || isBlank(content) || isBlank(country) || isBlank(style)) {
			request.setAttribute("post", original);
			request.setAttribute("msg", "제목, 본문, 여행지 구분, 여행 성향을 모두 입력해 주세요.");
			return true;   // posts/edit.jsp로 다시 forward
		}

		String imgPath = original.getImgPath();   // 새 사진을 고르지 않으면 기존 사진 유지

		Part photoPart = request.getPart("writePhoto");
		boolean imageChanged = false;

		if (photoPart != null && photoPart.getSize() > 0) {

			String uploaded = UploadUtil.save(photoPart, request.getServletContext(), "post");

			if (uploaded == null) {
				request.setAttribute("post", original);
				request.setAttribute("msg", "이미지 파일(5MB 미만)만 첨부할 수 있습니다.");
				return true;
			}

			imgPath = uploaded;
			imageChanged = true;
		}

		PostDTO dto = new PostDTO();
		dto.setBno(bno);
		dto.setWriter(original.getWriter());   // 작성자는 바꾸지 않는다
		dto.setTitle(title.trim());
		dto.setContent(content);
		dto.setCountry(country);
		dto.setStyle(style);
		dto.setImgPath(imgPath);

		//일반 회원은 본인 글만(where writer=?), 관리자는 전부 수정 가능
		int updatedRows = isAdmin ? postDAO.updateByAdmin(dto) : postDAO.update(dto);

		if (updatedRows == 0) {
			request.setAttribute("post", original);
			request.setAttribute("msg", "게시글 수정에 실패했습니다.");
			return true;
		}

		//사진을 새로 바꿨다면 이전 사진 파일은 정리한다 (업로드했던 사진일 때만, 기본 이미지는 그대로 둠).
		if (imageChanged) {
			UploadUtil.delete(original.getImgPath(), request.getServletContext());
		}

		//로그의 "작업자"는 실제로 이 수정을 실행한 사람(관리자일 수도 있음)으로 남긴다.
		new PostLogDAO().insert("UPDATE", bno, dto.getTitle(), loginMember.getId());

		response.sendRedirect(request.getContextPath() + "/post/view?bno=" + bno);
		return false;
	}

	private boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}

}
