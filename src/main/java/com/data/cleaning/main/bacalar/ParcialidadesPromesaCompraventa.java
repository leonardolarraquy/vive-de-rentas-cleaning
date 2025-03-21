package com.data.cleaning.main.bacalar;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.data.cleaning.main.BaseParser;
import com.data.cleaning.main.Commons;

public class ParcialidadesPromesaCompraventa extends BaseParser {

	public String getTipoContrato() {
		return "Parcialidades-Promesa de compraventa";
	}
	
	public String getProyecto() {
		return "Ecotown Bacalar";
	}
	
	public String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/bacalar-parcialidades-promesa-compra-venta/";
	}

	public String getFieldsTitle() {
//		return "Unidad|Manzana|Lote|Fase|Monto Contraprestacion|Monto Num|Moneda|Monto Apartado|Monto Apartado Num|Monto Liquidacion|Monto Liquidacion Num|Clausula Mensualidades|Nr Mensualidades|Nr Mensualidades Num|Cuota Mensual|Moneda Cuota Mensual|Primer Pago Cuota|Primer Pago Cuota Num|Tiempo prometido de construccion (meses)|Prorroga hasta entrega";
		return "UNIDAD|MANZANA|LOTE|FASE|MONTO_INVERSION|MONEDA|MONTO_APARTADO|MONTO_LIQUIDACION|NR_MENSUALIDADES|CUOTA_MENSUAL|MONEDA_CUOTA_MENSUAL|FECHA_1ER_PAGO_CUOTA|TIEMPO_DE_ENTREGA|PRORROGA_DE_ENTREGA";
	}

	public static void main(String[] args) {
		ParcialidadesPromesaCompraventa parser = new ParcialidadesPromesaCompraventa();
		parser.process();
	}

	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {
		String unidad             = Commons.extractLote(content);
		String lote               = "";
		String manzana            = "";

		if(unidad.length() > 30) {
			unidad             = "";
			revisionManual     = "Unidad.";
		}
		else {
			lote               = String.format("%02d", Integer.parseInt(unidad.split("M")[0].replaceAll("No.", "").replaceAll(",", "").replaceAll(" ", "").replaceAll("\\.", "") ));
			manzana            = String.format("%02d", Integer.parseInt(unidad.split("M")[1].replaceAll("anzana", "").replaceAll(",", "").replaceAll("de la ", "").replaceAll("\\.", "").replaceAll(" ", "") ));
		}

		String fase               = Commons.extractFase(content);

		String montoInversion     = Commons.extractMonto(content);
		if(montoInversion.length() == 0 || montoInversion.length() == 0)
			revisionManual     = revisionManual + "Contraprestacion.";

		String montoInversionNum  = Commons.numericValue(montoInversion);
		String moneda             = Commons.extractMoneda(montoInversion);

		String montoApartado      = Commons.extract(content, "la cantidad", ".", "entregó al");
		if(montoApartado.length() == 0 || montoApartado.length() == 0)
			revisionManual     = revisionManual + "Monto Apartado.";

		String montoApartadoNum   = Commons.numericValue(montoApartado);

		String montoLiquidacion   = Commons.extractMontoLiquidacion(content);
		String montoLiquidacionNum= Commons.numericValue(montoLiquidacion);

		String clausulaC          = Commons.extract(content, "realizar el pago de", "debiendo", "SEGUNDA.");
		String mensualidades      = extractMensualidades(clausulaC);
		
		if(clausulaC.length() == 0 || mensualidades.length() == 0)
			revisionManual     = revisionManual + "Mensualidades.";

		String parteMonetaria     = clausulaC.indexOf("$") > 0 ? clausulaC.substring(clausulaC.indexOf("$")) : "";
		String countaMensual      = Commons.numericValue(parteMonetaria);
						
		String fechaContrato      = Commons.toSingleLine(fechaContrato(content));
		String fechaContratoNum   = Commons.convertirFecha(fechaContrato);
		int    anoContrato        = 1970;
		
		if(fechaContrato.length() == 0 || fechaContratoNum.length() == 0) {
			revisionManual     = revisionManual + "Fecha Contrato.";
			fechaContrato      = "";
			fechaContratoNum   = "";
		}
		else {
			anoContrato = Integer.parseInt(fechaContratoNum.substring(fechaContratoNum.length() - 4));
		}

		String primerPago         = extractPrimerPago(content);
		String primerNum          = Commons.extraerFechaAPartirDeTexto(primerPago, anoContrato);

		String tipoPrometido      = Commons.extract(content, "entre", "contados", "llevarse a cabo");
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

//						Commons.toSingleLine(clausulaC),
//						Commons.toSingleLine(mensualidades),
						Commons.toSingleLine(Commons.numericValue(mensualidades)),
						
						Commons.toSingleLine(countaMensual),
						Commons.toSingleLine(Commons.extractMoneda(countaMensual)),

//						Commons.toSingleLine(primerPago),
						Commons.toSingleLine(primerNum),

						Commons.toSingleLine(tipoPrometido),
						Commons.toSingleLine(entrega)
				
				));

	}

	public static String extractMensualidades(String texto) {
		if(texto.length() == 0)
			return "";

		int index = texto.indexOf("(");
		if(index == -1)
			return "";

		return texto.substring(0, index);
	}

	public static String extractPrimerPago(String texto) {
		try {
			//esto puede estar en la clausula B o C
			int index = texto.indexOf("B. ", texto.indexOf("SEGUNDA. "));

			index  = texto.indexOf("realizando", index);
			if(index == -1)
				return "";

			int index2 = texto.indexOf("hasta", index);

			return texto.substring(index +10, index2);
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

			int index  = texto.indexOf("en dos tanto");
			if(index == -1)
				return "";
			
			int index2 = texto.indexOf(".", index + 10);
			if(index2 != -1)
				texto = texto.substring(index + 36, index2);

			index2 = texto.indexOf("EL");
			if(index2 != -1)
				texto = texto.substring(0, index2);

			index2 = texto.indexOf("“");
			if(index2 != -1)
				texto = texto.substring(0, index2);
			
			String res = texto.replaceAll("co ", "").replaceAll("a los", "").replaceAll("los", "").replaceAll("al ", "").replaceAll("a ", "").replaceAll("días ", "").replaceAll("de ", "").replaceAll("del ", "").trim();
			if(res.length() < 40)
				return res;

			String substr = texto.substring(index, texto.length());

			Pattern pattern = Pattern.compile("\\b(\\d{1,2})\\s+de\\s+(enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|octubre|noviembre|diciembre)\\s+del\\s+(\\d{4})\\b", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(substr);

			if(matcher.find())
				return matcher.group().replaceAll("mes", "").replaceAll("dias", "").replaceAll("el ", "");
		}
		catch(Exception e) {
		}

		return "";
	}

	public static String extractClausulaA(String texto) {
		if(texto.indexOf("MODIFICATORIO") > 0) 
			return "";

		try {

			int index2 = texto.indexOf("la cantidad de ", texto.indexOf("Previo a la fecha de firma del presente instrumento"));
			int index3 = texto.indexOf(".", texto.indexOf("por", index2));
			int index4 = texto.indexOf("en adelante", index2);
			int index5 = texto.indexOf("debiendo", index2);

			if(index3 == -1 || (index3 > index4 && index4 != -1))
				index3 = index4;

			if(index3 == -1 || (index3 > index5 && index5 != -1))
				index3 = index5;

			return texto.substring(index2 + 15, index3);
		}
		catch(Exception e) {
		}

		return "";
	}

	public static String extractClausulaB(String texto) {
		if(texto.indexOf("MODIFICATORIO") > 0) 
			return "";

		try {

			int index = texto.indexOf("B. ", texto.indexOf("SEGUNDA. "));
			if(index == -1)
				index = texto.indexOf("b. ", texto.indexOf("SEGUNDA. "));

			int index2 = texto.indexOf("$", index);

			int index3 = texto.indexOf("enganche", index) + 8;
			int index4 = texto.indexOf("debiendo", index);
			int index5 = texto.indexOf("en adelante", index);

			int index6 = texto.indexOf("C. ", index);
			if(index6 == -1)
				index6 = texto.indexOf("c. ", texto.indexOf("SEGUNDA. "));

			int index7 = texto.indexOf(" misma ", index);
			int index8 = texto.indexOf(".", index + 130);

			if(index3 == -1 || (index3 > index4 && index4 != -1))
				index3 = index4;

			if(index3 == -1 || (index3 > index5 && index5 != -1))
				index3 = index5;

			if(index3 == -1 || (index3 > index6 && index6 != -1))
				index3 = index6;

			if(index3 == -1 || (index3 > index7 && index7 != -1))
				index3 = index7;

			if(index3 < index2)
				index3 = index8;

			return texto.substring(index2, index3);
		}
		catch(Exception e) {
		}

		return "";
	}

}