# 📂 AI 개발 가이드라인 (CONVENTIONS.md)

이 문서는 **Anti-Gravity** 프로젝트의 코드 작성 및 구조 관리에 대한 절대적인 규칙을 정의합니다. AI는 모든 코드 생성 및 리팩토링 제안 시 아래 규칙을 준수해야 합니다.

---

## 1. 기술 스택 및 환경 (Tech Stack)
- **Language**: Java 17+
- **Framework**: Spring Boot 3.x
- **Database**: PostgreSQL (Supabase)
- **Architecture**: Layered Architecture (Presentation - Service - Domain - Infrastructure)

---

## 2. 코드 스타일 및 네이밍 (Coding Standards)
- **Naming**:
    - 클래스명은 `PascalCase`, 변수/메서드명은 `camelCase`를 사용한다.
    - 구현체보다는 인터페이스를 우선하며, 구현체는 `~Impl` 접미사를 사용한다.
    - 변수명에 자료형을 노출하지 않는다. (예: `userList` (X) -> `users` (O))
- **Lombok**: 적극 활용하되, `@Data` 사용은 지양하고 `@Getter`, `@RequiredArgsConstructor`, `@Builder`를 조합한다.
- **Final**: 재할당이 없는 모든 지역 변수와 파라미터에는 `final` 키워드를 붙인다.

---

## 3. 레이어별 책임 (Layered Responsibility)
### Controller (Presentation)
- 비즈니스 로직을 포함하지 않는다. 오직 요청 수신과 응답 변환만 담당한다.
- 엔티티를 직접 반환하지 않고 반드시 DTO를 사용한다.

### Service (Domain Logic)
- 하나의 메서드는 하나의 트랜잭션(`@Transactional`) 단위를 가진다.
- 외부 API 연동이나 DB 접근 세부 사항은 직접 구현하지 않고 추상화된 인터페이스를 호출한다.

### Repository (Infrastructure)
- 도메인 객체와 DB 스키마 간의 매핑만 담당한다.

### Exception Handling
- `GlobalExceptionHandler`를 통해 예외를 공통 관리한다.
- 매직 넘버나 하드코딩된 에러 메시지 대신 `ErrorCode` Enum을 사용한다.

---

## 4. 폴더 및 패키지 구조 (Directory Rules)
모든 소스 코드는 도메인 기반으로 분리하며, 각 도메인 내부에서 계층을 나눈다.

```plaintext
com.antigravity
├── domain
│   ├── user
│   │   ├── controller
│   │   ├── service
│   │   ├── repository
│   │   └── dto
│   └── (other domains...)
├── global
│   ├── config
│   ├── error
│   └── util
```

---

## 5. Git & Commit 규칙
AI가 코드를 수정하거나 제안할 때, 해당 작업에 적합한 **Conventional Commits** 메시지도 함께 제시해야 한다.
- 예: `feat(auth): add jwt provider for token generation`

---

## 6. AI 상호작용 제약 사항 (AI Constraints)
- **Incremental Update**: 전체 코드를 다시 작성하지 말고, 변경된 부분 위주로 코드 스니펫을 제공하라.
- **Explain Why**: 특정 디자인 패턴이나 라이브러리를 선택했을 때는 반드시 그 이유(Trade-off)를 설명하라.
- **Performance**: 특히 DB 쿼리 작성 시 N+1 문제가 발생하지 않도록 Fetch Join 등을 고려하여 작성하라.
- **Testability**: 모든 비즈니스 로직은 JUnit5로 테스트 가능하도록 작성하며, 테스트 코드 예시도 함께 포함하라.
