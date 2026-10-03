# 개인 서버 PC 배포 가이드

작성일: 2026-10-03

지금 개발 PC(`C:\AI2GI\trable-jdbc`)에서 Tomcat 10.1.59 + JDK 17 + Oracle Database 11g XE(11.2.0.2.0)로 전체 기능을 실제 DB까지 연결해서 검증한 걸, **개인 서버 PC**에 똑같이 올리고 도메인으로 실제 접속되게 만드는 절차다.

> **먼저 알아둘 것**: Oracle Database 11g는 2015년에 Oracle 공식 지원이 끝났다(이후 보안 패치 없음). 이 가이드는 **DB를 인터넷에 절대 직접 노출하지 않는 것**을 전제로 한다 — 공유기/방화벽에서 열어주는 포트는 웹(80/443)뿐이고, Oracle의 1521 포트와 Tomcat의 종료 포트는 외부에서 절대 접근 못 하게 막는다. 그래도 DB 자체가 오래된 버전이라는 위험은 남아있다는 걸 인지하고 진행할 것.

---

## 0. 지금까지 검증된 조합 (서버 PC에도 똑같이 맞출 것)

| 항목 | 버전 |
|---|---|
| JDK | 17 |
| Tomcat | 10.1.59 (Jakarta EE, `jakarta.servlet.*`) |
| Oracle | Database 11g Express Edition (11.2.0.2.0), SID `XE` |
| JDBC 드라이버 | `ojdbc11.jar` (Oracle 23ai 클라이언트, 11g에도 하위호환으로 정상 접속됨 — 개발 PC에서 확인함) |

---

## 1. 서버 PC에 소프트웨어 설치

1. **JDK 17** 설치 (Oracle 또는 Adoptium 등 아무 배포판)
2. **Oracle Database 11g Express Edition (64bit)** 설치
   - 공식 다운로드: https://www.oracle.com/database/technologies/xe-prior-release-downloads.html (Oracle 계정 로그인 필요)
   - 설치 후 서비스 확인: `OracleServiceXE`, `OracleXETNSListener` 둘 다 "자동" 시작으로 설정돼 있어야 재부팅해도 알아서 켜진다 (기본값이 이미 그렇다)
3. **Tomcat 10.1.59**
   - https://archive.apache.org/dist/tomcat/tomcat-10/v10.1.59/bin/apache-tomcat-10.1.59-windows-x64.zip
   - 압축을 `C:\apache-tomcat-10.1.59` 같은 곳에 푼다
4. **Git**으로 이 저장소를 받는다: `git clone https://github.com/noadot-pixel/trable-jdbc.git`

---

## 2. DB 계정 + 스키마

개발 PC와 똑같이:

```powershell
$env:PATH += ";C:\oraclexe\app\oracle\product\11.2.0\server\bin"
sqlplus "/ as sysdba"
```

```sql
CREATE USER trable IDENTIFIED BY "새_비밀번호_직접_정하기" DEFAULT TABLESPACE USERS TEMPORARY TABLESPACE TEMP;
GRANT CONNECT, RESOURCE TO trable;
GRANT UNLIMITED TABLESPACE TO trable;
exit
```

```powershell
sqlplus "trable/새_비밀번호@XE"
```
```sql
@C:\경로\trable-jdbc\docs\schema.sql
exit
```

`DESC members`로 컬럼 13개(특히 `REAL_NAME`)가 다 있는지 확인할 것 — 한글 주석이 든 `.sql`을 돌릴 때 SQL*Plus가 조용히 컬럼을 누락시킨 적이 있다(지금 `schema.sql`은 이미 고쳐서 영어 주석만 있지만, 혹시 직접 수정했다면 다시 확인).

비밀번호는 **Windows 사용자 환경변수**로 영구 저장한다(소스에 절대 적지 않음):

```powershell
[Environment]::SetEnvironmentVariable("TRABLE_DB_PASSWORD", "새_비밀번호", "User")
```

---

## 3. ojdbc11.jar

저장소에는 라이선스 때문에 포함돼 있지 않다. Oracle 공식 사이트에서 받거나, 개발 PC의 `main/webapp/WEB-INF/lib/ojdbc11.jar`를 복사해서 서버 PC의 같은 경로(`trable-jdbc\main\webapp\WEB-INF\lib\ojdbc11.jar`)에 넣는다.

---

## 4. 컴파일 + 배포

```powershell
cd C:\경로\trable-jdbc
& "C:\Program Files\Java\jdk-17\bin\javac.exe" -encoding UTF-8 -cp "C:\apache-tomcat-10.1.59\lib\servlet-api.jar" -d main\webapp\WEB-INF\classes (Get-ChildItem -Recurse src\main\java\*.java | % FullName)

New-Item -ItemType Junction -Path "C:\apache-tomcat-10.1.59\webapps\trablejdbc" -Target "C:\경로\trable-jdbc\main\webapp"
```

### Tomcat을 포트 80/443으로 띄우지 말고, 지금처럼 내부 포트(예: 8081)로 두고 앞에 리버스 프록시를 둘 것

이유: Tomcat에서 직접 HTTPS 인증서를 관리하는 것보다, 앞에 프록시를 하나 두고 거기서 인증서를 자동 갱신하게 하는 게 훨씬 간단하고 유지보수가 쉽다.

`conf/server.xml`에서 내부 포트를 그대로 8081(또는 원하는 값)로 둔다.

---

## 5. Tomcat을 Windows 서비스로 등록 (재부팅해도 자동 시작)

지금까지 한 것처럼 `startup.bat`을 수동으로 띄우면 로그아웃하거나 재부팅하면 꺼진다. 서버는 서비스로 등록해야 한다.

```powershell
cd C:\apache-tomcat-10.1.59\bin
.\service.bat install Tomcat10
```

서비스로 등록하면 **환경변수를 서비스 실행 계정 기준으로 다시 확인**해야 한다. `[Environment]::SetEnvironmentVariable(... , "User")`로 저장한 값은 그 Windows 계정으로 로그인했을 때만 적용되는데, 서비스는 보통 시스템 계정으로 돌아간다. 둘 중 하나로 처리:

- 간단한 방법: `"Machine"`(시스템 전체) 범위로 환경변수를 다시 설정
  ```powershell
  [Environment]::SetEnvironmentVariable("TRABLE_DB_PASSWORD", "새_비밀번호", "Machine")
  ```
- 서비스 설치 후에는 `services.msc`에서 Tomcat10 서비스를 재시작해야 새 환경변수가 반영된다.

서비스 시작 유형을 "자동"으로 설정해두면 PC가 켜질 때마다 Tomcat도 같이 켜진다.

---

## 6. 리버스 프록시 + HTTPS (Caddy 추천)

[Caddy](https://caddyserver.com/)는 설정 파일 몇 줄로 **Let's Encrypt 인증서를 알아서 발급·갱신**해준다 — nginx+certbot 조합보다 훨씬 간단해서 혼자 운영하는 개인 서버에 적합하다.

1. https://caddyserver.com/download 에서 Windows용 `caddy.exe` 받기
2. 같은 폴더에 `Caddyfile` 생성:

```
여러분의도메인.com {
    reverse_proxy localhost:8081
}
```

3. 실행: `caddy run` (테스트용) 또는 Windows 서비스로 등록해서 상시 구동
   - 서비스 등록: `caddy.exe add-package github.com/caddyserver/[email protected]` 같은 건 필요 없고, [NSSM](https://nssm.cc/)으로 `caddy run --config Caddyfile`을 서비스화하는 게 흔한 방법이다.

Caddy가 80/443 포트로 들어온 요청을 받아서 내부적으로 8081(Tomcat)로 넘겨준다. 도메인 접속 시 자동으로 `https://`가 적용된다.

---

## 7. 공유기 / 방화벽

1. **서버 PC의 로컬 IP 확인**: `ipconfig`에서 `IPv4 주소` 확인 (예: `192.168.0.50`)
2. **공유기 관리 페이지**(보통 `192.168.0.1` 또는 `192.168.1.1`)에 로그인 → **포트포워딩** 설정
   - 외부 포트 80 → 서버 PC의 192.168.0.50:80
   - 외부 포트 443 → 서버 PC의 192.168.0.50:443
   - **1521(Oracle), 8006(Tomcat 종료포트), 8081(Tomcat 내부포트)은 포워딩하지 않는다** — 외부에서 직접 못 들어오게 막아두는 것
3. **Windows 방화벽**: 인바운드 규칙에서 80/443 포트 허용 (Caddy/Tomcat 설치 시 자동으로 추가되기도 하지만 직접 확인할 것)
4. **공인 IP가 고정인지 확인**: 가정용 인터넷 회선은 보통 IP가 가끔 바뀐다(동적 IP). ISP에 고정 IP 신청이 가능한지 확인하거나, 안 되면 **DDNS**(예: No-IP, DuckDNS)로 IP가 바뀌어도 도메인이 계속 서버를 찾아오게 해야 한다. (고정 IP라면 이 단계는 생략)

---

## 8. 도메인 DNS 설정

이미 준비돼 있다고 하셨으니, 도메인을 산 곳(가비아, 후이즈, Namecheap 등)의 DNS 관리 화면에서:

- **A 레코드** 추가: `@`(또는 원하는 서브도메인) → 서버 PC의 **공인 IP**
- 공인 IP는 서버 PC에서 `(Invoke-WebRequest -Uri "https://api.ipify.org").Content` 같은 걸로 확인 가능
- DDNS를 쓴다면 A 레코드 대신 DDNS 서비스가 안내하는 방식(CNAME 등)을 따른다

DNS가 전파되는 데 몇 분~몇 시간 걸릴 수 있다.

---

## 9. 최종 확인 순서

1. 서버 PC에서 `http://localhost:8081/trablejdbc/main` 접속 → 로컬에서는 되는지 먼저 확인
2. 같은 네트워크의 다른 기기에서 서버 PC의 로컬 IP로 접속 → LAN 안에서는 되는지 확인
3. 핸드폰 데이터(와이파이 끄고) 등 **외부 네트워크**에서 `https://도메인.com` 접속 → 진짜 외부에서 되는지 최종 확인

---

## 10. 보안 체크리스트 (운영 시작 전 마지막으로)

- [ ] 1521, 8006, 8081 포트는 공유기에서 포워딩 안 됨 (80/443만 열림)
- [ ] `TRABLE_DB_PASSWORD`는 소스/저장소 어디에도 없고 환경변수로만 존재
- [ ] Oracle DB 접속 계정(`trable`)의 비밀번호가 충분히 복잡함
- [ ] Windows가 최신 업데이트 상태
- [ ] Tomcat, Caddy 둘 다 Windows 서비스로 등록되어 재부팅해도 자동 시작
- [ ] Oracle `members.pwd`가 평문 저장이라는 점 인지(PROGRESS.md 참고) — 실사용자 비밀번호가 쌓이기 전에 해시 적용을 권장
- [ ] DB 백업 계획(최소한 가끔 수동으로라도 `EXPDP`/데이터 내보내기)
