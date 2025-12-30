## Relevant Files

- `app/src/main/java/com/captain/voyage/ui/home/HomeDialogs.kt` - `CommonLogbookDialog` 수정 (Toast 추가, Dialog Wrapper 분리 등).
- `app/src/main/java/com/captain/voyage/ui/popup/PopupViewModel.kt` - `initialRecords` 로딩 및 `saveBatchRecords` 로직 추가.
- `app/src/main/java/com/captain/voyage/ui/popup/PopupActivity.kt` - 기존 `PopupScreen` 제거 및 `CommonLogbookDialog` 연결.
- `app/src/main/java/com/captain/voyage/service/ScoreOverlayService.kt` - 기존 `PopupScreen` 제거 및 `CommonLogbookDialog` 연결.

### Notes

- `CommonLogbookDialog` 내부의 `Dialog` 컴포넌트는 `PopupActivity`와 `Service`에서 불필요하므로(이미 투명 액티비티거나 오버레이), `contentOnly` 파라미터를 추가하여 제어하거나 컴포넌트를 분리해야 합니다.
- Toast 메시지는 `LocalContext.current`를 사용하여 띄웁니다.

## Instructions for Completing Tasks

**중요:** 각 작업을 완료할 때마다 이 마크다운 파일의 `- [ ]`를 `- [x]`로 변경하여 진행 상황을 추적하세요.

예시:
- `- [ ] 1.1 파일 읽기` → `- [x] 1.1 파일 읽기` (완료 후)

상위 작업을 완료했을 때뿐만 아니라, **각 하위 작업을 완료할 때마다 파일을 업데이트**해주세요.

## Tasks

- [ ] 0.0 기능 브랜치 생성
  - [ ] 0.1 새 브랜치 생성 및 체크아웃 (`git checkout -b refactor/score-input-popup`)

- [ ] 1.0 Refactor `HomeDialogs.kt` for Reusability & Add Toast Logic
  - [ ] 1.1 `CommonLogbookDialog` 내부 로직을 분리하여 `LogbookContent` 컴포넌트 생성 (Dialog Wrapper 제거 버전).
  - [ ] 1.2 `LogbookContent`에서 점수 추가/삭제 시 "+10점 추가됨", "-5점 차감됨" 등의 Toast 메시지 출력 로직 추가.
  - [ ] 1.3 기존 `CommonLogbookDialog`는 `Dialog`로 `LogbookContent`를 감싸도록 수정 (기존 코드 호환성 유지).

- [ ] 2.0 Enhance `PopupViewModel` as Data Provider
  - [ ] 2.1 `PopupViewModel`에 `loadTodayRecords` 함수 추가 (DB에서 오늘 기록 조회 후 `StateFlow` 업데이트).
  - [ ] 2.2 `PopupViewModel`에 `saveBatchRecords` 함수 구현 (리스트 받아 DB 저장 및 `DailyLog` 갱신).
  - [ ] 2.3 `PopupViewModel`에서 `uiState` (loading, records, rules) 관리하도록 수정.

- [ ] 3.0 Update `PopupActivity` to use `CommonLogbookDialog` (Content)
  - [ ] 3.1 `PopupActivity`에서 `PopupScreen` 제거하고 `LogbookContent` 호출.
  - [ ] 3.2 `viewModel` 데이터를 `LogbookContent`에 연결 (로딩 중일 땐 인디케이터 표시).
  - [ ] 3.3 저장 완료(`onSave`) 시 `finish()` 호출.

- [x] 4.0 Update `ScoreOverlayService` to use `CommonLogbookDialog` (Content)
  - [x] 4.1 `ScoreOverlayService`에서도 `PopupScreen` 대신 `LogbookContent` 호출.
  - [x] 4.2 배경(`Box` 등)을 추가하여 중앙 정렬 및 닫기 처리.
  - [x] 4.3 저장 완료 시 `stopSelf()` 호출.

- [ ] 5.0 Verification
  - [ ] 5.1 홈 화면 달력 클릭 시 기존 다이얼로그 정상 작동 확인 (+Toast 확인).
  - [ ] 5.2 알림 클릭 시 `PopupActivity`에서 동일한 UI 표시 확인.
  - [ ] 5.3 점수 저장 및 취소 동작 검증.
