# 작업 체크리스트

`docs/ARCHITECTURE.md` 9번 절의 표를 체크리스트로 옮긴 것. (더 자세한 설명·지금 Firebase 버전에서 하던 일과의 1:1 대응은 `docs/ARCHITECTURE.md` 9번 참고)

**2026-10-03: 0~5단계 코드 작성 + 실제 Oracle Database 11g XE(11.2.0.2.0)에 연결해서 전체 흐름 끝까지 테스트 완료.** `ai2giTrable`의 JSP 마크업/CSS를 재활용해 화면을 만들고, Java(Controller/Command/DTO/DAO)를 새로 짰다. 회원가입→로그인→글쓰기(한글 포함)→조회→댓글→좋아요→스크랩→마이페이지→관리자 승격→게시글 로그→시드→삭제(CASCADE)까지 실제 DB로 확인했다. 과정에서 `schema.sql`의 버그(아래 "발견한 문제" 참고)도 찾아서 고쳤다.

## 0단계: 뼈대 — 완료

- [x] `main/webapp` 웹 루트 / `src/main/java` 소스 폴더 구조로 작성됨
- [x] `ojdbc11.jar`를 `main/webapp/WEB-INF/lib/`에 추가함 (로컬에만, `.gitignore`로 커밋 제외 — `reference/BlogProject`에 있던 걸 그대로 복사)
- [x] `docs/schema.sql`을 Oracle 11g XE에서 실행해 6개 테이블 + 시퀀스 3개 생성 확인 (한 번 고친 뒤 테이블 전부 지우고 재실행해서 최종본 검증까지 끝남)
- [x] `src/main/java/util/DBUtil.java`를 환경변수(`TRABLE_DB_PASSWORD`) 기반으로 고치고 실제 접속 확인 (URL/계정명은 XE 기본값 그대로 맞음)
- [x] Eclipse 없이 Tomcat에 직접 배포하는 방식으로 테스트함 (Eclipse로 열어도 동일하게 동작할 것)

## 1단계: 회원 (MemberController) — 완료, 실제 DB로 테스트함

- [x] `model/MemberDTO.java`, `model/MemberDAO.java` (`idCheck`, `join`, `login`, `getById`, `update`)
- [x] `service/JoinCommand.java`, `LoginCommand.java`, `LogoutCommand.java`
- [x] `service/MypageCommand.java`(보기), `ProfileEditCommand.java`(저장)
- [x] `controller/MemberController.java`
- [x] 화면: `auth/signup.jsp`, `auth/login.jsp`, `mypage.jsp`, `mypage/edit.jsp`
- [x] **동작 확인 완료**: 회원가입 → 로그인 → 헤더에 닉네임 표시(한글 정상) → 마이페이지 → 로그아웃
- [ ] (직접 할 일) `mypage/edit.jsp`(프로필 수정 폼 저장)은 아직 브라우저로 안 눌러봄 — 코드상 문제는 없어 보이지만 실제 클릭 테스트는 남음
- 스킵: `IdCheckCommand` — 화면에 별도 "중복확인" 버튼이 없어서(가입 폼이 이메일만 받음) 만들지 않음. 대신 `JoinCommand`가 제출 시 서버에서 중복을 검사한다.

## 2단계: 게시글 CRUD (PostController) — 완료, 실제 DB로 테스트함

- [x] `model/PostDTO.java`, `model/PostDAO.java` (목록/검색/정렬, 상세, 등록, 수정, 삭제, 조회수, 국가별 최신)
- [x] `service/MainCommand.java`, `PostListCommand.java`, `PostViewCommand.java`, `PostWriteCommand.java`, `PostModifyFormCommand.java`, `PostModifyCommand.java`, `PostDeleteCommand.java`
- [x] `controller/PostController.java`
- [x] 화면: `index.jsp`(메인), `posts.jsp`(목록), `posts/write.jsp`, `posts/detail.jsp`, `posts/edit.jsp`
- [x] 사진 업로드: `util/UploadUtil.java` (5MB 제한, image/* 검사, UUID 파일명, 국가별 기본 이미지 지원)
- [x] **동작 확인 완료**: 글쓰기(한글 제목/본문 정상 저장) → 목록(전체 개수 정상) → 상세(조회수 증가 확인) → 삭제(본인 글, 관리자 글 둘 다)
- [ ] (직접 할 일) 수정(`/post/modify`)과 사진 업로드 자체, 필터/검색/정렬 버튼은 브라우저로 직접 눌러서 확인 필요 (코드 경로는 있지만 curl로는 안 눌러봄)
- [ ] (직접 할 일) "본인 글 아닌데 수정/삭제 시도하면 막히는지" 음성 테스트(negative test)는 아직 안 함

## 3단계: 댓글 — 완료, 실제 DB로 테스트함

- [x] `model/CommentDTO.java`, `model/CommentDAO.java` (`getList`, `getById`, `insert`, `delete`, `countByPost`)
- [x] `service/CommentWriteCommand.java`, `CommentDeleteCommand.java` (삭제는 댓글 작성자/글 작성자/관리자만, Ajax)
- [x] `PostController`에 `/comment/write`, `/comment/delete` 주소 추가
- [x] 화면: `posts/detail.jsp`에 댓글 목록/작성/삭제 포함
- [x] **동작 확인 완료**: 댓글 작성(한글 정상 저장), 게시글 삭제 시 댓글도 CASCADE로 같이 삭제되는 것까지 확인
- [ ] (직접 할 일) "남의 댓글은 못 지우는지" 음성 테스트는 아직 안 함

## 4단계: 좋아요 / 스크랩 — 완료, 실제 DB로 테스트함

- [x] `PostDAO`에 `isLiked`, `toggleLike`(트랜잭션), `getLikeCnt`
- [x] `PostDAO`에 `isScrapped`, `toggleScrap`, `getScrappedList`
- [x] `service/LikeCommand.java`, `ScrapCommand.java` (Ajax, JSON 응답)
- [x] 마이페이지: 내 글 목록, 스크랩한 글 목록(`mypage.jsp`)
- [x] 인기순 정렬: `getPopularList` (좋아요 수 → 조회수 → 최신순)
- [x] **동작 확인 완료**: 좋아요 토글(카운트 반영), 스크랩 토글, 마이페이지에 둘 다 반영, 삭제 시 CASCADE로 같이 삭제
- [ ] (직접 할 일) 인기순 정렬 버튼을 브라우저로 직접 눌러서 순서가 맞는지는 아직 눈으로 안 봄

## 5단계: 관리자 (AdminController) — 완료, 실제 DB로 테스트함

- [x] `model/PostLogDTO.java`, `model/PostLogDAO.java` (`insert`, `getList`)
- [x] 글쓰기/수정/삭제 Command 끝에서 `PostLogDAO.insert(...)` 호출
- [x] `service/PostLogListCommand.java`, `service/SeedCommand.java`(예시 게시글 9개)
- [x] `controller/AdminController.java` (로그인 + `IS_ADMIN` 확인)
- [x] 화면: `admin/index.jsp`, `admin/post-logs.jsp`
- [x] **관리자 계정 만들기 확인**: `UPDATE members SET is_admin='Y' WHERE id='...'; COMMIT;` 실행 후 **재로그인해야 세션에 반영됨**(로그인 시 세션에 캐시하는 구조라서) — 이 점 PROGRESS/README에 적어둠
- [x] **동작 확인 완료**: 관리자 메뉴 접근, 게시글 로그 조회(작성/삭제 이력 정상 기록), 예시 게시글 시드(이미 글 있으면 0건으로 건너뜀) 전부 확인

## 마무리 점검

- [x] 모든 삭제/수정 SQL에 작성자(또는 관리자) 조건이 걸려 있음
- [x] 모든 SQL이 `PreparedStatement`임 (문자열 이어붙이기로 만든 SQL 없음)
- [x] 화면에 출력하는 사용자 입력값에 XSS 이스케이프 적용(`EscapeUtil.html`/`EscapeUtil.json`)
- [x] **실제 Oracle 11g XE로 끝까지 연결 테스트 완료** (회원/게시글/댓글/좋아요/스크랩/관리자/CASCADE 삭제 전부)
- [ ] **(직접 결정할 일) 비밀번호 해시 적용 여부** — 지금은 BlogProject와 같은 평문 저장. 시간이 되면 `MessageDigest`(SHA-256) 등으로 해시해서 저장하도록 `MemberDAO.join`/`login`을 고칠 것
- [ ] (직접 할 일) 브라우저로 직접 눌러보면서 디자인/UX 확인 (지금까지 테스트는 전부 curl 기반이라 화면이 실제로 예쁘게 나오는지는 아직 안 봄)

## 테스트 중 발견해서 고친 문제

- **`schema.sql`의 한글 인라인 주석이 SQL\*Plus에서 `members.real_name` 컬럼을 에러 없이 누락시켰다.** NLS_LANG과 파일 인코딩(UTF-8)이 안 맞으면 생기는 문제로 보인다. 지금 `schema.sql`은 주석을 전부 영어(ASCII)로 바꿔서 해결했고, 수정 후 테이블을 전부 지우고 재실행해서 13개 컬럼이 정확히 다 생기는 것까지 확인했다. **직접 SQL 스크립트를 짤 때도 한글 주석을 넣었다면 `DESC 테이블명`으로 컬럼이 다 있는지 확인하는 습관을 들일 것.**
- SQL\*Plus 접속 문자열에서 `host:port:SID`(콜론, JDBC 전용 축약형)는 안 먹는다. `@XE`(tnsnames 별칭) 또는 `@host:port/SERVICE_NAME`(슬래시)을 써야 한다. `DBUtil.java`의 JDBC URL은 콜론 문법이 맞으므로 그대로 둔다 — JDBC thin 드라이버와 SQL\*Plus/OCI는 접속 문자열 문법이 다르다.
- 비밀번호를 `DBUtil.java` 소스에 직접 적지 않고 환경변수(`TRABLE_DB_PASSWORD`)로 받도록 바꿨다 — 공개 저장소에 실제 비밀번호가 올라가면 안 되기 때문.
