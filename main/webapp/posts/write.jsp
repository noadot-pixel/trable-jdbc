<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>

<%@ page import="model.MemberDTO" %>
<%@ page import="util.EscapeUtil" %>

<%
    String contextPath = request.getContextPath();

    //컨트롤러(PostController)가 이미 로그인을 확인하지만, 이 화면으로 직접 들어오는
    //경우까지 막도록 한 번 더 확인한다 (BlogProject의 write.jsp도 같은 방식).
    MemberDTO loginMember = (MemberDTO) session.getAttribute("loginMember");

    if (loginMember == null) {
        response.sendRedirect(contextPath + "/member/login");
        return;
    }

    String msg = (String) request.getAttribute("msg");   // 등록 실패 시 PostWriteCommand가 넣어준 메시지
%>

<!DOCTYPE html>
<html lang="ko">

<head>
    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>여행 이야기 작성 - 여행만들기</title>

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
            〉 글쓰기
        </p>

        <h1>여행 이야기 작성</h1>

        <p class="page-description">
            여행지 구분과 여행 성향을 각각 하나씩 선택하고 이야기를 남겨보세요.
        </p>

    </section>

    <% if (msg != null) { %>
    <p class="field-error"><%= EscapeUtil.html(msg) %></p>
    <% } %>

    <section class="write-section">

        <!-- 파일을 보내려면 enctype="multipart/form-data" 필수 (없으면 파일 이름만 가고 내용은 안 간다) -->
        <form id="writeForm"
              method="post"
              enctype="multipart/form-data"
              action="<%= contextPath %>/post/write">

            <div class="write-field">

                <label for="writeTitle">
                    제목
                </label>

                <input type="text"
                       id="writeTitle"
                       name="writeTitle"
                       value="<%= EscapeUtil.html(request.getParameter("writeTitle")) %>"
                       placeholder="제목을 입력해 주세요">

            </div>

            <!-- 선택한 값은 JS가 이 hidden input에 채워서 서버로 보낸다 -->
            <input type="hidden" id="countryInput" name="country" value="">
            <input type="hidden" id="styleInput" name="style" value="">

            <div class="filter-group" id="writeCountryGroup">

                <p class="filter-label">여행지 구분</p>

                <button type="button" class="filter-button" data-country="KR">국내여행</button>
                <button type="button" class="filter-button" data-country="JP">일본여행</button>
                <button type="button" class="filter-button" data-country="ETC">해외여행</button>

            </div>

            <div class="filter-group" id="writeStyleGroup">

                <p class="filter-label">여행 성향</p>

                <button type="button" class="filter-button" data-style="healing">힐링</button>
                <button type="button" class="filter-button" data-style="food">맛집</button>
                <button type="button" class="filter-button" data-style="activity">액티비티</button>
                <button type="button" class="filter-button" data-style="shopping">쇼핑</button>

            </div>

            <div class="write-field">

                <label for="writeBody">
                    본문
                </label>

                <textarea id="writeBody"
                          name="writeBody"
                          rows="10"
                          placeholder="여행 이야기를 자유롭게 남겨주세요"><%= EscapeUtil.html(request.getParameter("writeBody")) %></textarea>

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
                    사진을 올리지 않으면 여행지 구분에 맞는 기본 이미지가 사용됩니다.
                </p>

                <img id="writePhotoPreview"
                     class="post-image-preview"
                     alt="첨부할 사진 미리보기"
                     hidden>

            </div>

            <button type="submit" class="auth-submit">
                등록하기
            </button>

        </form>

    </section>

</main>

<%@ include file="/common/footer.jsp" %>

<script>
    /*
     * 여행지 구분 / 여행 성향은 각 그룹에서 하나만 선택되도록 처리하고,
     * 고른 값을 hidden input에 채워서 폼 제출 때 서버로 보낸다.
     */

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


    /*
     * 첨부할 사진을 고르면 바로 미리보기를 보여준다. 실제 업로드는 등록할 때 한 번만 일어난다.
     */

    const writePhotoInput = document.getElementById("writePhoto");
    const writePhotoPreview = document.getElementById("writePhotoPreview");

    writePhotoInput.addEventListener("change", function () {

        const file = writePhotoInput.files[0];

        if (!file) {
            writePhotoPreview.hidden = true;
            return;
        }

        if (!file.type.startsWith("image/")) {
            alert("이미지 파일만 선택할 수 있습니다.");
            writePhotoInput.value = "";
            writePhotoPreview.hidden = true;
            return;
        }

        if (file.size >= 5 * 1024 * 1024) {
            alert("5MB 미만의 이미지만 첨부할 수 있습니다.");
            writePhotoInput.value = "";
            writePhotoPreview.hidden = true;
            return;
        }

        writePhotoPreview.src = URL.createObjectURL(file);
        writePhotoPreview.hidden = false;

    });


    /*
     * 제출 전 마지막 확인 (서버도 다시 검사하지만, 사용자 편의를 위해 미리 알려준다)
     */

    document.getElementById("writeForm").addEventListener("submit", function (event) {

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
