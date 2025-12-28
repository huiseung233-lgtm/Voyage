# Task List: Fix Handover Log Flow

## Relevant Files

- `app/src/main/java/com/captain/voyage/ui/game/GameActivity.kt` - Main entry point for the game screen. Needs modification to prevent closing on log request.
- `app/src/main/java/com/captain/voyage/ui/game/GameViewModel.kt` - Handles state for docking and logging. Needs logic updates to support in-place state changes.
- `app/src/main/java/com/captain/voyage/ui/game/GameScreen.kt` - The composable UI. Needs to ensure the dialog is shown correctly over the game view.

### Notes

- The goal is to keep the user in the `GameActivity` throughout the "Handover -> Write Log -> Dock" process.
- Ensure that after docking, the UI immediately reflects the new status (e.g., ship stops moving, buttons change) without a full screen refresh.

## Instructions for Completing Tasks

**IMPORTANT:** As you complete each task, you must check it off in this markdown file by changing `- [ ]` to `- [x]`. This helps track progress and ensures you don't skip any steps.

## Tasks

- [x] 0.0 Create feature branch
  - [x] 0.1 Create and checkout a new branch for this feature (e.g., `git checkout -b fix/handover-log-flow`)

- [x] 1.0 Modify GameActivity
  - [x] 1.1 Remove the `viewModel.navigateToLogbook.observe` block in `GameActivity.kt`. This block currently finishes the activity, which is the root cause of the issue.

- [x] 2.0 Update GameViewModel
  - [x] 2.1 Refactor `openLogbookForDocking` in `GameViewModel.kt`. It should only set a state (e.g., `_showLogbookDialog`) that `GameScreen` observes, rather than sending a "navigate" signal to the Activity.
  - [x] 2.2 Verify `saveBatchRecords` in `GameViewModel.kt`. Ensure that when `repository.dockShip()` is called, the local `ship` LiveData/Flow updates automatically so the UI reflects the "ANCHORED" status immediately.
  - [x] 2.3 Remove `_navigateToLogbook` LiveData if it's no longer used.

- [x] 3.0 Update GameScreen
  - [x] 3.1 In `GameScreen.kt`, ensure `showLogbookDialog` state is controlled by the ViewModel (or the existing local state logic is sufficient if aligned with ViewModel changes).
  - [x] 3.2 Verify that the "Save" callback in `CommonLogbookDialog` (inside `GameScreen`) calls `viewModel.saveBatchRecords` and then simply closes the dialog (sets `showLogbookDialog = false`), keeping the user on the screen.

- [x] 4.0 Verify & Test
  - [x] 4.1 Launch the app and go to `GameActivity`.
  - [x] 4.2 Start sailing (if not already).
  - [x] 4.3 Click "Handover to Watch" (당직에게 인계하기).
  - [x] 4.4 Click "Yes, I will write it" (예, 작성할게요).
  - [x] 4.5 **Verify:** The app does NOT close. The Logbook dialog appears over the sea view.
  - [x] 4.6 Write a test log and click "Save".
  - [x] 4.7 **Verify:** The dialog closes. The ship status changes to "Anchored" (buttons change, ship stops). The user remains on the Game Screen.
