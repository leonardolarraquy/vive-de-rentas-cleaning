package com.data.cleaning.main.hoolbam;

import java.io.BufferedWriter;
import java.io.IOException;

import com.data.cleaning.main.BaseParser;
import com.data.cleaning.main.Commons;

public class HoolBam08ContratoMutuoInteres extends BaseParser {
	
	public String getTipoContrato() {
		return "Contrato de Mutuo con interés";
	}
	
	public String getProyecto() {
		return "Hool Balam";
	}
	
	public String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/hool-bam-contrato-mutuo-interes/";
	}
	
	public String getFieldsTitle() {
		return "Otorgamiento|Otorgamiento Num|Moneda|Plazo|Plazo Num|Interes|Interes Num|Cantidad con interes|Cantidad con Num|Clausula Sexta|Plazo|Derechos";
	}

	public static void main(String[] args) {
		HoolBam08ContratoMutuoInteres parser = new HoolBam08ContratoMutuoInteres();
		parser.process();
	}

	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {
		String otorgamiento         = Commons.extract(content, "la cantidad de", ")", "PRIMERA.");
		String otorgamientoNum      = Commons.numericValue(otorgamiento);
		String moneda               = Commons.extractMoneda(otorgamiento);

		String plazo                = Commons.extract(content, "en un plazo", ",", "TERCERA");
		String interes              = Commons.extract(content, "recibirá un", "sobre", "CUARTA");

		String cantidadIntereses    = Commons.extract(content, "de", "por", "PRINCIPAL");
		if(cantidadIntereses.indexOf("QUINTA") > 0)
			cantidadIntereses = cantidadIntereses.substring(0, cantidadIntereses.indexOf("QUINTA"));

		if(cantidadIntereses.indexOf(" o ") > 0)
			cantidadIntereses = cantidadIntereses.substring(0, cantidadIntereses.indexOf(" o "));

		String salida               = Commons.extract(content, "SEXTA", "En", "SEXTA");
		if(salida.indexOf("Al") > 0)
			salida = salida.substring(0, salida.indexOf("Al"));

		if(salida.indexOf("Las") > 0)
			salida = salida.substring(0, salida.indexOf("Las"));

		String plazoSalida          = Commons.extract(content, "plazo de", "contados", "SEXTA");

		String derechos             = Commons.extract(content, "correspondiente de", ")", "SEXTA");
		if(derechos.length()> 0)
			derechos = derechos + ")";

		csvWriter.write("|");

		csvWriter.write(
				String.join("|",
						revisionManual, 

						Commons.toSingleLine(otorgamiento),
						Commons.toSingleLine(otorgamientoNum),
						Commons.toSingleLine(moneda),

						Commons.toSingleLine(plazo),
						Commons.toSingleLine(Commons.numericValue(plazo)),
						
						Commons.toSingleLine(interes),
						Commons.toSingleLine(Commons.numericValue(interes)),

						Commons.toSingleLine(cantidadIntereses),
						Commons.toSingleLine(Commons.numericValue(cantidadIntereses)),

						Commons.toSingleLine(salida),
						Commons.toSingleLine(plazoSalida),

						Commons.toSingleLine(derechos)));

	}
}