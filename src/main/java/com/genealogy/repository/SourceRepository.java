package com.genealogy.repository;

import com.genealogy.entity.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SourceRepository extends JpaRepository<Source, Long> {
    
    List<Source> findByGenealogyId(Long genealogyId);
}
