<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>

<%@ page import="java.util.List" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="model.MemberDTO" %>
<%@ page import="model.PostDTO" %>
<%@ page import="util.EscapeUtil" %>

<%
    String contextPath = request.getContextPath();

    //이 페이지는 반드시 PostListCommand(/post/list)를 거쳐서 와야 한다.
    List<PostDTO> list = (List<PostDTO>) request.getAttribute("list");

    if (list == null) {
        response.sendRedirect(contextPath + "/post/list");
        return;
    }

    String country = (String) request.getAttribute("country");
    String style = (String) request.getAttribute("style");
    String keyword = (String) request.getAttribute("keyword");
    String sort = (String) request.getAttribute("sort");

    //필터/정렬 링크에 지금 조건을 계속 붙여줘야 한다 (country만 바꿔도 검색어는 유지되도록)
    String encodedKeyword = URLEncoder.encode(keyword, "UTF-8");
    String baseQuery = "keyword=" + encodedKeyword;

    MemberDTO loginMember = (MemberDTO) session.getAttribute("loginMember");

    //common/post-card.jspf가 쓰는 변수들
    boolean showMyBadge = false;
    String actionsHtml = null;
%>

<!DOCTYPE html>
<html lang="ko">

<head>
    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>게시글 목록 - 여행만들기</title>

    <link rel="stylesheet"
          href="<%= contextPath %>/css/common.css">

    <link rel="stylesheet"
          href="<%= contextPath %>/css/mystyle.css">
</head>

<body>

<!-- =========================
     상단 헤더
========================= -->

<% String activeNav = "posts"; %>
<%@ include file="/common/header.jsp" %>

<main>

    <!-- =========================
         페이지 타이틀
    ========================== -->

    <section class="page-heading">

        <p class="breadcrumb">
            <a href="<%= contextPath %>/main">홈</a>
            〉 게시글 목록
        </p>

        <h1>게시글 목록</h1>

        <p class="page-description">
            국가와 여행 성향으로 원하는 여행 이야기를 찾아보세요.
        </p>

    </section>

    <!-- =========================
         검색 및 필터
    ========================== -->

    <section class="filter-section">

        <form class="search-form"
              action="<%= contextPath %>/post/list"
              method="get">

            <input type="hidden" name="country" value="<%= country %>">
            <input type="hidden" name="style" value="<%= style %>">
            <input type="hidden" name="sort" value="<%= sort %>">

            <label for="searchKeyword"
                   class="blind">
                게시글 검색
            </label>

            <span class="search-icon">
                ⌕
            </span>

            <input type="text"
                   id="searchKeyword"
                   name="keyword"
                   value="<%= EscapeUtil.html(keyword) %>"
                   placeholder="제목으로 검색해 보세요">

            <button type="submit">
                검색
            </button>

        </form>

        <div class="filter-group">

            <p class="filter-label">국가</p>

            <a class="filter-button <%= "ALL".equals(country) ? "active" : "" %>"
               href="<%= contextPath %>/post/list?country=ALL&style=<%= style %>&sort=<%= sort %>&<%= baseQuery %>">전체</a>
            <a class="filter-button <%= "KR".equals(country) ? "active" : "" %>"
               href="<%= contextPath %>/post/list?country=KR&style=<%= style %>&sort=<%= sort %>&<%= baseQuery %>">한국</a>
            <a class="filter-button <%= "JP".equals(country) ? "active" : "" %>"
               href="<%= contextPath %>/post/list?country=JP&style=<%= style %>&sort=<%= sort %>&<%= baseQuery %>">일본</a>
            <a class="filter-button <%= "ETC".equals(country) ? "active" : "" %>"
               href="<%= contextPath %>/post/list?country=ETC&style=<%= style %>&sort=<%= sort %>&<%= baseQuery %>">세계</a>

        </div>

        <div class="filter-group">

            <p class="filter-label">여행 성향</p>

            <a class="filter-button <%= "ALL".equals(style) ? "active" : "" %>"
               href="<%= contextPath %>/post/list?style=ALL&country=<%= country %>&sort=<%= sort %>&<%= baseQuery %>">전체</a>
            <a class="filter-button <%= "healing".equals(style) ? "active" : "" %>"
               href="<%= contextPath %>/post/list?style=healing&country=<%= country %>&sort=<%= sort %>&<%= baseQuery %>">힐링</a>
            <a class="filter-button <%= "food".equals(style) ? "active" : "" %>"
               href="<%= contextPath %>/post/list?style=food&country=<%= country %>&sort=<%= sort %>&<%= baseQuery %>">맛집</a>
            <a class="filter-button <%= "activity".equals(style) ? "active" : "" %>"
               href="<%= contextPath %>/post/list?style=activity&country=<%= country %>&sort=<%= sort %>&<%= baseQuery %>">액티비티</a>
            <a class="filter-button <%= "shopping".equals(style) ? "active" : "" %>"
               href="<%= contextPath %>/post/list?style=shopping&country=<%= country %>&sort=<%= sort %>&<%= baseQuery %>">쇼핑</a>

        </div>

    </section>

    <!-- =========================
         목록 헤더 (결과 수 / 정렬)
    ========================== -->

    <section class="list-heading">

        <p>
            전체 게시글 <strong><%= list.size() %></strong>개
        </p>

        <div class="sort-group">

            <a class="sort-button <%= "popular".equals(sort) ? "" : "active" %>"
               href="<%= contextPath %>/post/list?sort=latest&country=<%= country %>&style=<%= style %>&<%= baseQuery %>">최신순</a>
            <a class="sort-button <%= "popular".equals(sort) ? "active" : "" %>"
               href="<%= contextPath %>/post/list?sort=popular&country=<%= country %>&style=<%= style %>&<%= baseQuery %>">인기순</a>

        </div>

    </section>

    <!-- =========================
         게시글 목록
    ========================== -->

    <section class="post-list">

        <% for (PostDTO post : list) {

            boolean isMine = loginMember != null && loginMember.getId().equals(post.getWriter());
            boolean canManage = isMine || (loginMember != null && loginMember.isAdmin());

            showMyBadge = isMine;

            if (canManage) {
                actionsHtml = "<div class=\"detail-actions local-post-actions\">"
                    + "<a href=\"" + contextPath + "/post/modify?bno=" + post.getBno() + "\" class=\"detail-edit-button\">수정</a>"
                    + "<form method=\"post\" action=\"" + contextPath + "/post/delete\" onsubmit=\"return confirm('이 게시글을 삭제하시겠습니까?');\">"
                    + "<input type=\"hidden\" name=\"bno\" value=\"" + post.getBno() + "\">"
                    + "<button type=\"submit\" class=\"detail-delete-button\">삭제</button>"
                    + "</form></div>";
            } else {
                actionsHtml = null;
            }
        %>
            <%@ include file="/common/post-card.jspf" %>
        <% } %>

    </section>

    <% if (list.isEmpty()) { %>
    <p class="empty-result">
        조건에 맞는 게시글이 없습니다.
    </p>
    <% } %>

</main>

<!-- =========================
     하단 푸터
========================= -->

<%@ include file="/common/footer.jsp" %>

</body>
</html>
