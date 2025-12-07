@echo off
echo Cleaning IntelliJ IDEA debugger files and Gradle cache...
echo.

REM Clean IntelliJ debugger temp files
echo Removing IntelliJ debugger initialization scripts...
for /d %%i in ("%TEMP%\ijJvmDebugger*.gradle") do if exist "%%i" rd /s /q "%%i"
for %%i in ("%TEMP%\ijJvmDebugger*.gradle") do if exist "%%i" del /f /q "%%i"

REM Clean Gradle cache (optional - uncomment if needed)
REM echo Cleaning Gradle cache...
REM rd /s /q "%USERPROFILE%\.gradle\caches" 2>nul
REM rd /s /q "%USERPROFILE%\.gradle\daemon" 2>nul

REM Clean Android Studio caches (optional - uncomment if needed)
REM echo Cleaning Android Studio caches...
REM rd /s /q "%USERPROFILE%\.AndroidStudio*\system\caches" 2>nul
REM rd /s /q "%USERPROFILE%\.AndroidStudio*\system\compile-server" 2>nul

echo.
echo Cleanup completed! Please restart Android Studio.
echo.
pause
