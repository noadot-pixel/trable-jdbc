<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>

<%@ page import="java.util.List" %>
<%@ page import="model.PostDTO" %>
<%@ page import="util.EscapeUtil" %>

<%
    String contextPath = request.getContextPath();

    //이 페이지는 반드시 MainCommand(/main)를 거쳐서 와야 한다. 직접 열면 데이터가 없으므로 돌려보낸다.
    List<PostDTO> koreaPosts = (List<PostDTO>) request.getAttribute("koreaPosts");

    if (koreaPosts == null) {
        response.sendRedirect(contextPath + "/main");
        return;
    }

    List<PostDTO> japanPosts = (List<PostDTO>) request.getAttribute("japanPosts");
    List<PostDTO> worldPosts = (List<PostDTO>) request.getAttribute("worldPosts");

    //common/post-card.jspf가 쓰는 변수들. 메인 화면은 수정/삭제 버튼도, "내 글" 배지도 없다.
    boolean showMyBadge = false;
    String actionsHtml = null;
%>

<!DOCTYPE html>
<html lang="ko">

<head>
    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>여행만들기</title>

    <link rel="stylesheet"
          href="<%= contextPath %>/css/common.css">

    <link rel="stylesheet"
          href="<%= contextPath %>/css/mystyle.css">
</head>

<body>

<!-- =========================
     상단 헤더
========================= -->

<% String activeNav = "home"; %>
<%@ include file="/common/header.jsp" %>

<main>

    <!-- =========================
         메인 이미지
    ========================== -->

    <section class="main-visual">

        <div class="main-overlay"></div>

        <div class="main-content">

            <p class="main-label">
                KOREA · JAPAN · WORLD
            </p>

            <h1>
                여행을 만들면
                <span>새로운 이야기가 시작된다</span>
            </h1>

            <p class="main-description">
                한국과 일본, 우리들의 특별한 여행을<br>
                함께 만들어가는 여행 커뮤니티입니다.
            </p>

            <form class="search-form"
                  action="<%= contextPath %>/post/list"
                  method="get">

                <label for="searchKeyword"
                       class="blind">
                    여행지 검색
                </label>

                <span class="search-icon">
                    ⌕
                </span>

                <input type="text"
                       id="searchKeyword"
                       name="keyword"
                       placeholder="어디로 떠나고 싶으신가요?">

                <button type="submit">
                    검색
                </button>

            </form>

            <div class="recommend-keywords">

                <button type="button" data-keyword="서울">#서울</button>
                <button type="button" data-keyword="제주도">#제주도</button>
                <button type="button" data-keyword="도쿄">#도쿄</button>
                <button type="button" data-keyword="오사카">#오사카</button>
                <button type="button" data-keyword="후쿠오카">#후쿠오카</button>
                <button type="button" data-keyword="바르셀로나">#바르셀로나</button>

            </div>

        </div>

    </section>

    <!-- =========================
         여행 카테고리
    ========================== -->

    <section class="category-section"
             id="travelCategory">

        <div class="section-heading">

            <h2>여행 카테고리</h2>

            <a href="<%= contextPath %>/post/list">
                전체보기 〉
            </a>

        </div>

        <div class="category-list">

            <!-- 한국 여행지 -->
            <a href="<%= contextPath %>/post/list?country=KR"
               class="category-card">

                <img src="<%= contextPath %>/images/korea.jpg"
                     alt="한국 여행지">

                <div class="category-content">

                    <h3>한국 여행지</h3>

                    <p>
                        국내의 아름다운 여행지를 만나보세요.
                    </p>

                </div>

            </a>

            <!-- 일본 여행지 -->
            <a href="<%= contextPath %>/post/list?country=JP"
               class="category-card">

                <img src="<%= contextPath %>/images/japan.jpg"
                     alt="일본 여행지">

                <div class="category-content">

                    <h3>일본 여행지</h3>

                    <p>
                        가깝고도 새로운 일본을 여행해 보세요.
                    </p>

                </div>

            </a>

            <!-- 세계 여행지 -->
            <a href="<%= contextPath %>/post/list?country=ETC"
               class="category-card">

                <img src="<%= contextPath %>/images/world.jpg"
                     alt="세계 여행지">

                <div class="category-content">

                    <h3>세계 여행지</h3>

                    <p>
                        더 멀리, 더 새로운 세계로 떠나보세요.
                    </p>

                </div>

            </a>

            <!-- 여행지 맛집 -->
            <a href="<%= contextPath %>/post/list?style=food"
               class="category-card">

                <img src="<%= contextPath %>/images/travel_food.jpg"
                     alt="여행지 맛집">

                <div class="category-content">

                    <h3>여행지 맛집</h3>

                    <p>
                        여행에서 놓칠 수 없는 맛집을 소개합니다.
                    </p>

                </div>

            </a>

        </div>

    </section>

    <!-- =========================
         국가별 최신 게시물
    ========================== -->

    <section class="story-section"
             id="travelStory">

        <div class="section-heading">

            <h2>📝 최신 여행 이야기</h2>

            <a href="<%= contextPath %>/post/list">
                전체보기 〉
            </a>

        </div>

        <!-- =====================
             한국 여행지
        ====================== -->

        <section class="country-board">

            <div class="country-heading">

                <h3>🌺 한국 여행지</h3>

                <div class="country-heading-right">

                    <span>최신순</span>

                    <a href="<%= contextPath %>/post/list?country=KR">
                        더보기 〉
                    </a>

                </div>

            </div>

            <div class="country-card-list">

                <% for (PostDTO post : koreaPosts) { %>
                    <%@ include file="/common/post-card.jspf" %>
                <% }
                   for (int i = koreaPosts.size(); i < 3; i++) { %>
                    <article class="empty-story-card" data-empty="true">
                        <div class="empty-folder">📁</div>
                        <p>새로운 한국 여행 이야기가<br>등록될 공간입니다.</p>
                    </article>
                <% } %>

            </div>

        </section>

        <!-- =====================
             일본 여행지
        ====================== -->

        <section class="country-board">

            <div class="country-heading">

                <h3>🍣 일본 여행지</h3>

                <div class="country-heading-right">

                    <span>최신순</span>

                    <a href="<%= contextPath %>/post/list?country=JP">
                        더보기 〉
                    </a>

                </div>

            </div>

            <div class="country-card-list">

                <% for (PostDTO post : japanPosts) { %>
                    <%@ include file="/common/post-card.jspf" %>
                <% }
                   for (int i = japanPosts.size(); i < 3; i++) { %>
                    <article class="empty-story-card" data-empty="true">
                        <div class="empty-folder">📁</div>
                        <p>새로운 일본 여행 이야기가<br>등록될 공간입니다.</p>
                    </article>
                <% } %>

            </div>

        </section>

        <!-- =====================
             세계 여행지
        ====================== -->

        <section class="country-board">

            <div class="country-heading">

                <h3>🌍 세계 여행지</h3>

                <div class="country-heading-right">

                    <span>최신순</span>

                    <a href="<%= contextPath %>/post/list?country=ETC">
                        더보기 〉
                    </a>

                </div>

            </div>

            <div class="country-card-list">

                <% for (PostDTO post : worldPosts) { %>
                    <%@ include file="/common/post-card.jspf" %>
                <% }
                   for (int i = worldPosts.size(); i < 3; i++) { %>
                    <article class="empty-story-card" data-empty="true">
                        <div class="empty-folder">📁</div>
                        <p>새로운 세계 여행 이야기가<br>등록될 공간입니다.</p>
                    </article>
                <% } %>

            </div>

        </section>

    </section>

    <!-- =========================
         하단 전체 사진
    ========================== -->

    <section class="footer-visual">

        <div class="footer-visual-overlay"></div>

        <div class="footer-visual-content">

            <h2>
                좋은 사람들과 함께 만드는 여행
            </h2>

            <p>
                새로운 여행 이야기를 여행만들기에서
                함께 공유해 보세요.
            </p>

            <a href="<%= contextPath %>/post/write">
                여행 이야기 작성하기
            </a>

        </div>

    </section>

</main>

<!-- =========================
     하단 푸터
========================= -->

<%@ include file="/common/footer.jsp" %>

<script>
    /*
     * 추천 검색어를 클릭하면 검색창에 입력
     */

    const searchInput =
        document.getElementById("searchKeyword");

    document.querySelectorAll(".recommend-keywords button").forEach(function (button) {

        button.addEventListener("click", function () {

            searchInput.value = button.dataset.keyword;

            searchInput.focus();
        });

    });
</script>

</body>
</html>
