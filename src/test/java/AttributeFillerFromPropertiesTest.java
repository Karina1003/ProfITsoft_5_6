import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task2.AttributeFillerFromProperties;
import task2.ClassToFillFromProperties;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.time.Instant;

public class AttributeFillerFromPropertiesTest {
    ClassToFillFromProperties classToFillFromProperties;

    @Test
    void loadFromPropertiesTest() {
        try {
            classToFillFromProperties = AttributeFillerFromProperties.loadFromProperties(ClassToFillFromProperties.class, Path.of("D:\\ProfitSoft_Lecture_5_6\\src\\main\\resources\\task2.properties"));
        } catch (IOException | IllegalAccessException | NoSuchMethodException | InvocationTargetException |
                 InstantiationException | OperationNotSupportedException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        Assertions.assertEquals(classToFillFromProperties.getStringProperty(), "value1");
        Assertions.assertEquals(classToFillFromProperties.getMyNumber(), 10);
        Assertions.assertEquals(classToFillFromProperties.getTimeProperty(), Instant.parse("2022-11-29T18:30:00Z"));
    }

    @Test
    void loadFromPropertiesFailedTest() {
        Assertions.assertThrows(NoSuchFieldException.class, () -> AttributeFillerFromProperties.loadFromProperties(ClassToFillFromProperties.class, Path.of("src/main/resources/failedTest.properties")));
    }

}
