package com.genealogy.repository;

import com.genealogy.entity.Discussion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiscussionRepository extends JpaRepository<Discussion, Long> {

    Page<Discussion> findByType(String type, Pageable pageable);

    Page<Discussion> findByStatus(String status, Pageable pageable);

    Page<Discussion> findByTypeAndStatus(String type, String status, Pageable pageable);

    List<Discussion> findTop10ByTypeAndStatusOrderByCreatedAtDesc(String type, String status);
}
