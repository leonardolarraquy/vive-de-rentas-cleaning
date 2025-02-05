package com.data.cleaning.main.smartdepas;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import com.data.cleaning.main.Commons;

public class ParserSmartDepasCompraVentaCompleto {
	
	public static String getTipoContrato() {
		return "Promesa compraventa-Derechos fideicomisarios-Completo";
	}
	
	public static String getProyecto() {
		return "Smart Depas Tulum";
	}
	
	public static String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/smart-depas-promesa-compraventa-completo/";
	}


	public static void main(String[] args) {
		String folderPath = getFolderPath();

		String[] rutas = Commons.readLines(folderPath + "_links");

		String csvOutputPath = folderPath + "/output.csv";

		int i = 0;

		try (BufferedWriter csvWriter = new BufferedWriter(new FileWriter(csvOutputPath))) {        	
			// Escribir encabezados en el archivo CSV
			csvWriter.write("Tipo Contrato|Nombre Proyecto|Nombre del archivo|Link a archivo|Revision Manual|Tags|ENAJENANTE|ADQUIRENTE|Clave Unica|CURP|Clave RFC|RFC|Nacionalidad|Estado Civil|Mail 1|Mail 2|Mail 3|Objeto|Contraprestacion|Contraprestacion Num|Moneda|Clausulas|Vigencia|Entrega|Entrega Num|Prorroga|Domicilio Adquiriente|Fecha Contrato|Fecha Contrato Num|Unidad|Unidad Abreviada|Forma de Pago\n");

			File[] txtFiles =  Commons.getFiles(folderPath);

			for (File txtFile : txtFiles) {
				System.out.println("processing: " + txtFile.getName());

				String ruta               = rutas[i];
				i++;

				String content = Files.readString(txtFile.toPath());

				String promitenteAdquirente = Commons.extractPromitenteAdquiriente(content);
				if(promitenteAdquirente.length() == 0)
					promitenteAdquirente = Commons.extract(content, "B.", "(").replaceAll("B. ", "");
				
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



				String objeto               = extractObjeto(content);

				String contraprestacion     = extractContraprestacion(content);
				String contraprestacionNum  = Commons.numericValue(contraprestacion);
				String moneda               = Commons.extractMoneda(contraprestacion);

				String obligacion           = extractObligacion(content);
				String vigencia             = extractVigencia(content);
				String entrega              = extractEntrega(content);
				if(entrega.indexOf("La ") > 0)
					entrega = entrega.substring(0, entrega.indexOf("La "));
				
				String entregaNum           = Commons.extraerFechaAPartirDeTexto(entrega.replaceAll("Fecha de entrega:", ""));
				if(entregaNum == null || entregaNum.length() == 0)
					revisionManual = "Fecha Entrega.";
				
				String prorroga             = extractProrroga(content);

				String domicilioAdquirente  = extractDomicilioAdquiriente(content);

				String fechaContrato        = Commons.fechaContrato(content);
				String fechaContratoNum     = Commons.convertirFecha(fechaContrato);
				
				String unidad               = extractUnidad(content);
				String unidadAbrev          = Commons.extraerUnidadAbrev(unidad);
				
				if(unidadAbrev.length() == 0)
					revisionManual = revisionManual + "Unidad.";
				
				String formaDePago          = extractFormaDePago(content);

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

						Commons.toSingleLine(objeto),

						Commons.toSingleLine(contraprestacion),
						Commons.toSingleLine(contraprestacionNum),
						Commons.toSingleLine(moneda),

						Commons.toSingleLine(obligacion),
						Commons.toSingleLine(vigencia),
						Commons.toSingleLine(entrega),
						Commons.toSingleLine(entregaNum),
						Commons.toSingleLine(prorroga),

						Commons.toSingleLine(domicilioAdquirente),

						Commons.toSingleLine(fechaContrato),
						Commons.toSingleLine(fechaContratoNum),

						Commons.toSingleLine(unidad),
						Commons.toSingleLine(unidadAbrev),
						
						Commons.toSingleLine(formaDePago)


						) + "\n");
			}

			System.out.println("Archivo CSV generado en: " + csvOutputPath);

		} catch (IOException e) {
			System.err.println("Ocurrió un error al procesar los archivos: " + e.getMessage());
		}
	}

	public static String extractFormaDePago(String texto) {
		try {
			int index = texto.indexOf("Forma de Pago:");
			int index2 = texto.indexOf("EL", index);

			int index3 = texto.indexOf(".", index);
			if(index3 != -1 && index3 < index2)
				index2 = index3;

			index3 = texto.indexOf("\n", index + 20);
			if(index3 != -1 && index3 < index2)
				index2 = index3;

			return texto.substring(index, index2);

		}
		catch(Exception e) {}

		return "";
	}

	public static String extractUnidad(String texto) {
		try {

			int index = texto.indexOf("Unidad número:");
			int index2 = texto.indexOf("\n", index + 15);

			if(index == -1) {
				index = texto.indexOf("Unidad Inmobiliaria");
				index2 = texto.indexOf("\n", index + 22);
			}
			
			return texto.substring(index, index2);

		}
		catch(Exception e) {}

		return "";
	}

	public static String extractDomicilioAdquiriente(String texto) {
		try {
			int index = texto.indexOf("ADQUIRENTE", texto.indexOf("NOVENA. ")) + 12;
			
			int index2 = texto.indexOf("Cualquiera ", index);

			int index3 = texto.indexOf("México", index);
			if(index3 != -1 && index3 < index2)
				index2 = index3 + 6;
			
			index3 = texto.indexOf("DÉCIMA", index);
			if(index3 != -1 && index3 < index2)
				index2 = index3 + 6;

			return texto.substring(index, index2);

		}
		catch(Exception e) {}

		return "";
	}

	public static String extractProrroga(String texto) {
		try {
			int index = texto.indexOf("plazo", texto.indexOf("ENTREGA DEL"));
			int index2 = texto.indexOf("en ", index);

			int index3 = texto.indexOf(",", index);
			if(index3 != -1 && index3 < index2)
				index2 = index3;

			return texto.substring(index, index2);

		}
		catch(Exception e) {}

		return "";
	}

	public static String extractVigencia(String content) {
		try {
			int index = content.indexOf("estará", content.indexOf("QUINTA. "));
			int index2 = content.indexOf(" a", index);

			int index3 = content.indexOf(",", index);
			if(index3 != -1 && index3 < index2)
				index2 = index3;

			return content.substring(index, index2).replaceAll("“", "").replaceAll("”", "");

		}
		catch(Exception e) {}

		return "";
	}

	public static String extractEntrega(String content) {
		try {
			int index2 = 0;
			
			int index = content.indexOf("realizar", content.indexOf("ENTREGA DE"));
			if(index == -1) {
				index  = content.indexOf("Fecha de entrega:");
				
				if(index == -1)
					return "";
				
				index2 = content.indexOf("\n", index + 20);
			}
			else index2 = content.indexOf(".", index);//buscar la coma despues de la coma del monto
			
			return content.substring(index, index2);

		}
		catch(Exception e) {}

		return "";
	}

	public static String extractContraprestacion(String content) {
		try {
			int index = content.indexOf("cantidad", content.indexOf("SEGUNDA. CONTRAPRESTA"));
			int index2 = content.indexOf(")", index + 30);//buscar la coma despues de la coma del monto

			return content.substring(index, index2 + 2);

		}
		catch(Exception e) {

		}

		return "";
	}

	public static String extractObjeto(String content) {
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

	public static String extractObligacion(String content) {
		try {
			int a      = content.indexOf("alor de los", content.indexOf("SEGUNDA"));
			if(a == -1)
				return "";
			
			int index  = content.indexOf("EL", a);
			int index4 = content.indexOf("El ", a);
			
			if(index == -1 || (index4 != -1 && index4 < index))
				index = index4;
			
			int index2 = content.indexOf("TERCERA", index);
			int index3 = content.indexOf("autoriza en este", index);

			if(index3 != -1 && index3 < index2)
				index2 = index3 - 27;

			return content.substring(index, index2);

		}
		catch(Exception e) {
		}

		return "";
	}

}