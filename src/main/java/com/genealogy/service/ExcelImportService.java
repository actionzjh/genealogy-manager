package com.genealogy.service;

import com.alibaba.excel.EasyExcel;
import com.genealogy.dto.GenealogyImportDto;
import com.genealogy.dto.PersonImportDto;
import com.genealogy.entity.Genealogy;
import com.genealogy.entity.Person;
import com.genealogy.repository.GenealogyRepository;
import com.genealogy.repository.PersonRepository;
import com.genealogy.util.ExcelImportListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Excel导入服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelImportService {

    private final GenealogyRepository genealogyRepository;
    private final PersonRepository personRepository;

    /**
     * 导入结果
     */
    @lombok.Data
    public static class ImportResult {
        private int genealogyCount;
        private int personCount;
        private List<String> errors;
        private Long genealogyId;

        public static ImportResult success(Long genealogyId, int genealogyCount, int personCount) {
            ImportResult result = new ImportResult();
            result.setGenealogyId(genealogyId);
            result.setGenealogyCount(genealogyCount);
            result.setPersonCount(personCount);
            result.setErrors(new ArrayList<>());
            return result;
        }

        public static ImportResult error(String message) {
            ImportResult result = new ImportResult();
            result.setGenealogyCount(0);
            result.setPersonCount(0);
            result.setErrors(new ArrayList<>());
            result.getErrors().add(message);
            return result;
        }

        public void addError(String error) {
            this.errors.add(error);
        }

        public boolean hasErrors() {
            return errors != null && !errors.isEmpty();
        }
    }

    /**
     * 导入家谱Excel文件
     * 格式要求：Sheet 0 = 家谱信息，Sheet 1 = 人物信息
     */
    public ImportResult importGenealogyFromExcel(MultipartFile file) {
        try {
            // 读取第一个Sheet：家谱信息
            List<GenealogyImportDto> genealogyList = readSheet(file, 0, GenealogyImportDto.class);

            if (genealogyList.isEmpty()) {
                return ImportResult.error("未找到家谱信息，请检查Excel格式");
            }

            // 只取第一条家谱信息
            GenealogyImportDto genealogyDto = genealogyList.get(0);
            if (genealogyDto.getName() == null || genealogyDto.getName().trim().isEmpty()) {
                return ImportResult.error("家谱名称不能为空");
            }

            // 保存家谱
            Genealogy genealogy = new Genealogy();
            genealogy.setName(genealogyDto.getName());
            genealogy.setSurname(genealogyDto.getSurname());
            genealogy.setDescription(genealogyDto.getDescription());
            genealogy.setOriginPlace(genealogyDto.getOriginPlace());
            genealogy = genealogyRepository.save(genealogy);
            log.info("创建家谱成功: {}, ID: {}", genealogy.getName(), genealogy.getId());

            // 读取第二个Sheet：人物信息
            List<PersonImportDto> personDtoList = readSheet(file, 1, PersonImportDto.class);

            if (personDtoList.isEmpty()) {
                return ImportResult.success(genealogy.getId(), 1, 0);
            }

            // 姓名->人物ID映射，用于建立关系
            Map<String, Long> nameToIdMap = new ConcurrentHashMap<>();
            List<String> errors = new ArrayList<>();
            int successCount = 0;

            // 第一遍：保存所有人物，建立姓名映射（假设同一姓名不重复，如果有重复需要用其他标识）
            for (PersonImportDto dto : personDtoList) {
                if (dto.getName() == null || dto.getName().trim().isEmpty()) {
                    errors.add("跳过空姓名记录");
                    continue;
                }

                Person person = convertToPerson(dto);
                person = personRepository.save(person);
                nameToIdMap.put(dto.getName().trim(), person.getId());
                if (dto.getFatherName() != null && !dto.getFatherName().trim().isEmpty()) {
                    // 父亲姓名先记录，第二遍处理
                }
                successCount++;
            }

            // 第二遍：处理亲属关系（根据姓名查找ID）
            for (PersonImportDto dto : personDtoList) {
                if (dto.getName() == null || dto.getName().trim().isEmpty()) {
                    continue;
                }
                Long personId = nameToIdMap.get(dto.getName().trim());
                if (personId == null) {
                    continue;
                }

                Person person = personRepository.findById(personId).orElse(null);
                if (person == null) {
                    continue;
                }

                // 处理父亲关系
                if (dto.getFatherName() != null && !dto.getFatherName().trim().isEmpty()) {
                    Long fatherId = nameToIdMap.get(dto.getFatherName().trim());
                    if (fatherId != null) {
                        person.setFatherId(fatherId);
                    } else {
                        errors.add("人物[" + dto.getName() + "]的父亲[" + dto.getFatherName() + "]未找到，跳过关系建立");
                    }
                }

                // 处理母亲关系
                if (dto.getMotherName() != null && !dto.getMotherName().trim().isEmpty()) {
                    Long motherId = nameToIdMap.get(dto.getMotherName().trim());
                    if (motherId != null) {
                        person.setMotherId(motherId);
                    } else {
                        errors.add("人物[" + dto.getName() + "]的母亲[" + dto.getMotherName() + "]未找到，跳过关系建立");
                    }
                }

                // 处理配偶关系
                if (dto.getSpouseName() != null && !dto.getSpouseName().trim().isEmpty()) {
                    Long spouseId = nameToIdMap.get(dto.getSpouseName().trim());
                    if (spouseId != null) {
                        person.setSpouseIds(String.valueOf(spouseId));
                        // 反向设置配偶
                        Person spouse = personRepository.findById(spouseId).orElse(null);
                        if (spouse != null && spouse.getSpouseIds() == null) {
                            spouse.setSpouseIds(String.valueOf(personId));
                            personRepository.save(spouse);
                        }
                    } else {
                        errors.add("人物[" + dto.getName() + "]的配偶[" + dto.getSpouseName() + "]未找到，跳过关系建立");
                    }
                }

                personRepository.save(person);
            }

            // 更新家谱统计信息
            genealogy.setTotalPeople(successCount);
            if (!personDtoList.isEmpty()) {
                int maxGen = personDtoList.stream()
                        .map(PersonImportDto::getGeneration)
                        .filter(Objects::nonNull)
                        .mapToInt(Integer::intValue)
                        .max().orElse(1);
                genealogy.setMaxGeneration(maxGen);
            }
            if (!nameToIdMap.isEmpty()) {
                // 设置始祖为第一个人物
                genealogy.setFounderId(nameToIdMap.values().iterator().next());
            }
            genealogyRepository.save(genealogy);

            ImportResult result = ImportResult.success(genealogy.getId(), 1, successCount);
            result.getErrors().addAll(errors);
            return result;

        } catch (Exception e) {
            log.error("Excel导入失败", e);
            return ImportResult.error("导入失败：" + e.getMessage());
        }
    }

    /**
     * 读取指定Sheet
     */
    private <T> List<T> readSheet(MultipartFile file, int sheetNo, Class<T> clazz) throws IOException {
        List<T> data = new ArrayList<>();
        ExcelImportListener<T> listener = new ExcelImportListener<>(batch -> data.addAll(batch));
        EasyExcel.read(file.getInputStream(), clazz, listener)
                .sheet(sheetNo)
                .doRead();
        return listener.getAllData();
    }

    /**
     * 转换Person实体
     */
    private Person convertToPerson(PersonImportDto dto) {
        Person person = new Person();
        person.setName(dto.getName());
        person.setStyleName(dto.getStyleName());
        person.setHao(dto.getHao());

        // 处理性别
        if (dto.getGender() != null) {
            String gender = dto.getGender().trim();
            if (gender.contains("男") || "M".equalsIgnoreCase(gender)) {
                person.setGender("M");
            } else if (gender.contains("女") || "F".equalsIgnoreCase(gender)) {
                person.setGender("F");
            } else {
                person.setGender("U");
            }
        }

        person.setBirthYear(dto.getBirthYear());
        person.setDeathYear(dto.getDeathYear());
        person.setBranch(dto.getBranch());
        person.setGeneration(dto.getGeneration());
        person.setSortOrder(dto.getSortOrder());
        person.setMigrationPath(dto.getMigrationPath());
        person.setAchievements(dto.getAchievements());
        person.setBiography(dto.getBiography());
        person.setSource(dto.getSource());
        person.setCemeteryLocation(dto.getCemeteryLocation());
        person.setTitle(dto.getTitle());
        person.setOccupation(dto.getOccupation());
        person.setMaritalStatus(dto.getMaritalStatus());

        // 处理状态
        if (dto.getStatus() != null) {
            String status = dto.getStatus().trim();
            if (status.contains("去世") || status.contains("逝世") || status.contains("故")) {
                person.setStatus("deceased");
            } else if (status.contains("在世") || status.contains("存活")) {
                person.setStatus("alive");
            }
        }

        return person;
    }

    /**
     * 生成Excel模板文件（下载模板用）
     */
    public void writeTemplate(java.io.OutputStream outputStream) {
        // 写入家谱信息Sheet
        EasyExcel.write(outputStream, GenealogyImportDto.class)
                .sheet("家谱信息")
                .doWrite(getSampleGenealogyData());

        // 继续写入人物信息Sheet
        EasyExcel.write(outputStream, PersonImportDto.class)
                .sheet("人物信息")
                .doWrite(getSamplePersonData());
    }

    /**
     * 获取示例家谱数据（用于模板）
     */
    private List<GenealogyImportDto> getSampleGenealogyData() {
        GenealogyImportDto dto = new GenealogyImportDto();
        dto.setName("张氏家谱");
        dto.setSurname("张");
        dto.setDescription("某地区张氏家族家谱");
        dto.setOriginPlace("某省某市");
        return Collections.singletonList(dto);
    }

    /**
     * 获取示例人物数据（用于模板）
     */
    private List<PersonImportDto> getSamplePersonData() {
        PersonImportDto dto = new PersonImportDto();
        dto.setName("张天一");
        dto.setStyleName("天行");
        dto.setHao("南山居士");
        dto.setGender("男");
        dto.setBirthYear("1600");
        dto.setDeathYear("1680");
        dto.setFatherName("");
        dto.setMotherName("");
        dto.setSpouseName("李氏");
        dto.setBranch("长房");
        dto.setGeneration(1);
        dto.setSortOrder(1);
        dto.setMigrationPath("从甲地迁到乙地");
        dto.setAchievements("创立支派");
        dto.setBiography("张氏开基始祖...");
        dto.setSource("族谱记载");
        dto.setCemeteryLocation("某地南山");
        dto.setTitle("");
        dto.setOccupation("");
        dto.setMaritalStatus("已婚");
        dto.setStatus("去世");
        return Collections.singletonList(dto);
    }
}
