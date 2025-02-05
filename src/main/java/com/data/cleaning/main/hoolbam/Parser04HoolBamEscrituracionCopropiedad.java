package com.data.cleaning.main.hoolbam;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.data.cleaning.main.Commons;

public class Parser04HoolBamEscrituracionCopropiedad {

	public static String getTipoContrato() {
		return "Promesa compraventa-Escrituración en copropiedad";
	}
	
	public static String getProyecto() {
		return "Hool Balam";
	}
	
	public static String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/hool-bam-promesa-escrituracion-copropiedad/";
	}

	public static void main(String[] args) {
		String folderPath = getFolderPath();

		String[] rutas = Commons.readLines(folderPath + "_links");

		String csvOutputPath = folderPath + "/output.csv";

		int i = 0;

		// Crear el archivo CSV de salida
		try (BufferedWriter csvWriter = new BufferedWriter(new FileWriter(csvOutputPath))) {        	
			// Escribir encabezados en el archivo CSV
			csvWriter.write("Tipo Contrato|Nombre Proyecto|Nombre del archivo|Link a archivo|Revision Manual|Tags|ENAJENANTE|ADQUIRENTE|Clave Unica|CURP|Clave RFC|RFC|Nacionalidad|Estado Civil|Mail 1|Mail 2|Mail 3|Beneficiario|Fecha Contrato|Fecha Contrato Num|Porcentaje de Copropiedad|Porcentaje de Copropiedad Num|Unidad|Unidad Num|Proposito|Derechos|Contraprestacion|Contraprestacion Num|Moneda|Apartado|Apartado Num|Valor restante|Valor restante Num|Forma de Pago|Plazo|Direccion Adquirente\n");

			// Obtener todos los archivos .txt en la carpeta
			File[] txtFiles =  Commons.getFiles(folderPath);

			for (File txtFile : txtFiles) {
				System.out.println("processing: " + txtFile.getName());

				String ruta               = rutas[i];
				i++;

				String content = Files.readString(txtFile.toPath());

				String promitenteAdquirente = Commons.extractPromitenteAdquiriente(content);
				String promitenteEnajenante = Commons.extractPromitenteEnajenante(content);

				if(promitenteAdquirente.length() == 0) {
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
				
				String revisionManual = "";

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
				
				String porcentaje           = Commons.extract(content, "porcentaje correspondiente", "de");
				String porcentajeNum        = extractParteDecimal(porcentaje);
				if(porcentajeNum.length() == 0)
					revisionManual = revisionManual + "Porcentaje.";
				else {
					porcentajeNum = porcentajeNum + "%";
				}

				String unidad               = Commons.extract(content, "Unidad Inmobiliaria:", ".");
				unidad = unidad.replaceAll("Unidad Inmobiliaria:", "");
				if(unidad.length() > 24)
					unidad = unidad.substring(0,24);

				String unidadSimple         = extractUnidad(unidad);
				
				String proposito            = Commons.extract(content, "propósitos", "pero", "OBJETO");
				String derecho              = Commons.extract(content, "derecho", ".", "OBJETO");
				
				String contraprestacion     = extractContraprestacion(content);
				String contraprestacionNum  = Commons.numericValue(contraprestacion);
				String moneda               = Commons.extractMoneda(contraprestacion);
				if(contraprestacionNum.length() == 0)
					revisionManual = revisionManual + "Contraprestacion.";

				String apartado             = Commons.extract(content, "la cantidad", ".", "entregó al");
				String montoRestante        = Commons.extract(content, "la cantidad", ".", "se obliga");

				String plazo                = Commons.extract(content, "plazo", "en ", "SÉPTIMA");

				String domicilioAdquirente  = Parser01HoolBamFractional.extractDomicilioAdquiriente(content);
				if(content.indexOf("MUTUARIO") > 0)
					domicilioAdquirente = "";

				if(domicilioAdquirente.indexOf("/") > 0)
					domicilioAdquirente = domicilioAdquirente.substring(0, domicilioAdquirente.indexOf("/"));

				String beneficiario       = Commons.extract(content, "a ", ",", "DÉCIMA CUARTA. BENEFICIARIO");
				if(beneficiario.length() > 0)
					beneficiario = beneficiario.substring(2, beneficiario.length());

				String fechaContrato        = Commons.extract(content, "de México a los", ".", "LEGISLACIÓN APLICABLE").replaceAll("de México a ", "");
				if(fechaContrato.indexOf("EL") > 0)
					fechaContrato = fechaContrato.substring(0, fechaContrato.indexOf("EL"));

				if(fechaContrato.length() == 0)
					fechaContrato = extractFechaContrato(content);
				
				String fechaContratoNum     = Commons.convertirFecha(fechaContrato);

				String formaDePago          = Commons.extract(content, "Forma de Pago:", "EL", "ANEXO");
				if(formaDePago.indexOf(".") > 0)
					formaDePago = formaDePago.substring(0, formaDePago.indexOf("."));
				
				// Escribir una fila en el archivo CSV
				csvWriter.write(String.join("|",
						Commons.toSingleLine(getTipoContrato()),
						Commons.toSingleLine(getProyecto()),
						Commons.toSingleLine(txtFile.getName().replaceAll(".txt", ".pdf")),
						Commons.toSingleLine(ruta),
						revisionManual,
						Commons.toSingleLine(tags),
						
						Commons.toSingleLine(promitenteEnajenante),
						Commons.toSingleLine(promitenteAdquirente),
						
						Commons.toSingleLine(CURP),
						Commons.toSingleLine(CURPLimpio),
						Commons.toSingleLine(RFC),
						Commons.toSingleLine(RFCLimpio),
						
						Commons.toSingleLine(Commons.extraerNacionalidad(content)),
						Commons.toSingleLine(Commons.extraerEstadoCivil(content)),
						Commons.toSingleLine(Commons.extraerCorreosUnicos(content)),

						Commons.toSingleLine(beneficiario),
						Commons.toSingleLine(fechaContrato),
						Commons.toSingleLine(fechaContratoNum),

						Commons.toSingleLine(porcentaje),
						Commons.toSingleLine(porcentajeNum),

						Commons.toSingleLine(unidad),
						Commons.toSingleLine(unidadSimple),
						
						Commons.toSingleLine(proposito),
						Commons.toSingleLine(derecho),

						Commons.toSingleLine(contraprestacion),
						Commons.toSingleLine(contraprestacionNum),
						Commons.toSingleLine(moneda),
						
						Commons.toSingleLine(apartado),
						Commons.toSingleLine(Commons.numericValue(apartado)),
						Commons.toSingleLine(montoRestante),
						Commons.toSingleLine(Commons.numericValue(montoRestante)),

						Commons.toSingleLine(formaDePago),

						Commons.toSingleLine(plazo),

						Commons.toSingleLine(domicilioAdquirente)

						) + "\n");
			}

			System.out.println("Archivo CSV generado en: " + csvOutputPath);

		} catch (IOException e) {
			System.err.println("Ocurrió un error al procesar los archivos: " + e.getMessage());
		}
	}

	public static String extractUnidad(String texto) {
		// Expresión regular mejorada
		String regex = "(?:Unidad|Departamento) número:\\s*([A-Z]*\\-?[0-9]+[A-Z]*)";

		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(texto);

		if (matcher.find()) 
			return matcher.group(1).trim();

		return "";
	}

	public static String extractParteDecimal(String content) {
		String regex = "\\s+([0-9]+(?:\\.[0-9]+)?)%";

		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(content);

		return matcher.find() ? matcher.group(1) : "";
	}

	public static String extractContraprestacion(String content) {
		try {
			int contraprestacion = content.indexOf("CONTRAPRESTACI");
			if(contraprestacion == -1)
				return "";

			int index = content.indexOf("cantidad", contraprestacion);
			int index2 = content.indexOf(")", index + 30);//buscar la coma despues de la coma del monto

			return content.substring(index, index2 + 2);

		}
		catch(Exception e) {

		}

		return "";
	}

	public static String extractFechaContrato(String texto) {
		try {

			int index  = texto.indexOf("día", texto.indexOf("lo firman de conformidad"));
			int index2 = texto.indexOf("E", index);

			return texto.substring(index - 4, index2);
		}
		catch(Exception e) {}

		return "";
	}
}