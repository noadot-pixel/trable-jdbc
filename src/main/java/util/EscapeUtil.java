package util;

/**
 * 사용자가 입력한 글자를 화면(HTML)이나 JSON에 그대로 넣으면 문제가 생기는
 * 특수문자를 바꿔주는 도구.
 * - html(): 제목에 &lt;script&gt;를 적어도 실행되지 않고 글자로만 보이게 한다 (XSS 방지)
 * - json(): Ajax 응답 문자열에 " 나 줄바꿈이 있어도 JSON이 깨지지 않게 한다
 */
public class EscapeUtil {

	public static String html(String str) {
		if (str == null) return "";
		return str.replace("&", "&amp;")      // &를 제일 먼저 바꿔야 아래에서 만든 &lt; 등이 다시 바뀌지 않는다
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;")
				.replace("'", "&#39;");
	}

	public static String json(String str) {
		if (str == null) return "";
		return str.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\r", "")
				.replace("\n", "\\n");
	}
}
