package br.com.dev.importadorNfe.clientws;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;



public class UtilXML {

  private static String ALIAS = "saneamento de goias sa01616929000102";

  public static String ESOCIAL = "eSocial";
  public static String REINF = "Reinf";
  private static Transformer transformer = criarTransformer();

  /**
   * Converte o objeto que cont�m anota��o @XmlRootElement em um Element XML
   *
   * @param object objeto a ser convertido um XML Element deve conter a anota��o @XmlRootElement.
   * @return Element .
   */

  public static Element converterEventoParaXMLElement(Object object) {
    JAXBContext context = null;
    Element element = null;
    try {
      context = JAXBContext.newInstance(object.getClass());
      Marshaller marshaller = context.createMarshaller();

      Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      marshaller.marshal(object, document);
      element = document.getDocumentElement();

    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    } catch (PropertyException e) {
      e.printStackTrace();
    } catch (JAXBException e) {
      e.printStackTrace();
    }

    return element;
  }

  /**
   * Converte uma String com conte�do XML em um Element XML
   *
   * @return Element .
   */

  public static Element converterStringParaXMLElement(String xml) {
    Element element = null;
    try {

      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      Document documento = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
      element = documento.getDocumentElement();

    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    } catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return element;
  }

  /**
   * Converte o objeto em uma String com estrutura XML.
   *
   * @param object objeto a ser convertido em XML.
   * @return String contendo a estrutura XML.
   */
  public static String serializarXML(Object objeto) {
    final StringWriter out = new StringWriter();
    JAXBContext context = null;
    try {
      context = JAXBContext.newInstance(objeto.getClass());
      Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.toString());
      marshaller.marshal(objeto, new StreamResult(out));
    } catch (PropertyException e) {
      e.printStackTrace();
    } catch (JAXBException e) {
      e.printStackTrace();
    }
    return out.toString();
  }

  /**
   * Converte uma String com estrutura XML em Objeto.
   *
   * @param <E>
   * @param object objeto a ser convertido em XML.
   * @return String contendo a estrutura XML.
   */
  public static <T> T desserializarXML(String XML, Class<T> classe) {
    JAXBContext context = null;
    try {
      context = JAXBContext.newInstance(classe);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      return unmarshaller.unmarshal(new StreamSource(new StringReader(XML)), classe).getValue();
    } catch (PropertyException e) {
      e.printStackTrace();
    } catch (JAXBException e) {
      e.printStackTrace();
    }
    return null;
  }

//  public static String[] getNomeFuncaoXML() {
//    String[] retorno = new String[3];
//    TipoBanco tipoBanco = Banco.verificarAmbiente();
//    File arquivo;
//    if (tipoBanco == TipoBanco.PROD) {
//      arquivo = new File("/opt/tomcat/conf/cert/ChavePrivada.key");
//      retorno[0] = "/opt/tomcat/conf/cert/SANEAMENTODEGOIASSA.pfx";
//      retorno[2] = "/opt/tomcat/conf/cert/Cacert-22-04-2018";
//    } else {
//      arquivo = new File("\\\\prod2\\certificado\\ChavePrivada.key");
//      retorno[0] = "\\\\prod2\\certificado\\SANEAMENTODEGOIASSA.pfx";
//      retorno[2] = "\\\\prod2\\certificado\\Cacert-22-04-2018";
//    }
//    try {
//      BufferedReader bf = new BufferedReader(new FileReader(arquivo));
//      retorno[1] = bf.readLine();
//      bf.close();
//    } catch (FileNotFoundException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
//    // TODO Auto-generated method stub
//    catch (IOException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
//
//    return retorno;
//
//  }

  /**
   * Inicializa dados da certifica��o a ser usada nas conex�es SSL com servidores da Receita Federal
   */
//  public static void inicializarCertificacaoSSL() {
//
//    System.clearProperty("javax.net.ssl.keyStore");
//    System.clearProperty("javax.net.ssl.keyStorePassword");
//    System.clearProperty("javax.net.ssl.trustStore");
//    System.clearProperty("javax.net.ssl.trustStorePassword");
//
//    String[] chave = UtilXML.getNomeFuncaoXML();
//    System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");
//    System.setProperty("javax.net.ssl.keyStore", chave[0]);
//    System.setProperty("javax.net.ssl.keyStorePassword", chave[1]);
//
//    System.setProperty("javax.net.ssl.trustStoreType", "JKS");
//    System.setProperty("javax.net.ssl.trustStore", chave[2]);
//    // System.setProperty("javax.net.ssl.trustStorePassword", chave[1]);
//    // System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
//
//  }

  /**
   * Realiza a Assinatura de um documento XML
   */

//  public static String assinarSHA256(String xml, String tagSistema) {
//    String[] lChave = UtilXML.getNomeFuncaoXML();
//    String xmlAssinado = null;
//    try {
//      KeyStore ks = KeyStore.getInstance("PKCS12");
//      ks.load(new FileInputStream(lChave[0]), lChave[1].toCharArray());
//      KeyStore.PrivateKeyEntry keyEntry = (KeyStore.PrivateKeyEntry) ks.getEntry(ALIAS,
//          new KeyStore.PasswordProtection(lChave[1].toCharArray()));
//
//      X509Certificate certificado = (X509Certificate) keyEntry.getCertificate();
//
//      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//      factory.setNamespaceAware(true);
//      DocumentBuilder builder = factory.newDocumentBuilder();
//      Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
//      Node no = doc.getElementsByTagName(tagSistema).item(0);
//
//      // NodeList list = doc.getElementsByTagName(tagSistema);
//      Element el = (Element) no.getChildNodes().item(1);// list.item(1);
//      String referenceURI = "";
//      if (tagSistema.equals(UtilXML.ESOCIAL)) {
//        el.setIdAttribute("Id", true);
//        referenceURI = el.getAttribute("Id");
//      } else if (tagSistema.equals(REINF)) {
//        el.setIdAttribute("id", true);
//        referenceURI = el.getAttribute("id");
//      }
//      XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
//      List<Transform> transformList = new ArrayList<>();
//      transformList.add(fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null));
//      transformList
//          .add(fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null));
//
//      Reference ref = fac.newReference("#" + referenceURI, fac.newDigestMethod(DigestMethod.SHA256, null),
//          transformList, null, null);
//
//      SignedInfo si = fac.newSignedInfo(
//          fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
//          fac.newSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", null),
//          Collections.singletonList(ref));
//
//      KeyInfoFactory kif = fac.getKeyInfoFactory();
//      X509Data x509Data = kif.newX509Data(Collections.singletonList(certificado));
//      KeyInfo ki = kif.newKeyInfo(Collections.singletonList(x509Data));
//
//      DOMSignContext dsc = new DOMSignContext(keyEntry.getPrivateKey(), el.getParentNode());
//
//      XMLSignature signature = fac.newXMLSignature(si, ki);
//      signature.sign(dsc);
//
//      StreamResult streamResult = new StreamResult(new StringWriter());
//      TransformerFactory tf = TransformerFactory.newInstance();
//      Transformer trans = tf.newTransformer();
//      trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
//      trans.setOutputProperty(OutputKeys.INDENT, "no");
//      trans.transform(new DOMSource(doc), streamResult);
//
//      xmlAssinado = streamResult.getWriter().toString();
//    } catch (KeyStoreException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    } catch (NoSuchAlgorithmException | CertificateException | IOException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    } catch (UnrecoverableEntryException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    } catch (ParserConfigurationException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    } catch (SAXException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    } catch (TransformerException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    } catch (MarshalException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    } catch (XMLSignatureException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    } catch (InvalidAlgorithmParameterException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
//
//    return xmlAssinado;
///
//  }

  /**
   * Realiza a Assinatura de uma classe dos sistemas REINF e ESocial
   */

//  public static <T> T assinarClasse(Class<T> classe, T objeto, String tagSistema) {
//    String xml = UtilXML.serializarXML(objeto);
//    // System.out.println(xml);
//    xml = UtilXML.removerTagXMLVersion(xml);
//    xml = UtilXML.removerTagNS2(xml);
//    xml = UtilXML.assinarSHA256(xml, tagSistema);
//    xml = UtilXML.removerTagNS2(xml);
//    // System.out.println(xml);
//    return desserializarXML(xml, classe);
//  }

  public static String removerTagXMLVersion(String xml) {
    String retorno = xml.replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>", "");
    return retorno;
  }

  public static String removerTagNS2(String xml) {
    String retorno = xml.replace("xmlns:ns2=\"http://www.w3.org/2000/09/xmldsig#\"", "");
    // String retorno = xml.replace(":ns2", "");
    retorno = retorno.replaceAll("ns2:", "");
    return retorno;
  }

  private static Transformer criarTransformer() {
    try {
      return TransformerFactory.newInstance().newTransformer();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Converte um MessageElement para a classe desejada. Este m�todo foi criado para facilitar tratamento de retorno nos
   * servi�os REINF/ESocial
   */
  public static <T> T converterMessageElementParaClasse(Class<T> classe, Node... mensagem) throws TransformerException {
    T retorno = desserializarXML(converterMessageElementParaString(mensagem), classe);
    return retorno;
  }

  /**
   * Converte um MessageElement para uma String. Este m�todo foi criado para facilitar tratamento de retorno nos
   * servi�os REINF/ESocial
   */
  public static String converterMessageElementParaString(Node... mensagem) throws TransformerException {
    StringWriter sw = new StringWriter();
    StreamResult sResult = new StreamResult(sw);
    for (Node item : mensagem) {
      transformer.transform(new DOMSource(item), sResult);
    }
    return sw.toString();

  }

  public static boolean validarXMLContraXSD(String xml, String xsd) {

    try {
      SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      Schema schema = factory
          .newSchema(new StreamSource(new ByteArrayInputStream(xsd.getBytes(StandardCharsets.UTF_8))));
      Validator validator = schema.newValidator();
      validator.validate(new StreamSource(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));
      return true;
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return false;
  }

  public static boolean validarXMLContraXSDCaminho(String xml, String caminhoXSD) {

    try {
      StringBuilder sbXSD = new StringBuilder();

      List<String> strings = Files.readAllLines(Paths.get(caminhoXSD), StandardCharsets.UTF_8);
      for (String linha : strings) {
        sbXSD.append(linha);
      }
      SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      Schema schema = factory
          .newSchema(new StreamSource(new ByteArrayInputStream(sbXSD.toString().getBytes(StandardCharsets.UTF_8))));
      Validator validator = schema.newValidator();
      validator.validate(new StreamSource(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));
      return true;
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return false;
  }

}
