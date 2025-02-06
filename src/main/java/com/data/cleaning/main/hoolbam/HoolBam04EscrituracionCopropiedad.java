package com.data.cleaning.main.hoolbam;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.data.cleaning.main.BaseParser;
import com.data.cleaning.main.Commons;

public class HoolBam04EscrituracionCopropiedad extends BaseParser {

	public String getTipoContrato() {
		return "Promesa compraventa-Escrituración en copropiedad";
	}
	
	public String getProyecto() {
		return "Hool Balam";
	}
	
	public String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/hool-bam-promesa-escrituracion-copropiedad/";
	}

	public String getFieldsTitle() {
		return "Porcentaje de Copropiedad|Porcentaje de Copropiedad Num|Unidad|Unidad Num|Proposito|Derechos|Contraprestacion|Contraprestacion Num|Moneda|Apartado|Apartado Num|Valor restante|Valor restante Num|Forma de Pago|Plazo";
	}

	public static void main(String[] args) {
		HoolBam04EscrituracionCopropiedad parser = new HoolBam04EscrituracionCopropiedad();
		parser.process();
	}

	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {
		String porcentaje           = Commons.extract(content, "porcentaje correspondiente", "de");
		String porcentajeNum        = extractParteDecimal(porcentaje);
		if(porcentajeNum.length() == 0)
			revisionManual = revisionManual + "Porcentaje.";
		else {
			porcentajeNum = porcentajeNum + "%";
		}

		String unidad               = Commons.extract(content, "Unidad Inmobiliaria:", ".");
		unidad = unidad.replaceAll("Unidad Inmobiliaria:", "");
		if(unidad.length() > 24)
			unidad = unidad.substring(0,24);

		String unidadSimple         = extractUnidad(unidad);
		
		String proposito            = Commons.extract(content, "propósitos", "pero", "OBJETO");
		String derecho              = Commons.extract(content, "derecho", ".", "OBJETO");
		
		String contraprestacion     = extractContraprestacion(content);
		String contraprestacionNum  = Commons.numericValue(contraprestacion);
		String moneda               = Commons.extractMoneda(contraprestacion);
		if(contraprestacionNum.length() == 0)
			revisionManual = revisionManual + "Contraprestacion.";

		String apartado             = Commons.extract(content, "la cantidad", ".", "entregó al");
		String montoRestante        = Commons.extract(content, "la cantidad", ".", "se obliga");

		String plazo                = Commons.extract(content, "plazo", "en ", "SÉPTIMA");

		String formaDePago          = Commons.extract(content, "Forma de Pago:", "EL", "ANEXO");
		if(formaDePago.indexOf(".") > 0)
			formaDePago = formaDePago.substring(0, formaDePago.indexOf("."));

		csvWriter.write("|");

		csvWriter.write(
				String.join("|",
						revisionManual, 

						Commons.toSingleLine(porcentaje),
						Commons.toSingleLine(porcentajeNum),

						Commons.toSingleLine(unidad),
						Commons.toSingleLine(unidadSimple),
						
						Commons.toSingleLine(proposito),
						Commons.toSingleLine(derecho),

						Commons.toSingleLine(contraprestacion),
						Commons.toSingleLine(contraprestacionNum),
						Commons.toSingleLine(moneda),
						
						Commons.toSingleLine(apartado),
						Commons.toSingleLine(Commons.numericValue(apartado)),
						Commons.toSingleLine(montoRestante),
						Commons.toSingleLine(Commons.numericValue(montoRestante)),

						Commons.toSingleLine(formaDePago),

						Commons.toSingleLine(plazo)));

	}

	public static String extractUnidad(String texto) {
		// Expresión regular mejorada
		String regex = "(?:Unidad|Departamento) número:\\s*([A-Z]*\\-?[0-9]+[A-Z]*)";

		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(texto);

		if (matcher.find()) 
			return matcher.group(1).trim();

		return "";
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