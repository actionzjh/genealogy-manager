package com.genealogy.repository;

import com.genealogy.entity.GenerationWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 字辈 Repository
 */
public interface GenerationWordRepository extends JpaRepository<GenerationWord, Long>,
        org.springframework.data.jpa.repository.JpaSpecificationExecutor<GenerationWord> {

    /**
     * 按姓氏搜索公开字辈
     */
    Page<GenerationWord> findBySurnameContainingIgnoreCaseAndIsPublicTrue(String surname, Pageable pageable);

    /**
     * 查询某姓氏所有公开字辈
     */
    List<GenerationWord> findBySurnameIgnoreCaseAndIsPublicTrue(String surname);
}
