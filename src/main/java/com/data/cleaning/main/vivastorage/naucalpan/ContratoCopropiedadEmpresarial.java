package com.data.cleaning.main.vivastorage.naucalpan;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.data.cleaning.main.BaseParser;
import com.data.cleaning.main.Commons;

public class ContratoCopropiedadEmpresarial extends BaseParser {

	public String getTipoContrato() {
		return "Copropiedad empresarial";
	}

	public String getProyecto() {
		return "Vive Storage Naucalpan";
	}

	public String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/viva-storage-naucalpan/";
	}

	public static void main(String[] args) {
		ContratoCopropiedadEmpresarial parser = new ContratoCopropiedadEmpresarial();
		parser.process();
	}

	public String getFieldsTitle() {
//		return "Metraje|Inversion|Inversion Num|Vigencia|Rendimiento Bruto Min|Monto Equivalente|Monto Equivalente Num|Plazo|Mensualidad|Mensualidad Num|Carta Garantia|Derechos|Metros|Equity";
		return "M2_MINIBODEGAS|MONTO_INVERSION|VIGENCIA_DE_CONTRATO|TASA_DE_INTERES_ANUAL|NR_MENSUALIDADES|CUOTA_MENSUAL|CARTA_GARANTIA|DERECHOS_ADQUIRIENTE|EQUITY_INSTANTANEO";
	}

	@Override
	public String getAdquiriente(String content) {
		return Commons.extract(content, "C. ", ",").replaceAll("C. ", "");
	}

	@Override
	public String getEnajenante(String content) {
		return Commons.extract(content, "LA OTRA", ",").replaceAll("LA OTRA", "");
	}

	@Override
	public String getBeneficiario(String content) {
		return Commons.extract(content, "su beneficiario al C.", ",").replaceAll("su beneficiario al C.", "");
	}
	
	@Override
	public String getDireccionAdquirente(String content) {
		String domicilioAdquirente  = Commons.extract(content, "domicilio en:", " y ").replaceAll("domicilio en:", "");
		 if (domicilioAdquirente.endsWith(",")) 
			 domicilioAdquirente = domicilioAdquirente.substring(0, domicilioAdquirente.length() - 1);
		 
		 return domicilioAdquirente;
	}


	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {

		String m2                   = extractParteDecimal(Commons.extract(content, "sobre", ")", "PRIMERA."));
		String montoInversion       = Commons.extract(content, "un monto", ")", "PRIMERA.") + ")";

		String vigencia             = Commons.extract(content, "vigencia de", "contados", "El presente contrato tendrá");

		String rendimientoBrutoMin  = extractRendimientoMinBruto(content);
		if(rendimientoBrutoMin.length() > 0)
			rendimientoBrutoMin = Commons.numericValue(rendimientoBrutoMin) + "%";
		
//		String montoEquivalente     = Commons.extract(content, "cantidad de", ")", "anual equivalente") + ")";
		
		String nrMensualidades      = Commons.numericValue(Commons.extract(content, "los primeros", ",", "anual equivalente"));
		String mensualidad          = Commons.extract(content, "cantidad", ")", "con mes") + ")";

		String cartaGarantia        = Commons.extract(content, "Adicionalmente,", "anexa", "con mes") + ")";
		if(cartaGarantia.length() > 0)
   			 cartaGarantia = "SI";
		else cartaGarantia = "NO";

		String derechos             = Commons.extract(content, "El ", ",", "DERECHOS Y OBLIGACIONES");

//		String metros               = Commons.extract(content, "“COPROPIETARIO A”:", "cuadrados", "propiedad de la").replaceAll("“COPROPIETARIO A”:", "") + "cuadrados)";

		String equity               = Commons.extract(content, "equity", "del", "Al respecto");
		if(equity.length() > 0)
			equity = Commons.numericValue(equity) + "%";

		csvWriter.write("|");

		csvWriter.write(
				String.join("|",
						revisionManual, 

						Commons.toSingleLine(m2),
						
//						Commons.toSingleLine(montoInversion),
						Commons.toSingleLine(Commons.numericValue(montoInversion)),

						Commons.toSingleLine(vigencia),

						Commons.toSingleLine(rendimientoBrutoMin),
						
//						Commons.toSingleLine(montoEquivalente),
//						Commons.toSingleLine(Commons.numericValue(montoEquivalente)),
						
						Commons.toSingleLine(nrMensualidades),

//						Commons.toSingleLine(mensualidad),
						Commons.toSingleLine(Commons.numericValue(mensualidad)),

						Commons.toSingleLine(cartaGarantia),
						Commons.toSingleLine(derechos),
//						Commons.toSingleLine(metros),

						Commons.toSingleLine(equity)));

	}
	
	private String extractParteDecimal(String content) {
		String regex = "\\s+([0-9]+(?:\\.[0-9]+)?)";

		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(content);

		return matcher.find() ? matcher.group(1) : "";
	}


	private static String extractRendimientoMinBruto(String content) {
		String regex = "●\\s*(.*?)\\s*anual"; // Sin ^

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);

		if (matcher.find()) 
			return matcher.group(1).trim();

		return "";
	}

	@Override
	public String fechaContrato(String texto) {
		try {

			int index  = texto.indexOf("por duplicado");
			int index2 = texto.indexOf(".", index);

			return texto.substring(index + 43, index2);
		}
		catch(Exception e) {}

		return "";
	}
}