-- ============================================================
-- 여행만들기 (trable-jdbc) Oracle 스키마
-- model 패키지의 DAO가 실제로 실행하는 SQL과 1:1로 맞춰져 있다.
-- 실행 순서: 이 파일 그대로 위에서 아래로.
-- ============================================================

-- 1. 회원
-- id = 로그인 아이디 = 이메일 (가입/로그인 화면이 "이메일" 입력 하나만 받으므로
--      별도의 "아이디" 컬럼을 두지 않고 id 자체를 이메일로 쓴다. MemberDTO 설명 참고.)
CREATE TABLE members (
    id               VARCHAR2(100)  PRIMARY KEY,
    pwd              VARCHAR2(100)  NOT NULL,
    nickname         VARCHAR2(30)   NOT NULL,
    is_admin         CHAR(1)        DEFAULT 'N' NOT NULL,
    join_date        DATE           DEFAULT SYSDATE NOT NULL,
    bio              VARCHAR2(500),
    country          VARCHAR2(10),               -- KR / JP / ETC
    country_other    VARCHAR2(50),                -- country가 ETC일 때 직접 입력값
    real_name        VARCHAR2(50),
    name_public      CHAR(1)        DEFAULT 'Y' NOT NULL,
    birthday         VARCHAR2(10),                -- "YYYY-MM-DD" 문자열 그대로 저장 (날짜 계산 안 함)
    birthday_public  CHAR(1)        DEFAULT 'Y' NOT NULL,
    photo_path       VARCHAR2(300)
);

-- 관리자 계정 만들기 (가입 화면으로 먼저 가입한 뒤 이 UPDATE로 관리자로 바꾼다)
-- UPDATE members SET is_admin = 'Y' WHERE id = '본인이 가입한 이메일';


-- 2. 게시글
CREATE SEQUENCE seq_posts START WITH 1 INCREMENT BY 1;

CREATE TABLE posts (
    bno         NUMBER          PRIMARY KEY,
    writer      VARCHAR2(100)   NOT NULL REFERENCES members(id),
    title       VARCHAR2(200)   NOT NULL,
    content     VARCHAR2(4000)  NOT NULL,
    country     VARCHAR2(3)     NOT NULL,          -- KR / JP / ETC
    style       VARCHAR2(10)    NOT NULL,          -- healing / food / activity / shopping
    img_path    VARCHAR2(300),                     -- "images/korea.jpg"(기본) 또는 "upload/post/xxx.jpg"(업로드)
    view_cnt    NUMBER          DEFAULT 0 NOT NULL,
    like_cnt    NUMBER          DEFAULT 0 NOT NULL,
    reg_date    DATE            DEFAULT SYSDATE NOT NULL,
    mod_date    DATE
);


-- 3. 댓글 (게시글이 지워지면 ON DELETE CASCADE로 함께 지워진다)
CREATE SEQUENCE seq_comments START WITH 1 INCREMENT BY 1;

CREATE TABLE comments (
    cno         NUMBER          PRIMARY KEY,
    bno         NUMBER          NOT NULL REFERENCES posts(bno) ON DELETE CASCADE,
    writer      VARCHAR2(100)   NOT NULL REFERENCES members(id),
    content     VARCHAR2(500)   NOT NULL,
    reg_date    DATE            DEFAULT SYSDATE NOT NULL
);


-- 4. 좋아요 (한 회원이 한 글에 한 번만 - PK가 (bno, member_id) 조합이라 중복이 막힌다)
CREATE TABLE post_likes (
    bno         NUMBER          NOT NULL REFERENCES posts(bno) ON DELETE CASCADE,
    member_id   VARCHAR2(100)   NOT NULL REFERENCES members(id),
    like_date   DATE            DEFAULT SYSDATE NOT NULL,
    PRIMARY KEY (bno, member_id)
);


-- 5. 스크랩
CREATE TABLE scraps (
    member_id    VARCHAR2(100)  NOT NULL REFERENCES members(id),
    bno          NUMBER         NOT NULL REFERENCES posts(bno) ON DELETE CASCADE,
    scrap_date   DATE           DEFAULT SYSDATE NOT NULL,
    PRIMARY KEY (member_id, bno)
);


-- 6. 게시글 작업 이력 (관리자 화면용)
-- 글이 삭제돼도 이력은 남아야 하므로 bno에는 FK를 걸지 않고, post_title은 당시 값을 복사해 저장한다.
CREATE SEQUENCE seq_post_logs START WITH 1 INCREMENT BY 1;

CREATE TABLE post_logs (
    log_id      NUMBER          PRIMARY KEY,
    log_type    VARCHAR2(10)    NOT NULL,          -- CREATE / UPDATE / DELETE
    bno         NUMBER          NOT NULL,
    post_title  VARCHAR2(200)   NOT NULL,
    actor_id    VARCHAR2(100)   NOT NULL REFERENCES members(id),
    log_date    DATE            DEFAULT SYSDATE NOT NULL
);
