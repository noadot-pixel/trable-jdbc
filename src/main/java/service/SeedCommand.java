package service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.MemberDTO;
import model.PostDAO;
import model.PostDTO;
import model.PostLogDAO;

/**
 * 예시 게시글 9개 추가(Ajax, POST /admin/seed). posts 테이블이 비어 있을 때만 동작한다.
 * (Firebase 버전의 postsStore.seedIfEmpty()와 같은 목적 - 관리자 화면의 "예시 게시글 시드 추가" 버튼)
 */
public class SeedCommand implements Command {

	private static final String[][] SEEDS = {
		//country, style, title, content, img
		{"KR", "healing", "제주도 2박 3일 힐링 여행", "아름다운 바다와 카페, 맛집까지 알차게 다녀온 제주 여행 기록입니다.", "images/korea.jpg"},
		{"KR", "food", "부산 해운대 맛집 총정리", "해운대 근처에서 직접 먹어보고 고른 진짜 맛집만 모았습니다.", "images/korea.jpg"},
		{"KR", "activity", "서울 근교 당일치기 액티비티 코스", "짧은 시간에도 알차게 즐길 수 있는 액티비티 코스를 소개합니다.", "images/korea.jpg"},
		{"JP", "food", "후쿠오카 3박 4일, 먹고 걷고 또 먹은 여행", "첫 일본 자유여행에서 만족했던 맛집과 숙소, 교통 정보를 정리했습니다.", "images/japan.jpg"},
		{"JP", "shopping", "오사카 쇼핑 스팟 완전 정리", "도톤보리부터 신사이바시까지 쇼핑 동선을 그대로 공유합니다.", "images/japan.jpg"},
		{"JP", "activity", "도쿄 디즈니랜드 완전 정복기", "대기 시간을 줄이는 동선과 꿀팁까지 한 번에 정리했습니다.", "images/japan.jpg"},
		{"ETC", "healing", "처음 떠나는 유럽, 스페인 바르셀로나", "캄프 누에서의 열정을 느낄 수 있는 바르셀로나 여행을 준비합니다.", "images/world.jpg"},
		{"ETC", "food", "방콕 길거리 맛집 탐방기", "현지인 추천 노점부터 유명 맛집까지 직접 다녀온 후기입니다.", "images/world.jpg"},
		{"ETC", "shopping", "파리에서 놓치면 안 되는 쇼핑 리스트", "면세 쇼핑부터 로컬 편집숍까지 동선대로 정리했습니다.", "images/world.jpg"}
	};

	@Override
	public boolean doCommand(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("application/json; charset=utf-8");
		PrintWriter out = response.getWriter();

		MemberDTO loginMember = (MemberDTO) request.getSession().getAttribute("loginMember");

		PostDAO postDAO = new PostDAO();
		List<PostDTO> existing = postDAO.getList("ALL", "ALL", "");

		if (!existing.isEmpty()) {
			out.print("{\"result\":\"ok\",\"added\":0}");
			return false;
		}

		PostLogDAO logDAO = new PostLogDAO();
		int added = 0;

		for (String[] seed : SEEDS) {

			PostDTO dto = new PostDTO();
			dto.setWriter(loginMember.getId());
			dto.setCountry(seed[0]);
			dto.setStyle(seed[1]);
			dto.setTitle(seed[2]);
			dto.setContent(seed[3]);
			dto.setImgPath(seed[4]);

			int bno = postDAO.insert(dto);

			if (bno != 0) {
				logDAO.insert("CREATE", bno, dto.getTitle(), loginMember.getId());
				added++;
			}
		}

		out.print("{\"result\":\"ok\",\"added\":" + added + "}");
		return false;
	}

}
