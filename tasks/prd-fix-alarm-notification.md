# PRD: Fix Alarm Notification Bug

## 1. Introduction/Overview
현재 설정 화면에서 알람 간격을 설정하고 알람을 활성화해도 실제 알림이 울리지 않는 버그가 있습니다. 이 기능은 사용자에게 주기적으로 항해 상태를 점검하도록 상기시켜주는 중요한 리텐션 기능입니다. 본 PRD는 이 버그를 수정하여 알람이 설정된 간격마다 안정적으로 동작하도록 하는 것을 목표로 합니다.

## 2. Goals
*   **버그 수정:** 사용자가 설정한 간격(예: 10분, 60분 등)마다 알림이 정상적으로 수신되도록 합니다.
*   **권한 확보:** 앱 시작 시 또는 적절한 시점에 알림 및 정확한 알람 권한을 확보하여 기능 실패를 방지합니다.
*   **UX 개선:** 알림 클릭 시 앱으로 진입하여 사용자가 즉시 항해 일지를 확인할 수 있도록 연결합니다.

## 3. User Stories
*   **사용자로서** 나는 설정 화면에서 "항해 보고 알림"을 켜고 간격을 "10분"으로 설정하면, 10분마다 폰에 알림이 오기를 기대한다.
*   **사용자로서** 나는 도착한 "정기 항해 보고" 알림을 터치했을 때, Voyage 앱이 열리면서 바로 항해 일지나 메인 화면을 보고 싶다.
*   **사용자로서** 나는 앱을 재부팅하더라도 알림 설정이 유지되어 계속해서 알림을 받고 싶다.

## 4. Functional Requirements

### 4.1. 알림 스케줄링 수정
*   `NotificationHelper` 또는 `AlarmManager` 로직을 개선하여, 최신 안드로이드 버전(12, 13, 14)에서도 반복 알람이 안정적으로 동작하도록 수정해야 합니다.
*   `setRepeating`이 부정확할 경우 `setExactAndAllowWhileIdle` 등을 사용하여 정확도를 높이거나, 적어도 설정된 간격과 유사하게 동작하도록 보장해야 합니다.

### 4.2. 권한 관리
*   `AndroidManifest.xml`에 `RECEIVE_BOOT_COMPLETED` 및 `SCHEDULE_EXACT_ALARM` 등 필수 권한이 누락되지 않았는지 확인하고 추가합니다.
*   앱 시작 시(Main Activity 진입 시) 알림 권한(`POST_NOTIFICATIONS`)과 정확한 알람 권한을 체크하고, 없다면 사용자에게 요청해야 합니다.

### 4.3. 알림 수신 및 처리
*   `AlarmReceiver`는 알람 신호를 받아 `NotificationHelper`를 통해 즉시 알림을 띄워야 합니다.
*   생성된 알림은 클릭 시 `MainActivity`를 실행해야 하며, 이미 앱이 실행 중이라면 기존 화면을 유지하거나 갱신해야 합니다.

### 4.4. 설정 UI 연동
*   설정 화면의 "알림 켜기/끄기" 토글과 "알림 간격" 슬라이더(또는 입력)가 `SettingsViewModel`을 통해 실제 알람 스케줄러와 즉시 동기화되어야 합니다.
*   알림을 끄면 예약된 모든 알람이 즉시 취소되어야 합니다.

## 5. Non-Goals (Out of Scope)
*   알림 사운드나 진동 패턴 커스터마이징 기능은 이번 버그 수정 범위에 포함하지 않습니다.
*   서버 푸시 알림(FCM) 연동은 포함하지 않습니다. (로컬 알람만 다룸)

## 6. Success Metrics
*   설정된 간격(예: 10분) 후 실제 기기/에뮬레이터 상단에 알림 배너가 표시됨.
*   알림 클릭 시 앱으로 정상 진입함.
*   기기 재부팅 후에도 알람 설정이 유지되어 알림이 옴.

## 7. Technical Considerations
*   **Android 13+ (API 33):** `POST_NOTIFICATIONS` 런타임 권한이 필수입니다.
*   **Android 12+ (API 31):** `SCHEDULE_EXACT_ALARM` 권한이 필요할 수 있으며, `PendingIntent`에 `FLAG_IMMUTABLE` 또는 `FLAG_MUTABLE`을 명시해야 합니다.
*   **Doze Mode:** 기기가 절전 모드일 때 알람이 지연될 수 있음을 인지하고, 필요 시 `setAndAllowWhileIdle`을 고려합니다.

## 8. Open Questions
*   현재 `AlarmReceiver`가 `manifest`에 올바르게 등록되어 있는지, `exported` 속성이 적절한지 확인이 필요합니다.
