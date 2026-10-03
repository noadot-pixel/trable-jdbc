<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>

<%@ page import="model.MemberDTO" %>

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
%>

<!DOCTYPE html>
<html lang="ko">

<head>
    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>관리자 - 여행만들기</title>

    <link rel="stylesheet"
          href="<%= contextPath %>/css/common.css">

    <link rel="stylesheet"
          href="<%= contextPath %>/css/mystyle.css">
</head>

<body>

<% String activeNav = ""; %>
<%@ include file="/common/header.jsp" %>

<main>

    <section class="page-heading">

        <p class="breadcrumb">
            <a href="<%= contextPath %>/main">홈</a>
            〉 관리자
        </p>

        <h1>관리자</h1>

        <p class="page-description">
            게시글 로그를 확인하고, 예시 게시글을 추가할 수 있습니다.
        </p>

    </section>

    <section class="admin-menu-section">

        <a href="<%= contextPath %>/admin/postLogs"
           class="admin-menu-card">

            <h2>게시글 로그</h2>

            <p>
                게시글 작성·수정·삭제 이력을 조회합니다.
            </p>

        </a>

    </section>

    <!-- =========================
         예시 게시글 시드
         posts 테이블이 비어 있을 때만 9개의 예시 게시글을 채워 넣는다.
    ========================== -->

    <section class="admin-seed-section">

        <p class="admin-seed-description">
            게시판이 비어 있을 때 예시 게시글 9개를 한 번에 채워 넣습니다.
            이미 게시글이 있으면 아무 것도 바뀌지 않습니다.
        </p>

        <button type="button"
                id="seedButton"
                class="auth-submit admin-seed-button">
            예시 게시글 시드 추가
        </button>

    </section>

</main>

<%@ include file="/common/footer.jsp" %>

<script>
    document.getElementById("seedButton")
        .addEventListener("click", function () {

            fetch("<%= contextPath %>/admin/seed", { method: "POST" })
                .then(function (res) { return res.json(); })
                .then(function (data) {

                    if (data.added === 0) {
                        alert("이미 게시글이 있어서 시드를 추가하지 않았습니다.");
                    } else {
                        alert(data.added + "개의 예시 게시글을 추가했습니다.");
                    }

                });

        });
</script>

</body>
</html>
