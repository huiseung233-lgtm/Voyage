## Relevant Files

- `app/src/main/java/com/captain/voyage/ui/settings/SettingsViewModel.kt` - 방해 금지 시간 설정 상태 관리 및 저장 로직 추가.
- `app/src/main/java/com/captain/voyage/ui/settings/SettingsScreen.kt` - 방해 금지 시간 토글 및 시간 선택(TimePicker) UI 추가.
- `app/src/main/java/com/captain/voyage/utils/AlarmReceiver.kt` - 방해 금지 시간인지 확인하고 알림 표시 여부를 결정하는 로직 추가.
- `app/src/main/java/com/captain/voyage/utils/TimeManager.kt` - (Optional) 시간 비교 유틸리티 함수 추가 가능성.

### Notes

- `AlarmReceiver`에서는 `SharedPreferences`를 직접 읽어야 합니다. `SettingsViewModel`은 UI 상태 관리용이므로 리시버에서 사용하지 않습니다.
- 시간 비교 시 `LocalTime`을 사용하면 자정(00:00)을 넘어가는 케이스(`start > end`)를 깔끔하게 처리할 수 있습니다.
- 알림을 건너뛰더라도 `scheduleNotification`을 통한 다음 알람 예약은 반드시 수행되어야 끊기지 않습니다.

## Instructions for Completing Tasks

**중요:** 각 작업을 완료할 때마다 이 마크다운 파일의 `- [ ]`를 `- [x]`로 변경하여 진행 상황을 추적하세요.

예시:
- `- [ ] 1.1 파일 읽기` → `- [x] 1.1 파일 읽기` (완료 후)

상위 작업을 완료했을 때뿐만 아니라, **각 하위 작업을 완료할 때마다 파일을 업데이트**해주세요.

## Tasks

- [x] 0.0 기능 브랜치 생성
  - [x] 0.1 새 브랜치 생성 및 체크아웃 (`git checkout -b feature/quiet-hours`)

- [x] 1.0 데이터 레이어 및 ViewModel 구현 (`SettingsViewModel`)
  - [x] 1.1 `SettingsState` 데이터 클래스에 `isQuietHoursEnabled`, `quietStartTime`, `quietEndTime` 필드 추가.
  - [x] 1.2 `SettingsViewModel.loadSettings`에서 `SharedPreferences`로부터 해당 값들을 읽어오는 로직 추가.
  - [x] 1.3 `SettingsViewModel`에 방해 금지 시간 설정을 업데이트하고 저장하는 `updateQuietHours` 함수 구현.

- [x] 2.0 방해 금지 시간 설정 UI 구현 (`SettingsScreen`)
  - [x] 2.1 설정 화면 UI에 "방해 금지 시간 설정" 섹션 추가 (토글 스위치 포함).
  - [x] 2.2 시작 시간 및 종료 시간을 표시하고, 클릭 시 `TimePickerDialog`를 띄우는 UI 컴포넌트(`TimeSettingRow` 재사용 가능) 구현.
  - [x] 2.3 UI 변경 사항이 `SettingsViewModel`을 통해 즉시 저장되고 반영되는지 확인.

- [x] 3.0 알람 수신부 로직 수정 (`AlarmReceiver`)
  - [x] 3.1 `AlarmReceiver.onReceive`에서 `SharedPreferences`로부터 방해 금지 설정 값을 읽어오는 로직 추가.
  - [x] 3.2 현재 시간이 설정된 방해 금지 시간 범위(Start ~ End) 내에 있는지 판별하는 `isInQuietHours` 로직 구현 (자정 통과 케이스 고려).
  - [x] 3.3 방해 금지 시간 내라면 `NotificationHelper.showNotification` 호출을 스킵하고 로그를 남기도록 분기 처리.
  - [x] 3.4 알림 표시 여부와 상관없이 다음 알람 예약(`scheduleNotification`)은 항상 실행되도록 보장.

- [x] 4.0 기능 검증 및 테스트
  - [x] 4.1 앱 실행하여 방해 금지 시간 설정 후 알림 스킵 확인.
  - [x] 4.2 방해 금지 해제 후 알림 정상 작동 확인.
  - [x] 4.3 자정 통과 설정 검증.
