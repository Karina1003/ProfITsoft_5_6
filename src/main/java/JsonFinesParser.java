import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class JsonFinesParser {

    /**
     * This method checks a directory of JSON files or a single JSON file and
     * sums violations from all files by type in asynchronous mode
     * @param jsonToParse - a file or a directory to look through and parse
     * @return totalFinesMap - a map of calculated and sorted in reverse order violations
     * @throws IOException - an exception that can be thrown by invalid or missing file "jsonToParse"
     */
    public static Map<String,Double> calculateFinesByTypeJson(File jsonToParse) throws IOException, ExecutionException, InterruptedException {
        Map<String,Double> totalFinesMap = new HashMap<>();
        File[] fileList;
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CompletableFuture<Void> future;
        if (jsonToParse.isDirectory()) {
            fileList = jsonToParse.listFiles();
        } else {
            fileList = new File[]{jsonToParse};
        }
        if (fileList != null) {
            long startTime = System.currentTimeMillis();
            for (File fileJson : fileList) {
                if (fileJson != null && fileJson.length() != 0 && fileJson.getName().endsWith(".json")) {
                    future = CompletableFuture.supplyAsync(()->parseFilesIntoMapJson(fileJson),executor).thenAccept(resultMap -> {
                        synchronized (totalFinesMap) {
                            for (Map.Entry<String,Double> entry : resultMap.entrySet()) {
                                if(totalFinesMap.containsKey(entry.getKey())) {
                                    totalFinesMap.put(entry.getKey(), totalFinesMap.get(entry.getKey())+entry.getValue());
                                } else {
                                    totalFinesMap.put(entry.getKey(), entry.getValue());
                                }
                            }
                        }
                    });
                    future.get();
                }
            }
            System.out.println((System.currentTimeMillis()-startTime)+" ms");
        }
        executor.shutdown();
        return totalFinesMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue(),(n, m)->n, LinkedHashMap<String,Double>::new));
    }

    /**
     * This method parses a file to pick a type of violation and a sum of fine
     * @param fileJson - a file to parse
     * @return fineAmountMap - a map of violations
     */
    public static Map<String,Double> parseFilesIntoMapJson(File fileJson) {
        Map<String,Double> fineAmountMap = new HashMap<>();
        JsonFactory jsonFactory = new JsonFactory();
        try (JsonParser jsonParser = jsonFactory.createParser(fileJson)) {
            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                String name = jsonParser.getCurrentName();
                String type;
                if ("type".equals(name)) {
                    jsonParser.nextToken();
                    type = jsonParser.getValueAsString();
                    jsonParser.nextToken();
                    name = jsonParser.getCurrentName();
                    if ("fine_amount".equals(name)) {
                        jsonParser.nextToken();
                        if (!fineAmountMap.containsKey(type)) {
                            fineAmountMap.put(type, jsonParser.getDoubleValue());
                        } else {
                            fineAmountMap.put(type, fineAmountMap.get(type) + jsonParser.getDoubleValue());
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error occurred during file parsing");
        }
        return fineAmountMap;
    }

    /**
     * This method creates an XML-file of fines based on data in the Map parameter
     * @param mapOfFines - map of calculated fines sorted in descending order
     * @param fileXml - XML file to be created
     * @throws ParserConfigurationException - an exception that can be thrown
     * by newDocumentBuilder() method of DocumentBuilderFactory class
     * @throws TransformerException - an exception that can be thrown by writeXml() method
     */
    public static void createXmlOfFines (Map<String, Double> mapOfFines, File fileXml) throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.newDocument();
        // root element
        Element rootElement = doc.createElement("violations");
        doc.appendChild(rootElement);
        // add xml elements
        if (mapOfFines != null) {
            Set<Map.Entry<String, Double>> fineEntrySet = mapOfFines.entrySet();
            for (Map.Entry<String, Double> fineEntry : fineEntrySet) {
                Element violation = doc.createElement("violation");
                rootElement.appendChild(violation);
                Element type = doc.createElement("type");
                type.setTextContent(fineEntry.getKey());
                violation.appendChild(type);
                Element totalAmount = doc.createElement("total_amount");
                totalAmount.setTextContent(fineEntry.getValue().toString());
                violation.appendChild(totalAmount);
            }
        }
        writeXml(doc, fileXml);
    }

    /**
     * This method writes an XML file of structure defined in doc parameter to a given file
     * @param doc - represents an XML document of a certain structure
     * @param fileXml - XML-file to be created or updated
     * @throws TransformerException - an exception that can be thrown
     * by transform() method of Transformer class
     */
    private static void writeXml(Document doc, File fileXml) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(fileXml);

        transformer.transform(source, result);
    }
}
