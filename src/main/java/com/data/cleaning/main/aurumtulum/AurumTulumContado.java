package com.data.cleaning.main.aurumtulum;

import java.io.BufferedWriter;
import java.io.IOException;

import com.data.cleaning.main.BaseParser;
import com.data.cleaning.main.Commons;

public class AurumTulumContado extends BaseParser {

	public String getTipoContrato()  {
		return "Promesa compraventa-Derechos fideicomisarios-Completo";
	}

	public String getProyecto() {
		return "Aurum Tulum Contado";
	}

	public String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/aurum-tulum-contado/";
	}

	public String getFieldsTitle() {
		//		return "Ubicacion|Fecha Entrega|Contraprestacion|Contraprestacion Num|Moneda|Constitucion|Terminacion|Vigencia|Unidad Inmobiliaria";
		return "N_LOTE|N_MANZANA|SUPERFICIE|PRECIO_LOTE|PRECIO_LOTE_ESCRITO|APARTADO|MONTO_PAGO_FINAL|MONTO_PAGO_FINAL_ESCRITO|MONEDA";
	}

	public static void main(String[] args) {
		AurumTulumContado parser = new AurumTulumContado();
		parser.process();
	}

	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {
		String lote               = Commons.extract(content, "Manzana.", ",", "OBJETO").replaceAll("Manzana.", "");
		String manzana            = Commons.extract(content, "Lote No. ", ",", "OBJETO").replaceAll("Lote No. ", "");

		String superficie         = Commons.extract(content, "superficie de", "metros", "OBJETO").replaceAll("superficie de", "");


		String montoInversion       = Commons.extract(content, "cantidad de", "(", "SEGUNDA").replaceAll("cantidad de", "");
		if(montoInversion.length() == 0)
			revisionManual = revisionManual + "Contraprestacion.";
		
		String montoInversionEsc    = Commons.extract(content, "(", ")", "SEGUNDA") + ")";

		String apartado             = Commons.extract(content, "cantidad de", "(", "entregó").replaceAll("cantidad de", "");
		//		String apartadoEscrito      = Commons.extract(content, "(", ")", "entregó").replace("(", "");
		
		String liquidacion          = Commons.extract(content, "cantidad de", "(", "entregará").replaceAll("cantidad de", "");
		String liquidacionEsc       = Commons.extract(content, "(", ")", "entregará") + ")";

		String moneda               = Commons.extractMoneda(montoInversion);

		csvWriter.write("|");

		csvWriter.write(
				String.join("|",
						revisionManual, 

						Commons.toSingleLine(lote),
						Commons.toSingleLine(manzana),
						Commons.toSingleLine(superficie),

						Commons.toSingleLine(montoInversion),
						Commons.toSingleLine(montoInversionEsc),
						
						Commons.toSingleLine(apartado),
						//						Commons.toSingleLine(apartadoEscrito),
						
						Commons.toSingleLine(liquidacion),
						Commons.toSingleLine(liquidacionEsc),
						Commons.toSingleLine(moneda)

						));
	}

	public String getBeneficiario(String content) {
		String beneficiario = super.getBeneficiario(content);

		int index = beneficiario.indexOf("C. ");
		if(index > 0)
			beneficiario = beneficiario.substring( index + 3);

		return beneficiario;
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

}