package com.yochiu.fund.until;


import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

import com.yochiu.fund.support.ExcelColumn;
import com.yochiu.fund.support.ExcelValueConverter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import static com.yochiu.fund.support.ExcelColumn.DEFAULT_ORDER;
import static com.yochiu.fund.support.ExcelColumn.EMPTY;


/**
 * @Author: yohold
 * @Description:
 * @Date: 2019/6/18
 */

@Slf4j
public abstract class ExcelUtil {

    private ExcelUtil() {
    }

    private static final String xls = "xls";
    private static final String xlsx = "xlsx";



    public static void write(OutputStream outputStream, List<?> items) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            write(workbook, items);
            workbook.write(outputStream);
        }
    }

    public static void write(Workbook workbook, List<?> items) {
        if (!CollectionUtils.isEmpty(items)) {
            writeSheet(workbook, items);
        }
    }

    private static List<ColumnDesc> collectFieldByHeaders(Class<?> clz, String[] headers) {
        List<ColumnDesc> columnDescs = columnDescs(clz);
        return Arrays.stream(headers)
                .map(header -> getColumnDesc(header, columnDescs))
                .collect(Collectors.toList());
    }

    private static ColumnDesc getColumnDesc(String header, List<ColumnDesc> columnDescs) {
        for (ColumnDesc columnDesc : columnDescs) {
            if (columnDesc.propertyName.equals(header) || columnDesc.header.equals(header)) {
                return columnDesc;
            }
        }
        return null;
    }


    private static Workbook getWorkBook(InputStream in, String fileName) {
        Workbook workbook = null;
        if (fileName == null) {
            fileName = xls;
        }
        try {
            if (fileName.endsWith(xls)) {
                workbook = new HSSFWorkbook(in);
            } else if (fileName.endsWith(xlsx)) {
                workbook = new XSSFWorkbook(in);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse Excel file", e);
        }
        return workbook;
    }

    private static <T> T newInstance(Class<T> clz) {
        try {
            return clz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getCellValueString(Cell cell){
        if (cell == null)  {
            return null;
        }
        CellType cellType = cell.getCellTypeEnum();

        if (cellType == CellType.BLANK) {
            return null;
        }
        return getCellStringValue(cell);
    }

    private static Object getCellValue(String cellStringValue, Class<?> clz, String columnName) {
        try {
            if (StringUtils.isEmpty(cellStringValue)) {
                return null;
            }
            if (clz.equals(String.class)) {
                return cellStringValue;
            }
            if (clz.equals(Long.class) || clz.equals(long.class)) {
                return Long.valueOf(cellStringValue);
            }
            if (clz.equals(Integer.class) || clz.equals(int.class)) {
                return Integer.valueOf(cellStringValue);
            }
            if (clz.equals(Short.class) || clz.equals(short.class)) {
                return Short.valueOf(cellStringValue);
            }
            if (clz.equals(Byte.class) || clz.equals(byte.class)) {
                return Byte.valueOf(cellStringValue);
            }
            if (clz.equals(Float.class) || clz.equals(float.class)) {
                return Float.valueOf(cellStringValue);
            }
            if (clz.equals(Double.class) || clz.equals(double.class)) {
                return Double.valueOf(cellStringValue);
            }
            if (clz.equals(Boolean.class) || clz.equals(boolean.class)) {
                if (cellStringValue.equalsIgnoreCase("false")) {
                    return false;
                } else if (cellStringValue.equalsIgnoreCase("true")) {
                    return true;
                }
                throw new IllegalArgumentException(String.format("数据格式不合法 column %s value %s", columnName, cellStringValue));
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("数据错误:%s column %s value %s", e.getMessage(), columnName, cellStringValue), e);
        }
        throw new IllegalArgumentException(String.format("不支持的类型:%s column %s value %s", clz, columnName, cellStringValue));
    }

    private static String getCellStringValue(Cell cell) {
        return new DataFormatter().formatCellValue(cell);
    }

    private static String getColumnName(ColumnDesc columnDesc) {
        return columnDesc.getHeader() != null ? columnDesc.getHeader() : columnDesc.getPropertyName();
    }


    private static List<ColumnDesc> sortColumnDescByOrder(Class<?> clz) {
        List<ColumnDesc> columnDescs = columnDescs(clz);
        columnDescs.sort(Comparator.comparingInt(o -> o.order));
        return columnDescs;
    }


    private static void writeSheet(Workbook workbook, List<?> datas) {
        Sheet sheet = workbook.createSheet();
        List<ColumnDesc> columnDescs = sortColumnDescByOrder(datas.get(0).getClass());
        setColumnStyle(workbook, sheet, columnDescs);
        writeHeader(sheet, columnDescs);
        writeContens(sheet, datas, columnDescs);
    }

    private static void writeHeader(Sheet sheet, List<ColumnDesc> columnDescs) {
        Row row = sheet.createRow(0);
        Cell firstCell = row.createCell(0);
        writeHeader(firstCell, columnDescs);
    }

    private static Cell writeHeader(Cell startCell, List<ColumnDesc> columnDescs) {
        Cell currentCell = startCell;
        Cell endCell = null;
        for (ColumnDesc columnDesc : columnDescs) {
            endCell = writeHeader(currentCell, columnDesc);
            currentCell = getCell(endCell.getSheet(), endCell.getRowIndex(), endCell.getColumnIndex() + 1);
        }
        return endCell;
    }

    private static Cell writeHeader(Cell startCell, ColumnDesc columnDesc) {
        if (!columnDesc.isBasicType() || columnDesc.isCollection()) {
            return writeHeader(startCell, columnDesc.getChilds());
        } else {
            startCell.setCellValue(columnDesc.getHeader());
            return startCell;
        }
    }

    private static void writeContens(Sheet sheet, List<?> objs, List<ColumnDesc> columnDescs) {
        Cell startCell = sheet.createRow(1).createCell(0);
        writeDataList(objs, startCell, columnDescs);
    }

    private static Cell writeDataList(Iterable<?> objs, Cell startCell, List<ColumnDesc> columnDescs) {
        Cell currentCell = startCell;
        Cell endCell = null;
        for (Object obj : objs) {
            endCell = writeNonBasicType(columnDescs, currentCell, obj);
            currentCell = getCell(endCell.getSheet(), endCell.getRowIndex() + 1, startCell.getColumnIndex());
        }
        return endCell;
    }

    /**
     * @param columnDesc 要写的字段详情
     * @param startCell  写入的起始cell
     * @param obj        字段所属的对象
     * @return
     */
    private static Cell writeField(ColumnDesc columnDesc, Cell startCell, Object obj) {
        Object field = invokeGetter(columnDesc, obj);
        if (columnDesc.isCollection()) {
            return writeDataList((Iterable<?>) field, startCell, columnDesc.getChilds());
        } else if (!columnDesc.isBasicType()) {
            return writeNonBasicType(columnDesc.getChilds(), startCell, field);
        } else {
            // 基本类型, 直接写
            startCell.setCellValue(getValue(columnDesc, field));
            return startCell;
        }
    }

    private static Cell writeNonBasicType(List<ColumnDesc> columnDescs, Cell startCell, Object obj) {
        Cell currentCell = startCell;
        Cell endCell = null;
        for (ColumnDesc childColumnDesc : columnDescs) {
            endCell = writeField(childColumnDesc, currentCell, obj);
            currentCell = getCell(currentCell.getSheet(), endCell.getRowIndex(),
                    endCell.getColumnIndex() + 1);
        }
        return endCell;
    }

    private static Object invokeGetter(ColumnDesc columnDesc, Object obj) {
        try {
            return columnDesc.getGetter().invoke(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getValue(ColumnDesc columnDesc, Object value) {

        try {
            if (value == null) {
                return columnDesc.getDefaultValue();
            }
            ExcelValueConverter excelValueConverter = columnDesc.getExcelValueConverter();
            if (excelValueConverter != null) {
                return excelValueConverter.converter(value);
            }
            return value.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Cell getCell(Sheet sheet, int rowIdx, int columnIdx) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) {
            row = sheet.createRow(rowIdx);
        }
        Cell cell = row.getCell(columnIdx);
        if (cell == null) {
            cell = row.createCell(columnIdx);
        }
        return cell;
    }

    private static List<ColumnDesc> columnDescs(Class<?> clz) {
        try {
            return Arrays.stream(Introspector.getBeanInfo(clz, Object.class)
                    .getPropertyDescriptors())
                    .filter(pd -> Objects.nonNull(pd.getReadMethod()))
                    .map(pd -> {
                        ColumnDesc columnDesc = new ColumnDesc();
                        try {
                            Field field = getField(clz, pd.getName());
                            ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
                            if (annotation != null) {
                                if (annotation.ignore()) {
                                    return null;
                                }
                                columnDesc.header = annotation.header().equals(EMPTY) ? pd.getName() : annotation.header();
                                columnDesc.order = annotation.order();
                                columnDesc.defaultValue = annotation.defaultValue();
                                columnDesc.cellType = annotation.cellType();
                                if (!annotation.valueConverter().equals(ExcelValueConverter.class)) {
                                    columnDesc.excelValueConverter = newInstance(annotation.valueConverter());
                                }
                            } else {
                                columnDesc.header = pd.getName();
                                columnDesc.order = DEFAULT_ORDER;
                                columnDesc.defaultValue = EMPTY;
                            }
                            columnDesc.type = field.getType();
                            columnDesc.getter = pd.getReadMethod();
                            columnDesc.setter = pd.getWriteMethod();
                            columnDesc.propertyName = pd.getName();

                            if (isCollection(field.getType())) {
                                columnDesc.setCollection(true);
                                columnDesc.setBasicType(false);
                                Class<?> childClz =
                                        (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];

                                if (isBasicType(childClz)) {
                                    throw new IllegalArgumentException("do not support List<java.lang.*> type");
                                } else if (isCollection(childClz)) {
                                    throw new IllegalArgumentException("do not support List<List<?>> type");
                                } else {
                                    columnDesc.setChilds(sortColumnDescByOrder(childClz));
                                }
                            } else if (!isBasicType(field.getType())) {
                                columnDesc.setBasicType(false);
                                columnDesc.setChilds(sortColumnDescByOrder(field.getType()));
                            }
                            return columnDesc;
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setColumnStyle(Workbook workbook, Sheet sheet, List<ColumnDesc> columnDescs) {
        for (int i = 0; i < columnDescs.size(); i++) {
            String cellFormat = columnDescs.get(i).cellType;
            if (StringUtils.isEmpty(cellFormat)) {
                continue;
            }
            DataFormat fmt = workbook.createDataFormat();
            CellStyle textStyle = workbook.createCellStyle();
            textStyle.setDataFormat(fmt.getFormat(cellFormat));
            sheet.setDefaultColumnStyle(i, textStyle);
        }
    }

    private static boolean isEmpty(Row row) {
        if (row == null) {
            return true;
        }
        if (row.getLastCellNum() <= 0) {
            return true;
        }
        for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
            Cell cell = row.getCell(cellNum);
            if (cell != null && cell.getCellTypeEnum() != CellType.BLANK && !StringUtils.isEmpty(cell.toString())) {
                return false;
            }
        }
        return true;
    }

    private static Field getField(Class<?> clazz, String name) {
        Class<?> searchType = clazz;
        while (Object.class != searchType && searchType != null) {
            Field[] fields = searchType.getDeclaredFields();
            for (Field field : fields) {
                if (name.equals(field.getName())) {
                    return field;
                }
            }
            searchType = searchType.getSuperclass();
        }
        throw new IllegalArgumentException(String.format("class %s can not find field %s", clazz.getName(), name));
    }

    private static boolean isCollection(Class<?> clazz) {
        return Iterable.class.isAssignableFrom(clazz);
    }

    private static boolean isBasicType(Class<?> clazz) {
        return clazz.getPackage().getName().contains("java.");
    }

    @Data
    private static class ColumnDesc {
        private Method getter;
        private Method setter;
        private int order;
        // excel 表头名称
        private String header;
        // 对象属性名称
        private String propertyName;
        private Class<?> type;
        private String defaultValue;
        private String cellType;
        private boolean isCollection = false;
        private boolean isBasicType = true;
        private ExcelValueConverter excelValueConverter;
        private List<ColumnDesc> childs = Collections.emptyList();

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(propertyName);
            if (!childs.isEmpty()) {
                sb.append(childs.toString());
            }
            return sb.toString();
        }
    }
}