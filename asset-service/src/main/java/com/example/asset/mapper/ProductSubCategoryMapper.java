
package com.example.asset.mapper;

import com.example.asset.dto.ProductSubCategoryDto;
import com.example.asset.entity.ProductSubCategory;

public class ProductSubCategoryMapper {

    public static ProductSubCategoryDto toDto(ProductSubCategory entity) {
        if (entity == null) return null;

        ProductSubCategoryDto dto = new ProductSubCategoryDto();
        dto.setSubCategoryId(entity.getSubCategoryId());
        dto.setSubCategoryName(entity.getSubCategoryName());
        dto.setDescription(entity.getDescription());
        dto.setSequenceOrder(entity.getSequenceOrder());
        dto.setIsFavourite(entity.getIsFavourite());
        dto.setIsMostLike(entity.getIsMostLike());
        dto.setActive(entity.getActive());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getCategory() != null) {
            dto.setCategoryId(entity.getCategory().getCategoryId());
            dto.setCategoryName(entity.getCategory().getCategoryName());
        }

        return dto;
    }
}


