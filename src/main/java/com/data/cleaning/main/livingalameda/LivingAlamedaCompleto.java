package com.data.cleaning.main.livingalameda;

import java.io.BufferedWriter;
import java.io.IOException;

import com.data.cleaning.main.BaseParser;
import com.data.cleaning.main.Commons;

public class LivingAlamedaCompleto extends BaseParser {
	
	public String getTipoContrato() {
		return "Promesa compraventa-Derechos fideicomisarios-Completo";
	}
	
	public String getProyecto() {
		return "Living Alameda";
	}
	
	public String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/living-alameda-completo/";
	}

	public String getFieldsTitle() {
//		return "Unidad|Unidad Abrev.|Tipo Contrato|Contraprestacion|Contraprestacion Num|Moneda|Apartado|Apartado Num|Liquidacion|Liquidacion Num|Vigencia|Entrega|Entrega Num|Prorroga|Unidad Anexo";
		return "UNIDAD|TIPO_DE_CONTRATO|MONTO_INVERSION|MONEDA|MONTO_APARTADO|MONTO_LIQUIDACION|VIGENCIA_DE_CONTRATO|FECHA_DE_ENTREGA|PRORROGA_DE_ENTREGA";
	}

	public static void main(String[] args) {
		LivingAlamedaCompleto parser = new LivingAlamedaCompleto();
		parser.process();
	}

	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {
//		String unidad               = Commons.extract(content, "Unidad", "Incluye", "PRIMERA");
//		if(unidad.indexOf("(") > 0)
//			unidad = unidad.substring(0, unidad.indexOf("(") - 1);
		
		String unidadAnexo          = Commons.extract(content, "Unidad Inmobiliaria:", "Incluye");
		if(unidadAnexo.length() == 0)
			unidadAnexo          = Commons.extract(content, "Unidad Inmobiliaria:", "Caract");
		
		unidadAnexo = unidadAnexo.replaceAll("Unidad Inmobiliaria: ", "");				
		unidadAnexo = unidadAnexo.replaceAll("- ", "");				

//		String unidadAbrev          = Commons.extraerUnidadAbrev(unidadAnexo);				
//		if(unidadAbrev.length() == 0)
//			revisionManual = revisionManual + "Unidad.";
		
		String tipoContrato         = Commons.extract(content, "mediante", "sobre", "PRIMERA.");
		if(tipoContrato.indexOf(".") > 0)
			tipoContrato = tipoContrato.substring(0, tipoContrato.indexOf("."));
		

		String montoInversion       = Commons.extract(content, "la cantidad", ")", "SEGUNDA") + ")";
		String montoInversionNum    = Commons.numericValue(montoInversion);
		String moneda               = Commons.extractMoneda(montoInversion);

		String apartado             = Commons.extract(content, "comprende", ")", "SEGUNDA");
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


		String vigencia             = Commons.extract(content, "vigente", ",", "SEXTA");
		
		String entrega              = Commons.extract(content, "entrega", ".", "ENTREGA");
		
		String prorroga             = Commons.extract(content, "prorrogarse", " en ", "ENTREGA");


		String beneficiario         = Commons.extract(content, "C. ", ",", "BENEFICIARIO");
		if(beneficiario.length() > 0)
			beneficiario = beneficiario.substring(2, beneficiario.length());
		
		csvWriter.write("|");
		
		csvWriter.write(
				String.join("|",
						revisionManual, 

//						Commons.toSingleLine(unidad),
						Commons.toSingleLine(unidadAnexo),

						Commons.toSingleLine(tipoContrato),

//						Commons.toSingleLine(montoInversion),
						Commons.toSingleLine(montoInversionNum),
						Commons.toSingleLine(moneda),

//						Commons.toSingleLine(apartado),
						Commons.toSingleLine(apartadoNum),
						
//						Commons.toSingleLine(liquidacion),
						Commons.toSingleLine(liquidacionNum),

						Commons.toSingleLine(vigencia),
						
//						Commons.toSingleLine(entrega),
						Commons.toSingleLine(Commons.extraerFechaAPartirDeTexto(entrega)),
						Commons.toSingleLine(prorroga)
												
						));

	}
}