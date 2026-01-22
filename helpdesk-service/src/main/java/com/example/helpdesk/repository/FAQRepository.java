package com.example.helpdesk.repository;

import com.example.helpdesk.entity.FAQ;
import com.example.helpdesk.enums.RelatedService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FAQRepository extends JpaRepository<FAQ, Long> {
    List<FAQ> findByRelatedService(RelatedService relatedService);
    List<FAQ> findByCategory(String category);
    List<FAQ> findByRelatedServiceAndCategory(RelatedService relatedService, String category);
    
    @Query("SELECT f FROM FAQ f WHERE f.question LIKE %:keyword% OR f.answer LIKE %:keyword%")
    List<FAQ> searchByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT f FROM FAQ f WHERE f.relatedService = :service AND (f.question LIKE %:keyword% OR f.answer LIKE %:keyword%)")
    List<FAQ> searchByServiceAndKeyword(@Param("service") RelatedService service, @Param("keyword") String keyword);
}

