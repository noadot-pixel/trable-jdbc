<%@ page pageEncoding="UTF-8" %>
<%--
    공통 푸터. 사용하는 페이지는 include 전에 아래 변수를 선언해야 한다.

        String contextPath = request.getContextPath();
--%>

<footer class="footer">

    <div class="footer-inner">

        <div class="footer-logo-area">

            <a href="<%= contextPath %>/main"
               class="bottom-logo">

                <img src="<%= contextPath %>/images/logo.png"
                     alt="여행만들기 로고">
            </a>

            <p>
                함께 만드는, 더 넓은 여행의 세상
            </p>

        </div>

        <div class="footer-menu">

            <strong>여행만들기</strong>

            <a href="#" class="footer-placeholder-link" data-message="서비스 소개 페이지는 준비 중입니다.">소개</a>
            <a href="#" class="footer-placeholder-link" data-message="이용약관 페이지는 준비 중입니다.">이용약관</a>
            <a href="#" class="footer-placeholder-link" data-message="개인정보처리방침 페이지는 준비 중입니다.">개인정보처리방침</a>

        </div>

        <div class="footer-menu">

            <strong>고객지원</strong>

            <a href="#" class="footer-placeholder-link" data-message="등록된 공지사항이 없습니다.">공지사항</a>
            <a href="#" class="footer-placeholder-link" data-message="자주 묻는 질문 페이지는 준비 중입니다.">자주 묻는 질문</a>
            <a href="#" class="footer-placeholder-link" data-message="문의하기 기능은 준비 중입니다. 급한 문의는 이메일로 남겨주세요.">문의하기</a>

        </div>

    </div>

    <p class="copyright">
        © 2026 여행만들기. All rights reserved.
    </p>

</footer>

<script>
    document.querySelectorAll(".footer-placeholder-link").forEach(function (link) {

        link.addEventListener("click", function (event) {

            event.preventDefault();

            alert(link.dataset.message);

        });

    });
</script>
