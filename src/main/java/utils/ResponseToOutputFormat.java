package utils;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.soap.Node;
import javax.xml.stream.*;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static telematics.GetTelematicsData.kp;
import static telematics.GetTelematicsData.ta;
import static utils.OutputFormatEnum.KAFKA;

/**
 * This class is responsible for formatting and outputting the response retrieved the by api command classes
 *
 * @author  Pascal De Poorter
 * @version 1.0
 * @since   14-03-2017
 */
public class ResponseToOutputFormat {

    private static String delimiter = null;
    private static String lastID = null;
    private static InputStream response = null;
    private static List<String> responseOfStrings = null;
    private static String method = null;
    private static String recordID = null;
    private static boolean responseList = false;

    /**
     * Start parsing the response. All parameters should have been provided by the setter methods or retrieved
     * from the TreadArguments class
     */
    public static void parse() {
        switch (ta.getOutputFormat()) {
            case CSV:
            case KAFKA:
                if (responseList) parseToCSV(responseOfStrings);
                else parseToCSV(response, recordID, ta.getHeader(), ta.getOutputFormat());
                break;
            case XML:
                if (!responseList) parseToXML(response, method);
                break;
            default:
                System.err.println("Unhandled output method received!");
        }
    }

    /**
     * Parse the response, creating records on the event method level
     *
     * @param is          response passed as xml format
     * @param eventMethod record delimiter for determining record level
     */
    private static void parseToXML(InputStream is, String eventMethod) {
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLOutputFactory oFactory = XMLOutputFactory.newInstance();
            XMLEventReader eventReader = factory.createXMLEventReader(is);
            XMLEventWriter eventWriter = null;
            StringWriter sw = new StringWriter();

            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                if (event.isStartElement() &&
                        ((StartElement) event).getName().getLocalPart().equals(eventMethod + "Result")) {
                    eventWriter = oFactory.createXMLEventWriter(sw);
                } else if (event.isEndElement() &&
                        ((EndElement) event).getName().getLocalPart().equals(eventMethod + "Result")) {
                    break;
                } else if (eventWriter != null) {
                    eventWriter.add(event);
                }
            }
            if (eventWriter != null) eventWriter.close();
            System.out.println(sw);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    private static void parseToCSV(List<String> outputList) {
        if (ta.getOutputFormat() == KAFKA) {
            outputList.forEach(s -> {
                String ID = s.substring(0, s.indexOf(";"));
                kp.sendMessage(ta.getKafkaTopic(), ID, s);
            });
        } else {
            outputList.forEach(System.out::println);
        }
    }

    private static void parseToCSV(InputStream is, String recordIdentifier, boolean withHeader, OutputFormatEnum format) {
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLOutputFactory oFactory = XMLOutputFactory.newInstance();
            XMLEventReader eventReader = factory.createXMLEventReader(is);
            XMLEventWriter eventWriter = null;
            StringWriter sw = null;
            Boolean processHeader = withHeader;

            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                if (event.isStartElement() &&
                        ((StartElement) event).getName().getLocalPart().equals(recordIdentifier)) {
                    sw = new StringWriter();
                    eventWriter = oFactory.createXMLEventWriter(sw);
                    if (eventWriter != null) eventWriter.add(event);
                } else if (event.isEndElement() &&
                        ((EndElement) event).getName().getLocalPart().equals(recordIdentifier)) {
                    assert eventWriter != null;
                    eventWriter.add(event);
                    String recordXML = sw.toString().replace("<" + recordIdentifier,
                            "<" + recordIdentifier + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
                    if (processHeader) {
                        processXMLRecord(recordXML, true, format);
                        processHeader = false;
                    }
                    processXMLRecord(recordXML, processHeader, format);
                    eventWriter = null;
                    sw.close();

                } else if (eventWriter != null) {
                    eventWriter.add(event);
                }
            }
            if (eventWriter != null) eventWriter.close();
        } catch (XMLStreamException | IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public static String getLastID() {
        return lastID;
    }

    private static void processXMLRecord(String source, boolean processHeader, OutputFormatEnum format) {
        try {
            DOMResult result = new DOMResult();
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer t = tFactory.newTransformer();
            t.transform(new StreamSource(new StringReader(source)), result);
            String record;
            record = processNodeList(result.getNode().getChildNodes(), true, "", processHeader);
            lastID = record.substring(0, record.indexOf(";"));
            if ((ta.getMaxID() == null) || (lastID.compareTo(ta.getMaxID().toString()) <= 0)) {
                if (format == KAFKA) {
                    kp.sendMessage(ta.getKafkaTopic(), lastID, record);
                } else {
                    System.out.println(record);
                }
                if (NumberUtils.isNumber(lastID)) ta.setLastEventID(Integer.parseInt(lastID));
            } else {
                if (ta.getMaxID() != null) ta.setContinuous(false);
            }
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    private List<String> processXMLRecord(String source) {
        List<String> fields = new ArrayList<>();

        return fields;
    }

    private static String processNodeList(NodeList nodes, boolean firstPass, String level, boolean processHeader) {
        String fieldLevel = "";
        String recordField = "";
        for (int i = 0; i < nodes.getLength(); i++) {
            if (firstPass) {
                delimiter = "";
            } else if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE
                    && nodes.item(i).hasChildNodes()
                    && nodes.item(i).getFirstChild().getNodeType() == Node.ELEMENT_NODE) {
                fieldLevel = level + nodes.item(i).getNodeName() + "_";
            } else if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE
                    && nodes.item(i).hasChildNodes()
                    && nodes.item(i).getFirstChild().getNodeType() == Node.TEXT_NODE) {
                if (processHeader) {
                    recordField = recordField + delimiter + level + nodes.item(i).getNodeName();
                    delimiter = ";";
                }
            } else if (nodes.item(i).getNodeType() == Node.TEXT_NODE) {
                if (!processHeader) {
                    recordField = recordField + delimiter + nodes.item(i).getTextContent();
                    delimiter = ";";
                }
            }
            recordField = recordField + processNodeList(nodes.item(i).getChildNodes(), false, fieldLevel, processHeader);
        }
        return recordField;
    }

    private static String parseRecordDefinition(InputStream is, String recordIdentifier) throws XMLStreamException, IOException {
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLOutputFactory oFactory = XMLOutputFactory.newInstance();
            XMLEventReader eventReader = factory.createXMLEventReader(is);
            XMLEventWriter eventWriter = null;
            StringWriter sw = null;

            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                if (event.isStartElement()) {
                    Iterator<Attribute> attributes  = ((StartElement) event).getAttributes();
                    String type = null;
                    boolean recordIDfound = false;
                    while (attributes.hasNext()) {
                        Attribute attribute = attributes.next();
                        if (attribute.getName().toString().equals("name") &&
                                attribute.getValue().equals(recordIdentifier)) {
                            recordIDfound = true;
                        }
                        if (attribute.getName().toString().equals("type")) {
                            type =  attribute.getValue();
                        }
                    }
                    if (recordIDfound) {
                        if (type == null) {
                            sw = new StringWriter();
                            eventWriter = oFactory.createXMLEventWriter(sw);

                            sw = new StringWriter();
                            if (eventWriter != null) eventWriter.add(event);
                        } else {
                            if (!type.substring(4).equals(recordIdentifier)) {
                                return parseRecordDefinition(is, type.substring(4));
                            }
                        }

                    }

                } else if (event.isEndElement() &&
                        ((EndElement) event).getName().getLocalPart().equals(recordIdentifier)) {
                    assert eventWriter != null;
                    eventWriter.add(event);
                    String fieldList = sw.toString();
                    sw.close();

                    return fieldList;

                } else if (eventWriter != null) {
                    eventWriter.add(event);
                }
                if (eventWriter != null) eventWriter.close();
            }

        } catch (XMLStreamException |
                IOException e)

        {
            e.printStackTrace();
        }
        return "";
    }


    private static void createFieldList() {
        HttpGet getRequest = new HttpGet("HTTP://api.fm-web.co.uk/webservices/AssetDataWebSvc/DriverProcessesWS.asmx?WSDL");
        HttpResponse httpResponse = HTTPClient.getResponse(getRequest);
        try {
            parseRecordDefinition(httpResponse.getEntity().getContent(), recordID);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Setter for response input stream
     * @param response  Response input stream
     */
    public static void setResponse(InputStream response) {
        responseList = false;
        ResponseToOutputFormat.response = response;
    }

    /**
     * Setter for response input list of strings
     * @param response  Response List of strings
     */
    public static void setResponse(List<String> response) {
        responseList = true;
        ResponseToOutputFormat.responseOfStrings = response;
    }

    /**
     * Setter for method that is used as XML record start level
     * @param method Method level to determine XML record level split
     */
    public static void setMethod(String method) {
        ResponseToOutputFormat.method = method;
    }

    /**
     * Setter to determine the record level for CSV
     * @param recordID Record level in XML
     */
    public static void setRecordID(String recordID) {
        ResponseToOutputFormat.recordID = recordID;
        createFieldList();
    }

}
