package com.data.cleaning.main.hoolbam;

import java.io.BufferedWriter;
import java.io.IOException;

import com.data.cleaning.main.BaseParser;
import com.data.cleaning.main.Commons;

public class HoolBam06GananciaCapital extends BaseParser{

	public String getTipoContrato() {
		return "Ganancia de capital";
	}

	public String getProyecto() {
		return "Hool Balam";
	}

	public String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/hool-bam-ganancia-capital/";
	}

	@Override
	public String getTags(String content) {
		return Commons.tags(content) + "-ganancia capital";
	}
	
	@Override
	public String getAdquiriente(String content) {
		String mutuante = Commons.extract(content, "PARTE", "COMPARECE").replaceAll("PARTE", "").replaceAll("LA C.", "").replaceAll("QUIEN", "");
		if(mutuante.length() == 0)
			mutuante = Commons.extract(content, "PARTE", "POR SU").replaceAll("PARTE", "").replaceAll("LA C.", "").replaceAll("QUIEN", "");

		return mutuante;
	}
	
	public String getDireccionAdquirente(String content) {
		String str = Commons.extract(content, "su domicilio en", "/").replaceAll("su domicilio en:", "");
		if(str.indexOf("D.") > 0)
			str = str.substring(0, str.indexOf("D."));

		if(str.indexOf("C.") > 0)
			str = str.substring(0, str.indexOf("C."));

		return str;
	}


	public String getFieldsTitle() {
//		return "Contraprestacion|Contraprestacion Num|Moneda|Numero Cuotas|Numero Cuotas Num|Mensualidades|Mensualidades Num|Termino|Termino Num|Interes Moratorio|Interes Moratorio Num|Adquisicion|Adquisicion Num|Unidad|Unidad Abrev";
		return "MONTO_INVERSION|MONEDA|NR_MENSUALIDADES|CUOTA_MENSUAL|PLAZO_DEVOLUCION_CAPITAL_E_INTERESES|TASA_INTERES_MORATORIO_MENSUAL|OPCIONES_DE_SALIDA|PORC_PROPIEDAD|UNIDAD";
	}

	public static void main(String[] args) {
		HoolBam06GananciaCapital parser = new HoolBam06GananciaCapital();
		parser.process();
	}

	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {
		String montoInversion       = Commons.extract(content, "cantidad de", "(", "PRIMERA.");
		String montoInversionNum    = Commons.numericValue(montoInversion);
		String moneda               = Commons.extractMoneda(montoInversion);

		String numeroCuotas        = Commons.extract(content, "obliga a efectuar", "por");
		String mensualidades        = Commons.extract(content, "cantidad ", "en ", "SEGUNDA");
		String termino              = Commons.extract(content, "no mayor de", "contados", "SEGUNDA");

		String interes              = Commons.extract(content, "estableciéndose un", "(", "TERCERA");

		String clausulaQuinta       = Commons.extract(content, "QUINTA", "SEXTA");
		
		String porcDerechos         = Commons.extract(content, "adquisición del", "correspondiente", "QUINTA");
		String porcDerechosNum      = Commons.extractParteDecimal(porcDerechos) + "%";

		String unidad               = Commons.extract(content, "Inmobiliaria No.", "de", "QUINTA");
		if(unidad.length() == 0)
			unidad                  = Commons.extract(content, "Unidad número:", "\n");

		String unidadSimple         = unidad.replaceAll("Inmobiliaria No. ", "");

		csvWriter.write("|");

		csvWriter.write(
				String.join("|",
						revisionManual, 

//						Commons.toSingleLine(montoInversion),
						Commons.toSingleLine(montoInversionNum),
						Commons.toSingleLine(moneda),

//						Commons.toSingleLine(numeroCuotas),
						Commons.toSingleLine(Commons.numericValue(numeroCuotas)),

//						Commons.toSingleLine(mensualidades),
						Commons.toSingleLine(Commons.numericValue(mensualidades)),

//						Commons.toSingleLine(termino),
						Commons.toSingleLine(Commons.numericValue(termino)),

//						Commons.toSingleLine(interes),
						Commons.toSingleLine(Commons.numericValue(interes) + "%"),

						Commons.toSingleLine(clausulaQuinta),
						
//						Commons.toSingleLine(porcDerechos),
						Commons.toSingleLine(porcDerechosNum),


//						Commons.toSingleLine(unidad),
						Commons.toSingleLine(unidadSimple)));
	}
}