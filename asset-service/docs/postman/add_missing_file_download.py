#!/usr/bin/env python3
"""Add FileDownloadController endpoints if missing"""

import json
import sys

def add_file_download_controller(collection):
    """Add FileDownloadController endpoints"""
    
    # Check if FileDownload folder exists
    has_file_download = False
    for item in collection.get('item', []):
        if 'FileDownload' in item.get('name', '') or 'File Download' in item.get('name', ''):
            has_file_download = True
            break
    
    if has_file_download:
        print("  FileDownloadController already exists")
        return collection
    
    # Find Document folder to insert after
    insert_index = None
    for i, item in enumerate(collection.get('item', [])):
        if 'Document' in item.get('name', ''):
            insert_index = i + 1
            break
    
    if insert_index is None:
        insert_index = len(collection.get('item', []))
    
    # Create FileDownload folder
    file_download_folder = {
        "name": "13. FileDownload",
        "item": [
            {
                "name": "Download or View File",
                "request": {
                    "method": "GET",
                    "header": [
                        {
                            "key": "Authorization",
                            "value": "Bearer {{accessToken}}",
                            "type": "text",
                            "description": "JWT Bearer token from auth-service"
                        }
                    ],
                    "url": {
                        "raw": "{{assetbaseUrl}}/api/asset/v1/files/download?filename={{filename}}&inline=false",
                        "host": ["{{assetbaseUrl}}"],
                        "path": ["api", "asset", "v1", "files", "download"],
                        "query": [
                            {
                                "key": "filename",
                                "value": "{{filename}}",
                                "description": "File name to download"
                            },
                            {
                                "key": "inline",
                                "value": "false",
                                "description": "true for inline view, false for download"
                            }
                        ]
                    },
                    "description": "Download or view a file by filename.\n\n**Query Parameters:**\n- filename: File name to download (required)\n- inline: true for inline view, false for download (default: false)\n\n**Controller:** FileDownloadController\n**Base Path:** /api/asset/v1/files"
                }
            }
        ]
    }
    
    # Insert the folder
    collection['item'].insert(insert_index, file_download_folder)
    
    # Renumber subsequent folders
    for i in range(insert_index + 1, len(collection['item'])):
        folder_name = collection['item'][i].get('name', '')
        # Extract number and controller name
        if folder_name and '. ' in folder_name:
            parts = folder_name.split('. ', 1)
            if parts[0].isdigit():
                new_num = int(parts[0]) + 1
                collection['item'][i]['name'] = f"{new_num}. {parts[1]}"
    
    print(f"  Added FileDownloadController at position {insert_index + 1}")
    return collection

if __name__ == "__main__":
    try:
        print("🔍 Checking for FileDownloadController...")
        
        with open('Asset_Service_Consolidated.postman_collection.json', 'r') as f:
            collection = json.load(f)
        
        collection = add_file_download_controller(collection)
        
        with open('Asset_Service_Consolidated.postman_collection.json', 'w') as f:
            json.dump(collection, f, indent=2)
        
        print("✅ Collection updated!")
        print(f"   Total folders: {len(collection['item'])}")
        
    except Exception as e:
        print(f"❌ Error: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc()
        sys.exit(1)

