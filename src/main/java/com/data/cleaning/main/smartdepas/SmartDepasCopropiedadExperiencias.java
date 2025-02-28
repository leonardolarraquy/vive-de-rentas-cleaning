package com.data.cleaning.main.smartdepas;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.data.cleaning.main.BaseParser;
import com.data.cleaning.main.Commons;

public class SmartDepasCopropiedadExperiencias extends BaseParser{

	public String getTipoContrato() {
		return "Copropiedad Experiencias";
	}

	public String getProyecto() {
		return "Smart Depas Tulum";
	}

	public String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/smart-depas-experiencias/";
	}

	public String getFieldsTitle() {
//		return "Unidad|Unidad Abrev|Tipo Contrato|Participacion|Porcentaje Participacion|Monto|Monto Num|Moneda|Vigencia|Redimiento Garantizado|Redimiento Garantizado Num|A partir de|Plazo rendimientos garantizados|Plazo rendimientos garantizados Num|Monto Rendimiento Mensual|Rendimiento Mensual Num|Rendimiento Mensual Moneda|Carta Garantia|Vigilancia Administracion|Equity Instantaneo";
		return "UNIDAD|TIPO CONTRATO|PORC_PROPIEDAD|MONTO_INVERSION|MONEDA|VIGENCIA_CONTRATO|RENDIMIENTO_GARANTIZADO|TASA_DE_INTERES_ANUAL|FECHA_COMIENZO_RENDIMIENTOS|NR_MENSUALIDADES|CUOTA_MENSUAL|RENDIMIENTO_MENSUAL_MONEDA|CARTA_GARANTIA|ADMINISTRADOR_VIGILANTE|EQUITY_INSTANTANEO";
	}

	public static void main(String[] args) {
		SmartDepasCopropiedadExperiencias parser = new SmartDepasCopropiedadExperiencias();
		parser.process();
	}

	@Override
	public String getDireccionAdquirente(String content) {
		String direccion = Commons.extract(content, "domicilio en", " y");

		if(direccion.length() > 0)
			direccion = direccion.replaceAll("domicilio en", "");

		if(direccion.startsWith(": "))
			direccion = direccion.substring(2, direccion.length());

		if(direccion.endsWith(","))
			direccion = direccion.substring(0, direccion.length() - 1);

		return direccion;
	}

	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {
		String unidad               = extractDepartamento(content);
		String propiedad            = extractPropiedad(content);
		String participacion        = extractParticipacion(content);
		String participacionPorc    = extraerPorcentaje(participacion);

		String inversion            = extractMontoInversion(content);
		String inversionNum         = Commons.numericValue(inversion);

		if(inversionNum.length() == 0)
			revisionManual = revisionManual + "Monto inversion.";

		String moneda               = Commons.extractMoneda(inversion);

		String vigencia             = extractVigencia(content);
		if(vigencia.length() == 0)
			revisionManual = revisionManual + "Vigencia.";

		String rendimientoGarant    = extractRendimientoGarantizado(content);
		String aPartirDe            = extraerAPartirDe(content);

		String cantidadCuotas       = extractCantidadCuotas(content);
		String rendimientoMensual   = extractMensualidad(content);

		String cartaGarantia        = extractCartaGarantia(content);
		if(cartaGarantia.length() == 0) {
			revisionManual = revisionManual + "Carta Garantia.";
			cartaGarantia = "NO";
		}
		else cartaGarantia = "SI";

		String vigilancia           = extractVigilancia(content);

		String equityInstantaneo    = extractEquity(content);
		if(equityInstantaneo.length() == 0)
			revisionManual = revisionManual + "Equity.";

		csvWriter.write("|");

		csvWriter.write(
				String.join("|",
						revisionManual, 
//						Commons.toSingleLine(unidad),
						Commons.toSingleLine(getUnidadAbrev(unidad)),

						Commons.toSingleLine(propiedad),
						
//						Commons.toSingleLine(participacion),
						Commons.toSingleLine(participacionPorc),

//						Commons.toSingleLine(inversion),
						Commons.toSingleLine(inversionNum),
						Commons.toSingleLine(moneda),

						Commons.toSingleLine(vigencia),

						Commons.toSingleLine("SI"),
						Commons.toSingleLine(rendimientoGarant.substring(0, 4)),
						
						Commons.toSingleLine(aPartirDe),

//						Commons.toSingleLine(cantidadCuotas),
						Commons.toSingleLine(extraerNumeroCuotas(cantidadCuotas)),

//						Commons.toSingleLine(rendimientoMensual),
						Commons.toSingleLine(Commons.numericValue(rendimientoMensual)),
						Commons.toSingleLine(Commons.extractMoneda(rendimientoMensual)),

						Commons.toSingleLine(cartaGarantia),
						Commons.toSingleLine(vigilancia),

						Commons.toSingleLine(equityInstantaneo)));

	}

	private static String getUnidadAbrev(String content) {
		String res = "";

		if(content.indexOf("D-301") > 0)
			res = "D-301";

		if(content.indexOf("C-301") > 0) {
			if(res.length() == 0)
				res = "C-301";
			else
				res = res + ",C-301";
		}

		if(content.indexOf("C-302") > 0) {
			if(res.length() == 0)
				res = "C-302";
			else
				res = res + ",C-302";
		}

		if(content.indexOf("D-303") > 0) {
			if(res.length() == 0)
				res = "D-303";
			else
				res = res + ",D-303";
		}

		return res;
	}

	public static String extraerAPartirDe(String texto) {
		try {

			int index  = texto.indexOf("a partir de");
			int index2 = texto.indexOf(".", index);

			return texto.substring(index, index2);
		}
		catch(Exception e) {}

		return "";
	}

	public static String extraerNumeroCuotas(String texto) {
		String regex = "\\b\\d+\\b";

		// Compilar la expresión regular:
		Pattern pattern = Pattern.compile(regex);

		// Crear un Matcher:
		Matcher matcher = pattern.matcher(texto);

		// Buscar coincidencias:
		if (matcher.find()) 
			// Extraer el número y agregar el símbolo de porcentaje:
			return matcher.group();

		return "";
	}

	public static String extraerPorcentaje(String texto) {
		// Expresión regular:
		String regex = "\\((\\d+)\\)\\s*%";

		// Compilar la expresión regular:
		Pattern pattern = Pattern.compile(regex);

		// Crear un Matcher:
		Matcher matcher = pattern.matcher(texto);

		// Buscar coincidencias:
		if (matcher.find()) {
			// Extraer el número y agregar el símbolo de porcentaje:
			return matcher.group(1) + "%";
		} else {
			return null; // O lanzar una excepción, según tu necesidad.
		}
	}

	@Override
	public String fechaContrato(String texto) {
		try {

			int index  = texto.indexOf("por duplicado");
			int index2 = texto.indexOf(".", index);

			return texto.substring(index + 43, index2);
		}
		catch(Exception e) {}

		return "";
	}

	public static String extractEquity(String texto) {
		try {

			int a = texto.indexOf("equity");
			if(a < 0)
				return "";

			int b = texto.indexOf("reconocido en", a);
			if(b < 0)
				return "";

			int index = texto.indexOf("un ", b);
			int index2 = texto.indexOf("adicionales", index);

			return texto.substring(index + 3, index2);
		}
		catch(Exception e) {}

		return "";
	}

	public static String extractVigilancia(String content) {
		try {
			int index = content.indexOf("al despacho", content.indexOf("designan como persona encargada"));
			int index2 = content.indexOf("quien", index);

			return content.substring(index, index2).replaceAll("“", "").replaceAll("”", "");

		}
		catch(Exception e) {}

		return "";
	}

	public static String extractVigencia(String content) {
		try {
			int index = content.indexOf("una vigencia", content.indexOf("PRIMERA. "));
			int index2 = content.indexOf("contados", index);

			return content.substring(index, index2);

		}
		catch(Exception e) {}

		return "";
	}

	public static String extractCartaGarantia(String content) {
		try {
			int a = content.indexOf("Adicionalmente,");
			if(a == -1)
				return "";

			int index = content.indexOf("mediante", a);
			if(index == -1)
				return "";

			int index2 = content.indexOf("al presente", index);//buscar la coma despues de la coma del monto

			int index3 = content.indexOf(",", index);
			if(index3 != -1 && index3 < index2)
				index2 = index3;

			return content.substring(index, index2).replaceAll("“", "").replaceAll("”", "");

		}
		catch(Exception e) {}

		return "";
	}

	public static String extractMensualidad(String content) {
		try {
			int index = content.indexOf("de", content.indexOf("pagaderas cada una") + 10);
			int index2 = content.indexOf(",", index + 40);//buscar la coma despues de la coma del monto

			return content.substring(index, index2);

		}
		catch(Exception e) {}

		return "";
	}

	public static String extractCantidadCuotas(String content) {
		try {
			int index = content.indexOf("durante", content.indexOf("el pago de rentas garantizadas"));
			int index2 = content.indexOf(",", index);//buscar la coma despues de la coma del monto

			return content.substring(index, index2);

		}
		catch(Exception e) {

		}

		return "";
	}

	public static String extractRendimientoGarantizado(String content) {
		try {
			int index = content.indexOf("%", content.indexOf("Derivado del porcentaje descrito"));
			int index2 = content.indexOf("equivalente", index);//buscar la coma despues de la coma del monto

			return content.substring(index - 3, index2);

		}
		catch(Exception e) {

		}

		return "";
	}

	public static String extractMontoInversion(String content) {
		try {
			int index = content.indexOf("de capital por un monto", content.indexOf("PRIMERA. "));
			int index2 = content.indexOf(",", index + 40);//buscar la coma despues de la coma del monto

			String res = content.substring(index + 10, index2);
			if(res.length() > 100)
				return "";
			
			return res;
		}
		catch(Exception e) {
		}

		return "";
	}

	public static String extractPropiedad(String content) {
		try {
			int index = content.indexOf("es su voluntad celebrar el presente contrato de");
			int index2 = content.indexOf(",", index);

			return content.substring(index + 35, index2);

		}
		catch(Exception e) {

		}

		return "";
	}

	public static String extractParticipacion(String content) {
		try {
			int index = content.indexOf("participación equivalente", content.indexOf("PRIMERA. "));
			int index2 = content.indexOf("sobre", index);

			return content.substring(index, index2);

		}
		catch(Exception e) {

		}

		return "";
	}

	public static String extractDepartamento(String content) {
		try {
			int index = content.indexOf("UNIDAD DE PROPIEDAD EXCLUSIVA");
			int index2 = content.indexOf("UBICADO", index);

			return content.substring(index, index2 - 2);

		}
		catch(Exception e) {

		}

		return "";
	}

	@Override
	public String getAdquiriente(String content) {
		try {
			int index = content.indexOf("C.");
			int index2 = content.indexOf(",");

			return content.substring(index + 2, index2);

		}
		catch(Exception e) {

		}

		return "";
	}
}