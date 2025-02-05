package com.data.cleaning.main.hoolbam;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.data.cleaning.main.Commons;

public class Parser02HoolBamCompleto {

	public static String getTipoContrato() {
		return "Promesa compraventa-Derechos fideicomisarios-Completo";
	}
	
	public static String getProyecto() {
		return "Hool Balam";
	}
	
	public static String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/hool-bam-completo/";
	}

	public static void main(String[] args) {
		String folderPath = getFolderPath();

		String[] rutas = Commons.readLines(folderPath + "_links");

		String csvOutputPath = folderPath + "/output.csv";

		int i = 0;

		// Crear el archivo CSV de salida
		try (BufferedWriter csvWriter = new BufferedWriter(new FileWriter(csvOutputPath))) {        	
			// Escribir encabezados en el archivo CSV
			
			csvWriter.write("Tipo Contrato|Nombre Proyecto|Nombre del archivo|Link a archivo|Revision Manual|Tags|ENAJENANTE|ADQUIRENTE|Clave Unica|CURP|Clave RFC|RFC|Nacionalidad|Estado Civil|Mail 1|Mail 2|Mail 3|Beneficiario|Fecha Contrato|Fecha Contrato Num|Derechos|Derecho Num|Participacion|Participacion Num|Unidad|Unidad Abrev.|Derecho|Contraprestacion|Contraprestacion Num|Moneda|Consitucion|Devolucion|Entrega|Entrega Num|Plazo|Domicilio Adquirente\n");

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

				String porcDerechos         = Commons.extract(content, "correspondientes", ")") + ")";
				String porcDerechosNum      = extractParteDecimal(porcDerechos);

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

				String participacion        = Commons.extract(content, "participación equivalente", "(");
				String participacionNum     = extractParteDecimal(participacion);
				if(participacionNum.length() > 0) 
					revisionManual   = revisionManual + "Participacion.";

				String unidad               = Commons.extract(content, "Departamento número:", "\n");
				if(unidad.length() == 0)
					unidad                  = Commons.extract(content, "Unidad número:", "\n");
				
				String unidadSimple         = Commons.extraerUnidadAbrev(unidad);

				String derecho              = Commons.extract(content, "tendrá derecho", ".", "PRIMERA");
				if(derecho.indexOf(";") > 0)
					derecho = derecho.substring(0, derecho.indexOf(";"));

				String contraprestacion     = extractContraprestacion(content);
				String contraprestacionNum  = Commons.numericValue(contraprestacion);
				String moneda               = Commons.extractMoneda(contraprestacion);

				if(contraprestacionNum.length() == 0) {
					contraprestacion = "";
					revisionManual   = revisionManual + "Contraprestacion.";
					moneda           = "";
				}

				String constitucion         = Commons.extract(content, "La constitución", ",", "CUARTA");

				String devolucion           = Commons.extract(content, "devolverá", "naturales", "CUARTA");

				String entrega              = Commons.extract(content, "La entrega de", ".", "SÉPTIMA");
				if(entrega.indexOf("de acuerdo") > 0)
					entrega = entrega.substring(0, entrega.indexOf("de acuerdo"));

				if(entrega.indexOf(",", 30) > 0)
					entrega = entrega.substring(0, entrega.indexOf(",", 30));

				String entregaNum           = Commons.extraerFechaAPartirDeTexto(entrega);

				String plazo                = Commons.extract(content, "plazo", "en ", "ENTREGA DEL");

				String fechaContrato        = Commons.extract(content, "de México a los", ".", "LEGISLACIÓN APLICABLE").replaceAll("de México a ", "");
				if(fechaContrato.indexOf("EL") > 0)
					fechaContrato = fechaContrato.substring(0, fechaContrato.indexOf("EL"));

				if(fechaContrato.length() == 0)
					fechaContrato = extractFechaContrato(content);

				String fechaContratoNum     = Commons.convertirFecha(fechaContrato);

				String domicilioAdquirente  = Parser01HoolBamFractional.extractDomicilioAdquiriente(content);

				String beneficiario       = Commons.extract(content, "a ", "llevando", "BENEFICIARIO").replaceAll("la señora", "");
				if(beneficiario.length() > 0)
					beneficiario = beneficiario.substring(2, beneficiario.length());

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

						Commons.toSingleLine(porcDerechos),
						Commons.toSingleLine(porcDerechosNum),

						Commons.toSingleLine(participacion),
						Commons.toSingleLine(participacionNum),

						Commons.toSingleLine(unidad),
						Commons.toSingleLine(unidadSimple),
						
						Commons.toSingleLine(derecho),

						Commons.toSingleLine(contraprestacion),
						Commons.toSingleLine(contraprestacionNum),
						Commons.toSingleLine(moneda),

						Commons.toSingleLine(constitucion),
						Commons.toSingleLine(devolucion),

						Commons.toSingleLine(entrega),
						Commons.toSingleLine(entregaNum),

						Commons.toSingleLine(plazo),

						Commons.toSingleLine(domicilioAdquirente)

						) + "\n");
			}

			System.out.println("Archivo CSV generado en: " + csvOutputPath);

		} catch (IOException e) {
			System.err.println("Ocurrió un error al procesar los archivos: " + e.getMessage());
		}
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

	public static String extractDerecho(String content) {
		try {
			int index = content.indexOf("con el ", content.indexOf("deseo celebrar"));
			int index2 = content.indexOf("conforme", index) - 1;

			int index3 = content.indexOf(",", index);
			if(index3 != -1 && index3 < index2)
				index2 = index3;

			index3 = content.indexOf(".", index);
			if(index3 != -1 && index3 < index2)
				index2 = index3;

			return content.substring(index, index2);

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