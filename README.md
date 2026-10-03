# 여행만들기 (Java + JSP + Oracle, MVC2)

NCS 시험용 "여행 커뮤니티" 웹사이트의 **실제 제출용 구현**. MVC2(프론트 컨트롤러 + Command 패턴) + JDBC + Oracle로 전 기능(회원/게시글/댓글/좋아요/스크랩/관리자 로그)이 구현돼 있다. 남은 건 **실제 Oracle에 연결해서 끝까지 돌려보는 것**이다.

## 이 저장소가 뭔지

- 자매 저장소 [`ai2giTrable`](https://github.com/noadot-pixel/ai2giTrable)에서 Firebase(Firestore/Auth/Storage)로 **먼저 목업을 완성**했다. 화면 구성, 기능 목록, CSS/이미지는 전부 거기서 검증이 끝났고, 그 JSP 마크업을 재활용해 이 저장소를 만들었다.
- 이 저장소는 같은 기능을 **서버(Java)만 DB(Oracle)에 접속하는 구조**로 새로 짰다. 브라우저가 DB에 직접 붙던 Firebase 방식과 달리, 여기서는 JSP의 `<script>`가 DB를 부르는 일이 없다 — 전부 서블릿(컨트롤러) → Command → DAO를 거친다.
- 수업에서 다룬 `BlogProject`(프론트 컨트롤러 + Command 패턴)와 같은 구조다. 막히면 그 프로젝트의 같은 역할 클래스(`BoardController`/`BoardDAO`/`LikeCommand` 등)를 참고.

## 설계 문서

- **[docs/DB-SCHEMA.md](docs/DB-SCHEMA.md)** — 테이블/컬럼 설계와 Firestore 대응표.
- **[docs/schema.sql](docs/schema.sql)** — 바로 실행할 수 있는 DDL (members/posts/comments/post_likes/scraps/post_logs + 시퀀스).
- **[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)** — MVC2 구조 설명, 요청 흐름, 코드 골격, **9번 절에 Controller/Command/DAO 전체 목록**.
- **[PROGRESS.md](PROGRESS.md)** — 체크리스트. 코드는 0~5단계 전부 작성 완료, **"(직접 할 일)"로 표시된 항목만 남아 있다** (Oracle 접속, 동작 확인, 관리자 계정 만들기 등).

## 기술 스택 / 환경

- JDK 17, Tomcat 10.x (Jakarta EE, `jakarta.servlet.*`)
- Oracle Database (XE 등 로컬 인스턴스)
- 패키지 구조: `controller`(서블릿) / `service`(Command) / `model`(DTO+DAO) / `util`(DBUtil 등) — `BlogProject`와 동일

## 시작하는 법

1. 이 저장소를 clone한 뒤, Eclipse에서 **Dynamic Web Project**로 열거나(`main/webapp` 웹 루트, `src/main/java` 소스 폴더), 지금처럼 Tomcat `webapps`에 정션/복사로 바로 배포해도 된다.
2. Oracle JDBC 드라이버(`ojdbc11.jar`)를 받아 `main/webapp/WEB-INF/lib/`에 넣는다 (저장소에는 포함 안 함 — `.gitignore` 참고).
3. `docs/schema.sql`을 Oracle 계정에서 그대로 실행해 테이블/시퀀스를 만든다.
4. `src/main/java/util/DBUtil.java`의 접속 정보(URL/계정/비밀번호)를 본인 환경에 맞게 고친다.
5. 소스를 컴파일해 `WEB-INF/classes`에 올리고(Eclipse라면 자동), Tomcat을 띄운다. `/main`으로 접속하면 메인 화면이 뜬다.
6. 가입 화면에서 계정을 하나 만든 뒤, `UPDATE members SET is_admin='Y' WHERE id='가입한 이메일'`로 관리자 계정을 하나 만들어두면 관리자 메뉴(게시글 로그, 예시 게시글 9개 시드 추가)를 쓸 수 있다.

## 지금 들어있는 것

- **전체 기능 구현 완료**: 회원가입/로그인/로그아웃/마이페이지(보기·수정), 게시글 CRUD(사진 업로드 포함, 국가/성향 필터, 제목 검색, 최신순/인기순 정렬), 댓글, 좋아요, 스크랩, 관리자(게시글 로그, 예시 게시글 시드).
- `main/webapp`: 화면(JSP, `ai2giTrable`의 마크업을 재활용) + `css/`, `images/`(화면 자원).
- `src/main/java`: `controller`(3개), `service`(Command 18개), `model`(DTO+DAO 4쌍), `util`(DBUtil, ParamUtil, EscapeUtil, UploadUtil).
- **DB 없이 검증한 것**: 로컬 Tomcat(10.1.59, JDK 17)에 배포해 전 페이지가 정상 HTTP 상태코드로 응답하는 것, 로그인/가입 폼 제출 시 DB 연결 실패가 조용히 처리되어 올바른 실패 메시지로 이어지는 것까지 확인함(500 에러 없음).
- **아직 안 한 것**: 실제 Oracle에 연결해서 데이터가 실제로 들어가고 나오는지 끝까지 테스트하는 것. `PROGRESS.md`의 "(직접 할 일)" 항목 참고. 비밀번호도 지금은 평문 저장(BlogProject와 동일) — 시간이 되면 해시 적용을 권장(PROGRESS.md 마무리 점검 참고).
