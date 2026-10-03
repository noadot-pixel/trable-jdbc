package model;

/**
 * 게시글 작업 이력 한 건. POST_LOGS 테이블 한 행에 대응한다.
 * 글이 삭제돼도 이력은 남아야 하므로 bno는 FK로 걸지 않고, 제목(postTitle)은
 * 삭제 당시 값을 그대로 복사해 저장해둔다.
 */
public class PostLogDTO {

	private int logId;
	private String logType;      // CREATE / UPDATE / DELETE
	private int bno;
	private String postTitle;
	private String actorId;      // 실제로 작업한 사람 (관리자가 남의 글을 고치면 관리자 id)
	private String actorNick;
	private String logDate;      // "YYYY-MM-DD HH24:MI"

	public int getLogId() {
		return logId;
	}
	public void setLogId(int logId) {
		this.logId = logId;
	}
	public String getLogType() {
		return logType;
	}
	public void setLogType(String logType) {
		this.logType = logType;
	}
	public int getBno() {
		return bno;
	}
	public void setBno(int bno) {
		this.bno = bno;
	}
	public String getPostTitle() {
		return postTitle;
	}
	public void setPostTitle(String postTitle) {
		this.postTitle = postTitle;
	}
	public String getActorId() {
		return actorId;
	}
	public void setActorId(String actorId) {
		this.actorId = actorId;
	}
	public String getActorNick() {
		return actorNick;
	}
	public void setActorNick(String actorNick) {
		this.actorNick = actorNick;
	}
	public String getLogDate() {
		return logDate;
	}
	public void setLogDate(String logDate) {
		this.logDate = logDate;
	}

	//화면에 보여줄 한글 이름
	public String getLogTypeLabel() {
		if ("CREATE".equals(logType)) return "작성";
		if ("UPDATE".equals(logType)) return "수정";
		if ("DELETE".equals(logType)) return "삭제";
		return logType;
	}

}
