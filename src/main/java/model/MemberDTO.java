package model;

/**
 * 회원 한 명. MEMBERS 테이블 한 행에 대응한다.
 * 로그인 성공 시 전체 컬럼을 채워 세션(loginMember)에 저장해두고,
 * 이후 화면에서는 DB를 다시 조회하지 않고 세션의 이 객체를 그대로 쓴다.
 *
 * id = 로그인 아이디(기본키). 기존 Firebase 버전이 이메일/비밀번호로 로그인했던 것과
 * 화면을 그대로 재사용하기 위해, 가입/로그인 화면의 "이메일" 입력값을 그대로 이 id로 쓴다
 * (별도의 "아이디" 입력칸을 추가하지 않음). 그래서 이메일 컬럼을 따로 두지 않는다.
 */
public class MemberDTO {

	private String id;             // 로그인 아이디 = 이메일
	private String pwd;
	private String nickname;
	private boolean admin;         // IS_ADMIN = 'Y' 여부
	private String joinDate;       // "YYYY-MM-DD"

	private String bio;
	private String country;        // KR / JP / ETC
	private String countryOther;   // country가 ETC일 때 직접 입력값
	private String realName;
	private boolean namePublic;
	private String birthday;       // "YYYY-MM-DD"
	private boolean birthdayPublic;
	private String photoPath;      // 업로드 경로 ("upload/xxx.jpg"), 없으면 null

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public boolean isAdmin() {
		return admin;
	}
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}
	public String getJoinDate() {
		return joinDate;
	}
	public void setJoinDate(String joinDate) {
		this.joinDate = joinDate;
	}
	public String getBio() {
		return bio;
	}
	public void setBio(String bio) {
		this.bio = bio;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getCountryOther() {
		return countryOther;
	}
	public void setCountryOther(String countryOther) {
		this.countryOther = countryOther;
	}
	public String getRealName() {
		return realName;
	}
	public void setRealName(String realName) {
		this.realName = realName;
	}
	public boolean isNamePublic() {
		return namePublic;
	}
	public void setNamePublic(boolean namePublic) {
		this.namePublic = namePublic;
	}
	public String getBirthday() {
		return birthday;
	}
	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}
	public boolean isBirthdayPublic() {
		return birthdayPublic;
	}
	public void setBirthdayPublic(boolean birthdayPublic) {
		this.birthdayPublic = birthdayPublic;
	}
	public String getPhotoPath() {
		return photoPath;
	}
	public void setPhotoPath(String photoPath) {
		this.photoPath = photoPath;
	}

}
