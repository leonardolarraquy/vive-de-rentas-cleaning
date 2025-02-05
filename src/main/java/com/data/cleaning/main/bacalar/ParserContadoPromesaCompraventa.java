package com.data.cleaning.main.bacalar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.data.cleaning.main.Commons;

public class ParserContadoPromesaCompraventa {
	
	public static String getTipoContrato() {
		return "Contado-Promesa de compraventa";
	}
	
	public static String getProyecto() {
		return "Ecotown Bacalar";
	}
	
	public static String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/bacalar-contado-promesa-compraventa/";
	}


	public static void main(String[] args) {
		String folderPath = getFolderPath();

		String[] rutas = Commons.readLines(folderPath + "_links");

		String csvOutputPath = folderPath + "/output.csv";

		int i = 0;

		// Crear el archivo CSV de salida
		try (BufferedWriter csvWriter = new BufferedWriter(new FileWriter(csvOutputPath))) {        	
			// Escribir encabezados en el archivo CSV
			csvWriter.write("Tipo Contrato|Nombre Proyecto|Nombre del archivo|Link a archivo|Revision Manual|Tags|ENAJENANTE|ADQUIRENTE|Clave Unica|CURP|Clave RFC|RFC|Nacionalidad|Estado Civil|Mail 1|Mail 2|Mail 3|Beneficiario|Fecha Contrato|Fecha Contrato Numerica|Unidad|Manzana|Lote|Fase|Monto Contraprestacion|Monto Num|Moneda|Monto Apartado|Monto Apartado Num|Monto Liquidacion|Monto Liquidacion Num|Monto Restante a Financiar|Monto Restante a Financiar a Num|Tiempo prometido de construccion (meses)|Prorroga hasta entrega|\n");

			// Obtener todos los archivos .txt en la carpeta
			File[] txtFiles =  Commons.getFiles(folderPath);
					
			
			for (File txtFile : txtFiles) {
				System.out.println("processing: " + txtFile.getName());

				String ruta               = rutas[i];
				i++;

				String content = Files.readString(txtFile.toPath());
				String promitenteEnajenante = Commons.extractPromitenteEnajenante(content);
								
				String revisionManual     = "";
				
				String tags                 = Commons.tags(content);
				
				String promitenteAdquirente = Commons.extractPromitenteAdquiriente(content);
				
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

				String contraprestacion   = Commons.extractMonto(content);
				String contraprestacionNum= Commons.numericValue(contraprestacion);
				if(contraprestacionNum.length() == 0)
					revisionManual     = revisionManual + "Contraprestacion.";				
				
				String moneda             = Commons.extractMoneda(contraprestacion);

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
								
				String beneficiario       = Commons.extract(content, " a ", ",", ". BENEFICIARIO");
				if(beneficiario.length() > 0)
					beneficiario = beneficiario.substring(2, beneficiario.length());
				
				String fechaContrato      = fechaContrato(content);
				String fechaContratoNum   = Commons.convertirFecha(fechaContrato);

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

						Commons.toSingleLine(beneficiario),
						Commons.toSingleLine(fechaContrato),
						Commons.toSingleLine(fechaContratoNum),

						Commons.toSingleLine(unidad),
						Commons.toSingleLine(manzana),
						Commons.toSingleLine(lote),

						Commons.toSingleLine(fase),

						Commons.toSingleLine(contraprestacion),
						Commons.toSingleLine(contraprestacionNum),
						Commons.toSingleLine(moneda),

						Commons.toSingleLine(montoApartado),
						Commons.toSingleLine(montoApartadoNum),

						Commons.toSingleLine(montoLiquidacion),
						Commons.toSingleLine(montoLiquidacionNum),

						Commons.toSingleLine(montoaFinanciar),
						Commons.toSingleLine(montoaFinanciarNum),

						Commons.toSingleLine(posesion),
						Commons.toSingleLine(entrega)
						
						) + "\n");
			}

			System.out.println("Archivo CSV generado en: " + csvOutputPath);

		} catch (IOException e) {
			System.err.println("Ocurrió un error al procesar los archivos: " + e.getMessage());
		}
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

	public static String fechaContrato(String texto) {
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