package br.com.dev.importadorNfe.clientws;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import br.com.samuelweb.certificado.Certificado;
import br.com.samuelweb.certificado.CertificadoService;
import br.com.samuelweb.certificado.exception.CertificadoException;
import br.com.samuelweb.nfe.Nfe;
import br.com.samuelweb.nfe.dom.ConfiguracoesIniciaisNfe;
import br.com.samuelweb.nfe.dom.Enum.StatusEnum;
import br.com.samuelweb.nfe.dom.Enum.TipoManifestacao;
import br.com.samuelweb.nfe.exception.NfeException;
import br.com.samuelweb.nfe.util.ConstantesUtil;
import br.com.samuelweb.nfe.util.Estados;
import br.com.samuelweb.nfe.util.XmlUtil;
import br.com.saneago.hnf.util.ParserNfe;
import br.com.saneago.osl.atributo.AEntradaNotaFiscalEletronica;
import br.com.saneago.osl.entidade.EEntradaNotaFiscalEletronica;
import br.com.saneago.osl.negocio.NEntradaNotaFiscalEletronica;
import br.com.saneago.osl.nfe.ResNFe;
// import br.com.saneago.io.Banco;
// import br.com.saneago.osl.entidade.EEntradaNotaFiscalEletronica;
// import br.com.saneago.osl.negocio.NEntradaNotaFiscalEletronica;
import br.com.saneago.util.Excecao;
import br.com.saneago.util.TiposOperacaoCampoConsulta;
import br.com.saneago.util.UtilXML;
import br.inf.portalfiscal.nfe.schema.envConfRecebto.TRetEnvEvento;
import br.inf.portalfiscal.nfe.schema.retdistdfeint.RetDistDFeInt;
import br.inf.portalfiscal.nfe.schema.retdistdfeint.RetDistDFeInt.LoteDistDFeInt.DocZip;

/**
 * @author Alex Pereira Maranhão
 */
public class DownloadNfe {

  public static void main(String[] args) throws Excecao, InterruptedException {
    try {
      String[] nomeFuncao = UtilXML.getNomeFuncaoXML();
      Certificado certificado = CertificadoService.certificadoPfx(nomeFuncao[0], nomeFuncao[1]);

      ConfiguracoesIniciaisNfe conf = ConfiguracoesIniciaisNfe.iniciaConfiguracoes(Estados.GO,
          ConstantesUtil.AMBIENTE.PRODUCAO, certificado, "");
      // conf.setContigenciaSCAN(true);

      NEntradaNotaFiscalEletronica oNEntradaNotaFiscalEletronica = new NEntradaNotaFiscalEletronica("OSL");
      EEntradaNotaFiscalEletronica oEEntradaNotaFiscalEletronica = new EEntradaNotaFiscalEletronica();
      oEEntradaNotaFiscalEletronica.adicionarRetorno(AEntradaNotaFiscalEletronica.NumeroSerialSefaz,
          TiposOperacaoCampoConsulta.Maximo);
      oEEntradaNotaFiscalEletronica = oNEntradaNotaFiscalEletronica.consultar(oEEntradaNotaFiscalEletronica);

      RetDistDFeInt retorno = consultaNsu(oEEntradaNotaFiscalEletronica.getNumeroSerialSefaz() != null
          ? oEEntradaNotaFiscalEletronica.getNumeroSerialSefaz() : 0);

      if (StatusEnum.DOC_LOCALIZADO_PARA_DESTINATARIO.getCodigo().equals(retorno.getCStat())) {

        List<DocZip> listaDoc = retorno.getLoteDistDFeInt().getDocZip();

        for (DocZip docZip : listaDoc) {
          oEEntradaNotaFiscalEletronica = new EEntradaNotaFiscalEletronica();
          oEEntradaNotaFiscalEletronica.setNumeroSerialSefaz(Integer.valueOf(docZip.getNSU()));// tipo nfe
          oEEntradaNotaFiscalEletronica.setXmlResumoNfe(XmlUtil.gZipToXml(docZip.getValue()));
          // System.out.println("Resumo da NFe: " + XmlUtil.gZipToXml(docZip.getValue()));
          manifestarBaixarXmlCompleto(docZip, oEEntradaNotaFiscalEletronica);
          oNEntradaNotaFiscalEletronica.incluir(oEEntradaNotaFiscalEletronica);
        }
      }
    } catch (NfeException | IOException | CertificadoException e) {
      System.out.println("Erro:" + e.getMessage());
    }
  }

  public static void manifestarBaixarXmlCompleto(DocZip xmlResumo,
      EEntradaNotaFiscalEletronica oEEntradaNotaFiscalEletronica)
      throws NfeException, IOException, Excecao, InterruptedException {
    ParserNfe oParserNfe = new ParserNfe();
    ResNFe oResNFe = new ResNFe();

    InputStream inputStream = new ByteArrayInputStream(
        XmlUtil.gZipToXml(xmlResumo.getValue()).getBytes(StandardCharsets.UTF_8));

    oResNFe = oParserNfe.parseXmlToNfe(inputStream);
    TRetEnvEvento oTRetEnvEvento = Nfe.manifestacao(oResNFe.getChNFe(), TipoManifestacao.CIENCIA_DA_OPERACAO,
        "01616929000102", null, new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "T00:00:15-03:00");
    // System.out.println("Status da manifesta��o: " + oTRetEnvEvento.getCStat() + " / Motivo: "
    // + oTRetEnvEvento.getXMotivo());
    Thread.sleep(10000);

    RetDistDFeInt nfe = Nfe.distribuicaoDfe(ConstantesUtil.TIPOS.CNPJ, "01616929000102", ConstantesUtil.TIPOS.CHAVE,
        oResNFe.getChNFe());
    // System.out.println("Status do download da NFe: " + nfe.getCStat() + " / Motivo: " + nfe.getXMotivo());
    Thread.sleep(10000);

    DocZip docZip = new DocZip();
    if (nfe.getLoteDistDFeInt() != null) {
      List<DocZip> listaDoc = nfe.getLoteDistDFeInt().getDocZip();
      docZip = listaDoc.get(0);
      oEEntradaNotaFiscalEletronica.setXmlDanfe(XmlUtil.gZipToXml(docZip.getValue()));
    }

  }

  public static RetDistDFeInt consultaNsu(Integer nsu) throws NfeException {
    String cnpj = "01616929000102";
    DecimalFormat oDecimalFormat15 = new DecimalFormat("000000000000000");
    return Nfe.distribuicaoDfe(ConstantesUtil.TIPOS.CNPJ, cnpj, ConstantesUtil.TIPOS.NSU, oDecimalFormat15.format(nsu));
  }
}