package com.genealogy.repository;

import com.genealogy.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByPersonIdOrderBySortOrderAscCreatedAtDesc(Long personId);

    List<Attachment> findByGenealogyIdOrderBySortOrderAscCreatedAtDesc(Long genealogyId);
}
