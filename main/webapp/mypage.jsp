<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>

<%@ page import="java.util.List" %>
<%@ page import="model.MemberDTO" %>
<%@ page import="model.PostDTO" %>
<%@ page import="util.EscapeUtil" %>

<%
    String contextPath = request.getContextPath();

    MemberDTO loginMember = (MemberDTO) session.getAttribute("loginMember");

    if (loginMember == null) {
        response.sendRedirect(contextPath + "/member/login");
        return;
    }

    //이 페이지는 반드시 MypageCommand(/member/mypage)를 거쳐서 와야 한다.
    List<PostDTO> myPosts = (List<PostDTO>) request.getAttribute("myPosts");

    if (myPosts == null) {
        response.sendRedirect(contextPath + "/member/mypage");
        return;
    }

    List<PostDTO> scrapPosts = (List<PostDTO>) request.getAttribute("scrapPosts");

    String countryLabel;
    if ("KR".equals(loginMember.getCountry())) countryLabel = "한국";
    else if ("JP".equals(loginMember.getCountry())) countryLabel = "일본";
    else if ("ETC".equals(loginMember.getCountry())) countryLabel = (loginMember.getCountryOther() != null ? loginMember.getCountryOther() : "기타");
    else countryLabel = "-";

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

    <title>마이페이지 - 여행만들기</title>

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
         마이페이지 (보기 전용)
         수정은 /member/mypageEdit에서 한다.
    ========================== -->

    <section class="page-heading">

        <p class="breadcrumb">
            <a href="<%= contextPath %>/main">홈</a>
            〉 마이페이지
        </p>

        <h1>마이페이지</h1>

        <p class="page-description">
            내 프로필과 내가 작성한 글을 확인하세요.
        </p>

    </section>

    <section class="profile-section">

        <div class="profile-card">

            <img class="profile-photo-large"
                 src="<%= loginMember.getPhotoPath() != null ? (contextPath + "/" + loginMember.getPhotoPath()) : (contextPath + "/images/logo.png") %>"
                 alt="프로필 이미지">

            <div class="profile-card-info">

                <h2><%= EscapeUtil.html(loginMember.getNickname()) %></h2>

                <p class="profile-bio"><%= loginMember.getBio() != null && !loginMember.getBio().isEmpty() ? EscapeUtil.html(loginMember.getBio()) : "아직 소개가 없습니다." %></p>

                <div class="profile-meta">
                    <span>국가 <%= EscapeUtil.html(countryLabel) %></span>
                    <span>이메일 <%= EscapeUtil.html(loginMember.getId()) %></span>
                </div>

                <div class="profile-personal">
                    <%
                        java.util.List<String> personalParts = new java.util.ArrayList<String>();

                        if (loginMember.getBirthday() != null && !loginMember.getBirthday().isEmpty()) {
                            personalParts.add("생일 " + loginMember.getBirthday()
                                + (loginMember.isBirthdayPublic() ? " (공개)" : " (비공개)"));
                        }

                        if (loginMember.getRealName() != null && !loginMember.getRealName().isEmpty()) {
                            personalParts.add("이름 " + EscapeUtil.html(loginMember.getRealName())
                                + (loginMember.isNamePublic() ? " (공개)" : " (비공개)"));
                        }
                    %>
                    <%= String.join(" · ", personalParts) %>
                </div>

                <a href="<%= contextPath %>/member/mypageEdit"
                   class="auth-submit profile-edit-button">
                    정보 수정
                </a>

            </div>

        </div>

    </section>

    <section class="page-heading">
        <h2>내가 작성한 글</h2>
    </section>

    <section class="post-list">

        <% for (PostDTO post : myPosts) {

            actionsHtml = "<div class=\"detail-actions local-post-actions\">"
                + "<a href=\"" + contextPath + "/post/modify?bno=" + post.getBno() + "\" class=\"detail-edit-button\">수정</a>"
                + "<form method=\"post\" action=\"" + contextPath + "/post/delete\" onsubmit=\"return confirm('이 게시글을 삭제하시겠습니까?');\">"
                + "<input type=\"hidden\" name=\"bno\" value=\"" + post.getBno() + "\">"
                + "<button type=\"submit\" class=\"detail-delete-button\">삭제</button>"
                + "</form></div>";
        %>
            <%@ include file="/common/post-card.jspf" %>
        <% } %>

    </section>

    <% if (myPosts.isEmpty()) { %>
    <p class="empty-result">
        아직 작성한 게시글이 없습니다.
    </p>
    <% } %>

    <section class="page-heading">
        <h2>스크랩한 글</h2>
    </section>

    <section class="post-list">

        <% for (PostDTO post : scrapPosts) {

            actionsHtml = "<div class=\"detail-actions local-post-actions\">"
                + "<form method=\"post\" action=\"" + contextPath + "/post/scrap\" class=\"scrap-remove-form\">"
                + "<input type=\"hidden\" name=\"bno\" value=\"" + post.getBno() + "\">"
                + "<button type=\"submit\" class=\"detail-delete-button\">스크랩 해제</button>"
                + "</form></div>";
        %>
            <%@ include file="/common/post-card.jspf" %>
        <% } %>

    </section>

    <% if (scrapPosts.isEmpty()) { %>
    <p class="empty-result">
        아직 스크랩한 게시글이 없습니다.
    </p>
    <% } %>

</main>

<%@ include file="/common/footer.jsp" %>

<script>
    /*
     * 스크랩 해제 버튼은 /post/scrap(Ajax 전용 JSON 엔드포인트)으로 그냥 폼 전송하면
     * 화면에 JSON이 그대로 떠버리므로, 폼 제출 대신 fetch로 보내고 성공하면 새로고침한다.
     */

    document.querySelectorAll(".scrap-remove-form").forEach(function (form) {

        form.addEventListener("submit", function (event) {

            event.preventDefault();

            const bno = form.querySelector('input[name="bno"]').value;

            fetch(form.action, {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: "bno=" + encodeURIComponent(bno)
            })
            .then(function () {
                location.reload();
            });

        });

    });
</script>

</body>
</html>
