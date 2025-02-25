package com.data.cleaning.main.accionesmanagement;

import java.io.BufferedWriter;
import java.io.IOException;

import com.data.cleaning.main.BaseParser;
import com.data.cleaning.main.Commons;

public class AccionesManagement extends BaseParser {
	
	public String getTipoContrato() {
		return "Contrato de Mutuo con interés";
	}
	
	public String getProyecto() {
		return "Acciones Rentiux Management";
	}
	
	public String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/acciones-management/";
	}
	
	public String getFieldsTitle() {
//		return "Otorgamiento|Otorgamiento Num|Moneda|Plazo|Plazo Num|Monto|Monto Num|Rendimiento|Rendimiento Num|Participacion|Accion";
		return "MONTO_INVERSION|MONEDA|PLAZO MESES|CAPITAL_INTERESES|RENDIMIENTO|RENDIMIENTO_NUMERO|CLAUSULAS";
	}

	public String getBeneficiario(String content) {
		String beneficiario       = Commons.extract(content, "designado", ",", "BENEFICIARIO");
		
		return beneficiario.replaceAll("designado a", "");
	}

	public static void main(String[] args) {
		AccionesManagement parser = new AccionesManagement();
		parser.process();
	}

	public String getDireccionAdquirente(String content) {
		String direccionAdquirente  = Commons.extract(content, "EL MUTUANTE", "Cualquiera", "NOVENA");
		if(direccionAdquirente.length() > 13)
			direccionAdquirente = direccionAdquirente.substring(13);

		if(direccionAdquirente.indexOf("DÉCIMA") > 0)
			direccionAdquirente = direccionAdquirente.substring(0, direccionAdquirente.indexOf("DÉCIMA"));	

		return Commons.toSingleLine(direccionAdquirente);
	}

	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {
		String otorgamiento         = Commons.extract(content, "la cantidad de", ")", "SEGUNDA.");
		String otorgamientoNum      = Commons.numericValue(otorgamiento);
		String moneda               = Commons.extractMoneda(otorgamiento);

		String plazo                = Commons.extract(content, "plazo de", "pactados", "CUARTA");
		
		String monto                = Commons.extract(content, "cantidad total", ".", "CUARTA");
		
		String rendimiento          = Commons.extract(content, "más", " por", "CUARTA");

		String clausula1            = Commons.toSingleLine(Commons.extract(content, "participación", ".", "QUINTA"));
		
		String clausula2            = Commons.toSingleLine(Commons.extract(content, "valor", ")", "QUINTA"));

		csvWriter.write("|");

		csvWriter.write(
				String.join("|",
						revisionManual, 

//						Commons.toSingleLine(otorgamiento),
						Commons.toSingleLine(otorgamientoNum),
						Commons.toSingleLine(moneda),

//						Commons.toSingleLine(plazo),
						Commons.toSingleLine(Commons.numericValue(plazo)),

//						Commons.toSingleLine(monto),
						Commons.toSingleLine(Commons.numericValue(monto)),

						Commons.toSingleLine(rendimiento),
						Commons.toSingleLine(Commons.numericValue(rendimiento)),

						'"' + clausula1 + " .\n " + clausula2 + '"'
						
						));

	}
}