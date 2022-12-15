package task2;

import javax.naming.OperationNotSupportedException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class AttributeFillerFromProperties {
    /**
     * This method loads properties from a file to the attributes of an object of a certain class
     * @param cls - class to create an instance of
     * @param propertiesPath - file to copy properties from
     * @return classInstance - an object to be created
     */
    public static <T>T loadFromProperties(Class<T> cls, Path propertiesPath) throws NoSuchMethodException,
            InvocationTargetException, InstantiationException, IllegalAccessException, IOException,
            OperationNotSupportedException, NoSuchFieldException {
        T classInstance = cls.getDeclaredConstructor().newInstance();
        Field[] fields = cls.getDeclaredFields();
        Properties properties = new Properties();
        properties.load(new FileReader(propertiesPath.toString()));
        Set<Map.Entry<Object, Object>> propertySet = properties.entrySet();

        for (Field field : fields) {
            field.setAccessible(true);
            field.set(classInstance, getFieldValue(propertySet, field));
        }
        return classInstance;
    }

    /**
     * This method parses given file and transfers values from its properties to the object attributes
     * @param propertySet - set of properties from the file
     * @param field - field of an object to map value from properties
     * @return value of certain type or an exception
     */
    public static Object getFieldValue (Set<Map.Entry<Object, Object>> propertySet, Field field) throws NoSuchFieldException,
            OperationNotSupportedException {
        String name;
        if (field.isAnnotationPresent(Property.class) && !field.getAnnotation(Property.class).name().equals("")) {
            name = field.getAnnotation(Property.class).name();
        } else {
            name = field.getName();
        }
        if (propertySet.stream().map(entry -> entry.getKey().toString()).noneMatch(name::equals)) {
            throw new NoSuchFieldException("No field matched with properties file");
        }
        for(Map.Entry<Object, Object> map : propertySet) {
            if (name.equals(map.getKey())) {
                Class<?> classType = field.getType();
                if (String.class.equals(classType)) {
                    return String.valueOf(map.getValue());
                } else if (int.class.equals(classType) || Integer.class.equals(classType)) {
                    return Integer.parseInt(String.valueOf(map.getValue()));
                } else if (Instant.class.equals(classType)) {
                    if (!field.getAnnotation(Property.class).format().equals("")) {
                        String format = field.getAnnotation(Property.class).format();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format, Locale.US);
                        LocalDateTime transformedField = LocalDateTime.parse(String.valueOf(map.getValue()),formatter);
                        return transformedField.toInstant(ZoneOffset.UTC);
                    }
                    return Instant.parse(String.valueOf(map.getValue()));
                }
            }
        }
        return new OperationNotSupportedException("Invalid type of the field");
    }
}
