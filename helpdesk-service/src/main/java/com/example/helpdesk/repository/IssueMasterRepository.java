package com.example.helpdesk.repository;

import com.example.helpdesk.entity.IssueMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueMasterRepository extends JpaRepository<IssueMaster, Long> {
    List<IssueMaster> findByCategoryId(Long categoryId);
    List<IssueMaster> findBySubCategoryId(Long subCategoryId);
    List<IssueMaster> findByComponentId(Long componentId);
    List<IssueMaster> findBySparePartId(Long sparePartId);
    List<IssueMaster> findByCategoryIdAndActiveTrue(Long categoryId);
    List<IssueMaster> findBySubCategoryIdAndActiveTrue(Long subCategoryId);
    List<IssueMaster> findByComponentIdAndActiveTrue(Long componentId);
}
