package com.data.cleaning.main.hoolbam;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.data.cleaning.main.BaseParser;
import com.data.cleaning.main.Commons;

public class HoolBam03Financiado extends BaseParser {

	public String getTipoContrato() {
		return "Promesa compraventa-Derechos fideicomisarios-Financiado";
	}
	
	public String getProyecto() {
		return "Hool Balam";
	}
	
	public String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/hool-bam-financiado/";
	}

	public String getFieldsTitle() {
//		return "% Fraccion|% Fraccion Num|Participacion|Participacion Num|Unidad|Unidad Abrev.|Contraprestacion|Contraprestacion Num|Moneda|Apartado|Apartado Num|Enganche|Enganche Num|Primer Pago|Primer Pago Num|Nr de Mensualidades|Nr de Mensualidades Num|Monto Cuota|Monto Cuota Num|Valor a Cubrir|Valor a Cubrir Num|Valor restante|Valor restante Num|Forma de Pago|Financiamiento|Opcion de compra|Constitucion|Devolucion|Vigencia|Operacion|Plazo Meses";
		return "PORC_PROPIEDAD|UNIDAD|MONTO_INVERSION|MONEDA|MONTO_APARTADO|MONTO_RESTO_ENGANCHE|FECHA_1ER_PAGO|NR_MENSUALIDADES|CUOTA_MENSUAL|MONTO_A_FINANCIAR_PARCIALIDADES|BALLOON_PAYMENT|OPCIONES PAGO BALOON PAYMENT|CONSTITUCION|DEVOLUCION_POR_TERMINACION_DE_CONTRATO|VIGENCIA_DE_CONTRATO|ENTREGA|PRORROGA_DE_ENTREGA";
	}
	
	public static void main(String[] args) {
		HoolBam03Financiado parser = new HoolBam03Financiado();
		parser.process();
	}

	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {
		
		String porcDerechos         = Commons.extract(content, "correspondientes", ")") + ")";
		String porcDerechosNum      = extractParteDecimal(porcDerechos) + "%";

//		String participacion        = Commons.extract(content, "participación equivalente", "(");
//		String participacionNum     = extractParteDecimal(participacion);

		String montoInversion       = extractContraprestacion(content);
		String montoInversionNum    = Commons.numericValue(montoInversion);
		String moneda               = Commons.extractMoneda(montoInversion);

		String apartado             = Commons.extract(content, "la cantidad", ".", "A. Previo");
		String enganche             = Commons.extract(content, "la cantidad", ".", "B. El");
		String primerPago           = Commons.extract(content, "primer pago", "hasta", "C. ");
		if(primerPago.indexOf(",") > 0)
			primerPago = primerPago.substring(0, primerPago.indexOf(","));

		if(primerPago.length() == 0)
			revisionManual = revisionManual + "Primer Pago.";

		String mensualidades        = Commons.extract(content, "el pago de", "mensualidades", "C. ");
		String valorACubrir         = Commons.extract(content, "cantidad total de", ")", "C. ");
		if(mensualidades.length() == 0)
			revisionManual = revisionManual + "Mensualidades.";
		
		String montoCuota           = Commons.extract(content, "la cantidad", "debiendo", "realizar el pago");
		if(montoCuota.length() == 0)
			revisionManual = revisionManual + "Monto Cuota.";
					
		String valorRestante        = Commons.extract(content, "restante", ")", "D. La");
		String valorRestanteNum     = Commons.numericValue(valorRestante);
		if(valorRestanteNum.length() == 0)
			revisionManual = revisionManual + "Valor Restante.";
		
		String formaDePago          = Commons.toSingleLine(Commons.extract(content, "Primera", "\n", "TERCERA."));
		String financiamiento       = Commons.toSingleLine(Commons.extract(content, "Segunda", "\n", "TERCERA."));
		String opcionDeCompra       = Commons.toSingleLine(Commons.extract(content, "Tercera", "\n", "TERCERA."));
						
		String constitucion         = Commons.extract(content, "La constitución", ",", "CUARTA");

		String devolucion           = Commons.extract(content, "devolverá", ".", "CUARTA");

		String vigencia             = Commons.extract(content, "vigente", ",", "SEXTA");

		String operacion            = Commons.extract(content, "se realizará", ".", "OCTAVA");
		
		String plazo                = Commons.extract(content, "plazo", "meses", "OCTAVA");

		String unidad               = Commons.extract(content, "Departamento número:", "\n");
		if(unidad.length() == 0)
			unidad                  = Commons.extract(content, "Unidad número:", "\n");
						
		String unidadSimple         = Commons.extraerUnidadAbrev(unidad);

		csvWriter.write("|");

		csvWriter.write(
				String.join("|",
						revisionManual, 

//						Commons.toSingleLine(porcDerechos),
						Commons.toSingleLine(porcDerechosNum),

//						Commons.toSingleLine(participacion),
//						Commons.toSingleLine(participacionNum),

//						Commons.toSingleLine(unidad),
						Commons.toSingleLine(unidadSimple),
						
//						Commons.toSingleLine(montoInversion),
						Commons.toSingleLine(montoInversionNum),
						Commons.toSingleLine(moneda),
						
//						Commons.toSingleLine(apartado),
						Commons.toSingleLine(Commons.numericValue(apartado)),
						
//						Commons.toSingleLine(enganche),
						Commons.toSingleLine(Commons.numericValue(enganche)),
						
//						Commons.toSingleLine(primerPago),
						Commons.toSingleLine(Commons.extraerFechaAPartirDeTexto(primerPago)),

//						Commons.toSingleLine(mensualidades),
						Commons.toSingleLine(Commons.numericValue(mensualidades)),

//						Commons.toSingleLine(montoCuota),
						Commons.toSingleLine(Commons.numericValue(montoCuota)),

//						Commons.toSingleLine(valorACubrir),
						Commons.toSingleLine(Commons.numericValue(valorACubrir)),
						
//						Commons.toSingleLine(valorRestante),
						Commons.toSingleLine(valorRestanteNum),

						'"' + formaDePago + " .\n" + financiamiento + " .\n" + opcionDeCompra + '"',
						
						Commons.toSingleLine(constitucion),
						Commons.toSingleLine(devolucion),

						Commons.toSingleLine(vigencia),
						Commons.toSingleLine(operacion),

						Commons.toSingleLine(plazo)));					
	}

	public static String extractParteDecimal(String content) {
		String regex = "\\s+([0-9]+(?:\\.[0-9]+)?)%";

		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(content);

		return matcher.find() ? matcher.group(1) : "";
	}

	public static String extractContraprestacion(String content) {
		try {
			int contraprestacion = content.indexOf("CONTRAPRESTACI");
			if(contraprestacion == -1)
				return "";

			int index = content.indexOf("cantidad", contraprestacion);
			int index2 = content.indexOf(")", index + 30);//buscar la coma despues de la coma del monto

			return content.substring(index, index2 + 2);

		}
		catch(Exception e) {

		}

		return "";
	}
}