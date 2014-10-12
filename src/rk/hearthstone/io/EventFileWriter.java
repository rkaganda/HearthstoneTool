package rk.hearthstone.io;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class EventFileWriter {
	
	private final static String version = "1";
	private final static String saveDir = "saved";
	
	public static String writeEventFile(List<Map<String,String>> events, String fileName ) {
		String fullName = fileName+".xml";
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			Document doc = docBuilder.newDocument();
			Element eventListElement = doc.createElement("eventlist");
			Attr attr = doc.createAttribute("version");
			attr.setValue(version);
			eventListElement.setAttributeNode(attr);
			doc.appendChild(eventListElement);
			
			for(Map<String,String> event:events) { //for each event
				Element eventElement = doc.createElement("event"); //create element
				for(String key:event.keySet()) { //for each event parameter
					Attr attrKey = doc.createAttribute(key);	//create attribute
					attrKey.setValue(event.get(key));
					eventElement.setAttributeNode(attrKey);
				}
				eventListElement.appendChild(eventElement); // add event to list
			}

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			
			//createSaveDirectory();
			File file = new File(fileName+".xml");
			file.createNewFile();
			StreamResult result = new StreamResult(file);
			//createSaveDirectory();
			transformer.transform(source, result);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return fullName;
	}
	
	private static void createSaveDirectory() {
		File dir = new File(saveDir);
		
		
		try{
			dir.mkdir();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
