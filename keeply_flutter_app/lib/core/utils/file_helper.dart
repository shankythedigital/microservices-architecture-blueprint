import 'dart:io';
import 'package:file_picker/file_picker.dart';
import 'package:image_picker/image_picker.dart';
import 'package:keeply_app/core/config/app_config.dart';
import 'package:keeply_app/core/utils/logger.dart';
import 'package:keeply_app/core/utils/edge_case_handler.dart';

/// File Helper
/// Handles file operations with comprehensive validation
class FileHelper {
  static final FileHelper _instance = FileHelper._internal();
  factory FileHelper() => _instance;
  FileHelper._internal();

  final ImagePicker _imagePicker = ImagePicker();

  /// Pick image from gallery
  Future<File?> pickImageFromGallery() async {
    try {
      final XFile? image = await _imagePicker.pickImage(
        source: ImageSource.gallery,
        imageQuality: 85,
        maxWidth: 1920,
        maxHeight: 1080,
      );

      if (image == null) return null;

      final file = File(image.path);

      // Edge case: Validate file size
      final size = await file.length();
      final sizeError = EdgeCaseHandler.validateFileSize(size);
      if (sizeError != null) {
        throw Exception(sizeError);
      }

      // Edge case: Validate file type
      final typeError = EdgeCaseHandler.validateFileType(
        image.path,
        ['jpg', 'jpeg', 'png'],
      );
      if (typeError != null) {
        throw Exception(typeError);
      }

      return file;
    } catch (e) {
      AppLogger.error('Pick image failed: $e');
      rethrow;
    }
  }

  /// Pick image from camera
  Future<File?> pickImageFromCamera() async {
    try {
      final XFile? image = await _imagePicker.pickImage(
        source: ImageSource.camera,
        imageQuality: 85,
        maxWidth: 1920,
        maxHeight: 1080,
      );

      if (image == null) return null;

      final file = File(image.path);

      // Edge case: Validate file size
      final size = await file.length();
      final sizeError = EdgeCaseHandler.validateFileSize(size);
      if (sizeError != null) {
        throw Exception(sizeError);
      }

      return file;
    } catch (e) {
      AppLogger.error('Pick image from camera failed: $e');
      rethrow;
    }
  }

  /// Pick document file
  Future<File?> pickDocument({
    List<String>? allowedExtensions,
  }) async {
    try {
      final result = await FilePicker.platform.pickFiles(
        type: FileType.custom,
        allowedExtensions: allowedExtensions ??
            ['pdf', 'doc', 'docx', 'xls', 'xlsx'],
        withData: false,
        withReadStream: false,
      );

      if (result == null || result.files.single.path == null) {
        return null;
      }

      final file = File(result.files.single.path!);

      // Edge case: Validate file size
      final size = await file.length();
      final sizeError = EdgeCaseHandler.validateFileSize(size);
      if (sizeError != null) {
        throw Exception(sizeError);
      }

      // Edge case: Validate file type
      if (allowedExtensions != null) {
        final fileName = result.files.single.name;
        final typeError = EdgeCaseHandler.validateFileType(
          fileName,
          allowedExtensions,
        );
        if (typeError != null) {
          throw Exception(typeError);
        }
      }

      return file;
    } catch (e) {
      AppLogger.error('Pick document failed: $e');
      rethrow;
    }
  }

  /// Get file size in human-readable format
  String getFileSizeString(int bytes) {
    if (bytes < 1024) {
      return '$bytes B';
    } else if (bytes < 1024 * 1024) {
      return '${(bytes / 1024).toStringAsFixed(2)} KB';
    } else {
      return '${(bytes / (1024 * 1024)).toStringAsFixed(2)} MB';
    }
  }

  /// Validate file before upload
  Future<String?> validateFileForUpload(File file) async {
    try {
      // Edge case: Check if file exists
      if (!await file.exists()) {
        return 'File does not exist';
      }

      // Edge case: Validate file size
      final size = await file.length();
      final sizeError = EdgeCaseHandler.validateFileSize(size);
      if (sizeError != null) {
        return sizeError;
      }

      // Edge case: Check file is readable
      try {
        await file.readAsBytes();
      } catch (e) {
        return 'File is not readable';
      }

      return null;
    } catch (e) {
      AppLogger.error('File validation failed: $e');
      return 'File validation failed: ${e.toString()}';
    }
  }
}

