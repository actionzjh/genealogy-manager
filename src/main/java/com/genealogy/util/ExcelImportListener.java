package com.genealogy.util;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * EasyExcel通用导入监听器
 * @param <T> 导入数据类型
 */
@Slf4j
public class ExcelImportListener<T> implements ReadListener<T> {

    /**
     * 每批处理大小
     */
    private static final int BATCH_SIZE = 100;

    /**
     * 缓存的数据列表
     */
    private List<T> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_SIZE);

    /**
     * 批处理回调
     */
    private final Consumer<List<T>> batchConsumer;

    /**
     * 完整数据列表
     */
    private final List<T> allData = new ArrayList<>();

    public ExcelImportListener(Consumer<List<T>> batchConsumer) {
        this.batchConsumer = batchConsumer;
    }

    @Override
    public void invoke(T data, AnalysisContext context) {
        cachedDataList.add(data);
        allData.add(data);
        if (cachedDataList.size() >= BATCH_SIZE) {
            batchConsumer.accept(cachedDataList);
            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_SIZE);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (!cachedDataList.isEmpty()) {
            batchConsumer.accept(cachedDataList);
        }
        log.info("Excel导入完成，共读取 {} 条数据", allData.size());
    }

    /**
     * 获取所有读取的数据
     */
    public List<T> getAllData() {
        return allData;
    }
}
