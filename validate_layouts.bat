@echo off
echo Validating all layout files...
echo ===============================

echo.
echo Checking for basic XML structure...
for %%f in ("app\src\main\res\layout\*.xml") do (
    echo Checking %%~nf.xml
    findstr /c:"<?xml" "%%f" >nul 2>&1
    if errorlevel 1 echo   - Missing XML declaration in %%~nf.xml

    findstr /c:"xmlns:android" "%%f" >nul 2>&1
    if errorlevel 1 echo   - Missing Android namespace in %%~nf.xml
)

echo.
echo Checking for required layout attributes...
for %%f in ("app\src\main\res\layout\*.xml") do (
    echo Checking %%~nf.xml for layout attributes...

    REM Check for missing layout_width/layout_height in non-self-closing tags
    powershell -Command "& { $content = Get-Content '%%f' -Raw; $missingWidth = [regex]::Matches($content, '<(\w+)[^>]*(?!layout_width)[^>]*>'); $missingHeight = [regex]::Matches($content, '<(\w+)[^>]*(?!layout_height)[^>]*>'); if ($missingWidth.Count -gt 0 -or $missingHeight.Count -gt 0) { Write-Host '   - Some elements may be missing layout attributes' } }"
)

echo.
echo Validation complete!
echo If you see any warnings above, check the specific layout files.
pause
