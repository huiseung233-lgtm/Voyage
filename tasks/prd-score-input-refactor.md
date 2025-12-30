# PRD: Score Input Popup Refactoring (Reusing CommonLogbookDialog)

## 1. Introduction/Overview
현재 하이브리드 팝업(오버레이 및 투명 액티비티)에서 사용하는 임시 점수 입력창을 제거하고, 홈 화면에서 사용 중인 `CommonLogbookDialog`를 재사용하도록 개선합니다. 이를 통해 앱 전체의 UI 일관성을 유지하고, 알림 클릭 한 번으로 상세한 일지 기록 기능을 사용자에게 제공합니다.

## 2. Goals
*   **UI 일관성:** 홈 화면, 게임 화면, 팝업창에서 모두 동일한 `CommonLogbookDialog`를 사용하여 사용자에게 익숙한 경험 제공.
*   **기능 통합:** 단순히 체크리스트만 보여주는 것이 아니라, 상세 기록 조회, 추가, 삭제 등 모든 일지 기능을 팝업에서 사용 가능하게 함.
*   **코드 재사용:** 중복된 입력 로직을 제거하고 공통 컴포넌트 중심으로 로직 통합.

## 3. User Stories
*   **사용자로서** 나는 알림을 클릭했을 때 뜨는 창이 홈 화면에서 날짜를 눌렀을 때와 똑같은 "항해 일지" 창이기를 기대한다.
*   **사용자로서** 나는 팝업창에서도 규칙 외에 새로운 기록을 직접 입력하거나, 실수로 넣은 기록을 삭제하고 싶다.
*   **사용자로서** 일지 저장을 완료하면 즉시 하던 일(다른 앱 등)로 돌아가고 싶다.

## 4. Functional Requirements

### 4.1. `PopupViewModel` 강화 (Data Provider)
*   `CommonLogbookDialog`에 필요한 데이터를 공급할 수 있도록 로직을 확장합니다.
*   **오늘의 기록 로드:** `repository.getScoreRecordsByDate(todayDate)`를 통해 이미 저장된 내역을 불러와 `initialRecords`로 전달합니다.
*   **일괄 저장 로직:** `HomeViewModel.saveBatchRecords`와 동일하게, 전달받은 `List<ScoreRecord>`를 DB에 저장하고 `DailyLog`를 업데이트하는 함수를 구현합니다.

### 4.2. `PopupActivity` 및 `ScoreOverlayService` 수정
*   기존의 `PopupScreen` 컴포넌트를 제거합니다.
*   대신 `CommonLogbookDialog`를 호출하며, `PopupViewModel`에서 관찰 중인 `rules`와 `initialRecords`를 넘겨줍니다.
*   `onSave` 콜백에서 ViewModel의 저장 함수를 호출하고, 완료 후 `finish()` (Activity) 또는 `stopSelf()` (Service)를 호출합니다.

### 4.3. UI/UX 미세 조정 (Optional)
*   팝업창(700.dp)이 화면에 너무 클 경우를 대비해, 오버레이 환경에서는 크기를 적절히 조절하거나 스크롤이 원활한지 확인합니다.

## 5. Non-Goals (Out of Scope)
*   `CommonLogbookDialog` 자체의 디자인을 크게 변경하는 것.
*   홈 화면(`HomeViewModel`)의 로직을 대대적으로 리팩토링하는 것 (필요한 로직만 `PopupViewModel`에 가져옴).

## 6. Success Metrics
*   알림 클릭 시 기존 `PopupScreen` 대신 정식 항해 일지(`CommonLogbookDialog`)가 나타남.
*   팝업 내에서 기록 추가/삭제가 정상 동작하고, "최종 저장" 시 DB에 반영됨.
*   저장 완료 후 자동으로 팝업이 닫히고 이전 화면으로 복귀함.

## 7. Technical Considerations
*   **Dependency:** `PopupViewModel`은 `HiltViewModel`로 유지하여 Repository 주입을 받습니다.
*   **State Management:** `initialRecords`가 로딩되는 동안 로딩 스피너(ProgressIndicator)를 보여주는 기존 `LogbookDialog`의 패턴을 참고합니다.

## 8. Open Questions
*   `CommonLogbookDialog` 내부에 `Dialog` 컴포넌트가 중첩되어 있는데, `PopupActivity`(이미 투명 배경)나 `Service`(WindowManager에 직접 추가)에서 호출할 때 문제가 없는지 확인이 필요합니다. (필요 시 `CommonLogbookDialog` 내부의 `Dialog` 래퍼를 조건부로 제거하거나 분리)
