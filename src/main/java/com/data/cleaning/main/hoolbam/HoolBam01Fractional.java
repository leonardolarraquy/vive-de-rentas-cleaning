package com.data.cleaning.main.hoolbam;

import java.io.BufferedWriter;
import java.io.IOException;

import com.data.cleaning.main.BaseParser;
import com.data.cleaning.main.Commons;

public class HoolBam01Fractional extends BaseParser {
	
	public String getTipoContrato() {
		return "Promesa compraventa-Derechos fideicomisarios-Fractional";
	}
	
	public String getProyecto() {
		return "Hool Balam";
	}
	
	public String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/hool-bam-fractional/";
	}

	public String getFieldsTitle() {
		return "% Fraccion|% Fraccion Num|% Participacion|% Participacion Num|Unidad|Unidad Abrev.|Derecho de uso|Contraprestacion|Contraprestacion Num|Moneda|Consitucion|Devolucion|Vigencia|Entrega|Fecha Entrega Num|Plazo";
	}
	
	public static void main(String[] args) {
		HoolBam01Fractional parser = new HoolBam01Fractional();
		parser.process();
	}
	
	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {
		
		String porcDerechos         = Commons.extract(content, "correspondientes", ")") + ")";
		String porcDerechosNum      = Commons.extractParteDecimal(porcDerechos);
		if(porcDerechosNum.length() > 0)
			porcDerechosNum  = porcDerechosNum + "%";
		else revisionManual  = revisionManual + "Fraccion.";

		String participacion        = Commons.extract(content, "equivalente", "(");
		String participacionNum     = Commons.extractParteDecimal(participacion);
		if(participacionNum.length() > 0)
			participacionNum  = participacionNum + "%";
		else revisionManual  = revisionManual + "Participacion.";

		String unidad               = Commons.extract(content, "correspondientes a la", "(");
		if(unidad.indexOf("(") > 0)
			unidad = unidad.substring(0, unidad.indexOf("(") - 1);

		if(unidad.indexOf("Deri") > 0)
			unidad = unidad.substring(0, unidad.indexOf("Deri") - 1);
		
		String unidadSimple         = Commons.extraerUnidadAbrev(unidad);

		String derecho              = Commons.extract(content, "tendrá derecho", ",");
		if(derecho.indexOf("dentro") > 0)
			derecho = derecho.substring(0, derecho.indexOf("dentro"));

		if(derecho.indexOf(";") > 0)
			derecho = derecho.substring(0, derecho.indexOf(";"));

		String contraprestacion     = extractContraprestacion(content);
		String contraprestacionNum  = Commons.numericValue(contraprestacion);
		if(contraprestacionNum.length() == 0)
			revisionManual  = revisionManual + "Contraprestacion.";
		
		String moneda               = Commons.extractMoneda(contraprestacion);
		
		String constitucion         = Commons.extract(content, "La constitución", ",", "CUARTA");

		String devolucion           = Commons.extract(content, "devolverá", "naturales", "CUARTA");

		String vigencia             = Commons.extract(content, "vigente", "o antes", "SEXTA");

		String entrega              = Commons.extract(content, "La entrega de", ".", "SÉPTIMA");
		if(entrega.indexOf("de acuerdo") > 0)
			entrega = entrega.substring(0, entrega.indexOf("de acuerdo"));
		
		String entregaNum           = Commons.extraerFechaAPartirDeTexto(entrega);
		if(entregaNum.length() == 0)
			revisionManual  = revisionManual + "Fecha Entrega.";
		
		String plazo                = Commons.extract(content, "plazo", "en ", "ENTREGA DEL");

		csvWriter.write("|");

		csvWriter.write(
				String.join("|",
						revisionManual, 
						
						Commons.toSingleLine(porcDerechos),
						Commons.toSingleLine(porcDerechosNum),

						Commons.toSingleLine(participacion),
						Commons.toSingleLine(participacionNum),

						Commons.toSingleLine(unidad),
						Commons.toSingleLine(unidadSimple),
						
						Commons.toSingleLine(derecho),

						Commons.toSingleLine(contraprestacion),
						Commons.toSingleLine(contraprestacionNum),
						Commons.toSingleLine(moneda),

						Commons.toSingleLine(constitucion),
						Commons.toSingleLine(devolucion),
						Commons.toSingleLine(vigencia),

						Commons.toSingleLine(entrega),
						Commons.toSingleLine(entregaNum),

						Commons.toSingleLine(plazo)
				));

	}
	
	public static String extractUnidad(String texto) {
		try {

			int index = texto.indexOf("Unidad número:");
			int index2 = texto.indexOf("\n", index + 15);

			if(index == -1) {
				index = texto.indexOf("Unidad Inmobiliaria:");
				index2 = texto.indexOf("\n", index + 22);
			}


			return texto.substring(index, index2);

		}
		catch(Exception e) {}

		return "";
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

	public static String extractDerecho(String content) {
		try {
			int index = content.indexOf("con el ", content.indexOf("deseo celebrar"));
			int index2 = content.indexOf("conforme", index) - 1;

			int index3 = content.indexOf(",", index);
			if(index3 != -1 && index3 < index2)
				index2 = index3;

			index3 = content.indexOf(".", index);
			if(index3 != -1 && index3 < index2)
				index2 = index3;

			return content.substring(index, index2);

		}
		catch(Exception e) {

		}

		return "";
	}	
}