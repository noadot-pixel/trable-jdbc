<%@ page pageEncoding="UTF-8" %>
<%@ page import="model.MemberDTO" %>
<%@ page import="util.EscapeUtil" %>
<%--
    공통 헤더. 사용하는 페이지는 include 전에 아래 변수를 선언해야 한다.

        String contextPath = request.getContextPath();
        String activeNav = "home"; // "home" | "posts" | "" (해당 없음)

    이 파일은 include 지시어로 각 페이지에 "코드째로 복사"되어 들어간다
    → 여기서 만든 변수 이름이 페이지의 변수 이름과 겹치면 중복 선언 에러가 나므로
      header 전용 이름(headerMember)을 쓴다.
--%>
<%
    MemberDTO headerMember = (MemberDTO) session.getAttribute("loginMember");
%>

<header class="header">

    <div class="header-inner">

        <a href="<%= contextPath %>/main"
           class="top-logo">

            <img src="<%= contextPath %>/images/logo.png"
                 alt="여행만들기 로고">
        </a>

        <nav class="navigation">

            <a href="<%= contextPath %>/main"
               class="<%= "home".equals(activeNav) ? "active" : "" %>">
                홈
            </a>

             <a href="<%= contextPath %>/post/list">
                커뮤니티
            </a>

        </nav>

        <div class="member-menu">

            <a href="<%= contextPath %>/post/write"
               class="write-link">
                글쓰기
            </a>

            <span id="memberMenu">

            <% if (headerMember == null) { %>

                <a href="<%= contextPath %>/member/login">
                    로그인
                </a>

                <a href="<%= contextPath %>/member/join"
                   class="join-button">
                    회원가입
                </a>

            <% } else { %>

                <% if (headerMember.isAdmin()) { %>
                <a href="<%= contextPath %>/admin/index.jsp">
                    관리자
                </a>
                <% } %>

                <a href="<%= contextPath %>/member/mypage">
                    마이페이지
                </a>

                <span class="member-nickname"><%= EscapeUtil.html(headerMember.getNickname()) %>님</span>

                <a href="<%= contextPath %>/member/logout">
                    로그아웃
                </a>

            <% } %>

            </span>

        </div>

    </div>

</header>
