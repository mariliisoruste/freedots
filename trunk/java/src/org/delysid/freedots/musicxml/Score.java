/* -*- c-basic-offset: 2; -*- */
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
 * This software is maintained by Mario Lang <mlang@delysid.org>.
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

public final class Score {
  private Document document;

  private static XPathFactory xPathFactory = XPathFactory.newInstance();

  private List<Part> parts;

  public Score(
    InputStream inputStream, String extension
  ) throws ParserConfigurationException,
           IOException, SAXException, XPathExpressionException {

    parse(inputStream, extension);
  }
  public Score(
    String filename
  ) throws ParserConfigurationException,
	   IOException, SAXException, XPathExpressionException {
    File file = new File(filename);
    InputStream inputStream = null;
    String extension = null;

    int dot = filename.lastIndexOf('.');
    if (dot != -1) extension = filename.substring(dot + 1);

    if (file.exists()) inputStream = new FileInputStream(file);
    else {
      URL url = new URL(filename);
      inputStream = url.openConnection().getInputStream();
    }
    parse(inputStream, extension);
  }
  private void parse(InputStream inputStream, String extension)
  throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
    DocumentBuilderFactory
    documentBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder
    documentBuilder = documentBuilderFactory.newDocumentBuilder();
    documentBuilder.setEntityResolver(new MusicXMLEntityResolver());

    if ("mxl".equalsIgnoreCase(extension)) {
      String zipEntryName = null;
      ZipInputStream zipInputStream = new ZipInputStream(inputStream);
      ZipEntry zipEntry = null;
      while ((zipEntry = zipInputStream.getNextEntry()) != null) {
	if ("META-INF/container.xml".equals(zipEntry.getName())) {
	  Document container =
	    documentBuilder.parse(
	      getInputSourceFromZipInputStream(zipInputStream));
	  XPath xpath = xPathFactory.newXPath();
	  zipEntryName = (String) xpath.evaluate("container/rootfiles/rootfile/@full-path",
						 container,
						 XPathConstants.STRING);
	} else if (zipEntry.getName().equals(zipEntryName))
    document = documentBuilder.parse(
		       getInputSourceFromZipInputStream(zipInputStream));
	zipInputStream.closeEntry();
      }
    } else
      document = documentBuilder.parse(inputStream);

    document.getDocumentElement().normalize();

    parts = new ArrayList<Part>();

    Element root = document.getDocumentElement();
    NodeList nodes = root.getElementsByTagName("part");
    Element partList = (Element) root.getElementsByTagName("part-list").item(0);
    NodeList partListKids = partList.getChildNodes();
    for (int i=0; i<nodes.getLength(); i++) {
      Element part = (Element) nodes.item(i);
      String idValue = part.getAttribute("id");
      Element scorePart = null;
      for (int j=0; j<partListKids.getLength(); j++) {
	Node kid = partListKids.item(j);
	if (kid.getNodeType() == Node.ELEMENT_NODE) {
	  Element elem = (Element) kid;
	  if (idValue.equals(elem.getAttribute("id"))) scorePart = elem;
	}
      }
      if (scorePart != null)
        parts.add(new Part(part, scorePart, this));
    }
  }

  private InputSource getInputSourceFromZipInputStream(
    ZipInputStream zipInputStream
  ) throws IOException {
    BufferedReader reader =
      new BufferedReader(new InputStreamReader(zipInputStream));
    StringBuilder stringBuilder = new StringBuilder();
    String string = null;
    while ((string = reader.readLine()) != null)
      stringBuilder.append(string + "\n");
    return new InputSource(new StringReader(stringBuilder.toString()));
  }

  public String getScoreType () {
    return document.getDocumentElement().getNodeName();
  }

  public int getDivisions() {
    XPath xPath = xPathFactory.newXPath();
    try {
      String xPathExpression = "//attributes/divisions/text()";
      NodeList nodeList = (NodeList) xPath.evaluate(xPathExpression,
						    document,
						    XPathConstants.NODESET);
      int count = nodeList.getLength();
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
}