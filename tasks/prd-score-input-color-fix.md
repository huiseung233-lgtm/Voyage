# Product Requirements Document (PRD): Score Input UI Visibility Improvement

## 1. Introduction
The current "Score Input" and related text entry dialogs in the **Ship's Log (Logbook)** and **Ship's Rules** screens suffer from poor contrast. Users report that the text is too light against the background, and the input field background blends in too much with the dialog background.

This feature aims to standardize the visual style of these dialogs to match the **Goals (Custom Goal)** dialog, which the user has identified as having good readability and aesthetics.

## 2. Goals
*   **Improve Readability:** Ensure all text within input fields is clearly visible (High Contrast).
*   **Visual Consistency:** Unify the dialog styling across the app by adopting the "Parchment/Cream" theme used in the Goals section.
*   **Clear Distinction:** Make input fields visually distinct from the dialog background.

## 3. User Stories
*   As a user, when I open the "Direct Record Add" dialog in the Logbook, I want to clearly see the numbers I am typing so that I don't make mistakes.
*   As a user, when I create a new Rule, I want the input fields for Title and Description to be easy to read against the dialog background.

## 4. Functional Requirements

### 4.1. "Direct Record Add" Dialog (`HomeDialogs.kt`)
*   **Dialog Background:** Change the `AlertDialog` container color to `Color(0xFFFFF8E1)` (Light Cream).
*   **Input Fields (Content & Score):**
    *   **Text Color:** Set to `Color.Black` or Dark Brown (`0xFF3E2723`).
    *   **Container/Background:** Set `OutlinedTextField` container color to `Color.White` (or a very light contrast color) to distinguish it from the dialog background.
    *   **Label/Placeholder:** Ensure these are darker and legible (e.g., `Color.Gray` or Light Brown).
    *   **Border:** Ensure the outline is visible.

### 4.2. "Rule Editor" Dialog (`RulesScreen.kt`)
*   **Dialog Background:** Change the `AlertDialog` container color to `Color(0xFFFFF8E1)`.
*   **Input Fields (Title & Description):**
    *   Apply the same high-contrast styling as above (White background, Black/Dark Text).
*   **Score Controls (Reward/Penalty):**
    *   Ensure the numeric text displaying the current score is Dark/Bold (`Color.Black` or `0xFF3E2723`).
    *   Ensure the +/- Buttons have sufficient contrast (Existing Brown buttons are likely okay, but verify against the new Cream background).

## 5. Non-Goals
*   Changing the logical functionality of how scores are calculated.
*   Adding new input fields (e.g., changing Rule Score stepper to a text field is NOT required, unless necessary for layout).
*   Redesigning the entire screen (only the Dialogs are in scope).

## 6. Design Considerations
*   **Reference Style:** `CustomGoalDialog` in `GoalsScreen.kt`.
    *   Background: `Color(0xFFFFF8E1)`
    *   The user prefers this "Parchment" aesthetic.
*   **Colors:**
    *   **Dialog Bg:** `0xFFFFF8E1`
    *   **Input Bg:** `Color.White` (Recommended for max contrast)
    *   **Text:** `Color(0xFF3E2723)` (Dark Brown) or `Color.Black`

## 7. Success Metrics
*   User confirmation that the text is readable.
*   Visual inspection confirms clear separation between Input Field and Dialog Background.
