package com.data.cleaning.main.hoolbam;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.data.cleaning.main.BaseParser;
import com.data.cleaning.main.Commons;

public class HoolBam02Completo extends BaseParser{

	public String getTipoContrato() {
		return "Promesa compraventa-Derechos fideicomisarios-Completo";
	}
	
	public String getProyecto() {
		return "Hool Balam";
	}
	
	public String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/hool-bam-completo/";
	}

	public String getFieldsTitle() {
		return "Derechos|Derecho Num|Participacion|Participacion Num|Unidad|Unidad Abrev.|Derecho|Contraprestacion|Contraprestacion Num|Moneda|Consitucion|Devolucion|Entrega|Entrega Num|Plazo";
	}

	public static void main(String[] args) {
		HoolBam02Completo parser = new HoolBam02Completo();
		parser.process();
	}
	
	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {
		String porcDerechos         = Commons.extract(content, "correspondientes", ")") + ")";
		String porcDerechosNum      = extractParteDecimal(porcDerechos);

		String participacion        = Commons.extract(content, "participación equivalente", "(");
		String participacionNum     = extractParteDecimal(participacion);
		if(participacionNum.length() > 0) 
			revisionManual   = revisionManual + "Participacion.";

		String unidad               = Commons.extract(content, "Departamento número:", "\n");
		if(unidad.length() == 0)
			unidad                  = Commons.extract(content, "Unidad número:", "\n");
		
		String unidadSimple         = Commons.extraerUnidadAbrev(unidad);

		String derecho              = Commons.extract(content, "tendrá derecho", ".", "PRIMERA");
		if(derecho.indexOf(";") > 0)
			derecho = derecho.substring(0, derecho.indexOf(";"));

		String contraprestacion     = extractContraprestacion(content);
		String contraprestacionNum  = Commons.numericValue(contraprestacion);
		String moneda               = Commons.extractMoneda(contraprestacion);

		if(contraprestacionNum.length() == 0) {
			contraprestacion = "";
			revisionManual   = revisionManual + "Contraprestacion.";
			moneda           = "";
		}

		String constitucion         = Commons.extract(content, "La constitución", ",", "CUARTA");

		String devolucion           = Commons.extract(content, "devolverá", "naturales", "CUARTA");

		String entrega              = Commons.extract(content, "La entrega de", ".", "SÉPTIMA");
		if(entrega.indexOf("de acuerdo") > 0)
			entrega = entrega.substring(0, entrega.indexOf("de acuerdo"));

		if(entrega.indexOf(",", 30) > 0)
			entrega = entrega.substring(0, entrega.indexOf(",", 30));

		String entregaNum           = Commons.extraerFechaAPartirDeTexto(entrega);

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

						Commons.toSingleLine(entrega),
						Commons.toSingleLine(entregaNum),

						Commons.toSingleLine(plazo)
						
				));

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