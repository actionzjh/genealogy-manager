package com.genealogy.service;

import com.genealogy.entity.Person;
import com.genealogy.entity.PersonRelationGraph;
import com.genealogy.repository.PersonRelationGraphRepository;
import com.genealogy.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 血缘关系可视化服务
 */
@Service
public class PersonRelationGraphService {

    @Autowired
    private PersonRelationGraphRepository relationGraphRepository;

    @Autowired
    private PersonRepository personRepository;

    /**
     * 创建/保存关系视图
     */
    public PersonRelationGraph save(PersonRelationGraph graph) {
        return relationGraphRepository.save(graph);
    }

    /**
     * 删除关系视图
     */
    public void delete(Long id) {
        relationGraphRepository.deleteById(id);
    }

    /**
     * 获取用户家谱下的所有视图
     */
    public List<PersonRelationGraph> listByUserAndGenealogy(Long genealogyId, Long userId) {
        return relationGraphRepository.findByGenealogyIdAndUserId(genealogyId, userId);
    }

    /**
     * 获取公开视图
     */
    public List<PersonRelationGraph> listPublicByGenealogy(Long genealogyId) {
        return relationGraphRepository.findByGenealogyIdAndIsPublicTrue(genealogyId);
    }

    /**
     * 根据ID获取
     */
    public Optional<PersonRelationGraph> findById(Long id) {
        return relationGraphRepository.findById(id);
    }

    /**
     * 构建血缘关系网络图数据
     * 从中心人物出发，递归拓展N代，收集所有节点和连线
     */
    public Map<String, Object> buildRelationGraph(Long centerPersonId, int maxDepth) {
        Optional<Person> centerOpt = personRepository.findById(centerPersonId);
        if (!centerOpt.isPresent()) {
            return Collections.emptyMap();
        }

        Set<Long> visited = new HashSet<>();
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> links = new ArrayList<>();

        // 递归收集关系
        collectRelations(centerOpt.get(), maxDepth, 0, visited, nodes, links);

        Map<String, Object> result = new HashMap<>();
        result.put("nodes", nodes);
        result.put("links", links);
        return result;
    }

    /**
     * 递归收集关系
     */
    private void collectRelations(Person person, int maxDepth, int currentDepth,
                                  Set<Long> visited, List<Map<String, Object>> nodes, List<Map<String, Object>> links) {
        if (currentDepth > maxDepth || visited.contains(person.getId())) {
            return;
        }
        visited.add(person.getId());

        // 添加节点
        Map<String, Object> node = new HashMap<>();
        node.put("id", person.getId().toString());
        node.put("name", person.getName());
        node.put("generation", person.getGeneration());
        node.put("gender", person.getGender());
        node.put("depth", currentDepth);
        nodes.add(node);

        // 父亲
        if (person.getFatherId() != null) {
            Optional<Person> father = personRepository.findById(person.getFatherId());
            if (father.isPresent()) {
                addLink(father.get(), person, "父亲", visited, nodes, links);
                collectRelations(father.get(), maxDepth, currentDepth + 1, visited, nodes, links);
            }
        }

        // 母亲
        if (person.getMotherId() != null) {
            Optional<Person> mother = personRepository.findById(person.getMotherId());
            if (mother.isPresent()) {
                addLink(mother.get(), person, "母亲", visited, nodes, links);
                collectRelations(mother.get(), maxDepth, currentDepth + 1, visited, nodes, links);
            }
        }

        // 配偶
        if (person.getSpouseIds() != null && !person.getSpouseIds().isEmpty()) {
            String firstSpouseId = person.getSpouseIds().split(",")[0].trim();
            if (!firstSpouseId.isEmpty()) {
                Optional<Person> spouse = personRepository.findById(Long.parseLong(firstSpouseId));
                if (spouse.isPresent()) {
                    addLink(person, spouse.get(), "配偶", visited, nodes, links);
                    collectRelations(spouse.get(), maxDepth, currentDepth + 1, visited, nodes, links);
                }
            }
        }

        // 子女（所有子女，以当前人物为父亲）
        List<Person> children = personRepository.findByFatherId(person.getId());
        for (Person child : children) {
            addLink(person, child, "子女", visited, nodes, links);
            collectRelations(child, maxDepth, currentDepth + 1, visited, nodes, links);
        }

        // 子女（以当前人物为母亲）
        List<Person> childrenByMother = personRepository.findByMotherId(person.getId());
        for (Person child : childrenByMother) {
            if (!visited.contains(child.getId())) {
                addLink(person, child, "子女", visited, nodes, links);
                collectRelations(child, maxDepth, currentDepth + 1, visited, nodes, links);
            }
        }
    }

    /**
     * 添加连线
     */
    private void addLink(Person source, Person target, String relation, Set<Long> visited,
                         List<Map<String, Object>> nodes, List<Map<String, Object>> links) {
        // 如果源节点还没访问，先添加
        if (!visited.contains(source.getId())) {
            // 不会被递归处理到这里的递归在上面调用
            Map<String, Object> node = new HashMap<>();
            node.put("id", source.getId().toString());
            node.put("name", source.getName());
            node.put("generation", source.getGeneration());
            node.put("gender", source.getGender());
            nodes.add(node);
            visited.add(source.getId());
        }

        Map<String, Object> link = new HashMap<>();
        link.put("source", source.getId().toString());
        link.put("target", target.getId().toString());
        link.put("relation", relation);
        links.add(link);
    }
}
