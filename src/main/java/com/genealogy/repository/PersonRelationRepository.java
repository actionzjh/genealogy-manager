package com.genealogy.repository;

import com.genealogy.entity.PersonRelation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonRelationRepository extends JpaRepository<PersonRelation, Long> {

    List<PersonRelation> findByGenealogyId(Long genealogyId);

    List<PersonRelation> findByPersonId(Long personId);

    List<PersonRelation> findByRelatedPersonId(Long relatedPersonId);

    List<PersonRelation> findByPersonIdAndRelationType(Long personId, String relationType);

    List<PersonRelation> findByRelatedPersonIdAndRelationType(Long relatedPersonId, String relationType);

    Optional<PersonRelation> findByPersonIdAndRelatedPersonIdAndRelationType(Long personId, Long relatedPersonId, String relationType);
}
