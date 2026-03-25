package com.genealogy.service.pdf;

import com.genealogy.entity.Genealogy;
import com.genealogy.entity.Person;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.layout.Document;

import java.io.IOException;
import java.util.List;

/**
 * PDF排版策略接口
 * 每种家谱排版格式实现此接口
 */
public interface PdfLayoutStrategy {

    /**
     * 获取排版格式名称
     */
    String getName();

    /**
     * 获取排版格式描述
     */
    String getDescription();

    /**
     * 排版整个家谱
     * @param document PDF文档对象
     * @param genealogy 家谱信息
     * @param allPersons 所有人物列表
     * @param font 中文字体
     * @throws IOException 排版异常
     */
    void layout(Document document, Genealogy genealogy, List<Person> allPersons, PdfFont font) throws IOException;
}
