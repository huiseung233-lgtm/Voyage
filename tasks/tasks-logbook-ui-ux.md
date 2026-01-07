# Tasks: Logbook UI/UX Improvement

## Relevant Files

- `app/src/main/java/com/captain/voyage/ui/home/HomeDialogs.kt` - Main file containing `CommonLogbookDialog`, `LogbookContent`, and `LogRecordItem` composables.
- `app/src/main/java/com/captain/voyage/data/model/ScoreRecord.kt` - Data model containing the `timestamp` field (reference only, no changes expected).

### Notes

- This refactoring focuses on Jetpack Compose UI changes within `HomeDialogs.kt`.
- Use `./gradlew assembleDebug` to ensure the project builds correctly after changes.

## Instructions for Completing Tasks

**IMPORTANT:** As you complete each task, you must check it off in this markdown file by changing `- [ ]` to `- [x]`. This helps track progress and ensures you don't skip any steps.

Example:
- `- [ ] 1.1 Read file` → `- [x] 1.1 Read file` (after completing)

Update the file after completing each sub-task, not just after completing an entire parent task.

## Tasks

- [x] 0.0 Create feature branch
  - [x] 0.1 Create and checkout a new branch for this feature (e.g., `git checkout -b feature/logbook-ui-ux`)

- [x] 1.0 Implement Timestamp Display
  - [x] 1.1 In `HomeDialogs.kt`, locate the `LogRecordItem` composable.
  - [x] 1.2 Add logic to format the `record.timestamp` (Long) into a readable time string (e.g., "HH:mm"). Use `java.time.format.DateTimeFormatter` (ensure API level compatibility or use `minSdk` check if needed, project is minSdk 26 so `java.time` is safe).
  - [x] 1.3 Update the `LogRecordItem` UI to display this formatted time string next to the score or rule title. Use a smaller font size and a secondary color (e.g., `Color.Gray`) to distinguish it from the main content.

- [x] 2.0 Refactor Dialog Layout & Dimensions
  - [x] 2.1 In `HomeDialogs.kt`, locate the `CommonLogbookDialog` composable (or the wrapper around `LogbookContent`).
  - [x] 2.2 (Rolled Back) Keep height at `700.dp` to maintain consistency with other popups as per user request.
  - [x] 2.3 Inside `LogbookContent`, locate the `LazyColumn` displaying the "Recorded History" (temp records).
  - [x] 2.4 Change its size modifier. Instead of `weight(1f)` (which splits space evenly with the rule list), set it to a fixed height (e.g., `height(150.dp)`) or a smaller weight (e.g., `weight(0.3f)`) to prioritize the rule list.
  - [x] 2.5 Locate the `LazyColumn` displaying the "Rule Selection" list. Ensure it has `weight(1f)` (or the remaining larger weight) so it expands to fill the available space.

- [x] 3.0 UI Verification & Polish
  - [x] 3.1 Build the app (`./gradlew assembleDebug`) to ensure no compilation errors.
  - [ ] 3.2 (Manual Step) Run the app and open the Logbook dialog. Verify that the dialog is smaller and the rule list area is larger than the history area.
  - [ ] 3.3 (Manual Step) Add a few records and verify that the time is displayed correctly (e.g., "14:30") for each entry.