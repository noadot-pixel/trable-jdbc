package util;

/**
 * 요청 파라미터(항상 String)를 숫자로 바꾸는 도구.
 * ?bno=abc 처럼 숫자가 아닌 값이 오거나 아예 없으면 Integer.parseInt가 예외를 던지므로,
 * 그런 경우 기본값(def)을 돌려줘서 페이지가 500 에러로 죽지 않게 한다.
 */
public class ParamUtil {

	public static int toInt(String value, int def) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {   // null이 들어와도 NumberFormatException이 발생한다
			return def;
		}
	}
}
