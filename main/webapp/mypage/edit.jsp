<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>

<%@ page import="model.MemberDTO" %>
<%@ page import="util.EscapeUtil" %>

<%
    String contextPath = request.getContextPath();

    MemberDTO loginMember = (MemberDTO) session.getAttribute("loginMember");

    if (loginMember == null) {
        response.sendRedirect(contextPath + "/member/login");
        return;
    }

    String msg = (String) request.getAttribute("msg");

    //실패해서 다시 돌아온 경우에는 방금 입력했던 값을, 처음 들어온 경우에는 세션의 현재 값을 보여준다.
    boolean isResubmit = request.getParameter("profileNickname") != null;

    String nicknameValue = isResubmit ? request.getParameter("profileNickname") : loginMember.getNickname();
    String bioValue = isResubmit ? request.getParameter("profileBio") : loginMember.getBio();
    String countryValue = isResubmit ? request.getParameter("profileCountry") : loginMember.getCountry();
    String countryOtherValue = isResubmit ? request.getParameter("profileCountryOther") : loginMember.getCountryOther();
    String nameValue = isResubmit ? request.getParameter("profileName") : loginMember.getRealName();
    String birthdayValue = isResubmit ? request.getParameter("profileBirthday") : loginMember.getBirthday();
    boolean namePublicValue = isResubmit ? !"private".equals(request.getParameter("nameVisibility")) : loginMember.isNamePublic();
    boolean birthdayPublicValue = isResubmit ? !"private".equals(request.getParameter("birthdayVisibility")) : loginMember.isBirthdayPublic();

    if (countryValue == null || countryValue.isEmpty()) countryValue = "KR";
    if (bioValue == null) bioValue = "";
    if (countryOtherValue == null) countryOtherValue = "";
    if (nameValue == null) nameValue = "";
    if (birthdayValue == null) birthdayValue = "";
%>

<!DOCTYPE html>
<html lang="ko">

<head>
    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>회원정보 수정 - 여행만들기</title>

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
            〉 <a href="<%= contextPath %>/member/mypage">마이페이지</a>
            〉 회원정보 수정
        </p>

        <h1>회원정보 수정</h1>

        <p class="page-description">
            내 프로필 정보를 수정하세요.
        </p>

    </section>

    <% if (msg != null) { %>
    <p class="field-error"><%= EscapeUtil.html(msg) %></p>
    <% } %>

    <section class="write-section mypage-section">

        <form id="profileForm"
              method="post"
              enctype="multipart/form-data"
              action="<%= contextPath %>/member/mypageEdit">

            <div class="write-field">

                <label for="profilePhoto">
                    프로필 이미지
                </label>

                <div class="profile-photo-field">

                    <img id="profilePhotoPreview"
                         class="profile-photo-preview"
                         src="<%= loginMember.getPhotoPath() != null ? (contextPath + "/" + loginMember.getPhotoPath()) : (contextPath + "/images/logo.png") %>"
                         alt="프로필 이미지 미리보기">

                    <input type="file"
                           id="profilePhoto"
                           name="profilePhoto"
                           accept="image/*">

                </div>

                <p class="write-field-note">
                    5MB 이하 이미지 파일만 업로드할 수 있습니다.
                </p>

            </div>

            <div class="write-field">

                <label for="profileNickname">
                    닉네임
                </label>

                <input type="text"
                       id="profileNickname"
                       name="profileNickname"
                       value="<%= EscapeUtil.html(nicknameValue) %>">

            </div>

            <div class="write-field">

                <label for="profileBio">
                    자기소개
                </label>

                <textarea id="profileBio"
                          name="profileBio"
                          rows="4"
                          placeholder="자신을 간단히 소개해 주세요"><%= EscapeUtil.html(bioValue) %></textarea>

            </div>

            <div class="write-field">

                <label for="profileCountry">
                    국가
                </label>

                <select id="profileCountry" name="profileCountry">
                    <option value="KR" <%= "KR".equals(countryValue) ? "selected" : "" %>>한국</option>
                    <option value="JP" <%= "JP".equals(countryValue) ? "selected" : "" %>>일본</option>
                    <option value="ETC" <%= "ETC".equals(countryValue) ? "selected" : "" %>>기타</option>
                </select>

            </div>

            <div class="write-field"
                 id="profileCountryOtherField"
                 <%= "ETC".equals(countryValue) ? "" : "hidden" %>>

                <label for="profileCountryOther">
                    국가 직접 입력
                </label>

                <input type="text"
                       id="profileCountryOther"
                       name="profileCountryOther"
                       value="<%= EscapeUtil.html(countryOtherValue) %>"
                       placeholder="국가를 입력해 주세요">

            </div>

            <div class="mypage-personal-section">

                <p class="filter-label">개인정보</p>

                <div class="write-field">

                    <label for="profileBirthday">
                        생일
                    </label>

                    <input type="date"
                           id="profileBirthday"
                           name="profileBirthday"
                           value="<%= birthdayValue %>">

                    <div class="radio-group">

                        <label class="radio-option">
                            <input type="radio" name="birthdayVisibility" value="public" <%= birthdayPublicValue ? "checked" : "" %>>
                            공개
                        </label>

                        <label class="radio-option">
                            <input type="radio" name="birthdayVisibility" value="private" <%= birthdayPublicValue ? "" : "checked" %>>
                            비공개
                        </label>

                    </div>

                </div>

                <div class="write-field">

                    <label for="profileName">
                        이름
                    </label>

                    <input type="text"
                           id="profileName"
                           name="profileName"
                           value="<%= EscapeUtil.html(nameValue) %>"
                           placeholder="이름을 입력해 주세요">

                    <div class="radio-group">

                        <label class="radio-option">
                            <input type="radio" name="nameVisibility" value="public" <%= namePublicValue ? "checked" : "" %>>
                            공개
                        </label>

                        <label class="radio-option">
                            <input type="radio" name="nameVisibility" value="private" <%= namePublicValue ? "" : "checked" %>>
                            비공개
                        </label>

                    </div>

                </div>

            </div>

            <button type="submit" class="auth-submit">
                저장하기
            </button>

        </form>

    </section>

</main>

<%@ include file="/common/footer.jsp" %>

<script>
    /*
     * 국가를 "기타"로 선택했을 때만 자유 입력란을 보여준다.
     */

    const countrySelect = document.getElementById("profileCountry");
    const countryOtherField = document.getElementById("profileCountryOtherField");

    countrySelect.addEventListener("change", function () {
        countryOtherField.hidden = countrySelect.value !== "ETC";
    });


    /*
     * 프로필 이미지를 고르면 바로 미리보기로 보여준다. 실제 업로드는 저장할 때 한 번만 일어난다.
     */

    const profilePhotoInput = document.getElementById("profilePhoto");
    const profilePhotoPreview = document.getElementById("profilePhotoPreview");

    profilePhotoInput.addEventListener("change", function () {

        const file = profilePhotoInput.files[0];

        if (!file) {
            return;
        }

        const reader = new FileReader();

        reader.onload = function (event) {
            profilePhotoPreview.src = event.target.result;
        };

        reader.readAsDataURL(file);

    });
</script>

</body>
</html>
