package com.genealogy.service;

import com.genealogy.entity.Person;
import com.genealogy.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 血缘关系计算服务 - 共同祖先、亲缘系数等
 */
@Service
public class RelationshipCalculator {

    @Autowired
    private PersonRepository personRepository;

    /**
     * 寻找两个人的共同祖先
     */
    public List<Person> findCommonAncestors(Long personId1, Long personId2) {
        Set<Long> ancestors1 = getAllAncestors(personId1);
        Set<Long> ancestors2 = getAllAncestors(personId2);

        List<Person> common = new ArrayList<>();
        for (Long id : ancestors1) {
            if (ancestors2.contains(id)) {
                personRepository.findById(id).ifPresent(common::add);
            }
        }
        return common;
    }

    /**
     * 获取所有祖先（向上遍历）
     */
    public Set<Long> getAllAncestors(Long personId) {
        Set<Long> ancestors = new HashSet<>();
        Queue<Long> queue = new LinkedList<>();
        queue.add(personId);

        while (!queue.isEmpty()) {
            Long current = queue.poll();
            if (ancestors.contains(current)) {
                continue; // 避免循环
            }
            ancestors.add(current);

            Optional<Person> personOpt = personRepository.findById(current);
            if (personOpt.isPresent()) {
                Person person = personOpt.get();
                if (person.getFatherId() != null) {
                    queue.add(person.getFatherId());
                }
                if (person.getMotherId() != null) {
                    queue.add(person.getMotherId());
                }
            }
        }

        ancestors.remove(personId); // 移除自己
        return ancestors;
    }

    /**
     * 计算两个人之间的亲缘系数
     * 亲缘系数表示两个人携带同一基因的概率
     *
     * 计算公式：r = Σ (1/2)^(n1 + n2 + 1) 对每个共同祖先
     * 其中 n1 是person1到共同祖先的世代数，n2是person2到共同祖先的世代数
     * 如果是近交，需要乘以 (1 + f_A)，f_A是共同祖先的近交系数
     */
    public double calculateCoefficientOfRelationship(Long personId1, Long personId2) {
        if (personId1.equals(personId2)) {
            return 1.0; // 自己和自己亲缘系数是1
        }

        List<Person> commonAncestors = findCommonAncestors(personId1, personId2);
        if (commonAncestors.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;

        for (Person ancestor : commonAncestors) {
            List<Integer> paths1 = findPathLengths(personId1, ancestor.getId());
            List<Integer> paths2 = findPathLengths(personId2, ancestor.getId());

            for (int n1 : paths1) {
                for (int n2 : paths2) {
                    // (1/2)^(n1 + n2 + 1)
                    double contribution = Math.pow(0.5, n1 + n2 + 1);
                    total += contribution;
                }
            }
        }

        // 这里简化，不计算近交系数，实际应为 2 * total 如果父母有血缘关系
        return total;
    }

    /**
     * 找到从person到ancestor的所有路径长度
     */
    private List<Integer> findPathLengths(Long from, Long to) {
        List<Integer> lengths = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        dfsFindPath(from, to, 0, visited, lengths);
        return lengths;
    }

    private void dfsFindPath(Long current, Long target, int currentLength, Set<Long> visited, List<Integer> lengths) {
        if (current.equals(target)) {
            lengths.add(currentLength);
            return;
        }
        if (visited.contains(current)) {
            return; // 避免循环
        }
        visited.add(current);

        Optional<Person> personOpt = personRepository.findById(current);
        if (personOpt.isPresent()) {
            Person person = personOpt.get();
            if (person.getFatherId() != null) {
                dfsFindPath(person.getFatherId(), target, currentLength + 1, new HashSet<>(visited), lengths);
            }
            if (person.getMotherId() != null) {
                dfsFindPath(person.getMotherId(), target, currentLength + 1, new HashSet<>(visited), lengths);
            }
        }
    }

    /**
     * 查找某人的所有后代
     */
    public List<Person> findAllDescendants(Long ancestorId) {
        List<Person> allPersons = personRepository.findAll();
        List<Person> descendants = new ArrayList<>();

        for (Person person : allPersons) {
            if (isDescendantOf(person.getId(), ancestorId, new HashSet<>())) {
                descendants.add(person);
            }
        }

        return descendants;
    }

    /**
     * 判断person是否是ancestor的后代
     */
    private boolean isDescendantOf(Long personId, Long ancestorId, Set<Long> visited) {
        if (personId.equals(ancestorId)) {
            return true;
        }
        if (visited.contains(personId)) {
            return false; // 避免循环
        }
        visited.add(personId);

        Optional<Person> personOpt = personRepository.findById(personId);
        if (personOpt.isPresent()) {
            Person person = personOpt.get();
            if (person.getFatherId() != null && isDescendantOf(person.getFatherId(), ancestorId, visited)) {
                return true;
            }
            if (person.getMotherId() != null && isDescendantOf(person.getMotherId(), ancestorId, visited)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 计算最近共同祖先
     */
    public Optional<Person> findMostRecentCommonAncestor(Long personId1, Long personId2) {
        List<Person> commonAncestors = findCommonAncestors(personId1, personId2);
        if (commonAncestors.isEmpty()) {
            return Optional.empty();
        }

        // 找到世代最大的（即最近的）
        Person mostRecent = commonAncestors.get(0);
        for (Person ancestor : commonAncestors) {
            if (ancestor.getGeneration() != null &&
                mostRecent.getGeneration() != null &&
                ancestor.getGeneration() > mostRecent.getGeneration()) {
                mostRecent = ancestor;
            }
        }
        return Optional.of(mostRecent);
    }

    /**
     * 统计人口分布 - 按性别
     */
    public Map<String, Long> countByGender(Long genealogyId) {
        Map<String, Long> result = new HashMap<>();
        List<Person> persons = personRepository.findByGenealogyId(genealogyId);

        long male = 0, female = 0, unknown = 0;
        for (Person person : persons) {
            String gender = person.getGender();
            if ("M".equals(gender)) {
                male++;
            } else if ("F".equals(gender)) {
                female++;
            } else {
                unknown++;
            }
        }

        result.put("male", male);
        result.put("female", female);
        result.put("unknown", unknown);
        result.put("total", (long) persons.size());
        return result;
    }

    /**
     * 统计人口分布 - 按世代
     */
    public Map<Integer, Long> countByGeneration(Long genealogyId) {
        Map<Integer, Long> result = new TreeMap<>();
        List<Person> persons = personRepository.findByGenealogyId(genealogyId);

        for (Person person : persons) {
            Integer gen = person.getGeneration();
            if (gen == null) gen = 0;
            result.merge(gen, 1L, Long::sum);
        }
        return result;
    }

    /**
     * 计算平均世代间隔
     */
    public Double calculateAverageGenerationInterval(Long genealogyId) {
        List<Person> persons = personRepository.findByGenealogyId(genealogyId);
        if (persons.size() < 2) {
            return null;
        }

        long totalInterval = 0;
        int count = 0;

        for (Person person : persons) {
            if (person.getBirthYear() != null && !person.getBirthYear().isEmpty()) {
                try {
                    int birthYear = parseYear(person.getBirthYear());
                    boolean hasParentYear = false;
                    int parentBirthYear = 0;

                    if (person.getFatherId() != null) {
                        Optional<Person> fatherOpt = personRepository.findById(person.getFatherId());
                        if (fatherOpt.isPresent() && fatherOpt.get().getBirthYear() != null) {
                            parentBirthYear = parseYear(fatherOpt.get().getBirthYear());
                            if (parentBirthYear > 0 && birthYear > parentBirthYear) {
                                totalInterval += (birthYear - parentBirthYear);
                                count++;
                                hasParentYear = true;
                            }
                        }
                    }
                    if (!hasParentYear && person.getMotherId() != null) {
                        Optional<Person> motherOpt = personRepository.findById(person.getMotherId());
                        if (motherOpt.isPresent() && motherOpt.get().getBirthYear() != null) {
                            parentBirthYear = parseYear(motherOpt.get().getBirthYear());
                            if (parentBirthYear > 0 && birthYear > parentBirthYear) {
                                totalInterval += (birthYear - parentBirthYear);
                                count++;
                            }
                        }
                    }
                } catch (Exception e) {
                    // ignore parse error
                }
            }
        }

        if (count == 0) {
            return null;
        }
        return (double) totalInterval / count;
    }

    /**
     * 解析年份，处理公元前
     */
    private int parseYear(String yearStr) {
        yearStr = yearStr.trim();
        if (yearStr.startsWith("BC") || yearStr.startsWith("bc") || yearStr.startsWith("公元前")) {
            // 公元前年份处理，这里简化
            String num = yearStr.replaceAll("[^0-9]", "");
            return -Integer.parseInt(num);
        }
        // 提取数字
        String num = yearStr.replaceAll("[^0-9]", "");
        if (num.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(num);
    }
}
