package com.data.cleaning.main.hoolbam;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.data.cleaning.main.Commons;

public class Parser03HoolBamFinanciado {

	public static String getTipoContrato() {
		return "Promesa compraventa-Derechos fideicomisarios-Financiado";
	}
	
	public static String getProyecto() {
		return "Hool Balam";
	}
	
	public static String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/hool-bam-financiado/";
	}

	public static void main(String[] args) {
		String folderPath = getFolderPath();

		String[] rutas = Commons.readLines(folderPath + "_links");

		String csvOutputPath = folderPath + "/output.csv";

		int i = 0;

		// Crear el archivo CSV de salida
		try (BufferedWriter csvWriter = new BufferedWriter(new FileWriter(csvOutputPath))) {        	
			// Escribir encabezados en el archivo CSV
			csvWriter.write("Tipo Contrato|Nombre Proyecto|Nombre del archivo|Link a archivo|Revision Manual|Tags|ENAJENANTE|ADQUIRENTE|Clave Unica|CURP|Clave RFC|RFC|Nacionalidad|Estado Civil|Mail 1|Mail 2|Mail 3|Beneficiario|Fecha Contrato|Fecha Contrato Num|% Fraccion|% Fraccion Num|Participacion|Participacion Num|Unidad|Unidad Abrev.|Contraprestacion|Contraprestacion Num|Moneda|Apartado|Apartado Num|Enganche|Enganche Num|Primer Pago|Primer Pago Num|Nr de Mensualidades|Nr de Mensualidades Num|Monto Cuota|Monto Cuota Num|Valor a Cubrir|Valor a Cubrir Num|Valor restante|Valor restante Num|Forma de Pago|Financiamiento|Opcion de compra|Constitucion|Devolucion|Vigencia|Operacion|Plazo Meses|Domicilio Adquirente\n");

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

				String porcDerechos         = Commons.extract(content, "correspondientes", ")") + ")";
				String porcDerechosNum      = extractParteDecimal(porcDerechos);

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
				
				String participacion        = Commons.extract(content, "participación equivalente", "(");
				String participacionNum     = extractParteDecimal(participacion);

				String contraprestacion     = extractContraprestacion(content);
				String contraprestacionNum  = Commons.numericValue(contraprestacion);
				String moneda               = Commons.extractMoneda(contraprestacion);

				String apartado             = Commons.extract(content, "la cantidad", ".", "A. Previo");
				String enganche             = Commons.extract(content, "la cantidad", ".", "B. El");
				String primerPago           = Commons.extract(content, "primer pago", "hasta", "C. ");
				if(primerPago.indexOf(",") > 0)
					primerPago = primerPago.substring(0, primerPago.indexOf(","));

				if(primerPago.length() == 0)
					revisionManual = revisionManual + "Primer Pago.";

				String mensualidades        = Commons.extract(content, "el pago de", "mensualidades", "C. ");
				String valorACubrir         = Commons.extract(content, "cantidad total de", ")", "C. ");
				if(mensualidades.length() == 0)
					revisionManual = revisionManual + "Mensualidades.";
				
				String montoCuota           = Commons.extract(content, "la cantidad", "debiendo", "realizar el pago");
				if(montoCuota.length() == 0)
					revisionManual = revisionManual + "Monto Cuota.";
							
				String valorRestante        = Commons.extract(content, "restante", ")", "D. La");
				String valorRestanteNum     = Commons.numericValue(valorRestante);
				if(valorRestanteNum.length() == 0)
					revisionManual = revisionManual + "Valor Restante.";
				
				String formaDePago          = Commons.extract(content, "Primera", "\n", "TERCERA.");
				String financiamiento       = Commons.extract(content, "Segunda", "\n", "TERCERA.");
				String oferta               = Commons.extract(content, "Tercera", "\n", "TERCERA.");
								
				String constitucion         = Commons.extract(content, "La constitución", ",", "CUARTA");

				String devolucion           = Commons.extract(content, "devolverá", ".", "CUARTA");

				String vigencia             = Commons.extract(content, "vigente", ",", "SEXTA");

				String operacion            = Commons.extract(content, "se realizará", ".", "OCTAVA");
				
				String plazo                = Commons.extract(content, "plazo", "meses", "OCTAVA");

				String domicilioAdquirente  = Parser01HoolBamFractional.extractDomicilioAdquiriente(content);
				if(domicilioAdquirente.indexOf("/") > 0)
					domicilioAdquirente = domicilioAdquirente.substring(0, domicilioAdquirente.indexOf("/"));

				String fechaContrato        = Commons.extract(content, "de México a los", ".", "LEGISLACIÓN APLICABLE").replaceAll("de México a ", "");
				if(fechaContrato.indexOf("EL") > 0)
					fechaContrato = fechaContrato.substring(0, fechaContrato.indexOf("EL"));

				if(fechaContrato.length() == 0)
					fechaContrato = extractFechaContrato(content);
				
				String beneficiario       = Commons.extract(content, "a ", ",", "DÉCIMA QUINTA. BENEFICIARIO");
				if(beneficiario.length() > 0)
					beneficiario = beneficiario.substring(2, beneficiario.length());

				String fechaContratoNum     = Commons.convertirFecha(fechaContrato);

				String unidad               = Commons.extract(content, "Departamento número:", "\n");
				if(unidad.length() == 0)
					unidad                  = Commons.extract(content, "Unidad número:", "\n");
								
				String unidadSimple         = Commons.extraerUnidadAbrev(unidad);

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

						Commons.toSingleLine(porcDerechos),
						Commons.toSingleLine(porcDerechosNum),

						Commons.toSingleLine(participacion),
						Commons.toSingleLine(participacionNum),

						Commons.toSingleLine(unidad),
						Commons.toSingleLine(unidadSimple),
						
						Commons.toSingleLine(contraprestacion),
						Commons.toSingleLine(contraprestacionNum),
						Commons.toSingleLine(moneda),
						
						Commons.toSingleLine(apartado),
						Commons.toSingleLine(Commons.numericValue(apartado)),
						Commons.toSingleLine(enganche),
						Commons.toSingleLine(Commons.numericValue(enganche)),
						
						Commons.toSingleLine(primerPago),
						Commons.toSingleLine(Commons.extraerFechaAPartirDeTexto(primerPago)),

						Commons.toSingleLine(mensualidades),
						Commons.toSingleLine(Commons.numericValue(mensualidades)),

						Commons.toSingleLine(montoCuota),
						Commons.toSingleLine(Commons.numericValue(montoCuota)),

						Commons.toSingleLine(valorACubrir),
						Commons.toSingleLine(Commons.numericValue(valorACubrir)),
						Commons.toSingleLine(valorRestante),
						Commons.toSingleLine(valorRestanteNum),

						Commons.toSingleLine(formaDePago),
						Commons.toSingleLine(financiamiento),
						Commons.toSingleLine(oferta),
						
						Commons.toSingleLine(constitucion),
						Commons.toSingleLine(devolucion),

						Commons.toSingleLine(vigencia),
						Commons.toSingleLine(operacion),

						Commons.toSingleLine(plazo),

						Commons.toSingleLine(domicilioAdquirente)

						) + "\n");
			}

			System.out.println("Archivo CSV generado en: " + csvOutputPath);

		} catch (IOException e) {
			System.err.println("Ocurrió un error al procesar los archivos: " + e.getMessage());
		}
	}

	public static String extractParteDecimal(String content) {
		String regex = "\\s+([0-9]+(?:\\.[0-9]+)?)%";

		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(content);

		return matcher.find() ? matcher.group(1) : "";
	}

	public static String extractContraprestacion(String content) {
		try {
			int contraprestacion = content.indexOf("CONTRAPRESTACI");
			if(contraprestacion == -1)
				return "";

			int index = content.indexOf("cantidad", contraprestacion);
			int index2 = content.indexOf(")", index + 30);//buscar la coma despues de la coma del monto

			return content.substring(index, index2 + 2);

		}
		catch(Exception e) {

		}

		return "";
	}

	public static String extractDerecho(String content) {
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