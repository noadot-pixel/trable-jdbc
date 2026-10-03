<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>

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

    //PostModifyFormCommand(GET) 또는 실패 후 재진입한 PostModifyCommand(POST)가 넣어준 글.
    PostDTO post = (PostDTO) request.getAttribute("post");

    if (post == null) {
        response.sendRedirect(contextPath + "/post/list");
        return;
    }

    String msg = (String) request.getAttribute("msg");

    //실패해서 다시 돌아온 경우에는 방금 입력했던 값을 유지하고, 처음 들어온 경우에는 기존 글 값을 보여준다.
    String titleValue = request.getParameter("writeTitle") != null ? request.getParameter("writeTitle") : post.getTitle();
    String bodyValue = request.getParameter("writeBody") != null ? request.getParameter("writeBody") : post.getContent();
    String countryValue = request.getParameter("country") != null ? request.getParameter("country") : post.getCountry();
    String styleValue = request.getParameter("style") != null ? request.getParameter("style") : post.getStyle();
%>

<!DOCTYPE html>
<html lang="ko">

<head>
    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>게시글 수정 - 여행만들기</title>

    <link rel="stylesheet"
          href="<%= contextPath %>/css/common.css">

    <link rel="stylesheet"
          href="<%= contextPath %>/css/mystyle.css">
</head>

<body>

<% String activeNav = "posts"; %>
<%@ include file="/common/header.jsp" %>

<main>

    <section class="page-heading">

        <p class="breadcrumb">
            <a href="<%= contextPath %>/main">홈</a>
            〉 <a href="<%= contextPath %>/post/list">게시글 목록</a>
            〉 게시글 수정
        </p>

        <h1>게시글 수정</h1>

        <p class="page-description">
            여행지 구분과 여행 성향을 각각 하나씩 선택하고 내용을 수정하세요.
        </p>

    </section>

    <% if (msg != null) { %>
    <p class="field-error"><%= EscapeUtil.html(msg) %></p>
    <% } %>

    <section class="write-section">

        <form id="editForm"
              method="post"
              enctype="multipart/form-data"
              action="<%= contextPath %>/post/modify">

            <input type="hidden" name="bno" value="<%= post.getBno() %>">

            <div class="write-field">

                <label for="writeTitle">
                    제목
                </label>

                <input type="text"
                       id="writeTitle"
                       name="writeTitle"
                       value="<%= EscapeUtil.html(titleValue) %>"
                       placeholder="제목을 입력해 주세요">

            </div>

            <input type="hidden" id="countryInput" name="country" value="<%= countryValue %>">
            <input type="hidden" id="styleInput" name="style" value="<%= styleValue %>">

            <div class="filter-group" id="writeCountryGroup">

                <p class="filter-label">여행지 구분</p>

                <button type="button" class="filter-button <%= "KR".equals(countryValue) ? "active" : "" %>" data-country="KR">국내여행</button>
                <button type="button" class="filter-button <%= "JP".equals(countryValue) ? "active" : "" %>" data-country="JP">일본여행</button>
                <button type="button" class="filter-button <%= "ETC".equals(countryValue) ? "active" : "" %>" data-country="ETC">해외여행</button>

            </div>

            <div class="filter-group" id="writeStyleGroup">

                <p class="filter-label">여행 성향</p>

                <button type="button" class="filter-button <%= "healing".equals(styleValue) ? "active" : "" %>" data-style="healing">힐링</button>
                <button type="button" class="filter-button <%= "food".equals(styleValue) ? "active" : "" %>" data-style="food">맛집</button>
                <button type="button" class="filter-button <%= "activity".equals(styleValue) ? "active" : "" %>" data-style="activity">액티비티</button>
                <button type="button" class="filter-button <%= "shopping".equals(styleValue) ? "active" : "" %>" data-style="shopping">쇼핑</button>

            </div>

            <div class="write-field">

                <label for="writeBody">
                    본문
                </label>

                <textarea id="writeBody"
                          name="writeBody"
                          rows="10"
                          placeholder="여행 이야기를 자유롭게 남겨주세요"><%= EscapeUtil.html(bodyValue) %></textarea>

            </div>

            <div class="write-field">

                <label for="writePhoto">
                    사진 첨부
                </label>

                <input type="file"
                       id="writePhoto"
                       name="writePhoto"
                       accept="image/*">

                <p class="write-field-note">
                    5MB 미만의 이미지 파일만 올릴 수 있습니다.
                    새 사진을 고르지 않으면 지금 사진이 그대로 유지됩니다.
                </p>

                <img id="writePhotoPreview"
                     class="post-image-preview"
                     src="<%= contextPath %>/<%= post.getImgPath() %>"
                     alt="게시글 사진 미리보기">

            </div>

            <button type="submit" class="auth-submit">
                수정하기
            </button>

        </form>

    </section>

</main>

<%@ include file="/common/footer.jsp" %>

<script>
    function bindFilterGroup(groupId, hiddenInputId, datasetKey) {

        const group = document.getElementById(groupId);
        const hiddenInput = document.getElementById(hiddenInputId);
        const buttons = group.querySelectorAll(".filter-button");

        buttons.forEach(function (button) {

            button.addEventListener("click", function () {

                buttons.forEach(function (other) {
                    other.classList.remove("active");
                });

                button.classList.add("active");

                hiddenInput.value = button.dataset[datasetKey];

            });

        });

    }

    bindFilterGroup("writeCountryGroup", "countryInput", "country");
    bindFilterGroup("writeStyleGroup", "styleInput", "style");

    const writePhotoInput = document.getElementById("writePhoto");
    const writePhotoPreview = document.getElementById("writePhotoPreview");
    const originalPhotoSrc = writePhotoPreview.src;

    writePhotoInput.addEventListener("change", function () {

        const file = writePhotoInput.files[0];

        if (!file) {
            writePhotoPreview.src = originalPhotoSrc;
            return;
        }

        if (!file.type.startsWith("image/")) {
            alert("이미지 파일만 선택할 수 있습니다.");
            writePhotoInput.value = "";
            writePhotoPreview.src = originalPhotoSrc;
            return;
        }

        if (file.size >= 5 * 1024 * 1024) {
            alert("5MB 미만의 이미지만 첨부할 수 있습니다.");
            writePhotoInput.value = "";
            writePhotoPreview.src = originalPhotoSrc;
            return;
        }

        writePhotoPreview.src = URL.createObjectURL(file);

    });

    document.getElementById("editForm").addEventListener("submit", function (event) {

        if (document.getElementById("writeTitle").value.trim() === ""
            || document.getElementById("writeBody").value.trim() === ""
            || document.getElementById("countryInput").value === ""
            || document.getElementById("styleInput").value === "") {

            event.preventDefault();

            alert("제목, 본문, 여행지 구분, 여행 성향을 모두 입력/선택해 주세요.");
        }

    });
</script>

</body>
</html>
