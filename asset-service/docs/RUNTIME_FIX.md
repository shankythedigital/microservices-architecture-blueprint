# Runtime Fix for Tesseract on macOS

## Problem
Even though Tesseract is installed and the library path is configured, the JVM cannot load the native libraries at runtime on macOS.

## Root Cause
On macOS, `DYLD_LIBRARY_PATH` must be set as an **environment variable BEFORE the JVM starts**. Setting `java.library.path` at runtime doesn't work for native library loading.

## Solution

### Option 1: Use the Run Script (RECOMMENDED)

The `run-with-tesseract.sh` script sets all necessary environment variables:

```bash
cd asset-service
./run-with-tesseract.sh
```

This script:
- Sets `DYLD_LIBRARY_PATH` to include `/opt/local/lib`
- Sets `TESSDATA_PREFIX` to `/opt/local/share/tessdata`
- Sets `java.library.path` via JVM arguments
- Starts the application with proper configuration

### Option 2: Manual Environment Setup

If you need to run manually, set environment variables before starting:

```bash
export DYLD_LIBRARY_PATH="/opt/local/lib:/opt/homebrew/lib:/usr/local/lib:$DYLD_LIBRARY_PATH"
export TESSDATA_PREFIX="/opt/local/share/tessdata"
export MAVEN_OPTS="-Djava.library.path=/opt/local/lib:/opt/homebrew/lib:/usr/local/lib"

cd asset-service
mvn spring-boot:run
```

### Option 3: Run from IDE

If running from IntelliJ IDEA or Eclipse:

1. **Edit Run Configuration**
2. **Add Environment Variables:**
   - `DYLD_LIBRARY_PATH` = `/opt/local/lib:/opt/homebrew/lib:/usr/local/lib`
   - `TESSDATA_PREFIX` = `/opt/local/share/tessdata`
3. **Add VM Options:**
   - `-Djava.library.path=/opt/local/lib:/opt/homebrew/lib:/usr/local/lib`

### Option 4: Create a Launch Script

Create a simple launch script:

```bash
#!/bin/bash
export DYLD_LIBRARY_PATH="/opt/local/lib:$DYLD_LIBRARY_PATH"
export TESSDATA_PREFIX="/opt/local/share/tessdata"
java -Djava.library.path=/opt/local/lib -jar target/asset-service-*.jar
```

## Code Changes Made

The code now includes:
1. **Explicit library loading** using JNA's `NativeLibrary.getInstance()` before creating Tesseract instance
2. **Automatic detection** of library paths (MacPorts, Homebrew)
3. **Better error messages** with diagnostic information

## Verification

After starting the application, check the logs for:
- `✅ Loaded leptonica: /opt/local/lib/libleptonica.6.dylib`
- `✅ Loaded tesseract: /opt/local/lib/libtesseract.5.dylib`
- `✅ Tesseract OCR initialized and verified`

If you see these messages, Tesseract is working correctly!

## Troubleshooting

### Issue: Still getting "Unable to load library 'tesseract'"

**Solution:**
1. Make sure you're using the run script: `./run-with-tesseract.sh`
2. Or manually set `DYLD_LIBRARY_PATH` before starting the JVM
3. Verify libraries exist: `ls -la /opt/local/lib/libtesseract*`

### Issue: Library loads but test fails

**Solution:**
1. Check tessdata path: `ls -la /opt/local/share/tessdata`
2. Verify `eng.traineddata` exists
3. Check `TESSDATA_PREFIX` environment variable

### Issue: Works in terminal but not in IDE

**Solution:**
Configure IDE run configuration with environment variables (see Option 3 above).

