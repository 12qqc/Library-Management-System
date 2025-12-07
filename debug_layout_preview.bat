@echo off
echo Android Studio Layout Preview Debug Information
echo ===============================================
echo.

echo 1. Android Studio Version and Location:
echo.
reg query "HKLM\SOFTWARE\Android Studio" /v "InstallPath" 2>nul
if %errorlevel% neq 0 (
    echo Android Studio not found in registry, checking common locations...
    if exist "C:\Program Files\Android\Android Studio" echo Found: C:\Program Files\Android\Android Studio
    if exist "C:\Program Files (x86)\Android\android-studio" echo Found: C:\Program Files (x86)\Android\android-studio
    for /d %%i in ("%LOCALAPPDATA%\Programs\Android Studio*") do echo Found: %%i
)

echo.
echo 2. JDK/JAVA_HOME Information:
echo JAVA_HOME: %JAVA_HOME%
echo.

echo 3. Android SDK Location:
echo Checking for Android SDK...
if defined ANDROID_HOME echo ANDROID_HOME: %ANDROID_HOME%
if defined ANDROID_SDK_ROOT echo ANDROID_SDK_ROOT: %ANDROID_SDK_ROOT%

echo.
echo 4. Gradle Version:
echo.
if exist "gradlew.bat" (
    call gradlew.bat --version 2>nul | findstr "Gradle"
) else (
    echo gradlew.bat not found
)

echo.
echo 5. Project Structure Check:
echo.
if exist "app\src\main\AndroidManifest.xml" echo ✓ AndroidManifest.xml found
if exist "app\src\main\res\values\themes.xml" echo ✓ themes.xml found
if exist "app\src\main\res\values\colors.xml" echo ✓ colors.xml found
if exist "app\build.gradle.kts" echo ✓ build.gradle.kts found

echo.
echo 6. Common Issues to Check:
echo - Ensure Android SDK is properly installed
echo - Check that target SDK version is installed
echo - Verify JDK version compatibility
echo - Make sure Android Studio is updated
echo - Try invalidating caches: File -> Invalidate Caches / Restart

echo.
echo 7. Quick Fix Commands:
echo.
echo To clean and rebuild:
echo   .\gradlew clean
echo   .\gradlew build
echo.
echo To reset Android Studio:
echo   Run: fix_android_studio.bat
echo.
pause
