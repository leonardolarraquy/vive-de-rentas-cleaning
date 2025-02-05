package com.data.cleaning.main.livinguniversidad;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.data.cleaning.main.Commons;

public class ParserLivingUniversidadConvenioAdhesion {

	public static String getTipoContrato() {
		return "Convenio de adhesión";
	}

	public static String getProyecto() {
		return "Living Universidad";
	}

	public static String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/living-universidad-convenio-adhesion/";
	}

	public static void main(String[] args) {
		String folderPath = getFolderPath();

		String[] rutas = Commons.readLines(folderPath + "_links");

		String csvOutputPath = folderPath + "/output.csv";

		int i = 0;

		// Crear el archivo CSV de salida
		try (BufferedWriter csvWriter = new BufferedWriter(new FileWriter(csvOutputPath))) {        	
			// Escribir encabezados en el archivo CSV

			csvWriter.write("Tipo Contrato|Nombre Proyecto|Nombre del archivo|Link a archivo|Revision Manual|Tags|ENAJENANTE|ADHERENTE|Clave Unica|CURP|Clave RFC|RFC|Nacionalidad|Estado Civil|Mail 1|Mail 2|Mail 3|Beneficiario|Fecha Contrato|Fecha Contrato Num|Contrato|Numero Contrato|Serie|Ubicacion|Contraprestacion|Contraprestacion Num|Moneda|Participacion|Participacion Num|Domicilio Adherente|Sustituto \n");

			// Obtener todos los archivos .txt en la carpeta
			File[] txtFiles =  Commons.getFiles(folderPath);

			for (File txtFile : txtFiles) {
				System.out.println("processing: " + txtFile.getName());

				String ruta               = rutas[i];
				i++;

				String content = Files.readString(txtFile.toPath());

				String adherente            = extractAdherente(content);
				String promitenteEnajenante = Commons.extractPromitenteEnajenante(content);

				String tags                 = Commons.tags(content);

				String revisionManual = "";

				String CURP                 = Commons.getCURP(content);
				String CURPLimpio           = Commons.getCURPLimpio(CURP);

				String RFC                  = Commons.getRFC(content, "inscrito");
				String RFCLimpio            = Commons.getRFCLimpio(RFC);



				String tipoContrato         = Commons.extract(content, "AL CONTRATO", "IDENTIFICADO").replaceAll("AL CONTRATO DE", "");
				String numeroContrato       = Commons.extract(content, "CON EL", "DENOMINADO");

				String serie                = Commons.extract(content, "denominada", ",", "TERCERO").replaceAll("denominada", "");
				String ubicacion            = Commons.extract(content, "ubicado", "el cual", "TERCERO").replaceAll("ubicado en", "");

				String contraprestacion     = Commons.extract(content, "la cantidad", ")", "SEGUNDA");
				if(contraprestacion.length() > 0)
					contraprestacion += ")";

				String contraprestacionNum  = Commons.numericValue(contraprestacion);
				String moneda               = Commons.extractMoneda(contraprestacion);


				String participacion        = Commons.extract(content, "corresponden", "relacionados", "TERCERA").replaceAll("corresponden al", "");

				String domicilioAdherente   = Commons.extract(content, "FIDEICOMITENTE ADHERENTE", ".", "NOVENA").replaceAll("FIDEICOMITENTE ADHERENTE:", "");

				String sustituto            = Commons.extract(content, "en este acto designa", "para").replaceAll("en este acto designa", "");


				String fechaContrato        = Commons.extract(content, "los ", ".", "que fue el presente");
				if(fechaContrato.indexOf("EL") > 0)
					fechaContrato = fechaContrato.substring(0, fechaContrato.indexOf("EL"));

				if(fechaContrato.length() == 0)
					fechaContrato = extractFechaContrato(content);

				if(fechaContrato.indexOf(".") > 0)
					fechaContrato = fechaContrato.substring(0, fechaContrato.indexOf("."));

				String fechaContratoNum     = Commons.convertirFecha(fechaContrato);
				if(fechaContratoNum.length() == 0)
					revisionManual = revisionManual + "Fecha Contrato.";					

				// Escribir una fila en el archivo CSV
				csvWriter.write(String.join("|",
						Commons.toSingleLine(getTipoContrato()),
						Commons.toSingleLine(getProyecto()),
						Commons.toSingleLine(txtFile.getName().replaceAll(".txt", ".pdf")),
						Commons.toSingleLine(ruta),
						revisionManual,
						Commons.toSingleLine(tags),

						Commons.toSingleLine(promitenteEnajenante),						
						Commons.toSingleLine(adherente),

						Commons.toSingleLine(CURP),
						Commons.toSingleLine(CURPLimpio),
						Commons.toSingleLine(RFC),
						Commons.toSingleLine(RFCLimpio),

						Commons.toSingleLine(Commons.extraerNacionalidad(content)),
						Commons.toSingleLine(extraerEstadoCivil(content)),
						Commons.toSingleLine(Commons.extraerCorreosUnicos(content)),

						Commons.toSingleLine(""), // beneficiario
						Commons.toSingleLine(fechaContrato),
						Commons.toSingleLine(fechaContratoNum),


						Commons.toSingleLine(tipoContrato),
						Commons.toSingleLine(numeroContrato),
						Commons.toSingleLine(serie),
						Commons.toSingleLine(ubicacion),

						Commons.toSingleLine(contraprestacion),
						Commons.toSingleLine(contraprestacionNum),
						Commons.toSingleLine(moneda),

						Commons.toSingleLine(participacion),
						Commons.toSingleLine(Commons.numericValue(participacion)),
						
						Commons.toSingleLine(domicilioAdherente),
						Commons.toSingleLine(sustituto)

						) + "\n");
			}

			System.out.println("Archivo CSV generado en: " + csvOutputPath);

		} catch (IOException e) {
			System.err.println("Ocurrió un error al procesar los archivos: " + e.getMessage());
		}
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

	public static String extractAdherente(String content) {
		String adherente = Commons.extract(content, "ADHERENTE", "(").replaceAll("EL SEÑOR", "").replaceAll("EL SENOR", "").replaceAll("LA SENORA", "").replaceAll("LA SEÑORA", "").replaceAll("LA SEÑORITA", "").replaceAll("ADHERENTE, ", "");

		if(adherente.indexOf(".") > 0)
			adherente = adherente.substring(0, adherente.indexOf("."));	

		if(adherente.indexOf(";") > 0)
			adherente = adherente.substring(0, adherente.indexOf(";"));	

		if(adherente.indexOf(",") > 0)
			adherente = adherente.substring(0, adherente.indexOf(","));	

		return adherente;
	}

	public static String extraerEstadoCivil(String contenidoArchivo) {
		if (contenidoArchivo == null || contenidoArchivo.isEmpty()) {
			return "";
		}

		List<String> lineas = Arrays.asList(contenidoArchivo.split("\\r?\\n"));
		Set<String> estadosCiviles = new HashSet<>();
		Pattern patronEstadoCivil = Pattern.compile("(soltero|soltera|casado|casada).*?(?=[,.;])");
		// Mejorada para capturar variaciones y régimen matrimonial

		for (String linea : lineas) {
			Matcher matcher = patronEstadoCivil.matcher(linea);
			if (matcher.find()) {
				String estadoCivil = matcher.group();
				estadosCiviles.add(estadoCivil.trim());
			}
		}
		return String.join("-", estadosCiviles);
	}
}