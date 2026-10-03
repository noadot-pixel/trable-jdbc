# 작업 체크리스트

`docs/ARCHITECTURE.md` 9번 절의 표를 체크리스트로 옮긴 것. 위에서 아래로 순서대로 진행하면 중간중간 실행해보면서 갈 수 있다 (더 자세한 설명·지금 Firebase 버전에서 하던 일과의 1:1 대응은 `docs/ARCHITECTURE.md` 9번 참고).

## 0단계: 뼈대

- [ ] Eclipse Dynamic Web Project 생성, `main/webapp` 웹 루트 / `src/main/java` 소스 폴더로 지정
- [ ] `ojdbc11.jar`를 `WEB-INF/lib/`에 추가
- [ ] `docs/DB-SCHEMA.md`의 DDL로 Oracle에 테이블 생성 (`MEMBERS`, `POSTS`, `COMMENTS`, `POST_LIKES`, `SCRAPS`, `POST_LOGS` + 시퀀스)
- [ ] `util/DBUtil.java` 접속 정보를 본인 환경에 맞게 수정, 연결 테스트

## 1단계: 회원 (MemberController)

- [ ] `model/MemberDTO.java`
- [ ] `model/MemberDAO.java` — `idCheck`, `join`, `login`, (닉네임/프로필 수정용 `update`)
- [ ] `service/JoinCommand.java`
- [ ] `service/LoginCommand.java` (`session.setAttribute("loginMember", dto)`)
- [ ] `service/LogoutCommand.java` (`session.invalidate()`)
- [ ] `service/IdCheckCommand.java` (Ajax)
- [ ] `controller/MemberController.java` — 위 Command들을 주소별로 분배
- [ ] 화면: `member/join.jsp`, `member/login.jsp`
- [ ] 동작 확인: 회원가입 → 로그인 → 헤더에 닉네임 표시 → 로그아웃

## 2단계: 게시글 CRUD (PostController)

- [ ] `model/PostDTO.java`
- [ ] `model/PostDAO.java` — `getList`(검색/페이징), `getBoard`, `insert`, `update`, `delete`, `increaseViewCnt`, `getLatest`
- [ ] `service/PostListCommand.java`, `PostViewCommand.java`, `PostWriteCommand.java`, `PostModifyCommand.java`, `PostDeleteCommand.java`
- [ ] `controller/PostController.java`
- [ ] 화면: `posts.jsp`(목록), `posts/write.jsp`, `posts/detail.jsp`, `posts/edit.jsp`
- [ ] 사진 업로드: `util/UploadUtil.java` (5MB 제한, image/* 검사, UUID 파일명)
- [ ] 동작 확인: 글쓰기(사진 포함) → 목록 → 상세 → 수정 → 삭제, 전부 **본인 글만** 되는지(작성자 아닌 계정으로 시도)

## 3단계: 댓글

- [ ] `model/CommentDTO.java`
- [ ] `model/CommentDAO.java` — `getList`, `insert`, `delete`
- [ ] `service/CommentWriteCommand.java`, `CommentDeleteCommand.java` (삭제는 댓글 작성자/글 작성자/관리자만)
- [ ] `PostController`(또는 별도 `CommentController`)에 주소 추가
- [ ] 동작 확인: 댓글 작성/삭제, 남의 댓글은 못 지우는지

## 4단계: 좋아요 / 스크랩

- [ ] `PostDAO`(또는 `LikeDAO`)에 `isLiked`, `toggleLike`, `getLikeCnt` — **트랜잭션**으로 (`board_like` 변경 + 카운트 갱신이 함께 성공/실패)
- [ ] `PostDAO`(또는 `ScrapDAO`)에 `isScrapped`, `toggleScrap`, `getScrappedList`
- [ ] `service/LikeCommand.java`, `ScrapCommand.java` (Ajax, JSON 응답)
- [ ] 마이페이지: 내 글 목록(`getListByWriter`), 스크랩한 글 목록(`getScrappedList`)
- [ ] 인기순 정렬: `getPopular` (좋아요 수 → 조회수 → 최신순)
- [ ] 동작 확인: 좋아요/스크랩 토글, 마이페이지에 반영, 인기순 정렬

## 5단계: 관리자 (AdminController)

- [ ] `model/PostLogDTO.java`, `model/PostLogDAO.java` — `insert`, `getList`
- [ ] 글쓰기/수정/삭제 Command 끝에서 `PostLogDAO.insert(...)` 호출
- [ ] `service/PostLogListCommand.java`
- [ ] `controller/AdminController.java`
- [ ] 화면: `admin/postLogs.jsp` (관리자만 접근 가능하도록 `IS_ADMIN` 확인)

## 마무리 점검

- [ ] 모든 삭제/수정 SQL에 작성자(또는 관리자) 조건이 걸려 있는지 다시 확인
- [ ] 모든 SQL이 `PreparedStatement`인지 (문자열 이어붙이기로 만든 SQL이 없는지)
- [ ] 화면에 출력하는 사용자 입력값에 XSS 이스케이프가 돼 있는지 (`EscapeUtil` 등)
- [ ] 비밀번호를 평문으로 저장하지 않는지 (해시 적용 여부 — 시간이 되면)
