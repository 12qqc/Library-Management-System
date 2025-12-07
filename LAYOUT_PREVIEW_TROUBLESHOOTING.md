# Android Studio Layout Preview Troubleshooting Guide

## Common Render Problems and Solutions

### Problem: "Failed to instantiate one or more classes"
**Solutions:**
1. **Theme Issues**: Ensure your theme extends a proper Material theme
2. **Missing Dependencies**: Check that all Material Design libraries are included
3. **API Level**: Make sure target SDK is installed in Android SDK

### Problem: "NoSuchFileException: framework_res.jar"
**Solutions:**
1. Run `fix_android_studio.bat` to clean caches
2. Invalidate caches: File → Invalidate Caches / Restart
3. Check Android SDK installation

### Problem: "Render problem - Couldn't resolve resource"
**Solutions:**
1. Check that all referenced resources exist in the correct directories
2. Verify resource names match exactly (case-sensitive)
3. Ensure colors, themes, and styles are properly defined

### Problem: "Material Design components not rendering"
**Solutions:**
1. Update `themes.xml` to use proper Material 3 theme
2. Add `tools:context` to layout files
3. Add `tools:style` attributes for design-time rendering

## Quick Fix Steps

### 1. Clean Everything
```batch
# Run the provided script
fix_android_studio.bat

# Or manually:
# Delete %USERPROFILE%\.AndroidStudio*\system\caches\
# Delete %USERPROFILE%\.gradle\caches\
# Delete project .gradle and build folders
```

### 2. Invalidate Caches
- File → Invalidate Caches / Restart
- Select "Invalidate and Restart"
- Wait for complete sync

### 3. Check Dependencies
- Ensure `build.gradle.kts` has all required dependencies
- Verify Material Design library version compatibility
- Check that target/compile SDK versions are correct

### 4. Theme Configuration
- Use `Theme.Material3.Light.NoActionBar` for stability
- Ensure all required color attributes are defined
- Add proper text input styles

### 5. Layout Attributes
- Add `tools:context` to specify the owning activity/fragment
- Add `tools:style` for Material Design components
- Add `tools:text` for preview content

## Debug Information

Run `debug_layout_preview.bat` to get diagnostic information about:
- Android Studio installation
- JDK/JAVA_HOME settings
- Android SDK location
- Gradle version
- Project structure validation

## Test Layout

Use `test_layout.xml` to verify basic rendering works. If this renders correctly, the issue is with specific components in your main layouts.

## Emergency Fixes

If all else fails:

1. **Create a new project** and compare configurations
2. **Reinstall Android Studio** (last resort)
3. **Update Android SDK** to latest stable version
4. **Use Android Studio Canary** for latest fixes

## Prevention

- Keep Android Studio updated
- Regularly invalidate caches
- Use stable Material Design versions
- Test layout previews frequently during development
