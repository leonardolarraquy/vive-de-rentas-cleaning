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
//		return "Contraprestacion|Contraprestacion Num|Moneda|Participacion|Participacion Num|Compromete a Pagar|Compromete a Pagar Num|Concepto Interes|Tasa|Tasa num|Monto interes anual|Monto interes anual num|Cuota Mensual|Cuota Mensual Num|Carta Garantia|Opciones|Mensualidad para ejercer derecho de salida|Mensualidad para ejercer derecho de salida Num|Oferta|Prorroga pago derecho de salida|Participacion|Participacion Num|Opciones para formalizar contrato final|Vigencia|Equity Instantaneo";
		return "MONTO INVERSION|MONEDA|PORC_PROPIEDAD|TASA_DE_INTERES_ANUAL|CUOTA_MENSUAL|CARTA_GARANTIA|OPCIONES_DE_SALIDA|MENSUALIDAD PARA EJERCER DERECHO DE SALIDA|MENSUALIDAD PARA EJERCER DERECHO DE SALIDA NUM|OFERTA|PRORROGA PAGO DERECHO DE SALIDA|PARTICIPACION|PARTICIPACION NUM|OPCIONES PARA FORMALIZAR CONTRATO FINAL|VIGENCIA|EQUITY INSTANTANEO";
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
		
//		String comprometeAlPago     = Commons.extract(content, "al pago de", "más", "SEGUNDA");
//		String comprometeAlPagoNum  = Commons.numericValue(comprometeAlPago);

		String conceptoInteres      = Commons.extract(content, "por concepto de", "sobre", "SEGUNDA");
		if(conceptoInteres.indexOf(",") > 0)
			conceptoInteres = conceptoInteres.substring(0, conceptoInteres.indexOf(",") );
		
		String tasaAnual            = Commons.extract(content, "tasa anual", "equivalente", "se obligan a pagar");	
		
//		String montoEquivalente     = Commons.extract(content, "la cantidad", "(", "se obligan a pagar");
		
		String mensualidad          = Commons.extract(content, "cantidad de", "(", "pagaderas");
		
		String cartaGarantia        = Commons.extract(content, "Adicionalmente, mediante", "anexa", "SEGUNDA");
		cartaGarantia = (cartaGarantia.length() > 0) ? "SI" : "NO";
		
		String opcionesSalida       = Commons.toSingleLine(Commons.extract(content, "TERCERA: ","concluir")).replaceAll("\"", "'");
		String mensualidadSalida    = Commons.toSingleLine(Commons.extract(content, "posteriores a", ")", "En virtud").replaceAll("posteriores a", "")).replaceAll("\"", "'")  + ")";
		
		String oferta               = Commons.extract(content, "realizada" , ",", "TERCERA");
		String prorroga             = Commons.extract(content, "dentro de un plazo", "." , "TERCERA");
		String participacion2       = Commons.extract(content, "equivalente a", "del bien" , "TERCERA");
		
		String opcionesFormalizar   = Commons.extract(content, "constitución de la", "en " , "CUARTA");
		
		String vigencia             = Commons.extract(content, "vigente", "," , "SÉPTIMA");

		String equity               = Commons.extract(content, "equity instantáneo", "del " , "JURISDICCIÓN");

		csvWriter.write("|");

		csvWriter.write(
				String.join("|",
						revisionManual, 

//						Commons.toSingleLine(montoInversion),
						Commons.toSingleLine(montoInversionNum),
						Commons.toSingleLine(moneda),
						
//						Commons.toSingleLine(participacion),
						Commons.toSingleLine(participacionNum + "%"),
						
//						Commons.toSingleLine(comprometeAlPago),
//						Commons.toSingleLine(comprometeAlPagoNum),
						
//						Commons.toSingleLine(conceptoInteres),
						
//						Commons.toSingleLine(tasaAnual),
						Commons.toSingleLine(Commons.numericValue(tasaAnual)  + "%"),
						
//						Commons.toSingleLine(montoEquivalente),
//						Commons.toSingleLine(Commons.numericValue(montoEquivalente)),
						
//						Commons.toSingleLine(mensualidad),
						Commons.toSingleLine(Commons.numericValue(mensualidad)),
						
						Commons.toSingleLine(cartaGarantia),

						'"' + opcionesSalida + " .\n" + mensualidadSalida + '"',

						Commons.toSingleLine(oferta),
						
						Commons.toSingleLine(prorroga),
						
						Commons.toSingleLine(participacion2),
						Commons.toSingleLine(Commons.numericValue(participacion2) + "%"),
						Commons.toSingleLine(opcionesFormalizar),
						Commons.toSingleLine(vigencia),
						Commons.toSingleLine(equity)));
						
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