package com.genealogy.controller;

import com.genealogy.entity.Person;
import com.genealogy.repository.PersonRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 支系层级可视化控制器
 * 提供树形数据结构API用于层级可视化展示
 */
@RestController
@RequestMapping("/api/hierarchy")
public class HierarchyVisualizerController {

    private final PersonRepository personRepository;

    public HierarchyVisualizerController(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    /**
     * 获取支系树形结构数据
     */
    @GetMapping("/branch-data")
    public ResponseEntity<HierarchyNode> getBranchData(
            Long genealogyId,
            Long rootPersonId) {

        // 查找根人物
        Person rootPerson = personRepository.findById(rootPersonId).orElse(null);
        if (rootPerson == null) {
            return ResponseEntity.notFound().build();
        }

        // 递归构建树形结构
        HierarchyNode root = buildHierarchy(rootPerson, 0);

        return ResponseEntity.ok(root);
    }

    /**
     * 递归构建树形结构
     */
    private HierarchyNode buildHierarchy(Person person, int depth) {
        HierarchyNode node = new HierarchyNode();
        node.setId(person.getId());
        node.setDepth(depth);
        node.setData(new NodeData(
                person.getName(),
                person.getBirthYear(),
                person.getDeathYear(),
                person.getTitle(),
                person.getGender()
        ));

        // 查找所有子女（childrenIds不为空的情况下）
        List<HierarchyNode> children = new ArrayList<>();

        // 根据fatherId反向查找子女
        List<Person> childList = personRepository.findByFatherId(person.getId());
        for (Person child : childList) {
            children.add(buildHierarchy(child, depth + 1));
        }

        // 如果还有motherId关联的子女也需要添加？
        // 通常家谱中子女跟随父亲，所以只处理fatherId即可

        if (!children.isEmpty()) {
            node.setChildren(children);
        }

        return node;
    }

    /**
     * 树形节点DTO
     */
    public static class HierarchyNode {
        private Long id;
        private int depth;
        private NodeData data;
        private List<HierarchyNode> children;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public int getDepth() {
            return depth;
        }

        public void setDepth(int depth) {
            this.depth = depth;
        }

        public NodeData getData() {
            return data;
        }

        public void setData(NodeData data) {
            this.data = data;
        }

        public List<HierarchyNode> getChildren() {
            return children;
        }

        public void setChildren(List<HierarchyNode> children) {
            this.children = children;
        }
    }

    /**
     * 节点数据DTO
     */
    public static class NodeData {
        private String name;
        private String birthYear;
        private String deathYear;
        private String title;
        private String gender;

        public NodeData(String name, String birthYear, String deathYear, String title, String gender) {
            this.name = name;
            this.birthYear = birthYear;
            this.deathYear = deathYear;
            this.title = title;
            this.gender = gender;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getBirthYear() {
            return birthYear;
        }

        public void setBirthYear(String birthYear) {
            this.birthYear = birthYear;
        }

        public String getDeathYear() {
            return deathYear;
        }

        public void setDeathYear(String deathYear) {
            this.deathYear = deathYear;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }
    }
}
