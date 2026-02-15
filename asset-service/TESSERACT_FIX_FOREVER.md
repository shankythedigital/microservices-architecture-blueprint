# 🔧 Tesseract Fix - Permanent Solution

## The Problem

Tess4j (Java Tesseract wrapper) uses JNA to load native libraries. On macOS, the library must be:
1. In `java.library.path` (for JVM)
2. In `DYLD_LIBRARY_PATH` (for macOS dynamic linker) - **MUST be set BEFORE JVM starts**
3. Loaded via JNA's `NativeLibrary.getInstance()` (for tess4j)

## The Solution

We've implemented a **multi-layered approach**:

### 1. **Application Startup** (`AssetServiceApplication.java`)
- Sets `java.library.path` before Spring Boot starts
- Adds MacPorts (`/opt/local/lib`) and Homebrew paths
- Sets `TESSDATA_PREFIX` if not already set

### 2. **Service Initialization** (`OcrService.java`)
- Sets `java.library.path` again (in case it wasn't set at startup)
- Resets ClassLoader cache using reflection
- **Loads libraries using JNA's `NativeLibrary.getInstance()`** with absolute paths
- Also tries loading by name to ensure tess4j can find it
- Verifies library is accessible before marking as available

### 3. **Runtime Protection** (`OcrService.java`)
- `ensureLibraryLoaded()` method called before each OCR operation
- Ensures library is in JNA's cache before use

### 4. **Run Script** (`run-with-tesseract.sh`)
- Sets `DYLD_LIBRARY_PATH` **before JVM starts** (critical for macOS)
- Sets `TESSDATA_PREFIX` environment variable
- Passes `java.library.path` via `MAVEN_OPTS`

## How to Use

### ✅ **RECOMMENDED: Use the Run Script**

```bash
cd asset-service
./run-with-tesseract.sh
```

This script:
- ✅ Detects Tesseract installation
- ✅ Finds tessdata directory
- ✅ Sets `DYLD_LIBRARY_PATH` before JVM starts
- ✅ Sets `java.library.path` via JVM arguments
- ✅ Sets `TESSDATA_PREFIX`

### ⚠️ **Alternative: Manual Setup**

If you must run without the script:

```bash
# Set environment variables BEFORE starting JVM
export DYLD_LIBRARY_PATH="/opt/local/lib:$DYLD_LIBRARY_PATH"
export TESSDATA_PREFIX="/opt/local/share/tessdata"

# Set java.library.path via JVM argument
export MAVEN_OPTS="-Djava.library.path=/opt/local/lib -DTESSDATA_PREFIX=/opt/local/share/tessdata"

# Run
mvn spring-boot:run
```

## Verification

After starting, check logs for:

```
✅ Loaded leptonica using JNA: /opt/local/lib/libleptonica.6.dylib
✅ Loaded tesseract using JNA (absolute path): /opt/local/lib/libtesseract.5.dylib
✅ Verified tesseract can be loaded by name via JNA
✅ Tesseract instance created successfully
✅ Tesseract OCR initialized and verified. Data path: /opt/local/share/tessdata
```

## Troubleshooting

### Error: "Unable to load library 'tesseract'"

**Cause:** `DYLD_LIBRARY_PATH` not set before JVM started.

**Fix:**
1. Stop the application
2. Use `./run-with-tesseract.sh` instead of `mvn spring-boot:run`
3. Or manually set `DYLD_LIBRARY_PATH` before running

### Error: "Native library (darwin-aarch64/libtesseract.dylib) not found"

**Cause:** Library not in `java.library.path` or JNA can't find it.

**Fix:**
1. Check logs for "📚 Set java.library.path to: ..."
2. Ensure `/opt/local/lib` (or your Tesseract lib path) is in the path
3. Restart using `./run-with-tesseract.sh`

### Error: "Please make sure the TESSDATA_PREFIX environment variable is set"

**Cause:** Tessdata path not configured.

**Fix:**
1. Check logs for "📁 TESSDATA_PREFIX set to: ..."
2. Verify tessdata directory exists: `ls -la /opt/local/share/tessdata`
3. Set manually: `export TESSDATA_PREFIX="/opt/local/share/tessdata"`

## Why This Works

1. **JNA Loading**: We use `NativeLibrary.getInstance()` with absolute paths, which registers the library in JNA's internal cache. When tess4j tries to load it later, JNA finds it in the cache.

2. **Name-based Loading**: We also try loading by name "tesseract" to ensure tess4j's `NativeLibrary.getInstance("tesseract")` call will succeed.

3. **Runtime Protection**: The `ensureLibraryLoaded()` method ensures the library is loaded before each OCR operation, preventing runtime failures.

4. **Environment Variables**: `DYLD_LIBRARY_PATH` is set before JVM starts, ensuring macOS's dynamic linker can find the libraries.

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│ 1. run-with-tesseract.sh                                 │
│    - Sets DYLD_LIBRARY_PATH (BEFORE JVM starts)          │
│    - Sets TESSDATA_PREFIX                                │
│    - Sets java.library.path via MAVEN_OPTS               │
└─────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│ 2. AssetServiceApplication.main()                        │
│    - setupNativeLibraryPath()                           │
│    - Sets java.library.path                              │
│    - Sets TESSDATA_PREFIX                                │
└─────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│ 3. OcrService.initializeTesseract()                     │
│    - Sets java.library.path (again)                      │
│    - Resets ClassLoader cache                           │
│    - Loads libraries via JNA (absolute paths)            │
│    - Verifies by-name loading works                     │
│    - Creates Tesseract instance                         │
└─────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│ 4. OcrService.extractTextFromImage()                     │
│    - ensureLibraryLoaded() (runtime check)               │
│    - Performs OCR                                        │
└─────────────────────────────────────────────────────────┘
```

## Summary

This solution works because:
- ✅ Libraries are loaded via JNA (what tess4j uses)
- ✅ Libraries are registered in JNA's cache
- ✅ `DYLD_LIBRARY_PATH` is set before JVM starts
- ✅ `java.library.path` is set correctly
- ✅ Runtime protection ensures library is available when needed

**Always use `./run-with-tesseract.sh` to start the application!**

