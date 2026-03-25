package com.genealogy.service;

import com.genealogy.entity.PersonRelation;
import com.genealogy.entity.Person;
import com.genealogy.repository.PersonRelationRepository;
import com.genealogy.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PersonRelationService {

    @Autowired
    private PersonRelationRepository personRelationRepository;

    @Autowired
    private PersonRepository personRepository;

    @Transactional
    public PersonRelation save(PersonRelation relation) {
        String relationType = normalizeRelationType(relation.getRelationType());
        relation.setRelationType(relationType);

        if (relation.getId() != null) {
            personRelationRepository.findById(relation.getId())
                    .ifPresent(this::removeRelationSideEffects);
        }

        Optional<PersonRelation> existing = personRelationRepository
                .findByPersonIdAndRelatedPersonIdAndRelationType(
                        relation.getPersonId(),
                        relation.getRelatedPersonId(),
                        relationType
                );

        if (existing.isPresent() && (relation.getId() == null || !existing.get().getId().equals(relation.getId()))) {
            relation.setId(existing.get().getId());
        }

        PersonRelation saved = personRelationRepository.save(relation);
        applyRelationSideEffects(saved);
        return saved;
    }

    public Optional<PersonRelation> findById(Long id) {
        return personRelationRepository.findById(id);
    }

    public List<PersonRelation> findAll() {
        return personRelationRepository.findAll();
    }

    public List<PersonRelation> findByGenealogyId(Long genealogyId) {
        return personRelationRepository.findByGenealogyId(genealogyId);
    }

    public List<PersonRelation> findByPersonId(Long personId) {
        return personRelationRepository.findByPersonId(personId);
    }

    public List<PersonRelation> findParents(Long personId) {
        List<PersonRelation> list = new ArrayList<>();
        list.addAll(personRelationRepository.findByPersonIdAndRelationType(personId, "FATHER"));
        list.addAll(personRelationRepository.findByPersonIdAndRelationType(personId, "MOTHER"));
        return list;
    }

    public List<PersonRelation> findSpouses(Long personId) {
        return personRelationRepository.findByPersonIdAndRelationType(personId, "SPOUSE");
    }

    public List<PersonRelation> findChildren(Long personId) {
        List<PersonRelation> list = new ArrayList<>();
        list.addAll(personRelationRepository.findByRelatedPersonIdAndRelationType(personId, "FATHER"));
        list.addAll(personRelationRepository.findByRelatedPersonIdAndRelationType(personId, "MOTHER"));
        return list;
    }

    @Transactional
    public void deleteById(Long id) {
        personRelationRepository.findById(id).ifPresent(this::removeRelationSideEffects);
        personRelationRepository.deleteById(id);
    }

    public long count() {
        return personRelationRepository.count();
    }

    private String normalizeRelationType(String relationType) {
        return relationType == null ? "" : relationType.trim().toUpperCase();
    }

    private void applyRelationSideEffects(PersonRelation relation) {
        String relationType = normalizeRelationType(relation.getRelationType());
        Optional<Person> subjectOpt = personRepository.findById(relation.getPersonId());
        Optional<Person> relatedOpt = personRepository.findById(relation.getRelatedPersonId());
        if (subjectOpt.isEmpty() || relatedOpt.isEmpty()) {
            return;
        }

        Person subject = subjectOpt.get();
        Person related = relatedOpt.get();

        switch (relationType) {
            case "FATHER" -> {
                subject.setFatherId(related.getId());
                personRepository.save(subject);
            }
            case "MOTHER" -> {
                subject.setMotherId(related.getId());
                personRepository.save(subject);
            }
            case "SPOUSE" -> {
                subject.setSpouseIds(addId(subject.getSpouseIds(), related.getId()));
                related.setSpouseIds(addId(related.getSpouseIds(), subject.getId()));
                personRepository.save(subject);
                personRepository.save(related);
                ensureReverseSpouseRelation(relation);
            }
            default -> {
            }
        }
    }

    private void removeRelationSideEffects(PersonRelation relation) {
        String relationType = normalizeRelationType(relation.getRelationType());
        Optional<Person> subjectOpt = personRepository.findById(relation.getPersonId());
        Optional<Person> relatedOpt = personRepository.findById(relation.getRelatedPersonId());

        switch (relationType) {
            case "FATHER" -> subjectOpt.ifPresent(subject -> {
                if (subject.getFatherId() != null && subject.getFatherId().equals(relation.getRelatedPersonId())) {
                    subject.setFatherId(null);
                    personRepository.save(subject);
                }
            });
            case "MOTHER" -> subjectOpt.ifPresent(subject -> {
                if (subject.getMotherId() != null && subject.getMotherId().equals(relation.getRelatedPersonId())) {
                    subject.setMotherId(null);
                    personRepository.save(subject);
                }
            });
            case "SPOUSE" -> {
                subjectOpt.ifPresent(subject -> {
                    subject.setSpouseIds(removeId(subject.getSpouseIds(), relation.getRelatedPersonId()));
                    personRepository.save(subject);
                });
                relatedOpt.ifPresent(related -> {
                    related.setSpouseIds(removeId(related.getSpouseIds(), relation.getPersonId()));
                    personRepository.save(related);
                });
                removeReverseSpouseRelation(relation);
            }
            default -> {
            }
        }
    }

    private void ensureReverseSpouseRelation(PersonRelation relation) {
        if (personRelationRepository.findByPersonIdAndRelatedPersonIdAndRelationType(
                relation.getRelatedPersonId(),
                relation.getPersonId(),
                "SPOUSE"
        ).isPresent()) {
            return;
        }

        PersonRelation reverse = new PersonRelation();
        reverse.setGenealogyId(relation.getGenealogyId());
        reverse.setPersonId(relation.getRelatedPersonId());
        reverse.setRelatedPersonId(relation.getPersonId());
        reverse.setRelationType("SPOUSE");
        reverse.setRemark(relation.getRemark());
        reverse.setStartDate(relation.getStartDate());
        reverse.setEndDate(relation.getEndDate());
        reverse.setSortOrder(relation.getSortOrder());
        personRelationRepository.save(reverse);
    }

    private void removeReverseSpouseRelation(PersonRelation relation) {
        personRelationRepository.findByPersonIdAndRelatedPersonIdAndRelationType(
                relation.getRelatedPersonId(),
                relation.getPersonId(),
                "SPOUSE"
        ).ifPresent(reverse -> {
            if (!reverse.getId().equals(relation.getId())) {
                personRelationRepository.delete(reverse);
            }
        });
    }

    private String addId(String csv, Long id) {
        Set<String> values = new LinkedHashSet<>();
        if (csv != null && !csv.isBlank()) {
            values.addAll(Arrays.stream(csv.split(","))
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
        }
        values.add(String.valueOf(id));
        return String.join(",", values);
    }

    private String removeId(String csv, Long id) {
        if (csv == null || csv.isBlank()) {
            return null;
        }
        Set<String> values = Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank() && !value.equals(String.valueOf(id)))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return values.isEmpty() ? null : String.join(",", values);
    }
}
