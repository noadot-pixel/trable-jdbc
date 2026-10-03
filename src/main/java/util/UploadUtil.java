package util;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Part;

/**
 * 게시글 사진 / 프로필 사진 업로드·삭제 도구.
 * 파일은 웹앱 폴더 안의 /upload/{subDir}에 저장하고, DB에는 "upload/{subDir}/파일명"
 * 문자열만 저장한다 (경로를 그대로 <img src>에 붙이면 된다).
 */
public class UploadUtil {

	private static final String UPLOAD_DIR = "upload";
	private static final long MAX_SIZE = 5 * 1024 * 1024;   // 5MB

	//업로드된 파일(Part)을 저장하고 DB에 넣을 경로를 돌려준다. 파일이 없거나 이미지가 아니면 null.
	public static String save(Part part, ServletContext context, String subDir) throws IOException {

		if (part == null || part.getSize() == 0) {
			return null;   // 파일을 선택하지 않고 제출한 경우
		}

		if (part.getSize() > MAX_SIZE) {
			return null;   // 5MB 초과는 저장하지 않음 (Command에서 이 경우 안내 메시지를 띄운다)
		}

		String contentType = part.getContentType();
		if (contentType == null || !contentType.startsWith("image/")) {
			return null;   // 이미지 파일만 허용
		}

		//원래 파일명에서 확장자(.jpg 등)만 가져온다.
		String original = part.getSubmittedFileName();
		String ext = "";
		if (original != null && original.lastIndexOf('.') != -1) {
			ext = original.substring(original.lastIndexOf('.')).toLowerCase();
		}

		//파일명은 UUID(겹치지 않는 랜덤 문자열)로 새로 짓는다.
		//→ 다른 사람이 같은 이름(photo.jpg)을 올려도 덮어쓰지 않고, 한글/공백 파일명 문제도 피한다.
		String fileName = UUID.randomUUID().toString() + ext;

		String dirPath = context.getRealPath("/" + UPLOAD_DIR + "/" + subDir);
		File dir = new File(dirPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		part.write(dirPath + File.separator + fileName);

		return UPLOAD_DIR + "/" + subDir + "/" + fileName;
	}

	//DB에 저장된 경로("upload/post/xxx.jpg")에 해당하는 실제 파일을 지운다.
	//국가별 기본 이미지("images/korea.jpg" 등)는 업로드 폴더 밖이라 건드리지 않는다.
	public static void delete(String path, ServletContext context) {

		if (path == null || !path.startsWith(UPLOAD_DIR + "/")) {
			return;
		}

		File file = new File(context.getRealPath("/" + path));
		if (file.exists()) {
			file.delete();
		}
	}
}
