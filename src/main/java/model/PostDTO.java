package model;

/**
 * 게시글 한 건. POSTS 테이블 한 행 + 작성자 닉네임(JOIN으로 가져옴)에 대응한다.
 */
public class PostDTO {

	private int bno;
	private String writer;       // MEMBERS.ID (작성자 아이디)
	private String writerNick;   // JOIN으로 가져온 작성자 닉네임 (화면 표시용, POSTS에는 없음)
	private String title;
	private String content;
	private String country;      // KR / JP / ETC
	private String style;        // healing / food / activity / shopping
	private String imgPath;      // "images/korea.jpg"(기본) 또는 "upload/xxx.jpg"(업로드)
	private int viewCnt;
	private int likeCnt;
	private String regDate;      // "YYYY-MM-DD"
	private String modDate;      // "YYYY-MM-DD" 또는 null(수정 전)

	public int getBno() {
		return bno;
	}
	public void setBno(int bno) {
		this.bno = bno;
	}
	public String getWriter() {
		return writer;
	}
	public void setWriter(String writer) {
		this.writer = writer;
	}
	public String getWriterNick() {
		return writerNick;
	}
	public void setWriterNick(String writerNick) {
		this.writerNick = writerNick;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getStyle() {
		return style;
	}
	public void setStyle(String style) {
		this.style = style;
	}
	public String getImgPath() {
		return imgPath;
	}
	public void setImgPath(String imgPath) {
		this.imgPath = imgPath;
	}
	public int getViewCnt() {
		return viewCnt;
	}
	public void setViewCnt(int viewCnt) {
		this.viewCnt = viewCnt;
	}
	public int getLikeCnt() {
		return likeCnt;
	}
	public void setLikeCnt(int likeCnt) {
		this.likeCnt = likeCnt;
	}
	public String getRegDate() {
		return regDate;
	}
	public void setRegDate(String regDate) {
		this.regDate = regDate;
	}
	public String getModDate() {
		return modDate;
	}
	public void setModDate(String modDate) {
		this.modDate = modDate;
	}

	//국가 코드를 화면에 보여줄 이름으로 바꿔준다 (Firebase 버전의 countryLabel과 같은 값)
	public String getCountryLabel() {
		if ("KR".equals(country)) return "한국 여행지";
		if ("JP".equals(country)) return "일본 여행지";
		if ("ETC".equals(country)) return "세계 여행지";
		return country;
	}

	//여행 성향 코드를 화면에 보여줄 이름으로 바꿔준다
	public String getStyleLabel() {
		if ("healing".equals(style)) return "힐링";
		if ("food".equals(style)) return "맛집";
		if ("activity".equals(style)) return "액티비티";
		if ("shopping".equals(style)) return "쇼핑";
		return style;
	}

}
