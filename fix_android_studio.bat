@echo off
echo Fixing Android Studio Layout Preview Issues...
echo.

REM Stop Android Studio if running
echo Please close Android Studio completely before running this script.
timeout /t 3 >nul

REM Clean Android Studio caches
echo Cleaning Android Studio caches...
if exist "%USERPROFILE%\.AndroidStudio*\system\caches" (
    rd /s /q "%USERPROFILE%\.AndroidStudio*\system\caches" 2>nul
    echo Android Studio caches cleaned.
) else (
    echo No Android Studio caches found.
)

REM Clean Android Studio compile server
if exist "%USERPROFILE%\.AndroidStudio*\system\compile-server" (
    rd /s /q "%USERPROFILE%\.AndroidStudio*\system\compile-server" 2>nul
    echo Compile server cache cleaned.
)

REM Clean Gradle caches (optional)
echo Cleaning Gradle caches...
if exist "%USERPROFILE%\.gradle\caches" (
    rd /s /q "%USERPROFILE%\.gradle\caches" 2>nul
    echo Gradle caches cleaned.
)

REM Clean project-specific caches
echo Cleaning project caches...
if exist ".gradle" rd /s /q ".gradle" 2>nul
if exist "build" rd /s /q "build" 2>nul
if exist "app\build" rd /s /q "app\build" 2>nul

echo.
echo Cache cleanup completed!
echo.
echo Next steps:
echo 1. Restart Android Studio
echo 2. Invalidate caches: File -> Invalidate Caches / Restart
echo 3. Wait for Gradle sync to complete
echo 4. Try opening a layout file to test the preview
echo.
pause
