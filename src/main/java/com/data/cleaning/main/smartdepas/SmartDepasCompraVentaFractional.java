package com.data.cleaning.main.smartdepas;

import java.io.BufferedWriter;
import java.io.IOException;

import com.data.cleaning.main.BaseParser;
import com.data.cleaning.main.Commons;

public class SmartDepasCompraVentaFractional extends BaseParser{
	
	public String getTipoContrato() {
		return "Promesa compraventa-Derechos fideicomisarios-Fractional";
	}
	
	public String getProyecto() {
		return "Smart Depas Tulum";
	}
	
	public String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/smart-depas-promesa-compraventa-fractional/";
	}
	
	public String getFieldsTitle() {
		return "Tipo de contrato|Porcentaje|Porcentaje Num|Participacion|Participacion Num|Unidad|Unidad Abrev|Contraprestacion|Contraprestacion Num|Vigencia|Entrega|Entrega Num";
	}
	public static void main(String[] args) {
		SmartDepasCompraVentaFractional parser = new SmartDepasCompraVentaFractional();
		parser.process();
	}

	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {
		String tipoDeContrato       = tipoDeContrato(content);
		if(tipoDeContrato.length() == 0)
			revisionManual = revisionManual + "Tipo Contrato.";
		
		String porcentaje           = extractPorcentaje(content);
		String porcentajeNum        = extractPorcentajeNum(porcentaje);
		if(porcentajeNum.length() == 0)
			revisionManual = revisionManual + "Porcentaje.";

		String participacion        = extractParticipacion(content);
		String participacionNum     = extractParticipacionNum(participacion);

		String unidad               = Commons.extract(content, "unidad", "(", "PRIMERA");
		if(unidad.length() > 40)
			unidad = extractUnidad(content);
		
		String unidadAbrev          = Commons.extraerUnidadAbrev(unidad);

		String contraprestacion     = Commons.extract(content, "la cantidad", ")", "SEGUNDA") + ")";
		String contraprestacionNum  = Commons.numericValue(contraprestacion);
		if(contraprestacionNum.length() == 0)
			revisionManual = revisionManual + "Contraprestacion.";

		String vigencia             = extractVigencia(content);
		if(vigencia.length() == 0)
			revisionManual = revisionManual + "Vigencia.";
		
		String entrega              = extractEntrega(content);
		String entregaNum           = Commons.extraerFechaAPartirDeTexto(Commons.toSingleLine(entrega));
	    if(entregaNum == null || entregaNum.length() == 0)
			revisionManual = revisionManual + "Fecha Entrega.";

		csvWriter.write("|");

		csvWriter.write(
				String.join("|",
						revisionManual, 

						Commons.toSingleLine(tipoDeContrato),
						Commons.toSingleLine(porcentaje),
						Commons.toSingleLine(porcentajeNum),

						Commons.toSingleLine(participacion),
						Commons.toSingleLine(participacionNum),
						
						Commons.toSingleLine(unidad),
						Commons.toSingleLine(unidadAbrev),
						
						Commons.toSingleLine(contraprestacion),
						Commons.toSingleLine(contraprestacionNum),

						Commons.toSingleLine(vigencia),

						Commons.toSingleLine(entrega),
						Commons.toSingleLine(entregaNum)));

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
			csvWriter.write("Tipo Contrato|Nombre Proyecto|Nombre del archivo|Link a archivo|Revision Manual|Tags|ENAJENANTE|ADQUIRENTE|Clave Unica|CURP|Clave RFC|RFC|Nacionalidad|Estado Civil|Mail 1|Mail 2|Mail 3|Tipo de contrato|Porcentaje|Porcentaje Num|Participacion|Participacion Num|Unidad|Unidad Abrev|Contraprestacion|Contraprestacion Num|Vigencia|Entrega|Entrega Num|Beneficiario|Fecha Contrato|Fecha Numerica\n");

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

				
				String tipoDeContrato       = tipoDeContrato(content);
				if(tipoDeContrato.length() == 0)
					revisionManual = "Tipo Contrato.";
				
				String porcentaje           = extractPorcentaje(content);
				String porcentajeNum        = extractPorcentajeNum(porcentaje);
				if(porcentajeNum.length() == 0)
					revisionManual = revisionManual + "Porcentaje.";

				String participacion        = extractParticipacion(content);
				String participacionNum     = extractParticipacionNum(participacion);

				String unidad               = Commons.extract(content, "unidad", "(", "PRIMERA");
				if(unidad.length() > 40)
					unidad = ParserSmartDepasCompraVentaCompleto.extractUnidad(content);
				
				String unidadAbrev          = Commons.extraerUnidadAbrev(unidad);

				String contraprestacion     = Commons.extract(content, "la cantidad", ")", "SEGUNDA") + ")";
				String contraprestacionNum  = Commons.numericValue(contraprestacion);
				if(contraprestacionNum.length() == 0)
					revisionManual = revisionManual + "Contraprestacion.";

				String vigencia             = extractVigencia(content);
				if(vigencia.length() == 0)
					revisionManual = revisionManual + "Vigencia.";
				
				String entrega              = extractEntrega(content);
				String entregaNum           = Commons.extraerFechaAPartirDeTexto(Commons.toSingleLine(entrega));
			    if(entregaNum == null || entregaNum.length() == 0)
					revisionManual = revisionManual + "Fecha Entrega.";
				
				String beneficiario       = Commons.extract(content, " a ", "llevando", ". BENEFICIARIO");
				if(beneficiario.length() > 0)
					beneficiario = beneficiario.substring(2, beneficiario.length());
				
				if(beneficiario.indexOf(",") > 0)
					beneficiario = beneficiario.substring(0, beneficiario.indexOf(","));

				String fechaContrato        = extractFechaContrato(content);
				String fechaContratoNum     = Commons.convertirFecha(fechaContrato);

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

						Commons.toSingleLine(tipoDeContrato),
						Commons.toSingleLine(porcentaje),
						Commons.toSingleLine(porcentajeNum),

						Commons.toSingleLine(participacion),
						Commons.toSingleLine(participacionNum),
						
						Commons.toSingleLine(unidad),
						Commons.toSingleLine(unidadAbrev),
						
						Commons.toSingleLine(contraprestacion),
						Commons.toSingleLine(contraprestacionNum),

						Commons.toSingleLine(vigencia),

						Commons.toSingleLine(entrega),
						Commons.toSingleLine(entregaNum),
						Commons.toSingleLine(beneficiario),
						
						Commons.toSingleLine(fechaContrato),
						Commons.toSingleLine(fechaContratoNum)

						) + "\n");
				csvWriter.flush();
			}

			System.out.println("Archivo CSV generado en: " + csvOutputPath);

		} catch (IOException e) {
			System.err.println("Ocurrió un error al procesar los archivos: " + e.getMessage());
		}
	}
	*/

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
	
	public static String extractFechaContrato(String texto) {
		try {

			int index  = texto.indexOf("día", texto.indexOf("firman de conformidad"));
			int index2 = texto.indexOf("EL", index);
			
			int index3 = texto.indexOf(".", index);
			if(index3 != -1 && index3 < index2)
				index2 = index3;

			return texto.substring(index - 4, index2);
		}
		catch(Exception e) {}

		return "";
	}
	public static String extractVigencia(String content) {
		try {
			int index = content.indexOf("estará", content.indexOf("SEXTA. "));
			int index2 = content.indexOf(",", index);
						
			return content.substring(index, index2).replaceAll("“", "").replaceAll("”", "");
			
		}
		catch(Exception e) {}

		return "";
	}

	public static String extractEntrega(String content) {
		try {
			int base = content.indexOf("ENTREGA DEL");
			if(base == -1)
				return "";
			
			int index = content.indexOf("se realizar", base);
			int index2 = content.indexOf(".", base);//buscar la coma despues de la coma del monto
			
			int index3 = content.indexOf("La ", base + 30);
			if(index3 != -1 && index3 < index2)
				index2 = index3;
					
			String res = content.substring(index, index2);
			if(res.indexOf(",") > 0)
				res = res.substring(0, res.indexOf(","));
				
			return res;
			
		}
		catch(Exception e) {}

		return "";
	}

	public static String extractParticipacionNum(String content) {
		try {
			int index = content.indexOf("un ");
			int index2 = content.indexOf("(");
			
			return content.substring(index + 3, index2);
			
		}
		catch(Exception e) {

		}

		return "";
	}

	public static String extractPorcentajeNum(String content) {
		try {
			int index = content.indexOf("al ");
			int index2 = content.indexOf("(");
			
			return content.substring(index + 3, index2);
			
		}
		catch(Exception e) {

		}

		return "";
	}

	public static String extractPorcentaje(String content) {
		try {
			int index = content.indexOf("correspondientes al", content.indexOf("PRIMERO. "));
			int index2 = content.indexOf(") ", content.indexOf(")", index) + 10);
			
			return content.substring(index, index2 + 1);
			
		}
		catch(Exception e) {

		}

		return "";
	}

	public static String tipoDeContrato(String content) {
		try {
			int index = content.indexOf("celebrar un");
			int index2 = content.indexOf("corresp", index) - 1;

			int index3 = content.indexOf(",", index);
			if(index3 != -1 && index3 < index2)
				index2 = index3;

			return content.substring(index, index2).replaceAll("celebrar un", "") + " - FRACTIONAL";
			
		}
		catch(Exception e) {

		}

		return "";
	}

	public static String extractParticipacion(String content) {
		try {
			int index = content.indexOf("equivalente", content.indexOf("PRIMERA. "));
			int index2 = content.indexOf("correspondientes", index);
			
			return content.substring(index, index2);
			
		}
		catch(Exception e) {

		}

		return "";
	}
}