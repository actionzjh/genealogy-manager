package com.genealogy.service;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.genealogy.entity.Genealogy;
import com.genealogy.entity.Person;
import com.genealogy.repository.GenealogyRepository;
import com.genealogy.repository.PersonRepository;
import com.genealogy.service.pdf.BiographyLayoutStrategy;
import com.genealogy.service.pdf.EuropeanFiveGenLayoutStrategy;
import com.genealogy.service.pdf.PagodaLayoutStrategy;
import com.genealogy.service.pdf.PdfLayoutStrategy;
import com.genealogy.service.pdf.TableListLayoutStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PDF家谱导出服务
 * 支持多种排版格式，用户可自主选择
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfExportService {

    private final GenealogyRepository genealogyRepository;
    private final PersonRepository personRepository;

    /**
     * 排版策略注册表
     */
    private final Map<String, PdfLayoutStrategy> strategies = new HashMap<>();

    /**
     * 初始化注册所有排版策略
     */
    {
        registerStrategy(new TableListLayoutStrategy());
        registerStrategy(new BiographyLayoutStrategy());
        registerStrategy(new EuropeanFiveGenLayoutStrategy());
        registerStrategy(new PagodaLayoutStrategy());
        // 新增排版策略只需在这里注册即可
    }

    /**
     * 注册排版策略
     */
    private void registerStrategy(PdfLayoutStrategy strategy) {
        strategies.put(strategy.getName(), strategy);
    }

    /**
     * 获取所有可用的排版格式
     */
    public List<LayoutInfo> getAvailableLayouts() {
        List<LayoutInfo> result = new ArrayList<>();
        for (PdfLayoutStrategy strategy : strategies.values()) {
            result.add(new LayoutInfo(strategy.getName(), strategy.getDescription()));
        }
        return result;
    }

    /**
     * 导出家谱PDF，指定排版格式
     */
    public ByteArrayOutputStream exportGenealogyToPdf(Long genealogyId, String layoutName) throws Exception {
        Genealogy genealogy = genealogyRepository.findById(genealogyId).orElse(null);
        if (genealogy == null) {
            throw new IllegalArgumentException("家谱不存在，ID: " + genealogyId);
        }

        List<Person> allPersons = personRepository.findAll();
        if (allPersons.isEmpty()) {
            throw new IllegalStateException("该家谱没有人物数据");
        }

        // 默认使用表格列表式
        if (layoutName == null || !strategies.containsKey(layoutName)) {
            layoutName = "table-list";
        }
        PdfLayoutStrategy strategy = strategies.get(layoutName);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);
        document.setMargins(50, 40, 50, 40);

        // 加载中文字体
        PdfFont font = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H", PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);

        // 使用策略排版
        strategy.layout(document, genealogy, allPersons, font);

        document.close();
        pdfDoc.close();

        log.info("PDF导出成功，家谱：{}，排版：{}，共{}页，{}人",
                genealogy.getName(), layoutName, pdfDoc.getNumberOfPages(), allPersons.size());

        return outputStream;
    }

    /**
     * 使用默认格式导出
     */
    public ByteArrayOutputStream exportGenealogyToPdf(Long genealogyId) throws Exception {
        return exportGenealogyToPdf(genealogyId, "table-list");
    }

    /**
     * 检查排版格式是否存在
     */
    public boolean hasLayout(String layoutName) {
        return strategies.containsKey(layoutName);
    }

    /**
     * 排版信息DTO
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class LayoutInfo {
        private String name;
        private String description;
    }
}
