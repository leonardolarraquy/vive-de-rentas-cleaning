package com.data.cleaning.main.livinguniversidad2;

import java.io.BufferedWriter;
import java.io.IOException;

import com.data.cleaning.main.BaseParser;
import com.data.cleaning.main.Commons;

public class LivingUniversidad2 extends BaseParser {

	public String getTipoContrato()  {
		return "Cancelado-Cliente reasignado";
	}

	public String getProyecto() {
		return "Living Universidad 2";
	}

	public String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/living-universidad-2/";
	}

	public String getFieldsTitle() {
//		return "Ubicacion|Fecha Entrega|Contraprestacion|Contraprestacion Num|Moneda|Constitucion|Unidad Inmobiliaria";
		return "UBICACION_PROPIEDAD|FECHA_DE_ENTREGA|MONTO_INVERSION|MONEDA|OBLIGACIONES_ENAJENANTE|UNIDAD|CARTA_GARANTIA|TASA_INTERES_ANUAL|PLAZO_MESES|FECHA_COMIENZO_PAGO_RENDIMIENTOS";
	}

	public static void main(String[] args) {
		LivingUniversidad2 parser = new LivingUniversidad2();
		parser.process();
	}

	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {
		String ubicacion            = Commons.extract(content, "ubicado", "(", "TERCERO").replaceAll("ubicado en", "");
		if(ubicacion.length() == 0)
			revisionManual = revisionManual + "Ubicacion.";
		
		String montoInversion     = Commons.extract(content, "cantidad de", "(", "SEGUNDA");
		if(montoInversion.length() == 0)
			revisionManual = revisionManual + "Contraprestacion.";

		String montoInversionNum  = Commons.numericValue(montoInversion);
		String moneda               = Commons.extractMoneda(montoInversion);

		String constitucion         = Commons.extract(content, "La", ",", "CUARTA");

		String entrega              = Commons.extract(content, "en ", ".", "ENTREGA DEL");
		
		String unidad               = extractUnidad(content);
						
		csvWriter.write("|");

		csvWriter.write(
				String.join("|",
						revisionManual, 

						Commons.toSingleLine(ubicacion),

						Commons.toSingleLine(entrega),

//						Commons.toSingleLine(montoInversion),
						Commons.toSingleLine(montoInversionNum),
						Commons.toSingleLine(moneda),

        				Commons.toSingleLine(constitucion),
        				Commons.toSingleLine(unidad),

        				Commons.toSingleLine("NO"),
        				Commons.toSingleLine(""),
        				Commons.toSingleLine(""),
        				Commons.toSingleLine("")
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