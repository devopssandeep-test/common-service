package srdm.cloud.commonService.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;

/**
 * O/X Mapper Processor
 * AppconfigでBean定義
 *
 */
public class OxmProcessor {

	private static final Logger logger = LoggerFactory.getLogger(OxmProcessor.class);

	private final String XML_DECLARETION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

	private Marshaller marshaller;
	private Unmarshaller unmarshaller;

	public void setMarshaller(Marshaller marshaller) {
		this.marshaller = marshaller;
	}
	public void setUnmarshaller(Unmarshaller unmarshaller) {
		this.unmarshaller = unmarshaller;
	}

	// Converts Object to XML
	public String objectToXML(Object object) {

		String strXml;
		try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			marshaller.marshal(object, new StreamResult(baos));
			strXml = baos.toString("UTF-8");
		} catch (XmlMappingException | IOException e) {
			logger.error("[OXMapper]objectToXML error!!", e);
			strXml = "";
		}
		return strXml;
	}

	// Converts XML to Object
	public Object xmlToObject(String sourceXml) {

		try {
			String strXml = XML_DECLARETION + sourceXml;
			logger.debug("\nSource XML:" + strXml);
			return unmarshaller.unmarshal(new StreamSource(new ByteArrayInputStream(strXml.getBytes("UTF-8"))));
		} catch (XmlMappingException | IOException e) {
			logger.error("[OXMapper]xmlToObject error!!", e);
			return null;
		}
	}
}
