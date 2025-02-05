package com.data.cleaning.main.smartdepas;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.data.cleaning.main.Commons;

public class ParserSmartDepasCopropiedadExperiencias {
	
	public static String getTipoContrato() {
		return "Copropiedad Experiencias";
	}
	
	public static String getProyecto() {
		return "Smart Depas Tulum";
	}
	
	public static String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/smart-depas-experiencias/";
	}


	public static void main(String[] args) {
		String folderPath = getFolderPath();

		String[] rutas = Commons.readLines(folderPath + "_links");

		String csvOutputPath = folderPath + "/output.csv";

		int i = 0;

		// Crear el archivo CSV de salida
		try (BufferedWriter csvWriter = new BufferedWriter(new FileWriter(csvOutputPath))) {        	
			// Escribir encabezados en el archivo CSV
			csvWriter.write("Tipo Contrato|Nombre Proyecto|Nombre del archivo|Link a archivo|Revision Manual|Tags|ENAJENANTE|ADQUIRENTE|Clave Unica|CURP|Clave RFC|RFC|Nacionalidad|Estado Civil|Mail 1|Mail 2|Mail 3|Unidad|Unidad Abrev|Tipo Contrato|Participacion|Porcentaje Participacion|Monto|Monto Num|Moneda|Vigencia|Redimiento Garantizado|Redimiento Garantizado Num|A partir de|Plazo rendimientos garantizados|Plazo rendimientos garantizados Num|Monto Rendimiento Mensual|Rendimiento Mensual Num|Rendimiento Mensual Moneda|Carta Garantia|Vigilancia Administracion|Beneficiario|Fecha Contrato|Fecha Numerica|Equity Instantaneo\n");

			File[] txtFiles =  Commons.getFiles(folderPath);

			for (File txtFile : txtFiles) {
				System.out.println("processing: " + txtFile.getName());

				String ruta               = rutas[i];
				i++;

				String content = Files.readString(txtFile.toPath());
				String promitenteEnajenante = Commons.extractPromitenteEnajenante(content);
				
				String revisionManual       = "";

				String tags                 = Commons.tags(content);

				String CURP                 = Commons.getCURP(content);
				String CURPLimpio           = Commons.getCURPLimpio(CURP);
				
				if(CURP.length() == 0 )
					revisionManual = revisionManual + "CURP.";
				else {
					if(CURPLimpio.length() != 18)
						revisionManual = revisionManual + "CURP Invalido.";
				}

				String RFC                  = Commons.getRFC(content, "inscrito");
				String RFCLimpio            = Commons.getRFCLimpio(RFC);
				
				if(RFC.length() == 0 )
					revisionManual = revisionManual + "RFC.";
				else {
					if(RFCLimpio.length() != 13 && RFCLimpio.length() != 12)
						revisionManual = revisionManual + "RFC Invalido.";					
				}

				
				String promitenteAdquirente = extractPromitenteAdquiriente(content);

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
				if(cartaGarantia.length() == 0)
					revisionManual = revisionManual + "Carta Garantia.";

				String vigilancia           = extractVigilancia(content);
				String beneficiario         = extractBeneficiario(content);

				String fechaContrato        = extractFechaContrato(content);
				String fechaContratoNum     = Commons.convertirFecha(fechaContrato);

				String equityInstantaneo    = extractEquity(content);
				if(equityInstantaneo.length() == 0)
					revisionManual = revisionManual + "Equity.";

				// Escribir una fila en el archivo CSV
				csvWriter.write(String.join("|",
						Commons.toSingleLine(getTipoContrato()),
						Commons.toSingleLine(getProyecto()),
						Commons.toSingleLine(txtFile.getName().replaceAll(".txt", ".pdf")),
						Commons.toSingleLine(ruta),
						revisionManual,
						tags,

						Commons.toSingleLine(promitenteEnajenante),
						Commons.toSingleLine(promitenteAdquirente),

						Commons.toSingleLine(CURP),
						Commons.toSingleLine(CURPLimpio),
						Commons.toSingleLine(RFC),
						Commons.toSingleLine(RFCLimpio),

						Commons.toSingleLine(Commons.extraerNacionalidad(content)),
						Commons.toSingleLine(Commons.extraerEstadoCivil(content)),
						Commons.toSingleLine(Commons.extraerCorreosUnicos(content)),

						Commons.toSingleLine(unidad),
						Commons.toSingleLine(getUnidadAbrev(unidad)),

						Commons.toSingleLine(propiedad),
						Commons.toSingleLine(participacion),
						Commons.toSingleLine(participacionPorc),

						Commons.toSingleLine(inversion),
						Commons.toSingleLine(inversionNum),
						Commons.toSingleLine(moneda),

						Commons.toSingleLine(vigencia),

						Commons.toSingleLine(rendimientoGarant),
						Commons.toSingleLine(rendimientoGarant.substring(0, 4)),
						Commons.toSingleLine(aPartirDe),

						Commons.toSingleLine(cantidadCuotas),
						Commons.toSingleLine(extraerNumeroCuotas(cantidadCuotas)),

						Commons.toSingleLine(rendimientoMensual),
						Commons.toSingleLine(Commons.numericValue(rendimientoMensual)),
						Commons.toSingleLine(Commons.extractMoneda(rendimientoMensual)),

						Commons.toSingleLine(cartaGarantia),
						Commons.toSingleLine(vigilancia),
						Commons.toSingleLine(beneficiario),

						Commons.toSingleLine(fechaContrato),
						Commons.toSingleLine(fechaContratoNum),

						Commons.toSingleLine(equityInstantaneo)

						) + "\n");
			}

			System.out.println("Archivo CSV generado en: " + csvOutputPath);

		} catch (IOException e) {
			System.err.println("Ocurrió un error al procesar los archivos: " + e.getMessage());
		}
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

	public static String extractFechaContrato(String texto) {
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

	public static String extractBeneficiario(String content) {
		try {
			int index = content.indexOf(" C.", content.indexOf("OCTAVA."));
			int index2 = content.indexOf("llevando", index);

			int index3 = content.indexOf(",", index);
			if(index3 != -1 && index3 < index2)
				index2 = index3;

			return content.substring(index + 3, index2);
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

			return content.substring(index + 10, index2);

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

	public static String extractPromitenteAdquiriente(String content) {
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