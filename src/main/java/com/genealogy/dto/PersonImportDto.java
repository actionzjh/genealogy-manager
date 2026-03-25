package com.genealogy.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 人物信息导入DTO
 * 按照Excel表头顺序排列，必填项标注*
 */
@Data
public class PersonImportDto {

    @ExcelProperty("姓名*")
    private String name;

    @ExcelProperty("字")
    private String styleName;

    @ExcelProperty("号")
    private String hao;

    @ExcelProperty("性别")
    private String gender;

    @ExcelProperty("出生年份")
    private String birthYear;

    @ExcelProperty("逝世年份")
    private String deathYear;

    @ExcelProperty("父亲姓名")
    private String fatherName;

    @ExcelProperty("母亲姓名")
    private String motherName;

    @ExcelProperty("配偶姓名")
    private String spouseName;

    @ExcelProperty("支系")
    private String branch;

    @ExcelProperty("世代代数")
    private Integer generation;

    @ExcelProperty("排序序号")
    private Integer sortOrder;

    @ExcelProperty("迁徙路线")
    private String migrationPath;

    @ExcelProperty("功绩/称号")
    private String achievements;

    @ExcelProperty("传记")
    private String biography;

    @ExcelProperty("传记来源")
    private String source;

    @ExcelProperty("墓地位置")
    private String cemeteryLocation;

    @ExcelProperty("封号/爵位")
    private String title;

    @ExcelProperty("官职/职业")
    private String occupation;

    @ExcelProperty("婚姻状况")
    private String maritalStatus;

    @ExcelProperty("状态(在世/去世)")
    private String status;

    @ExcelProperty("备注")
    private String remark;
}
