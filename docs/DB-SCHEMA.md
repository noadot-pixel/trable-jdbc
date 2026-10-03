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

## 2. Oracle(SQL) 기본 설계안 (초보자용)

### 설계 방침

- **필수 3개 테이블**(회원, 게시글, 댓글)로 시작하고, 기능을 붙일 때 **선택 3개**(좋아요, 스크랩, 작업 이력)를 추가한다.
- 오래된 Oracle 버전에서도 되도록 기본 문법만 쓴다. 번호 생성은 `IDENTITY` 대신 **시퀀스**(`SEQUENCE`)를 쓴다.
- 자료형은 `NUMBER`, `VARCHAR2`, `DATE`만 쓴다. `DATE`는 날짜와 시간을 함께 저장한다.
- 본문은 `CLOB` 대신 `VARCHAR2(4000)`을 쓴다. JDBC에서 `getString()`으로 바로 읽을 수 있다.
- 댓글 수, 좋아요 수는 컬럼으로 저장하지 않고 `COUNT(*)`로 센다. 숫자를 따로 갱신하다가 어긋나는 문제를 피한다.
- 값이 정해진 컬럼(`COUNTRY`, `STYLE`)은 코드 문자열로 저장하고, 검증은 Java에서 한다. (`CHECK` 제약은 나중에 추가해도 된다.)

### 테이블 관계

```
MEMBERS 1 ──< POSTS 1 ──< COMMENTS          ← 필수 3개
   │             │
   │             ├──< POST_LIKES >── MEMBERS   ← 선택: 좋아요
   ├──< SCRAPS >── POSTS                        ← 선택: 스크랩
   └──< POST_LOGS                               ← 선택: 작업 이력
```

`1 ──< N`은 "한 명이 여러 개"라는 뜻이다. 회원 1명이 게시글 여러 개를, 게시글 1개가 댓글 여러 개를 가진다.

### 필수 테이블 (1단계)

```sql
-- 번호 생성기 (INSERT 할 때 시퀀스명.NEXTVAL 로 다음 번호를 받는다)
CREATE SEQUENCE SEQ_MEMBERS  START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE SEQ_POSTS    START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE SEQ_COMMENTS START WITH 1 INCREMENT BY 1;

-- 1. 회원
CREATE TABLE MEMBERS (
    MEMBER_ID       NUMBER          PRIMARY KEY,
    EMAIL           VARCHAR2(100)   NOT NULL UNIQUE,
    PASSWORD        VARCHAR2(100)   NOT NULL,
    NICKNAME        VARCHAR2(30)    NOT NULL,
    IS_ADMIN        CHAR(1)         DEFAULT 'N' NOT NULL,
    STATUS          VARCHAR2(10)    DEFAULT 'ACTIVE' NOT NULL,
    JOINED_AT       DATE            DEFAULT SYSDATE NOT NULL,
    BIO             VARCHAR2(500),
    COUNTRY         VARCHAR2(50),
    REAL_NAME       VARCHAR2(50),
    NAME_PUBLIC     CHAR(1)         DEFAULT 'Y' NOT NULL,
    BIRTHDAY        DATE,
    BIRTHDAY_PUBLIC CHAR(1)         DEFAULT 'Y' NOT NULL,
    PHOTO_PATH      VARCHAR2(300)
);

-- 2. 게시글
CREATE TABLE POSTS (
    POST_ID     NUMBER          PRIMARY KEY,
    WRITER_ID   NUMBER          NOT NULL REFERENCES MEMBERS (MEMBER_ID),
    TITLE       VARCHAR2(200)   NOT NULL,
    CONTENT     VARCHAR2(4000)  NOT NULL,
    COUNTRY     VARCHAR2(3)     NOT NULL,
    STYLE       VARCHAR2(10)    NOT NULL,
    IMAGE_PATH  VARCHAR2(300),
    VIEW_COUNT  NUMBER          DEFAULT 0 NOT NULL,
    CREATED_AT  DATE            DEFAULT SYSDATE NOT NULL
);

-- 3. 댓글 (게시글이 지워지면 그 글의 댓글도 함께 지워진다)
CREATE TABLE COMMENTS (
    COMMENT_ID  NUMBER          PRIMARY KEY,
    POST_ID     NUMBER          NOT NULL REFERENCES POSTS (POST_ID) ON DELETE CASCADE,
    WRITER_ID   NUMBER          NOT NULL REFERENCES MEMBERS (MEMBER_ID),
    CONTENT     VARCHAR2(500)   NOT NULL,
    CREATED_AT  DATE            DEFAULT SYSDATE NOT NULL
);
```

| 컬럼 값 | 의미 |
|---|---|
| `MEMBERS.IS_ADMIN` | `Y` 관리자 / `N` 일반 회원 |
| `MEMBERS.STATUS` | `ACTIVE` 활동 중 (정지/탈퇴 기능을 만들면 다른 값을 추가) |
| `*_PUBLIC` | `Y` 공개 / `N` 비공개 |
| `POSTS.COUNTRY` | `KR` 국내 / `JP` 일본 / `ETC` 해외 |
| `POSTS.STYLE` | `healing` / `food` / `activity` / `shopping` |
| `POSTS.IMAGE_PATH` | 업로드한 사진 경로. `NULL`이면 국가별 기본 이미지를 쓴다. |

### 선택 테이블 (2단계: 기능을 붙일 때 추가)

```sql
CREATE SEQUENCE SEQ_POST_LOGS START WITH 1 INCREMENT BY 1;

-- 좋아요: 한 사람이 한 글에 한 번만 (PK가 (글, 회원) 조합이라 중복이 막힌다)
CREATE TABLE POST_LIKES (
    POST_ID     NUMBER  NOT NULL REFERENCES POSTS (POST_ID) ON DELETE CASCADE,
    MEMBER_ID   NUMBER  NOT NULL REFERENCES MEMBERS (MEMBER_ID),
    CREATED_AT  DATE    DEFAULT SYSDATE NOT NULL,
    PRIMARY KEY (POST_ID, MEMBER_ID)
);

-- 스크랩
CREATE TABLE SCRAPS (
    MEMBER_ID   NUMBER  NOT NULL REFERENCES MEMBERS (MEMBER_ID),
    POST_ID     NUMBER  NOT NULL REFERENCES POSTS (POST_ID) ON DELETE CASCADE,
    SCRAPPED_AT DATE    DEFAULT SYSDATE NOT NULL,
    PRIMARY KEY (MEMBER_ID, POST_ID)
);

-- 작업 이력 (관리자 화면용). 글이 지워져도 이력은 남아야 해서 POST_ID에는 FK를 걸지 않는다.
CREATE TABLE POST_LOGS (
    LOG_ID      NUMBER          PRIMARY KEY,
    LOG_TYPE    VARCHAR2(10)    NOT NULL,   -- CREATE / UPDATE / DELETE
    POST_ID     NUMBER          NOT NULL,
    POST_TITLE  VARCHAR2(200)   NOT NULL,
    ACTOR_ID    NUMBER          NOT NULL REFERENCES MEMBERS (MEMBER_ID),
    LOGGED_AT   DATE            DEFAULT SYSDATE NOT NULL
);
```

### Firestore 필드 → SQL 컬럼 대응

**`users` → `MEMBERS`**

| Firestore | SQL | 비고 |
|---|---|---|
| (문서 id = uid) | `MEMBER_ID` | 시퀀스 번호 |
| `email` | `EMAIL` | |
| (Firebase Auth가 보관) | `PASSWORD` | 해시한 값을 저장 |
| `nickname` | `NICKNAME` | |
| (`admins` 컬렉션) | `IS_ADMIN` | 별도 테이블 대신 컬럼 |
| `status` | `STATUS` | |
| `joinedDate` | `JOINED_AT` | |
| `bio` | `BIO` | |
| `country` + `countryOther` | `COUNTRY` | 합침: 화면에 보이는 국가 이름을 그대로 저장 |
| `name` / `nameVisibility` | `REAL_NAME` / `NAME_PUBLIC` | |
| `birthday` / `birthdayVisibility` | `BIRTHDAY` / `BIRTHDAY_PUBLIC` | |
| `photoUrl` | `PHOTO_PATH` | |
| `emailLocal`, `emailDomain`, `emailDomainOther` | (삭제) | `EMAIL` 하나로 통일 |

**`posts` → `POSTS`**

| Firestore | SQL | 비고 |
|---|---|---|
| (문서 id) | `POST_ID` | 시퀀스 번호 |
| `authorUid` | `WRITER_ID` | `MEMBERS`와 연결 |
| `authorNickname` | (삭제) | `MEMBERS`와 JOIN해서 가져옴 |
| `title` / `body` | `TITLE` / `CONTENT` | |
| `country` / `style` | `COUNTRY` / `STYLE` | 코드 그대로 |
| `countryLabel` | (삭제) | `COUNTRY` 코드로 화면에서 변환 |
| `image` + `imageUrl` | `IMAGE_PATH` | 합침: 업로드 사진이 없으면 `NULL` |
| `views` | `VIEW_COUNT` | |
| `createdAt` | `CREATED_AT` | |
| `comments`, `likes` | (삭제) | `COUNT(*)`로 계산 |

`comments/*` → `COMMENTS`, `likes/*` → `POST_LIKES`, `scraps/*` → `SCRAPS`, `postLogs` → `POST_LOGS`로 그대로 옮긴다. 컬럼 이름만 `authorUid` → `WRITER_ID`, `actorUid` → `ACTOR_ID`처럼 바뀐다.

---

## 3. 자주 쓰는 SQL (JDBC의 `?` 자리표시자 형식)

```sql
-- 회원가입
INSERT INTO MEMBERS (MEMBER_ID, EMAIL, PASSWORD, NICKNAME)
VALUES (SEQ_MEMBERS.NEXTVAL, ?, ?, ?);

-- 로그인 (이메일 + 비밀번호가 맞는 회원 1명 조회. 없으면 로그인 실패)
SELECT MEMBER_ID, NICKNAME, IS_ADMIN
  FROM MEMBERS
 WHERE EMAIL = ? AND PASSWORD = ? AND STATUS = 'ACTIVE';

-- 게시글 등록
INSERT INTO POSTS (POST_ID, WRITER_ID, TITLE, CONTENT, COUNTRY, STYLE, IMAGE_PATH)
VALUES (SEQ_POSTS.NEXTVAL, ?, ?, ?, ?, ?, ?);

-- 게시글 목록 (작성자 닉네임 + 댓글 수 포함, 최신순)
SELECT p.POST_ID, p.TITLE, m.NICKNAME, p.CREATED_AT, p.VIEW_COUNT,
       (SELECT COUNT(*) FROM COMMENTS c WHERE c.POST_ID = p.POST_ID) AS COMMENT_COUNT
  FROM POSTS p
  JOIN MEMBERS m ON m.MEMBER_ID = p.WRITER_ID
 ORDER BY p.CREATED_AT DESC;

-- 게시글 상세
SELECT p.*, m.NICKNAME
  FROM POSTS p
  JOIN MEMBERS m ON m.MEMBER_ID = p.WRITER_ID
 WHERE p.POST_ID = ?;

-- 댓글 목록 / 등록
SELECT c.COMMENT_ID, c.CONTENT, c.CREATED_AT, c.WRITER_ID, m.NICKNAME
  FROM COMMENTS c
  JOIN MEMBERS m ON m.MEMBER_ID = c.WRITER_ID
 WHERE c.POST_ID = ?
 ORDER BY c.CREATED_AT;

INSERT INTO COMMENTS (COMMENT_ID, POST_ID, WRITER_ID, CONTENT)
VALUES (SEQ_COMMENTS.NEXTVAL, ?, ?, ?);
```

---

## 4. 수정/삭제 권한

글 수정/삭제는 **로그인한 회원과 글 작성자가 같을 때만** 가능하다. 예외는 관리자(`IS_ADMIN = 'Y'`)이다.

| | 현재 (Firebase) | 나중 (Java/JSP + Oracle) |
|---|---|---|
| 로그인 정보 | Firebase Auth 세션 | `HttpSession`에 저장한 회원 번호와 `IS_ADMIN` |
| 화면 검사 | 본인/관리자일 때만 수정·삭제 버튼 표시 | 같음 (JSP에서 조건부로 버튼 출력) |
| **실제 방어선** | Firestore 보안 규칙 | 서블릿에서 세션 정보를 확인한 뒤 SQL 실행 |

화면에서 버튼을 숨기는 것만으로는 막을 수 없다(주소를 직접 호출하면 우회됨). 서블릿에서 반드시 다시 확인해야 한다.

가장 쉬운 방법은 관리자 여부에 따라 SQL 조건을 나누는 것이다.

```java
// 세션에서 꺼낸 값: memberId(회원 번호), isAdmin("Y"/"N")
// 일반 회원: 내가 쓴 글만 지워진다.   관리자: 모든 글을 지울 수 있다.
String sql = "Y".equals(isAdmin)
        ? "DELETE FROM POSTS WHERE POST_ID = ?"
        : "DELETE FROM POSTS WHERE POST_ID = ? AND WRITER_ID = ?";
// 삭제된 행 수(executeUpdate 결과)가 0이면 "권한이 없거나 글이 없음"으로 처리한다.
```

수정(`UPDATE POSTS SET ... WHERE POST_ID = ? [AND WRITER_ID = ?]`)도 같은 방식이다.

댓글 삭제는 **댓글 작성자, 글 작성자, 관리자** 셋 중 하나일 때 허용한다(현재 Firestore 규칙과 같음).
좋아요와 스크랩은 본인 행만 넣고 지울 수 있다(`MEMBER_ID = 세션의 회원 번호`).

---

## 5. 개발 순서 제안

1. **1단계**: `MEMBERS`, `POSTS`, `COMMENTS`로 회원가입, 로그인, 글 CRUD, 댓글을 완성한다.
2. **2단계**: `POST_LOGS`를 추가한다. (관리자 화면의 게시글 로그)
3. **3단계**: `POST_LIKES`, `SCRAPS`를 추가한다.
