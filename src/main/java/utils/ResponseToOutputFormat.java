package utils;

import org.apache.commons.lang3.math.NumberUtils;
import org.w3c.dom.NodeList;

import javax.xml.soap.Node;
import javax.xml.stream.*;
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
    private static String method = null;
    private static String recordID = null;

    /**
     * Start parsing the response. All parameters should have been provided by the setter methods or retrieved
     * from the TreadArguments class
     */
    public static void parse() {
        switch (ta.getOutputFormat()) {
            case CSV:
            case KAFKA:
                parseToCSV(response, recordID, ta.getHeader(), ta.getOutputFormat());
                break;
            case XML:
                parseToXML(response, method);
                break;
            default:
                System.err.println("Unhandled output method received!");
        }
    }

    /**
     * Parse the response, creating records on the event method level
     * @param is response passed as xml format
     * @param eventMethod record delimiter for determining record level
     */
    private static void parseToXML(InputStream is, String eventMethod) {
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLOutputFactory oFactory = XMLOutputFactory.newInstance();
            XMLEventReader eventReader = factory.createXMLEventReader(is);
            XMLEventWriter eventWriter = null;
            StringWriter sw = new StringWriter();

            while(eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                if (event.isStartElement() &&
                        ((StartElement) event).getName().getLocalPart().equals(eventMethod + "Result") ) {
                    eventWriter = oFactory.createXMLEventWriter(sw);
                } else if (event.isEndElement() &&
                        ((EndElement) event).getName().getLocalPart().equals(eventMethod+ "Result") ) {
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


    private static void parseToCSV(InputStream is, String recordIdentifier, boolean withHeader, OutputFormatEnum format) {
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLOutputFactory oFactory = XMLOutputFactory.newInstance();
            XMLEventReader eventReader = factory.createXMLEventReader(is);
            XMLEventWriter eventWriter = null;
            StringWriter sw = null;
            Boolean processHeader = withHeader;

            while(eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                if (event.isStartElement() &&
                        ((StartElement) event).getName().getLocalPart().equals(recordIdentifier) ) {
                    sw = new StringWriter();
                    eventWriter = oFactory.createXMLEventWriter(sw);
                    if(eventWriter != null) eventWriter.add(event);
                } else if (event.isEndElement() &&
                        ((EndElement) event).getName().getLocalPart().equals(recordIdentifier) ) {
                    assert eventWriter != null;
                    eventWriter.add(event);
                    if (processHeader) {
                        processXMLRecord(sw.toString(), true, format);
                        processHeader = false;
                    }
                    processXMLRecord(sw.toString(), processHeader, format);
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

    public static String getLastID() {return lastID;}

    private static void processXMLRecord(String source, boolean processHeader, OutputFormatEnum format) {
        try {
            DOMResult result = new DOMResult();
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer t = tFactory.newTransformer();
            t.transform(new StreamSource(new StringReader(source)), result);
            String record;
            record = processNodeList(result.getNode().getChildNodes(), true,"", processHeader);
            lastID = record.substring(0, record.indexOf(";"));
            if (format == KAFKA) {
                kp.sendMessage(ta.getKafkaTopic(), lastID, record);
            } else {
                System.out.println(record);
            }
            if (NumberUtils.isNumber(lastID)) ta.setLastEventID(Integer.parseInt(lastID));
        } catch ( TransformerException e) {
            e.printStackTrace();
        }
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

    /**
     * Setter for response input stream
     * @param response  Response input stream
     */
    public static void setResponse(InputStream response) {
        ResponseToOutputFormat.response = response;
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
    }

}
