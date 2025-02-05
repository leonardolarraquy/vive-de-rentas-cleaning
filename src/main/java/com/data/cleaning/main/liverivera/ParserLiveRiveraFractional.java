package com.data.cleaning.main.liverivera;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import com.data.cleaning.main.Commons;

public class ParserLiveRiveraFractional {
	
	public static String getTipoContrato() {
		return "Promesa compraventa-Derechos fideicomisarios-Fractional";
	}
	
	public static String getProyecto() {
		return "Live Riviera";
	}
	
	public static String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/live-rivera-promesa-compraventa-fractional/";
	}

	public static void main(String[] args) {
		String folderPath = getFolderPath();

		String[] rutas = Commons.readLines(folderPath + "_links");

		String csvOutputPath = folderPath + "/output.csv";

		int i = 0;

		// Crear el archivo CSV de salida
		try (BufferedWriter csvWriter = new BufferedWriter(new FileWriter(csvOutputPath))) {        	
			// Escribir encabezados en el archivo CSV

			csvWriter.write("Tipo Contrato|Nombre Proyecto|Nombre del archivo|Link a archivo|Revision Manual|Tags|ENAJENANTE|ADQUIRENTE|Clave Unica|CURP|Clave RFC|RFC|Nacionalidad|Estado Civil|Mail 1|Mail 2|Mail 3|Beneficiario|Fecha Contrato|Fecha Contrato Num|Tickets|Tickets Num|% Fraccion|% Fraccion Num|Unidad|Unidad Abrev.|Contraprestacion|Contraprestacion Num|Moneda|Apartado|Apartado Num|Liquidacion|Liquidacion Num|Carta Garantia|Opciones de formalizacion de contrato|Mensualidad en la que toma decision de contrato|Obligaciones Enajenante|Plazo de penalizacion tras entrega|Vigencia|Direccion Adquirente|Unidad Anexo|Forma de Pago|Plazo Garantia|Rentabilidad Anual|Rentabilidad Anual Num|Equity|\n");

			// Obtener todos los archivos .txt en la carpeta
			File[] txtFiles =  Commons.getFiles(folderPath);

			for (File txtFile : txtFiles) {
				System.out.println("processing: " + txtFile.getName());

				String ruta               = rutas[i];
				i++;

				String content = Files.readString(txtFile.toPath());

				String promitenteAdquirente = Commons.extractPromitenteAdquiriente(content);
				String promitenteEnajenante = Commons.extractPromitenteEnajenante(content);

				String tags                 = Commons.tags(content);

				String revisionManual = "";

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

				String participacion        = Commons.extract(content, "equivalente", ",", "OBJETO");
				String participacionNum     = Commons.numericValue(participacion);
				
				String participacionPorc    = Commons.extract(content, "decir", ",", "OBJETO");
				String participacionPorcNum = Commons.numericValue(participacionPorc) + "%";
				
				String unidad               = Commons.extract(content, "correspondientes a la", "(");
				if(unidad.indexOf("(") > 0)
					unidad = unidad.substring(0, unidad.indexOf("(") - 1);
				
				String unidadAbrev          = Commons.extraerUnidadAbrev(unidad);				
				if(unidadAbrev.length() == 0)
					revisionManual = revisionManual + "Unidad.";					

				String contraprestacion     = Commons.extract(content, "la cantidad", ")", "SEGUNDA") + ")";
				String contraprestacionNum  = Commons.numericValue(contraprestacion);
				String moneda               = Commons.extractMoneda(contraprestacion);

				String apartado             = Commons.extract(content, "entregó", ")", "SEGUNDA");
				if(apartado.length() > 0)
					apartado = apartado + ")";
				String apartadoNum          = Commons.numericValue(apartado);
				if(apartadoNum.length() == 0)
					revisionManual = revisionManual + "Apartado.";					

				
				String liquidacion          = Commons.extract(content, "entregará al", ")");
				if(liquidacion.length() > 0)
					liquidacion = liquidacion + ")";

				String liquidacionNum       = Commons.numericValue(liquidacion);
				if(liquidacionNum.length() == 0)
					revisionManual = revisionManual + "Liquidacion.";					

				String cartaGarantia        = Commons.extract(content, "mediante", "anexa", "Adicionalmente");
				if(cartaGarantia.length() == 0)
					revisionManual = revisionManual + "Carta Garantia.";

				String opcionesFinalizacion = Commons.extract(content, "mediante", "que", "estipulado").replaceAll("mediante la", "");
				if(opcionesFinalizacion.indexOf(",") > 0)
					opcionesFinalizacion = opcionesFinalizacion.substring(0, opcionesFinalizacion.indexOf(","));
				
				String mensualidad          = Commons.extract(content, "mensualidad", "(", "CUARTA.");
				if(mensualidad.indexOf("B.") > 0)
					mensualidad = mensualidad.substring(0, mensualidad.indexOf("B."));	
				
				String obligaciones         = Commons.extract(content, "A.", "para", "al efecto");
				if(obligaciones.indexOf(" en") > 0)
					obligaciones = obligaciones.substring(0, obligaciones.indexOf(" en"));	
				
				String plazo                = Commons.extract(content, "dentro de", "llevando", "una vez finalizado");

				String vigencia             = Commons.extract(content, "vigente", ",", "SÉPTIMA");

				String beneficiario         = Commons.extract(content, "C. ", ",", "BENEFICIARIO");
				if(beneficiario.length() > 0)
					beneficiario = beneficiario.substring(2, beneficiario.length());
				

				String direccionAdquirente  = Commons.extract(content, "ADQUIRENTE", "EL “", "DÉCIMA TERCERA");
				if(direccionAdquirente.length() > 13)
					direccionAdquirente = direccionAdquirente.substring(13);
				
				if(direccionAdquirente.indexOf("Cualquiera") > 0)
					direccionAdquirente = direccionAdquirente.substring(0, direccionAdquirente.indexOf("Cualquiera"));	

				if(direccionAdquirente.indexOf("/") > 0)
					direccionAdquirente = direccionAdquirente.substring(0, direccionAdquirente.indexOf("/"));	

				
				String fechaContrato        = Commons.extract(content, "en dos tanto", ".", "LEGISLACIÓN APLICABLE").replaceAll("en dos tanto", "");
				if(fechaContrato.indexOf("EL") > 0)
					fechaContrato = fechaContrato.substring(0, fechaContrato.indexOf("EL"));

				if(fechaContrato.length() == 0)
					fechaContrato = extractFechaContrato(content);

				if(fechaContrato.indexOf(".") > 0)
					fechaContrato = fechaContrato.substring(0, fechaContrato.indexOf("."));

				String fechaContratoNum     = Commons.convertirFecha(fechaContrato);
				
				String unidadAnexo          = Commons.extract(content, "Unidad número:", "\n", "Unidad Inmobiliaria:").replaceAll("Unidad número: ", "");
				
				String formaDePago          = Commons.extract(content, "Forma de Pago:", "EL", "ANEXO").replaceAll("Forma de Pago:", "");
				if(formaDePago.indexOf(".") > 0)
					formaDePago = formaDePago.substring(0, formaDePago.indexOf("."));	

				String plazoGarantia        = Commons.extract(content, "exclusivamente", "denominado", "Adquirente única y exclusivamente");
				String rentabilidadAnual    = Commons.extract(content, "correspondiente al", ",", "exclusivamente");
				String equity               = Commons.extract(content, "un equity", ")", "exclusivamente");
				if(equity.length() > 0)
					equity = equity + ")";
				else revisionManual = revisionManual + "Equity.";


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

						Commons.toSingleLine(participacion),
						Commons.toSingleLine(participacionNum),

						Commons.toSingleLine(participacionPorc),
						Commons.toSingleLine(participacionPorcNum),

						Commons.toSingleLine(unidad),
						Commons.toSingleLine(unidadAbrev),
						
						Commons.toSingleLine(contraprestacion),
						Commons.toSingleLine(contraprestacionNum),
						Commons.toSingleLine(moneda),

						Commons.toSingleLine(apartado),
						Commons.toSingleLine(apartadoNum),
						
						Commons.toSingleLine(liquidacion),
						Commons.toSingleLine(liquidacionNum),

						Commons.toSingleLine(cartaGarantia),
						Commons.toSingleLine(opcionesFinalizacion),
						Commons.toSingleLine(mensualidad),


						Commons.toSingleLine(obligaciones),
						Commons.toSingleLine(plazo),
						Commons.toSingleLine(vigencia),

						Commons.toSingleLine(direccionAdquirente),
						
						Commons.toSingleLine(unidadAnexo),
						Commons.toSingleLine(formaDePago),

						Commons.toSingleLine(plazoGarantia),
						
						Commons.toSingleLine(rentabilidadAnual),
						Commons.toSingleLine(Commons.numericValue(rentabilidadAnual) + "%"),
						
						Commons.toSingleLine(equity)

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