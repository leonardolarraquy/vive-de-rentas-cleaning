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
//		return "Ubicacion|Fecha Entrega|Contraprestacion|Contraprestacion Num|Moneda|Constitucion|Terminacion|Vigencia|Unidad Inmobiliaria";
		return "UBICACION_PROPIEDAD|FECHA_DE_ENTREGA|MONTO_INVERSION|MONEDA|OBLIGACIONES_ENAJENANTE|DEVOLUCION_POR_TERMINACION DE_CONTRATO|VIGENCIA_CONTRATO|UNIDAD";
	}

	public static void main(String[] args) {
		MeridaMontejo parser = new MeridaMontejo();
		parser.process();
	}

	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {
		String ubicacion            = Commons.extract(content, "ubicado", "(", "TERCERO").replaceAll("ubicado en", "");
		if(ubicacion.length() == 0)
			revisionManual = revisionManual + "Ubicacion.";
		
		String entrega              = Commons.extract(content, "mes", ".", "ENTREGA DEL");
		
		String montoInversion       = Commons.extract(content, "cantidad de", "(", "SEGUNDA");
		if(montoInversion.length() == 0)
			revisionManual = revisionManual + "Contraprestacion.";

		String montoInversionNum    = Commons.numericValue(montoInversion);
		String moneda               = Commons.extractMoneda(montoInversion);

		String constitucion         = Commons.extract(content, "La", ",", "CUARTA");
		String terminacion          = "no se emitan \"Los Derechos\", devolverá el valor de la Contraprestación a más tardar dentro de los 90 (noventa) días naturales";

		String vigencia             = Commons.extract(content, "hasta", ",", "SEXTA");
		
		String unidad               = extractUnidad(content);
						
		csvWriter.write("|");

		csvWriter.write(
				String.join("|",
						revisionManual, 

						Commons.toSingleLine(ubicacion),

						Commons.toSingleLine(Commons.extraerFechaAPartirDeTexto(entrega)),

//						Commons.toSingleLine(montoInversion),
						Commons.toSingleLine(montoInversionNum),
						Commons.toSingleLine(moneda),

        				Commons.toSingleLine(constitucion),
        				Commons.toSingleLine(terminacion),

        				Commons.toSingleLine(vigencia),

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