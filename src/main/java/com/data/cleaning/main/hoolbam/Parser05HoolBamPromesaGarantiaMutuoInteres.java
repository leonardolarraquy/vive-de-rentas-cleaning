package com.data.cleaning.main.hoolbam;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.data.cleaning.main.Commons;

public class Parser05HoolBamPromesaGarantiaMutuoInteres {
	
	public static String getTipoContrato() {
		return "Promesa compraventa-Garantía de mutuo con interés";
	}
	
	public static String getProyecto() {
		return "Hool Balam";
	}
	
	public static String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/hool-bam-promesa-garantia-con-mutuo-interes/";
	}


	public static void main(String[] args) {
		String folderPath = getFolderPath();

		String[] rutas = Commons.readLines(folderPath + "_links");

		String csvOutputPath = folderPath + "/output.csv";

		int i = 0;

		// Crear el archivo CSV de salida
		try (BufferedWriter csvWriter = new BufferedWriter(new FileWriter(csvOutputPath))) {        	
			// Escribir encabezados en el archivo CSV
			csvWriter.write("Tipo Contrato|Nombre Proyecto|Nombre del archivo|Link a archivo|Revision Manual|Tags|ENAJENANTE|ADQUIRENTE|Clave Unica|CURP|Clave RFC|RFC|Nacionalidad|Estado Civil|Mail 1|Mail 2|Mail 3|Beneficiario|Fecha Contrato|Fecha Contrato Num|Domicilio|Contraprestacion|Contraprestacion Num|Moneda|Participacion|Participacion Num|Compromete a Pagar|Compromete a Pagar Num|Concepto Interes|Tasa|Tasa num|Monto interes anual|Monto interes anual num|Cuota Mensual|Cuota Mensual Num|Carta Garantia|Opciones|Mensualidad para ejercer derecho de salida|Mensualidad para ejercer derecho de salida Num|Oferta|Prorroga pago derecho de salida|Participacion|Participacion Num|Opciones para formalizar contrato final|Vigencia|Equity Instantaneo\n");

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
				
				String domicilioAdquirente  = Commons.extract(content, "su domicilio en", ",", "ADQUIRENTE").replaceAll("su domicilio en: ", "").replaceAll("su domicilio en; ", "");
				
				String contraprestacion     = Commons.extract(content, "la cantidad", "(", "OBJETO");
				String contraprestacionNum  = Commons.numericValue(contraprestacion);
				String moneda               = Commons.extractMoneda(contraprestacion);
				
				String participacion        = Commons.extract(content, "equivalente a un", "sobre", "OBJETO");
				String participacionNum     = extraerPorcentaje(participacion);
				
				String comprometeAlPago     = Commons.extract(content, "al pago de", "más", "SEGUNDA");
				String comprometeAlPagoNum  = Commons.numericValue(comprometeAlPago);

				String conceptoInteres      = Commons.extract(content, "por concepto de", "sobre", "SEGUNDA");
				if(conceptoInteres.indexOf(",") > 0)
					conceptoInteres = conceptoInteres.substring(0, conceptoInteres.indexOf(",") );
				
				String tasaAnual            = Commons.extract(content, "tasa anual", "equivalente", "se obligan a pagar");				
				String montoEquivalente     = Commons.extract(content, "la cantidad", "(", "se obligan a pagar");
				String mensualidad          = Commons.extract(content, "cantidad de", "(", "pagaderas");
				
				String cartaGarantia        = Commons.extract(content, "Adicionalmente, mediante", "anexa", "SEGUNDA");
				
				String opcionesSalida       = Commons.extract(content, "TERCERA: ","concluir");
				String mensualidadSalida    = Commons.extract(content, "posteriores a","simple", "OPCIONES").replaceAll("posteriores a", "");
				
				String oferta               = Commons.extract(content, "realizada" , ",", "TERCERA");
				String prorroga             = Commons.extract(content, "dentro de un plazo", "." , "TERCERA");
				String participacion2       = Commons.extract(content, "equivalente a", "del bien" , "TERCERA");
				
				String opcionesFormalizar   = Commons.extract(content, "constitución de la", "en " , "CUARTA");
				
				String vigencia             = Commons.extract(content, "vigente", "," , "SÉPTIMA");

				String beneficiario         = Commons.extract(content, " C.", "," , "BENEFICIARIO");
				if(beneficiario.length() > 0)
					beneficiario= beneficiario.substring(4, beneficiario.length());

				String equity               = Commons.extract(content, "equity instantáneo", "del " , "JURISDICCIÓN");
		
				String fechaContrato        = Commons.extract(content, "de México a los", ".", "LEGISLACIÓN APLICABLE").replaceAll("de México a ", "");
				if(fechaContrato.indexOf("EL") > 0)
					fechaContrato = fechaContrato.substring(0, fechaContrato.indexOf("EL"));

				if(fechaContrato.length() == 0)
					fechaContrato = extractFechaContrato(content);
				
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

						Commons.toSingleLine(domicilioAdquirente),

						Commons.toSingleLine(contraprestacion),
						Commons.toSingleLine(contraprestacionNum),
						Commons.toSingleLine(moneda),
						
						Commons.toSingleLine(participacion),
						Commons.toSingleLine(participacionNum + "%"),
						
						Commons.toSingleLine(comprometeAlPago),
						Commons.toSingleLine(comprometeAlPagoNum),
						
						Commons.toSingleLine(conceptoInteres),
						
						Commons.toSingleLine(tasaAnual),
						Commons.toSingleLine(Commons.numericValue(tasaAnual)  + "%"),
						Commons.toSingleLine(montoEquivalente),
						Commons.toSingleLine(Commons.numericValue(montoEquivalente)),
						Commons.toSingleLine(mensualidad),
						Commons.toSingleLine(Commons.numericValue(mensualidad)),
						
						Commons.toSingleLine(cartaGarantia),
						Commons.toSingleLine(opcionesSalida),
						Commons.toSingleLine(mensualidadSalida),
						Commons.toSingleLine(Commons.numericValue(mensualidadSalida)),

						Commons.toSingleLine(oferta),
						
						Commons.toSingleLine(prorroga),
						
						Commons.toSingleLine(participacion2),
						Commons.toSingleLine(Commons.numericValue(participacion2) + "%"),
						Commons.toSingleLine(opcionesFormalizar),
						Commons.toSingleLine(vigencia),
						Commons.toSingleLine(equity)


						) + "\n");
			}

			System.out.println("Archivo CSV generado en: " + csvOutputPath);

		} catch (IOException e) {
			System.err.println("Ocurrió un error al procesar los archivos: " + e.getMessage());
		}
	}

	public static String extraerPorcentaje(String content) {
		String regex = "\\((\\d+)\\)";

		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(content);

		return matcher.find() ? matcher.group(1) : "";
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