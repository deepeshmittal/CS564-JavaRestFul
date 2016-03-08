package main;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Random;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;



@Path("/serverclass")
public class ServerClass {
	
	static String NS = "http://cse564.asu.edu/PoxAssignment";
	static HashSet<String> itemID = new HashSet<String>();
	
	@POST
	@Path("/postrequesthandler")
	public Response postRequestHandler(String cont) throws SAXException, IOException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException {
		
		DocumentBuilderFactory icFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder icBuilder = icFactory.newDocumentBuilder();
		Document xmlres = icBuilder.newDocument();

		Document input_xml = icBuilder.parse(new InputSource(new StringReader(cont)));
		input_xml.getDocumentElement().normalize();
		
		if (input_xml.getDocumentElement().getNodeName().equalsIgnoreCase("NewFoodItems")){
			xmlres = modifyXMLHandler(input_xml);
		}else if(input_xml.getDocumentElement().getNodeName().equalsIgnoreCase("SelectedFoodItems")){
			xmlres = queryXMLHandler(input_xml);
		}
		
		
		DOMSource source = new DOMSource(xmlres);
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
        StringWriter writer = new StringWriter();
        source = new DOMSource(xmlres);
        transformer.transform(source, new StreamResult(writer));
        String output = writer.getBuffer().toString();
		
		return Response.status(200).entity(output).build();
	}
	
	public static Document queryXMLHandler(Document input_xml) throws ParserConfigurationException, SAXException, IOException, TransformerException{
		
		DocumentBuilderFactory icFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder icBuilder = icFactory.newDocumentBuilder();
		Document xmlres = icBuilder.newDocument();

		NodeList itemList = input_xml.getElementsByTagName("FoodItemId");
		
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		File file = new File(classLoader.getResource("FoodItemData.xml").getPath());
		Document doc = icBuilder.parse(file);
		doc.getDocumentElement().normalize();
		NodeList nList = doc.getElementsByTagName("FoodItem");
		
		xmlres = icBuilder.newDocument();
		Element mainRootElement = xmlres.createElementNS(NS, "RetrievedFoodItems");
		xmlres.appendChild(mainRootElement);

		
		for (int i = 0; i < itemList.getLength(); i++) {
			String inputID = input_xml.getElementsByTagName("FoodItemId").item(i).getTextContent();
			if (!inputID.matches("[0-9]+")){
				xmlres = icBuilder.newDocument();
				Element RootElement = xmlres.createElementNS(NS, "InvalidMessage");
				xmlres.appendChild(RootElement);
				return xmlres;
			}
			
			int j = 0;
			for (j = 0; j < nList.getLength(); j++) {

				Node nNode = nList.item(j);					
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;
					String elementID = eElement.getElementsByTagName("id").item(0).getTextContent();
					
					
					if (elementID.equalsIgnoreCase(inputID)){							
						Node input_node = xmlres.importNode(doc.getElementsByTagName("FoodItem").item(j), true);
						xmlres.getDocumentElement().appendChild(input_node);
						break;
					}
				}
			}
			if (j == nList.getLength()){
				Element itemID = xmlres.createElement("InvalidFoodItem");
				
				Element ouputitemID = xmlres.createElement("FoodItemId");
				ouputitemID.appendChild(xmlres.createTextNode(inputID));
				itemID.appendChild(ouputitemID);
				mainRootElement.appendChild(itemID);
			}
		}
		
		DOMSource source = new DOMSource(xmlres);
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
        StringWriter writer = new StringWriter();
        source = new DOMSource(xmlres);
        transformer.transform(source, new StreamResult(writer));
        String output = writer.getBuffer().toString();

		System.out.println(output);
		return xmlres;
		
	}

	
	public static Document modifyXMLHandler(Document input_xml) throws ParserConfigurationException, SAXException, IOException, TransformerException{
		
		DocumentBuilderFactory icFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder icBuilder = icFactory.newDocumentBuilder();
		Document xmlres = icBuilder.newDocument();

		String input_name = input_xml.getElementsByTagName("name").item(0).getTextContent();
		String input_category = input_xml.getElementsByTagName("category").item(0).getTextContent();
		String input_desc = input_xml.getElementsByTagName("description").item(0).getTextContent();
		String input_price = input_xml.getElementsByTagName("price").item(0).getTextContent();
		String input_country = input_xml.getElementsByTagName("FoodItem").item(0).getAttributes().getNamedItem("country").getNodeValue();
		
		if (input_name.isEmpty() || input_category.isEmpty() || input_desc.isEmpty() || input_price.isEmpty() || input_country.isEmpty()){
			
			Element mainRootElement = xmlres.createElementNS(NS, "InvalidMessage");
			xmlres.appendChild(mainRootElement);
			return xmlres;
		}
		
		try  
		  {  
		    @SuppressWarnings("unused")
			double d = Double.parseDouble(input_price);  
		  }  
		  catch(NumberFormatException nfe)  
		  {  
			  	Element mainRootElement = xmlres.createElementNS(NS, "InvalidMessage");
				xmlres.appendChild(mainRootElement);
				return xmlres;  
		  }  
		
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		File file = new File(classLoader.getResource("FoodItemData.xml").getPath());
		Document doc = icBuilder.parse(file);
		doc.getDocumentElement().normalize();
		
		NodeList nList = doc.getElementsByTagName("FoodItem");
		
		for (int temp = 0; temp < nList.getLength(); temp++) {

			Node nNode = nList.item(temp);					
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) nNode;
				
				String elementname = eElement.getElementsByTagName("name").item(0).getTextContent();
				String elementcat = eElement.getElementsByTagName("category").item(0).getTextContent();
				
				if (elementname.equalsIgnoreCase(input_name) && elementcat.equalsIgnoreCase(input_category)){
					String elementID = eElement.getElementsByTagName("id").item(0).getTextContent();
					
					Element mainRootElement = xmlres.createElementNS(NS, "FoodItemExists");
					xmlres.appendChild(mainRootElement);
					Element itemID = xmlres.createElement("FoodItemId");
					itemID.appendChild(xmlres.createTextNode(elementID));
					mainRootElement.appendChild(itemID);
					return xmlres;
				}
			}
		}
		
		for (int temp = 0; temp < nList.getLength(); temp++) {

			Node nNode = nList.item(temp);					
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) nNode;
				
				String elementID = eElement.getElementsByTagName("id").item(0).getTextContent();
				itemID.add(elementID);
			}	
		}

		Integer randomItemId = null;
		boolean found = false;
		while(!found){
			Random r = new Random();
			randomItemId = r.nextInt((999 - 100) + 1) + 100;
			if(!itemID.contains(randomItemId.toString())){
				found = true;
			}
		}
		
		Element input_element = (Element) input_xml.getElementsByTagName("FoodItem").item(0);
		Element newitemID = input_xml.createElement("id");
		newitemID.appendChild(input_xml.createTextNode(randomItemId.toString()));
		input_element.appendChild(newitemID);
		
		Node input_node = doc.importNode(input_xml.getElementsByTagName("FoodItem").item(0), true);
		doc.getDocumentElement().appendChild(input_node);
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(file);
		transformer.transform(source, result);
		
		Element mainRootElement = xmlres.createElementNS(NS, "FoodItemAdded");
		xmlres.appendChild(mainRootElement);
		Element itemID = xmlres.createElement("FoodItemId");
		itemID.appendChild(xmlres.createTextNode(randomItemId.toString()));
		mainRootElement.appendChild(itemID);
		return xmlres;

	}

}