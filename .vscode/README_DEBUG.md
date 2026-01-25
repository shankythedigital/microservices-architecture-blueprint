# VS Code Debug Configuration for Helpdesk Service

## If you see "ConfigError: The project 'helpdesk-service' is not a valid java project"

Follow these steps to fix it:

### Step 1: Reload VS Code Window
1. Press `Cmd+Shift+P` (Mac) or `Ctrl+Shift+P` (Windows/Linux)
2. Type "Reload Window" and select "Developer: Reload Window"

### Step 2: Clean Java Workspace
1. Press `Cmd+Shift+P` (Mac) or `Ctrl+Shift+P` (Windows/Linux)
2. Type "Java: Clean Java Language Server Workspace"
3. Select it and confirm
4. Wait for the Java extension to re-index projects

### Step 3: Ensure Maven is Installed
Make sure Maven is installed and accessible:
```bash
mvn --version
```

### Step 4: Build the Project
From the workspace root, run:
```bash
mvn clean install -DskipTests
```

This will ensure all modules are built and the Java extension can recognize them.

### Step 5: Verify Project Recognition
1. Open the Java Projects view in VS Code (Java extension icon in sidebar)
2. You should see "helpdesk-service" listed as a project
3. If not, wait a few moments for indexing to complete

## Debugging

Once the project is recognized:
1. Open the Debug panel (`Cmd+Shift+D` / `Ctrl+Shift+D`)
2. Select "HelpdeskServiceApplication" from the dropdown
3. Set breakpoints in your code
4. Press F5 to start debugging

## Alternative: Manual Classpath Configuration

If the automatic detection still doesn't work, you can manually specify the classpath in the launch.json by adding:
```json
"classPaths": [
    "${workspaceFolder}/helpdesk-service/target/classes",
    "${workspaceFolder}/common-service/target/classes"
],
"modulePaths": []
```

However, the automatic detection should work once the Java extension indexes the Maven projects.

