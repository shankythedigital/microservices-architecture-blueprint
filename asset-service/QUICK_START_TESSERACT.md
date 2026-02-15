# Quick Start: Fix Tesseract Runtime Error

## The Problem
You're seeing: `Tesseract OCR is not installed or not available on this system`

Even though Tesseract is installed, the JVM can't find the native libraries at runtime.

## Quick Fix (3 Steps)

### Step 1: Stop the Current Application
Press `Ctrl+C` in the terminal where the application is running.

### Step 2: Rebuild the Application
```bash
cd asset-service
mvn clean compile
```

### Step 3: Restart Using the Run Script
```bash
./run-with-tesseract.sh
```

**That's it!** The run script sets all necessary environment variables.

## Alternative: Manual Start

If you prefer to run manually:

```bash
export DYLD_LIBRARY_PATH="/opt/local/lib:$DYLD_LIBRARY_PATH"
export TESSDATA_PREFIX="/opt/local/share/tessdata"
mvn spring-boot:run
```

## What Changed?

The code now:
1. ✅ Explicitly loads native libraries using `System.load()` with absolute paths
2. ✅ Tries multiple library paths (MacPorts, Homebrew)
3. ✅ Falls back to JNA if System.load() fails
4. ✅ Provides better error messages

## Verification

After restarting, check the logs for:
- `✅ Loaded leptonica using System.load(): /opt/local/lib/libleptonica.6.dylib`
- `✅ Loaded tesseract using System.load(): /opt/local/lib/libtesseract.5.dylib`
- `✅ Tesseract OCR initialized and verified`

If you see these messages, Tesseract is working! 🎉

## Still Not Working?

1. **Verify Tesseract is installed:**
   ```bash
   tesseract --version
   ```

2. **Check library files exist:**
   ```bash
   ls -la /opt/local/lib/libtesseract* /opt/local/lib/libleptonica*
   ```

3. **Check tessdata exists:**
   ```bash
   ls -la /opt/local/share/tessdata
   ```

4. **Use the run script** - it handles everything automatically:
   ```bash
   ./run-with-tesseract.sh
   ```

