# 구조 설계 (ARCHITECTURE): Firebase 프로토타입 → 이 저장소(Oracle JDBC, MVC2)

원작성일: 2026-09-21 / 2026-10-03 MVC2·Command 패턴 반영 / 이 저장소로 복사: 2026-10-03

이 문서는 자매 저장소 `ai2giTrable`에서 Firebase로 먼저 완성한 목업과, **이 저장소에서 실제로 만들 Oracle JDBC + MVC2**(서블릿 하나가 가상 주소들을 받아 Command로 나누고, DB가 필요한 것만 DTO/DAO로 넘기는 구조) 구현이 **같은 기능을 어떻게 다른 방식으로 만드는지** 비교하고, 이 저장소에서 만들어야 할 클래스 목록을 정리한다.

수업에서 다룬 `BlogProject`(프론트 컨트롤러 + Command 패턴 예제)의 클래스 이름·방식을 그대로 따른다. 테이블/컬럼 설계는 [DB-SCHEMA.md](DB-SCHEMA.md)에 있고, 이 문서는 **"코드가 어디에 있고, 누가 DB를 지키는가"**에 집중한다. 이 저장소에서 실제로 만들어야 할 클래스 목록은 맨 아래 **9번**(그리고 체크리스트로 뽑아놓은 [PROGRESS.md](../PROGRESS.md))에 정리했다.

---

## 1. 한 줄 요약

| | 지금 (Firebase) | 나중 (Oracle JDBC) |
|---|---|---|
| DB에 누가 접속하는가 | **브라우저**가 Firestore에 직접 접속 | **Java 서버**만 Oracle에 접속. 브라우저는 서버 근처도 못 감 |
| DB를 지키는 것 | Firebase **보안 규칙**(`firestore.rules`) | **Java 코드**(DAO, 서블릿의 `if`문) |
| 로그인 상태를 기억하는 곳 | 브라우저(Firebase Auth 세션) | 서버(`HttpSession`) |
| "게시글 쓰기" 버튼을 누르면 | 브라우저 JS → Firestore로 바로 저장 | 브라우저 → 서버(JSP/Servlet) → DAO → Oracle |

가장 큰 차이는 이것이다. **지금은 브라우저가 DB와 직접 대화하고, 그 대화 내용이 옳은지를 "규칙"이 검사한다.** 나중에는 **브라우저는 서버하고만 대화하고, DB와의 대화는 전부 서버(Java) 안에서 끝난다.** 그래서 "규칙"이라는 별도의 개념이 아예 없어지고, 그 자리를 **평범한 Java `if`문**이 대신한다.

---

## 2. 계층 대응표

지금 쓰는 JS 파일들과, 앞으로 만들 Java 계층이 하는 일은 같다. 이름과 언어만 다르다.

| 하는 일 | 지금 (JS) | 나중 (Java, MVC2) |
|---|---|---|
| 주소를 받아서 작업을 나눠주는 곳 | 없음 (각 `.jsp`가 자기 주소를 자기가 처리) | **컨트롤러**(서블릿) 하나가 `/post/write`, `/post/delete` 같은 여러 주소를 한꺼번에 받아, `switch`로 알맞은 **Command**를 고른다. 문제에서 말한 **Main**이 바로 이 컨트롤러다. (BlogProject의 `BoardController`) |
| 작업 하나(글쓰기, 좋아요 등)를 실제로 처리하는 곳 | `.jsp`의 `<script>` 안 함수 하나 | **Command**: `doCommand()` 메서드 하나짜리 클래스. 작업 1개 = 클래스 1개. (`BoardWriteCommand`, `LikeCommand` …) |
| "게시글 1건"을 표현하는 자료 모양 | JS 객체 `{title, body, authorUid, ...}` | **DTO**: `PostDTO`(Firebase 맥락에선 이 이름, BlogProject 실제 이름은 `BoardDto`) 클래스 (`getTitle()`, `setTitle()` …) |
| DB에 SQL을 실제로 날리는 곳 | `posts-store.js`의 `createPost()`, `getAllPosts()` 등 | **DAO**: `PostDAO`(BlogProject의 `BoardDAO`) 클래스의 `insert()`, `getList()` 등. Command는 이 메서드를 호출만 하고 SQL은 모른다. |
| DB 연결을 맺고 끊는 곳 | Firebase SDK가 알아서 처리 (`firebase.firestore()`) | **DBM**: `DBUtil.getInstance()`처럼 `Connection`을 만들어주는 공통 클래스 하나를 모든 DAO가 가져다 씀 |
| 로그인 여부 확인 | `firebase.auth().currentUser` | `HttpSession`의 `session.getAttribute("loginMember")` |
| 권한 검사("내 글만 지울 수 있다") | Firestore **보안 규칙** | `PostDAO.delete()` 안의 SQL `WHERE ... AND WRITER_ID=?` 조건, 또는 Command 안의 `if` |

**화면(`.jsp`)이 하는 일도 줄어든다.** 지금은 `.jsp`의 `<script>`가 Firestore를 직접 부르고 화면도 그리지만, MVC2에서는 컨트롤러가 Command를 실행해 결과를 `request.setAttribute(...)`로 담아준 뒤 그 JSP로 `forward`한다. JSP는 그 결과를 `<%= %>`나 JSTL로 **찍기만** 한다 — 지금 식으로 치면 "화면 그리는 역할만 남고, Firestore를 부르던 부분은 전부 Command/DAO로 이사간다"는 뜻이다.

이 프로젝트 안에서 실제로 대응되는 파일을 짝지으면 이렇다.

| 지금 | 나중 |
|---|---|
| `js/posts-store.js` | `model/PostDAO.java`, `model/CommentDAO.java` (좋아요·스크랩도 보통 `PostDAO`에 같이 둠) |
| `js/mock-auth.js`의 로그인 상태 부분 | `HttpSession`(`loginMember`) — `LoginCommand`가 만들고, 이후 모든 Command가 읽음 |
| `js/mock-auth.js`의 `isAdmin()` | `MemberDTO.isAdmin()` (세션에 들어있는 로그인 회원 정보의 필드 하나, `IS_ADMIN` 컬럼) |
| `common/firebase-init.jspf` | `util/DBUtil.java` (DBM) — Oracle 접속 정보와 `Connection` 생성 (BlogProject와 동일) |
| Firestore 문서 하나 (`posts/{id}`) | `PostDTO` 객체 하나 (Java 인스턴스) |
| `firebase/firestore.rules` | 없음(파일이 아니라 **여러 Java 파일에 나눠 들어감**) — 아래 4번에서 자세히 설명 |

---

## 3. 로그인/세션 비교

### 지금 (Firebase Auth)

1. 로그인하면 Firebase가 브라우저의 IndexedDB에 로그인 토큰을 저장한다.
2. 페이지를 새로 열 때마다 `firebase.auth().onAuthStateChanged()`가 그 토큰을 읽어 **브라우저 안에서** 로그인 상태를 복원한다. (`window.authReady`로 기다리게 만든 그 부분)
3. Firestore에 쓰기를 할 때마다 그 토큰이 자동으로 요청에 실려가고, **Firebase 서버가** 토큰의 uid를 규칙의 `request.auth.uid`로 사용한다.
4. 즉 "내가 로그인했다"는 사실을 **브라우저가 계속 들고 다니고, 매번 Firebase에게 증명**한다.

### 나중 (Java `HttpSession`)

1. 로그인 폼을 제출하면 **서버의 로그인 서블릿**이 `MEMBERS` 테이블에서 이메일/비밀번호를 SQL로 조회한다.
2. 맞으면 `HttpSession session = request.getSession();`으로 세션을 만들고 `session.setAttribute("loginMember", memberDTO);`로 로그인 회원 정보를 **서버 메모리에** 저장한다.
3. 브라우저는 `JSESSIONID`라는 쿠키만 들고 다닌다. 로그인 정보 자체는 브라우저에 없다.
4. 이후 어떤 JSP/서블릿이든 `request.getSession().getAttribute("loginMember")`로 "지금 이 요청을 보낸 사람이 누구인지"를 서버 안에서 바로 알 수 있다.
5. 로그아웃은 `session.invalidate()`.

**비유하면:** 지금은 손님(브라우저)이 신분증(토큰)을 들고 다니면서 상점(Firebase)마다 직접 보여준다. 나중에는 손님이 매표소(서버)에서 표(JSESSIONID)만 받고, 매표소 안쪽 창고(Oracle)는 직원(Java)만 드나든다. 손님은 창고 열쇠를 아예 가진 적이 없다.

---

## 4. "규칙"은 어디로 가는가 — 가장 중요한 부분

지금 `firebase/firestore.rules`에 있는 한 줄 한 줄이, 나중에는 **DAO 메서드 안의 SQL이나 서블릿의 `if`문**으로 흩어져 들어간다. 대응표로 보면 이렇다.

| Firestore 규칙 (지금) | Java로 옮기면 (나중) |
|---|---|
| `match /posts/{postId} { allow read: if true; }` | 그냥 아무 `if`도 없이 `SELECT * FROM POSTS`를 실행하는 서블릿을 만들면 된다. (모두가 볼 수 있음 = 애초에 검사할 게 없음) |
| `allow create: if isSignedIn() && request.resource.data.authorUid == request.auth.uid;` | 글쓰기 서블릿 맨 앞에: <br>`MemberDTO loginMember = (MemberDTO) session.getAttribute("loginMember");`<br>`if (loginMember == null) { response.sendRedirect("login.jsp"); return; }`<br>그 다음 INSERT할 때 `WRITER_ID` 컬럼에 무조건 `loginMember.getMemberId()`를 넣는다(화면에서 받은 값을 쓰지 않는다). |
| `allow update, delete: if authorUid == request.auth.uid \|\| isAdmin();` | 수정/삭제 서블릿에서: <br>`if (!loginMember.getMemberId().equals(post.getWriterId()) && !loginMember.isAdmin()) { response.sendError(403); return; }`<br>또는 DAO의 SQL 자체에 조건을 건다: <br>`DELETE FROM POSTS WHERE POST_ID=? AND (WRITER_ID=? OR ? = 'Y')` |
| `allow update: if ... \|\| onlyCounterChange();` (좋아요 수만 바꾸는 건 허용) | 애초에 "좋아요 수 바꾸기"와 "글 수정하기"를 **다른 메서드**로 분리한다: `PostDAO.increaseLike(postId)`는 권한 검사 없이 호출 가능, `PostDAO.updatePost(dto)`는 작성자 확인 필요. Firestore처럼 "한 문서의 필드 일부만 허용"할 필요가 없다 — SQL은 컬럼 단위로 UPDATE 문을 따로 쓰면 그만이다. |
| `match /comments/{commentId} { allow delete: if 댓글작성자 \|\| 글작성자 \|\| 관리자; }` | `CommentDAO.deleteComment()` 호출 전에 서블릿에서 세 조건을 `||`로 검사. SQL로 하고 싶으면 `DELETE FROM COMMENTS WHERE COMMENT_ID=? AND (WRITER_ID=? OR ? IN (SELECT WRITER_ID FROM POSTS WHERE POST_ID=?) OR ?='Y')` |
| `match /users/{uid} { allow update: if request.auth.uid == uid; }` | 프로필 수정 서블릿에서: 화면(hidden input 등)에서 회원번호를 받더라도 **믿지 않고**, `UPDATE MEMBERS SET ... WHERE MEMBER_ID = ?`의 `?` 자리에는 **세션에 든 값**(`loginMember.getMemberId()`)만 쓴다. 이게 핵심이다 — 화면이 보낸 id를 그대로 믿으면 남의 정보를 바꿀 수 있게 된다. |
| `admins/{uid}` 컬렉션 존재 여부로 관리자 판별 | `MEMBERS.IS_ADMIN` 컬럼 하나. 로그인할 때 이미 세션에 실려있어서 매번 DB를 다시 볼 필요도 없다. |
| `request.resource.data.body.size() <= 500` (댓글 500자 제한) | Java에서 `if (content.length() > 500) { ...에러... }`. 이것도 이중으로 하면 더 안전: HTML `<textarea maxlength="500">`(사용자 편의) + 서버 Java 검사(진짜 방어선). |
| Storage의 `request.resource.size < 5MB`, `contentType.matches('image/.*')` | 파일 업로드 서블릿(주로 `Apache Commons FileUpload` 라이브러리 사용)에서 `if (file.getSize() > 5*1024*1024) {...}`, 확장자/MIME 타입 검사를 Java로 직접. |

### 왜 파일 하나가 아니라 여러 곳에 흩어지는가

Firestore 규칙은 "DB 앞을 지키는 문지기 하나"였다. **모든** 접근이 반드시 그 문을 통과해야 했다 — 브라우저가 Firestore에 직접 붙기 때문이다.

Oracle은 애초에 브라우저가 직접 접속할 수 없고 **Java 코드를 거쳐야만** 접속된다. 그래서 "문지기 하나"가 필요 없고, **접속 통로(서블릿/DAO) 하나하나에 검사 코드를 심는** 방식이 된다. 검사가 사라지는 게 아니라, **위치가 "DB 서버 앞"에서 "Java 코드 안"으로 옮겨갈 뿐**이다. 오히려 실수로 검사를 빠뜨린 통로가 생기지 않도록, 다음처럼 **공통화**해서 관리하는 게 일반적이다.

- **로그인 여부 검사**: 모든 서블릿 맨 앞에서 반복하지 않고, `LoginFilter`(서블릿 필터) 하나가 `/mypage/*`, `/posts/write` 같은 주소들을 가로채서 미로그인이면 로그인 페이지로 보낸다. (Firestore의 `isSignedIn()` 함수 하나를 여러 규칙이 재사용하던 것과 같은 발상)
- **본인/관리자 확인**: 공통 메서드 `AuthUtil.checkOwnerOrAdmin(loginMember, post)`를 만들어 여러 서블릿에서 재사용한다.

---

## 5. 요청 하나의 흐름 비교 (댓글 삭제를 예로)

### 지금 (Firebase)

```
[브라우저]
  댓글 삭제 버튼 클릭
    → JS: postsStore.deleteComment(postId, commentId)
      → Firestore SDK가 delete 요청을 Firebase 서버로 전송
        (요청에 로그인 토큰이 자동으로 실림)

[Firebase 서버]
  보안 규칙 검사
    → request.auth.uid가 댓글 작성자/글 작성자/관리자 중 하나인가?
    → 통과 → 문서 삭제, 실패 → "Missing or insufficient permissions" 에러 반환

[브라우저]
  성공/실패 결과를 받아 화면 갱신
```

### 나중 (Oracle JDBC, MVC2 — BlogProject의 `ReplyDeleteCommand`와 같은 방식)

```
[브라우저]
  댓글 삭제 버튼 클릭
    → POST /trable/comment/delete (postId, commentId를 파라미터로)

[Tomcat: PostController (컨트롤러, BlogProject의 BoardController에 해당)]
  1) getServletPath()로 주소가 "/comment/delete"인 걸 확인
  2) switch문에서 cmd = new CommentDeleteCommand(); 로 작업 담당자를 고름
  3) cmd.doCommand(request, response) 호출 — 여기서부턴 Command가 전부 처리

[CommentDeleteCommand.doCommand()]
  1) HttpSession에서 loginMember 꺼내기 → 없으면 로그인 페이지로 이동 (권한 검사 1)
  2) CommentDAO로 댓글/게시글 작성자를 조회 (DAO 내부에서 DBUtil.getInstance()로 Connection을 빌림)
  3) loginMember와 댓글 작성자/글 작성자/관리자 비교 → 아니면 거부 (권한 검사 2)
  4) 통과하면 CommentDAO.delete(commentId, ...) 호출 → 내부에서 PreparedStatement로 DELETE 실행
  5) try-with-resources라 Connection은 메서드가 끝나며 자동으로 반납(close)됨
  6) 처리 결과를 JSON으로 응답(Ajax)하거나 false를 반환(= "forward 하지 마, 내가 이미 응답함")

[PostController]
  Command가 true를 돌려줬을 때만 정해둔 JSP로 forward. false면 Command가 이미 응답을 끝낸 것

[브라우저]
  갱신된 화면/JSON을 받는다
```

**차이의 핵심**: Firebase 버전은 "요청 → 규칙 검사 → DB"가 Firebase 서버 안에서 한 번에 일어난다. JDBC 버전은 "요청 → 컨트롤러(주소 분배) → Command(권한 검사 + 흐름 제어) → DAO(SQL 실행) → DBM(연결 관리)"로 **역할이 클래스별로 쪼개져 있다.** 이게 MVC2(DTO/DAO/DBM/컨트롤러+Command) 구조를 쓰는 이유이기도 하다 — 한 클래스가 한 가지 일만 하게 나눠서, 나중에 수정하거나 실수를 찾기 쉽게 만드는 것이다.

---

## 6. 최소 코드 골격 (MVC2 — BlogProject와 같은 모양)

실제 코드는 아니고, "이런 모양이 된다"는 걸 보여주는 뼈대다. 클래스 이름·패턴(Command, 컨트롤러의 switch 분배, DAO의 `WHERE`절 권한 검사)은 `BlogProject`(수업 예제)를 그대로 따랐다.

> **한 가지 차이**: BlogProject는 회원 아이디(로그인 id, 문자열)를 그대로 기본키로 쓴다(`MEMBER.ID`, `BOARD.WRITER`도 문자열). 아래 예시와 [DB-SCHEMA.md](DB-SCHEMA.md)는 숫자로 된 **대리 키**(`MEMBER_ID`, `WRITER_ID`)를 쓰도록 설계했다 — 더 일반적으로 권장되는 방식이라서다. 둘 중 뭘 따를지는 자유다: **BlogProject와 똑같이** 하고 싶으면 `MemberDTO.getId()`(String)를 그대로 쓰고 `POSTS.WRITER` 컬럼도 `VARCHAR2`로 바꾸면 되고(DB-SCHEMA.md의 `MEMBER_ID`/`WRITER_ID`를 전부 `ID`/`WRITER`로 바꿔 읽으면 된다), **DB-SCHEMA.md 그대로** 가고 싶으면 아래 코드처럼 숫자를 쓰면 된다. 수업 진도/과제 기준에 맞는 쪽으로 고르면 된다.

```java
// ── DTO: 게시글 한 건을 담는 상자. 필드 이름은 DB 컬럼과 맞춘다. (model 패키지, BlogProject의 BoardDto)
public class PostDTO {
    private long postId;
    private long writerId;
    private String title;
    private String content;
    private String country;
    private String style;
    private String imagePath;
    private int viewCount;
    private String createdAt;
    // getter / setter 전부
}
```

```java
// ── DBM: Connection을 만들고 돌려주는 곳. 프로젝트 전체가 이거 하나만 씀. (util 패키지, BlogProject의 DBUtil)
public class DBUtil {
    public static Connection getInstance() {
        Connection conn = null;
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            conn = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:xe", "trable", "****");
        } catch (Exception e) {
            System.out.println("[DB 연결 실패] " + e.getMessage());
        }
        return conn;
    }
}
```

```java
// ── DAO: SQL을 실제로 실행하는 곳. 권한 조건은 SQL의 WHERE에 바로 건다. (model 패키지, BlogProject의 BoardDAO)
public class PostDAO {

    public int insert(PostDTO post) {
        String sql = "insert into posts(post_id, writer_id, title, content, country, style) "
                   + "values(seq_posts.nextval, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, post.getWriterId());   // ← 화면 값이 아니라 세션의 회원 id여야 한다!
            pstmt.setString(2, post.getTitle());
            pstmt.setString(3, post.getContent());
            pstmt.setString(4, post.getCountry());
            pstmt.setString(5, post.getStyle());
            return pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    // 본인 글일 때만 지워지도록 where절에 writer_id까지 건다 (BoardDAO.delete와 같은 방식)
    public int delete(long postId, long writerId) {
        String sql = "delete from posts where post_id = ? and writer_id = ?";

        try (Connection conn = DBUtil.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, postId);
            pstmt.setLong(2, writerId);
            return pstmt.executeUpdate();   // 0이면 "권한 없음 또는 글 없음"

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }
}
```

```java
// ── Command: 작업 하나를 처리하는 단위. 모든 Command가 이 규칙을 따른다. (service 패키지, BlogProject의 Command)
public interface Command {
    // true  → 처리 끝, 컨트롤러가 정해둔 JSP로 forward
    // false → 이미 응답을 끝냈으니 forward 하지 마 (redirect/JSON 등)
    boolean doCommand(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException;
}
```

```java
// ── 글 삭제 Command (service 패키지, BlogProject의 BoardDeleteCommand와 같은 방식)
public class PostDeleteCommand implements Command {

    @Override
    public boolean doCommand(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        MemberDTO loginMember = (MemberDTO) request.getSession().getAttribute("loginMember");

        long postId = Long.parseLong(request.getParameter("postId"));

        //관리자는 모든 글을, 일반 회원은 writer_id가 일치하는 글만 지워지게 DAO에서 구분해도 되고,
        //여기서 먼저 조회해 작성자/관리자인지 확인한 뒤 delete를 호출해도 된다. (BlogProject는 전자 방식)
        int deletedRows = new PostDAO().delete(postId, loginMember.getMemberId());   // 숫자 PK 기준 예시 (위 안내 참고)

        if (deletedRows == 0) {
            request.setAttribute("msg", "권한이 없거나 글이 없습니다.");
            return true;   // 다시 상세 화면 등으로 forward
        }

        response.sendRedirect(request.getContextPath() + "/post/list");
        return false;   // 이미 redirect 했으니 forward 금지
    }
}
```

```java
// ── 컨트롤러(Main): 여러 주소를 한곳에서 받아 Command로 나눠준다. (controller 패키지, BlogProject의 BoardController)
@WebServlet({"/post/list", "/post/view", "/post/write", "/post/modify", "/post/delete"})
public class PostController extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException { doAction(request, response); }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException { doAction(request, response); }

    protected void doAction(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String path = request.getServletPath();   // 예: "/post/delete"
        String method = request.getMethod();
        String page = null;
        Command cmd = null;

        //로그인이 필요한 주소는 여기서 한 번에 막는다 (여러 Command에서 중복 검사하지 않도록)
        MemberDTO loginMember = (MemberDTO) request.getSession().getAttribute("loginMember");
        if (("/post/write".equals(path) || "/post/modify".equals(path) || "/post/delete".equals(path))
                && loginMember == null) {
            response.sendRedirect(request.getContextPath() + "/member/login");
            return;
        }

        switch (path) {
            case "/post/list":   cmd = new PostListCommand();   page = "/posts.jsp";         break;
            case "/post/view":   cmd = new PostViewCommand();   page = "/posts/detail.jsp";  break;
            case "/post/write":  if ("POST".equals(method)) cmd = new PostWriteCommand();
                                  page = "/posts/write.jsp";                                 break;
            case "/post/delete": if ("POST".equals(method)) cmd = new PostDeleteCommand();   break;
        }

        boolean isForward = true;
        if (cmd != null) {
            isForward = cmd.doCommand(request, response);
        }
        if (isForward && page != null) {
            request.getRequestDispatcher(page).forward(request, response);
        }
    }
}
```

이 골격에서 보듯, **지금 `firebase/firestore.rules`의 `allow delete: if authorUid == uid || isAdmin();` 한 줄이, `PostController`의 로그인 검사(미로그인 차단) + `PostDeleteCommand`의 흐름 제어 + `PostDAO.delete()`의 SQL `WHERE` 조건, 이렇게 세 군데로 나뉘어 들어갔다.**

---

## 7. 파일 구조 제안 (BlogProject와 같은 패키지 구성)

BlogProject는 dto/dao를 패키지로 나누지 않고 **`model` 패키지 하나에 DTO와 DAO를 같이** 둔다(`BoardDto`+`BoardDAO`가 둘 다 `model`). 여행 블로그도 같은 방식을 쓰면 수업에서 쓰던 구조와 100% 같아진다.

```
main/
  webapp/                  ← 지금과 동일 (화면: .jsp, .css, .js, .images)
    index.jsp
    posts/
      write.jsp            ← 화면만 (입력 폼 출력, <form action="<contextPath>/post/write" method="post">)
      detail.jsp
    WEB-INF/
      web.xml
      lib/
        ojdbc11.jar         ← Oracle JDBC 드라이버
  src/
    main/java/
      controller/           ← Main(실행 전용) — @WebServlet이 붙은 "주소 분배" 서블릿들
        PostController.java     (/post/list, /post/view, /post/write, /post/modify, /post/delete, /post/like, /post/scrap)
        CommentController.java  (/comment/write, /comment/delete)  ※PostController에 합쳐도 됨 (BlogProject가 그렇게 함)
        MemberController.java   (/member/join, /member/login, /member/logout, /member/idCheck, /member/mypage)
        AdminController.java    (/admin/postLogs)
      service/              ← Command.java(인터페이스) + 작업 하나당 *Command.java 클래스들
        Command.java
        PostListCommand.java, PostViewCommand.java, PostWriteCommand.java,
        PostModifyCommand.java, PostDeleteCommand.java, LikeCommand.java, ScrapCommand.java
        CommentWriteCommand.java, CommentDeleteCommand.java
        JoinCommand.java, LoginCommand.java, LogoutCommand.java, IdCheckCommand.java, MypageCommand.java
      model/                 ← DTO + DAO를 같이 둠 (BlogProject 방식)
        MemberDTO.java, MemberDAO.java
        PostDTO.java, PostDAO.java          ← 좋아요/스크랩 메서드도 여기 포함 가능 (BlogProject의 BoardDAO가 좋아요까지 다루는 것과 같음)
        CommentDTO.java, CommentDAO.java
        PostLogDTO.java, PostLogDAO.java
      util/
        DBUtil.java           ← DBM
        ParamUtil.java        ← 파라미터를 숫자로 안전하게 변환
        EscapeUtil.java       ← XSS 방지(HTML 이스케이프), JSON 이스케이프
        UploadUtil.java       ← 게시글 사진 업로드/삭제
```

지금 구조(`main/webapp` 하나)와 비교하면, **`src/main/java` 아래에 controller/service/model/util이 새로 생기는 것**이 가장 큰 변화다. `.jsp`는 남지만 하는 일이 줄어든다 — 지금은 `.jsp` 안의 `<script>`가 Firestore를 직접 부르고 화면도 그리지만, 나중에는 컨트롤러·Command가 이미 처리해서 `request`에 담아준 결과를 `.jsp`가 **찍기만** 하는 역할로 축소된다.

---

## 8. 정리: 지금 배운 개념이 그대로 쓰인다

Firebase로 이미 만들어본 개념들이 이름만 바뀌어 그대로 재사용된다.

| 지금 이미 한 것 | 나중에 이름 |
|---|---|
| "로그인 안 했으면 로그인 페이지로 보낸다" (`mockAuth.isLoggedIn()` 검사) | `LoginCheckFilter` 또는 각 서블릿 앞의 세션 검사 |
| "내 글이거나 관리자면 수정/삭제 버튼 보여주기" | 화면(JSP)에서는 그대로 유지(사용자 편의), **진짜 방어는** 서블릿/DAO로 이동 |
| "글을 지우면 댓글/좋아요도 같이 지운다" (`deletePostChildren`) | `PostDAO.deletePost()` 안에서 `DELETE FROM COMMENTS WHERE POST_ID=?` 등을 순서대로 실행 (또는 테이블에 `ON DELETE CASCADE`를 걸어 Oracle이 자동으로 처리 — DB-SCHEMA.md의 `COMMENTS`, `POST_LIKES`, `SCRAPS`에 이미 걸어둠) |
| "닉네임을 바꾸면 옛 글에도 바로 반영" (`resolveNicknames`) | SQL의 `JOIN`: `SELECT p.*, m.NICKNAME FROM POSTS p JOIN MEMBERS m ON ...`으로 **항상 최신 닉네임을 그때그때 가져오므로** 이 문제 자체가 원래 안 생긴다 (DB-SCHEMA.md 2번 참고) |
| Firestore 규칙 파일 하나로 몰아서 관리 | 대신 DAO/서블릿 코드 리뷰, 그리고 "화면이 보낸 값을 그대로 믿지 않는다"는 원칙을 팀(본인) 스스로 지키는 것으로 대체 |

가장 명심할 한 가지는: **Firestore 규칙이 없어진다고 검사를 안 해도 되는 게 아니라, 그 검사를 이제 Java 코드가 직접, 매번 손으로 써줘야 한다**는 것이다. 특히 "화면에서 보낸 회원 id·작성자 id를 그대로 믿지 말고, 항상 `session`에 든 로그인 정보를 기준으로 판단한다"는 원칙이 SQL Injection 방지(`PreparedStatement` 사용)와 함께 Oracle 버전에서 가장 중요한 보안 규칙이 된다.

---

## 9. 여행 블로그 프로젝트 전환 목록 (BlogProject 패턴 그대로 적용)

지금 `posts-store.js`, `mock-auth.js`의 함수들을 BlogProject 방식의 클래스로 옮기면 아래 표와 같다. **지금 만든 기능과 1:1로 짝지었으니, 이 표가 곧 작업 목록이다.**

### 9-1. 회원 (MemberController)

| 주소 | Command | 지금 하던 일 (JS) | DAO 메서드 (MemberDAO) |
|---|---|---|---|
| GET/POST `/member/join` | `JoinCommand` | (지금은 alert만 하는 목업) `auth/signup.jsp` | `idCheck(id)`, `join(dto)` |
| GET/POST `/member/login` | `LoginCommand` | `auth/login.jsp`의 `firebase.auth().signInWithEmailAndPassword` | `login(id, pwd)` |
| `/member/logout` | `LogoutCommand` | 헤더의 로그아웃 (`mockAuth.logout()`) | 없음 (`session.invalidate()`만) |
| `/member/idCheck` (Ajax) | `IdCheckCommand` | (지금 없음, 새로 추가) | `idCheck(id)` |
| GET `/member/mypage` | `MypageCommand` | `mypage.jsp`의 프로필 카드 | `MemberDAO.find... `, 아래 `PostDAO.getListByWriter/getLikedList`도 같이 호출 |
| (`mypage/edit.jsp` 저장) | `ProfileModifyCommand` | `mypage/edit.jsp`의 저장 로직 | `MemberDAO.update(dto)` |

관리자 여부는 Firestore의 `admins` 컬렉션 대신 **`MEMBERS.IS_ADMIN` 컬럼**(DB-SCHEMA.md 참고)이라, 로그인할 때 `loginMember`에 이미 실려 있다. 매번 따로 조회할 필요가 없다.

### 9-2. 게시글 + 좋아요 + 스크랩 (PostController) — BlogProject의 `BoardController`에 대응

| 주소 | Command | 지금 하던 일 (JS, `posts-store.js`) | DAO 메서드 (PostDAO) |
|---|---|---|---|
| GET `/post/list` | `PostListCommand` | `getAllPosts()` (+ 검색/필터는 화면에서 하던 걸 SQL `WHERE`로 옮김) | `getList(searchType, keyword, start, end)` |
| GET `/post/view` | `PostViewCommand` | `getPostById(id)` + 조회수는 안 올리던 것 → 이제 올림 | `getBoard(bno)`, `increaseViewCnt(bno)` |
| GET/POST `/post/write` | `PostWriteCommand` | `createPost(data)` (사진 업로드 포함) | `insert(dto)` (사진은 `UploadUtil` 사용) |
| GET/POST `/post/modify` | `PostModifyCommand` | `updatePost(id, data, ...)` | `update(dto)` (where에 `writer_id` 포함) |
| POST `/post/delete` | `PostDeleteCommand` | `deletePost(id, ...)` (댓글/좋아요/사진까지 연쇄 삭제하던 `deletePostChildren`) | `delete(bno, writer)` 하나만 호출하면 끝 — **댓글/좋아요는 `ON DELETE CASCADE`로 Oracle이 자동으로 지워줌** (DB-SCHEMA.md에 이미 반영됨). 사진 파일만 `UploadUtil.delete()`로 따로 지운다. |
| POST `/post/like` (Ajax) | `LikeCommand` | `toggleLike(postId)`, `isLiked(postId)` | `toggleLike(bno, memberId)`, `isLiked(...)`, `getLikeCnt(...)` — BlogProject의 `LikeCommand`를 거의 그대로 가져다 쓸 수 있다 |
| POST `/post/scrap` (Ajax) | `ScrapCommand` | `toggleScrap(postId)`, `isScrapped(postId)` | 좋아요와 같은 모양으로 `PostDAO`에 `toggleScrap`, `isScrapped` 추가 (또는 `ScrapDAO`로 분리) |
| (메인 화면 최신글) | `MainCommand` | `index.jsp`의 국가별 최신 3개 | `getLatest(n)` / 국가별로는 `getLatestByCountry(country, n)` 추가 |
| (인기순 정렬) | `PostListCommand` 안에서 처리 | `posts.jsp`의 좋아요→조회수→최신 비교 | `getPopular(n)` (BlogProject의 `getPopular`와 완전히 같은 정렬 기준: `like_cnt desc, view_cnt desc, bno desc`) |
| (마이페이지 - 내 글) | `MypageCommand` | `mypage.jsp`의 `getAllPosts()` 필터링 | `getListByWriter(writer)` |
| (마이페이지 - 스크랩한 글) | `MypageCommand` | `getMyScrapIds()` + 목록 매칭 | `getLikedList`와 같은 모양의 `getScrappedList(memberId)` (JOIN으로 한 번에 가져옴 — 지금처럼 "사라진 글의 스크랩 정리" 로직 자체가 필요 없어진다. `ON DELETE CASCADE`가 대신 해주기 때문) |

**참고**: 좋아요(`board_like`)는 BlogProject에도 별도 DTO가 없다. 화면에 "좋아요 전체 목록"을 보여줄 일이 없어서, DAO 메서드(`isLiked`, `toggleLike`, `getLikeCnt`)만 있으면 충분하기 때문이다. 스크랩도 같은 이유로 `ScrapDTO` 없이 DAO 메서드만으로 처리할 수 있다 — **테이블이 있다고 꼭 DTO가 있어야 하는 건 아니고, "그 테이블의 행 하나를 화면에 그대로 보여줄 일이 있는가"가 기준이다.**

### 9-3. 댓글 (BoardController에 포함하거나 CommentController로 분리)

| 주소 | Command | 지금 하던 일 (JS) | DAO 메서드 (CommentDAO) |
|---|---|---|---|
| POST `/comment/write` (Ajax) | `CommentWriteCommand` | `addComment(postId, body, nickname)` | `insert(dto)` + `POSTS.comments`는 컬럼으로 안 두고 `SELECT COUNT(*)`로 세므로 DAO에 따로 갱신 코드가 없다 (DB-SCHEMA.md 설계 방침) |
| POST `/comment/delete` (Ajax) | `CommentDeleteCommand` | `deleteComment(postId, commentId)` | `delete(commentId, memberId, isAdmin, ...)` — 댓글 작성자/글 작성자/관리자 세 조건을 SQL `WHERE`나 Command의 `if`로 검사 (BlogProject의 `ReplyDeleteCommand`가 "글 작성자" 조건까지는 안 보여줬을 수 있으니, `PostDAO.getBoard()`로 글 작성자를 먼저 조회해 비교하는 코드를 Command에 추가) |
| (상세화면 댓글 목록) | `PostViewCommand`에 포함 | `getComments(postId)` | `CommentDAO.getList(bno)` |

### 9-4. 관리자 — 게시글 로그 (AdminController)

| 주소 | Command | 지금 하던 일 (JS) | DAO 메서드 (PostLogDAO) |
|---|---|---|---|
| GET `/admin/postLogs` | `PostLogListCommand` | `getAllLogs()` | `getList()` |
| (글쓰기/수정/삭제 시 자동 기록) | 별도 주소 없음 — `PostWriteCommand`/`PostModifyCommand`/`PostDeleteCommand` 끝에서 호출 | `addLog({...})` | `PostLogDAO.insert(dto)` |

회원 관리(`admin/users.jsp`)는 지금 프로젝트에서 이미 목업(alert만)으로 결정했으므로, 전환 때도 화면만 두고 Command는 당장 안 만들어도 된다.

### 9-5. 작업 순서 제안 (수업 진도에 맞춰 단계적으로)

DB-SCHEMA.md 5번의 테이블 순서와 맞춰서, 화면도 같은 순서로 Command를 만들면 중간중간 실행해보며 진행할 수 있다.

1. **회원**: `MemberController` + `JoinCommand`/`LoginCommand`/`LogoutCommand` → 로그인해서 세션에 `loginMember`가 들어가는지부터 확인
2. **게시글 CRUD**: `PostController` + `PostListCommand`/`PostViewCommand`/`PostWriteCommand`/`PostModifyCommand`/`PostDeleteCommand` → 로그인 세션을 이용한 작성자 확인까지 동작 확인
3. **댓글**: `CommentWriteCommand`/`CommentDeleteCommand`
4. **좋아요/스크랩**: `LikeCommand`/`ScrapCommand` (Ajax 응답 포함)
5. **게시글 로그(관리자)**: `PostLogListCommand`

각 단계가 끝날 때마다 `BlogProject`(수업 예제)의 같은 기능(좋아요는 `LikeCommand`, 댓글은 `Reply*Command`)과 나란히 비교해보면 막히는 부분을 빨리 찾을 수 있다.
