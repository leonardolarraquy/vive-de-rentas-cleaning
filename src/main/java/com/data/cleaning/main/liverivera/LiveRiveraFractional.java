package com.data.cleaning.main.liverivera;

import java.io.BufferedWriter;
import java.io.IOException;

import com.data.cleaning.main.BaseParser;
import com.data.cleaning.main.Commons;

public class LiveRiveraFractional extends BaseParser {

	public String getTipoContrato() {
		return "Promesa compraventa-Derechos fideicomisarios-Fractional";
	}

	public String getProyecto() {
		return "Live Riviera";
	}

	public String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/live-rivera-promesa-compraventa-fractional/";
	}

	public String getFieldsTitle() {
//		return "Tickets|Tickets Num|% Fraccion|% Fraccion Num|Unidad|Unidad Abrev.|Contraprestacion|Contraprestacion Num|Moneda|Apartado|Apartado Num|Liquidacion|Liquidacion Num|Carta Garantia|Opciones de formalizacion de contrato|Mensualidad en la que toma decision de contrato|Obligaciones Enajenante|Plazo de penalizacion tras entrega|Vigencia|Unidad Anexo|Forma de Pago|Plazo Garantia|Rentabilidad Anual|Rentabilidad Anual Num|Equity";
		return "NR_TICKETS|PORC_PROPIEDAD|UNIDAD|MONTO_INVERSION|MONEDA|MONTO_APARTADO|MONTO_LIQUIDACION|CARTA_GARANTIA|OPCIONES_DE_FORMALIZACION_DE_CONTRATO|MENSUALIDAD_AVISO_OPCIONES_SALIDA|OBLIGACIONES_ENAJENANTE|DEVOLUCION_POR_TERMINACION_DE_CONTRATO|VIGENCIA_DE_CONTRATO|PLAZO_PAGO_RENTABILIDAD|TASA_ANUAL_RENTABILIDAD_GARANTIZADA|EQUITY_INSTANTANEO";
	}

	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {
		String participacion        = Commons.extract(content, "equivalente", ",", "OBJETO");
		String participacionNum     = Commons.numericValue(participacion);

		String participacionPorc    = Commons.extract(content, "decir", ",", "OBJETO");
		String participacionPorcNum = Commons.numericValue(participacionPorc) + "%";

		String unidad               = Commons.extract(content, "correspondientes a la", "(");
		if(unidad.indexOf("(") > 0)
			unidad = unidad.substring(0, unidad.indexOf("(") - 1);

		String unidadAbrev          = Commons.extraerUnidadAbrev(unidad);				
		if(unidadAbrev.length() == 0)
			revisionManual = revisionManual + "Unidad.";					

		String montoInversion       = Commons.extract(content, "la cantidad", ")", "SEGUNDA") + ")";
		String montoInversionNum    = Commons.numericValue(montoInversion);
		String moneda               = Commons.extractMoneda(montoInversion);

		String apartado             = Commons.extract(content, "entregó", ")", "SEGUNDA");
		if(apartado.length() > 0)
			apartado = apartado + ")";
		String apartadoNum          = Commons.numericValue(apartado);
		if(apartadoNum.length() == 0)
			revisionManual = revisionManual + "Apartado.";					


		String liquidacion          = Commons.extract(content, "entregará al", ")");
		if(liquidacion.length() > 0)
			liquidacion = liquidacion + ")";

		String liquidacionNum       = Commons.numericValue(liquidacion);
		if(liquidacionNum.length() == 0)
			revisionManual = revisionManual + "Liquidacion.";					

		String cartaGarantia        = Commons.extract(content, "mediante", "anexa", "Adicionalmente");
		if(cartaGarantia.length() == 0) {
			cartaGarantia = "NO";
			revisionManual = revisionManual + "Carta Garantia.";			
		}
		else cartaGarantia = "SI";

		String opcionesFinalizacion = Commons.extract(content, "mediante", "que", "estipulado").replaceAll("mediante la", "");
		if(opcionesFinalizacion.indexOf(",") > 0)
			opcionesFinalizacion = opcionesFinalizacion.substring(0, opcionesFinalizacion.indexOf(","));

		String mensualidad          = Commons.extract(content, "mensualidad", "(", "CUARTA.");
		if(mensualidad.indexOf("B.") > 0)
			mensualidad = mensualidad.substring(0, mensualidad.indexOf("B."));	

		String obligaciones         = Commons.extract(content, "A.", "para", "al efecto");
		if(obligaciones.indexOf(" en") > 0)
			obligaciones = obligaciones.substring(0, obligaciones.indexOf(" en"));	

		String plazo                = Commons.extract(content, "dentro de", "llevando", "una vez finalizado");

		String vigencia             = Commons.extract(content, "vigente", ",", "SÉPTIMA");

		String beneficiario         = Commons.extract(content, "C. ", ",", "BENEFICIARIO");
		if(beneficiario.length() > 0)
			beneficiario = beneficiario.substring(2, beneficiario.length());

//		String unidadAnexo          = Commons.extract(content, "Unidad número:", "\n", "Unidad Inmobiliaria:").replaceAll("Unidad número: ", "");

//		String formaDePago          = Commons.extract(content, "Forma de Pago:", "EL", "ANEXO").replaceAll("Forma de Pago:", "");
//		if(formaDePago.indexOf(".") > 0)
//			formaDePago = formaDePago.substring(0, formaDePago.indexOf("."));	

		String plazoGarantia        = Commons.extract(content, "exclusivamente", "denominado", "Adquirente única y exclusivamente");
		String rentabilidadAnual    = Commons.extract(content, "correspondiente al", ",", "exclusivamente");
		String equity               = Commons.extract(content, "un equity", ")", "exclusivamente");
		if(equity.length() > 0)
			equity = equity + ")";
		else revisionManual = revisionManual + "Equity.";

		csvWriter.write("|");

		csvWriter.write(
				String.join("|",
						revisionManual, 

//						Commons.toSingleLine(participacion),
						Commons.toSingleLine(participacionNum),

//						Commons.toSingleLine(participacionPorc),
						Commons.toSingleLine(participacionPorcNum),

//						Commons.toSingleLine(unidad),
						Commons.toSingleLine(unidadAbrev),

//						Commons.toSingleLine(montoInversion),
						Commons.toSingleLine(montoInversionNum),
						Commons.toSingleLine(moneda),

//						Commons.toSingleLine(apartado),
						Commons.toSingleLine(apartadoNum),

//						Commons.toSingleLine(liquidacion),
						Commons.toSingleLine(liquidacionNum),

						Commons.toSingleLine(cartaGarantia),
						Commons.toSingleLine(opcionesFinalizacion),
						Commons.toSingleLine(mensualidad),


						Commons.toSingleLine(obligaciones),
						Commons.toSingleLine(plazo),
						Commons.toSingleLine(vigencia),

//						Commons.toSingleLine(unidadAnexo),
//						Commons.toSingleLine(formaDePago),

						Commons.toSingleLine(plazoGarantia),

//						Commons.toSingleLine(rentabilidadAnual),
						Commons.toSingleLine(Commons.numericValue(rentabilidadAnual) + "%"),

						Commons.toSingleLine(equity)));

	}

	public static void main(String[] args) {
		LiveRiveraFractional parser = new LiveRiveraFractional();
		parser.process();
	}

	public static String extractFechaContrato(String texto) {
		try {

			int index  = texto.indexOf("día", texto.indexOf("lo firman de conformidad"));
			int index2 = texto.indexOf("E", index);

			return texto.substring(index - 4, index2);
		}
		catch(Exception e) {}

		return "";
	}
}