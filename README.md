# nirvana-community-service

## 서비스 개요

- 목적: 커뮤니티 서비스. 게시판·카테고리, 게시글, 공지, 댓글, 좋아요, 신고, 리뷰(게시글 형태)를 담당합니다.
- 담당자 / 백업 리뷰어: @xoxxi / <!-- 확정 후 기입 -->
- 소유 데이터와 운영 책임: `nirvana_community` 스키마. 작성자 닉네임 등 회원 정보는 member-service, 첨부 이미지는 media-service를 참조합니다(UUID 참조, FK 없음).
- API 문서: [Swagger UI](http://localhost:8087/swagger-ui/index.html) (local 프로필)
- 기본 포트 / 헬스체크: `8087` / `GET /actuator/health`

| 항목 | 값 |
| --- | --- |
| Gradle 프로젝트 | `nirvana-community-service` |
| Java 패키지 | `com.nirvana.community` |
| 시작 클래스 | `CommunityServiceApplication` |
| `spring.application.name` | `community-service` → Config Repo `community-service*.yml` |

## 로컬 실행

- 사전 요구사항은 JDK 21입니다.
- macOS와 Linux: `./gradlew bootRun`
- Windows PowerShell: `.\gradlew.bat bootRun`
- Windows Git Bash: `./gradlew bootRun`
- 기본 local 프로필은 Config Server 없이 시작하며 Swagger UI를 활성화합니다.
- 다른 프로필에서는 API 문서와 Swagger UI가 기본으로 비활성화됩니다.

### 로컬 DB

local 프로필은 MySQL(업무 데이터)과 MongoDB(게시글 조회수·좋아요 수·댓글 수)에 접속합니다.

| DB | 환경변수 (기본값) |
| --- | --- |
| MySQL `nirvana_community` | `DB_HOST`(`localhost`), `DB_PORT`(`13307`), `DB_USERNAME`, `DB_PASSWORD` |
| MongoDB `nirvana_community` | `MONGO_HOST`(`localhost`), `MONGO_PORT`(`27117`) |

MongoDB는 인증 없는 단일 노드 컨테이너로 띄웁니다.

```bash
docker run -d --name community-mongo -p 127.0.0.1:27117:27017 -v community-mongo-data:/data/db mongo:8.0
```

- MongoDB가 꺼져 있어도 앱은 시작됩니다. 집계 수는 0으로 나오고 경고 로그가 남습니다. `/actuator/health`는 MongoDB를 검사하지 않습니다(`management.health.mongodb.enabled: false`).
- 테스트(`./gradlew build`)는 MongoDB 없이 통과합니다.

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

## 패키지 구조

```
com.nirvana.community
├── global/   # 여러 도메인이 같이 쓰는 코드: 응답, 예외, 인증 정보, 설정
└── domain/   # 도메인별 코드
    ├── board/
    ├── post/
    ├── comment/
    ├── like/
    ├── report/
    └── review/
```

새 코드는 그 코드가 다루는 테이블을 맡은 도메인 패키지에 둡니다.
여러 도메인이 같이 쓰는 코드만 `global`에 둡니다.

| 패키지 | 맡는 테이블 | 요구사항 |
| --- | --- | --- |
| `board` | boardTypes, boardPermissions, boardCategories, templeBoards | COM-BRD |
| `post` | posts, postMedia, postPins, postsCount | COM-PST, 조회수 COM-LIK-10~11 |
| `comment` | comments | COM-CMT |
| `like` | postLikes, commentLikes | COM-LIK (조회수 제외) |
| `report` | reportTargets, reports, reportThresholds | COM-RPT |
| `review` | reviewEligibilities, reviewDetails | COM-REV |

각 패키지의 `package-info.java`에 맡는 일과 요구사항 ID 범위가 적혀 있습니다.

### 도메인 패키지 안의 계층

도메인 패키지 안은 계층별 하위 패키지로 나눕니다. 예: `domain/post/service/PostService.java`

| 하위 패키지 | 두는 것 |
| --- | --- |
| `controller` | HTTP 요청·응답 처리, 요청 DTO 검증 |
| `service` | 비즈니스 로직, 권한 검증, 트랜잭션 |
| `repository` | Entity 조회·저장 (DB 접근) |
| `entity` | Entity와 도메인 상태 규칙, 상태 enum |
| `dto` | API 요청·응답 객체 |

- 호출 방향은 `controller → service → repository`입니다. Controller가 Repository를 직접 부르지 않습니다.
- 다른 도메인의 데이터가 필요하면 그 도메인의 Service를 부릅니다.
- 하위 패키지는 미리 만들지 않습니다. 그 계층의 첫 클래스를 만들 때 생깁니다.
- `outboxEvents`, `processedEvents`는 도메인이 아니라 Kafka 연동용 테이블이라, Kafka 작업 때 `global` 아래에 둡니다.

## 개발 규칙

개발과 기여 방법은 [CONTRIBUTING.md](CONTRIBUTING.md)를 참고하세요.
