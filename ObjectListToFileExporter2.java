import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

public class ObjectListToFileExporter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        // Sample list of objects (Replace this with your list of Java objects)
        List<Person> people = Arrays.asList(
                new Person("Alice", 28, "Engineer"),
                new Person("Bob", 34, "Doctor"),
                new Person("Charlie", 25, "Artist")
        );

        // Convert list to JSON, CSV, and Excel
        saveListAsJson(people, "people.json");
        saveListAsCsv(people, "people.csv");
        saveListAsExcel(people, "people.xlsx");

        System.out.println("Export completed: people.json, people.csv, people.xlsx");
    }

    // Save list of objects as JSON
    public static <T> void saveListAsJson(List<T> objects, String filePath) throws IOException {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), objects);
    }

    // Save list of objects as CSV
    public static <T> void saveListAsCsv(List<T> objects, String filePath) throws IOException {
        if (objects.isEmpty()) {
            System.out.println("No data to write to CSV.");
            return;
        }

        List<Map<String, String>> records = extractAttributes(objects);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(records.get(0).keySet().toArray(new String[0])))) {
            for (Map<String, String> record : records) {
                csvPrinter.printRecord(record.values());
            }
        }
    }

    // Save list of objects as Excel
    public static <T> void saveListAsExcel(List<T> objects, String filePath) throws IOException {
        if (objects.isEmpty()) {
            System.out.println("No data to write to Excel.");
            return;
        }

        List<Map<String, String>> records = extractAttributes(objects);
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

    // Extracts attributes from a list of objects using reflection
    private static <T> List<Map<String, String>> extractAttributes(List<T> objects) {
        List<Map<String, String>> records = new ArrayList<>();
        for (T obj : objects) {
            records.add(getObjectAttributes(obj));
        }
        return records;
    }

    // Extract attributes from a single object
    private static <T> Map<String, String> getObjectAttributes(T obj) {
        Map<String, String> attributes = new LinkedHashMap<>();

        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                attributes.put(field.getName(), String.valueOf(field.get(obj)));
            } catch (IllegalAccessException e) {
                attributes.put(field.getName(), "ERROR");
            }
        }
        return attributes;
    }
}

// Sample Java Object Class
class Person {
    private String name;
    private int age;
    private String profession;

    public Person(String name, int age, String profession) {
        this.name = name;
        this.age = age;
        this.profession = profession;
    }
}
