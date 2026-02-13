package com.example.asset.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
        try {
            // Try to create Tesseract instance - this will throw UnsatisfiedLinkError if not installed
            this.tesseract = new Tesseract();
            
            // Set Tesseract data path (adjust based on your installation)
            String tessDataPath = System.getenv("TESSDATA_PREFIX");
            if (tessDataPath == null || tessDataPath.isEmpty()) {
                // Default paths (adjust based on your system)
                String osName = System.getProperty("os.name").toLowerCase();
                if (osName.contains("win")) {
                    tessDataPath = "C:\\Program Files\\Tesseract-OCR\\tessdata";
                } else if (osName.contains("mac")) {
                    // Try multiple common macOS paths
                    String[] macPaths = {
                        "/opt/homebrew/share/tessdata",  // Homebrew (Apple Silicon)
                        "/usr/local/share/tessdata",      // Homebrew (Intel)
                        "/opt/local/share/tessdata"       // MacPorts
                    };
                    for (String path : macPaths) {
                        java.io.File pathFile = new java.io.File(path);
                        if (pathFile.exists() && pathFile.isDirectory()) {
                            tessDataPath = path;
                            break;
                        }
                    }
                    if (tessDataPath == null || tessDataPath.isEmpty()) {
                        tessDataPath = "/opt/homebrew/share/tessdata"; // Default for Homebrew on Apple Silicon
                    }
                } else {
                    tessDataPath = "/usr/share/tesseract-ocr/5/tessdata";
                }
            }
            
            tesseract.setDatapath(tessDataPath);
            tesseract.setLanguage("eng");
            tesseract.setPageSegMode(1); // Automatic page segmentation with OSD
            tesseract.setOcrEngineMode(1); // Neural nets LSTM engine only
            
            // Test Tesseract availability immediately
            tesseractAvailable = testTesseractAvailability();
            
            if (tesseractAvailable) {
                log.info("✅ Tesseract OCR initialized and verified. Data path: {}", tessDataPath);
            } else {
                log.warn("⚠️ Tesseract OCR initialized but not available. Image OCR will be disabled.");
                logInstallationInstructions();
            }
        } catch (UnsatisfiedLinkError e) {
            tesseractAvailable = false;
            tesseract = null;
            String osName = System.getProperty("os.name").toLowerCase();
            String installCmd = osName.contains("mac") ? "brew install tesseract" :
                               osName.contains("win") ? "Download from https://github.com/UB-Mannheim/tesseract/wiki" :
                               "sudo apt-get install tesseract-ocr";
            
            log.error("❌ Tesseract OCR library not found on this system.");
            log.error("📝 To install Tesseract, run: {}", installCmd);
            log.error("   After installation, restart the application.");
            log.error("   Error details: {}", e.getMessage());
        } catch (NoClassDefFoundError e) {
            tesseractAvailable = false;
            tesseract = null;
            log.error("❌ Tesseract OCR Java bindings cannot be initialized: {}", e.getMessage());
            log.error("📝 This usually means the native Tesseract library is not accessible.");
            log.error("   Try: brew reinstall tesseract (macOS)");
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
        try {
            // Create a minimal test image (1x1 pixel white image)
            BufferedImage testImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
            testImage.setRGB(0, 0, 0xFFFFFF); // White pixel
            
            // Try to run OCR on the test image
            // This will fail if the native library can't be loaded
            try {
                tesseract.doOCR(testImage);
                return true; // Tesseract is working
            } catch (NoClassDefFoundError | UnsatisfiedLinkError e) {
                log.error("❌ Tesseract native library test failed: {}", e.getMessage());
                return false;
            } catch (TesseractException e) {
                // TesseractException is fine - it means Tesseract is working but couldn't extract text from 1x1 image
                // This is expected and means Tesseract is functional
                return true;
            }
        } catch (NoClassDefFoundError | UnsatisfiedLinkError e) {
            log.error("❌ Tesseract native library cannot be loaded: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.warn("⚠️ Tesseract availability test had issues: {}", e.getMessage());
            // Assume it might work, but mark as uncertain
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
        return tesseractAvailable && tesseract != null;
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
    private String extractTextFromImage(MultipartFile file) throws IOException, TesseractException {
        long startTime = System.currentTimeMillis();
        
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
