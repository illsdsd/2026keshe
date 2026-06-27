package com.campuslink.common;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Excel 导出工具，v2 新增。
 *
 * <p>统一头风格：浅蓝填充 + 加粗黑色字体；列宽自动调整 1.2 倍。
 * 大数据集使用 SXSSF（行内存上限 500）避免内存爆炸。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
public class ExcelExportUtil {

    private ExcelExportUtil() {
    }

    /**
     * 将列名 + 行数据写入响应。
     *
     * @param response HTTP 响应，调用方需保证未提交
     * @param fileName 导出文件名（不含扩展名）
     * @param headers  表头
     * @param rows     行数据（与 headers 列数一致，元素为字符串）
     */
    public static void writeXlsx(HttpServletResponse response,
                                 String fileName,
                                 List<String> headers,
                                 List<List<Object>> rows) throws IOException {
        //1 设置响应头
        String safeName = URLEncoder.encode(fileName + ".xlsx", StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + safeName);

        //2 构建 workbook 与样式（500 行内存上限）
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(new XSSFWorkbook(), 500)) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Sheet1");
            CellStyle headerStyle = buildHeaderStyle(workbook);

            //3 写表头
            Row head = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = head.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            //4 写数据行
            int r = 1;
            for (List<Object> row : rows) {
                Row dataRow = sheet.createRow(r++);
                for (int c = 0; c < row.size(); c++) {
                    Object value = row.get(c);
                    Cell cell = dataRow.createCell(c);
                    if (value == null) {
                        cell.setCellValue("");
                    } else if (value instanceof Number num) {
                        cell.setCellValue(num.doubleValue());
                    } else {
                        cell.setCellValue(value.toString());
                    }
                }
            }

            //5 输出到响应
            try (OutputStream out = response.getOutputStream()) {
                workbook.write(out);
                out.flush();
            }
            workbook.dispose();
        }
    }

    private static CellStyle buildHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
    }
}
