package telematics.rest;

import org.w3c.dom.NodeList;

import javax.xml.soap.Node;
import javax.xml.stream.*;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Created by depoorterp on 3/01/2017.
 */
public class ProcessXMLResponse {;
    private static String delimiter = null;
    private static String lastID = null;

    public static void parse(InputStream is, String eventMethod) {
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

    public static void parseToCSV(InputStream is, String recordIdentifier) {
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLOutputFactory oFactory = XMLOutputFactory.newInstance();
            XMLEventReader eventReader = factory.createXMLEventReader(is);
            XMLEventWriter eventWriter = null;
            StringWriter sw = null;
            Boolean processHeader = true;

            while(eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                if (event.isStartElement() &&
                        ((StartElement) event).getName().getLocalPart().equals(recordIdentifier) ) {
                    sw = new StringWriter();
                    eventWriter = oFactory.createXMLEventWriter(sw);
                    eventWriter.add(event);
                } else if (event.isEndElement() &&
                        ((EndElement) event).getName().getLocalPart().equals(recordIdentifier) ) {
                    eventWriter.add(event);
                    if (processHeader) {
                        processXMLRecord(sw.toString(), processHeader);
                        processHeader = false;
                    }
                    processXMLRecord(sw.toString(), processHeader);
                    eventWriter = null;
                    sw.close();

                } else if (eventWriter != null) {
                    eventWriter.add(event);
                }
            }
            if (eventWriter != null) eventWriter.close();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getLastID() {return lastID;}

    private static void processXMLRecord(String source, boolean processHeader) {
        try {
            DOMResult result = new DOMResult();
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer t = tFactory.newTransformer();
            t.transform(new StreamSource(new StringReader(source)), result);
            String record = null;
            record = processNodeList(result.getNode().getChildNodes(), true,"", processHeader);
            System.out.println(record);
            lastID = record.substring(0,record.indexOf(";") );
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
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
}
