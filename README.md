# 여행만들기 (Java + JSP + Oracle, MVC2)

NCS 시험용 "여행 커뮤니티" 웹사이트의 **실제 제출용 구현**. 기획·화면·기능은 전부 정해져 있고, 이번 프로젝트는 그걸 **MVC2(프론트 컨트롤러 + Command 패턴) + JDBC + Oracle**로 구현하는 것만 남았다.

## 이 저장소가 뭔지

- 자매 저장소 [`ai2giTrable`](https://github.com/noadot-pixel/ai2giTrable)에서 Firebase(Firestore/Auth/Storage)로 **먼저 목업을 완성**했다. 화면 구성, 기능 목록(회원/게시글/댓글/좋아요/스크랩/관리자 로그), CSS/이미지는 전부 거기서 검증이 끝났다.
- 이 저장소는 그 목업을 **똑같은 기능으로, 서버(Java)만 DB(Oracle)에 접속하는 구조**로 새로 짠다. 브라우저가 DB에 직접 붙던 Firebase 방식과 달리, 여기서는 JSP의 `<script>`가 DB를 부르는 일이 없다 — 전부 서블릿(컨트롤러) → Command → DAO를 거친다.
- 수업에서 다룬 `BlogProject`(프론트 컨트롤러 + Command 패턴)와 같은 구조다. 막히면 그 프로젝트의 같은 역할 클래스(`BoardController`/`BoardDAO`/`LikeCommand` 등)를 참고.

## 설계 문서 (먼저 읽을 것)

- **[docs/DB-SCHEMA.md](docs/DB-SCHEMA.md)** — 테이블/컬럼 설계 (`MEMBERS`, `POSTS`, `COMMENTS`, `POST_LIKES`, `SCRAPS`, `POST_LOGS`), DDL, 자주 쓰는 SQL.
- **[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)** — MVC2 구조 설명, 요청 흐름, 코드 골격, 그리고 **9번 절에 만들어야 할 Controller/Command/DAO 전체 목록과 작업 순서**가 있다. 이게 사실상 작업 체크리스트다.
- **[PROGRESS.md](PROGRESS.md)** — 위 9번 절을 체크리스트로 뽑아놓은 것. 진행하면서 체크해나가면 된다.

## 기술 스택 / 환경

- JDK 17, Tomcat 10.x (Jakarta EE, `jakarta.servlet.*`)
- Oracle Database (XE 등 로컬 인스턴스)
- 패키지 구조: `controller`(서블릿) / `service`(Command) / `model`(DTO+DAO) / `util`(DBUtil 등) — `BlogProject`와 동일

## 시작하는 법

1. 이 저장소를 clone한 뒤, Eclipse에서 **Dynamic Web Project**로 새로 만들고 `main/webapp`을 웹 루트로, `src/main/java`를 소스 폴더로 지정한다 (`BlogProject`를 새로 만들 때와 같은 방식).
2. Oracle JDBC 드라이버(`ojdbc11.jar`)를 받아 `main/webapp/WEB-INF/lib/`에 넣는다 (저장소에는 포함 안 함 — `.gitignore` 참고).
3. `docs/DB-SCHEMA.md`의 DDL로 테이블을 만든다.
4. `src/main/java/util/DBUtil.java`의 접속 정보(URL/계정/비밀번호)를 본인 환경에 맞게 고친다.
5. `PROGRESS.md` 순서대로 Controller → Command → DAO를 하나씩 만들어간다.
6. 화면(CSS/이미지)은 `main/webapp/css`, `main/webapp/images`에 `ai2giTrable`에서 그대로 가져와 뒀다. JSP 마크업도 `ai2giTrable`의 `.jsp`를 참고해서 쓰되, `<script>` 안의 Firebase 호출 부분만 "서버가 이미 구해다 준 값을 출력"하는 식으로 바꾸면 된다.

## 지금 들어있는 것 / 안 들어있는 것

- **들어있음**: `css/`, `images/`(화면 자원), `service/Command.java`(인터페이스), `util/DBUtil.java`(DBM 틀), 기본 `web.xml`, 설계 문서 2개.
- **안 들어있음(직접 구현)**: 실제 Controller/Command/DTO/DAO 전부. `PROGRESS.md` 체크리스트를 보고 하나씩 만든다 — 이게 이 프로젝트의 본체다.
