package com.data.cleaning.main.vivastorage.naucalpan;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.data.cleaning.main.BaseParser;
import com.data.cleaning.main.Commons;

public class ContratoCopropiedadEmpresarial extends BaseParser {

	public String getTipoContrato() {
		return "Copropiedad empresarial";
	}

	public String getProyecto() {
		return "Vive Storage Naucalpan";
	}

	public String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/viva-storage-naucalpan/";
	}

	public static void main(String[] args) {
		ContratoCopropiedadEmpresarial parser = new ContratoCopropiedadEmpresarial();
		parser.process();
	}

	public String getFieldsTitle() {
		return "Metraje|Inversion|Inversion Num|Vigencia|Rendimiento Bruto Min|Monto Equivalente|Monto Equivalente Num|Plazo|Mensualidad|Mensualidad Num|Carta Garantia|Derechos|Metros|Equity";
	}

	@Override
	public String getAdquiriente(String content) {
		return Commons.extract(content, "C. ", ",").replaceAll("C. ", "");
	}

	@Override
	public String getEnajenante(String content) {
		return Commons.extract(content, "LA OTRA", ",").replaceAll("LA OTRA", "");
	}

	@Override
	public String getBeneficiario(String content) {
		return Commons.extract(content, "su beneficiario al C.", ",").replaceAll("su beneficiario al C.", "");
	}
	
	@Override
	public String getDireccionAdquirente(String content) {
		String domicilioAdquirente  = Commons.extract(content, "domicilio en:", " y ").replaceAll("domicilio en:", "");
		 if (domicilioAdquirente.endsWith(",")) 
			 domicilioAdquirente = domicilioAdquirente.substring(0, domicilioAdquirente.length() - 1);
		 
		 return domicilioAdquirente;
	}


	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {

		String metraje              = Commons.extract(content, "sobre", ")", "PRIMERA.") + ")";
		String inversion            = Commons.extract(content, "un monto", ")", "PRIMERA.") + ")";

		String vigencia             = Commons.extract(content, "vigencia de", "contados", "El presente contrato tendrá");

		String rendimientoBrutoMin  = extractRendimientoMinBruto(content);
		String montoEquivalente     = Commons.extract(content, "cantidad de", ")", "anual equivalente") + ")";
		String plazo                = Commons.extract(content, "los primeros", ",", "anual equivalente");
		String mensualidad          = Commons.extract(content, "cantidad", ")", "con mes") + ")";

		String cartaGarantia        = Commons.extract(content, "Adicionalmente,", "anexa", "con mes") + ")";

		String derechos             = Commons.extract(content, "El ", ",", "DERECHOS Y OBLIGACIONES");

		String metros               = Commons.extract(content, "“COPROPIETARIO A”:", "cuadrados", "propiedad de la").replaceAll("“COPROPIETARIO A”:", "") + "cuadrados)";

		String equity               = Commons.extract(content, "equity", "del", "Al respecto");

		csvWriter.write("|");

		csvWriter.write(
				String.join("|",
						revisionManual, 

						Commons.toSingleLine(metraje),
						Commons.toSingleLine(inversion),
						Commons.toSingleLine(Commons.numericValue(inversion)),

						Commons.toSingleLine(vigencia),

						Commons.toSingleLine(rendimientoBrutoMin),
						Commons.toSingleLine(montoEquivalente),
						Commons.toSingleLine(Commons.numericValue(montoEquivalente)),
						Commons.toSingleLine(plazo),

						Commons.toSingleLine(mensualidad),
						Commons.toSingleLine(Commons.numericValue(mensualidad)),

						Commons.toSingleLine(cartaGarantia),
						Commons.toSingleLine(derechos),
						Commons.toSingleLine(metros),

						Commons.toSingleLine(equity)));

	}

	/*
	public static void main(String[] args) {
		String folderPath = getFolderPath();

		String[] rutas = Commons.readLines(folderPath + "_links");

		String csvOutputPath = folderPath + "/output.csv";

		int i = 0;

		// Crear el archivo CSV de salida
		try (BufferedWriter csvWriter = new BufferedWriter(new FileWriter(csvOutputPath))) {        	
			// Escribir encabezados en el archivo CSV
			csvWriter.write("Tipo Contrato|Nombre Proyecto|Nombre del archivo|Link a archivo|Revision Manual|Tags|Copropietario A|Copropietario B|Clave Unica|CURP|Clave RFC|RFC|Nacionalidad|Estado Civil|Mail 1|Mail 2|Mail 3|Domicilio|Metraje|Inversion|Inversion Num|Vigencia|Rendimiento Bruto Min|Monto Equivalente|Monto Equivalente Num|Plazo|Mensualidad|Mensualidad Num|Carta Garantia|Derechos|Metros|Beneficiario|Fecha Contrato|Fecha Contrato Num|Equity\n");

			// Obtener todos los archivos .txt en la carpeta
			File[] txtFiles =  Commons.getFiles(folderPath);

			for (File txtFile : txtFiles) {
				System.out.println("processing: " + txtFile.getName());

				String ruta               = rutas[i];
				i++;

				String content = Files.readString(txtFile.toPath());

				String copropietarioA = Commons.extract(content, "C. ", ",").replaceAll("C. ", "");
				String copropietarioB = Commons.extract(content, "LA OTRA", ",").replaceAll("LA OTRA", "");

				if(copropietarioA.length() == 0 || content.indexOf("ENAJENANTE") > 0) {
					csvWriter.write(String.join("|",
							Commons.toSingleLine(getTipoContrato()),
							Commons.toSingleLine(getProyecto()),
							Commons.toSingleLine(txtFile.getName().replaceAll(".txt", ".pdf")),
							ruta,
							"No legible OCR" + "\n"));

					continue;
				}

				String tags                 = Commons.tags(content);

				String CURP                 = Commons.getCURP(content);
				String CURPLimpio           = Commons.getCURPLimpio(CURP);

				String RFC                  = Commons.getRFC(content, "Contribuyentes");
				String RFCLimpio            = Commons.getRFCLimpio(RFC);

				String revisionManual = "NO";
				if(CURPLimpio.length() == 0 || RFCLimpio.length() == 0)
					revisionManual = "SI";

				String domicilioAdquirente  = Commons.extract(content, "domicilio en:", " y ").replaceAll("domicilio en:", "");
				 if (domicilioAdquirente.endsWith(",")) 
					 domicilioAdquirente = domicilioAdquirente.substring(0, domicilioAdquirente.length() - 1);

				String metraje              = Commons.extract(content, "sobre", ")", "PRIMERA.") + ")";
				String inversion            = Commons.extract(content, "un monto", ")", "PRIMERA.") + ")";

				String vigencia             = Commons.extract(content, "vigencia de", "contados", "El presente contrato tendrá");

				String rendimientoBrutoMin  = extractRendimientoMinBruto(content);
				String montoEquivalente     = Commons.extract(content, "cantidad de", ")", "anual equivalente") + ")";
				String plazo                = Commons.extract(content, "los primeros", ",", "anual equivalente");
				String mensualidad          = Commons.extract(content, "cantidad", ")", "con mes") + ")";

				String cartaGarantia        = Commons.extract(content, "Adicionalmente,", "anexa", "con mes") + ")";

				String derechos             = Commons.extract(content, "El ", ",", "DERECHOS Y OBLIGACIONES");

				String metros               = Commons.extract(content, "● “COPROPIETARIO A”:", "cuadrados").replaceAll("● “COPROPIETARIO A”:", "") + "cuadrados)";
				String beneficiario         = Commons.extract(content, "su beneficiario al C.", ",").replaceAll("su beneficiario al C.", "");

				String fechaContrato        = Commons.extract(content, "a los", ".", "LEGISLACIÓN APLICABLE");
				if(fechaContrato.length() == 0)
					fechaContrato = extractFechaContrato(content);

				if(fechaContrato.indexOf(".") > 0)
					fechaContrato = fechaContrato.substring(0, fechaContrato.indexOf("."));

				String fechaContratoNum     = Commons.convertirFecha(fechaContrato);

				String equity               = Commons.extract(content, "equity", "del", "Al respecto");

				// Escribir una fila en el archivo CSV
				csvWriter.write(String.join("|",
						Commons.toSingleLine(getTipoContrato()),
						Commons.toSingleLine(getProyecto()),
						Commons.toSingleLine(txtFile.getName().replaceAll(".txt", ".pdf")),
						Commons.toSingleLine(ruta),
						revisionManual,
						tags,

						Commons.toSingleLine(copropietarioB),
						Commons.toSingleLine(copropietarioA),

						Commons.toSingleLine(CURP),
						Commons.toSingleLine(CURPLimpio),
						Commons.toSingleLine(RFC),
						Commons.toSingleLine(RFCLimpio),

						Commons.toSingleLine(Commons.extraerNacionalidad(content)),
						Commons.toSingleLine(Commons.extraerEstadoCivil(content)),
						Commons.toSingleLine(Commons.extraerCorreosUnicos(content)),						

						Commons.toSingleLine(domicilioAdquirente),

						Commons.toSingleLine(metraje),
						Commons.toSingleLine(inversion),
						Commons.toSingleLine(Commons.numericValue(inversion)),

						Commons.toSingleLine(vigencia),

						Commons.toSingleLine(rendimientoBrutoMin),
						Commons.toSingleLine(montoEquivalente),
						Commons.toSingleLine(Commons.numericValue(montoEquivalente)),
						Commons.toSingleLine(plazo),

						Commons.toSingleLine(mensualidad),
						Commons.toSingleLine(Commons.numericValue(mensualidad)),

						Commons.toSingleLine(cartaGarantia),
						Commons.toSingleLine(derechos),
						Commons.toSingleLine(metros),
						Commons.toSingleLine(beneficiario),

						Commons.toSingleLine(fechaContrato),
						Commons.toSingleLine(fechaContratoNum),

						Commons.toSingleLine(equity)

						) + "\n");
			}

			System.out.println("Archivo CSV generado en: " + csvOutputPath);

		} catch (IOException e) {
			System.err.println("Ocurrió un error al procesar los archivos: " + e.getMessage());
		}
	}
	 */

	private static String extractRendimientoMinBruto(String content) {
		String regex = "●\\s*(.*?)\\s*anual"; // Sin ^

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);

		if (matcher.find()) 
			return matcher.group(1).trim();

		return "";
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
}