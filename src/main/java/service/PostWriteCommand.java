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
import util.UploadUtil;

/**
 * 글쓰기 폼 제출(POST /post/write). 사진 업로드 포함.
 * 폼이 multipart/form-data라서 컨트롤러(PostController)에 @MultipartConfig가 붙어 있어야 한다.
 */
public class PostWriteCommand implements Command {

	//사진을 올리지 않았을 때 국가별로 쓰는 기본 이미지 (main/webapp/images 안의 기존 파일)
	private static String defaultImage(String country) {
		if ("KR".equals(country)) return "images/korea.jpg";
		if ("JP".equals(country)) return "images/japan.jpg";
		return "images/world.jpg";
	}

	@Override
	public boolean doCommand(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		MemberDTO loginMember = (MemberDTO) request.getSession().getAttribute("loginMember");

		String title = request.getParameter("writeTitle");
		String content = request.getParameter("writeBody");
		String country = request.getParameter("country");
		String style = request.getParameter("style");

		if (isBlank(title) || isBlank(content) || isBlank(country) || isBlank(style)) {
			request.setAttribute("msg", "제목, 본문, 여행지 구분, 여행 성향을 모두 입력해 주세요.");
			return true;   // posts/write.jsp로 다시 forward
		}

		String imgPath = defaultImage(country);

		Part photoPart = request.getPart("writePhoto");

		if (photoPart != null && photoPart.getSize() > 0) {

			String uploaded = UploadUtil.save(photoPart, request.getServletContext(), "post");

			if (uploaded == null) {
				request.setAttribute("msg", "이미지 파일(5MB 미만)만 첨부할 수 있습니다.");
				return true;
			}

			imgPath = uploaded;
		}

		PostDTO dto = new PostDTO();
		dto.setWriter(loginMember.getId());   // 화면 값이 아니라 세션의 로그인 id!
		dto.setTitle(title.trim());
		dto.setContent(content);
		dto.setCountry(country);
		dto.setStyle(style);
		dto.setImgPath(imgPath);

		int bno = new PostDAO().insert(dto);

		if (bno == 0) {
			UploadUtil.delete(imgPath, request.getServletContext());   // DB 저장 실패 → 올린 파일도 정리
			request.setAttribute("msg", "게시글 등록에 실패했습니다.");
			return true;
		}

		new PostLogDAO().insert("CREATE", bno, dto.getTitle(), loginMember.getId());

		response.sendRedirect(request.getContextPath() + "/post/view?bno=" + bno);
		return false;
	}

	private boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}

}
