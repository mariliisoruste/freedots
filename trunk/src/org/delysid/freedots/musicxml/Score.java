/* -*- c-basic-offset: 2; indent-tabs-mode: nil; -*- */
/*
 * FreeDots -- MusicXML to braille music transcription
 *
 * Copyright 2008-2009 Mario Lang  All Rights Reserved.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details (a copy is included in the LICENSE.txt file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License
 * along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This file is maintained by Mario Lang <mlang@delysid.org>.
 */
package org.delysid.freedots.musicxml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A MusicXML document in score-partwise format.
 */
public final class Score {
  private Document document;

  private static XPathFactory xPathFactory = XPathFactory.newInstance();
  private static DocumentBuilder documentBuilder;
  static {
    DocumentBuilderFactory
    documentBuilderFactory = DocumentBuilderFactory.newInstance();
    try {
      documentBuilder = documentBuilderFactory.newDocumentBuilder();
      documentBuilder.setEntityResolver(new MusicXMLEntityResolver());
    } catch (ParserConfigurationException e) { e.printStackTrace(); }
  }

  /* --- Header fields --- */
  private Text workNumber, workTitle;
  private Text movementNumber, movementTitle;
  private Text composer, poet;
  private Text rights;

  private List<Part> parts;

  public Score(
    final InputStream inputStream, final String extension
  ) throws ParserConfigurationException,
           IOException, SAXException, XPathExpressionException {
    parse(inputStream, extension);
  }

  /**
   * Construct a score object from a URL.
   */
  public Score (
    final String filenameOrURL
  ) throws ParserConfigurationException,
           IOException, SAXException, XPathExpressionException
  {
    File file = new File(filenameOrURL);
    InputStream inputStream = null;
    String extension = null;

    int dot = filenameOrURL.lastIndexOf('.');
    if (dot != -1) extension = filenameOrURL.substring(dot + 1);

    if (file.exists()) { /* Local file */
      inputStream = new FileInputStream(file);
    } else {
      URL url = new URL(filenameOrURL);
      inputStream = url.openConnection().getInputStream();
    }

    parse(inputStream, extension);
  }

  private void parse(
    InputStream inputStream, String extension
  ) throws ParserConfigurationException, IOException, SAXException,
           XPathExpressionException {
    if ("mxl".equalsIgnoreCase(extension)) {
      String zipEntryName = null;

      ZipInputStream zipInputStream = new ZipInputStream(inputStream);
      ZipEntry zipEntry = null;

      while ((zipEntry = zipInputStream.getNextEntry()) != null) {
        if ("META-INF/container.xml".equals(zipEntry.getName())) {
          Document
          container = documentBuilder.parse(getInputSourceFromZipInputStream(zipInputStream));
          XPath xpath = xPathFactory.newXPath();
          zipEntryName = (String) xpath.evaluate("container/rootfiles/rootfile/@full-path",
                                                 container,
                                                 XPathConstants.STRING);
        } else if (zipEntry.getName().equals(zipEntryName))
          document = documentBuilder.parse(getInputSourceFromZipInputStream(zipInputStream));
        zipInputStream.closeEntry();
      }
    } else {
      document = documentBuilder.parse(inputStream);
    }

    document.getDocumentElement().normalize();

    Element root = document.getDocumentElement();

    /* Parse score-header */
    Element partList = null;

    movementNumber = getTextNode(root, "movement-number");
    movementTitle = getTextNode(root, "movement-title");

    for (Element scoreElement:getChildElements(root)) {
      if (scoreElement.getNodeName().equals("work")) {
        workNumber = getTextNode(scoreElement, "work-number");
        workTitle = getTextNode(scoreElement, "work-title");
      } else if (scoreElement.getNodeName().equals("identification")) {
        for (Element identificationElement:getChildElements(scoreElement)) {
          if (identificationElement.getNodeName().equals("creator")) {
            Element creator = identificationElement;
            NodeList creatorNodes = creator.getChildNodes();
            Text textNode = null;
            for (int i = 0; i < creatorNodes.getLength(); i++) {
              Node creatorNode = creatorNodes.item(i);
              if (creatorNode.getNodeType() == Node.TEXT_NODE) {
                textNode = (Text)creatorNode;
              }
            }
            if (creator.getAttribute("type").equals("composer")) {
              composer = textNode;
            } else if (creator.getAttribute("type").equals("poet")) {
              poet = textNode;
            }
          }
        }
      } else if (scoreElement.getNodeName().equals("part-list"))
        partList = scoreElement;
    }


    /* Parse (partwise) part elements */
    parts = new ArrayList<Part>();

    NodeList nodes = root.getElementsByTagName("part");
    List<Element> partListElements = getChildElements(partList);
    for (int i = 0; i < nodes.getLength(); i++) {
      Element part = (Element) nodes.item(i);
      String idValue = part.getAttribute("id");
      Element scorePart = null;
      for (Element partListElement : partListElements) {
        if (partListElement.getNodeName().equals("score-part") &&
            idValue.equals(partListElement.getAttribute("id")))
          scorePart = partListElement;
      }
      if (scorePart != null)
        parts.add(new Part(part, scorePart, this));
      else
        throw new RuntimeException("Unable to find <score-part> for part "
                                   + idValue);
    }
  }

  public String getWorkNumber() {
    return workNumber != null ? workNumber.getWholeText() : null;
  }
  public String getWorkTitle() {
    return workTitle != null ? workTitle.getWholeText() : null;
  }
  public String getMovementNumber() {
    return movementNumber != null ? movementNumber.getWholeText() : null;
  }
  public String getMovementTitle() {
    return movementTitle != null ? movementTitle.getWholeText() : null;
  }
  public String getComposer() {
    return composer != null ? composer.getWholeText() : null;
  }
  public String getPoet() {
    return poet != null ? poet.getWholeText() : null;
  }

  private InputSource getInputSourceFromZipInputStream(
    ZipInputStream zipInputStream
  ) throws IOException {
    BufferedReader
    reader = new BufferedReader(new InputStreamReader(zipInputStream));
    StringBuilder stringBuilder = new StringBuilder();
    String string = null;
    while ((string = reader.readLine()) != null)
      stringBuilder.append(string + "\n");
    return new InputSource(new StringReader(stringBuilder.toString()));
  }

  public String getScoreType() {
    return document.getDocumentElement().getNodeName();
  }

  /**
   * Calculate the least common multiple of all divisions elements in the score.
   */
  public int getDivisions() {
    XPath xPath = xPathFactory.newXPath();
    try {
      String xPathExpression = "//attributes/divisions/text()";
      NodeList nodeList = (NodeList) xPath.evaluate(xPathExpression,
                                                    document,
                                                    XPathConstants.NODESET);
      final int count = nodeList.getLength();
      BigInteger result = BigInteger.ONE;
      for (int index = 0; index < count; index++) {
        Node node = nodeList.item(index);
        BigInteger divisions = new BigInteger(new Integer(Math.round(Float.parseFloat(node.getNodeValue()))).toString());
        result = result.multiply(divisions).divide(result.gcd(divisions));
      }
      return result.intValue();
    } catch (XPathExpressionException e) {
      return 0;
    }
  }

  public List<Part> getParts() {
    return parts;
  }

  /* --- W3C DOM convenience access utilities --- */

  static Text getTextNode(Element element, String childTagName) {
    NodeList nodeList = element.getElementsByTagName(childTagName);
    if (nodeList.getLength() >= 1) {
      nodeList = nodeList.item(nodeList.getLength()-1).getChildNodes();
      for (int index = 0; index < nodeList.getLength(); index++) {
        Node node = nodeList.item(index);
        if (node.getNodeType() == Node.TEXT_NODE) return (Text)node;
      }
    }
    return null;
  }
  static List<Element> getChildElements(Element root) {
    final NodeList children = root.getChildNodes();
    final int childCount = children.getLength();
    List<Element> elements = new ArrayList<Element>(childCount);
    for (int i = 0; i < childCount; i++) {
      Node node = children.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE)
        elements.add((Element)node);
    }
    return elements;
  }
}
