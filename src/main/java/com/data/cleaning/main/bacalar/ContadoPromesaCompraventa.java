package com.data.cleaning.main.bacalar;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.data.cleaning.main.BaseParser;
import com.data.cleaning.main.Commons;

public class ContadoPromesaCompraventa extends BaseParser {

	public String getTipoContrato() {
		return "Contado-Promesa de compraventa";
	}

	public String getProyecto() {
		return "Ecotown Bacalar";
	}

	public String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/bacalar-contado-promesa-compraventa/";
	}

	public String getFieldsTitle() {
//		return "UNIDAD|MANZANA|LOTE|FASE|MONTO CONTRAPRESTACION|MONTO NUM|MONEDA|MONTO APARTADO|MONTO APARTADO NUM|MONTO LIQUIDACION|MONTO LIQUIDACION NUM|MONTO RESTANTE A FINANCIAR|MONTO RESTANTE A FINANCIAR A NUM|TIEMPO PROMETIDO DE CONSTRUCCION (MESES)|PRORROGA HASTA ENTREGA";
		return "UNIDAD|MANZANA|LOTE|FASE|MONTO_INVERSION|MONEDA|MONTO_APARTADO|MONTO_LIQUIDACION|MONTO_A_FINANCIAR|TIEMPO_DE_ENTREGA|PRORROGA_DE_ENTREGA";
	}

	public static void main(String[] args) {
		ContadoPromesaCompraventa parser = new ContadoPromesaCompraventa();
		parser.process();
	}

	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {
		String unidad             = Commons.extractLote(content);
		String lote               = "";
		String manzana            = "";

		if(unidad.length() > 0) {
			lote = unidad.split("M")[0].replaceAll("No.", "").replaceAll(",", "");
			if(lote.indexOf("y") > 0)
				revisionManual     = "Unidad.";

			manzana            = unidad.split("M")[1].replaceAll("anzana", "").replaceAll("de la ", "").replaceAll("\\.", "");
		}
		else {
			revisionManual     = "Unidad.";
		}


		String fase               = Commons.extractFase(content);

		String montoInversion     = Commons.extractMonto(content);
		String montoInversionNum  = Commons.numericValue(montoInversion);
		if(montoInversionNum.length() == 0)
			revisionManual     = revisionManual + "Contraprestacion.";				

		String moneda             = Commons.extractMoneda(montoInversion);

		String montoApartado      = Commons.extract(content, "la cantidad", ".", "entregó al");
		if(montoApartado.length() == 0 || montoApartado.length() == 0)
			revisionManual     = revisionManual + "Monto Apartado.";

		String montoApartadoNum   = Commons.numericValue(montoApartado);

		String montoLiquidacion   = Commons.extractMontoLiquidacion(content);
		String montoLiquidacionNum= Commons.numericValue(montoLiquidacion);

		String montoaFinanciar    = extractRestanteAFinanciar(content);
		String montoaFinanciarNum = Commons.numericValue(montoaFinanciar);

		String posesion           = extractPlazo(content);
		String entrega            = extractPlazoEntrega(content);

		csvWriter.write("|");

		csvWriter.write(
				String.join("|",
						revisionManual, 
						Commons.toSingleLine(unidad),
						Commons.toSingleLine(manzana),
						Commons.toSingleLine(lote),

						Commons.toSingleLine(fase),

//						Commons.toSingleLine(montoInversion),
						Commons.toSingleLine(montoInversionNum),
						Commons.toSingleLine(moneda),

//						Commons.toSingleLine(montoApartado),
						Commons.toSingleLine(montoApartadoNum),

//						Commons.toSingleLine(montoLiquidacion),
						Commons.toSingleLine(montoLiquidacionNum),

//						Commons.toSingleLine(montoaFinanciar),
						Commons.toSingleLine(montoaFinanciarNum),

						Commons.toSingleLine(posesion),
						Commons.toSingleLine(entrega)));

	}

	public static String extractPlazo(String texto) {
		try {
			int index  = texto.indexOf("OCTAVA. ");
			int index2 = texto.indexOf(".", index + 60);

			String clausulaOctava = texto.substring(index, index2);

			String regexPlazo = "entre\\s+(\\d+) \\(([^)]+)\\)\\s+y\\s+(\\d+) \\(([^)]+)\\)"; // Expresión mejorada para el plazo
			Pattern patternPlazo = Pattern.compile(regexPlazo);
			Matcher matcherPlazo = patternPlazo.matcher(clausulaOctava);

			if (matcherPlazo.find()) 
				return matcherPlazo.group(0);
		}
		catch(Exception e) {
		}

		return "";
	}

	private static String extractRestanteAFinanciar(String content) {
		try {

			int index  = content.indexOf("SEGUNDA. ") + 10;
			int index2 = content.indexOf("TERCERA. ");

			int index3 = content.indexOf("C. ", index);
			if(index3 == -1) {
				index3 = content.indexOf("C ", index);

				if(index3 == -1) {
					index3 = content.indexOf("c. ", index);

					if(index3 == -1) 
						index3 = content.length();
				}
			}


			content = content.substring(index3, index2);

			index  = content.indexOf("cantidad");

			int max = content.indexOf(")");
			if(max == -1) {
				max = content.indexOf("]");

				if(max == -1) {
					max = content.indexOf("la cual");

					if(max == -1) 
						max = content.length();
				}
			}

			return content.substring(index, max) ;

		}
		catch(Exception e) {
		}

		return "";
	}

	private static String extractPlazoEntrega(String content) {
		Pattern pattern = Pattern.compile("prorrogarse por un plazo de hasta\\s+(\\d+) \\(([^)]+)\\) meses", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

		Matcher matcher = pattern.matcher(content);
		return matcher.find() ? matcher.group().trim() : "";
	}

	public String fechaContrato(String texto) {
		try {

			int index  = texto.indexOf("en dos tanto en el Estado de México");
			int index2 = texto.indexOf(".", index + 10);

			if((index2 - (index + 36)) > 40)
				index2 = texto.indexOf("EL", index + 10) - 1;

			return Commons.toSingleLine(texto.substring(index + 36, index2)).replaceAll("a los", "").replaceAll("al ", "").replaceAll("a ", "").replaceAll("días ", "").replaceAll("de ", "").replaceAll("del ", "").trim();
		}
		catch(Exception e) {
		}

		return "";
	}
}