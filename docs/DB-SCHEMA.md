# 데이터 구조 설계 (DB-SCHEMA)

원작성일: 2026-09-19 (자매 저장소 `ai2giTrable`의 Firebase 프로토타입에서 작성) / 이 저장소로 복사: 2026-10-03

이 프로젝트(여행만들기)의 **Oracle 테이블 설계 스펙**이다. 1번은 Firebase 프로토타입(`ai2giTrable`)에 실제로 있던 데이터 구성이고(어떤 기능이 어떤 데이터를 쓰는지 참고용), 2번부터가 **이 저장소에서 실제로 만들 테이블**이다.

> **"어느 코드가 Controller/Command/DTO/DAO/DBM 중 어디로 가는지"는 [ARCHITECTURE.md](ARCHITECTURE.md)를 참고.**

---

## 1. 현재 Firestore 구성

```
users/{uid}                     유저 정보 (문서 id = Firebase Auth uid)
  └─ scraps/{postId}            내가 스크랩한 글

posts/{postId}                  게시글
  ├─ comments/{commentId}       댓글
  └─ likes/{uid}                좋아요 (문서가 있으면 좋아요한 상태)

postLogs/{logId}                게시글 작성/수정/삭제 이력
admins/{uid}                    관리자 표시 (필드 없음, 문서 존재 여부만 확인)
```

### `users/{uid}`

| 필드 | 타입 | 설명 |
|---|---|---|
| `nickname` | string | 닉네임 |
| `email` | string | 로그인 이메일 |
| `joinedDate` | string | 가입일 |
| `status` | string | 계정 상태 (지금은 항상 "활성") |
| `bio` | string | 자기소개 |
| `country` / `countryOther` | string | 국가 / "기타" 선택 시 직접 입력값 |
| `emailLocal` / `emailDomain` / `emailDomainOther` | string | 프로필의 이메일 입력값 (`email`과 일부 중복) |
| `birthday` / `birthdayVisibility` | string | 생일 / 공개 여부 (`public`, `private`) |
| `name` / `nameVisibility` | string | 이름 / 공개 여부 (`public`, `private`) |
| `photoUrl` | string | 프로필 사진 (Storage URL) |

- 비밀번호는 저장하지 않는다. Firebase Authentication이 별도로 보관한다.
- 회원가입은 지금 목업(alert만)이라 이 컬렉션에 문서가 만들어지지 않는다. 프로필 수정 화면에서 저장할 때 처음 생긴다. (계정은 Firebase 콘솔 Authentication에서 직접 추가)

### `users/{uid}/scraps/{postId}`

| 필드 | 타입 | 설명 |
|---|---|---|
| `postId` | string | 스크랩한 게시글 id |
| `scrappedAt` | string (ISO) | 스크랩한 시각 |

### `posts/{postId}`

| 필드 | 타입 | 설명 |
|---|---|---|
| `title` / `body` | string | 제목 / 본문 |
| `country` | string | 여행지 구분: `KR`, `JP`, `ETC` |
| `countryLabel` | string | 표시용 이름 (예: "한국 여행지"). `country`에서 파생되는 중복값 |
| `style` | string | 여행 성향: `healing`, `food`, `activity`, `shopping` |
| `image` | string | 기본 이미지 파일명 (`korea.jpg` 등) |
| `imageUrl` | string | 업로드한 사진의 Storage URL (없을 수 있음) |
| `authorUid` | string | 작성자 uid |
| `authorNickname` | string | 작성 당시 닉네임 복사본 |
| `createdAt` | string (ISO) | 작성 시각 |
| `views` | number | 조회수 |
| `comments` | number | 댓글 수 (카운터) |
| `likes` | number | 좋아요 수 (카운터, 옛 글엔 없을 수 있음) |

### `posts/{postId}/comments/{commentId}`

| 필드 | 타입 | 설명 |
|---|---|---|
| `body` | string | 내용 (1~500자) |
| `authorUid` / `authorNickname` | string | 작성자 |
| `createdAt` | string (ISO) | 작성 시각 |

### `posts/{postId}/likes/{uid}`

| 필드 | 타입 | 설명 |
|---|---|---|
| `createdAt` | string (ISO) | 좋아요한 시각 (문서 id가 좋아요한 사용자 uid) |

### `postLogs/{logId}`

| 필드 | 타입 | 설명 |
|---|---|---|
| `type` | string | `CREATE`, `UPDATE`, `DELETE` |
| `postId` / `postTitle` | string | 대상 게시글 (삭제된 글도 이력은 남는다) |
| `actorUid` / `actorNickname` | string | 실제로 작업한 사람 (관리자가 남의 글을 고치면 관리자) |
| `time` | string (ISO) | 작업 시각 |

### 저장소 (Firebase Storage)

| 경로 | 용도 |
|---|---|
| `profile-images/{uid}` | 프로필 사진 |
| `post-images/{uid}/{파일명}` | 게시글 사진 |

Oracle로 옮기면 파일은 서버 폴더(예: `webapp/uploads/`)에 저장하고, DB에는 경로(URL)만 컬럼에 넣는다.

---

## 2. Oracle 테이블 (구현 완료)

**DDL은 [schema.sql](schema.sql)에 있다. 이 저장소의 Oracle 계정에서 그 파일을 위에서부터 그대로 실행하면 된다.** 아래는 그 요약이다.

### 설계 방침 (지난 설계안에서 바뀐 점)

- 처음 설계(이 섹션의 이전 버전)는 숫자 대리 키(`MEMBER_ID`, `WRITER_ID`)를 썼지만, **실제 구현은 수업 예제(BlogProject)와 같은 방식으로 로그인 아이디(이메일)를 기본키로 그대로 쓴다.** (`members.id`)
- 화면(`auth/signup.jsp`, `auth/login.jsp`)이 "이메일" 입력 하나만 받으므로, 별도의 "아이디" 입력칸을 추가하지 않고 그 이메일 값을 그대로 로그인 아이디로 쓴다. 그래서 `EMAIL` 컬럼을 따로 두지 않는다 — `id` 자체가 이메일이다.
- `BIRTHDAY`는 날짜 계산을 할 일이 없는 단순 표시값이라 `DATE`가 아니라 `VARCHAR2(10)`로 저장한다(“YYYY-MM-DD” 문자열 그대로).
- `comments`(댓글 수)는 컬럼으로 저장하지 않고 `COUNT(*)`로 센다. `likes`(좋아요 수)는 `posts.like_cnt` 컬럼으로 저장하되, 토글할 때마다 `+1/-1` 하지 않고 **실제 좋아요 개수를 다시 세서** 넣는다(`CommentDAO`/`PostDAO`의 `toggleLike` 참고) — 값이 어긋나 있어도 자동으로 맞춰지도록.
- 자료형은 `NUMBER`, `VARCHAR2`, `DATE`, `CHAR(1)`('Y'/'N' 플래그)만 쓴다.

### 테이블 관계

```
MEMBERS 1 ──< POSTS 1 ──< COMMENTS
   │             │
   │             ├──< POST_LIKES >── MEMBERS
   ├──< SCRAPS >── POSTS
   └──< POST_LOGS
```

### 테이블 요약

| 테이블 | 기본키 | 설명 |
|---|---|---|
| `members` | `id` (VARCHAR2, = 이메일) | 회원. `is_admin`='Y'면 관리자. `MemberDAO` |
| `posts` | `bno` (시퀀스 `seq_posts`) | 게시글. `writer`는 `members.id` 참조. `PostDAO` |
| `comments` | `cno` (시퀀스 `seq_comments`) | 댓글. `bno`는 `posts.bno` 참조(`ON DELETE CASCADE`). `CommentDAO` |
| `post_likes` | `(bno, member_id)` | 좋아요. 문서가 아니라 행 존재 여부로 판단. `PostDAO`에 같이 있음 |
| `scraps` | `(member_id, bno)` | 스크랩. `PostDAO`에 같이 있음 |
| `post_logs` | `log_id` (시퀀스 `seq_post_logs`) | 작업 이력. `bno`는 FK 없음(글이 지워져도 이력은 남아야 함). `PostLogDAO` |

### Firestore 필드 → 컬럼 대응 (1번과 비교용)

| Firestore (`ai2giTrable`) | Oracle (이 저장소) | 비고 |
|---|---|---|
| `users/{uid}` 문서 id | `members.id` | uid 대신 이메일을 그대로 기본키로 씀 |
| `email` | `members.id` | 컬럼을 따로 안 둠(위 설명) |
| `nickname`, `bio`, `country`, `countryOther` | 같은 이름의 컬럼 | |
| `name`, `nameVisibility` | `real_name`, `name_public`('Y'/'N') | |
| `birthday`, `birthdayVisibility` | `birthday`, `birthday_public`('Y'/'N') | |
| `photoUrl` | `photo_path` | Storage URL 대신 서버 업로드 경로("upload/profile/xxx.jpg") |
| (`admins` 컬렉션) | `members.is_admin` | 별도 테이블 대신 컬럼 |
| `posts/{id}` 문서 id | `posts.bno` | |
| `authorUid`, `authorNickname` | `posts.writer` (닉네임은 `members`와 JOIN) | 닉네임을 중복 저장하지 않아서, 닉네임을 바꾸면 옛 글에도 바로 반영됨 |
| `countryLabel` | (저장 안 함) | `PostDTO.getCountryLabel()`에서 즉석으로 변환 |
| `image` + `imageUrl` | `img_path` 하나로 합침 | 업로드 안 했으면 "images/korea.jpg" 등, 업로드했으면 "upload/post/xxx.jpg" |
| `views` | `view_cnt` | |
| `comments`(카운터) | (저장 안 함) | `COUNT(*)` |
| `likes`(카운터) | `like_cnt` | |
| `posts/{id}/comments/{id}` | `comments` 테이블 | |
| `posts/{id}/likes/{uid}` | `post_likes` 테이블 | |
| `users/{uid}/scraps/{postId}` | `scraps` 테이블 | |
| `postLogs/{id}` | `post_logs` 테이블 | |

---

## 3. 자주 쓰는 SQL (DAO에 실제로 있는 것, 참고용)

```sql
-- 회원가입 (MemberDAO.join)
insert into members(id, pwd, nickname) values(?, ?, ?);

-- 로그인 (MemberDAO.login)
select id, pwd, nickname, is_admin, ... from members where id = ? and pwd = ?;

-- 게시글 등록 (PostDAO.insert) - bno는 시퀀스에서 먼저 뽑아서 insert에 그대로 쓴다
select seq_posts.nextval from dual;
insert into posts(bno, writer, title, content, country, style, img_path) values(?, ?, ?, ?, ?, ?, ?);

-- 게시글 목록 (PostDAO.getList) - 작성자 닉네임을 JOIN으로 가져온다
select p.bno, p.title, m.nickname writer_nick, p.view_cnt, p.like_cnt, to_char(p.reg_date,'YYYY-MM-DD') reg_date
  from posts p join members m on p.writer = m.id
 order by p.bno desc;

-- 좋아요 토글 (PostDAO.toggleLike) - 두 테이블이 같이 바뀌어야 하므로 트랜잭션으로 묶는다
-- (1) post_likes에 insert 또는 delete  (2) posts.like_cnt를 count(*)로 다시 계산해서 갱신
-- conn.setAutoCommit(false) → 둘 다 성공하면 commit(), 하나라도 실패하면 rollback()

-- 게시글 삭제 (PostDAO.delete) - 본인 글일 때만 지워지도록 where에 writer까지 건다
delete from posts where bno = ? and writer = ?;
-- comments/post_likes/scraps는 ON DELETE CASCADE라 Oracle이 알아서 같이 지운다
```

---

## 4. 수정/삭제 권한

글 수정/삭제는 **세션의 로그인 아이디와 글의 writer가 같을 때만** 가능하다. 예외는 관리자(`is_admin = 'Y'`)이다.

| | 검사 위치 |
|---|---|
| 로그인 여부 | 각 Controller(`PostController`/`MemberController`/`AdminController`)가 주소별로 세션(`loginMember`)을 확인 |
| 본인 글인지 | `PostDAO.delete(bno, writer)`의 SQL `where bno=? and writer=?` (일반 회원), `PostDAO.deleteByAdmin(bno)`(관리자, Command에서 `isAdmin()`으로 분기) |
| 댓글 삭제(작성자/글작성자/관리자 셋 중 하나) | `CommentDeleteCommand`가 `CommentDAO.getById`로 댓글을, `PostDAO.getPost`로 글 작성자를 조회해 Java `if`로 비교(SQL 조건 하나로 끝내기 애매해서) |
| 화면(수정/삭제 버튼 노출) | JSP에서 `loginMember.getId().equals(post.getWriter()) \|\| loginMember.isAdmin()`로 조건부 렌더링(사용자 편의일 뿐, 실제 방어선은 위 DAO/Command) |

자세한 설계 배경은 [ARCHITECTURE.md](ARCHITECTURE.md) 4번·9번 참고.

---

## 5. 구현 상태

PROGRESS.md의 1~5단계(회원, 게시글 CRUD, 댓글, 좋아요/스크랩, 관리자 로그)가 전부 구현되어 있다. `docs/ARCHITECTURE.md` 9번의 표가 실제 클래스 이름과 거의 그대로 대응된다(일부는 Command를 합치거나 이름을 조금 바꿨다 - 예: `IdCheckCommand`는 화면에 중복확인 버튼이 없어서 만들지 않았고, 대신 `JoinCommand`가 제출 시 서버에서 중복을 검사한다).

**아직 해보지 않은 것**: 실제 Oracle 인스턴스에 `schema.sql`을 실행하고 전체 흐름(가입→로그인→글쓰기→댓글→좋아요→스크랩→관리자 로그)을 끝까지 돌려보는 것. 코드는 로컬 Tomcat에 배포해 컴파일·페이지 렌더링까지는 확인했지만(DB 연결 없이도 각 화면이 정상적인 HTTP 상태코드로 응답하고, 로그인/가입 폼 제출 시 "DB 연결 실패"가 조용히 처리되어 적절한 실패 메시지로 이어지는 것까지 확인함), 실제 DB 값을 넣고 꺼내보는 테스트는 아직이다.
