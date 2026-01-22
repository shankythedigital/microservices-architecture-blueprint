package com.example.helpdesk.repository;

import com.example.helpdesk.entity.ServiceKnowledge;
import com.example.helpdesk.enums.RelatedService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceKnowledgeRepository extends JpaRepository<ServiceKnowledge, Long> {
    List<ServiceKnowledge> findByService(RelatedService service);
    List<ServiceKnowledge> findByServiceAndCategory(RelatedService service, String category);
    
    @Query("SELECT s FROM ServiceKnowledge s WHERE s.service = :service AND (s.topic LIKE %:keyword% OR s.content LIKE %:keyword%)")
    List<ServiceKnowledge> searchByServiceAndKeyword(@Param("service") RelatedService service, @Param("keyword") String keyword);
}

