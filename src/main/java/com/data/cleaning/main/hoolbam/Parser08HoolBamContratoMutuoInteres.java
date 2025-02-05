package com.data.cleaning.main.hoolbam;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import com.data.cleaning.main.Commons;

public class Parser08HoolBamContratoMutuoInteres {
	
	public static String getTipoContrato() {
		return "Contrato de Mutuo con interés";
	}
	
	public static String getProyecto() {
		return "Hool Balam";
	}
	
	public static String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/hool-bam-contrato-mutuo-interes/";
	}


	public static void main(String[] args) {
		String folderPath = getFolderPath();

		String[] rutas = Commons.readLines(folderPath + "_links");

		String csvOutputPath = folderPath + "/output.csv";

		int i = 0;

		// Crear el archivo CSV de salida
		try (BufferedWriter csvWriter = new BufferedWriter(new FileWriter(csvOutputPath))) {        	
			// Escribir encabezados en el archivo CSV
			csvWriter.write("Tipo Contrato|Nombre Proyecto|Nombre del archivo|Link a archivo|Revision Manual|Tags|Mutuario|Mutuante|Clave Unica|CURP|Clave RFC|RFC|Nacionalidad|Estado Civil|Mail 1|Mail 2|Mail 3|Beneficiario|Fecha Contrato|Fecha Contrato Num|Otorgamiento|Otorgamiento Num|Moneda|Plazo|Plazo Num|Interes|Interes Num|Cantidad con interes|Cantidad con Num|Clausula Sexta|Plazo|Derechos\n");

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
				String revisionManual       = "";
				
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

				String beneficiario         = Commons.extract(content, "designado", ",", "OCTAVA.").replaceAll("designado a", "");

				String fechaContrato        = Commons.extract(content, "a los", ".", "LEGISLACIÓN APLICABLE");
				if(fechaContrato.length() == 0)
					fechaContrato = extractFechaContrato(content);

				if(fechaContrato.indexOf(".") > 0)
					fechaContrato = fechaContrato.substring(0, fechaContrato.indexOf("."));

				if(fechaContrato.indexOf("POR") > 0)
					fechaContrato = fechaContrato.substring(0, fechaContrato.indexOf("POR"));

				String fechaContratoNum     = Commons.convertirFecha(fechaContrato);

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

						Commons.toSingleLine(derechos)

						) + "\n");
			}

			System.out.println("Archivo CSV generado en: " + csvOutputPath);

		} catch (IOException e) {
			System.err.println("Ocurrió un error al procesar los archivos: " + e.getMessage());
		}
	}

	public static String extractFechaContrato(String texto) {
		try {

			int index  = texto.indexOf("en el", texto.indexOf("lo firman de conformidad"));
			int index2 = texto.indexOf("E", index);

			return texto.substring(index, index2);
		}
		catch(Exception e) {}

		return "";
	}
}