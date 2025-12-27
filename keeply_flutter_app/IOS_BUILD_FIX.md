# iOS Build Fix Guide

## Issues Fixed

### 1. ✅ Added Required Permissions to Info.plist
Added the following permission descriptions:
- `NSPhotoLibraryUsageDescription` - For accessing photo library
- `NSPhotoLibraryAddUsageDescription` - For saving photos
- `NSCameraUsageDescription` - For camera access
- `NSFaceIDUsageDescription` - For Face ID authentication
- `NSLocalNetworkUsageDescription` - For network access

### 2. ✅ Updated Podfile Configuration
- Set minimum iOS deployment target to 13.0
- Added build settings to disable bitcode
- Configured proper deployment target for all pods

## Quick Fix Commands

### Option 1: Use the Fix Script
```bash
cd keeply_flutter_app
./ios_fix.sh
```

### Option 2: Manual Fix
```bash
cd keeply_flutter_app/ios

# Clean previous builds
rm -rf Pods Podfile.lock build

# Reinstall pods
pod deintegrate
pod install --repo-update

# Go back and run
cd ..
flutter clean
flutter pub get
flutter run
```

## Common iOS Build Issues & Solutions

### Issue: "Could not build the application for the simulator"

**Solution 1: Clean and Rebuild**
```bash
flutter clean
cd ios
rm -rf Pods Podfile.lock
pod install
cd ..
flutter pub get
flutter run
```

**Solution 2: Xcode Signing**
1. Open Xcode: `open ios/Runner.xcworkspace`
2. Select Runner target
3. Go to "Signing & Capabilities"
4. Select your development team
5. Build from Xcode or run `flutter run`

**Solution 3: Update CocoaPods**
```bash
sudo gem install cocoapods
cd ios
pod repo update
pod install
```

### Issue: "No such module 'Flutter'"

**Solution:**
```bash
flutter clean
flutter pub get
cd ios
pod install
cd ..
flutter run
```

### Issue: "Command PhaseScriptExecution failed"

**Solution:**
```bash
cd ios
rm -rf Pods Podfile.lock
pod cache clean --all
pod install
cd ..
flutter clean
flutter pub get
```

## Verification

After applying fixes, verify:
1. ✅ Info.plist has all required permissions
2. ✅ Podfile has correct iOS version (13.0+)
3. ✅ CocoaPods dependencies are installed
4. ✅ Xcode signing is configured

## Next Steps

1. **Run the fix script:**
   ```bash
   ./ios_fix.sh
   ```

2. **Or manually fix:**
   ```bash
   cd ios
   pod install
   cd ..
   flutter run
   ```

3. **If still having issues, open in Xcode:**
   ```bash
   open ios/Runner.xcworkspace
   ```
   Then build and run from Xcode to see detailed error messages.

## Notes

- Minimum iOS version: 13.0
- Requires Xcode 12.0 or later
- Requires CocoaPods installed
- Development team must be configured in Xcode for signing

