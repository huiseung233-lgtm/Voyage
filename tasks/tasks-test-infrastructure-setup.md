## Relevant Files

- `app/build.gradle.kts` - 테스트 관련 의존성(JUnit, Hilt, Coroutines Test 등)을 관리하는 파일입니다.
- `app/src/androidTest/java/com/captain/voyage/CustomTestRunner.kt` - Hilt를 안드로이드 테스트 환경(Instrumented Test)에서 사용하기 위한 커스텀 Runner입니다.
- `app/src/test/java/com/captain/voyage/utils/MainDispatcherRule.kt` - 단위 테스트(Unit Test)에서 Coroutines의 `Main` 디스패처를 제어하기 위한 JUnit Rule입니다.
- `app/src/test/java/com/captain/voyage/ExampleUnitTest.kt` - 로컬 단위 테스트 환경 검증용 샘플 테스트입니다.
- `app/src/androidTest/java/com/captain/voyage/ExampleInstrumentedTest.kt` - 안드로이드 기기 테스트 환경 검증용 샘플 테스트입니다.

### Notes

- 모든 작업은 로컬 환경에서 테스트가 "성공(Pass)"하는 것을 확인해야 완료됩니다.
- 에뮬레이터 또는 실제 안드로이드 기기가 연결되어 있어야 `Instrumented Test`를 실행할 수 있습니다.
- `./gradlew testDebugUnitTest` 명령어로 로컬 단위 테스트를 실행합니다.
- `./gradlew connectedDebugAndroidTest` 명령어로 기기 테스트를 실행합니다.

## Instructions for Completing Tasks

**중요:** 각 작업을 완료할 때마다 이 마크다운 파일의 `- [ ]`를 `- [x]`로 변경하여 진행 상황을 추적하세요.

예시:
- `- [ ] 1.1 파일 읽기` → `- [x] 1.1 파일 읽기` (완료 후)

상위 작업을 완료했을 때뿐만 아니라, **각 하위 작업을 완료할 때마다 파일을 업데이트**해주세요.

## Tasks

- [x] 0.0 기능 브랜치 생성
  - [x] 0.1 새 브랜치 생성 및 체크아웃 (`git checkout -b feature/test-infra-setup`)

- [x] 1.0 현재 테스트 환경 진단 및 오류 분석
  - [x] 1.1 `./gradlew testDebugUnitTest`를 실행하여 현재 단위 테스트 실패 로그 및 빌드 에러를 수집한다.
  - [x] 1.2 `./gradlew connectedDebugAndroidTest`를 실행하여 현재 안드로이드 테스트 실패 로그 및 빌드 에러를 수집한다.
  - [x] 1.3 `app/build.gradle.kts`를 분석하여 중복되거나 충돌하는 테스트 의존성이 있는지 확인한다 (특히 Hilt, Compose, Room 관련).

- [x] 2.0 로컬 단위 테스트(Unit Test) 인프라 구축 (`src/test`)
  - [x] 2.1 `app/build.gradle.kts`에 필요한 Unit Test 의존성 추가/수정 (JUnit4, Mockito-Kotlin, Coroutines-Test, Hilt-Android-Testing 등).
  - [x] 2.2 Coroutines 테스트를 위한 `MainDispatcherRule.kt` 유틸리티 클래스 생성 (`StandardTestDispatcher` 활용).
  - [x] 2.3 `LiveData` 테스트를 위한 `InstantTaskExecutorRule` 적용 준비 (필요 시 `androidx.arch.core:core-testing` 의존성 확인).

- [x] 3.0 안드로이드 테스트(Instrumented Test) 인프라 구축 (`src/androidTest`)
  - [x] 3.1 `app/build.gradle.kts`에 필요한 Android Test 의존성 추가/수정 (Hilt-Android-Testing, Compose-UI-Test 등).
  - [x] 3.2 Hilt 테스트 실행을 위한 `CustomTestRunner` 클래스 생성 및 `Application` 상속 처리.
  - [x] 3.3 `app/build.gradle.kts`의 `defaultConfig` 내 `testInstrumentationRunner`를 새로 만든 `CustomTestRunner` 경로로 변경.

- [x] 4.0 기존 테스트 코드 수정 및 환경 검증
  - [x] 4.1 `ExampleUnitTest.kt`를 수정하여 `MainDispatcherRule`과 기본 Assert 로직이 정상 동작하는지 확인한다.
  - [x] 4.2 `ExampleInstrumentedTest.kt`를 수정하여 `CustomTestRunner` 하에서 Hilt 주입이 정상 동작하는지 확인한다 (간단한 `@AndroidEntryPoint` 테스트).
  - [x] 4.3 `./gradlew testDebugUnitTest` 및 `./gradlew connectedDebugAndroidTest`가 모두 성공(BUILD SUCCESS)하는지 최종 확인. (Instrumented Test는 빌드 성공까지만)

- [x] 5.0 가이드 문서 작성 및 최종 검토
  - [x] 5.1 프로젝트 루트의 `README.md` 또는 별도의 `DOCS/TESTING.md`에 테스트 실행 방법 및 Rule 사용법을 간략히 기록한다.
  - [x] 5.2 불필요한 임시 파일 정리 및 최종 커밋 준비.
