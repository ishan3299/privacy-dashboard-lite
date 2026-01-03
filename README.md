# Privacy Dashboard Lite

Privacy Dashboard Lite is an Android application designed to help users understand the privacy implications of their installed apps. It scans device applications, classifies them by risk level based on requested permissions, and provides a clear, actionable dashboard.

## Features

*   **Privacy Score:** A dynamic score (0-100) indicating the overall privacy health of your device.
*   **Risk Classification:** Apps are categorized as Critical, High, Medium, or Low risk based on sensitive permissions (e.g., Camera, Microphone, Location).
*   **Detailed Analysis:** View specific permissions and exported components for each app.
*   **Advanced Mode:** Toggle to reveal technical details like package names and component counts.
*   **Sorting & Filtering:** Easily find apps by name, risk level, or search query.
*   **Offline First:** All analysis is performed locally on the device. No data is sent to external servers.

## Play Store Compliance & Privacy Policy

### QUERY_ALL_PACKAGES Permission
This application's core functionality is to analyze and display a dashboard of *all* installed applications to assess privacy risks. Therefore, it requires the `QUERY_ALL_PACKAGES` permission to function. This usage falls under the "Device search" and "Antivirus/Security" permitted use cases for this permission.

### Data Safety
*   **Data Collection:** This app does **not** collect, store, or share any user data.
*   **Local Processing:** All permission references and package analysis happen strictly on the device.
*   **Permissions:**
    *   `QUERY_ALL_PACKAGES`: To list installed apps for analysis.

## Setup & Build

1.  **Prerequisites:** Android SDK 34, JDK 17+.
2.  **Build:** Run `./gradlew assembleDebug` to build the APK.
3.  **Run:** Install on a device using `./gradlew installDebug`.

## Developer
Developed by Ishan.

## License
MIT License.
