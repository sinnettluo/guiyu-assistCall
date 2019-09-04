package com.guiji.da.util;

import lombok.extern.log4j.Log4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.SheetUtil;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author Zhouzl
 * @date 2019年06月24日
 * @since 1.0
 */
@Log4j
public class ExportUtil {

    public enum ContentType {
        //二进制流
        BYTES("application/octet-stream;charset=UTF-8"),
        //excel表格(.xlsx)
        EXCEL("application/vnd.ms-excel;charset=utf-8"),
        //zip压缩文件(.zip)
        ZIP("application/x-zip-compressed;charset=UTF-8");

        public final String contentType;

        ContentType(String contentType) {
            this.contentType = contentType;
        }

        @Override
        public String toString() {
            return this.contentType;
        }
    }

    /**
     * 将Excel导出
     *
     * @param workbook
     * @param outputStream
     * @throws IOException
     */
    public static void exportExcel(@NotNull Workbook workbook, @NotNull OutputStream outputStream) throws IOException {
        workbook.write(outputStream);
        workbook.close();
        outputStream.flush();
    }

    /**
     * 导出为Excel工作簿
     *
     * @param outputStream 输出流
     * @param sheetName    表格名
     * @param title        标题
     * @param headList     表格头
     * @param dataList     表格内容数据
     */
    public static void exportExcel(OutputStream outputStream, String sheetName, String title, List<String> headList, List<List<Object>> dataList) throws IOException {
        exportExcel(outputStream, sheetName, title, headList, dataList, null);
    }

    /**
     * 导出为Excel工作簿
     *
     * @param outputStream 输出流
     * @param title        标题
     * @param headList     表格头
     * @param dataList     表格内容数据
     * @param columnWidths 每一列的宽度
     */
    public static void exportExcel(OutputStream outputStream, String sheetName, String title, List<String> headList, List<List<Object>> dataList, List<Short> columnWidths) throws IOException {
        Workbook workbook = createWorkBook(sheetName, title, headList, dataList);
        setColSize(workbook.getSheet(sheetName), headList.size(), columnWidths);
        exportExcel(workbook, outputStream);
    }

    /**
     * 导出字节数组到输出流
     *
     * @param outputStream 输出流
     * @param data         字节数组数据
     * @throws IOException
     */
    public static void exportBytes(OutputStream outputStream, byte[] data) throws IOException {
        outputStream.write(data);
        outputStream.flush();
    }

    /**
     * 打开响应输出流
     *
     * @param response    响应
     * @param contentType 内容类型
     * @param fileName    文件名
     * @return
     * @throws IOException
     */
    public static OutputStream openOutputStream(HttpServletResponse response, ContentType contentType, String fileName) throws IOException {
        try {
            response.setContentType(contentType.toString());
            response.setCharacterEncoding("UTF-8");
            if (StringUtils.isNotBlank(fileName)) {
                fileName = new String(fileName.getBytes(), StandardCharsets.ISO_8859_1);
                response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            }
            return response.getOutputStream();
        } catch (IOException e) {
            log.error("It Caused Error On Openning OutputStream", e);
            throw e;
        }
    }

    /**
     * 创建工作簿
     *
     * @return
     */
    public static Workbook createWorkBook() {
        Workbook workbook = new HSSFWorkbook();
        return workbook;
    }

    /**
     * 添加工作表
     *
     * @return
     */
    public static Sheet addSheet(Workbook workbook, String sheetName, String title, List<String> headList, List<List<Object>> dataList) {
        Sheet sheet = workbook.createSheet(sheetName);
        CellStyle cellStyle = getDefaultStyle(workbook);
        int rowOffset;
        if (StringUtils.isNotBlank(title)) {
            //第一行放标题
            Row titleRow = sheet.createRow(0);
            createCell(titleRow, 0, title, cellStyle);
            //第二行放表头
            Row headRow = sheet.createRow(1);
            fullHeadRow(headRow, headList, cellStyle);
            mergedTitleRegion(sheet, (short) headList.size());
            rowOffset = 2;
        } else {
            //第一行放表头
            Row headRow = sheet.createRow(0);
            fullHeadRow(headRow, headList, cellStyle);
            rowOffset = 1;
        }
        addDataRows(sheet, rowOffset, dataList, cellStyle);
        return sheet;
    }

    /**
     * 自动调整列宽
     *
     * @param sheet 工作表
     * @param from  起始列
     * @param to    终止列
     */
    public static void autoColumnWidth(Sheet sheet, int from, int to) {
        for (; from < to; from++) {
            double width = SheetUtil.getColumnWidth(sheet, from, false);
            if (width != -1) {
                width *= 256 * 2;
                int maxColumnWidth = 255 * 256;
                if (width > maxColumnWidth) {
                    width = maxColumnWidth;
                }
                sheet.setColumnWidth(from, (int) (width));
            }
        }
    }

    /**
     * 创建工作簿
     *
     * @param sheetName 表格名称
     * @param title     标题
     * @param headList  表头
     * @param dataList  表格内容数据
     * @return
     */
    public static Workbook createWorkBook(String sheetName, String title, List<String> headList, List<List<Object>> dataList) {
        Workbook workbook = createWorkBook();
        addSheet(workbook, sheetName, title, headList, dataList);
        return workbook;
    }

    /**
     * 添加表格数据行
     *
     * @param sheet     表格
     * @param rowOffset 行偏移量
     * @param dataList  表格内容列表
     * @param cellStyle 单元格样式
     */
    public static void addDataRows(Sheet sheet, int rowOffset, List<List<Object>> dataList, CellStyle cellStyle) {
        for (int i = 0; i < dataList.size(); i++) {
            Row dataRow = sheet.createRow(i + rowOffset);
            List<Object> data = dataList.get(i);
            for (int l = 0; l < data.size(); l++) {
                Object content = data.get(l) == null ? "" : data.get(l);
                createCell(dataRow, l, content.toString(), cellStyle);
            }
        }
    }

    /**
     * 添加表格数据行
     *
     * @param sheet     表格
     * @param rowOffset 行偏移量
     * @param dataList  表格内容列表
     * @param cellStyle 单元格样式
     * @param addNo     是否添加序号
     */
    public static void addDataRows(Sheet sheet, int rowOffset, List<List<Object>> dataList, CellStyle cellStyle, boolean addNo) {
        for (int i = 0; i < dataList.size(); i++) {
            Row dataRow = sheet.createRow(i + rowOffset);
            List<Object> data = dataList.get(i);
            if (addNo) {
                createCell(dataRow, 0, String.valueOf(i + 1), cellStyle);
            }
            for (int l = 0; l < data.size(); l++) {
                Object content = data.get(l) == null ? "" : data.get(l);
                createCell(dataRow, addNo ? l + 1 : l, content.toString(), cellStyle);
            }
        }
    }

    /**
     * 填写表格头
     *
     * @param row       表格头那一行
     * @param headList  表格头数据
     * @param cellStyle 单元格样式
     */
    public static void fullHeadRow(Row row, List<String> headList, CellStyle cellStyle) {
        if (CollectionUtils.isNotEmpty(headList)) {
            for (int i = 0; i < headList.size(); i++) {
                createCell(row, i, headList.get(i), cellStyle);
            }
        }
    }

    /**
     * 创建单元格
     *
     * @param row       行
     * @param columnId  列ID
     * @param content   内容
     * @param cellStyle 单元格样式
     */
    public static void createCell(Row row, int columnId, String content, CellStyle cellStyle) {
        Cell cell = row.createCell(columnId, CellType.STRING);
        if (cellStyle != null) {
            cell.setCellStyle(cellStyle);
        }
        cell.setCellValue(content);
    }

    /**
     * 默认单元格样式
     *
     * @param workbook 工作簿
     * @return
     */
    public static CellStyle getDefaultStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(HorizontalAlignment.CENTER);
        // 设置单元格字体
        Font headerFont = workbook.createFont();
        headerFont.setFontHeightInPoints((short) 10);
        headerFont.setFontName("simsun");
        style.setFont(headerFont);
        style.setWrapText(true);

        // 设置单元格边框及颜色
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        return style;
    }

    /**
     * 指定表格每一列的宽度,如果不指定则将所有列宽度设为自适应,取最长的一行作为标准
     *
     * @param sheet           表格
     * @param columnLength    列长度
     * @param columnWidthList 宽度列表
     */
    public static void setColSize(Sheet sheet, int columnLength, List<Short> columnWidthList) {
        if (CollectionUtils.isEmpty(columnWidthList)) {
            for (int column = 0; column < columnLength; column++) {
                sheet.autoSizeColumn(column);
            }
        } else {
            for (int i = 0; i < columnWidthList.size(); i++) {
                Short columnWidth = columnWidthList.get(i);
                sheet.setColumnWidth(i, columnWidth > 255 ? 255 : columnWidth * 256);
            }
        }
    }

    /**
     * 合并标题单元格(第一行,从第一列开始到{@code lastCol}位置结束)
     *
     * @param sheet   表格
     * @param lastCol 合并单元格的最终位置
     */
    public static void mergedTitleRegion(Sheet sheet, short lastCol) {
        mergedRegion(sheet, (short) 0, (short) 0, (short) 0, lastCol);
    }

    /**
     * 合并单元格
     *
     * @param sheet    表格
     * @param firstRow 起始行
     * @param lastRow  终止行
     * @param firstCol 起始列
     * @param lastCol  终止列
     */
    public static void mergedRegion(Sheet sheet, short firstRow, short lastRow, short firstCol, short lastCol) {
        CellRangeAddress cellRangeAddress = new CellRangeAddress(firstRow, lastRow, firstCol, lastCol);
        sheet.addMergedRegion(cellRangeAddress);
    }
}
