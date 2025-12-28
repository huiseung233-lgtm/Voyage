# PRD: Fix Persistent Light Grey Status Bar

## 1. Introduction/Overview
The user reports that the system status bar appears as a "Light Grey" color across the entire application, which clashes with the app's immersive "Voyage" theme. Previous attempts to make it transparent or use a scrim have not resolved the core issue of the base color being incorrect.
This task involves investigating the root cause (likely in `themes.xml` or programmatic overrides) and enforcing a solid, theme-appropriate color (e.g., Dark Wood/Brown) globally.

## 2. Goals
*   **Eliminate Light Grey:** Remove the default light grey status bar color.
*   **Enforce Solid Color:** Set the status bar background to a solid color that matches the app's theme (e.g., `VoyageWoodMedium` or similar Dark Brown).
*   **Icon Visibility:** Ensure system icons (time, battery) are visible against this new solid background (likely requiring "Light" icons).

## 3. User Stories
*   As a user, I want the top of the screen (status bar) to blend in with the app's wooden/parchment theme, rather than looking like a standard gray Android bar.
*   As a user, I need to clearly read the time and battery level at all times.

## 4. Functional Requirements

### 4.1. Global Status Bar Color
*   **Color:** The status bar must be a solid, opaque color defined in the app's palette (e.g., `#3E2723` or similar).
*   **Scope:** Applied to `MainActivity`, `GameActivity`, `MapActivity`, and any other entry points.
*   **Method:**
    *   Primary: Defined in `themes.xml` (Light and Night variations).
    *   Secondary: Enforced programmatically in `onCreate` or via `SystemUiController` side effects in Compose to override any dynamic theming.

### 4.2. Icon Contrast
*   **Behavior:** With a dark solid background, system icons must be forced to **White (Light)**.
*   **Constraint:** `windowLightStatusBar` must be set to `false`.

## 5. Non-Goals
*   Making the status bar transparent or "edge-to-edge" with an image underlay (User explicitly selected "Solid Color" 1B).
*   Per-screen dynamic coloring (User selected "All Screens" 3A, though icon color can be dynamic if needed (2C), solid background implies fixed icon color is safer).

## 6. Technical Considerations
*   **`themes.xml`:** Check for `<item name="android:statusBarColor">` and `<item name="android:windowLightStatusBar">`.
*   **`EdgeToEdge`:** Check if `WindowCompat.setDecorFitsSystemWindows(window, false)` is being called. If so, the content draws *behind* the status bar. If we want a solid color, we might actually want `setDecorFitsSystemWindows(window, true)` OR we must manually place a colored Box of status bar height behind it.
*   **Material3:** Check if `ColorScheme.surface` or similar tokens are bleeding into the status bar color.

## 7. Success Metrics
*   Status bar is NO LONGER light grey on any screen.
*   Status bar is a solid dark color.
*   Icons are white and legible.
