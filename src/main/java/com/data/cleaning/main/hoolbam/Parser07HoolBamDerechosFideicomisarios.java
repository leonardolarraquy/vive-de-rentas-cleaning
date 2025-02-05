package com.data.cleaning.main.hoolbam;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import com.data.cleaning.main.Commons;

public class Parser07HoolBamDerechosFideicomisarios {
	
	public static String getTipoContrato() {
		return "Promesa compraventa-Derechos fideicomisarios-Torre 2";
	}
	
	public static String getProyecto() {
		return "Hool Balam";
	}
	
	public static String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/hool-bam-promesa-compra-venta-derecho-fideicomisarios/";
	}


	public static void main(String[] args) {
		String folderPath = getFolderPath();

		String[] rutas = Commons.readLines(folderPath + "_links");

		String csvOutputPath = folderPath + "/output.csv";

		int i = 0;

		// Crear el archivo CSV de salida
		try (BufferedWriter csvWriter = new BufferedWriter(new FileWriter(csvOutputPath))) {        	
			// Escribir encabezados en el archivo CSV
			csvWriter.write("Tipo Contrato|Nombre Proyecto|Nombre del archivo|Link a archivo|Revision Manual|Tags|ENAJENANTE|ADQUIRENTE|Clave Unica|CURP|Clave RFC|RFC|Nacionalidad|Estado Civil|Mail 1|Mail 2|Mail 3|Beneficiario|Fecha Contrato|Fecha Contrato Num|Torre|Participacion|Participacion Num|Contraprestacion|Contraprestacion Num|Moneda|Apartado|Apartado Num|Monto Liquidacion|Monto Liquidacion Num|Constitucion|Vigencia|Prorroga|Direccion Adquirente|Entrega|Fecha Entrega Num\n");

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

				String revisionManual = "";

				String torre                = Commons.extract(content, "UBICADOS EN", "DEL", "EXCLUSIVA DEPARTAMENTOS").replaceAll("UBICADOS EN", "");
				if(torre.length() == 0)
					revisionManual = revisionManual + "Torre.";					

				String CURP                 = Commons.getCURP(content);
				String CURPLimpio           = Commons.getCURPLimpio(CURP);
				
				if(CURP.length() == 0 )
					revisionManual = revisionManual + "CURP.";
				else {
					if(CURPLimpio.length() != 18)
						revisionManual = revisionManual + "CURP Invalido.";
				}

				String RFC                  = Commons.getRFC(content);
				String RFCLimpio            = Commons.getRFCLimpio(RFC);
				
				if(RFC.length() == 0 )
					revisionManual = revisionManual + "RFC.";
				else {
					if(RFCLimpio.length() != 13 && RFCLimpio.length() != 12)
						revisionManual = revisionManual + "RFC Invalido.";					
				}
				
				String participacion        = Commons.extract(content, "equivalente a", "sobre", "PRIMERA");
				
				String contraprestacion     = Commons.extract(content, "cantidad de", "(", "PRIMERA.");
				String contraprestacionNum  = Commons.numericValue(contraprestacion);
				String moneda               = Commons.extractMoneda(contraprestacion);
				String apartado             = Commons.extract(content, "cantidad de", "por", "Previo a");
				String obligacion           = Commons.extract(content, "valor de", "misma", "se obliga");

				String constitucion         = Commons.extract(content, "La", "que", "CUARTA");
				String vigencia             = Commons.extract(content, "estará", ",", "SEXTA");
				String plazo                = Commons.extract(content, "prorrogarse", "en", "ENTREGA DEL");

				String direccionAdquirente  = Commons.extract(content, "manifiesta tener su domicilio", "mismos", "con la clave").replaceAll("manifiesta tener su domicilio en:", "");
				if(direccionAdquirente.length() == 0) {
					direccionAdquirente  = Commons.extract(content, "ADQUIRENTE", "EL \u201C", "CUARTA");
					if(direccionAdquirente.length() > 13)
						direccionAdquirente = direccionAdquirente.substring(13, direccionAdquirente.length());
	
				}
				if(direccionAdquirente.indexOf("QUINTA") > 0)
					direccionAdquirente= direccionAdquirente.substring(0, direccionAdquirente.indexOf("QUINTA"));

				
				String beneficiario         = Commons.extract(content, " C.", "," , "DÉCIMA");
				if(beneficiario.length() > 0)
					beneficiario= beneficiario.substring(4, beneficiario.length());
				
				if(beneficiario.indexOf("llevando") > 0)
					beneficiario= beneficiario.substring(0, beneficiario.indexOf("llevando"));
				
				beneficiario = beneficiario.replaceAll("\\.","");
				
				String unidad               = Commons.extract(content, "Inmobiliaria No.", "de", "QUINTA");
				if(unidad.length() == 0)
					unidad                  = Commons.extract(content, "Unidad número:", "\n");

				String fechaContrato        = Commons.extract(content, "de México a los", ".", "LEGISLACIÓN APLICABLE").replaceAll("de México a ", "");
				if(fechaContrato.length() == 0)
					fechaContrato = extractFechaContrato(content);
				
				if(fechaContrato.indexOf(".") > 0)
					fechaContrato = fechaContrato.substring(0, fechaContrato.indexOf("."));

				String fechaContratoNum     = Commons.convertirFecha(fechaContrato);
				
				String fechaDeEntrega       = Commons.extract(content, "Fecha de entrega:", "\n");
				if(fechaDeEntrega.length() == 0)
					revisionManual = revisionManual + "Fecha Entrega.";					
				
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

						Commons.toSingleLine(torre),
						
						Commons.toSingleLine(participacion),
						Commons.toSingleLine(Commons.numericValue(participacion) + "%"),

						Commons.toSingleLine(contraprestacion),
						Commons.toSingleLine(contraprestacionNum),
						Commons.toSingleLine(moneda),
						
						Commons.toSingleLine(apartado),
						Commons.toSingleLine(Commons.numericValue(apartado)),
						
						Commons.toSingleLine(obligacion),
						Commons.toSingleLine(Commons.numericValue(obligacion)),

						Commons.toSingleLine(constitucion),
						Commons.toSingleLine(vigencia),
						Commons.toSingleLine(plazo),

						Commons.toSingleLine(direccionAdquirente),
						
						Commons.toSingleLine(fechaDeEntrega),
						Commons.toSingleLine(Commons.extraerFechaAPartirDeTexto(fechaDeEntrega))

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