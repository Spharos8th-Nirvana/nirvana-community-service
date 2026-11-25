# nirvana-community-service

## 서비스 개요

- 목적: 커뮤니티 서비스. 게시판·카테고리, 게시글, 공지, 댓글, 좋아요, 신고, 리뷰(게시글 형태)를 담당합니다.
- 담당자 / 백업 리뷰어: @xoxxi / <!-- 확정 후 기입 -->
- 소유 데이터와 운영 책임: `nirvana_community` 스키마. 작성자 닉네임 등 회원 정보는 member-service, 첨부 이미지는 media-service를 참조합니다(UUID 참조, FK 없음).
- API 문서: [Swagger UI](http://localhost:8084/swagger-ui/index.html) (local 프로필)
- 기본 포트 / 헬스체크: `8084` (임시, 팀 확정 전) / `GET /actuator/health`

| 항목 | 값 |
| --- | --- |
| Gradle 프로젝트 | `nirvana-community-service` |
| Java 패키지 | `com.nirvana.community` |
| 시작 클래스 | `CommunityServiceApplication` |
| `spring.application.name` | `community-service` → Config Repo `community-service*.yml` |

## 로컬 실행

- 사전 요구사항은 JDK 21입니다.
- 팀 공통 라이브러리 `nirvana-jpa-auditing`을 GitHub Packages에서 받습니다. 빌드 전에 환경 변수 `GITHUB_ACTOR`(GitHub 아이디)와 `GITHUB_TOKEN`(`read:packages` 권한 토큰)을 설정합니다.
- macOS와 Linux: `./gradlew bootRun`
- Windows PowerShell: `.\gradlew.bat bootRun`
- Windows Git Bash: `./gradlew bootRun`
- 기본 local 프로필은 Config Server 없이 시작하며 Swagger UI를 활성화합니다.
- 다른 프로필에서는 API 문서와 Swagger UI가 기본으로 비활성화됩니다.

### Config Server 사용

- `SPRING_PROFILES_ACTIVE=config-server`로 설정합니다.
- 로컬 Swagger UI도 함께 사용하려면 `local,config-server`로 설정합니다.
- `CONFIG_SERVER_URL` 기본값은 `http://localhost:8888`입니다.

## 빌드·테스트

macOS, Linux, Windows Git Bash:

```bash
./gradlew build
```

Windows PowerShell:

```powershell
.\gradlew.bat build
```

JAR만 빌드하려면 bootJar를 사용합니다.
현재 기본 CI는 develop·main 대상 PR에서 bootJar를 실행합니다.
build에 포함된 테스트 실행과 CI의 실제 검사 범위는 구분해서 확인하세요.

## 개발 규칙

개발과 기여 방법은 [CONTRIBUTING.md](CONTRIBUTING.md)를 참고하세요.
