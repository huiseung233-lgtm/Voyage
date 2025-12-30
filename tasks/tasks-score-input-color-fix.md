## Relevant Files

- `app/src/main/java/com/captain/voyage/ui/home/HomeDialogs.kt` - Contains `AddRecordDialog` (Logbook score input).
- `app/src/main/java/com/captain/voyage/ui/rules/RulesScreen.kt` - Contains `RuleEditorDialog` (Ship's Rules editor).
- `app/src/main/java/com/captain/voyage/ui/goals/GoalsScreen.kt` - Reference for the "Parchment" UI style.
- `app/src/main/java/com/captain/voyage/ui/theme/Color.kt` - Project color definitions.

### Notes

- 이 작업은 UI/UX 개선 작업으로, 로직 변경은 최소화하고 시각적 대비 확보에 집중합니다.
- `OutlinedTextFieldDefaults.colors()`를 사용하여 입력창의 배경색과 텍스트 색상을 명시적으로 제어합니다.
- 목표 탭(`GoalsScreen.kt`)의 스타일을 벤치마킹하여 일관성을 유지합니다.

## Instructions for Completing Tasks

**IMPORTANT:** 각 하위 작업을 완료할 때마다 `- [ ]`를 `- [x]`로 변경하여 진행 상황을 추적하세요. 모든 변경 사항은 사용자의 허락을 받은 후 적용해야 합니다.

## Tasks

- [x] 0.0 Create feature branch
  - [x] 0.1 새 브랜치 생성 및 체크아웃 (`git checkout -b feature/score-input-visibility`)
- [x] 0.5 Check and Define Theme Colors
  - [x] 0.5.1 `Color.kt` 및 `Theme.kt`에서 사용 가능한 공통 색상 확인
  - [x] 0.5.2 `GoalsScreen.kt`의 `CustomGoalDialog`에서 사용하는 색상 값 및 스타일 참조 확인
- [x] 1.0 Improve "Direct Record Add" Dialog UI (Logbook)
  - [x] 1.1 `HomeDialogs.kt`의 `AddRecordDialog` 코드 분석
  - [x] 1.2 `AlertDialog`의 `containerColor`를 `0xFFFFF8E1`로 변경
  - [x] 1.3 `OutlinedTextField`에 `OutlinedTextFieldDefaults.colors()` 적용 (배경: White, 텍스트: Dark Brown)
  - [x] 1.4 버튼 및 레이블 색상 가독성 최적화
- [x] 2.0 Improve "Rule Editor" Dialog UI (Rules)
  - [x] 2.1 `RulesScreen.kt`의 `RuleEditorDialog` 코드 분석
  - [x] 2.2 `AlertDialog`의 `containerColor`를 `0xFFFFF8E1`로 변경
  - [x] 2.3 입력 필드(제목, 설명)에 고대비 스타일 적용
  - [x] 2.4 점수 조절기(Stepper)의 숫자 및 버튼 대비 개선
- [x] 3.0 Verify UI Changes
  - [x] 3.1 로그북(Logbook) 다이얼로그 가독성 확인
  - [x] 3.2 규칙(Rules) 다이얼로그 가독성 확인
  - [x] 3.3 목표(Goals) 화면과의 시각적 일관성 최종 확인
- [x] 4.0 Optimize Code and Resources
  - [x] 4.1 중복되는 다이얼로그 스타일 코드 최적화 (필요 시 공통 컴포넌트화 고려)
  - [x] 4.2 불필요한 리소스나 코드 정리
- [x] 5.0 Final Review and Cleanup
  - [x] 5.1 PRD 요구사항 충족 여부 최종 검토
  - [x] 5.2 코드 리뷰 및 클린업
  - [x] 5.3 변경 사항 커밋 준비