package com.genealogy.config;

import com.genealogy.entity.Branch;
import com.genealogy.entity.Genealogy;
import com.genealogy.entity.Person;
import com.genealogy.entity.PersonRelation;
import com.genealogy.repository.BranchRepository;
import com.genealogy.repository.GenealogyRepository;
import com.genealogy.repository.PersonRelationRepository;
import com.genealogy.repository.PersonRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SampleDataInitializer implements CommandLineRunner {

    private final GenealogyRepository genealogyRepository;
    private final PersonRepository personRepository;
    private final BranchRepository branchRepository;
    private final PersonRelationRepository personRelationRepository;

    public SampleDataInitializer(
            GenealogyRepository genealogyRepository,
            PersonRepository personRepository,
            BranchRepository branchRepository,
            PersonRelationRepository personRelationRepository) {
        this.genealogyRepository = genealogyRepository;
        this.personRepository = personRepository;
        this.branchRepository = branchRepository;
        this.personRelationRepository = personRelationRepository;
    }

    @Override
    public void run(String... args) {
        if (genealogyRepository.count() > 0 || personRepository.count() > 0) {
            return;
        }

        Genealogy genealogy = new Genealogy();
        genealogy.setName("张氏家谱演示");
        genealogy.setSurname("张");
        genealogy.setDescription("用于演示家谱管理、人物关系与前端树形可视化的初始化数据。");
        genealogy.setOriginPlace("安徽歙县");
        genealogy = genealogyRepository.save(genealogy);

        Branch mainBranch = new Branch();
        mainBranch.setGenealogyId(genealogy.getId());
        mainBranch.setName("本支");
        mainBranch.setDescription("家谱演示主支系");
        mainBranch.setOriginPlace("安徽歙县");
        mainBranch.setSortOrder(1);
        mainBranch = branchRepository.save(mainBranch);

        Branch huiningBranch = new Branch();
        huiningBranch.setGenealogyId(genealogy.getId());
        huiningBranch.setParentId(mainBranch.getId());
        huiningBranch.setName("休宁支");
        huiningBranch.setDescription("迁居休宁的支系");
        huiningBranch.setOriginPlace("安徽休宁");
        huiningBranch.setSortOrder(2);
        huiningBranch = branchRepository.save(huiningBranch);

        Person ancestor = createPerson("张启源", 1, "始迁祖", "1680", "1748");
        ancestor.setBranch("本支");
        ancestor.setAchievements("家族始迁祖，立谱起源人物。");
        ancestor = personRepository.save(ancestor);

        Person sonA = createPerson("张文盛", 2, "长房", "1708", "1771");
        sonA.setFatherId(ancestor.getId());
        sonA.setBranch("本支");
        sonA.setAchievements("长房后裔，负责族中田产与谱牒整理。");

        Person sonB = createPerson("张文达", 2, "二房", "1712", "1782");
        sonB.setFatherId(ancestor.getId());
        sonB.setBranch("休宁支");
        sonB.setAchievements("迁至休宁，形成新支系。");

        personRepository.saveAll(List.of(sonA, sonB));

        Person grandsonA1 = createPerson("张廷瑞", 3, "长房长子", "1734", "1798");
        grandsonA1.setFatherId(sonA.getId());
        grandsonA1.setBranch("本支");
        grandsonA1.setAchievements("续修支谱，家中设义学。");

        Person grandsonA2 = createPerson("张廷芳", 3, "长房次子", "1738", "1804");
        grandsonA2.setFatherId(sonA.getId());
        grandsonA2.setBranch("本支");
        grandsonA2.setAchievements("擅书法，存有碑刻题记。");

        Person grandsonB1 = createPerson("张廷翰", 3, "休宁支长子", "1741", "1810");
        grandsonB1.setFatherId(sonB.getId());
        grandsonB1.setBranch("休宁支");
        grandsonB1.setAchievements("经营茶业，推动家族迁徙扩展。");

        personRepository.saveAll(List.of(grandsonA1, grandsonA2, grandsonB1));

        Person greatGrandson = createPerson("张继远", 4, "休宁支后裔", "1770", "1836");
        greatGrandson.setFatherId(grandsonB1.getId());
        greatGrandson.setBranch("休宁支");
        greatGrandson.setAchievements("后续分房祖，演示四代树结构。");
        personRepository.save(greatGrandson);

        personRelationRepository.saveAll(List.of(
                relation(genealogy.getId(), sonA.getId(), ancestor.getId(), "FATHER"),
                relation(genealogy.getId(), sonB.getId(), ancestor.getId(), "FATHER"),
                relation(genealogy.getId(), grandsonA1.getId(), sonA.getId(), "FATHER"),
                relation(genealogy.getId(), grandsonA2.getId(), sonA.getId(), "FATHER"),
                relation(genealogy.getId(), grandsonB1.getId(), sonB.getId(), "FATHER"),
                relation(genealogy.getId(), greatGrandson.getId(), grandsonB1.getId(), "FATHER")
        ));
    }

    private Person createPerson(String name, Integer generation, String title, String birthYear, String deathYear) {
        Person person = new Person();
        person.setName(name);
        person.setGender("M");
        person.setGeneration(generation);
        person.setTitle(title);
        person.setBirthYear(birthYear);
        person.setDeathYear(deathYear);
        person.setStatus("deceased");
        person.setSortOrder(generation);
        return person;
    }

    private PersonRelation relation(Long genealogyId, Long personId, Long relatedPersonId, String type) {
        PersonRelation relation = new PersonRelation();
        relation.setGenealogyId(genealogyId);
        relation.setPersonId(personId);
        relation.setRelatedPersonId(relatedPersonId);
        relation.setRelationType(type);
        return relation;
    }
}
