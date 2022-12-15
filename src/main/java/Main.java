import task2.AttributeFillerFromProperties;
import task2.ClassToFillFromProperties;

import javax.naming.OperationNotSupportedException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) {
        //Task 1
        System.out.println("2 threads: ");
        try {
            Map<String, Double> mapOfFines = JsonFinesParser.calculateFinesByTypeJson(new File("./in/fines/"));
            JsonFinesParser.createXmlOfFines(mapOfFines, new File("./out/totalFines2.xml"));
        } catch (IOException | ExecutionException | InterruptedException | ParserConfigurationException |
                 TransformerException e) {
            e.printStackTrace();
        }

        //Task 2
        try {
            ClassToFillFromProperties classToFillFromProperties = AttributeFillerFromProperties.loadFromProperties(task2.ClassToFillFromProperties.class, Path.of("D:\\ProfitSoft_Lecture_5_6\\src\\main\\resources\\task2.properties"));
            System.out.println(classToFillFromProperties.toString());
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            System.out.println("Error occurred during execution of this method");
        } catch (IOException e) {
            System.out.println("Path not found");
        } catch (OperationNotSupportedException e) {
            System.out.println("Check the types of attributes");
        } catch (NoSuchFieldException e) {
            System.out.println("Names of the fields and properties do not match");
        }

    }
}
