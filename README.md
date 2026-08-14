# Rob House Rental 🏠

A professional, modern Android application for house rental and property management built with **Material 3**, **Room Database**, and clean architecture patterns.

## 🚀 Features

- **Property & Unit Management**: Manage multi-floor properties, units, rental amounts, and occupancy status (Occupied, Vacant, Maintenance).
- **Tenant Directory**: Track tenant profiles, contact details, emergency contacts, National ID (NID), and active tenancies.
- **Rent Collection & Utility Bills**: Record monthly rent payments, partial balances, overdue dues, water/electricity/gas bills, and print or export receipts.
- **Financial Reports & Analytics**: View income summaries, property performance metrics, expenses, and pending balances.
- **Reminders & Notifications**: Automatic notifications for upcoming rent due dates and bill payments.
- **Backup & Restore**: Secure local database backup and restore capabilities.
- **Material 3 Design & Dark Mode**: Professional Teal & Slate Blue theme with dynamic light/dark mode support and custom iconography.

## 🛠️ Architecture & Tech Stack

- **Language**: Java 17
- **UI Framework**: Material Design 3 (AndroidX / Material Components)
- **Database**: Room Database with schema migration support
- **Threading**: Single-threaded DB Executor & Fixed I/O Thread Pool (`AppExecutors`)
- **Build System**: Gradle Kotlin DSL (`build.gradle.kts`) with Version Catalogs (`libs.versions.toml`)

## 💻 Building the Project

```bash
# Clone the repository
git clone https://github.com/kauserislam09/Rob-House-Rental.git

# Open in Android Studio or build via CLI
./gradlew assembleDebug
```

License: MIT
