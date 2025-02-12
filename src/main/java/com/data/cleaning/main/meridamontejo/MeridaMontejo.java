package com.data.cleaning.main.meridamontejo;

import java.io.BufferedWriter;
import java.io.IOException;

import com.data.cleaning.main.BaseParser;
import com.data.cleaning.main.Commons;

public class MeridaMontejo extends BaseParser {

	public String getTipoContrato()  {
		return "Promesa compraventa-Derechos fideicomisarios-Completo";
	}

	public String getProyecto() {
		return "Merida Montejo";
	}

	public String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/merida-montejo/";
	}

	public String getFieldsTitle() {
		return "Ubicacion|Fecha Entrega|Contraprestacion|Contraprestacion Num|Moneda|Constitucion|Terminacion|Vigencia|Unidad Inmobiliaria";
	}

	public static void main(String[] args) {
		MeridaMontejo parser = new MeridaMontejo();
		parser.process();
	}

	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {
		String ubicacion            = Commons.extract(content, "ubicado", "(", "TERCERO").replaceAll("ubicado en", "");
		if(ubicacion.length() == 0)
			revisionManual = revisionManual + "Ubicacion.";
		
		String entrega              = Commons.extract(content, "en ", ".", "ENTREGA DEL");
		
		String contraprestacion     = Commons.extract(content, "cantidad de", "(", "SEGUNDA");
		if(contraprestacion.length() == 0)
			revisionManual = revisionManual + "Contraprestacion.";

		String contraprestacionNum  = Commons.numericValue(contraprestacion);
		String moneda               = Commons.extractMoneda(contraprestacion);

		String constitucion         = Commons.extract(content, "La", ",", "CUARTA");
		String terminacion          = "no se emitan \"Los Derechos\", devolverá el valor de la Contraprestación a más tardar dentro de los 90 (noventa) días naturales";
		
		

		String unidad               = extractUnidad(content);
						
		csvWriter.write("|");

		csvWriter.write(
				String.join("|",
						revisionManual, 

						Commons.toSingleLine(ubicacion),

						Commons.toSingleLine(entrega),

						Commons.toSingleLine(contraprestacion),
						Commons.toSingleLine(contraprestacionNum),
						Commons.toSingleLine(moneda),

        				Commons.toSingleLine(constitucion),
        				Commons.toSingleLine(terminacion),
        				Commons.toSingleLine(unidad)

				));

				
	}
	
	public static String extractUnidad(String texto) {
		try {

			int index = texto.indexOf("Unidad número:");
			int index2 = texto.indexOf("\n", index + 15);

			if(index == -1) {
				index = texto.indexOf("Unidad Inmobiliaria");
				index2 = texto.indexOf("\n", index + 22);
			}

			return texto.substring(index, index2);

		}
		catch(Exception e) {}

		return "";
	}

}