## Relevant Files

- `app/src/main/AndroidManifest.xml` - 알람 및 알림 관련 권한 선언, 리시버 등록 설정 수정.
- `app/src/main/java/com/captain/voyage/utils/NotificationHelper.kt` - 알람 스케줄링(`setRepeating` -> `setExact` 등) 및 알림 채널 생성 로직 수정.
- `app/src/main/java/com/captain/voyage/utils/AlarmReceiver.kt` - 알람 수신 후 처리 로직 및 다음 알람 재예약 로직(필요시) 추가.
- `app/src/main/java/com/captain/voyage/ui/settings/SettingsScreen.kt` - 알림 토글 시 권한 요청 로직 추가.
- `app/src/main/java/com/captain/voyage/ui/main/MainActivity.kt` - 앱 시작 시 알림 권한 체크 및 요청 로직 추가.
- `app/src/main/java/com/captain/voyage/ui/settings/SettingsViewModel.kt` - 권한 상태와 설정값 동기화.

### Notes

- Android 13 이상에서는 `POST_NOTIFICATIONS` 권한을 사용자에게 직접 요청해야 합니다.
- Android 12 이상에서 `SCHEDULE_EXACT_ALARM` 권한은 보통 자동으로 부여되지만, 정책에 따라 주의가 필요합니다.
- `AlarmManager.setRepeating`은 정확하지 않을 수 있으므로, 정확한 간격을 위해 `setExactAndAllowWhileIdle` 사용 후 알람 수신 시 다음 알람을 수동으로 예약하는 방식을 고려해야 합니다.

## Instructions for Completing Tasks

**중요:** 각 작업을 완료할 때마다 이 마크다운 파일의 `- [ ]`를 `- [x]`로 변경하여 진행 상황을 추적하세요.

예시:
- `- [ ] 1.1 파일 읽기` → `- [x] 1.1 파일 읽기` (완료 후)

상위 작업을 완료했을 때뿐만 아니라, **각 하위 작업을 완료할 때마다 파일을 업데이트**해주세요.

## Tasks

- [x] 0.0 버그 수정을 위한 브랜치 생성
  - [x] 0.1 현재 브랜치에서 새 브랜치 생성 및 체크아웃 (`git checkout -b fix/alarm-notification`)

- [x] 1.0 매니페스트(Manifest) 권한 및 설정 수정
  - [x] 1.1 `AndroidManifest.xml`에 `RECEIVE_BOOT_COMPLETED` 권한 추가.
  - [x] 1.2 `AlarmReceiver`의 `intent-filter`에 `BOOT_COMPLETED`가 있는지 확인하고, 필요 시 `exported="true"` (또는 권한에 따른 적절한 설정) 확인.

- [x] 2.0 알람 예약 로직 개선 (`NotificationHelper`)
  - [x] 2.1 `NotificationHelper.kt`의 `calculateNextAlarmTime` 로직을 단순화하여 테스트 용이성 확보 (현재 시간 + 간격).
  - [x] 2.2 `setRepeating` 대신 `setExactAndAllowWhileIdle` (또는 `setExact`)을 사용하여 정확도 향상. (단, 이 경우 리시버에서 다음 알람을 재예약해야 함을 고려)
  - [x] 2.3 `createNotificationChannel` 호출 시점이 앱 시작 시점인지 확인하고 보장.

- [x] 3.0 UI 레벨에서의 런타임 권한 요청 구현
  - [x] 3.1 `MainActivity.kt`의 `onCreate` 또는 적절한 위치에서 `POST_NOTIFICATIONS` 권한을 체크하고 요청하는 로직 추가.
  - [x] 3.2 `SettingsScreen.kt`에서 알림 스위치(Switch) 토글 시, 권한이 없다면 권한 요청 팝업을 띄우도록 수정.
  - [x] 3.3 `SettingsViewModel`에서 알림 설정 저장 전 권한 보유 여부를 확인하는 방어 로직 추가.

- [x] 4.0 `AlarmReceiver` 기능 강화 및 재부팅 처리 로직 보완
  - [x] 4.1 `AlarmReceiver.onReceive`에 로그(`Timber` 또는 `Log`)를 추가하여 수신 여부 확인.
  - [x] 4.2 `BOOT_COMPLETED` 액션 수신 시, 저장된 설정(Preferences)을 읽어 알람을 재등록하는 로직 구현.
  - [x] 4.3 (일회성 알람 사용 시) 알람 수신 후 `NotificationHelper`를 통해 다음 알람을 예약하는 로직 추가.

- [x] 5.0 최종 검증 및 디버깅
  - [x] 5.1 앱 실행 후 알림 간격을 1~2분으로 짧게 설정하여 실제 알림이 오는지 테스트.
  - [x] 5.2 알림 클릭 시 앱 메인 화면으로 이동하는지 확인.
  - [x] 5.3 기기 재부팅 후에도 알림이 계속 오는지 확인.
