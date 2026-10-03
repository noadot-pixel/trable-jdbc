# 작업 체크리스트

`docs/ARCHITECTURE.md` 9번 절의 표를 체크리스트로 옮긴 것. (더 자세한 설명·지금 Firebase 버전에서 하던 일과의 1:1 대응은 `docs/ARCHITECTURE.md` 9번 참고)

**2026-10-03: 0~5단계 코드를 전부 작성했다.** `ai2giTrable`의 JSP 마크업/CSS를 재활용해 화면을 만들고, Java(Controller/Command/DTO/DAO)를 새로 짰다. 아래 체크는 "코드가 있다"는 뜻이고, 실제 Oracle에 연결해서 끝까지 돌려보는 건 아직이다 — 그 부분만 직접 확인하면 된다.

## 0단계: 뼈대

- [x] `main/webapp` 웹 루트 / `src/main/java` 소스 폴더 구조로 작성됨
- [ ] **(직접 할 일) Eclipse Dynamic Web Project로 열기**, 또는 지금처럼 Tomcat에 직접 배포해서 계속 써도 됨
- [ ] **(직접 할 일) `ojdbc11.jar`를 `main/webapp/WEB-INF/lib/`에 추가** (라이선스상 저장소에는 안 넣었음)
- [ ] **(직접 할 일) `docs/schema.sql`을 Oracle에서 실행**해 테이블/시퀀스 생성
- [ ] **(직접 할 일) `src/main/java/util/DBUtil.java`의 URL/계정/비밀번호를 본인 환경에 맞게 수정**

## 1단계: 회원 (MemberController) — 코드 작성 완료

- [x] `model/MemberDTO.java`, `model/MemberDAO.java` (`idCheck`, `join`, `login`, `getById`, `update`)
- [x] `service/JoinCommand.java`, `LoginCommand.java`, `LogoutCommand.java`
- [x] `service/MypageCommand.java`(보기), `ProfileEditCommand.java`(저장)
- [x] `controller/MemberController.java`
- [x] 화면: `auth/signup.jsp`, `auth/login.jsp`, `mypage.jsp`, `mypage/edit.jsp`
- [ ] **(직접 할 일) 동작 확인**: 회원가입 → 로그인 → 헤더에 닉네임 표시 → 마이페이지 → 프로필 수정 → 로그아웃
- 스킵: `IdCheckCommand` — 화면에 별도 "중복확인" 버튼이 없어서(가입 폼이 이메일만 받음) 만들지 않음. 대신 `JoinCommand`가 제출 시 서버에서 중복을 검사한다.

## 2단계: 게시글 CRUD (PostController) — 코드 작성 완료

- [x] `model/PostDTO.java`, `model/PostDAO.java` (목록/검색/정렬, 상세, 등록, 수정, 삭제, 조회수, 국가별 최신)
- [x] `service/MainCommand.java`, `PostListCommand.java`, `PostViewCommand.java`, `PostWriteCommand.java`, `PostModifyFormCommand.java`, `PostModifyCommand.java`, `PostDeleteCommand.java`
- [x] `controller/PostController.java`
- [x] 화면: `index.jsp`(메인), `posts.jsp`(목록), `posts/write.jsp`, `posts/detail.jsp`, `posts/edit.jsp`
- [x] 사진 업로드: `util/UploadUtil.java` (5MB 제한, image/* 검사, UUID 파일명, 국가별 기본 이미지 지원)
- [ ] **(직접 할 일) 동작 확인**: 글쓰기(사진 포함) → 목록(필터/검색/정렬) → 상세 → 수정 → 삭제, 전부 **본인 글만** 되는지(작성자 아닌 계정으로 시도)

## 3단계: 댓글 — 코드 작성 완료

- [x] `model/CommentDTO.java`, `model/CommentDAO.java` (`getList`, `getById`, `insert`, `delete`, `countByPost`)
- [x] `service/CommentWriteCommand.java`, `CommentDeleteCommand.java` (삭제는 댓글 작성자/글 작성자/관리자만, Ajax)
- [x] `PostController`에 `/comment/write`, `/comment/delete` 주소 추가
- [x] 화면: `posts/detail.jsp`에 댓글 목록/작성/삭제 포함
- [ ] **(직접 할 일) 동작 확인**: 댓글 작성/삭제, 남의 댓글은 못 지우는지(단, 글 작성자/관리자는 지울 수 있어야 함)

## 4단계: 좋아요 / 스크랩 — 코드 작성 완료

- [x] `PostDAO`에 `isLiked`, `toggleLike`(트랜잭션), `getLikeCnt`
- [x] `PostDAO`에 `isScrapped`, `toggleScrap`, `getScrappedList`
- [x] `service/LikeCommand.java`, `ScrapCommand.java` (Ajax, JSON 응답)
- [x] 마이페이지: 내 글 목록, 스크랩한 글 목록(`mypage.jsp`)
- [x] 인기순 정렬: `getPopularList` (좋아요 수 → 조회수 → 최신순)
- [ ] **(직접 할 일) 동작 확인**: 좋아요/스크랩 토글, 마이페이지에 반영, 인기순 정렬

## 5단계: 관리자 (AdminController) — 코드 작성 완료

- [x] `model/PostLogDTO.java`, `model/PostLogDAO.java` (`insert`, `getList`)
- [x] 글쓰기/수정/삭제 Command 끝에서 `PostLogDAO.insert(...)` 호출
- [x] `service/PostLogListCommand.java`, `service/SeedCommand.java`(예시 게시글 9개)
- [x] `controller/AdminController.java` (로그인 + `IS_ADMIN` 확인)
- [x] 화면: `admin/index.jsp`, `admin/post-logs.jsp`
- [ ] **(직접 할 일) 관리자 계정 만들기**: 일반 가입 후 `UPDATE members SET is_admin='Y' WHERE id='...'`(schema.sql 맨 아래 주석 참고)
- [ ] **(직접 할 일) 동작 확인**: 게시글 로그 조회, 예시 게시글 시드 추가

## 마무리 점검

- [x] 모든 삭제/수정 SQL에 작성자(또는 관리자) 조건이 걸려 있음
- [x] 모든 SQL이 `PreparedStatement`임 (문자열 이어붙이기로 만든 SQL 없음)
- [x] 화면에 출력하는 사용자 입력값에 XSS 이스케이프 적용(`EscapeUtil.html`/`EscapeUtil.json`)
- [ ] **(직접 결정할 일) 비밀번호 해시 적용 여부** — 지금은 BlogProject와 같은 평문 저장. 시간이 되면 `MessageDigest`(SHA-256) 등으로 해시해서 저장하도록 `MemberDAO.join`/`login`을 고칠 것
- [ ] **(직접 확인) 로컬 환경에서 Oracle 없이 JSP/서블릿 컴파일·페이지 렌더링은 확인됨** (Tomcat 10.1.59 + JDK 17). 실제 DB 연결 후 전체 흐름 테스트는 아직
