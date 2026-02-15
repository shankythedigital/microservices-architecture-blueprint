# 🔧 Tesseract OCR - Critical Setup Instructions

## ⚠️ IMPORTANT: macOS Requirement

**On macOS, you MUST use the startup script to run the application.**

The Tesseract native library requires `DYLD_LIBRARY_PATH` to be set **BEFORE** the JVM starts. This cannot be done from Java code.

## ✅ Quick Start

### Use the Startup Script (Recommended)

```bash
cd asset-service
./start.sh
```

This script:
- ✅ Sets `DYLD_LIBRARY_PATH` before JVM starts
- ✅ Sets `TESSDATA_PREFIX` environment variable
- ✅ Configures `java.library.path` via JVM arguments
- ✅ Verifies Tesseract installation

### Alternative: Manual Setup

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

## ❌ Common Error

If you see this error:
```
Unable to load library 'tesseract':
dlopen(libtesseract.dylib, 0x0009): tried: 'libtesseract.dylib' (no such file)...
```

**Cause:** `DYLD_LIBRARY_PATH` was not set before the JVM started.

**Solution:** Stop the application and restart using `./start.sh`

## 📝 Why This Is Required

1. **macOS Dynamic Linker (`dyld`)**: On macOS, the dynamic linker requires `DYLD_LIBRARY_PATH` to be set as an environment variable **before** the process starts.

2. **JVM Limitation**: Once the JVM is running, you cannot modify `DYLD_LIBRARY_PATH` - it's already been read by the system.

3. **Tess4j Behavior**: Tess4j uses JNA to load native libraries, and JNA calls `dlopen()` which respects `DYLD_LIBRARY_PATH` but not `java.library.path` on macOS.

## 🔍 Verification

After starting with the script, check the logs for:

```
✅ Loaded leptonica using JNA: /opt/local/lib/libleptonica.6.dylib
✅ Loaded tesseract using JNA (absolute path): /opt/local/lib/libtesseract.5.dylib
✅ Tesseract instance created successfully
✅ Tesseract OCR initialized and verified. Data path: /opt/local/share/tessdata
```

If you see these messages, Tesseract is working! 🎉

## 🚨 Troubleshooting

### Error: "DYLD_LIBRARY_PATH must be set BEFORE starting the JVM"

**Solution:** Use the startup script:
```bash
./start.sh
```

### Error: "Tesseract is not installed"

**Solution:** Install Tesseract:
```bash
# MacPorts (if using MacPorts)
sudo port install tesseract

# Homebrew
brew install tesseract
```

### Error: "tessdata directory not found"

**Solution:** Verify tessdata exists:
```bash
ls -la /opt/local/share/tessdata  # MacPorts
ls -la /opt/homebrew/share/tessdata  # Homebrew (Apple Silicon)
ls -la /usr/local/share/tessdata  # Homebrew (Intel)
```

## 📚 Additional Resources

- `TESSERACT_FIX_FOREVER.md` - Complete technical documentation
- `docs/TESSERACT_INSTALLATION.md` - Installation instructions
- `docs/TESSERACT_INTEGRATION.md` - Integration details

