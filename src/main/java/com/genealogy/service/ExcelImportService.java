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
import lombok.Data;
import lombok.AllArgsConstructor;
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
    @Data
    public static class ImportResult {
        private int genealogyCount;
        private int personCount;
        private List<String> errors;
        private List<String> warnings;
        private Long genealogyId;

        public static ImportResult success(Long genealogyId, int genealogyCount, int personCount) {
            ImportResult result = new ImportResult();
            result.setGenealogyId(genealogyId);
            result.setGenealogyCount(genealogyCount);
            result.setPersonCount(personCount);
            result.setErrors(new ArrayList<>());
            result.setWarnings(new ArrayList<>());
            return result;
        }

        public static ImportResult error(String message) {
            ImportResult result = new ImportResult();
            result.setGenealogyCount(0);
            result.setPersonCount(0);
            result.setErrors(new ArrayList<>());
            result.setWarnings(new ArrayList<>());
            result.getErrors().add(message);
            return result;
        }

        public void addError(String error) {
            this.errors.add(error);
        }

        public void addWarning(String warning) {
            this.warnings.add(warning);
        }

        public boolean hasErrors() {
            return errors != null && !errors.isEmpty();
        }

        public boolean hasWarnings() {
            return warnings != null && !warnings.isEmpty();
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
            Map<String, PersonImportDto> nameToDtoMap = new ConcurrentHashMap<>();
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            int successCount = 0;

            // 第一遍：数据校验
            List<String> validationWarnings = validatePersonData(personDtoList);
            warnings.addAll(validationWarnings);

            // 检查重复姓名
            Set<String> duplicateNames = findDuplicateNames(personDtoList);
            if (!duplicateNames.isEmpty()) {
                warnings.add("检测到重复姓名: " + String.join(", ", duplicateNames) + "，关系建立可能不准确，建议Excel中区分开");
            }

            // 第一遍：保存所有人物，建立姓名映射（假设同一姓名不重复，如果有重复需要用其他标识）
            for (PersonImportDto dto : personDtoList) {
                if (dto.getName() == null || dto.getName().trim().isEmpty()) {
                    errors.add("跳过空姓名记录");
                    continue;
                }

                String nameTrim = dto.getName().trim();
                if (nameToIdMap.containsKey(nameTrim)) {
                    warnings.add("人物姓名[" + nameTrim + "]重复，已存在，后一条覆盖前一条");
                }

                Person person = convertToPerson(dto);
                person = personRepository.save(person);
                nameToIdMap.put(nameTrim, person.getId());
                nameToDtoMap.put(nameTrim, dto);
                successCount++;
            }

            // 第二遍：处理亲属关系（根据姓名查找ID）+ 出生日期合理性校验
            for (PersonImportDto dto : personDtoList) {
                if (dto.getName() == null || dto.getName().trim().isEmpty()) {
                    continue;
                }
                String nameTrim = dto.getName().trim();
                Long personId = nameToIdMap.get(nameTrim);
                if (personId == null) {
                    continue;
                }

                Person person = personRepository.findById(personId).orElse(null);
                if (person == null) {
                    continue;
                }

                // 处理父亲关系
                if (dto.getFatherName() != null && !dto.getFatherName().trim().isEmpty()) {
                    String fatherNameTrim = dto.getFatherName().trim();
                    Long fatherId = nameToIdMap.get(fatherNameTrim);
                    if (fatherId != null) {
                        person.setFatherId(fatherId);
                        // 校验出生年份合理性
                        PersonImportDto fatherDto = nameToDtoMap.get(fatherNameTrim);
                        if (fatherDto != null) {
                            String validationWarning = validateBirthDateOrder(fatherDto, dto, nameTrim);
                            if (validationWarning != null) {
                                warnings.add(validationWarning);
                            }
                        }
                    } else {
                        warnings.add("人物[" + dto.getName() + "]的父亲[" + dto.getFatherName() + "]未找到，跳过关系建立");
                    }
                }

                // 处理母亲关系
                if (dto.getMotherName() != null && !dto.getMotherName().trim().isEmpty()) {
                    String motherNameTrim = dto.getMotherName().trim();
                    Long motherId = nameToIdMap.get(motherNameTrim);
                    if (motherId != null) {
                        person.setMotherId(motherId);
                        // 校验出生年份合理性
                        PersonImportDto motherDto = nameToDtoMap.get(motherNameTrim);
                        if (motherDto != null) {
                            String validationWarning = validateBirthDateOrder(motherDto, dto, nameTrim);
                            if (validationWarning != null) {
                                warnings.add(validationWarning);
                            }
                        }
                    } else {
                        warnings.add("人物[" + dto.getName() + "]的母亲[" + dto.getMotherName() + "]未找到，跳过关系建立");
                    }
                }

                // 处理配偶关系
                if (dto.getSpouseName() != null && !dto.getSpouseName().trim().isEmpty()) {
                    String spouseNameTrim = dto.getSpouseName().trim();
                    Long spouseId = nameToIdMap.get(spouseNameTrim);
                    if (spouseId != null) {
                        person.setSpouseIds(String.valueOf(spouseId));
                        // 反向设置配偶
                        Person spouse = personRepository.findById(spouseId).orElse(null);
                        if (spouse != null && spouse.getSpouseIds() == null) {
                            spouse.setSpouseIds(String.valueOf(personId));
                            personRepository.save(spouse);
                        }
                    } else {
                        warnings.add("人物[" + dto.getName() + "]的配偶[" + dto.getSpouseName() + "]未找到，跳过关系建立");
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
            result.getWarnings().addAll(warnings);
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
     * 校验所有人的数据，返回警告信息
     */
    private List<String> validatePersonData(List<PersonImportDto> personDtoList) {
        List<String> warnings = new ArrayList<>();
        int nullGenerationCount = 0;
        for (PersonImportDto dto : personDtoList) {
            if (dto.getGeneration() == null) {
                nullGenerationCount++;
            }
        }
        if (nullGenerationCount > 0) {
            warnings.add("有" + nullGenerationCount + "条记录未填写世代代数，可能影响排序和显示");
        }
        return warnings;
    }

    /**
     * 查找重复姓名
     */
    private Set<String> findDuplicateNames(List<PersonImportDto> personDtoList) {
        Set<String> names = new HashSet<>();
        Set<String> duplicates = new HashSet<>();
        for (PersonImportDto dto : personDtoList) {
            if (dto.getName() == null || dto.getName().trim().isEmpty()) {
                continue;
            }
            String name = dto.getName().trim();
            if (!names.add(name)) {
                duplicates.add(name);
            }
        }
        return duplicates;
    }

    /**
     * 校验父母出生年份是否合理（父亲/母亲不能比孩子晚出生）
     */
    private String validateBirthDateOrder(PersonImportDto parent, PersonImportDto child, String childName) {
        if (parent.getBirthYear() == null || parent.getBirthYear().isEmpty() ||
            child.getBirthYear() == null || child.getBirthYear().isEmpty()) {
            return null;
        }
        try {
            // 处理公元前（负数）
            int parentYear = parseBirthYear(parent.getBirthYear());
            int childYear = parseBirthYear(child.getBirthYear());

            // 正常来说父母至少比孩子大15岁
            if (childYear <= parentYear) {
                return String.format("数据警告: %s(%d年出生)的父母%s(%d年出生)出生年份晚于或等于孩子，可能有误",
                        childName, childYear, parent.getName(), parentYear);
            }
            if (childYear - parentYear < 10) {
                return String.format("数据提示: %s(%d)与父母%s(%d)年龄差仅%d岁，可能有误，请检查",
                        childName, childYear, parent.getName(), parentYear, childYear - parentYear);
            }
            // 父母年龄差太大也提示一下（超过60岁）
            if (childYear - parentYear > 60) {
                return String.format("数据提示: %s(%d)与父母%s(%d)年龄差%d岁，请注意是否正确",
                        childName, childYear, parent.getName(), parentYear, childYear - parentYear);
            }
        } catch (NumberFormatException e) {
            // 无法解析年份，不报错，只跳过
            return null;
        }
        return null;
    }

    /**
     * 解析出生年份，支持"公元前100" → -100，"1600" → 1600
     */
    private int parseBirthYear(String yearStr) {
        yearStr = yearStr.trim();
        if (yearStr.startsWith("公元前") || yearStr.startsWith("前")) {
            String numStr = yearStr.replace("公元前", "").replace("前", "").trim();
            return -Integer.parseInt(numStr);
        }
        return Integer.parseInt(yearStr.replaceAll("[^0-9-]", ""));
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
        dto.setRemark("");
        return Collections.singletonList(dto);
    }
}
