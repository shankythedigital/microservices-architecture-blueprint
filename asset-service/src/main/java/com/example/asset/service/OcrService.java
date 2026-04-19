package com.example.asset.service;

import jakarta.annotation.PostConstruct;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.example.asset.config.TesseractConfig;

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

    private boolean tesseractAvailable = false;
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
    }

    @PostConstruct
    public void initializeTesseract() {
        log.info("Initializing Tesseract OCR (external process only; no tess4j/JNA in classpath)...");
        log.info("   TESSDATA_PREFIX: {}", System.getenv("TESSDATA_PREFIX"));

        if (tesseractConfig != null && !tesseractConfig.isEnabled()) {
            log.info("Tesseract OCR is disabled in configuration.");
            tesseractAvailable = false;
            return;
        }

        if (tesseractConfig != null && !tesseractConfig.isUseProcess()) {
            log.warn("tesseract.use-process=false is ignored: this build uses the external tesseract CLI only (tess4j removed to reduce JAR size). Set use-process: true.");
        }

        tesseractExecutablePath = resolveTesseractExecutable();
        tesseractAvailable = tesseractExecutablePath != null;
        if (tesseractAvailable) {
            log.info("Tesseract executable resolved: {}", tesseractExecutablePath);
            if (!verifyTesseractProcess()) {
                log.warn("Tesseract binary present but smoke test failed; image OCR may still work at runtime.");
            }
        } else {
            log.warn("Tesseract executable not found. Image OCR disabled. Install tesseract or set tesseract.executable-path.");
            logInstallationInstructions();
        }
    }

    /** Quick check: tesseract --version */
    private boolean verifyTesseractProcess() {
        try {
            ProcessBuilder pb = new ProcessBuilder(tesseractExecutablePath, "--version");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            int code = p.waitFor();
            return code == 0;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.debug("tesseract --version failed: {}", e.getMessage());
            return false;
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
        return tesseractAvailable && tesseractExecutablePath != null;
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
    public String extractText(MultipartFile file) throws IOException, OcrException {
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
    private String extractTextFromImageViaProcess(MultipartFile file) throws IOException, OcrException {
        long startTime = System.currentTimeMillis();
        
        if (!tesseractAvailable || tesseractExecutablePath == null) {
            throw new OcrException(
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
                throw new OcrException(
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
                throw new OcrException("OCR process was interrupted", e);
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
                throw new OcrException("Tesseract process exited with code " + exitCode + ": " + stderr.toString());
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
    
    private String extractTextFromImage(MultipartFile file) throws IOException, OcrException {
        return extractTextFromImageViaProcess(file);
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
