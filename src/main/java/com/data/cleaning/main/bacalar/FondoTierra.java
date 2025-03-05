package com.data.cleaning.main.bacalar;

import java.io.BufferedWriter;
import java.io.IOException;

import com.data.cleaning.main.BaseParser;
import com.data.cleaning.main.Commons;

public class FondoTierra extends BaseParser {
	
	public String getTipoContrato() {
		return "Contrato de Mutuo con interés";
	}
	
	public String getProyecto() {
		return "Ecotown Bacalar";
	}
	
	public String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/bacalar-fondo-tierra/";
	}
	
	public String getFieldsTitle() {
//		return "Otorgamiento|Otorgamiento Num|Moneda|Plazo|Plazo Num|Interes|Interes Num|Clausula I|Clausula II|";
		return "MONTO_INVERSION|MONEDA|PLAZO_MESES|TASA_DE_INTERES_ANUAL|CLAUSULAS|";
	}
	
	@Override
	public String getBeneficiario(String content) {
		String res =  Commons.extract(content, "designado a", "cuyo", "BENEFICIARIO").replaceAll("designado a", "").replaceAll(",", "");
		if(res.indexOf("de las cantidades") > 0)
			res = res.substring(0, res.indexOf("de las cantidades"));
		
		return res;
	}

	@Override
	public String getDireccionAdquirente(String content) {
		String direccionAdquirente  = Commons.extract(content, "EL MUTUANTE", "Cualquiera", "OCTAVA");
		if(direccionAdquirente.length() > 13)
			direccionAdquirente = direccionAdquirente.substring(13);

		if(direccionAdquirente.indexOf("DÉCIMA") > 0)
			direccionAdquirente = direccionAdquirente.substring(0, direccionAdquirente.indexOf("DÉCIMA"));	

		if(direccionAdquirente.indexOf("Cualquiera") > 0)
			direccionAdquirente = direccionAdquirente.substring(0, direccionAdquirente.indexOf("Cualquiera"));	

		return Commons.toSingleLine(direccionAdquirente);
	}

	public static void main(String[] args) {
		FondoTierra parser = new FondoTierra();
		parser.process();
	}

	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {
		String montoInversion       = Commons.extract(content, "la cantidad de", ")", "PRIMERA.") + ")";
		String montoInversionNum    = Commons.numericValue(montoInversion);
		String moneda               = Commons.extractMoneda(montoInversion);

		String plazo                = Commons.extract(content, "en un plazo", ",", "TERCERA");
		String interes              = Commons.extract(content, "recibir", "sobre", "CUARTA");

		String clausulaI            = Commons.toSingleLine(Commons.extract(content, "i.", ".", "CUARTA"));
		String clausulaII           = Commons.toSingleLine(Commons.extract(content, "ii.", ".", "CUARTA"));

		csvWriter.write("|");

		csvWriter.write(
				String.join("|",
						revisionManual, 

//						Commons.toSingleLine(montoInversion),
						Commons.toSingleLine(montoInversionNum),
						Commons.toSingleLine(moneda),

//						Commons.toSingleLine(plazo),
						Commons.toSingleLine(Commons.numericValue(plazo)),
						
//						Commons.toSingleLine(interes),
						Commons.toSingleLine(Commons.numericValue(interes) + "%"),

						'"' + clausulaI + " .\n" + clausulaII + '"'

						));

	}
}