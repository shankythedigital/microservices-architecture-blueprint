import 'dart:io';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:keeply_app/core/utils/file_helper.dart';
import 'package:keeply_app/core/utils/permission_helper.dart';
import 'package:keeply_app/core/widgets/confirm_dialog.dart';

/// Image Picker Widget
/// Handles image selection with permission and validation
class ImagePickerWidget extends StatelessWidget {
  final Function(File) onImageSelected;
  final String? currentImagePath;
  final bool showRemoveOption;

  const ImagePickerWidget({
    super.key,
    required this.onImageSelected,
    this.currentImagePath,
    this.showRemoveOption = true,
  });

  Future<void> _pickImage(BuildContext context, ImageSource source) async {
    try {
      // Edge case: Request permission for camera
      if (source == ImageSource.camera) {
        final hasPermission = await PermissionHelper.requestCameraPermission();
        if (!hasPermission) {
          if (context.mounted) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(
                content: Text('Camera permission is required'),
              ),
            );
          }
          return;
        }
      }

      // Edge case: Request permission for gallery
      if (source == ImageSource.gallery) {
        final hasPermission = await PermissionHelper.requestPhotosPermission();
        if (!hasPermission) {
          if (context.mounted) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(
                content: Text('Photo library permission is required'),
              ),
            );
          }
          return;
        }
      }

      File? image;
      if (source == ImageSource.camera) {
        image = await FileHelper().pickImageFromCamera();
      } else {
        image = await FileHelper().pickImageFromGallery();
      }

      if (image != null && context.mounted) {
        onImageSelected(image);
      }
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Failed to pick image: ${e.toString()}'),
            backgroundColor: Colors.red,
          ),
        );
      }
    }
  }

  void _showImageSourceDialog(BuildContext context) {
    showModalBottomSheet(
      context: context,
      builder: (context) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ListTile(
              leading: const Icon(Icons.camera_alt),
              title: const Text('Camera'),
              onTap: () {
                Navigator.pop(context);
                _pickImage(context, ImageSource.camera);
              },
            ),
            ListTile(
              leading: const Icon(Icons.photo_library),
              title: const Text('Gallery'),
              onTap: () {
                Navigator.pop(context);
                _pickImage(context, ImageSource.gallery);
              },
            ),
            if (showRemoveOption && currentImagePath != null)
              ListTile(
                leading: const Icon(Icons.delete, color: Colors.red),
                title: const Text('Remove Image', style: TextStyle(color: Colors.red)),
                onTap: () {
                  Navigator.pop(context);
                  ConfirmDialog.show(
                    context,
                    title: 'Remove Image',
                    message: 'Are you sure you want to remove this image?',
                    confirmText: 'Remove',
                    confirmColor: Colors.red,
                  ).then((confirmed) {
                    if (confirmed == true) {
                      onImageSelected(File(''));
                    }
                  });
                },
              ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: () => _showImageSourceDialog(context),
      child: Container(
        width: 120,
        height: 120,
        decoration: BoxDecoration(
          border: Border.all(color: Colors.grey),
          borderRadius: BorderRadius.circular(8),
        ),
        child: currentImagePath != null && currentImagePath!.isNotEmpty
            ? ClipRRect(
                borderRadius: BorderRadius.circular(8),
                child: Image.file(
                  File(currentImagePath!),
                  fit: BoxFit.cover,
                ),
              )
            : const Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(Icons.add_photo_alternate, size: 40),
                  SizedBox(height: 8),
                  Text('Add Image'),
                ],
              ),
      ),
    );
  }
}

