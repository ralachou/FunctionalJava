import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class ObjectListToFileExporter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        // Sample list of objects (Replace with actual Vasara objects)
        Map<String, Object> objectMap = new LinkedHashMap<>();
        objectMap.put("person_1", new Person("Alice", 28, "Engineer"));
        objectMap.put("person_2", new Person("Bob", 34, "Doctor"));
        objectMap.put("person_3", new Person("Charlie", 25, "Artist"));

        // Convert and save to different formats
        saveListAsJson(objectMap, "people.json");
        saveListAsCsv(objectMap, "people.csv");
        saveListAsExcel(objectMap, "people.xlsx");

        System.out.println("Export completed: people.json, people.csv, people.xlsx");
    }

    // Save list of objects as JSON with keys and Java types
    public static void saveListAsJson(Map<String, Object> objectMap, String filePath) throws IOException {
        List<Map<String, Object>> records = new ArrayList<>();

        for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
            Map<String, Object> record = new LinkedHashMap<>();
            record.put("key", entry.getKey());
            record.put("object_type", entry.getValue().getClass().getName());
            record.putAll(getObjectAttributes(entry.getValue()));
            records.add(record);
        }

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), records);
    }

    // Save list of objects as CSV with keys and Java types
    public static void saveListAsCsv(Map<String, Object> objectMap, String filePath) throws IOException {
        if (objectMap.isEmpty()) {
            System.out.println("No data to write to CSV.");
            return;
        }

        List<Map<String, String>> records = new ArrayList<>();
        for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
            Map<String, String> record = new LinkedHashMap<>();
            record.put("key", entry.getKey());
            record.put("object_type", entry.getValue().getClass().getName());
            record.putAll(getObjectAttributes(entry.getValue()));
            records.add(record);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(records.get(0).keySet().toArray(new String[0])))) {
            for (Map<String, String> record : records) {
                csvPrinter.printRecord(record.values());
            }
        }
    }

    // Save list of objects as Excel with keys and Java types
    public static void saveListAsExcel(Map<String, Object> objectMap, String filePath) throws IOException {
        if (objectMap.isEmpty()) {
            System.out.println("No data to write to Excel.");
            return;
        }

        List<Map<String, String>> records = new ArrayList<>();
        for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
            Map<String, String> record = new LinkedHashMap<>();
            record.put("key", entry.getKey());
            record.put("object_type", entry.getValue().getClass().getName());
            record.putAll(getObjectAttributes(entry.getValue()));
            records.add(record);
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Data");

        // Create header row
        Row headerRow = sheet.createRow(0);
        Set<String> headers = records.get(0).keySet();
        int colNum = 0;
        for (String header : headers) {
            headerRow.createCell(colNum++).setCellValue(header);
        }

        // Populate rows with data
        int rowNum = 1;
        for (Map<String, String> record : records) {
            Row row = sheet.createRow(rowNum++);
            colNum = 0;
            for (String value : record.values()) {
                row.createCell(colNum++).setCellValue(value);
            }
        }

        // Write to file
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        }
        workbook.close();
    }

    // Extract attributes using reflection (Fields, Methods, Nested Collections)
    private static Map<String, Object> getObjectAttributes(Object obj) {
        Map<String, Object> attributes = new LinkedHashMap<>();

        if (obj == null) {
            return attributes;
        }

        Class<?> clazz = obj.getClass();

        // Extract fields (including private and protected)
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                attributes.put(field.getName(), processValue(value));
            } catch (IllegalAccessException e) {
                attributes.put(field.getName(), "ERROR_ACCESS");
            }
        }

        // Extract methods (only getters / meaningful methods)
        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers()) && method.getParameterCount() == 0) {
                try {
                    Object value = method.invoke(obj);
                    attributes.put(method.getName() + "()", processValue(value));
                } catch (Exception ignored) {
                }
            }
        }

        return attributes;
    }

    // Process values (Handle Maps, Lists, Iterables, Nested Objects)
    private static Object processValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Map<?, ?>) {
            Map<String, Object> mapData = new LinkedHashMap<>();
            ((Map<?, ?>) value).forEach((k, v) -> mapData.put(String.valueOf(k), processValue(v)));
            return mapData;
        }

        if (value instanceof Collection<?>) {
            List<Object> listData = new ArrayList<>();
            for (Object item : (Collection<?>) value) {
                listData.add(processValue(item));
            }
            return listData;
        }

        if (value.getClass().isArray()) {
            List<Object> arrayData = new ArrayList<>();
            for (Object item : (Object[]) value) {
                arrayData.add(processValue(item));
            }
            return arrayData;
        }

        // If value is an object, get its attributes recursively
        if (!(value instanceof String) && !(value instanceof Number) && !(value instanceof Boolean)) {
            return getObjectAttributes(value);
        }

        return value;
    }
}

// Sample Java Object Class
class Person {
    private String name;
    protected int age;
    private String profession;
    private List<String> skills;
    private Map<String, String> attributes;

    public Person(String name, int age, String profession) {
        this.name = name;
        this.age = age;
        this.profession = profession;
        this.skills = Arrays.asList("Java", "Python", "SQL");
        this.attributes = new HashMap<>();
        this.attributes.put("Experience", "5 years");
        this.attributes.put("Certifications", "AWS Certified");
    }

    public String getFullName() {
        return "Mr./Ms. " + name;
    }
}
