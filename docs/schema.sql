-- ============================================================
-- trable-jdbc Oracle schema
-- Matches the SQL that the DAO classes in the model package run, 1:1.
-- Run this file top to bottom, as-is, logged in as the app's DB user (e.g. "trable").
--
-- NOTE ON ENCODING: comments in this file are kept in plain ASCII on purpose.
-- Korean comments here previously caused SQL*Plus (on a Windows client whose
-- NLS_LANG didn't match the file's UTF-8 encoding) to silently misparse a
-- line and DROP the next column definition -- no error was printed, the
-- CREATE TABLE just "succeeded" with one column missing (MEMBERS.REAL_NAME).
-- If you want to add your own Korean comments, first run (in the same
-- session, before this script):  SET SQLPROMPT ""  and make sure your
-- Windows console codepage / NLS_LANG matches the file's encoding --
-- otherwise verify with DESC <table> afterwards that every column exists.
-- ============================================================

-- 1. Members
-- id = login id = email address. The signup/login screens only ask for one
-- "email" field, so that value is used directly as the primary key instead
-- of adding a separate "id" field (see MemberDTO's class comment).
CREATE TABLE members (
    id               VARCHAR2(100)  PRIMARY KEY,
    pwd              VARCHAR2(100)  NOT NULL,
    nickname         VARCHAR2(30)   NOT NULL,
    is_admin         CHAR(1)        DEFAULT 'N' NOT NULL,
    join_date        DATE           DEFAULT SYSDATE NOT NULL,
    bio              VARCHAR2(500),
    country          VARCHAR2(10),                -- KR / JP / ETC
    country_other    VARCHAR2(50),                -- free-text value when country = ETC
    real_name        VARCHAR2(50),
    name_public      CHAR(1)        DEFAULT 'Y' NOT NULL,
    birthday         VARCHAR2(10),                -- "YYYY-MM-DD" stored as plain text, never parsed
    birthday_public  CHAR(1)        DEFAULT 'Y' NOT NULL,
    photo_path       VARCHAR2(300)
);

-- Make an account an admin (sign up through the normal form first, then run):
-- UPDATE members SET is_admin = 'Y' WHERE id = 'the email you signed up with';
-- COMMIT;


-- 2. Posts
CREATE SEQUENCE seq_posts START WITH 1 INCREMENT BY 1;

CREATE TABLE posts (
    bno         NUMBER          PRIMARY KEY,
    writer      VARCHAR2(100)   NOT NULL REFERENCES members(id),
    title       VARCHAR2(200)   NOT NULL,
    content     VARCHAR2(4000)  NOT NULL,
    country     VARCHAR2(3)     NOT NULL,          -- KR / JP / ETC
    style       VARCHAR2(10)    NOT NULL,          -- healing / food / activity / shopping
    img_path    VARCHAR2(300),                     -- "images/korea.jpg" (default) or "upload/post/xxx.jpg" (uploaded)
    view_cnt    NUMBER          DEFAULT 0 NOT NULL,
    like_cnt    NUMBER          DEFAULT 0 NOT NULL,
    reg_date    DATE            DEFAULT SYSDATE NOT NULL,
    mod_date    DATE
);


-- 3. Comments (cascades when the post is deleted)
CREATE SEQUENCE seq_comments START WITH 1 INCREMENT BY 1;

CREATE TABLE comments (
    cno         NUMBER          PRIMARY KEY,
    bno         NUMBER          NOT NULL REFERENCES posts(bno) ON DELETE CASCADE,
    writer      VARCHAR2(100)   NOT NULL REFERENCES members(id),
    content     VARCHAR2(500)   NOT NULL,
    reg_date    DATE            DEFAULT SYSDATE NOT NULL
);


-- 4. Likes (one member can like a post at most once -- the (bno, member_id)
-- primary key enforces that)
CREATE TABLE post_likes (
    bno         NUMBER          NOT NULL REFERENCES posts(bno) ON DELETE CASCADE,
    member_id   VARCHAR2(100)   NOT NULL REFERENCES members(id),
    like_date   DATE            DEFAULT SYSDATE NOT NULL,
    PRIMARY KEY (bno, member_id)
);


-- 5. Scraps (bookmarks)
CREATE TABLE scraps (
    member_id    VARCHAR2(100)  NOT NULL REFERENCES members(id),
    bno          NUMBER         NOT NULL REFERENCES posts(bno) ON DELETE CASCADE,
    scrap_date   DATE           DEFAULT SYSDATE NOT NULL,
    PRIMARY KEY (member_id, bno)
);


-- 6. Post action log (admin screen). No FK on bno -- a deleted post's log
-- entries must stay, so post_title is copied in at the time of the action
-- instead of being looked up later.
CREATE SEQUENCE seq_post_logs START WITH 1 INCREMENT BY 1;

CREATE TABLE post_logs (
    log_id      NUMBER          PRIMARY KEY,
    log_type    VARCHAR2(10)    NOT NULL,          -- CREATE / UPDATE / DELETE
    bno         NUMBER          NOT NULL,
    post_title  VARCHAR2(200)   NOT NULL,
    actor_id    VARCHAR2(100)   NOT NULL REFERENCES members(id),
    log_date    DATE            DEFAULT SYSDATE NOT NULL
);
