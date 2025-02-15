import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class ObjectToJsonConverter {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Set<Object> visitedObjects = Collections.newSetFromMap(new IdentityHashMap<>());

    public static void main(String[] args) throws Exception {
        // Sample Java Object with nested values, lists, and maps
        Person person = new Person("Alice", 28, "Engineer");
        person.addSkill("Java");
        person.addSkill("Python");
        person.addAttribute("Experience", "5 years");
        person.addAttribute("Certifications", "AWS Certified");

        // Convert and print JSON
        String jsonResult = convertObjectToJson(person);
        System.out.println(jsonResult);
    }

    // Entry function to convert any object to JSON
    public static String convertObjectToJson(Object obj) throws Exception {
        if (obj == null) return "{}";

        // Reset visitedObjects to prevent cross-object reference loops
        visitedObjects.clear();

        // Convert object to a structured Map
        Map<String, Object> jsonStructure = processObject(obj);

        // Convert Map to JSON string
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonStructure);
    }

    // Recursively processes an object and returns its structured representation
    private static Map<String, Object> processObject(Object obj) {
        if (obj == null) return null;
        if (visitedObjects.contains(obj)) return Map.of("error", "Circular reference detected");

        visitedObjects.add(obj);

        Map<String, Object> attributes = new LinkedHashMap<>();
        Class<?> clazz = obj.getClass();
        attributes.put("object_type", clazz.getName());

        // Process Fields (including private and protected)
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                attributes.put(field.getName(), processValue(value));
            } catch (IllegalAccessException e) {
                attributes.put(field.getName(), "ACCESS_ERROR");
            }
        }

        // Process Methods (only getters / useful public methods)
        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers()) && method.getParameterCount() == 0 && method.getReturnType() != void.class) {
                try {
                    String methodName = method.getName();
                    if (methodName.startsWith("get") || methodName.startsWith("is")) {
                        attributes.put(methodName + "()", processValue(method.invoke(obj)));
                    }
                } catch (Exception ignored) {}
            }
        }

        return attributes;
    }

    // Processes all possible value types, including collections and maps
    private static Object processValue(Object value) {
        if (value == null) return null;

        if (value instanceof String || value instanceof Number || value instanceof Boolean || value instanceof Enum) {
            return value;
        }

        if (value instanceof Map<?, ?>) {
            Map<String, Object> processedMap = new LinkedHashMap<>();
            ((Map<?, ?>) value).forEach((k, v) -> processedMap.put(String.valueOf(k), processValue(v)));
            return processedMap;
        }

        if (value instanceof Collection<?>) {
            List<Object> processedList = new ArrayList<>();
            for (Object item : (Collection<?>) value) {
                processedList.add(processValue(item));
            }
            return processedList;
        }

        if (value.getClass().isArray()) {
            List<Object> processedArray = new ArrayList<>();
            for (Object item : (Object[]) value) {
                processedArray.add(processValue(item));
            }
            return processedArray;
        }

        // If the object is a custom object, recurse into its attributes
        return processObject(value);
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
        this.skills = new ArrayList<>();
        this.attributes = new HashMap<>();
    }

    public void addSkill(String skill) {
        skills.add(skill);
    }

    public void addAttribute(String key, String value) {
        attributes.put(key, value);
    }

    public String getFullName() {
        return "Mr./Ms. " + name;
    }
}
