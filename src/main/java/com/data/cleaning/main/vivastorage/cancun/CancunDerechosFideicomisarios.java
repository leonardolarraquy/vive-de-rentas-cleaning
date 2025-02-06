package com.data.cleaning.main.vivastorage.cancun;

import java.io.BufferedWriter;
import java.io.IOException;

import com.data.cleaning.main.BaseParser;
import com.data.cleaning.main.Commons;

public class CancunDerechosFideicomisarios extends BaseParser {

	public String getTipoContrato()  {
		return "Promesa compraventa-Derechos fideicomisarios-m2";
	}

	public String getProyecto() {
		return "Vive Storage Cancún";
	}

	public String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/cancun-promesa-compra-venta-derecho-fideicomisarios/";
	}

	public String getFieldsTitle() {
		return "Ubicacion|Propiedad|Contraprestacion|Contraprestacion Num|Moneda|Terminacion|Vigencia|Prorroga|Entrega|Fecha Entrega Num|Plazo Rendimiento Garantizado|Rentabilidad Anual|Fecha a partir que recibe rendimientos";
	}

	public static void main(String[] args) {
		CancunDerechosFideicomisarios parser = new CancunDerechosFideicomisarios();
		parser.process();
	}

	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {
		String ubicacion            = Commons.extract(content, "ubicado", "(", "SEGUNDO").replaceAll("ubicado en", "");
		if(ubicacion.length() == 0)
			revisionManual = revisionManual + "Ubicacion.";					


		String propiedad            = Commons.extract(content, "correspondientes", "ubicada", "SEGUNDO");

		String contraprestacion     = Commons.extract(content, "cantidad de", "(", "SEGUNDA");
		String contraprestacionNum  = Commons.numericValue(contraprestacion);
		String moneda               = Commons.extractMoneda(contraprestacion);

		String terminacion          = Commons.extract(content, "En caso", ".", "CUARTA");
		String vigencia             = Commons.extract(content, "estar", " a ", "SEXTA");

		String fechaDeEntrega       = Commons.extract(content, "realizar", ".", "La entrega de");
		if(fechaDeEntrega.length() == 0)
			revisionManual = revisionManual + "Fecha Entrega.";					

		String plazo                = Commons.extract(content, "prorrogarse", "en", "ENTREGA DE");

		String plazoRendimiento     = Commons.extract(content, "durante", "el pago", "Al respecto");

		String rentabilidadAnual    = Commons.extract(content, "correspondiente", ";", "Al respecto");
		if(rentabilidadAnual.indexOf(",") > 0)
			rentabilidadAnual = rentabilidadAnual.substring(0, rentabilidadAnual.indexOf(","));

		String aPartir              = Commons.extract(content, "partir", ".", "Al respecto");
		if(aPartir.indexOf(",") > 0)
			aPartir = aPartir.substring(0, aPartir.indexOf(","));

		if(aPartir.indexOf("el pago") > 0)
			aPartir = aPartir.substring(0, aPartir.indexOf("el pago"));

		csvWriter.write("|");

		csvWriter.write(
				String.join("|",
						revisionManual, 

						Commons.toSingleLine(ubicacion),

						Commons.toSingleLine(propiedad),

						Commons.toSingleLine(contraprestacion),
						Commons.toSingleLine(contraprestacionNum),
						Commons.toSingleLine(moneda),

						Commons.toSingleLine(terminacion),
						Commons.toSingleLine(vigencia),
						Commons.toSingleLine(plazo),

						Commons.toSingleLine(fechaDeEntrega),
						Commons.toSingleLine(Commons.extraerFechaAPartirDeTexto(fechaDeEntrega)),

						Commons.toSingleLine(plazoRendimiento),
						Commons.toSingleLine(rentabilidadAnual),
						Commons.toSingleLine(aPartir)));

	}

	/*
	public static void main(String[] args) {
		String folderPath = getFolderPath();

		String[] rutas = Commons.readLines(folderPath + "_links");

		String csvOutputPath = folderPath + "/output.csv";

		int i = 0;

		// Crear el archivo CSV de salida
		try (BufferedWriter csvWriter = new BufferedWriter(new FileWriter(csvOutputPath))) {        	
			// Escribir encabezados en el archivo CSV
			csvWriter.write("Tipo Contrato|Nombre Proyecto|Nombre del archivo|Link a archivo|Revision Manual|Tags|ENAJENANTE|ADQUIRENTE|Clave Unica|CURP|Clave RFC|RFC|Nacionalidad|Estado Civil|Mail 1|Mail 2|Mail 3|Ubicacion|Propiedad|Contraprestacion|Contraprestacion Num|Moneda|Terminacion|Vigencia|Prorroga|Direccion Adquirente|Beneficiario|Fecha Contrato|Fecha Contrato Num|Entrega|Fecha Entrega Num|Plazo Rendimiento Garantizado|Rentabilidad Anual|Fecha a partir que recibe rendimientos\n");

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

				String ubicacion            = Commons.extract(content, "ubicado", "(", "SEGUNDO").replaceAll("ubicado en", "");
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

				String propiedad            = Commons.extract(content, "correspondientes", "ubicada", "SEGUNDO");

				String contraprestacion     = Commons.extract(content, "cantidad de", "(", "SEGUNDA");
				String contraprestacionNum  = Commons.numericValue(contraprestacion);
				String moneda               = Commons.extractMoneda(contraprestacion);

				String terminacion          = Commons.extract(content, "En caso", ".", "CUARTA");
				String vigencia             = Commons.extract(content, "estar", " a ", "SEXTA");

				String fechaDeEntrega       = Commons.extract(content, "realizar", ".", "La entrega de");
				if(fechaDeEntrega.length() == 0)
					revisionManual = revisionManual + "Fecha Entrega.";					


				String plazo                = Commons.extract(content, "prorrogarse", "en", "ENTREGA DE");

				String direccionAdquirente  = Commons.extract(content, "ADQUIRENTE", "EL “", "DOMICILIOS");
				if(direccionAdquirente.length() > 13)
					direccionAdquirente = direccionAdquirente.substring(13);

				if(direccionAdquirente.indexOf("Cualquiera") > 0)
					direccionAdquirente = direccionAdquirente.substring(0, direccionAdquirente.indexOf("Cualquiera"));	

				if(direccionAdquirente.indexOf("/") > 0)
					direccionAdquirente = direccionAdquirente.substring(0, direccionAdquirente.indexOf("/"));	


				String beneficiario         = Commons.extract(content, "transmitido", "llevando" , "DÉCIMA").replaceAll("transmitido a", "");


				String fechaContrato        = Commons.extract(content, "de México a los", ".", "LEGISLACIÓN APLICABLE").replaceAll("de México a ", "");
				if(fechaContrato.length() == 0)
					fechaContrato = extractFechaContrato(content);

				if(fechaContrato.indexOf(".") > 0)
					fechaContrato = fechaContrato.substring(0, fechaContrato.indexOf("."));

				String fechaContratoNum     = Commons.convertirFecha(fechaContrato);

				String plazoRendimiento     = Commons.extract(content, "durante", "el pago", "Al respecto");

				String rentabilidadAnual    = Commons.extract(content, "correspondiente", ";", "Al respecto");
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
						Commons.toSingleLine(contraprestacionNum),
						Commons.toSingleLine(moneda),

						Commons.toSingleLine(terminacion),
						Commons.toSingleLine(vigencia),
						Commons.toSingleLine(plazo),

						Commons.toSingleLine(direccionAdquirente),
						Commons.toSingleLine(beneficiario),

						Commons.toSingleLine(fechaContrato),
						Commons.toSingleLine(fechaContratoNum),

						Commons.toSingleLine(fechaDeEntrega),
						Commons.toSingleLine(Commons.extraerFechaAPartirDeTexto(fechaDeEntrega)),

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
	*/

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