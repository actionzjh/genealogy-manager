package com.genealogy.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 家谱信息导入DTO
 */
@Data
public class GenealogyImportDto {

    @ExcelProperty("家谱名称*")
    private String name;

    @ExcelProperty("姓氏")
    private String surname;

    @ExcelProperty("家谱描述")
    private String description;

    @ExcelProperty("起源地")
    private String originPlace;
}
