package com.data.cleaning.main.torremonterrey;

import java.io.BufferedWriter;
import java.io.IOException;

import com.data.cleaning.main.BaseParser;
import com.data.cleaning.main.Commons;

public class TorreMonterrey extends BaseParser {

	public String getTipoContrato()  {
		return "Torre Monterrey Barrio Antiguo";
	}

	public String getProyecto() {
		return "Torre Monterrey Barrio Antiguo";
	}

	public String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/torre-monterrey/";
	}

	public String getFieldsTitle() {
		return "Ubicacion|Credencial para votar|Constitucion|Contraprestacion|Contraprestacion Num|Moneda|Entrega|Entrega Num";
	}

	public static void main(String[] args) {
		TorreMonterrey parser = new TorreMonterrey();
		parser.process();
	}

	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {
		String ubicacion            = Commons.extract(content, "ubicado", "(", "SEGUNDO").replaceAll("ubicado en", "");
		if(ubicacion.length() == 0)
			revisionManual = revisionManual + "Ubicacion.";

		String credencial           = Commons.extract(content, "No", ".", "Credencial");

		String constitucion         = Commons.extract(content, "constitu", ",", "CUARTA");

		String contraprestacion     = Commons.extract(content, "cantidad de", "(", "QUINTA");
		if(contraprestacion.length() == 0)
			revisionManual = revisionManual + "Contraprestacion.";

		String contraprestacionNum  = Commons.numericValue(contraprestacion);
		String moneda               = Commons.extractMoneda(contraprestacion);

		String fechaDeEntrega       = Commons.extract(content, "realizar", ".", "La entrega");
		if(fechaDeEntrega.length() == 0)
			revisionManual = revisionManual + "Fecha Entrega.";					

		csvWriter.write("|");

		csvWriter.write(
				String.join("|",
						revisionManual, 

						Commons.toSingleLine(ubicacion),

						Commons.toSingleLine(credencial),
						Commons.toSingleLine(constitucion),

						Commons.toSingleLine(contraprestacion),
						Commons.toSingleLine(contraprestacionNum),
						Commons.toSingleLine(moneda),

						Commons.toSingleLine(fechaDeEntrega),
						Commons.toSingleLine(Commons.extraerFechaAPartirDeTexto(fechaDeEntrega))));
				
	}
}