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
//		return "Ubicacion|Credencial para votar|Constitucion|Contraprestacion|Contraprestacion Num|Moneda|Entrega|Entrega Num";
		return "UBICACION_PROPIEDAD|OBLIGACIONES_ENAJENANTE|MONTO_INVERSION|MONEDA|FECHA_DE_ENTREGA|CAJON_ESTACIONAMIENTO";
	}

	public static void main(String[] args) {
		TorreMonterrey parser = new TorreMonterrey();
		parser.process();
	}

	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {
		String ubicacion            = Commons.extract(content, "ubicado", "(", "SEGUNDO").replaceAll("ubicado en", "");
		if(ubicacion.length() == 0)
			revisionManual = revisionManual + "Ubicacion.";

//		String credencial           = Commons.extract(content, "No", ".", "Credencial");

		String constitucion         = Commons.extract(content, "constitu", ",", "CUARTA");

		String montoInversion       = Commons.extract(content, "la cantidad", "(", "En virtud de");
		if(montoInversion.length() == 0)
			montoInversion       = Commons.extract(content, "la cantidad", "(", "QUINTA");
		
		if(montoInversion.length() == 0)
			revisionManual = revisionManual + "Contraprestacion.";

		String montoInversionNum    = Commons.numericValue(montoInversion);
		String moneda               = Commons.extractMoneda(montoInversion);

		String fechaDeEntrega       = Commons.extract(content, "realizar", ".", "La entrega");
		if(fechaDeEntrega.length() == 0)
			revisionManual = revisionManual + "Fecha Entrega.";		
		
		String cajon               = Commons.extract(content, "estacionamiento", ".");
		if(cajon.length() > 0)
			 cajon = "SI";
		else cajon = "NO";

		csvWriter.write("|");

		csvWriter.write(
				String.join("|",
						revisionManual, 

						Commons.toSingleLine(ubicacion),

//						Commons.toSingleLine(credencial),
						Commons.toSingleLine(constitucion),

//						Commons.toSingleLine(montoInversion),
						Commons.toSingleLine(montoInversionNum),
						Commons.toSingleLine(moneda),

//						Commons.toSingleLine(fechaDeEntrega),
						Commons.toSingleLine(Commons.extraerFechaAPartirDeTexto(fechaDeEntrega)),
						Commons.toSingleLine(cajon)
						
						));
				
	}
}