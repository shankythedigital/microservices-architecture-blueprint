package com.example.asset.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * ✅ BulkUploadResponse DTO
 * Response wrapper for bulk upload operations.
 * Contains success/failure counts and detailed results for each item.
 */
public class BulkUploadResponse<T> {
    
    private int totalCount;
    private int successCount;
    private int failureCount;
    private List<BulkItemResult<T>> results;
    
    public BulkUploadResponse() {
        this.results = new ArrayList<>();
    }
    
    public BulkUploadResponse(int totalCount, int successCount, int failureCount, List<BulkItemResult<T>> results) {
        this.totalCount = totalCount;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.results = results != null ? results : new ArrayList<>();
    }
    
    // ============================================================
    // 🧾 Getters and Setters
    // ============================================================
    public int getTotalCount() {
        return totalCount;
    }
    
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
    
    public int getSuccessCount() {
        return successCount;
    }
    
    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }
    
    public int getFailureCount() {
        return failureCount;
    }
    
    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }
    
    public List<BulkItemResult<T>> getResults() {
        return results;
    }
    
    public void setResults(List<BulkItemResult<T>> results) {
        this.results = results;
    }
    
    // ============================================================
    // 🧩 Helper Methods
    // ============================================================
    public void addSuccess(int index, T item) {
        this.results.add(new BulkItemResult<>(index, true, null, item));
        this.successCount++;
    }
    
    public void addFailure(int index, String errorMessage) {
        this.results.add(new BulkItemResult<>(index, false, errorMessage, null));
        this.failureCount++;
    }
    
    /**
     * ✅ BulkItemResult
     * Represents the result of processing a single item in a bulk operation.
     */
    public static class BulkItemResult<T> {
        private int index;
        private boolean success;
        private String errorMessage;
        private T item;
        
        public BulkItemResult() {}
        
        public BulkItemResult(int index, boolean success, String errorMessage, T item) {
            this.index = index;
            this.success = success;
            this.errorMessage = errorMessage;
            this.item = item;
        }
        
        public int getIndex() {
            return index;
        }
        
        public void setIndex(int index) {
            this.index = index;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
        
        public T getItem() {
            return item;
        }
        
        public void setItem(T item) {
            this.item = item;
        }
    }
}
