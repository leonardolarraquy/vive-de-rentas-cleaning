package com.data.cleaning.main.meridamontejo;

import java.io.BufferedWriter;
import java.io.IOException;

import com.data.cleaning.main.BaseParser;
import com.data.cleaning.main.Commons;

public class MeridaMontejo extends BaseParser {

	public String getTipoContrato()  {
		return "Promesa compraventa-Derechos fideicomisarios-Completo";
	}

	public String getProyecto() {
		return "Merida Montejo";
	}

	public String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/merida-montejo/";
	}

	public String getFieldsTitle() {
		//		return "Ubicacion|Fecha Entrega|Contraprestacion|Contraprestacion Num|Moneda|Constitucion|Terminacion|Vigencia|Unidad Inmobiliaria";
		return "N._DE_TICKETS|FASE_PROYECTO|PRECIO_TICKET|APARTADO|APARTADOESCRITO|LIQUIDACION|MONEDA|CANTIDAD_DE_MESES_SOBRE_LA_QUE_PUEDE_EJERCER_RECONOCIMIENTO_DE_INVERSION|FECHA_DE_ENTREGA|CARTA_RENTAS_ADELANTADAS|CANTIDAD_DE_MESES_DE_RENTAS_ADELANTADAS|MES_INICIO_RENTAS_ADELANTADAS|MES_PRIMER_PAGO_RENTAS_ADELANTADAS|PORCENTAJE_RENDIMIENTOS";
	}

	public static void main(String[] args) {
		MeridaMontejo parser = new MeridaMontejo();
		parser.process();
	}

	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {
		String nroTicket            = Commons.extract(content, "equivalente a", ")", "OBJETO");
		nroTicket = Commons.numericValue(nroTicket);
		if(nroTicket.length() == 0)
			revisionManual = revisionManual + "Nro Ticket.";

		String fase                 = Commons.extract(content, "(", ")", "Serie").replace("(", "");
		if(fase.length() == 0)
			revisionManual = revisionManual + "Fase.";

		String montoInversion       = Commons.extract(content, "total de", "(", "SEGUNDA").replaceAll("total de", "");
		if(montoInversion.length() == 0)
			revisionManual = revisionManual + "Contraprestacion.";

		String apartado             = Commons.extract(content, "cantidad de", "(", "entregó").replaceAll("cantidad de", "");
		String apartadoEscrito      = Commons.extract(content, "(", ")", "entregó").replace("(", "");
		String liquidacion          = Commons.extract(content, "cantidad de", "(", "entregará").replaceAll("cantidad de", "");
		String moneda               = Commons.extractMoneda(montoInversion);

		String cantidadMeses        = Commons.extract(content, "transcurridos", "meses", "SEXTA").replaceAll("transcurridos", "");

		String entrega              = Commons.extract(content, "mes", ".", "NOVENA");

		String rentasAdelantadas    = content.indexOf("Rentas Adelantadas") > 0 ? "SI" : "NO";

		String mesesAdelantadas     = "";
		String mesRentas            = "";
		String mesPrimerPago        = "";
		String porcRendimiento      = "";

		if("SI".equals(rentasAdelantadas)) {
			mesesAdelantadas        = Commons.extract(content, "durante", "(", "Rentas Adelantadas").replaceAll("durante ", "");
			mesRentas               = Commons.extract(content, "partir del", ",", "Rentas Adelantadas").replaceAll("partir del ", "");
			mesPrimerPago           = Commons.extract(content, "partir del", ".", "previamente mencionada").replaceAll("partir del ", "");
			porcRendimiento         = Commons.extract(content, "correspondiente al", "%", "Rentas Adelantadas").replaceAll("correspondiente al", "");
		}

		csvWriter.write("|");

		csvWriter.write(
				String.join("|",
						revisionManual, 

						Commons.toSingleLine(nroTicket),
						Commons.toSingleLine(fase),


						Commons.toSingleLine(montoInversion),
						Commons.toSingleLine(apartado),
						Commons.toSingleLine(apartadoEscrito),
						Commons.toSingleLine(liquidacion),
						Commons.toSingleLine(moneda),

						Commons.toSingleLine(cantidadMeses),

						Commons.toSingleLine(Commons.extraerFechaAPartirDeTexto(entrega)),

						Commons.toSingleLine(rentasAdelantadas),
						Commons.toSingleLine(Commons.numericValue(mesesAdelantadas)),
						Commons.toSingleLine(mesRentas),
						Commons.toSingleLine(mesPrimerPago),
						Commons.toSingleLine(porcRendimiento)

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