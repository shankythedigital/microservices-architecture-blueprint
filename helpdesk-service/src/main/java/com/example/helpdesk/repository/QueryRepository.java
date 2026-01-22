package com.example.helpdesk.repository;

import com.example.helpdesk.entity.Query;
import com.example.helpdesk.enums.QueryStatus;
import com.example.helpdesk.enums.RelatedService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QueryRepository extends JpaRepository<Query, Long> {
    List<Query> findByStatus(QueryStatus status);
    List<Query> findByRelatedService(RelatedService relatedService);
    List<Query> findByAskedBy(String askedBy);
    List<Query> findByStatusAndRelatedService(QueryStatus status, RelatedService relatedService);
}

