# Collapsible Snap Content Scaffold

[![](https://jitpack.io/v/EudyContreras/Snap-Scaffold.svg)](https://jitpack.io/#EudyContreras/Snap-Scaffold)

The Collapsible Snap Content Scaffold is a customizable Jetpack Compose component that provides a flexible and intuitive way to create collapsible snap behavior in scrollable UIs. This scaffold allows you to easily implement collapsible headers, sticky headers, and other dynamic content within your Compose applications.

## Features

- **Collapsible Snap Behavior**: Easily create collapsible snap areas that snap to predefined positions based on user interactions.
- **Flexible Layout**: Customize the layout with top bars, bottom bars, sticky headers, and collapsible content areas to suit your application's design requirements.
- **State Management**: Manage the collapsible snap behavior using a dedicated state holder, allowing for easy integration with other Compose components and state management libraries.

## Getting Started

To integrate the Collapsible Snap Content Scaffold into your Jetpack Compose project, follow these steps:

1. **Add Dependency**: Add the Collapsible Snap Content Scaffold library to your project dependencies. You can do this by including the appropriate dependency declaration in your project's `build.gradle` or `build.gradle.kts` file.

    ```gradle
    implementation "com.eudycontreras:snap-scaffold:1.0.0"
    ```

2. **Initialize Snap Area State**: Create and remember a `SnapScrollAreaState` object using the `rememberSnapScrollAreaState` function. This state object will manage the collapsible snap behavior.

    ```kotlin
    val snapAreaState = rememberSnapScrollAreaState()
    ```

3. **Compose Scaffold**: Use the `CollapsibleSnapContentScaffold` composable function to create a scaffold layout with collapsible snap behavior. Customize the scaffold by providing parameters for top bars, bottom bars, sticky headers, collapsible areas, and content.

    ```kotlin
    CollapsibleSnapContentScaffold(
        snapAreaState = snapAreaState,
        topBar = { /* Top bar content */ },
        bottomBar = { /* Bottom bar content */ },
        stickyHeader = { /* Sticky header content */ },
        collapsibleArea = { /* Collapsible area content */ },
        ....
    )
    ```

4. **Customization**: Customize the appearance and behavior of the scaffold by adjusting parameters such as snap thresholds, animations, and scroll behavior within the provided composable functions.

5. **Run and Test**: Run your application to see the Collapsible Snap Content Scaffold in action. Test the scaffold thoroughly to ensure it behaves as expected and integrates seamlessly with the rest of your application.

## Example

```kotlin
// Initialize Snap Area State
val snapAreaState = rememberSnapScrollAreaState()

// Compose Scaffold
CollapsibleSnapContentScaffold(
    snapAreaState = snapAreaState,
    topBar = { /* Top bar content */ },
    bottomBar = { /* Bottom bar content */ },
    stickyHeader = { /* Sticky header content */ },
    collapsibleArea = { /* Collapsible area content */ },
    ....
)
```
## Contributing
Contributions to the Collapsible Snap Content Scaffold project are welcome! If you encounter any issues, have feature requests, or would like to contribute code improvements, please feel free to open an issue or submit a pull request on the GitHub repository.

Before contributing, please review the Contribution Guidelines for detailed instructions on how to contribute to the project.

## License
The Collapsible Snap Content Scaffold library is open-source software licensed under the Apache License 2.0. Feel free to use, modify, and distribute the library in accordance with the terms of the license.

