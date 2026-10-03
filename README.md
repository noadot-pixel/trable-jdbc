# 여행만들기 (Java + JSP + Oracle, MVC2)

NCS 시험용 "여행 커뮤니티" 웹사이트의 **실제 제출용 구현**. MVC2(프론트 컨트롤러 + Command 패턴) + JDBC + Oracle로 전 기능(회원/게시글/댓글/좋아요/스크랩/관리자 로그)이 구현돼 있고, **실제 Oracle Database 11g Express Edition(11.2.0.2.0)에 연결해서 가입→로그인→글쓰기→댓글→좋아요→스크랩→관리자→삭제(CASCADE 포함)까지 전부 끝까지 돌려서 확인했다.**

## 이 저장소가 뭔지

- 자매 저장소 [`ai2giTrable`](https://github.com/noadot-pixel/ai2giTrable)에서 Firebase(Firestore/Auth/Storage)로 **먼저 목업을 완성**했다. 화면 구성, 기능 목록, CSS/이미지는 전부 거기서 검증이 끝났고, 그 JSP 마크업을 재활용해 이 저장소를 만들었다.
- 이 저장소는 같은 기능을 **서버(Java)만 DB(Oracle)에 접속하는 구조**로 새로 짰다. 브라우저가 DB에 직접 붙던 Firebase 방식과 달리, 여기서는 JSP의 `<script>`가 DB를 부르는 일이 없다 — 전부 서블릿(컨트롤러) → Command → DAO를 거친다.
- 수업에서 다룬 `BlogProject`(프론트 컨트롤러 + Command 패턴)와 같은 구조다. 막히면 그 프로젝트의 같은 역할 클래스(`BoardController`/`BoardDAO`/`LikeCommand` 등)를 참고.

## 설계 문서

- **[docs/DB-SCHEMA.md](docs/DB-SCHEMA.md)** — 테이블/컬럼 설계와 Firestore 대응표.
- **[docs/schema.sql](docs/schema.sql)** — 바로 실행할 수 있는 DDL (members/posts/comments/post_likes/scraps/post_logs + 시퀀스). **실제 Oracle 11g XE에서 두 번 실행해서(처음 것 하나 고친 뒤 테이블 전부 지우고 재실행) 검증 완료.**
- **[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)** — MVC2 구조 설명, 요청 흐름, 코드 골격, **9번 절에 Controller/Command/DAO 전체 목록**.
- **[PROGRESS.md](PROGRESS.md)** — 체크리스트. 거의 전부 완료.

## 기술 스택 / 환경 (실제 검증된 조합)

- JDK 17, Tomcat 10.1.x (Jakarta EE, `jakarta.servlet.*`)
- **Oracle Database 11g Express Edition (11.2.0.2.0)**, SID `XE`
- JDBC 드라이버: `ojdbc11.jar`(Oracle 23ai 클라이언트, 구버전 DB에도 하위호환으로 정상 접속됨)
- 패키지 구조: `controller`(서블릿) / `service`(Command) / `model`(DTO+DAO) / `util`(DBUtil 등) — `BlogProject`와 동일

## 시작하는 법

1. 이 저장소를 clone한 뒤, Eclipse에서 **Dynamic Web Project**로 열거나(`main/webapp` 웹 루트, `src/main/java` 소스 폴더), Tomcat `webapps`에 정션/복사로 바로 배포해도 된다.
2. Oracle JDBC 드라이버(`ojdbc11.jar`)를 `main/webapp/WEB-INF/lib/`에 넣는다 (저장소에는 포함 안 함 — `.gitignore` 참고. `reference/BlogProject`에 같은 파일이 있다면 그걸 복사해도 된다).
3. Oracle에 애플리케이션 계정을 만든다 (SYS로 접속해서):
   ```sql
   CREATE USER trable IDENTIFIED BY "원하는_비밀번호" DEFAULT TABLESPACE USERS TEMPORARY TABLESPACE TEMP;
   GRANT CONNECT, RESOURCE TO trable;
   GRANT UNLIMITED TABLESPACE TO trable;
   ```
4. `docs/schema.sql`을 그 `trable` 계정으로 그대로 실행해 테이블/시퀀스를 만든다 (SQL*Plus라면 `sqlplus trable/비밀번호@XE @docs/schema.sql` — `@호스트:포트:SID` 콜론 문법은 SQL*Plus에서 안 먹는다, TNS 별칭이나 `@host:port/service` 슬래시 문법을 쓸 것. 자세한 이유는 schema.sql 맨 위 주석 참고).
5. **비밀번호는 소스에 적지 않고 환경변수로 넘긴다.** Tomcat을 띄우기 전에:
   ```powershell
   $env:TRABLE_DB_PASSWORD = "3단계에서 만든 비밀번호"
   ```
   (`src/main/java/util/DBUtil.java`가 `System.getenv("TRABLE_DB_PASSWORD")`로 읽는다. URL/계정명이 다르면 `DBUtil.java`의 `url`/`id`도 같이 고칠 것.)
6. 소스를 컴파일해 `WEB-INF/classes`에 올리고(Eclipse라면 자동), Tomcat을 띄운다. `/main`으로 접속하면 메인 화면이 뜬다.
7. 가입 화면에서 계정을 하나 만든 뒤, `UPDATE members SET is_admin='Y' WHERE id='가입한 이메일'; COMMIT;`로 관리자 계정을 만들어두면 관리자 메뉴(게시글 로그, 예시 게시글 9개 시드 추가)를 쓸 수 있다. **세션에 로그인 정보가 캐시되므로, 관리자로 바꾼 뒤에는 한 번 다시 로그인해야 반영된다.**

## 지금 들어있는 것

- **전체 기능 구현 + 실제 DB로 끝까지 검증 완료**: 회원가입/로그인/로그아웃/마이페이지(보기·수정), 게시글 CRUD(한글 제목/본문 정상 저장, 국가/성향 필터, 제목 검색, 최신순/인기순 정렬, 조회수 증가), 댓글, 좋아요, 스크랩, 관리자(게시글 로그, 예시 게시글 시드 — 이미 글이 있으면 건너뜀), 게시글 삭제 시 댓글/좋아요/스크랩이 `ON DELETE CASCADE`로 실제로 함께 삭제되는 것까지 확인.
- `main/webapp`: 화면(JSP, `ai2giTrable`의 마크업을 재활용) + `css/`, `images/`(화면 자원).
- `src/main/java`: `controller`(3개), `service`(Command 18개), `model`(DTO+DAO 4쌍), `util`(DBUtil, ParamUtil, EscapeUtil, UploadUtil).

## 검증 중 발견해서 고친 문제 (남겨두는 이유: 같은 실수를 피하기 위해)

- **`schema.sql`의 한글 인라인 주석이 SQL\*Plus에서 조용히 테이블 컬럼 하나를 날렸다.** `country_other` 줄의 한글 주석 때문에 `members.real_name` 컬럼이 **에러 메시지 없이** 통째로 안 만들어졌고, 로그인 시 `ORA-00904: invalid identifier`로만 드러났다. 지금 `schema.sql`은 주석을 전부 ASCII(영어)로 바꿔서 이 문제를 원천적으로 피했다. 직접 수정하다가 한글 주석을 또 넣고 싶다면, 넣은 뒤 반드시 `DESC <table>`로 컬럼이 전부 있는지 확인할 것.
- SQL\*Plus 접속 문자열은 JDBC와 문법이 다르다. `jdbc:oracle:thin:@host:port:SID`(콜론, JDBC 전용 축약형)는 SQL\*Plus에서는 안 먹고, `@host:port/SERVICE_NAME`(슬래시) 또는 `tnsnames.ora`에 등록된 별칭(`@XE`)을 써야 한다. `DBUtil.java`의 JDBC URL은 그대로 콜론 문법이 맞다 — 거긴 안 고쳐도 된다.
- 비밀번호 평문 저장은 아직 그대로다(BlogProject와 동일한 방식). 시험 제출 전 시간이 되면 해시 적용을 권장한다(PROGRESS.md 마무리 점검 참고).
