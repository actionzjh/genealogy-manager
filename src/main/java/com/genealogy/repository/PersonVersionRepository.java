package com.genealogy.repository;

import com.genealogy.entity.PersonVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 人物版本Repository
 */
public interface PersonVersionRepository extends JpaRepository<PersonVersion, Long> {

    /**
     * 查询某个人物的所有版本，按时间倒序
     */
    Page<PersonVersion> findByPersonIdOrderByCreatedAtDesc(Long personId, Pageable pageable);

    /**
     * 查询某个人物的最近一个版本
     */
    PersonVersion findFirstByPersonIdOrderByCreatedAtDesc(Long personId);

    /**
     * 删除某个人物的所有版本（一般不需要，保持历史）
     */
    void deleteByPersonId(Long personId);
}
