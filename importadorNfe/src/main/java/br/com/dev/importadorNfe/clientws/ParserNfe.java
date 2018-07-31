package br.com.dev.importadorNfe.clientws;

import java.io.InputStream;
import java.io.Reader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.CannotResolveClassException;

public class ParserNfe {

  public ResNFe parseXmlToNfe(Reader arquivoNfe) throws Exception {
    XStream xstream = new XStream();
    ResNFe eResNFe = null;
    try {

      eResNFe = (ResNFe) xstream.fromXML(arquivoNfe);
    } catch (Exception e) {
      e.printStackTrace();
      if (e instanceof CannotResolveClassException) {
        throw new Exception("Arquivo n�o identificado como nfe.");
      }
    }

    return eResNFe;
  }

  public ResNFe parseXmlToNfe(InputStream inputStream) throws Exception {

    ResNFe tResNFe = null;
    try {

      JAXBContext jaxbContext = JAXBContext.newInstance(ResNFe.class);

      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

      tResNFe = jaxbUnmarshaller.unmarshal(new StreamSource(inputStream), ResNFe.class).getValue();
    } catch (Exception e) {
      e.printStackTrace();
      if (e instanceof CannotResolveClassException) {
        throw new Exception("Arquivo n�o identificado como nfe.");
      }
    }

    return tResNFe;
  }
}
