package com.genealogy.repository;

import com.genealogy.entity.AiOcrResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AiOcrResultRepository extends JpaRepository<AiOcrResult, Long>, JpaSpecificationExecutor<AiOcrResult> {

    List<AiOcrResult> findByUserIdAndGenealogyIdOrderByCreatedAtDesc(Long userId, Long genealogyId);
}
