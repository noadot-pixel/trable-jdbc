package service;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 요청 하나(작업 하나)를 처리하는 클래스들이 공통으로 따르는 규칙.
 * 컨트롤러(각 *Controller.java)가 주소별로 이 인터페이스의 구현체를 골라 실행한다.
 *
 * 반환값 약속:
 *   true  → 처리 끝. 컨트롤러가 정해둔 JSP로 forward 해줘 (화면을 보여줘야 할 때)
 *   false → 내가 이미 응답을 끝냈으니 forward 하지 마 (JSON 응답, sendRedirect 한 경우)
 */
public interface Command {

	boolean doCommand(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException;

}
