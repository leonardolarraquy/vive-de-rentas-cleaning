package com.data.cleaning.main.hoolbam;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.data.cleaning.main.BaseParser;
import com.data.cleaning.main.Commons;

public class HoolBam05PromesaGarantiaMutuoInteres extends BaseParser{

	public String getTipoContrato() {
		return "Promesa compraventa-Garantía de mutuo con interés";
	}

	public String getProyecto() {
		return "Hool Balam";
	}

	public String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/hool-bam-promesa-garantia-con-mutuo-interes/";
	}

	public String getFieldsTitle() {
		return "MONTO_INVERSION|MONEDA|PORC_PROPIEDAD|TASA_DE_INTERES_ANUAL|CUOTA_MENSUAL|CARTA_GARANTIA|OPCIONES_DE_SALIDA|OBLIGACIONES_ENAJENANTE|VIGENCIA_DE_CONTRATO|PLAZO_DEVOLUCION_CAPITAL_E_INTERESES|NUMERO_CUOTAS|MONTO_X_PAGAR_FINANCIADO_X_CUOTA|CARTA_GARANTIA|EQUITY_INSTANTANEO|MONTO_EQUITY";
	}

	public static void main(String[] args) {
		HoolBam05PromesaGarantiaMutuoInteres parser = new HoolBam05PromesaGarantiaMutuoInteres();
		parser.process();
	}

	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {
		String montoInversion       = Commons.extract(content, "la cantidad", "(", "OBJETO");
		String montoInversionNum    = Commons.numericValue(montoInversion);
		String moneda               = Commons.extractMoneda(montoInversion);

		String participacion        = Commons.extract(content, "equivalente a un", "sobre", "OBJETO");
		String participacionNum     = extraerPorcentaje(participacion);

		String conceptoInteres      = Commons.extract(content, "por concepto de", "sobre", "SEGUNDA");
		if(conceptoInteres.indexOf(",") > 0)
			conceptoInteres = conceptoInteres.substring(0, conceptoInteres.indexOf(",") );

		String tasaAnual            = Commons.extract(content, "tasa anual", "equivalente", "se obligan a pagar");	

		String mensualidad          = Commons.extract(content, "cantidad de", "(", "pagaderas");

		String opcionesSalida       = Commons.toSingleLine(Commons.extract(content, "TERCERA: ","descrita")).replaceAll("\"", "'");
		String mensualidadSalida    = Commons.toSingleLine(Commons.extract(content, "En virtud", "simple", "TERCERA")).replaceAll("\"", "'");
		String oferta               = Commons.toSingleLine(Commons.extract(content, "realizada" , ",", "TERCERA"));
		String prorroga             = Commons.toSingleLine(Commons.extract(content, "dentro de un plazo", "." , "TERCERA"));

		String opcionesFormalizar   = Commons.extract(content, "constitución de la", "en " , "CUARTA");

		String vigencia             = Commons.extract(content, "vigente", "," , "SÉPTIMA");

		String plazoCapitalEInt     = Commons.extract(content, "plazo que no", ",");
		if(plazoCapitalEInt.indexOf("a partir") > 0)
			plazoCapitalEInt = plazoCapitalEInt.substring(0, plazoCapitalEInt.indexOf("a partir"));

		String exhibiciones         = Commons.extract(content, "pago de", "exhibiciones" , "se obligan").replaceAll("pago de", "");
		String montoXPagarCuotas    = Commons.extract(content, "de ", "mensuales" , "pagaderas").replaceAll("de ", "");

		String cartaGarantia = "NO";
		String equity        = "";
		String montoEquity   = "";

		if(content.indexOf("Carta garantia") > 0) {
			cartaGarantia = "SI";

			equity      = Commons.extract(content, "reconocido en un", "(", "Carta garantia").replaceAll("reconocido en un", "");
			montoEquity = Commons.extract(content, "es decir", "adicionales", "Carta garantia").replaceAll("es decir", "");
		}

		csvWriter.write("|");

		csvWriter.write(
				String.join("|",
						revisionManual, 

						Commons.toSingleLine(montoInversionNum),
						Commons.toSingleLine(moneda),						
						Commons.toSingleLine(participacionNum + "%"),

						Commons.toSingleLine(Commons.numericValue(tasaAnual)  + "%"),

						Commons.toSingleLine(Commons.numericValue(mensualidad)),
						Commons.toSingleLine(cartaGarantia),

						'"' + opcionesSalida + " .\n" + mensualidadSalida + " .\n" + oferta + " .\n" + prorroga + '"',

						Commons.toSingleLine(opcionesFormalizar),
						Commons.toSingleLine(vigencia),

						Commons.toSingleLine(plazoCapitalEInt),

						Commons.toSingleLine(exhibiciones),
						Commons.toSingleLine(montoXPagarCuotas),

						Commons.toSingleLine(cartaGarantia),
						Commons.toSingleLine(equity),
						Commons.toSingleLine(montoEquity)

						));
	}

	@Override
	public String getDireccionAdquirente(String content) {
		String domicilioAdquirente  = Commons.extract(content, "domicilio en:", " y ").replaceAll("domicilio en:", "");
		if (domicilioAdquirente.endsWith(",")) 
			domicilioAdquirente = domicilioAdquirente.substring(0, domicilioAdquirente.length() - 1);

		return domicilioAdquirente;
	}

	public static String extraerPorcentaje(String content) {
		String regex = "\\((\\d+)\\)";

		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(content);

		return matcher.find() ? matcher.group(1) : "";
	}
}