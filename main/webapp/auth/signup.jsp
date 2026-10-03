<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>

<%@ page import="util.EscapeUtil" %>

<%
    String contextPath = request.getContextPath();

    if (session.getAttribute("loginMember") != null) {
        response.sendRedirect(contextPath + "/main");
        return;
    }

    String msg = (String) request.getAttribute("msg");
%>

<!DOCTYPE html>
<html lang="ko">

<head>
    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>회원가입 - 여행만들기</title>

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
         회원가입
         "이메일"을 그대로 로그인 아이디(MEMBERS.ID)로 쓴다.
    ========================== -->

    <section class="auth-section">

        <div class="auth-layout">

            <div class="auth-visual">
                <div class="auth-visual-overlay"></div>

                <div class="auth-visual-content">
                    <p class="auth-kicker">
                        TRAVEL MAKER
                    </p>

                    <h1>
                        여행만들기와 함께<br>
                        첫 이야기를 시작해요
                    </h1>

                    <p>
                        가입하면 여행 이야기를 작성하고<br>
                        나만의 기록을 남길 수 있어요.
                    </p>

                    <div class="auth-visual-tags" aria-hidden="true">
                        <span>#서울</span>
                        <span>#도쿄</span>
                        <span>#제주</span>
                    </div>
                </div>
            </div>

            <div class="auth-panel">

                <div class="auth-box">

                    <div class="auth-title-area">
                        <span class="auth-title-mark">✈</span>

                        <h2>회원가입</h2>

                        <p class="auth-description">
                            기본 정보를 입력하고 여행만들기를 시작하세요.
                        </p>
                    </div>

                    <% if (msg != null) { %>
                    <p class="field-error"><%= EscapeUtil.html(msg) %></p>
                    <% } %>

                    <form id="signupForm"
                          method="post"
                          action="<%= contextPath %>/member/join">

                        <div class="auth-field">
                            <label for="signupNickname">
                                닉네임
                            </label>

                            <input type="text"
                                   id="signupNickname"
                                   name="signupNickname"
                                   value="<%= EscapeUtil.html(request.getParameter("signupNickname")) %>"
                                   placeholder="사용할 닉네임을 입력해 주세요"
                                   autocomplete="nickname">
                        </div>

                        <div class="auth-field">
                            <label for="signupEmail">
                                이메일
                            </label>

                            <input type="email"
                                   id="signupEmail"
                                   name="signupEmail"
                                   value="<%= EscapeUtil.html(request.getParameter("signupEmail")) %>"
                                   placeholder="이메일을 입력해 주세요"
                                   autocomplete="username">
                        </div>

                        <div class="auth-field">
                            <label for="signupPassword">
                                비밀번호
                            </label>

                            <input type="password"
                                   id="signupPassword"
                                   name="signupPassword"
                                   placeholder="6자 이상 입력해 주세요"
                                   autocomplete="new-password">
                        </div>

                        <div class="auth-field">
                            <label for="signupPasswordConfirm">
                                비밀번호 확인
                            </label>

                            <input type="password"
                                   id="signupPasswordConfirm"
                                   name="signupPasswordConfirm"
                                   placeholder="비밀번호를 다시 입력해 주세요"
                                   autocomplete="new-password">
                        </div>

                        <button type="submit"
                                class="auth-submit">
                            회원가입
                        </button>

                    </form>

                    <div class="auth-divider">
                        <span>TRAVEL COMMUNITY</span>
                    </div>

                    <div class="auth-links">
                        <a href="<%= contextPath %>/member/login">로그인</a>
                        <span aria-hidden="true"></span>
                        <a href="<%= contextPath %>/main">메인으로 돌아가기</a>
                    </div>

                </div>

            </div>

        </div>

    </section>

</main>

<%@ include file="/common/footer.jsp" %>

<script>
    /*
     * 서버에서도 똑같이 검사하지만(JoinCommand), 사용자 편의를 위해 제출 전에 미리 알려준다.
     */

    document.getElementById("signupForm").addEventListener("submit", function (event) {

        const nickname = document.getElementById("signupNickname").value.trim();
        const email = document.getElementById("signupEmail").value.trim();
        const password = document.getElementById("signupPassword").value;
        const passwordConfirm = document.getElementById("signupPasswordConfirm").value;

        if (nickname === "" || email === "" || password === "" || passwordConfirm === "") {
            event.preventDefault();
            alert("모든 항목을 입력해 주세요.");
            return;
        }

        if (password !== passwordConfirm) {
            event.preventDefault();
            alert("비밀번호가 서로 일치하지 않습니다.");
            return;
        }

        if (password.length < 6) {
            event.preventDefault();
            alert("비밀번호는 6자 이상이어야 합니다.");
        }

    });
</script>

</body>
</html>
