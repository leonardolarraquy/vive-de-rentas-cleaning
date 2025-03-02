package com.data.cleaning.main.toledosuites;

import java.io.BufferedWriter;
import java.io.IOException;

import com.data.cleaning.main.BaseParser;
import com.data.cleaning.main.Commons;

public class ToledoSuites extends BaseParser {

	public String getTipoContrato()  {
		return "Toledo Suites Chapultepec";
	}

	public String getProyecto() {
		return "Toledo Suites Chapultepec";
	}

	public String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/toledo-suites/";
	}

	public String getFieldsTitle() {
//		return "Ubicacion|Derechos|Emision|Contraprestacion|Contraprestacion Num|Moneda|Devolucion|Porcentaje";
		return "UBICACION_PROPIEDAD|PROC_PROPIEDAD|VIGENCIA_DE_CONTRATO|MONTO_INVERSION|MONEDA|DEVOLUCION_POR_TERMINACION_DE_CONTRATO";
	}

	public static void main(String[] args) {
		ToledoSuites parser = new ToledoSuites();
		parser.process();
	}

	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {
		String ubicacion            = Commons.extract(content, "ubicado", "(", "UNICO").replaceAll("ubicado en", "");
		if(ubicacion.length() == 0)
			revisionManual = revisionManual + "Ubicacion.";
		
		String derechos             = Commons.extract(content, "emisión", ")", "virtud");
		derechos                    = Commons.extractParteDecimal(derechos) + "%";

		String montoInversion       = Commons.extract(content, "cantidad de", "(", "SEGUNDA");
		if(montoInversion.length() == 0)
			revisionManual = revisionManual + "Contraprestacion.";

		String montoInversionNum    = Commons.numericValue(montoInversion);
		String moneda               = Commons.extractMoneda(montoInversion);

		String devolucion           = Commons.extract(content, "devolverá", ".", "CUARTA");

		String vigencia             = Commons.extract(content, "hasta", ",", "SEXTA");
		
//		String porcentaje           = Commons.extract(content, "Porcentaje", ")") + ")";
				
		csvWriter.write("|");

		csvWriter.write(
				String.join("|",
						revisionManual, 

						Commons.toSingleLine(ubicacion),

						Commons.toSingleLine(derechos),
						Commons.toSingleLine(vigencia),

//						Commons.toSingleLine(montoInversion),
						Commons.toSingleLine(montoInversionNum),
						Commons.toSingleLine(moneda),

        				Commons.toSingleLine(devolucion)
//        				Commons.toSingleLine(porcentaje)

				));

				
	}
}