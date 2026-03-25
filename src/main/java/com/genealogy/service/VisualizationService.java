package com.genealogy.service;

import com.genealogy.entity.Genealogy;
import com.genealogy.entity.Person;
import com.genealogy.entity.PersonRelation;
import com.genealogy.repository.GenealogyRepository;
import com.genealogy.repository.PersonRelationRepository;
import com.genealogy.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VisualizationService {

    @Autowired
    private GenealogyRepository genealogyRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PersonRelationRepository personRelationRepository;

    public Map<String, Object> buildTree(String branchName) {
        List<Person> persons = loadPersons(branchName);
        Optional<Genealogy> genealogy = Optional.empty();

        Map<Long, Person> personMap = persons.stream()
                .collect(Collectors.toMap(Person::getId, person -> person, (left, right) -> left, LinkedHashMap::new));

        Map<Long, Long> parentMap = buildParentMap(persons, personMap);
        Map<Long, List<Person>> childrenMap = buildChildrenMap(persons, parentMap);

        List<Person> roots = findRoots(persons, parentMap).stream()
                .sorted(personComparator())
                .toList();

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("id", branchName != null ? "genealogy-" + branchName : "genealogy-root");
        root.put("title", branchName != null ? branchName : "Genealogy Tree");
        root.put("type", "genealogy");
        root.put("status", "done");
        root.put("children", roots.stream()
                .map(person -> buildPersonNode(person, childrenMap, new HashSet<>()))
                .collect(Collectors.toList()));
        return root;
    }

    private List<Person> loadPersons(String branchName) {
        if (branchName != null && !branchName.isBlank()) {
            return personRepository.findByBranchContainingIgnoreCase(branchName).stream()
                    .sorted(personComparator())
                    .toList();
        }
        return personRepository.findAll().stream()
                .sorted(personComparator())
                .toList();
    }

    private Map<Long, Long> buildParentMap(List<Person> persons, Map<Long, Person> personMap) {
        Map<Long, Long> parentMap = new HashMap<>();

        for (Person person : persons) {
            Long fatherId = normalizeParentId(person.getFatherId(), personMap);
            Long motherId = normalizeParentId(person.getMotherId(), personMap);

            if (fatherId != null) {
                parentMap.put(person.getId(), fatherId);
            } else if (motherId != null) {
                parentMap.put(person.getId(), motherId);
            }
        }

        List<PersonRelation> relations = personRelationRepository.findAll();

        for (PersonRelation relation : relations) {
            if (!personMap.containsKey(relation.getPersonId()) || !personMap.containsKey(relation.getRelatedPersonId())) {
                continue;
            }

            String relationType = relation.getRelationType();
            if ("FATHER".equalsIgnoreCase(relationType) || "MOTHER".equalsIgnoreCase(relationType)) {
                parentMap.putIfAbsent(relation.getPersonId(), relation.getRelatedPersonId());
            }
        }

        return parentMap;
    }

    private Long normalizeParentId(Long parentId, Map<Long, Person> personMap) {
        if (parentId == null) {
            return null;
        }
        return personMap.containsKey(parentId) ? parentId : null;
    }

    private Map<Long, List<Person>> buildChildrenMap(List<Person> persons, Map<Long, Long> parentMap) {
        Map<Long, List<Person>> childrenMap = new HashMap<>();
        for (Person person : persons) {
            Long parentId = parentMap.get(person.getId());
            if (parentId != null) {
                childrenMap.computeIfAbsent(parentId, key -> new ArrayList<>()).add(person);
            }
        }

        for (List<Person> children : childrenMap.values()) {
            children.sort(personComparator());
        }
        return childrenMap;
    }

    private List<Person> findRoots(List<Person> persons, Map<Long, Long> parentMap) {
        return persons.stream()
                .filter(person -> !parentMap.containsKey(person.getId()))
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildPersonNode(Person person, Map<Long, List<Person>> childrenMap, Set<Long> path) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("id", "person-" + person.getId());
        node.put("personId", person.getId());
        node.put("title", person.getName());
        node.put("type", "person");
        node.put("status", "done");
        node.put("description", buildDescription(person));
        node.put("branch", person.getBranch());
        node.put("generation", person.getGeneration());

        if (path.contains(person.getId())) {
            node.put("children", List.of());
            return node;
        }

        Set<Long> nextPath = new HashSet<>(path);
        nextPath.add(person.getId());

        List<Map<String, Object>> children = childrenMap.getOrDefault(person.getId(), List.of())
                .stream()
                .map(child -> buildPersonNode(child, childrenMap, nextPath))
                .collect(Collectors.toList());
        node.put("children", children);
        return node;
    }

    private String buildDescription(Person person) {
        List<String> parts = new ArrayList<>();
        if (person.getStyleName() != null && !person.getStyleName().isBlank()) {
            parts.add("字: " + person.getStyleName());
        }
        if (person.getHao() != null && !person.getHao().isBlank()) {
            parts.add("号: " + person.getHao());
        }
        if (person.getBirthYear() != null && !person.getBirthYear().isBlank()) {
            parts.add("生于 " + person.getBirthYear());
        }
        if (person.getDeathYear() != null && !person.getDeathYear().isBlank()) {
            parts.add("卒于 " + person.getDeathYear());
        }
        if (person.getBiography() != null && !person.getBiography().isBlank()) {
            parts.add(person.getBiography());
        }
        return String.join(" | ", parts);
    }

    private Comparator<Person> personComparator() {
        return Comparator
                .comparing(Person::getGeneration, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(Person::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(Person::getId, Comparator.nullsLast(Long::compareTo));
    }
}
