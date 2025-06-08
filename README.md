# Notify - Your Simple Task Scheduler

![App Screenshot Placeholder](link-to-your-app-screenshot.png)

Notify is a simple and intuitive Android application designed to help you schedule and manage your tasks effectively. With a clean and user-friendly interface, you can easily create, view, and organize your scheduled tasks, ensuring you never miss an important deadline or event.

## Features

*   **Intuitive Task Scheduling:** Easily set a title, details, and a specific time for your tasks.
*   **View All Tasks:** A dedicated screen to see all your scheduled tasks in one place.
*   **Upcoming Tasks:** A section on the home screen to highlight tasks scheduled for the near future.
*   **Search Functionality:** Quickly find tasks using the built-in search feature.
*   **Delete Tasks:** Remove tasks you no longer need.
*   **Simple and Clean UI:** Built with Jetpack Compose for a modern and responsive user experience.
*   **Offline Persistence:** Tasks are stored locally on your device using Room Database, so your data is always available.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

*   Android Studio (latest version recommended)
*   Android SDK
*   A physical Android device or an emulator

### Installation

1.  **Clone the repository:** git clone https://github.com/l1kiiiiii/notify.git
2.  **Open the project in Android Studio:**
    *   Open Android Studio.
    *   Select "Open an existing Android Studio project".
    *   Navigate to the cloned `notify` directory and click "Open".

3.  **Sync the project:**
    *   Android Studio will automatically sync the project dependencies. If not, click "Sync Project with Gradle Files" (the elephant icon in the toolbar).

4.  **Build and run the app:**
    *   Connect an Android device to your computer or start an emulator.
    *   Select your target device/emulator from the toolbar.
    *   Click the "Run 'app'" button (the green play icon).

The app should now build and install on your device or emulator.

## Project Structure

The project follows the standard Android architecture recommendations, utilizing Jetpack Compose for the UI, ViewModel for managing UI-related data, and Room Database for local data persistence.

Key directories and files:

*   `app/src/main/java/com/example/notify/`: Contains the main application source code.
    *   `MainActivity.kt`: The main entry point of the application.
    *   `ui/theme/`: Defines the application's theme.
    *   `data/`: Contains data-related classes, including Room entities, DAOs, and the database class.
        *   `Task.kt`: The Room Entity for a task.
        *   `TaskDao.kt`: The Data Access Object for database operations.
        *   `AppDatabase.kt`: The Room Database class.
    *   `ui/viewmodel/`: Contains the ViewModel classes for managing UI data and interacting with the data layer.
        *   `TaskViewModel.kt`: The ViewModel for handling task-related logic.
        *   `TaskViewModelFactory.kt`: Factory for creating the TaskViewModel with dependencies.
    *   `screens/`: Contains composable functions that represent different screens of the application.
        *   `HomeScreen.kt`: The main screen displaying upcoming tasks and navigation options.
        *   `Create.kt`: The screen for creating new tasks.
        *   `AllTasks.kt`: The screen for viewing all scheduled tasks.
        *   `TaskDetailsScreen.kt` (if implemented): The screen for viewing details of a specific task.
    *   `navigation/`: (Optional, if you extract navigation logic) Contains navigation-related code.
    *   `NotifyApp.kt` (or similar) (Optional): A top-level composable that sets up navigation and other global components.
*   `app/src/main/res/`: Contains application resources (layouts, drawables, values, etc.).

## Technologies Used

*   **Kotlin:** The programming language used for development.
*   **Jetpack Compose:** Modern toolkit for building native Android UI.
*   **Android Architecture Components:**
    *   **ViewModel:** Lifecycle-aware UI data management.
    *   **Room Database:** Local data persistence.
    *   **Lifecycle:** Managing activity and fragment lifecycles.
*   **Kotlin Coroutines and Flow:** For asynchronous programming and observing data streams.
*   **Dependency Injection:** (If used, mention the library, e.g., Hilt) For managing dependencies.
*   **Material Design 3:** Implementing the latest Material Design guidelines.

## Contributing

Contributions are welcome! If you find a bug or want to add a new feature, please follow these steps:

1.  Fork the repository.
2.  Create a new branch (`git checkout -b feature/your-feature`).
3.  Make your changes.
4.  Commit your changes (`git commit -am 'Add some feature'`).
5.  Push to the branch (`git push origin feature/your-feature`).
6.  Create a new Pull Request free to contribute.

Please ensure your code follows the project's coding style and includes appropriate tests.
