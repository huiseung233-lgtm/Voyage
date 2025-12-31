# Task List: Rules Reordering (규칙 순서 변경)

## Relevant Files

- `app/src/main/java/com/captain/voyage/ui/rules/RulesScreen.kt` - 드래그 앤 드롭 UI 로직 추가 필요.
- `app/src/main/java/com/captain/voyage/ui/rules/RulesViewModel.kt` - `updateRulesOrder` 함수 검증 및 로직 보완.
- `app/src/main/java/com/captain/voyage/data/local/VoyageDao.kt` - 순서 저장 쿼리 검증.

## Instructions for Completing Tasks

**IMPORTANT:** 각 작업을 완료할 때마다 `- [ ]`를 `- [x]`로 변경하여 진행 상황을 추적하세요.

## Tasks

- [x] 0.0 기능 브랜치 생성
  - [x] 0.1 `feature/rules-reordering` 브랜치 생성 및 체크아웃 (`git checkout -b feature/rules-reordering`)

- [x] 1.0 현상 분석 (왜 안 되는지 파악)
  - [x] 1.1 `RulesScreen.kt` 코드 분석: UI 드래그 로직이 완전히 누락됨을 확인.
  - [x] 1.2 `RulesViewModel.kt` 코드 분석: `updateRulesOrder` 로직은 정상이나 호출부가 없음.
  - [x] 1.3 `VoyageDao.kt` 코드 분석: `orderIndex` 및 `@Update` 쿼리 준비 완료됨.
  - [x] 1.4 **분석 결론 도출:** UI 단의 드래그 앤 드롭 구현만 추가하면 됨. (기존 로직 활용 가능)

- [x] 2.0 구현 방안 결정 및 적용
  - [x] 2.1 **구현 방식 선택:** 검증된 라이브러리(`sh.calvin.reorderable`) 도입 결정.
  - [x] 2.2 의존성 추가 완료 (`libs.versions.toml`, `build.gradle.kts`).

- [x] 3.0 UI 및 로직 연결
  - [x] 3.1 `RulesScreen.kt`에 드래그 상태(State) 관리 로직 추가.
  - [x] 3.2 드래그 핸들 아이콘에 실제 드래그 감지 제스처 연결.
  - [x] 3.3 드래그 중 아이템의 위치가 실시간으로 바뀌도록 리스트 UI 갱신.
  - [x] 3.4 드래그 종료 시(`onDragEnd`), `RulesViewModel.updateRulesOrder()`를 호출하여 DB 저장.

- [x] 4.0 기능 테스트 및 검증
  - [x] 4.1 검색어 입력 시 드래그 기능이 비활성화되는지 확인 완료.
  - [x] 4.2 앱 재실행 후 순서가 유지되는지 검증 완료.

