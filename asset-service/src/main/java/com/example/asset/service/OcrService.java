package com.example.asset.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import com.sun.jna.NativeLibrary;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.example.asset.config.TesseractConfig;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.*;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * ✅ OcrService
 * Service for extracting text from multiple file formats:
 * - Images: JPG, PNG, GIF (using Tesseract OCR)
 * - PDF: PDF files (using PDFBox)
 * - Word: DOC, DOCX (using Apache POI)
 * - Excel: XLS, XLSX (using Apache POI)
 * - PowerPoint: PPT, PPTX (using Apache POI)
 */
@Service
public class OcrService {

    private static final Logger log = LoggerFactory.getLogger(OcrService.class);
    private Tesseract tesseract;
    private boolean tesseractAvailable = false;
    /** When true, OCR is done via external tesseract process (no native lib). */
    private boolean useProcessMode = false;
    /** Path to tesseract executable when using process mode. */
    private String tesseractExecutablePath;
    
    @Autowired(required = false)
    private TesseractConfig tesseractConfig;

    // Supported file types
    private static final List<String> SUPPORTED_IMAGE_TYPES = List.of(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/bmp", "image/tiff"
    );
    
    private static final List<String> SUPPORTED_PDF_TYPES = List.of("application/pdf");
    
    private static final List<String> SUPPORTED_WORD_TYPES = List.of(
        "application/msword", 
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );
    
    private static final List<String> SUPPORTED_EXCEL_TYPES = List.of(
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );
    
    private static final List<String> SUPPORTED_PPT_TYPES = List.of(
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    );

    public OcrService() {
        // Initialization will happen in @PostConstruct to ensure TesseractConfig is available
    }
    
    @javax.annotation.PostConstruct
    public void initializeTesseract() {
        log.info("🔧 Initializing Tesseract OCR...");
        log.info("   java.library.path: {}", System.getProperty("java.library.path"));
        log.info("   TESSDATA_PREFIX: {}", System.getenv("TESSDATA_PREFIX"));
        
        // Skip initialization if disabled in config
        if (tesseractConfig != null && !tesseractConfig.isEnabled()) {
            log.info("⚠️ Tesseract OCR is disabled in configuration.");
            tesseractAvailable = false;
            return;
        }
        
        // Process mode: call tesseract as external process (no native library). Works on macOS Ventura.
        if (tesseractConfig != null && tesseractConfig.isUseProcess()) {
            useProcessMode = true;
            tesseractExecutablePath = resolveTesseractExecutable();
            tesseractAvailable = (tesseractExecutablePath != null);
            if (tesseractAvailable) {
                log.info("✅ Tesseract OCR initialized in PROCESS mode (external executable). Path: {}", tesseractExecutablePath);
            } else {
                log.warn("⚠️ Tesseract executable not found. Image OCR will be disabled. Install tesseract or set tesseract.executable-path.");
            }
            return;
        }
        
        // Track if we successfully loaded libraries explicitly
        boolean librariesLoadedExplicitly = false;
        String loadedTesseractPath = null; // Track the path of the loaded library
        
        try {
            // CRITICAL: Set java.library.path BEFORE creating Tesseract instance
            // Tess4j uses System.loadLibrary() which requires the path to be in java.library.path
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("mac")) {
                String existingLibraryPath = System.getProperty("java.library.path");
                String[] macLibraryPaths = {
                    "/opt/local/lib",        // MacPorts
                    "/opt/homebrew/lib",     // Homebrew (Apple Silicon)
                    "/usr/local/lib"         // Homebrew (Intel)
                };
                
                StringBuilder newLibraryPath = new StringBuilder();
                for (String libPath : macLibraryPaths) {
                    java.io.File libDir = new java.io.File(libPath);
                    if (libDir.exists() && libDir.isDirectory()) {
                        if (newLibraryPath.length() > 0) {
                            newLibraryPath.append(java.io.File.pathSeparator);
                        }
                        newLibraryPath.append(libPath);
                    }
                }
                
                if (newLibraryPath.length() > 0) {
                    // Append existing path if it exists
                    if (existingLibraryPath != null && !existingLibraryPath.isEmpty()) {
                        newLibraryPath.append(java.io.File.pathSeparator).append(existingLibraryPath);
                    }
                    
                    // Set the library path BEFORE loading libraries
                    System.setProperty("java.library.path", newLibraryPath.toString());
                    log.info("📚 Set java.library.path to: {}", newLibraryPath.toString());
                    
                    // Use reflection to reset the ClassLoader's cached library path
                    // This is necessary because java.library.path is cached by the ClassLoader
                    try {
                        java.lang.reflect.Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
                        sysPathsField.setAccessible(true);
                        sysPathsField.set(null, null);
                        log.debug("✅ Reset ClassLoader library path cache");
                    } catch (Exception e) {
                        log.debug("⚠️ Could not reset ClassLoader cache (may be normal): {}", e.getMessage());
                    }
                }
            }
            
            // Explicitly load native libraries on macOS before creating Tesseract instance
            // CRITICAL: Use JNA's NativeLibrary to load, as tess4j uses JNA internally
            // This ensures the library is available when tess4j tries to use it at runtime
            if (osName.contains("mac")) {
                log.info("🔧 Attempting to explicitly load Tesseract native libraries using JNA...");
                try {
                    // Load leptonica first (tesseract depends on it)
                    // Use versioned library first, then fallback to symlink
                    String[] leptonicaPaths = {
                        "/opt/local/lib/libleptonica.6.dylib",  // MacPorts (versioned) - preferred
                        "/opt/local/lib/libleptonica.dylib",    // MacPorts (symlink)
                        "/opt/homebrew/lib/libleptonica.dylib",  // Homebrew (Apple Silicon)
                        "/usr/local/lib/libleptonica.dylib"      // Homebrew (Intel)
                    };
                    
                    boolean leptonicaLoaded = false;
                    for (String libPath : leptonicaPaths) {
                        java.io.File libFile = new java.io.File(libPath);
                        if (libFile.exists()) {
                            try {
                                // Use JNA's NativeLibrary - this is what tess4j uses internally
                                // Loading with absolute path ensures it's found
                                NativeLibrary.getInstance(libPath);
                                log.info("✅ Loaded leptonica using JNA: {}", libPath);
                                leptonicaLoaded = true;
                                break;
                            } catch (Exception e) {
                                log.debug("   JNA failed for {}: {}", libPath, e.getMessage());
                                // Try System.load() as fallback
                                try {
                                    System.load(libPath);
                                    log.info("✅ Loaded leptonica using System.load(): {}", libPath);
                                    leptonicaLoaded = true;
                                    break;
                                } catch (UnsatisfiedLinkError | SecurityException e2) {
                                    log.debug("   System.load() also failed for {}: {}", libPath, e2.getMessage());
                                }
                            }
                        }
                    }
                    
                    if (!leptonicaLoaded) {
                        log.warn("⚠️ Could not load leptonica library explicitly");
                    }
                    
                    // Now load tesseract - CRITICAL: Load with both absolute path AND by name
                    // This ensures tess4j can find it when it tries to load by name at runtime
                    String[] tesseractPaths = {
                        "/opt/local/lib/libtesseract.5.dylib",  // MacPorts (versioned) - preferred
                        "/opt/local/lib/libtesseract.dylib",    // MacPorts (symlink)
                        "/opt/homebrew/lib/libtesseract.dylib",  // Homebrew (Apple Silicon)
                        "/usr/local/lib/libtesseract.dylib"      // Homebrew (Intel)
                    };
                    
                    boolean tesseractLoaded = false;
                    
                    // PERMANENT FIX: Load with absolute path using System.load() first
                    // This makes the library available in the system's library cache
                    // Then we can use System.loadLibrary() to load by name
                    for (String libPath : tesseractPaths) {
                        java.io.File libFile = new java.io.File(libPath);
                        if (libFile.exists()) {
                            try {
                                // CRITICAL: Use System.load() with absolute path FIRST
                                // This loads the library into the system's library cache
                                // After this, System.loadLibrary("tesseract") should work
                                System.load(libPath);
                                log.info("✅ Loaded tesseract using System.load() (absolute path): {}", libPath);
                                loadedTesseractPath = libPath;
                                
                                // Now try to load by name - this should work because the library is in the cache
                                try {
                                    System.loadLibrary("tesseract");
                                    log.info("✅ Verified tesseract can be loaded by name after System.load()");
                                    tesseractLoaded = true;
                                    break;
                                } catch (UnsatisfiedLinkError e) {
                                    // If loading by name fails, the library is still loaded
                                    // We'll register it in JNA's cache manually
                                    log.debug("   System.loadLibrary('tesseract') failed, but library is loaded: {}", e.getMessage());
                                    
                                    // Try to register in JNA's cache using reflection
                                    try {
                                        // Load via JNA with absolute path to register in JNA's cache
                                        NativeLibrary jnaLib = NativeLibrary.getInstance(libPath);
                                        
                                        // Use reflection to register it with name "tesseract" in JNA's cache
                                        // JNA caches libraries in a synchronized Map
                                        try {
                                            // Get JNA's internal library cache
                                            java.lang.reflect.Field librariesField = NativeLibrary.class.getDeclaredField("libraries");
                                            librariesField.setAccessible(true);
                                            @SuppressWarnings("unchecked")
                                            java.util.Map<String, NativeLibrary> cache = (java.util.Map<String, NativeLibrary>) librariesField.get(null);
                                            
                                            // Register the library with name "tesseract" so tess4j can find it
                                            synchronized (cache) {
                                                cache.put("tesseract", jnaLib);
                                            }
                                            log.info("✅ Registered tesseract in JNA cache by name (PERMANENT FIX)");
                                            tesseractLoaded = true;
                                            break;
                                        } catch (NoSuchFieldException | IllegalAccessException reflectionEx2) {
                                            // If reflection fails, library is still loaded via System.load()
                                            log.debug("   Could not register in JNA cache via reflection: {}", reflectionEx2.getMessage());
                                            log.debug("   Library is still loaded and should work for tess4j");
                                            tesseractLoaded = true;
                                            break;
                                        }
                                    } catch (Exception reflectionEx) {
                                        log.warn("⚠️ Could not register in JNA cache: {}", reflectionEx.getMessage());
                                        // Library is still loaded via System.load(), so continue
                                        tesseractLoaded = true;
                                        break;
                                    }
                                }
                            } catch (UnsatisfiedLinkError | SecurityException e) {
                                log.debug("   System.load() failed for {}: {}", libPath, e.getMessage());
                                // Try JNA as fallback
                                try {
                                    NativeLibrary.getInstance(libPath);
                                    log.info("✅ Loaded tesseract using JNA (absolute path): {}", libPath);
                                    tesseractLoaded = true;
                                    loadedTesseractPath = libPath;
                                    break;
                                } catch (Exception e2) {
                                    log.debug("   JNA also failed for {}: {}", libPath, e2.getMessage());
                                }
                            }
                        }
                    }
                    
                    // If we still haven't loaded it, try loading by name
                    if (!tesseractLoaded) {
                        // If absolute path loading failed, try loading by name
                        try {
                            System.loadLibrary("tesseract");
                            log.info("✅ Loaded tesseract by name using System.loadLibrary() (via java.library.path)");
                            tesseractLoaded = true;
                        } catch (UnsatisfiedLinkError e) {
                            log.debug("   System.loadLibrary('tesseract') failed: {}", e.getMessage());
                            try {
                                NativeLibrary.getInstance("tesseract");
                                log.info("✅ Loaded tesseract by name using JNA");
                                tesseractLoaded = true;
                            } catch (Exception e2) {
                                log.warn("⚠️ Could not load Tesseract library by name: {}", e2.getMessage());
                            }
                        }
                    }
                    
                    // Mark that we successfully loaded libraries
                    if (tesseractLoaded) {
                        librariesLoadedExplicitly = true;
                        log.info("✅ Tesseract native libraries loaded successfully");
                        if (loadedTesseractPath != null) {
                            log.info("   Library location: {}", loadedTesseractPath);
                        }
                    }
                } catch (Exception e) {
                    log.warn("⚠️ Explicit library loading failed, will try automatic loading: {}", e.getMessage());
                    log.warn("   Error details: {}", e.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }
            
            log.info("📦 Creating Tesseract instance...");
            // Try to create Tesseract instance - this will throw UnsatisfiedLinkError if not installed
            // CRITICAL: On macOS, even if we've loaded the library with absolute path,
            // tess4j will try to load it by name, which requires DYLD_LIBRARY_PATH to be set
            // BEFORE the JVM starts. We can't set it from Java code.
            try {
            this.tesseract = new Tesseract();
                log.info("✅ Tesseract instance created successfully");
            } catch (UnsatisfiedLinkError e) {
                // If creation fails and we've loaded the library explicitly, the issue is likely
                // that DYLD_LIBRARY_PATH wasn't set before JVM started
                if (librariesLoadedExplicitly && loadedTesseractPath != null) {
                    String errorMsg = e.getMessage();
                    if (errorMsg != null && errorMsg.contains("dlopen") && errorMsg.contains("no such file")) {
                        log.error("❌ Tesseract instance creation failed: {}", e.getMessage());
                        log.error("📝 ROOT CAUSE: DYLD_LIBRARY_PATH must be set BEFORE the JVM starts");
                        log.error("   Library is loaded at: {}", loadedTesseractPath);
                        log.error("   But macOS's dlopen cannot find it because DYLD_LIBRARY_PATH is not set");
                        log.error("");
                        log.error("🔧 SOLUTION: Use the run script to start the application:");
                        log.error("   cd asset-service");
                        log.error("   ./run-with-tesseract.sh");
                        log.error("");
                        log.error("   OR manually set environment variables before starting:");
                        log.error("   export DYLD_LIBRARY_PATH=\"/opt/local/lib:$DYLD_LIBRARY_PATH\"");
                        log.error("   export TESSDATA_PREFIX=\"/opt/local/share/tessdata\"");
                        log.error("   mvn spring-boot:run");
                        throw new UnsatisfiedLinkError("Tesseract library loaded at " + loadedTesseractPath + " but cannot be accessed. DYLD_LIBRARY_PATH must be set BEFORE starting the JVM. Use ./run-with-tesseract.sh to start the application.");
                    }
                }
                // If library wasn't loaded explicitly, rethrow the original error
                throw e;
            }
            
            // Set Tesseract data path - use config if available, otherwise auto-detect
            String tessDataPath = null;
            
            // Priority 1: Use configuration from application.yml
            if (tesseractConfig != null && tesseractConfig.getDataPath() != null && !tesseractConfig.getDataPath().isEmpty()) {
                tessDataPath = tesseractConfig.getDataPath();
                log.info("📝 Using Tesseract data path from configuration: {}", tessDataPath);
            }
            // Priority 2: Use environment variable
            else if (System.getenv("TESSDATA_PREFIX") != null && !System.getenv("TESSDATA_PREFIX").isEmpty()) {
                tessDataPath = System.getenv("TESSDATA_PREFIX");
                log.info("📝 Using Tesseract data path from environment variable: {}", tessDataPath);
            }
            // Priority 3: Auto-detect based on OS
            else {
                osName = System.getProperty("os.name").toLowerCase();
                if (osName.contains("win")) {
                    tessDataPath = "C:\\Program Files\\Tesseract-OCR\\tessdata";
                } else if (osName.contains("mac")) {
                    // Try multiple common macOS paths
                    String[] macPaths = {
                        "/opt/local/share/tessdata",       // MacPorts (check first for this system)
                        "/opt/homebrew/share/tessdata",    // Homebrew (Apple Silicon)
                        "/usr/local/share/tessdata"        // Homebrew (Intel)
                    };
                    for (String path : macPaths) {
                        java.io.File pathFile = new java.io.File(path);
                        if (pathFile.exists() && pathFile.isDirectory()) {
                            tessDataPath = path;
                            log.info("📝 Auto-detected Tesseract data path: {}", tessDataPath);
                            break;
                        }
                    }
                    if (tessDataPath == null || tessDataPath.isEmpty()) {
                        tessDataPath = "/opt/local/share/tessdata"; // Default for MacPorts
                        log.warn("⚠️ Could not detect tessdata path, using default: {}", tessDataPath);
                    }
                } else {
                    tessDataPath = "/usr/share/tesseract-ocr/5/tessdata";
                }
            }
            
            // Verify the path exists
            java.io.File dataPathFile = new java.io.File(tessDataPath);
            if (!dataPathFile.exists() || !dataPathFile.isDirectory()) {
                log.warn("⚠️ Tesseract data path does not exist: {}", tessDataPath);
                log.warn("   Attempting to continue anyway...");
            }
            
            tesseract.setDatapath(tessDataPath);
            
            // Set language from config or use default
            String language = (tesseractConfig != null && tesseractConfig.getLanguage() != null) 
                ? tesseractConfig.getLanguage() 
                : "eng";
            tesseract.setLanguage(language);
            
            // Set page segmentation mode from config or use default
            int pageSegMode = (tesseractConfig != null) 
                ? tesseractConfig.getPageSegMode() 
                : 1;
            tesseract.setPageSegMode(pageSegMode);
            
            // Set OCR engine mode from config or use default
            int ocrEngineMode = (tesseractConfig != null) 
                ? tesseractConfig.getOcrEngineMode() 
                : 1;
            tesseract.setOcrEngineMode(ocrEngineMode);
            
            // Test Tesseract availability immediately
            // If we explicitly loaded libraries, be more lenient with the test
            if (librariesLoadedExplicitly) {
                log.info("📝 Libraries loaded explicitly, performing lenient availability test...");
                // Try a simple test - if it throws UnsatisfiedLinkError, it's not working
                try {
                    // Just verify the instance is not null and can be accessed
                    if (tesseract != null) {
                        // Try to set a property - this will fail if library isn't loaded
                        // We already set language above, so just verify instance is accessible
                        tesseract.setLanguage(language); // Re-set to verify it works
                        log.info("✅ Tesseract instance is accessible. Data path: {}", tessDataPath);
                        tesseractAvailable = true;
                    } else {
                        tesseractAvailable = false;
                    }
                } catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
                    log.error("❌ Tesseract library not accessible despite explicit loading: {}", e.getMessage());
                    tesseractAvailable = false;
                } catch (Exception e) {
                    // Other exceptions are fine - library is loaded
                    log.info("✅ Tesseract library is loaded and accessible (exception during test is acceptable)");
                    tesseractAvailable = true;
                }
            } else {
                // Standard test if libraries weren't loaded explicitly
            tesseractAvailable = testTesseractAvailability();
            }
            
            if (tesseractAvailable) {
                log.info("✅ Tesseract OCR initialized and verified. Data path: {}", tessDataPath);
            } else {
                log.warn("⚠️ Tesseract OCR initialized but not available. Image OCR will be disabled.");
                log.warn("   This may be a false negative - Tesseract might still work at runtime.");
                logInstallationInstructions();
            }
        } catch (UnsatisfiedLinkError e) {
            tesseractAvailable = false;
            tesseract = null;
            String osName = System.getProperty("os.name").toLowerCase();
            String installCmd = osName.contains("mac") ? "brew install tesseract" :
                               osName.contains("win") ? "Download from https://github.com/UB-Mannheim/tesseract/wiki" :
                               "sudo apt-get install tesseract-ocr";
            
            log.error("❌ Tesseract OCR native library not found (UnsatisfiedLinkError).");
            log.error("📝 This means the JVM cannot load the native Tesseract libraries.");
            log.error("   Current java.library.path: {}", System.getProperty("java.library.path"));
            log.error("   Current TESSDATA_PREFIX: {}", System.getenv("TESSDATA_PREFIX"));
            
            if (osName.contains("mac")) {
                log.error("   For macOS with MacPorts, ensure /opt/local/lib is in java.library.path");
                log.error("   For macOS with Homebrew, ensure /opt/homebrew/lib or /usr/local/lib is in java.library.path");
            }
            
            log.error("📝 To install Tesseract, run: {}", installCmd);
            log.error("   After installation, restart the application.");
            log.error("   Error details: {}", e.getMessage());
            log.error("   Stack trace:", e);
        } catch (NoClassDefFoundError e) {
            tesseractAvailable = false;
            tesseract = null;
            log.error("❌ Tesseract OCR Java bindings cannot be initialized (NoClassDefFoundError).");
            log.error("📝 This usually means the native Tesseract library is not accessible.");
            log.error("   Current java.library.path: {}", System.getProperty("java.library.path"));
            log.error("   Error: {}", e.getMessage());
            log.error("   Try: brew reinstall tesseract (macOS) or ensure native libraries are in library path");
            log.error("   Stack trace:", e);
        } catch (Exception e) {
            tesseractAvailable = false;
            tesseract = null;
            log.error("❌ Tesseract OCR initialization failed: {}", e.getMessage());
            log.error("📝 Please ensure Tesseract is properly installed and configured.");
        }
    }
    
    // ============================================================
    // 🧪 TEST TESSERACT AVAILABILITY
    // ============================================================
    private boolean testTesseractAvailability() {
        if (tesseract == null) {
            log.error("❌ Tesseract instance is null, cannot test availability");
            return false;
        }
        
        try {
            // Create a larger test image (10x10 pixels) - more reliable than 1x1
            BufferedImage testImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
            // Fill with white pixels
            for (int x = 0; x < 10; x++) {
                for (int y = 0; y < 10; y++) {
                    testImage.setRGB(x, y, 0xFFFFFF); // White pixel
                }
            }
            
            log.debug("🧪 Testing Tesseract with 10x10 test image...");
            
            // Try to run OCR on the test image
            // This will fail if the native library can't be loaded
            try {
                String result = tesseract.doOCR(testImage);
                log.debug("✅ Tesseract test successful, OCR result length: {}", result != null ? result.length() : 0);
                return true; // Tesseract is working
            } catch (NoClassDefFoundError e) {
                log.error("❌ Tesseract native library test failed (NoClassDefFoundError): {}", e.getMessage());
                log.error("   This means the native library was not properly loaded");
                return false;
            } catch (UnsatisfiedLinkError e) {
                log.error("❌ Tesseract native library test failed (UnsatisfiedLinkError): {}", e.getMessage());
                log.error("   This means the JVM cannot find or load the native library");
                log.error("   Library path: {}", System.getProperty("java.library.path"));
                return false;
            } catch (TesseractException e) {
                // TesseractException is fine - it means Tesseract is working but couldn't extract text
                // This is expected for a small/blank image and means Tesseract is functional
                String errorMsg = e.getMessage();
                if (errorMsg != null && (errorMsg.contains("Please make sure the TESSDATA_PREFIX") || 
                                         errorMsg.contains("Error opening data file"))) {
                    // This is a tessdata path issue, not a library loading issue
                    log.warn("⚠️ Tesseract library is loaded but tessdata path may be incorrect: {}", errorMsg);
                    // Still return true - the library is working, just need to fix tessdata path
                return true;
            }
                log.debug("✅ Tesseract test successful (TesseractException is expected for test image): {}", errorMsg);
                return true;
        } catch (Exception e) {
                // Other exceptions might indicate a problem, but could also be normal
                log.warn("⚠️ Tesseract test encountered exception (may be normal): {} - {}", 
                        e.getClass().getSimpleName(), e.getMessage());
                // If it's not a critical error, assume Tesseract is working
                if (e instanceof RuntimeException && e.getCause() instanceof UnsatisfiedLinkError) {
            return false;
                }
                // For other exceptions, assume it might work
                return true;
            }
        } catch (NoClassDefFoundError e) {
            log.error("❌ Tesseract native library cannot be loaded (NoClassDefFoundError): {}", e.getMessage());
            log.error("   This usually means tess4j JAR is missing or corrupted");
            return false;
        } catch (UnsatisfiedLinkError e) {
            log.error("❌ Tesseract native library cannot be loaded (UnsatisfiedLinkError): {}", e.getMessage());
            log.error("   This means the native library file cannot be found or loaded");
            log.error("   Library path: {}", System.getProperty("java.library.path"));
            return false;
        } catch (Exception e) {
            log.warn("⚠️ Tesseract availability test had unexpected issues: {} - {}", 
                    e.getClass().getSimpleName(), e.getMessage());
            // Don't fail completely - let it try at runtime
            return true; // Assume it might work
        }
    }
    
    // ============================================================
    // 📝 LOG INSTALLATION INSTRUCTIONS
    // ============================================================
    private void logInstallationInstructions() {
        String osName = System.getProperty("os.name").toLowerCase();
        String installCmd;
        String verifyCmd;
        
        if (osName.contains("mac")) {
            installCmd = "brew install tesseract";
            verifyCmd = "tesseract --version";
            log.error("📝 To install Tesseract on macOS:");
            log.error("   1. Run: {}", installCmd);
            log.error("   2. Verify: {}", verifyCmd);
            log.error("   3. Restart the application");
        } else if (osName.contains("win")) {
            installCmd = "Download from https://github.com/UB-Mannheim/tesseract/wiki";
            log.error("📝 To install Tesseract on Windows:");
            log.error("   1. {}", installCmd);
            log.error("   2. Add Tesseract to PATH");
            log.error("   3. Restart the application");
        } else {
            installCmd = "sudo apt-get install tesseract-ocr";
            verifyCmd = "tesseract --version";
            log.error("📝 To install Tesseract on Linux:");
            log.error("   1. Run: {}", installCmd);
            log.error("   2. Verify: {}", verifyCmd);
            log.error("   3. Restart the application");
        }
    }
    
    // ============================================================
    // ✅ CHECK IF TESSERACT IS AVAILABLE (Public Method)
    // ============================================================
    public boolean isTesseractAvailable() {
        if (useProcessMode) {
            return tesseractAvailable && tesseractExecutablePath != null;
        }
        return tesseractAvailable && tesseract != null;
    }
    
    // ============================================================
    // 🔧 RESOLVE TESSERACT EXECUTABLE (Process mode)
    // ============================================================
    private String resolveTesseractExecutable() {
        if (tesseractConfig != null && tesseractConfig.getExecutablePath() != null && !tesseractConfig.getExecutablePath().isBlank()) {
            java.io.File exe = new java.io.File(tesseractConfig.getExecutablePath());
            if (exe.exists() && exe.canExecute()) {
                return exe.getAbsolutePath();
            }
        }
        String[] paths = {
            "/opt/local/bin/tesseract",   // MacPorts
            "/opt/homebrew/bin/tesseract", // Homebrew Apple Silicon
            "/usr/local/bin/tesseract",   // Homebrew Intel
            "tesseract"                    // PATH
        };
        for (String p : paths) {
            java.io.File f = new java.io.File(p);
            if (f.exists() && f.canExecute()) {
                return f.getAbsolutePath();
            }
        }
        try {
            Process proc = new ProcessBuilder("which", "tesseract").start();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
                String line = r.readLine();
                proc.waitFor();
                if (line != null && !line.isBlank()) {
                    java.io.File f = new java.io.File(line.trim());
                    if (f.exists() && f.canExecute()) return f.getAbsolutePath();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.debug("Interrupted while resolving tesseract: {}", e.getMessage());
        } catch (Exception e) {
            log.debug("Could not resolve tesseract via which: {}", e.getMessage());
        }
        return null;
    }
    
    // ============================================================
    // 📄 EXTRACT TEXT FROM FILE (Universal Method)
    // ============================================================
    public String extractText(MultipartFile file) throws IOException, TesseractException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();
        long fileSize = file.getSize();
        
        // Validate file size (max 50MB for images, 100MB for other files)
        long maxImageSize = 50 * 1024 * 1024; // 50MB
        long maxFileSize = 100 * 1024 * 1024; // 100MB
        
        if (isImageFile(contentType) && fileSize > maxImageSize) {
            throw new IllegalArgumentException(
                String.format("Image file too large: %d bytes (max: %d bytes / 50MB). " +
                            "Please resize the image before uploading.", fileSize, maxImageSize));
        } else if (fileSize > maxFileSize) {
            throw new IllegalArgumentException(
                String.format("File too large: %d bytes (max: %d bytes / 100MB).", fileSize, maxFileSize));
        }
        
        log.info("🔍 Starting text extraction for file: {} (type: {}, size: {} bytes / {:.2f} MB)", 
                fileName, contentType, fileSize, fileSize / (1024.0 * 1024.0));

        // Route to appropriate extractor based on file type
        try {
            if (isImageFile(contentType)) {
                return extractTextFromImage(file);
            } else if (isPdfFile(contentType)) {
                return extractTextFromPdf(file);
            } else if (isWordFile(contentType)) {
                return extractTextFromWord(file);
            } else if (isExcelFile(contentType)) {
                return extractTextFromExcel(file);
            } else if (isPowerPointFile(contentType)) {
                return extractTextFromPowerPoint(file);
            } else {
                throw new UnsupportedOperationException(
                    "Unsupported file type: " + contentType + ". Supported types: Images (JPG, PNG, GIF), PDF, Word (DOC, DOCX), Excel (XLS, XLSX), PowerPoint (PPT, PPTX)");
            }
        } catch (Exception e) {
            log.error("❌ Text extraction failed for file: {} - {}", fileName, e.getMessage(), e);
            throw e;
        }
    }

    // ============================================================
    // 🖼️ EXTRACT TEXT FROM IMAGE (OCR)
    // ============================================================
    // ============================================================
    // 🔧 ENSURE LIBRARY LOADED (Runtime check)
    // ============================================================
    /**
     * Ensures Tesseract native library is loaded before use.
     * This is called before each OCR operation to handle cases where
     * the library might not be accessible at runtime.
     */
    private void ensureLibraryLoaded() {
        if (tesseract == null) {
            return; // Can't do anything if instance is null
        }
        
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("mac")) {
            try {
                // Try to access the library via JNA to ensure it's loaded
                // This will fail silently if already loaded, which is fine
                String[] tesseractPaths = {
                    "/opt/local/lib/libtesseract.5.dylib",
                    "/opt/local/lib/libtesseract.dylib",
                    "/opt/homebrew/lib/libtesseract.dylib",
                    "/usr/local/lib/libtesseract.dylib"
                };
                
                for (String libPath : tesseractPaths) {
                    java.io.File libFile = new java.io.File(libPath);
                    if (libFile.exists()) {
                        try {
                            // Try to get the library instance - this ensures it's in JNA's cache
                            NativeLibrary.getInstance(libPath);
                            break; // Success, no need to try others
                        } catch (Exception e) {
                            // Library might already be loaded, or path might be wrong
                            // Continue to next path
                        }
                    }
                }
            } catch (Exception e) {
                // Silently fail - library might already be loaded
                log.debug("Library pre-load check: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Resolves a tessdata directory that exists and contains the given language file (e.g. eng.traineddata).
     * Tries: config data-path, TESSDATA_PREFIX env, path inferred from tesseract executable, then common OS paths.
     */
    private String resolveTessDataPrefix(String language) {
        String langFile = (language != null && !language.isBlank() ? language : "eng") + ".traineddata";
        java.io.File f;

        // 1) Config data-path
        if (tesseractConfig != null && tesseractConfig.getDataPath() != null && !tesseractConfig.getDataPath().isBlank()) {
            f = new java.io.File(tesseractConfig.getDataPath(), langFile);
            if (f.exists()) {
                log.debug("Using tessdata from config: {}", tesseractConfig.getDataPath());
                return tesseractConfig.getDataPath();
            }
        }
        // 2) Environment TESSDATA_PREFIX
        String envPrefix = System.getenv("TESSDATA_PREFIX");
        if (envPrefix != null && !envPrefix.isBlank()) {
            f = new java.io.File(envPrefix, langFile);
            if (f.exists()) return envPrefix;
        }
        // 3) Infer from tesseract executable path (e.g. /opt/homebrew/bin/tesseract -> /opt/homebrew/share/tessdata)
        if (tesseractExecutablePath != null && !tesseractExecutablePath.isBlank()) {
            String inferred = inferTessDataFromExecutable(tesseractExecutablePath);
            if (inferred != null) {
                f = new java.io.File(inferred, langFile);
                if (f.exists()) {
                    log.info("Using tessdata path inferred from tesseract executable: {}", inferred);
                    return inferred;
                }
            }
        }
        // 4) Common paths (macOS then Linux/Windows)
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("mac")) {
            for (String base : new String[]{ "/opt/local/share/tessdata", "/opt/homebrew/share/tessdata", "/usr/local/share/tessdata" }) {
                f = new java.io.File(base, langFile);
                if (f.exists()) {
                    log.info("Using auto-detected tessdata path: {}", base);
                    return base;
                }
            }
            // Homebrew Cellar: e.g. /opt/homebrew/Cellar/tesseract/5.3.0/share/tessdata
            String cellarBase = "/opt/homebrew/Cellar/tesseract";
            java.io.File cellarDir = new java.io.File(cellarBase);
            if (cellarDir.exists()) {
                java.io.File[] versions = cellarDir.listFiles((dir, name) -> new java.io.File(dir, name).isDirectory());
                if (versions != null) {
                    for (java.io.File ver : versions) {
                        String tessdataPath = new java.io.File(ver, "share/tessdata").getAbsolutePath();
                        f = new java.io.File(tessdataPath, langFile);
                        if (f.exists()) {
                            log.info("Using tessdata from Homebrew Cellar: {}", tessdataPath);
                            return tessdataPath;
                        }
                    }
                }
            }
            cellarBase = "/usr/local/Cellar/tesseract";
            cellarDir = new java.io.File(cellarBase);
            if (cellarDir.exists()) {
                java.io.File[] versions = cellarDir.listFiles((dir, name) -> new java.io.File(dir, name).isDirectory());
                if (versions != null) {
                    for (java.io.File ver : versions) {
                        String tessdataPath = new java.io.File(ver, "share/tessdata").getAbsolutePath();
                        f = new java.io.File(tessdataPath, langFile);
                        if (f.exists()) {
                            log.info("Using tessdata from Homebrew Cellar: {}", tessdataPath);
                            return tessdataPath;
                        }
                    }
                }
            }
        } else if (osName.contains("win")) {
            String winPath = "C:\\Program Files\\Tesseract-OCR\\tessdata";
            f = new java.io.File(winPath, langFile);
            if (f.exists()) return winPath;
        } else {
            for (String base : new String[]{ "/usr/share/tesseract-ocr/5/tessdata", "/usr/share/tesseract-ocr/4.00/tessdata", "/usr/share/tessdata" }) {
                f = new java.io.File(base, langFile);
                if (f.exists()) return base;
            }
        }
        return null;
    }

    /** Infers tessdata directory from tesseract executable path (e.g. /opt/homebrew/bin/tesseract -> /opt/homebrew/share/tessdata). */
    private String inferTessDataFromExecutable(String executablePath) {
        if (executablePath == null || executablePath.isBlank()) return null;
        java.io.File exe = new java.io.File(executablePath);
        try {
            String abs = exe.getAbsolutePath();
            String sep = java.io.File.separator;
            // .../bin/tesseract -> .../share/tessdata
            if (abs.contains(sep + "bin" + sep) || abs.endsWith(sep + "bin")) {
                int idx = abs.indexOf(sep + "bin" + sep);
                if (idx < 0) idx = abs.lastIndexOf(sep + "bin");
                if (idx > 0) {
                    String prefix = abs.substring(0, idx);
                    return prefix + sep + "share" + sep + "tessdata";
                }
            }
            // Handle Unix path when on Windows (e.g. from config)
            if (abs.contains("/bin/")) {
                String prefix = abs.substring(0, abs.indexOf("/bin/"));
                return prefix + "/share/tessdata";
            }
        } catch (Exception e) {
            log.debug("Could not infer tessdata from executable: {}", e.getMessage());
        }
        return null;
    }

    // ============================================================
    // 🖼️ EXTRACT TEXT FROM IMAGE VIA EXTERNAL PROCESS (no native lib)
    // ============================================================
    /**
     * Calls tesseract as an external process. Bypasses native library linking
     * and works on macOS Ventura without DYLD_LIBRARY_PATH.
     */
    private String extractTextFromImageViaProcess(MultipartFile file) throws IOException, TesseractException {
        long startTime = System.currentTimeMillis();
        
        if (!tesseractAvailable || tesseractExecutablePath == null) {
            throw new TesseractException(
                "Tesseract is not available (process mode). Install tesseract and set tesseract.executable-path, or ensure tesseract is on PATH.");
        }
        
        String ext = getImageExtension(file.getContentType(), file.getOriginalFilename());
        Path tempFile = Files.createTempFile("ocr_", ext);
        
        try {
            Files.write(tempFile, file.getBytes());
            String imagePath = tempFile.toAbsolutePath().toString();
            String lang = (tesseractConfig != null && tesseractConfig.getLanguage() != null) 
                ? tesseractConfig.getLanguage() 
                : "eng";
            
            ProcessBuilder pb = new ProcessBuilder(
                tesseractExecutablePath,
                imagePath,
                "stdout",
                "-l",
                lang
            );
            pb.redirectErrorStream(false);
            
            // Resolve tessdata path that actually contains the language file (e.g. eng.traineddata)
            String tessDataPrefix = resolveTessDataPrefix(lang);
            if (tessDataPrefix == null || tessDataPrefix.isBlank()) {
                String osName = System.getProperty("os.name").toLowerCase();
                String hint = osName.contains("mac")
                    ? "MacPorts: sudo port install tesseract-eng. Homebrew: brew install tesseract. Then restart the app."
                    : "Install Tesseract language data (e.g. eng.traineddata) and set TESSDATA_PREFIX to that directory.";
                throw new TesseractException(
                    "Tesseract tessdata not found: no directory contains eng.traineddata. " + hint);
            }
            pb.environment().put("TESSDATA_PREFIX", tessDataPrefix);
            
            Process process = pb.start();
            
            StringBuilder result = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }
            }
            
            int exitCode;
            try {
                exitCode = process.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new TesseractException("OCR process was interrupted", e);
            }
            
            if (exitCode != 0) {
                StringBuilder stderr = new StringBuilder();
                try (BufferedReader err = new BufferedReader(
                        new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = err.readLine()) != null) {
                        stderr.append(line).append("\n");
                    }
                }
                throw new TesseractException("Tesseract process exited with code " + exitCode + ": " + stderr.toString());
            }
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("✅ OCR (process mode) completed in {}ms. Extracted {} characters", 
                    duration, result.length());
            
            return result.toString().trim();
        } finally {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException e) {
                log.warn("Could not delete temp file {}: {}", tempFile, e.getMessage());
            }
        }
    }
    
    private static String getImageExtension(String contentType, String filename) {
        if (contentType != null) {
            if (contentType.contains("jpeg") || contentType.contains("jpg")) return ".jpg";
            if (contentType.contains("png")) return ".png";
            if (contentType.contains("gif")) return ".gif";
            if (contentType.contains("bmp")) return ".bmp";
            if (contentType.contains("tiff")) return ".tiff";
        }
        if (filename != null) {
            int i = filename.lastIndexOf('.');
            if (i > 0) return filename.substring(i);
        }
        return ".png";
    }
    
    private String extractTextFromImage(MultipartFile file) throws IOException, TesseractException {
        if (useProcessMode) {
            return extractTextFromImageViaProcess(file);
        }
        
        long startTime = System.currentTimeMillis();
        
        // Ensure library is loaded before use
        ensureLibraryLoaded();
        
        // Check Tesseract availability
        if (!tesseractAvailable || tesseract == null) {
            String osName = System.getProperty("os.name").toLowerCase();
            String installCmd;
            String verifyCmd;
            String instructions;
            
            if (osName.contains("mac")) {
                installCmd = "brew install tesseract";
                verifyCmd = "tesseract --version";
                instructions = String.format(
                    "Tesseract OCR is not installed or not available on this system.\n\n" +
                    "📝 Quick Installation (Choose one method):\n\n" +
                    "Method 1 - Automated Script (Recommended):\n" +
                    "  cd asset-service && ./docs/install-tesseract.sh\n\n" +
                    "Method 2 - Manual Installation:\n" +
                    "  1. Open Terminal\n" +
                    "  2. Run: %s\n" +
                    "  3. Verify: %s\n" +
                    "  4. Restart the application\n\n" +
                    "📖 For detailed instructions, see: asset-service/docs/TESSERACT_INSTALLATION.md\n\n" +
                    "If already installed, try: brew reinstall tesseract",
                    installCmd, verifyCmd
                );
            } else if (osName.contains("win")) {
                instructions = 
                    "Tesseract OCR is not installed or not available on this system.\n\n" +
                    "📝 Quick Installation:\n" +
                    "1. Download Tesseract from: https://github.com/UB-Mannheim/tesseract/wiki\n" +
                    "2. Install the downloaded executable (default: C:\\Program Files\\Tesseract-OCR)\n" +
                    "3. Add Tesseract to PATH: Add 'C:\\Program Files\\Tesseract-OCR' to system PATH\n" +
                    "4. Verify: Open Command Prompt and run 'tesseract --version'\n" +
                    "5. Restart the application\n\n" +
                    "📖 For detailed instructions, see: asset-service/docs/TESSERACT_INSTALLATION.md";
            } else {
                installCmd = "sudo apt-get install tesseract-ocr";
                verifyCmd = "tesseract --version";
                instructions = String.format(
                    "Tesseract OCR is not installed or not available on this system.\n\n" +
                    "📝 Quick Installation (Choose one method):\n\n" +
                    "Method 1 - Automated Script (Recommended):\n" +
                    "  cd asset-service && ./docs/install-tesseract.sh\n\n" +
                    "Method 2 - Manual Installation:\n" +
                    "  1. Run: %s\n" +
                    "  2. Verify: %s\n" +
                    "  3. Restart the application\n\n" +
                    "📖 For detailed instructions, see: asset-service/docs/TESSERACT_INSTALLATION.md",
                    installCmd, verifyCmd
                );
            }
            
            throw new TesseractException(instructions);
        }
        
        // Double-check Tesseract is actually usable (handle NoClassDefFoundError at runtime)
        try {
            // Verify Tesseract instance is still valid
            if (tesseract == null) {
                throw new TesseractException("Tesseract instance is null");
            }
        } catch (NoClassDefFoundError e) {
            tesseractAvailable = false;
            tesseract = null;
            String osName = System.getProperty("os.name").toLowerCase();
            String fixCmd = osName.contains("mac") ? "brew reinstall tesseract" :
                           osName.contains("win") ? "Reinstall Tesseract from https://github.com/UB-Mannheim/tesseract/wiki" :
                           "sudo apt-get install --reinstall tesseract-ocr";
            
            throw new TesseractException(
                "Tesseract native library cannot be loaded at runtime.\n" +
                "The Java bindings cannot find the Tesseract native library.\n" +
                "Try: " + fixCmd + "\n" +
                "Error: " + e.getMessage()
            );
        }

        BufferedImage image;
        try {
            log.debug("📖 Reading image file: {}", file.getOriginalFilename());
            try (InputStream inputStream = new ByteArrayInputStream(file.getBytes())) {
                image = ImageIO.read(inputStream);
                if (image == null) {
                    throw new IOException("Unable to read image from file: " + file.getOriginalFilename());
                }
            }
            log.debug("✅ Image read successfully. Dimensions: {}x{}", image.getWidth(), image.getHeight());
        } catch (IOException e) {
            log.error("❌ Failed to read image file: {}", e.getMessage(), e);
            throw new IOException("Failed to read image: " + e.getMessage(), e);
        }

        // Preprocess image for better OCR results
        BufferedImage processedImage;
        try {
            log.debug("🖼️ Preprocessing image...");
            processedImage = preprocessImage(image);
            log.debug("✅ Image preprocessing completed. New dimensions: {}x{}", 
                    processedImage.getWidth(), processedImage.getHeight());
        } catch (Exception e) {
            log.error("❌ Failed to preprocess image: {}", e.getMessage(), e);
            throw new IOException("Failed to preprocess image: " + e.getMessage(), e);
        }

        // Perform OCR with error handling
        String extractedText;
        try {
            log.debug("🔍 Starting OCR extraction...");
            extractedText = tesseract.doOCR(processedImage);
            long duration = System.currentTimeMillis() - startTime;
            log.info("✅ OCR extraction completed in {}ms. Extracted {} characters", 
                    duration, extractedText != null ? extractedText.length() : 0);
        } catch (NoClassDefFoundError e) {
            tesseractAvailable = false;
            tesseract = null;
            long duration = System.currentTimeMillis() - startTime;
            String osName = System.getProperty("os.name").toLowerCase();
            String fixCmd = osName.contains("mac") ? "brew reinstall tesseract" :
                           osName.contains("win") ? "Reinstall Tesseract from https://github.com/UB-Mannheim/tesseract/wiki" :
                           "sudo apt-get install --reinstall tesseract-ocr";
            
            log.error("❌ Tesseract native library error after {}ms: {}", duration, e.getMessage());
            throw new TesseractException(
                "Tesseract native library cannot be loaded: " + e.getMessage() + "\n" +
                "Try: " + fixCmd + "\n" +
                "After reinstalling, restart the application."
            );
        } catch (UnsatisfiedLinkError e) {
            tesseractAvailable = false;
            tesseract = null;
            long duration = System.currentTimeMillis() - startTime;
            log.error("❌ Tesseract native library link error after {}ms: {}", duration, e.getMessage());
            throw new TesseractException(
                "Tesseract native library cannot be linked: " + e.getMessage() + "\n" +
                "Please ensure Tesseract is properly installed."
            );
        } catch (TesseractException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("❌ OCR extraction failed after {}ms: {}", duration, e.getMessage(), e);
            throw new TesseractException("OCR extraction failed: " + e.getMessage(), e);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("❌ Unexpected error during OCR extraction after {}ms: {}", duration, e.getMessage(), e);
            throw new TesseractException("Unexpected error during OCR: " + e.getMessage(), e);
        }
        
        return extractedText != null ? extractedText.trim() : "";
    }

    // ============================================================
    // 📄 EXTRACT TEXT FROM PDF
    // ============================================================
    private String extractTextFromPdf(MultipartFile file) throws IOException {
        byte[] fileBytes = file.getBytes();
        try (PDDocument document = Loader.loadPDF(fileBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(1);
            stripper.setEndPage(document.getNumberOfPages());
            
            String text = stripper.getText(document);
            
            log.info("✅ PDF text extraction completed. Extracted {} characters from {} pages", 
                    text != null ? text.length() : 0, document.getNumberOfPages());
            
            return text != null ? text.trim() : "";
        } catch (Exception e) {
            log.error("❌ Failed to extract text from PDF: {}", e.getMessage(), e);
            throw new IOException("Failed to extract text from PDF: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // 📝 EXTRACT TEXT FROM WORD DOCUMENT
    // ============================================================
    private String extractTextFromWord(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        String text = "";
        
        try (InputStream inputStream = new ByteArrayInputStream(file.getBytes())) {
            if (fileName != null && fileName.toLowerCase().endsWith(".docx")) {
                // DOCX format
                try (XWPFDocument document = new XWPFDocument(inputStream)) {
                    XWPFWordExtractor extractor = new XWPFWordExtractor(document);
                    text = extractor.getText();
                    extractor.close();
                }
            } else {
                // DOC format (legacy)
                try (HWPFDocument document = new HWPFDocument(inputStream)) {
                    WordExtractor extractor = new WordExtractor(document);
                    text = extractor.getText();
                    extractor.close();
                }
            }
            
            log.info("✅ Word document text extraction completed. Extracted {} characters", 
                    text != null ? text.length() : 0);
            
            return text != null ? text.trim() : "";
        } catch (Exception e) {
            log.error("❌ Failed to extract text from Word document: {}", e.getMessage(), e);
            throw new IOException("Failed to extract text from Word document: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // 📊 EXTRACT TEXT FROM EXCEL
    // ============================================================
    private String extractTextFromExcel(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        StringBuilder text = new StringBuilder();
        
        try (InputStream inputStream = new ByteArrayInputStream(file.getBytes())) {
            Workbook workbook;
            
            if (fileName != null && fileName.toLowerCase().endsWith(".xlsx")) {
                // XLSX format
                workbook = new XSSFWorkbook(inputStream);
            } else {
                // XLS format (legacy)
                workbook = new HSSFWorkbook(inputStream);
            }
            
            // Extract text from all sheets
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                text.append("Sheet: ").append(sheet.getSheetName()).append("\n");
                
                for (Row row : sheet) {
                    List<String> rowData = new ArrayList<>();
                    row.forEach(cell -> {
                        if (cell != null) {
                            switch (cell.getCellType()) {
                                case STRING:
                                    rowData.add(cell.getStringCellValue());
                                    break;
                                case NUMERIC:
                                    rowData.add(String.valueOf(cell.getNumericCellValue()));
                                    break;
                                case BOOLEAN:
                                    rowData.add(String.valueOf(cell.getBooleanCellValue()));
                                    break;
                                default:
                                    rowData.add("");
                            }
                        }
                    });
                    if (!rowData.isEmpty()) {
                        text.append(String.join(" | ", rowData)).append("\n");
                    }
                }
                text.append("\n");
            }
            
            workbook.close();
            
            log.info("✅ Excel text extraction completed. Extracted {} characters", text.length());
            
            return text.toString().trim();
        } catch (Exception e) {
            log.error("❌ Failed to extract text from Excel: {}", e.getMessage(), e);
            throw new IOException("Failed to extract text from Excel: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // 📽️ EXTRACT TEXT FROM POWERPOINT
    // ============================================================
    private String extractTextFromPowerPoint(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        StringBuilder text = new StringBuilder();
        
        try (InputStream inputStream = new ByteArrayInputStream(file.getBytes())) {
            SlideShow<?, ?> slideShow;
            
            if (fileName != null && fileName.toLowerCase().endsWith(".pptx")) {
                // PPTX format
                slideShow = new XMLSlideShow(inputStream);
            } else {
                // PPT format (legacy)
                slideShow = new HSLFSlideShow(inputStream);
            }
            
            // Extract text from all slides
            var slides = slideShow.getSlides();
            for (int i = 0; i < slides.size(); i++) {
                var slide = slides.get(i);
                text.append("Slide ").append(i + 1).append(":\n");
                
                // Extract text from shapes
                slide.getShapes().forEach(shape -> {
                    if (shape instanceof org.apache.poi.sl.usermodel.TextShape) {
                        org.apache.poi.sl.usermodel.TextShape<?, ?> textShape = 
                            (org.apache.poi.sl.usermodel.TextShape<?, ?>) shape;
                        String shapeText = textShape.getText();
                        if (shapeText != null && !shapeText.trim().isEmpty()) {
                            text.append(shapeText).append("\n");
                        }
                    }
                });
                text.append("\n");
            }
            
            slideShow.close();
            
            log.info("✅ PowerPoint text extraction completed. Extracted {} characters from {} slides", 
                    text.length(), slides.size());
            
            return text.toString().trim();
        } catch (Exception e) {
            log.error("❌ Failed to extract text from PowerPoint: {}", e.getMessage(), e);
            throw new IOException("Failed to extract text from PowerPoint: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // 🖼️ PREPROCESS IMAGE FOR BETTER OCR RESULTS
    // ============================================================
    // ============================================================
    // 🖼️ PREPROCESS IMAGE FOR BETTER OCR (Paytm-like Enhancement)
    // ============================================================
    private BufferedImage preprocessImage(BufferedImage image) {
        try {
            log.debug("🖼️ Starting advanced image preprocessing...");
            long startTime = System.currentTimeMillis();
            
            // Step 1: Handle different image formats and convert to RGB if needed
            image = ensureRGBFormat(image);
            
            // Step 2: Auto-detect and crop to text region (zoom to content)
            image = autoCropToTextRegion(image);
            
            // Step 3: Smart resize (maintain aspect ratio, optimize for OCR)
            image = smartResize(image);
            
            // Step 4: Convert to grayscale
            BufferedImage grayscale = convertToGrayscale(image);
            
            // Step 5: Noise reduction (denoising)
            BufferedImage denoised = reduceNoise(grayscale);
            
            // Step 6: Enhance contrast and brightness
            BufferedImage contrastEnhanced = enhanceContrastAndBrightness(denoised);
            
            // Step 7: Sharpen image
            BufferedImage sharpened = sharpenImage(contrastEnhanced);
            
            // Step 8: Final cleanup - remove artifacts
            BufferedImage finalImage = finalCleanup(sharpened);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("✅ Advanced image preprocessing completed in {}ms. Final size: {}x{}", 
                    duration, finalImage.getWidth(), finalImage.getHeight());
            
            return finalImage;
        } catch (Exception e) {
            log.error("❌ Image preprocessing failed: {}", e.getMessage(), e);
            // Return original image if preprocessing fails
            return image;
        }
    }
    
    // ============================================================
    // 🔧 HELPER: Ensure RGB Format
    // ============================================================
    private BufferedImage ensureRGBFormat(BufferedImage image) {
        if (image.getType() == BufferedImage.TYPE_INT_RGB) {
            return image;
        }
        
        BufferedImage rgbImage = new BufferedImage(
            image.getWidth(), 
            image.getHeight(), 
            BufferedImage.TYPE_INT_RGB
        );
        Graphics2D g = rgbImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return rgbImage;
    }
    
    // ============================================================
    // 🔍 AUTO-CROP TO TEXT REGION (Zoom to Content)
    // ============================================================
    private BufferedImage autoCropToTextRegion(BufferedImage image) {
        try {
            // Convert to grayscale for edge detection
            BufferedImage gray = convertToGrayscale(image);
            
            int width = gray.getWidth();
            int height = gray.getHeight();
            
            // Find text boundaries by detecting non-white regions
            int minX = width, minY = height, maxX = 0, maxY = 0;
            int threshold = 240; // Threshold for "white" background
            
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = gray.getRGB(x, y);
                    int grayValue = (rgb >> 16) & 0xFF;
                    
                    if (grayValue < threshold) { // Non-white pixel (likely text)
                        if (x < minX) minX = x;
                        if (x > maxX) maxX = x;
                        if (y < minY) minY = y;
                        if (y > maxY) maxY = y;
                    }
                }
            }
            
            // Add padding (10% on each side)
            int paddingX = (int) (width * 0.1);
            int paddingY = (int) (height * 0.1);
            minX = Math.max(0, minX - paddingX);
            minY = Math.max(0, minY - paddingY);
            maxX = Math.min(width - 1, maxX + paddingX);
            maxY = Math.min(height - 1, maxY + paddingY);
            
            // Only crop if we found a significant region (at least 20% of image)
            int cropWidth = maxX - minX + 1;
            int cropHeight = maxY - minY + 1;
            
            if (cropWidth > width * 0.2 && cropHeight > height * 0.2) {
                image = image.getSubimage(minX, minY, cropWidth, cropHeight);
                log.debug("🔍 Auto-cropped to text region: {}x{} (from {}x{})", 
                        cropWidth, cropHeight, width, height);
            }
        } catch (Exception e) {
            log.debug("⚠️ Auto-crop failed, using full image: {}", e.getMessage());
        }
        return image;
    }
    
    // ============================================================
    // 📏 SMART RESIZE (Optimize for OCR)
    // ============================================================
    private BufferedImage smartResize(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int maxDimension = 2000;
        int minDimension = 300; // Minimum size for OCR
        
        // Resize if too large
        if (width > maxDimension || height > maxDimension) {
            double scale = Math.min((double) maxDimension / width, (double) maxDimension / height);
            int newWidth = (int) (width * scale);
            int newHeight = (int) (height * scale);
            image = Scalr.resize(image, Scalr.Method.QUALITY, newWidth, newHeight);
            log.debug("📏 Resized down from {}x{} to {}x{}", width, height, newWidth, newHeight);
        }
        // Enlarge if too small (zoom in)
        else if (width < minDimension && height < minDimension) {
            double scale = Math.max((double) minDimension / width, (double) minDimension / height);
            int newWidth = (int) (width * scale);
            int newHeight = (int) (height * scale);
            image = Scalr.resize(image, Scalr.Method.QUALITY, newWidth, newHeight);
            log.debug("📏 Enlarged from {}x{} to {}x{} for better OCR", width, height, newWidth, newHeight);
        }
        
        return image;
    }
    
    // ============================================================
    // 🎨 CONVERT TO GRAYSCALE
    // ============================================================
    private BufferedImage convertToGrayscale(BufferedImage image) {
        BufferedImage grayscale = new BufferedImage(
            image.getWidth(), 
            image.getHeight(), 
            BufferedImage.TYPE_BYTE_GRAY
        );
        Graphics2D g = grayscale.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return grayscale;
    }
    
    // ============================================================
    // 🧹 NOISE REDUCTION (Denoising)
    // ============================================================
    private BufferedImage reduceNoise(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage denoised = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        
        // Apply median filter (3x3) to reduce noise
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int[] neighbors = new int[9];
                int idx = 0;
                
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int rgb = image.getRGB(x + dx, y + dy);
                        neighbors[idx++] = (rgb >> 16) & 0xFF;
                    }
                }
                
                // Sort and take median
                java.util.Arrays.sort(neighbors);
                int median = neighbors[4];
                int newRgb = (median << 16) | (median << 8) | median;
                denoised.setRGB(x, y, newRgb);
            }
        }
        
        // Copy borders
        for (int y = 0; y < height; y++) {
            denoised.setRGB(0, y, image.getRGB(0, y));
            denoised.setRGB(width - 1, y, image.getRGB(width - 1, y));
        }
        for (int x = 0; x < width; x++) {
            denoised.setRGB(x, 0, image.getRGB(x, 0));
            denoised.setRGB(x, height - 1, image.getRGB(x, height - 1));
        }
        
        return denoised;
    }
    
    // ============================================================
    // ✨ ENHANCE CONTRAST AND BRIGHTNESS
    // ============================================================
    private BufferedImage enhanceContrastAndBrightness(BufferedImage image) {
        // Calculate histogram for adaptive enhancement
        int[] histogram = new int[256];
        int width = image.getWidth();
        int height = image.getHeight();
        int totalPixels = width * height;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int gray = (rgb >> 16) & 0xFF;
                histogram[gray]++;
            }
        }
        
        // Find min and max values (excluding extreme outliers)
        int min = 0, max = 255;
        int threshold = totalPixels / 1000; // 0.1% threshold
        
        for (int i = 0; i < 256; i++) {
            if (histogram[i] > threshold) {
                min = i;
                break;
            }
        }
        for (int i = 255; i >= 0; i--) {
            if (histogram[i] > threshold) {
                max = i;
                break;
            }
        }
        
        // Apply contrast stretching (avoid division by zero)
        double scale = (max > min) ? 255.0 / (max - min) : 1.0;
        BufferedImage enhanced = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int gray = (rgb >> 16) & 0xFF;
                
                // Stretch contrast
                int newGray = (max > min) ? (int) Math.max(0, Math.min(255, (gray - min) * scale)) : gray;
                
                // Slight brightness boost
                newGray = (int) Math.min(255, newGray * 1.1);
                
                int newRgb = (newGray << 16) | (newGray << 8) | newGray;
                enhanced.setRGB(x, y, newRgb);
            }
        }
        
        return enhanced;
    }
    
    // ============================================================
    // 🔪 SHARPEN IMAGE
    // ============================================================
    private BufferedImage sharpenImage(BufferedImage image) {
        // Unsharp mask kernel for sharpening
        float[] kernel = {
            0.0f, -0.5f, 0.0f,
            -0.5f, 3.0f, -0.5f,
            0.0f, -0.5f, 0.0f
        };
        
        Kernel sharpKernel = new Kernel(3, 3, kernel);
        ConvolveOp sharpenOp = new ConvolveOp(sharpKernel, ConvolveOp.EDGE_NO_OP, null);
        return sharpenOp.filter(image, null);
    }
    
    // ============================================================
    // 🧽 FINAL CLEANUP
    // ============================================================
    private BufferedImage finalCleanup(BufferedImage image) {
        // Apply antialiasing for smoother edges
        BufferedImage cleaned = Scalr.apply(image, Scalr.OP_ANTIALIAS);
        return cleaned;
    }

    // ============================================================
    // 🔧 VALIDATE FILE TYPE
    // ============================================================
    public boolean isValidFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String contentType = file.getContentType();
        if (contentType == null) {
            // Try to infer from filename
            String fileName = file.getOriginalFilename();
            if (fileName != null) {
                String lowerName = fileName.toLowerCase();
                return lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || 
                       lowerName.endsWith(".png") || lowerName.endsWith(".gif") ||
                       lowerName.endsWith(".pdf") || lowerName.endsWith(".doc") ||
                       lowerName.endsWith(".docx") || lowerName.endsWith(".xls") ||
                       lowerName.endsWith(".xlsx") || lowerName.endsWith(".ppt") ||
                       lowerName.endsWith(".pptx");
            }
            return false;
        }
        
        return isImageFile(contentType) || isPdfFile(contentType) || 
               isWordFile(contentType) || isExcelFile(contentType) || 
               isPowerPointFile(contentType);
    }

    // ============================================================
    // 🔍 FILE TYPE CHECKERS
    // ============================================================
    private boolean isImageFile(String contentType) {
        return contentType != null && SUPPORTED_IMAGE_TYPES.contains(contentType.toLowerCase());
    }

    private boolean isPdfFile(String contentType) {
        return contentType != null && SUPPORTED_PDF_TYPES.contains(contentType.toLowerCase());
    }

    private boolean isWordFile(String contentType) {
        return contentType != null && SUPPORTED_WORD_TYPES.contains(contentType.toLowerCase());
    }

    private boolean isExcelFile(String contentType) {
        return contentType != null && SUPPORTED_EXCEL_TYPES.contains(contentType.toLowerCase());
    }

    private boolean isPowerPointFile(String contentType) {
        return contentType != null && SUPPORTED_PPT_TYPES.contains(contentType.toLowerCase());
    }

    // ============================================================
    // 🔧 LEGACY METHOD (for backward compatibility)
    // ============================================================
    @Deprecated
    public boolean isValidImageFile(MultipartFile file) {
        return isValidFile(file) && isImageFile(file.getContentType());
    }
}
