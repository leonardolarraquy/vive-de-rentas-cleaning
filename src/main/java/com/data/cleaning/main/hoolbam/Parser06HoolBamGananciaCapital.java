package com.data.cleaning.main.hoolbam;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import com.data.cleaning.main.Commons;

public class Parser06HoolBamGananciaCapital {
	
	public static String getTipoContrato() {
		return "Ganancia de capital";
	}
	
	public static String getProyecto() {
		return "Hool Balam";
	}
	
	public static String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/hool-bam-ganancia-capital/";
	}


	public static void main(String[] args) {
		String folderPath = getFolderPath();

		String[] rutas = Commons.readLines(folderPath + "_links");

		String csvOutputPath = folderPath + "/output.csv";

		int i = 0;

		// Crear el archivo CSV de salida
		try (BufferedWriter csvWriter = new BufferedWriter(new FileWriter(csvOutputPath))) {        	
			// Escribir encabezados en el archivo CSV
			csvWriter.write("Tipo Contrato|Nombre Proyecto|Nombre del archivo|Link a archivo|Revision Manual|Tags|Mutuatario|Mutuante|Clave Unica|CURP|Clave RFC|RFC|Nacionalidad|Estado Civil|Mail 1|Mail 2|Mail 3|Beneficiario|Fecha Contrato|Fecha Contrato Num|Domicilio Mutuante|Contraprestacion|Contraprestacion Num|Moneda|Numero Cuotas|Numero Cuotas Num|Mensualidades|Mensualidades Num|Termino|Termino Num|Interes Moratorio|Interes Moratorio Num|Adquisicion|Adquisicion Num|Unidad|Unidad Abrev\n");

			// Obtener todos los archivos .txt en la carpeta
			File[] txtFiles =  Commons.getFiles(folderPath);

			for (File txtFile : txtFiles) {
				System.out.println("processing: " + txtFile.getName());

				String ruta               = rutas[i];
				i++;

				String content = Files.readString(txtFile.toPath());

				String mututante = Commons.extract(content, "PARTE", "COMPARECE").replaceAll("PARTE", "").replaceAll("LA C.", "").replaceAll("QUIEN", "");
				if(mututante.length() == 0)
					mututante = Commons.extract(content, "PARTE", "POR SU").replaceAll("PARTE", "").replaceAll("LA C.", "").replaceAll("QUIEN", "");
				
				String mutuatario = Commons.extractPromitenteEnajenante(content);
				
				String tags                 = Commons.tags(content) + "-ganancia capital";

				String CURP                 = Commons.getCURP(content);
				String CURPLimpio           = Commons.getCURPLimpio(CURP);
				
				String revisionManual = "";

				if(CURP.length() == 0 )
					revisionManual = revisionManual + "CURP.";
				else {
					if(CURPLimpio.length() != 18)
						revisionManual = revisionManual + "CURP Invalido.";
				}

				String RFC                  = Commons.getRFC(content, "inscrita");
				String RFCLimpio            = Commons.getRFCLimpio(RFC);
				
				if(RFC.length() == 0 )
					revisionManual = revisionManual + "RFC.";
				else {
					if(RFCLimpio.length() != 13 && RFCLimpio.length() != 12)
						revisionManual = revisionManual + "RFC Invalido.";					
				}

				String domicilioAdquirente  = Commons.extract(content, "su domicilio en:", "D.").replaceAll("su domicilio en:", "");
				if(domicilioAdquirente.indexOf("Que ") > 0)
					domicilioAdquirente = domicilioAdquirente.substring(0, domicilioAdquirente.indexOf("Que "));

				String contraprestacion     = Commons.extract(content, "cantidad de", "(", "PRIMERA.");
				String contraprestacionNum  = Commons.numericValue(contraprestacion);
				String moneda               = Commons.extractMoneda(contraprestacion);
				
				String numeroCuotas        = Commons.extract(content, "obliga a efectuar", "por");
				String mensualidades        = Commons.extract(content, "cantidad ", "en ", "SEGUNDA");
				String termino              = Commons.extract(content, "no mayor de", "contados", "SEGUNDA");
			
				String interes              = Commons.extract(content, "estableciéndose un", "(", "TERCERA");
				
				String porcDerechos         = Commons.extract(content, "adquisición del", "correspondiente", "QUINTA");
				String porcDerechosNum      = Commons.extractParteDecimal(porcDerechos);
				
				String unidad               = Commons.extract(content, "Inmobiliaria No.", "de", "QUINTA");
				if(unidad.length() == 0)
					unidad                  = Commons.extract(content, "Unidad número:", "\n");

				String beneficiario         = Commons.extract(content, " C.", "," , "BENEFICIARIO");
				if(beneficiario.length() > 0)
					beneficiario= beneficiario.substring(4, beneficiario.length());

				String fechaContrato        = Commons.extract(content, "de México a los", ".", "LEGISLACIÓN APLICABLE").replaceAll("de México a ", "");
				if(fechaContrato.length() == 0)
					fechaContrato = extractFechaContrato(content);
				
				if(fechaContrato.indexOf(".") > 0)
					fechaContrato = fechaContrato.substring(0, fechaContrato.indexOf("."));

				String fechaContratoNum     = Commons.convertirFecha(fechaContrato);
				
				String unidadSimple         = unidad.replaceAll("Inmobiliaria No. ", "");

				// Escribir una fila en el archivo CSV
				csvWriter.write(String.join("|",
						Commons.toSingleLine(getTipoContrato()),
						Commons.toSingleLine(getProyecto()),
						Commons.toSingleLine(txtFile.getName().replaceAll(".txt", ".pdf")),
						Commons.toSingleLine(ruta),
						revisionManual,
						Commons.toSingleLine(tags),
						
						Commons.toSingleLine(mutuatario),
						Commons.toSingleLine(mututante),
						
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

						Commons.toSingleLine(domicilioAdquirente),

						Commons.toSingleLine(contraprestacion),
						Commons.toSingleLine(contraprestacionNum),
						Commons.toSingleLine(moneda),

						Commons.toSingleLine(numeroCuotas),
						Commons.toSingleLine(Commons.numericValue(numeroCuotas)),
						
						Commons.toSingleLine(mensualidades),
						Commons.toSingleLine(Commons.numericValue(mensualidades)),
						
						Commons.toSingleLine(termino),
						Commons.toSingleLine(Commons.numericValue(termino)),

						Commons.toSingleLine(interes),
						Commons.toSingleLine(Commons.numericValue(interes) + "%"),

						Commons.toSingleLine(porcDerechos),
						Commons.toSingleLine(porcDerechosNum),


						Commons.toSingleLine(unidad),
						Commons.toSingleLine(unidadSimple)


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
}