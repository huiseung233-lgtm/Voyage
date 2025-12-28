# PRD: Fix Handover to Watch Log Flow

## 1. Introduction
Currently, in `GameActivity`, clicking the "Handover to Watch" button opens a confirmation dialog. Selecting "Yes, I will write it" immediately finishes the activity and returns to the Main Screen, failing to show the intended Daily Log dialog.
This feature aims to fix this flow so that the Daily Log dialog appears as an overlay on the Game Screen. The user completes the log, saves it, and only then does the ship docking process complete. The user remains on the Game Screen with the updated status.

## 2. Goals
*   **Fix Premature Exit:** Prevent `GameActivity` from closing immediately when the user confirms they want to write the log.
*   **Show Dialog:** Display the `CommonLogbookDialog` (same as Main Screen) as an overlay within `GameActivity`.
*   **Seamless Docking:** Ensure saving the log triggers the ship docking process and updates the UI state to "Docked" without leaving the screen.

## 3. User Stories
*   As a Captain, when I decide to hand over the watch (dock), I want to be prompted to write my daily log.
*   When I choose to write the log, I expect a dialog to appear right there on the sea view, not to be kicked out to the main menu.
*   After I fill out and save my log, I want to see my ship successfully docked and remain in the game view to perform other port actions (like visiting the market).

## 4. Functional Requirements

### 4.1. Handover Confirmation
*   **Trigger:** User clicks "Handover to Watch" (당직에게 인계하기) button in `GameScreen`.
*   **Action:** Show "Prepare for Anchoring" (정박준비) confirmation dialog (Existing).

### 4.2. Logbook Dialog Display
*   **Trigger:** User clicks "Yes, I will write it" (예, 작성할게요) in the confirmation dialog.
*   **Action:**
    *   **Do NOT** close the `GameActivity`.
    *   Open the `CommonLogbookDialog` as an overlay on the current screen.
    *   Load the data for the current date (same logic as Main Screen).

### 4.3. Saving and Docking
*   **Trigger:** User clicks "Save" in the `CommonLogbookDialog`.
*   **Action:**
    *   Save the score records to the database.
    *   Execute the `dockShip` logic (update status to `ANCHORED`).
    *   Close the dialog.
    *   Update the Game Screen UI to reflect the "Anchored" status (e.g., change button text to "Sail").
    *   **Stay** on the `GameActivity`.

## 5. Non-Goals
*   Changing the design of the `CommonLogbookDialog`.
*   Adding new fields to the logbook.

## 6. Technical Considerations
*   **`GameActivity.kt`:** Remove the observer for `navigateToLogbook` that triggers `finish()`.
*   **`GameViewModel.kt`:**
    *   Modify `openLogbookForDocking` to primarily trigger the UI state for showing the dialog, rather than a navigation event that closes the activity.
    *   Ensure `saveBatchRecords` handles the docking logic (`repository.dockShip()`) and updates the local state so the UI refreshes automatically.
*   **`GameScreen.kt`:** Ensure the `showLogbookDialog` state is correctly toggled and the dialog is composed on top of the existing content.

## 7. Success Metrics
*   User can successfully open the log dialog from the Handover button.
*   `GameActivity` remains open throughout the process.
*   Ship status updates to `ANCHORED` after saving.
