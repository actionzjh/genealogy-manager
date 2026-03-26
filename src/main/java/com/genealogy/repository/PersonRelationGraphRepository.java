package com.genealogy.repository;

import com.genealogy.entity.PersonRelationGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PersonRelationGraphRepository extends JpaRepository<PersonRelationGraph, Long>, JpaSpecificationExecutor<PersonRelationGraph> {

    List<PersonRelationGraph> findByGenealogyIdAndUserId(Long genealogyId, Long userId);

    List<PersonRelationGraph> findByGenealogyIdAndIsPublicTrue(Long genealogyId);
}
