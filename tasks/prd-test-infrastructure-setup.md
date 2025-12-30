# PRD: Test Infrastructure Setup & Stabilization

## 1. Introduction/Overview
현재 프로젝트(`Voyage`)는 테스트 실행 시 원활하지 않은 문제가 있으며, 구체적인 원인 파악과 해결이 필요한 상태입니다. 본 PRD는 현재의 테스트 실행 문제를 진단하여 해결하고, 향후 개발 시 Local Unit Test(JVM)와 Instrumented Test(Android Device) 모두 매끄럽게 작성하고 실행할 수 있도록 견고한 테스트 인프라를 구축하는 것을 목표로 합니다.

## 2. Goals
*   **테스트 환경 정상화:** 현재 발생하는 테스트 실행 문제(빌드 오류, 설정 오류 등)를 진단하고 해결하여 모든 테스트가 정상적으로 실행되도록 합니다.
*   **표준 테스트 기반 구축:** Hilt, Room, Compose 등 주요 기술 스택에 맞춘 재사용 가능한 테스트 베이스 및 유틸리티를 구성합니다.
*   **문서화 및 가이드:** 개발자가 테스트를 쉽게 작성할 수 있도록 예제 코드와 실행 방법을 정립합니다.

## 3. User Stories
*   **개발자로서** 나는 `./gradlew test` 또는 `./gradlew connectedAndroidTest` 명령어를 통해 로컬에서 모든 테스트를 문제없이 실행하고 싶다.
*   **개발자로서** 나는 Hilt가 적용된 ViewModel이나 Repository를 테스트할 때, 복잡한 설정 없이 의존성을 주입받거나 Mocking하여 테스트하고 싶다.
*   **개발자로서** 나는 Room Database 쿼리가 정상적으로 동작하는지 기기(또는 에뮬레이터) 상에서 빠르게 검증하고 싶다.

## 4. Functional Requirements

### 4.1. 환경 진단 및 수정
*   현재 존재하는 `test` 및 `androidTest` 디렉터리 내의 테스트 코드(`ExampleUnitTest.kt`, `VoyageRepositoryTest.kt` 등)를 실행하여 실패 원인을 분석합니다.
*   `build.gradle.kts`의 테스트 관련 의존성(JUnit, Mockito, Hilt Testing, Compose Testing 등)이 올바른 버전과 조합으로 설정되어 있는지 확인하고 수정합니다.

### 4.2. Local Unit Test 인프라 구축 (`src/test`)
*   **Mocking 환경:** Mockito 또는 MockK를 활용한 단위 테스트 설정을 최적화합니다.
*   **Coroutines 테스트:** `MainDispatcher`를 테스트 환경에서 대체할 수 있는 Rule 또는 Extension을 마련합니다 (`MainDispatcherRule` 등).
*   **ViewModel 테스트:** `LiveData` 또는 `StateFlow` 관찰을 위한 테스트 유틸리티를 구성합니다.

### 4.3. Instrumented Test 인프라 구축 (`src/androidTest`)
*   **Hilt 테스트 설정:** `CustomTestRunner`를 작성하여 Hilt가 적용된 안드로이드 테스트 환경을 구축합니다.
*   **Room DB 테스트:** In-Memory Database를 활용하여 실제 데이터 변경 없이 DB 조작을 검증하는 테스트 패턴을 정립합니다.
*   **Compose UI 테스트:** Compose UI 컴포넌트를 독립적으로 테스트할 수 있는 기본 설정을 마련합니다.

## 5. Non-Goals (Out of Scope)
*   **전수 테스트 작성:** 현재 존재하는 모든 기능에 대해 테스트 케이스를 작성하는 것은 목표가 아닙니다. 환경 구축과 대표적인 샘플 테스트(Happy Path) 정상화에 집중합니다.
*   **CI/CD 파이프라인 연동:** Github Actions 등의 원격 CI 설정은 추후 과제로 미룹니다.

## 6. Success Metrics
*   `./gradlew testDebugUnitTest` 실행 시 빌드 성공 및 기존 샘플 테스트 통과.
*   `./gradlew connectedDebugAndroidTest` 실행 시 빌드 성공 및 기존 샘플 테스트 통과.
*   ViewModel 및 Repository에 대한 새로운 테스트 케이스 작성 시 추가 설정 없이 바로 작성이 가능한 상태.

## 7. Technical Considerations
*   **Dependency Injection:** Hilt를 사용 중이므로, 테스트 환경에서도 Hilt Testing 라이브러리(`hilt-android-testing`)를 올바르게 적용해야 합니다.
*   **Asynchronous Testing:** Kotlin Coroutines 및 Flow 테스트를 위해 `kotlinx-coroutines-test` 라이브러리의 `runTest`, `UnconfinedTestDispatcher` 등을 적절히 활용해야 합니다.
*   **Gradle Configuration:** 테스트 관련 의존성은 `testImplementation`, `androidTestImplementation`, `kaptTest`, `kaptAndroidTest` 등으로 정확히 분리되어야 합니다.

## 8. Open Questions
*   현재 구체적인 에러 로그를 확인하지 못했으므로, 첫 단계로 로그 수집 및 분석이 선행되어야 합니다.
