package com.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/xml")
public class XmlController {

    @PostMapping(value = "/parse", consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE, MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<?> parseXml(@RequestBody String xmlBody) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlBody)));
            doc.getDocumentElement().normalize();

            Map<String, String> result = new HashMap<>();
            NodeList nameNodes = doc.getElementsByTagName("name");
            if (nameNodes.getLength() > 0) {
                result.put("name", nameNodes.item(0).getTextContent());
            }
            NodeList urlNodes = doc.getElementsByTagName("url");
            if (urlNodes.getLength() > 0) {
                result.put("url", urlNodes.item(0).getTextContent());
            }
            result.put("rootElement", doc.getDocumentElement().getNodeName());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("XML parse error: " + e.getMessage());
        }
    }

    @PostMapping(value = "/import-user", consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE, MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<?> importUser(@RequestBody String xmlBody) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlBody)));

            Map<String, String> user = new HashMap<>();
            for (String field : new String[]{"username", "email", "role"}) {
                NodeList nodes = doc.getElementsByTagName(field);
                if (nodes.getLength() > 0) {
                    user.put(field, nodes.item(0).getTextContent());
                }
            }
            return ResponseEntity.ok(Map.of("imported", user));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Import error: " + e.getMessage());
        }
    }
}
