## Relevant Files

- `app/src/main/AndroidManifest.xml` - 오버레이 권한 선언, 서비스 및 액티비티 등록.
- `app/src/main/java/com/captain/voyage/ui/settings/SettingsScreen.kt` - 오버레이 권한 요청 토글 UI 추가.
- `app/src/main/java/com/captain/voyage/ui/popup/PopupActivity.kt` - 투명 배경 액티비티 (알림 클릭 시 실행).
- `app/src/main/java/com/captain/voyage/service/ScoreOverlayService.kt` - 윈도우 매니저를 이용한 오버레이 뷰 관리 서비스.
- `app/src/main/java/com/captain/voyage/utils/AlarmReceiver.kt` - 권한/설정 확인 후 서비스 시작 또는 알림 발송 분기 처리.
- `app/src/main/java/com/captain/voyage/utils/NotificationHelper.kt` - 알림 인텐트를 PopupActivity로 연결.

### Notes

- 오버레이 뷰는 Compose를 `ComposeView`로 래핑하여 `WindowManager`에 추가할 수 있습니다.
- `PopupActivity`는 `Theme.AppCompat.Translucent.NoTitleBar` 또는 투명 스타일을 적용해야 합니다.
- Android 10+에서는 백그라운드에서 액티비티 실행이 차단되므로, 오버레이가 불가능할 때는 반드시 알림(Notification)을 통해야 합니다.

## Instructions for Completing Tasks

**중요:** 각 작업을 완료할 때마다 이 마크다운 파일의 `- [ ]`를 `- [x]`로 변경하여 진행 상황을 추적하세요.

예시:
- `- [ ] 1.1 파일 읽기` → `- [x] 1.1 파일 읽기` (완료 후)

상위 작업을 완료했을 때뿐만 아니라, **각 하위 작업을 완료할 때마다 파일을 업데이트**해주세요.

## Tasks

- [x] 0.0 기능 브랜치 생성
  - [x] 0.1 새 브랜치 생성 및 체크아웃 (`git checkout -b feature/hybrid-popup`)

- [x] 1.0 오버레이 권한 설정 구현 (`SettingsScreen` & `ViewModel`)
  - [x] 1.1 `AndroidManifest.xml`에 `SYSTEM_ALERT_WINDOW` 권한 추가.
  - [x] 1.2 `SettingsViewModel`에 오버레이 사용 여부(`pref_use_overlay`) 저장 로직 추가.
  - [x] 1.3 `SettingsScreen`에 "팝업으로 바로 띄우기" 토글 추가.
  - [x] 1.4 토글 ON 시 `Settings.actionManageOverlayPermission` 인텐트로 시스템 권한 설정 화면 이동 로직 구현.

- [x] 2.0 투명 액티비티 구현 (`PopupActivity`)
  - [x] 2.1 `AndroidManifest.xml`에 `PopupActivity` 등록 및 투명 테마(`Theme.Transparent`) 적용.
  - [x] 2.2 `PopupActivity.kt` 생성 및 다이얼로그 스타일의 Compose UI 구현 (점수 입력창).
  - [x] 2.3 입력 완료 또는 취소 시 `finish()` 호출하여 이전 화면 복귀 처리.

- [x] 3.0 오버레이 서비스 구현 (`ScoreOverlayService`)
  - [x] 3.1 `ScoreOverlayService.kt` (Service 상속) 생성 및 Manifest 등록.
  - [x] 3.2 `WindowManager`를 사용하여 화면 중앙에 ComposeView(`ScoreInputPopup`)를 추가하는 로직 구현.
  - [x] 3.3 뷰 내부의 닫기/완료 버튼 동작 시 뷰를 제거(`removeView`)하고 서비스 종료(`stopSelf`) 처리.
  - [x] 3.4 `FLAG_NOT_TOUCH_MODAL` 등을 사용하여 배경 터치 시 뒤쪽 앱(유튜브 등)이 동작하도록 설정.

- [ ] 4.0 알림 및 알람 로직 연결
  - [ ] 4.1 `NotificationHelper` 수정: 알림 클릭 시 `MainActivity` 대신 `PopupActivity` 실행하도록 PendingIntent 변경.
  - [ ] 4.2 `AlarmReceiver` 수정: `pref_use_overlay`가 켜져 있고 권한이 있다면 `ScoreOverlayService` 시작.
  - [ ] 4.3 `AlarmReceiver`: 권한이 없거나 설정이 꺼져 있다면 기존대로 `NotificationHelper.showNotification` 호출.

- [ ] 5.0 검증 및 마무리
  - [ ] 5.1 설정에서 오버레이 권한 허용 후 알람 시 팝업 자동 노출 테스트.
  - [ ] 5.2 권한 거부 후 알람 시 상단 알림 -> 클릭 -> 투명 팝업 노출 테스트.
  - [ ] 5.3 입력 후 원래 앱(유튜브 등)으로 정상 복귀 확인.
