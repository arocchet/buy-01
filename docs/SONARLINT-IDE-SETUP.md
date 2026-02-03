# SonarLint IDE Integration Guide

This guide explains how to set up SonarLint in your IDE for real-time code quality feedback during development.

## What is SonarLint?

SonarLint is an IDE extension that provides real-time feedback on code quality and security issues as you write code. It connects to your SonarQube server to use the same rules and quality profiles.

## Installation

### IntelliJ IDEA / WebStorm

1. **Install Plugin:**
   - Go to `Settings/Preferences` > `Plugins`
   - Search for "SonarLint"
   - Click `Install` and restart the IDE

2. **Connect to SonarQube:**
   - Go to `Settings/Preferences` > `Tools` > `SonarLint`
   - Click `+` to add a new connection
   - Select `SonarQube`
   - Enter connection details:
     - **Name:** `Buy01 SonarQube`
     - **Server URL:** `http://localhost:9000`
   - Click `Create Token` or enter your token manually
   - Token: Use the token from `.sonarqube-token` file

3. **Bind Project:**
   - Go to `Settings/Preferences` > `Tools` > `SonarLint` > `Project Settings`
   - Check `Bind project to SonarQube / SonarCloud`
   - Select your connection
   - Search for project: `buy01-ecommerce`
   - Click `OK`

### Visual Studio Code

1. **Install Extension:**
   - Open Extensions view (`Ctrl+Shift+X` / `Cmd+Shift+X`)
   - Search for "SonarLint"
   - Install "SonarLint" by SonarSource

2. **Configure Connection:**
   - Open VS Code settings (`Ctrl+,` / `Cmd+,`)
   - Search for "sonarlint"
   - Click "Edit in settings.json"
   - Add the following configuration:

```json
{
  "sonarlint.connectedMode.connections.sonarqube": [
    {
      "serverUrl": "http://localhost:9000",
      "token": "<your-token-here>"
    }
  ],
  "sonarlint.connectedMode.project": {
    "connectionId": "http://localhost:9000",
    "projectKey": "buy01-ecommerce"
  }
}
```

3. **Reload VS Code** to apply settings

### Eclipse

1. **Install Plugin:**
   - Go to `Help` > `Eclipse Marketplace`
   - Search for "SonarLint"
   - Click `Install`

2. **Configure Connection:**
   - Go to `Window` > `Preferences` > `SonarLint` > `Connected Mode`
   - Click `Add...`
   - Enter SonarQube URL: `http://localhost:9000`
   - Generate or enter token
   - Click `Finish`

3. **Bind Project:**
   - Right-click on project > `SonarLint` > `Bind to SonarQube or SonarCloud`
   - Select your connection
   - Choose `buy01-ecommerce`

## Features

Once configured, SonarLint provides:

- **Real-time Analysis:** Issues are highlighted as you type
- **Quick Fixes:** Some issues can be automatically fixed
- **Rule Descriptions:** Hover over issues to see explanations
- **Synchronized Rules:** Uses the same rules as your SonarQube server
- **Security Hotspots:** Identifies potential security vulnerabilities

## Verification

To verify SonarLint is working:

1. Open any Java file in the project
2. Introduce a code smell (e.g., unused variable)
3. SonarLint should highlight it within seconds
4. Hover over the highlight to see the issue description

## Troubleshooting

### Connection Issues
- Verify SonarQube is running: `http://localhost:9000`
- Check token is valid
- Ensure firewall allows connection

### Rules Not Syncing
- Click "Update binding" in SonarLint settings
- Restart IDE

### Analysis Not Running
- Check SonarLint output console for errors
- Verify project is correctly bound

## Benefits for Development

1. **Catch Issues Early:** Find problems before commit
2. **Learn Best Practices:** Rule explanations teach coding standards
3. **Consistent Quality:** Same rules as CI/CD pipeline
4. **Security Focus:** Immediate feedback on security issues
5. **Time Savings:** No waiting for CI/CD analysis results
