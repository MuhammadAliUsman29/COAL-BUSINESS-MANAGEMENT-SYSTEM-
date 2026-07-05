# Project Plan

Overhaul the Coal Business Management System (CMS) with onboarding, authentication, profile management, dashboard refinements, search logic, critical reset features, and reporting exports.

## Project Brief

# Coal Business Management System (CMS) Overhaul - Project Brief

## Features
1. **Onboarding & Secure Access**: A mandatory sign-up and login system capturing essential user data (CNIC, Mobile, Profile Pic) with full profile management and password security.
2. **Dynamic KPI Dashboard**: Real-time summary cards for Total Cash and Total Stock with high-contrast Material 3 styling, ensuring critical metrics are visible at a glance.
3. **Inventory Control & Search**: Functional real-time search with strict validation logic (Value >= 0) to prevent negative stock and financial discrepancies.
4. **Administrative Data Resets**: High-security tools for wiping individual customer transaction histories or performing a master system reset with password confirmation.
5. **Comprehensive Reporting**: Localized (Rs., 12-hour AM/PM) PDF and Excel exports for ledgers, daily cash flow, and custom date range summaries.

## High-Level Technical Stack
- **Kotlin**: Primary language for robust and type-safe development.
- **Jetpack Compose**: For a modern, high-contrast Material 3 UI with native Light/Dark mode support and Sidebar navigation.
- **Room Persistence**: Implemented using **KSP** for high-performance local storage of transaction history, customer profiles, and inventory data.
- **Kotlin Coroutines & Flow**: Powering real-time search and reactive UI updates for a fluid user experience.
- **Navigation Compose**: Managing the complex hierarchy between the Dashboard, Sidebar modules, and Profile management screens.

## UI Design Image
![UI Design](file://E:/MyApplication55/input_images/image_0.png)

## Implementation Steps
**Total Duration:** 16m 48s

### Task_1_DataModel_UI_Foundation: Set up the Room database entities (CoalStock, Customer, Transaction) and the Material 3 theme with a vibrant energetic color scheme and edge-to-edge support. Configure Navigation.
- **Status:** COMPLETED
- **Updates:** I have successfully completed the foundation of the Coal Business Management System (CMS). Here is a summary of the actions taken:

### Task_2_Inventory_Dashboard: Implement the Dynamic KPI Dashboard and the Precision Inventory Tracking module. Include Stock In/Out logging with country of origin and moisture levels.
- **Status:** COMPLETED
- **Updates:** I have successfully implemented the **Dynamic KPI Dashboard** and the **Precision Inventory Tracking** module, ensuring the UI strictly follows the provided design.

### Task_3_Ledger_Reporting: Implement the Digital Khata (Customer Ledger) for tracking balances and the Reporting module with PDF/Excel export capabilities.
- **Status:** COMPLETED
- **Updates:** I have successfully implemented the **Digital Khata (Customer Ledger)** and the **Reporting & Analytics** module. This completes the core functionality of the Coal Business Management System (CMS).

### Task_4_Run_Verify: Final polish including adaptive app icon generation and full edge-to-edge display verification. Conduct final run and stability check.
- **Status:** COMPLETED
- **Updates:** I have successfully completed the final polish and verification of the Coal Business Management System (CMS).

### Task_5_Nav_Dashboard_Enhancements: Implement the Sidebar (Navigation Drawer), User Profile dropdown, and Light/Dark mode toggle. Update the KPI Dashboard with real-time summary cards and balance validation.
- **Status:** COMPLETED
- **Updates:** I have successfully addressed all the issues reported by the critic in the Coal Business Management System (CMS).

### Task_6_Transactions_Reporting_Verify: Refine Transaction Management with filters and 'View Details'. Update PDF/Excel exports with Rs. currency and 12-hour AM/PM formatting. Final run and stability verification.
- **Status:** COMPLETED
- **Updates:** I have successfully refined the **Smart Transaction Management** and **Professional Reporting** modules of the Coal Business Management System (CMS).

### Task_7_Auth_Onboarding_Resets: Implement Onboarding and Secure Access (Sign-up/Login with CNIC, Mobile, Profile Pic, Password), Profile Management, and Administrative Data Resets (Individual/Master with password confirmation).
- **Status:** COMPLETED
- **Updates:** I have successfully implemented the **Onboarding, Secure Access, Profile Management, and Administrative Data Resets** for the Coal Business Management System (CMS).

### Key Features Implemented:
1.  **Onboarding & Secure Access**:
    *   **Sign-up Screen**: Captures Full Name, CNIC, Mobile Number, Password, and includes an image picker for the **Profile Picture**.
    *   **Login Screen**: Secure authentication using CNIC and Password.
    *   **Session Management**: Integrated `AuthViewModel` in `MainActivity` to toggle between authentication flow and the main application flow based on login state.

2.  **Profile Management**:
    *   Dedicated **Profile Screen** allowing users to view their CNIC and profile picture and update their info.

3.  **Administrative Data Resets**:
    *   **Master System Reset**: A high-security tool in the Profile screen to wipe all app data.
    *   **Individual Customer Reset**: Ability to wipe transaction history for a specific customer.
    *   **Security**: Both reset actions require **Password Confirmation**.

4.  **Technical Refinements**:
    *   Updated Room Database with a new `User` entity.
    *   Ensured full **Edge-to-Edge** support and a high-contrast Material 3 UI.

The app now provides a complete lifecycle from user registration to secure data management.
- **Acceptance Criteria:**
  - Onboarding captures CNIC, Mobile, and Profile Pic securely
  - Login system with password protection is functional
  - Profile Management screen allows updating user info
  - Individual and Master data reset features require password confirmation
  - The implemented UI must match the design provided in E:/MyApplication55/input_images/image_0.png
- **Duration:** 16m 48s

### Task_8_Search_Dashboard_Reporting_Verify: Implement real-time search in Inventory/Ledger with validation (>= 0), overhaul Dashboard with high-contrast UI, and expand Reporting (Daily cash flow, custom date ranges). Conduct final verification.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - Real-time search filters inventory and transactions with positive value validation
  - Dashboard cards use high-contrast Material 3 styling for readability
  - Exports support Daily Cash Flow and custom Date Range summaries
  - The implemented UI must match the design provided in E:/MyApplication55/input_images/image_0.png
  - Make sure all existing tests pass
  - Build pass
  - App does not crash
- **StartTime:** 2026-04-17 22:54:46 PKT

