package com.genealogy.repository;

import com.genealogy.entity.PersonImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonImageRepository extends JpaRepository<PersonImage, Long> {
    
    List<PersonImage> findByPersonId(Long personId);
    
    List<PersonImage> findByGenealogyId(Long genealogyId);
    
    PersonImage findByPersonIdAndIsAvatar(Long personId, Boolean isAvatar);
}
