package com.data.cleaning.main.vivastorage.cancun;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import com.data.cleaning.main.Commons;

public class ParserCancunDerechosFideicomisariosMod2 {
	
	public static String getTipoContrato() {
		return "Promesa compraventa-Derechos fideicomisarios-m2";
	}
	
	public static String getProyecto() {
		return "Vive Storage Cancún";
	}
	
	public static String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/cancun-promesa-compra-venta-derecho-fideicomisarios-mod2/";
	}


	public static void main(String[] args) {
		String folderPath = getFolderPath();

		String[] rutas = Commons.readLines(folderPath + "_links");

		String csvOutputPath = folderPath + "/output.csv";

		int i = 0;

		// Crear el archivo CSV de salida
		try (BufferedWriter csvWriter = new BufferedWriter(new FileWriter(csvOutputPath))) {        	
			// Escribir encabezados en el archivo CSV
			csvWriter.write("Tipo Contrato|Nombre Proyecto|Nombre del archivo|Link a archivo|Revision Manual|Tags|ENAJENANTE|ADQUIRENTE|Clave Unica|CURP|Clave RFC|RFC|Nacionalidad|Estado Civil|Mail 1|Mail 2|Mail 3|Ubicacion|Propiedad|Contraprestacion|Contraprestacion Num|Moneda|Apartado|Apartado Num|Liquidacion|Liquidacion Num|Obligaciones del Enajenante|Vigencia|Emision Derechos|Emision Derechos Num|Prorroga|Direccion Adquirente|Beneficiario|Fecha Contrato|Fecha Contrato Num|Plazo Rendimiento Garantizado|Rentabilidad Anual|Fecha a partir que recibe rendimientos\n");

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

				String ubicacion            = Commons.extract(content, "ubicado", "(", "proyecto").replaceAll("ubicado en", "");
				if(ubicacion.length() == 0)
					revisionManual = revisionManual + "Ubicacion.";					

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
				
				String propiedad            = Commons.extract(content, "correspondientes", "ubicada", "PRIMERA");
				
				String contraprestacion     = Commons.extract(content, "cantidad de", "(", "SEGUNDA");
				String moneda               = Commons.extractMoneda(contraprestacion);
				
				String apartado             = Commons.extract(content, "cantidad", "(", "entregó");
				String liquidacion          = Commons.extract(content, "cantidad", "(", "pagar");
				
				String obligaciones         = Commons.extract(content, "del", "que", "constitución");
				String vigencia             = Commons.extract(content, "estar", " a ", "SEXTA");
				
				String emisionDerechos      = Commons.extract(content, "mes", ".", "DERECHOS");
				if(emisionDerechos.length() == 0)
					revisionManual = revisionManual + "Emision Derechos.";					

				String prorroga             = Commons.extract(content, "prorrogarse", "en", "DERECHOS");
				
				
				String direccionAdquirente  = Commons.extract(content, "ADQUIRENTE", "EL “", "DOMICILIOS");
				if(direccionAdquirente.length() > 13)
					direccionAdquirente = direccionAdquirente.substring(13);
				
				if(direccionAdquirente.indexOf("Cualquiera") > 0)
					direccionAdquirente = direccionAdquirente.substring(0, direccionAdquirente.indexOf("Cualquiera"));	

				if(direccionAdquirente.indexOf("/") > 0)
					direccionAdquirente = direccionAdquirente.substring(0, direccionAdquirente.indexOf("/"));	

				
				String beneficiario         = Commons.extract(content, "transmitido", "llevando" , "DÉCIMA").replaceAll("transmitido a", "");

				
				String fechaContrato        = Commons.extract(content, " a ", ".", "obligacional");
				if(fechaContrato.indexOf(".") > 0)
					fechaContrato = fechaContrato.substring(0, fechaContrato.indexOf("."));

				String fechaContratoNum     = Commons.convertirFecha(fechaContrato);
				
				String plazoRendimiento     = Commons.extract(content, "durante", "contados", "Al respecto");
				
				String rentabilidadAnual    = Commons.extract(content, "correspondiente", ",", "Al respecto");
				if(rentabilidadAnual.indexOf(",") > 0)
					rentabilidadAnual = rentabilidadAnual.substring(0, rentabilidadAnual.indexOf(","));
				
				String aPartir              = Commons.extract(content, "partir", ".", "Al respecto");
				if(aPartir.indexOf(",") > 0)
					aPartir = aPartir.substring(0, aPartir.indexOf(","));

				if(aPartir.indexOf("el pago") > 0)
					aPartir = aPartir.substring(0, aPartir.indexOf("el pago"));

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

						Commons.toSingleLine(ubicacion),
						
						Commons.toSingleLine(propiedad),

						Commons.toSingleLine(contraprestacion),
						Commons.toSingleLine(Commons.numericValue(contraprestacion)),
						Commons.toSingleLine(moneda),
						
						Commons.toSingleLine(apartado),
						Commons.toSingleLine(Commons.numericValue(apartado)),
						Commons.toSingleLine(liquidacion),
						Commons.toSingleLine(Commons.numericValue(liquidacion)),
						
						Commons.toSingleLine(obligaciones),
						Commons.toSingleLine(vigencia),

						Commons.toSingleLine(emisionDerechos),
						Commons.toSingleLine(Commons.extraerFechaAPartirDeTexto(emisionDerechos)),

						Commons.toSingleLine(prorroga),

						Commons.toSingleLine(direccionAdquirente),
						Commons.toSingleLine(beneficiario),
												
						Commons.toSingleLine(fechaContrato),
						Commons.toSingleLine(fechaContratoNum),

						Commons.toSingleLine(plazoRendimiento),
						Commons.toSingleLine(rentabilidadAnual),
						Commons.toSingleLine(aPartir)

						) + "\n");
			}

			System.out.println("Archivo CSV generado en: " + csvOutputPath);

		} catch (IOException e) {
			System.err.println("Ocurrió un error al procesar los archivos: " + e.getMessage());
		}
	}

}