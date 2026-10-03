<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>

<%@ page import="java.util.List" %>
<%@ page import="model.MemberDTO" %>
<%@ page import="model.PostLogDTO" %>
<%@ page import="util.EscapeUtil" %>

<%
    String contextPath = request.getContextPath();

    MemberDTO loginMember = (MemberDTO) session.getAttribute("loginMember");

    if (loginMember == null) {
        response.sendRedirect(contextPath + "/member/login");
        return;
    }

    if (!loginMember.isAdmin()) {
        response.sendRedirect(contextPath + "/main");
        return;
    }

    //이 페이지는 반드시 PostLogListCommand(/admin/postLogs)를 거쳐서 와야 한다.
    List<PostLogDTO> logList = (List<PostLogDTO>) request.getAttribute("logList");

    if (logList == null) {
        response.sendRedirect(contextPath + "/admin/postLogs");
        return;
    }
%>

<!DOCTYPE html>
<html lang="ko">

<head>
    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>게시글 로그 - 여행만들기</title>

    <link rel="stylesheet"
          href="<%= contextPath %>/css/common.css">

    <link rel="stylesheet"
          href="<%= contextPath %>/css/mystyle.css">
</head>

<body>

<% String activeNav = ""; %>
<%@ include file="/common/header.jsp" %>

<main>

    <!-- =========================
         게시글 로그
         POST_LOGS 테이블(작성/수정/삭제 시 자동 기록됨)을 조회한다.
    ========================== -->

    <section class="page-heading">

        <p class="breadcrumb">
            <a href="<%= contextPath %>/main">홈</a>
            〉 <a href="<%= contextPath %>/admin/index.jsp">관리자</a>
            〉 게시글 로그
        </p>

        <h1>게시글 로그</h1>

        <p class="page-description">
            게시글 작성·수정·삭제 이력을 조회합니다.
        </p>

    </section>

    <section class="admin-table-section">

        <p>
            전체 로그 <strong><%= logList.size() %></strong>건
        </p>

        <table class="admin-table">

            <thead>
                <tr>
                    <th>작업 종류</th>
                    <th>작업 시각</th>
                    <th>대상 게시글</th>
                    <th>작업자</th>
                </tr>
            </thead>

            <tbody>

                <% for (PostLogDTO log : logList) { %>
                <tr>
                    <td><span class="admin-log-type admin-log-<%= log.getLogType().toLowerCase() %>"><%= log.getLogTypeLabel() %></span></td>
                    <td><%= log.getLogDate() %></td>
                    <td><%= EscapeUtil.html(log.getPostTitle()) %></td>
                    <td><%= EscapeUtil.html(log.getActorNick()) %></td>
                </tr>
                <% } %>

            </tbody>

        </table>

        <% if (logList.isEmpty()) { %>
        <p class="empty-result">
            아직 기록된 로그가 없습니다.
        </p>
        <% } %>

    </section>

</main>

<%@ include file="/common/footer.jsp" %>

</body>
</html>
