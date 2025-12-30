# PRD: Notification-Triggered Score Input Popup

## 1. Introduction/Overview
사용자가 정기 항해 보고 알림을 클릭했을 때, 메인 화면으로 이동하는 대신 즉시 점수를 입력할 수 있는 다이얼로그 창을 띄웁니다. 이 창은 배경이 투명하거나 반투명하여 직전의 컨텍스트(다른 앱 또는 현재 앱 화면)를 유지하는 느낌을 주며, 입력 완료 후 즉시 원래 상태로 복귀할 수 있어야 합니다.

## 2. Goals
*   **접근성 향상:** 알림 클릭 한 번으로 핵심 동작(점수 입력)에 즉시 도달하도록 합니다.
*   **심리스한 흐름:** 점수 입력 후 사용자가 원래 하던 일(유튜브 시청, 지도 확인 등)로 자연스럽게 돌아가게 합니다.
*   **독립적 구현:** 기존 메인 화면 로직을 복잡하게 수정하지 않고 별도의 컴포넌트로 구현합니다.

## 3. User Stories
*   **사용자로서** 나는 유튜브를 보다가 "정기 항해 보고" 알림이 오면, 이를 클릭하여 유튜브 위에 뜨는 작은 창에 바로 점수를 입력하고 싶다.
*   **사용자로서** 점수 입력을 마치면, 앱이 꺼지고 다시 보던 유튜브 화면이 바로 나타나기를 원한다.
*   **사용자로서** 앱 내에서 항해 중(지도 화면)에 알림을 눌렀더라도, 점수 입력 후 다시 지도 화면으로 돌아가고 싶다.

## 4. Functional Requirements

### 4.1. 전용 액티비티 생성 (`ScoreInputActivity`)
*   알림 클릭 시 실행될 별도의 액티비티를 생성합니다.
*   **투명 배경 테마:** 액티비티 배경을 투명하게 설정하고, 중앙에 Compose로 다이얼로그 UI를 렌더링합니다.
*   **작업 완료 처리:** 점수 저장 또는 취소 시 `finish()`를 호출하여 액티비티를 종료합니다.

### 4.2. 알림 인텐트 수정
*   `NotificationHelper`에서 알림 생성 시, `PendingIntent`가 `MainActivity`가 아닌 `ScoreInputActivity`를 가리키도록 수정합니다.
*   `FLAG_ACTIVITY_NEW_TASK`와 `FLAG_ACTIVITY_CLEAR_TOP`을 적절히 사용하여 앱 상태와 상관없이 팝업이 뜨도록 합니다.

### 4.3. 점수 입력 UI (Compose)
*   기존에 구현된 점수 입력 로직(Rule 리스트 표시, 점수 입력 등)을 재사용하거나 공유 컴포넌트로 분리하여 사용합니다.
*   다이얼로그 외부 터치 시 닫기 여부 등을 설정합니다.

### 4.4. 데이터 연동
*   `ScoreInputActivity`에서도 Hilt를 통해 `VoyageRepository`를 주입받아 실시간으로 DB에 점수를 저장할 수 있어야 합니다.

## 5. Non-Goals (Out of Scope)
*   메인 화면의 전체적인 레이아웃 변경.
*   복잡한 애니메이션 효과 (기본 페이드 인/아웃 정도만 포함).

## 6. Success Metrics
*   알림 클릭 시 1초 이내에 점수 입력 팝업이 화면에 나타남.
*   입력 완료 후 `finish()` 호출 시, 이전 앱(또는 이전 화면)으로 즉시 포커스가 돌아감.

## 7. Technical Considerations
*   **Android Theme:** `Theme.AppCompat.Translucent.NoTitleBar` 또는 Compose를 위한 `Theme.Android.Transparent` 설정을 `AndroidManifest.xml`에 적용해야 합니다.
*   **Task Management:** 알림으로 띄운 액티비티가 메인 앱의 백스택(Backstack)을 꼬이지 않게 주의해야 합니다.

## 8. Open Questions
*   점수 입력 완료 후 "항해 일지"를 보여줘야 할 필요가 있을까요? -> 일단은 이전 화면 복귀를 우선으로 합니다.
