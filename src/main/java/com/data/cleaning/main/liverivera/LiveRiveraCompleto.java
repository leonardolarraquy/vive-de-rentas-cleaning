package com.data.cleaning.main.liverivera;

import java.io.BufferedWriter;
import java.io.IOException;

import com.data.cleaning.main.BaseParser;
import com.data.cleaning.main.Commons;

public class LiveRiveraCompleto extends BaseParser {
	
	public String getTipoContrato() {
		return "Promesa compraventa-Derechos fideicomisarios-Completo";
	}
	
	public String getProyecto() {
		return "Live Riviera";
	}
	
	public String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/live-rivera-promesa-compraventa-completo/";
	}
	
	public String getFieldsTitle() {
//		return "Unidad|Unidad Abrev.|Tipo Contrato|Contraprestacion|Contraprestacion Num|Moneda|Apartado|Apartado Num|Liquidacion|Liquidacion Num|Vigencia|Entrega|Entrega Num|Prorroga|Unidad Anexo";
		return "UNIDAD|MONTO_INVERSION|MONEDA|MONTO_APARTADO|MONTO_LIQUIDACION|VIGENCIA_DE_CONTRATO|FECHA_DE_ENTREGA|PRORROGA_DE_ENTREGA";
	}
	
	public static void main(String[] args) {
		LiveRiveraCompleto parser = new LiveRiveraCompleto();
		parser.process();
	}
	
	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {
		String unidad               = Commons.extract(content, "Unidad", "(", "PRIMERA");
		if(unidad.indexOf("(") > 0)
			unidad = unidad.substring(0, unidad.indexOf("(") - 1);
		
		String unidadAbrev          = Commons.extraerUnidadAbrev(unidad);				
		if(unidadAbrev.length() == 0)
			revisionManual = revisionManual + "Unidad.";
		
//		String tipoContrato         = Commons.extract(content, "mediante", "sobre", "PRIMERA.");
//		if(tipoContrato.indexOf(".") > 0)
//			tipoContrato = tipoContrato.substring(0, tipoContrato.indexOf("."));
		

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


		String vigencia             = Commons.extract(content, "vigente", ",", "SEXTA");
		
		String entrega              = Commons.extract(content, "entrega", ".", "ENTREGA DEL");
		
		String prorroga             = Commons.extract(content, "prorrogarse", " en ", "ENTREGA DEL");

//		String unidadAnexo          = Commons.extract(content, "Unidad número:", "\n", "Unidad Inmobiliaria:").replaceAll("Unidad número: ", "");

		csvWriter.write("|");
		
		csvWriter.write(
				String.join("|",
						revisionManual, 
						
//						Commons.toSingleLine(unidad),
						Commons.toSingleLine(unidadAbrev),

//						Commons.toSingleLine(tipoContrato),

//						Commons.toSingleLine(montoInversion),
						Commons.toSingleLine(montoInversionNum),
						Commons.toSingleLine(moneda),

//						Commons.toSingleLine(apartado),
						Commons.toSingleLine(apartadoNum),
						
//						Commons.toSingleLine(liquidacion),
						Commons.toSingleLine(liquidacionNum),

						Commons.toSingleLine(vigencia),
						
//						Commons.toSingleLine(entrega),
						Commons.toSingleLine(Commons.extraerFechaAPartirDeTexto(entrega)),
						Commons.toSingleLine(prorroga)
												
//						Commons.toSingleLine(unidadAnexo)
						));

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