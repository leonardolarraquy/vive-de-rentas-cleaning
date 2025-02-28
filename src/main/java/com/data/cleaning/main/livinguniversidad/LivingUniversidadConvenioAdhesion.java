package com.data.cleaning.main.livinguniversidad;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.data.cleaning.main.BaseParser;
import com.data.cleaning.main.Commons;

public class LivingUniversidadConvenioAdhesion extends BaseParser {

	public String getTipoContrato() {
		return "Convenio de adhesión";
	}

	public String getProyecto() {
		return "Living Universidad";
	}

	public String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/living-universidad-convenio-adhesion/";
	}

	public String getFieldsTitle() {
//		return "Contrato|Numero Contrato|Serie|Ubicacion|Contraprestacion|Contraprestacion Num|Moneda|Participacion|Participacion Num|Sustituto";
		return "CONTRATO|NR_FIDECOMISO|SERIE_FIDECOMISO|UBICACION_PROPIEDAD|MONTO_INVERSION|MONEDA|PORC_PROPIEDAD";
	}
	
	public static void main(String[] args) {
		LivingUniversidadConvenioAdhesion parser = new LivingUniversidadConvenioAdhesion();
		parser.process();
	}
	
	@Override
	public String getDireccionAdquirente(String content) {
		return Commons.extract(content, "FIDEICOMITENTE ADHERENTE", "FIDUCIARIA", "NOVENA").replaceAll("FIDEICOMITENTE ADHERENTE:", "");
	}

	@Override
	public String getBeneficiario(String content) {
		return Commons.extract(content, "en este acto designa", "como").replaceAll("en este acto designa", "").replaceAll("al señor ", "").replaceAll("a la señora ", "");
	}
	
	@Override
	public String fechaContrato(String content) {
		return Commons.extract(content, "de fecha", ",").replaceAll("de fecha", "");
	}


	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {
//		String tipoContrato         = Commons.extract(content, "AL CONTRATO", "IDENTIFICADO").replaceAll("AL CONTRATO DE", "");
		String numeroContrato       = Commons.extract(content, "CON EL", "DENOMINADO");

//		String serie                = Commons.extract(content, "denominada", ",", "TERCERO").replaceAll("denominada", "");
		String serie                = "SERIE C-LIVING UNIVERSIDAD";
		String ubicacion            = Commons.extract(content, "ubicado", "el cual", "TERCERO").replaceAll("ubicado en", "");

		String montoInversion       = Commons.extract(content, "la cantidad", ")", "SEGUNDA");
		if(montoInversion.length() > 0)
			montoInversion += ")";

		String montoInversionNum    = Commons.numericValue(montoInversion);
		String moneda               = Commons.extractMoneda(montoInversion);

		String participacion        = Commons.extract(content, "corresponden", "relacionados", "TERCERA").replaceAll("corresponden al", "");

		csvWriter.write("|");

		csvWriter.write(
				String.join("|",
						revisionManual, 

						Commons.toSingleLine("FIDEICOMISO IRREVOCABLE DE ADMINISTRACION"),
						Commons.toSingleLine(numeroContrato),
						Commons.toSingleLine(serie),
						Commons.toSingleLine(ubicacion),

//						Commons.toSingleLine(contraprestacion),
						Commons.toSingleLine(montoInversionNum),
						Commons.toSingleLine(moneda),

//						Commons.toSingleLine(participacion),
						Commons.toSingleLine(Commons.numericValue(participacion) + "%")
						
						));
	}

	public String getAdquiriente(String content) {
		String adherente = Commons.extract(content, "ADHERENTE", "(").replaceAll("EL SEÑOR", "").replaceAll("EL SENOR", "").replaceAll("LA SENORA", "").replaceAll("LA SEÑORA", "").replaceAll("LA SEÑORITA", "").replaceAll("ADHERENTE, ", "");

		if(adherente.indexOf(".") > 0)
			adherente = adherente.substring(0, adherente.indexOf("."));	

		if(adherente.indexOf(";") > 0)
			adherente = adherente.substring(0, adherente.indexOf(";"));	

		if(adherente.indexOf(",") > 0)
			adherente = adherente.substring(0, adherente.indexOf(","));	

		return adherente;
	}

	@Override
	public String getEnajenante(String content) {
		return "FIDEICOMISO BMI 85101677 (OCHO CINCO UNO CERO UNO SEIS SIETE SIETE) - VIVE DE LAS RENTAS";
	}

	public static String extraerEstadoCivil(String contenidoArchivo) {
		if (contenidoArchivo == null || contenidoArchivo.isEmpty()) {
			return "";
		}

		List<String> lineas = Arrays.asList(contenidoArchivo.split("\\r?\\n"));
		Set<String> estadosCiviles = new HashSet<>();
		Pattern patronEstadoCivil = Pattern.compile("(soltero|soltera|casado|casada).*?(?=[,.;])");
		// Mejorada para capturar variaciones y régimen matrimonial

		for (String linea : lineas) {
			Matcher matcher = patronEstadoCivil.matcher(linea);
			if (matcher.find()) {
				String estadoCivil = matcher.group();
				estadosCiviles.add(estadoCivil.trim());
			}
		}
		return String.join("-", estadosCiviles);
	}
}