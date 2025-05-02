package com.data.cleaning.main.hoolbam;

import java.io.BufferedWriter;
import java.io.IOException;

import com.data.cleaning.main.BaseParser;
import com.data.cleaning.main.Commons;

public class HoolBam07DerechosFideicomisarios extends BaseParser {

	public String getTipoContrato() {
		return "Promesa compraventa-Derechos fideicomisarios-Torre 2";
	}

	public String getProyecto() {
		return "Hool Balam";
	}

	public  String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/hool-bam-promesa-compra-venta-derecho-fideicomisarios/old/";
	}

	public String getFieldsTitle() {
		return "TORRE|PORC_PROPIEDAD|MONTO_INVERSION|MONEDA|MONTO_APARTADO|MONTO_LIQUIDACION|OBLIGACIONES_ENAJENANTE|VIGENCIA_DE_CONTRATO|PRORROGA_DE_ENTREGA|FECHA_ENTREGA|CARTA_RENDIMIENTO|FECHA_PAGO_RENDIMIENTOS|PORC_RENDIMIENTOS|MESES_RENDIMIENTOS|CARTA_GARANTIA|EQUITY_INSTANTANEO|MONTO_EQUITY";
	}

	public static void main(String[] args) {
		HoolBam07DerechosFideicomisarios parser = new HoolBam07DerechosFideicomisarios();
		parser.process();
	}

	public String getDireccionAdquirente(String content) {
		String direccion  = Commons.extract(content, "manifiesta tener su domicilio", "mismos", "con la clave").replaceAll("manifiesta tener su domicilio en:", "");
		if(direccion.length() == 0) {
			direccion  = Commons.extract(content, "ADQUIRENTE", "EL \u201C", "CUARTA");
			if(direccion.length() > 13)
				direccion = direccion.substring(13, direccion.length());

		}
		if(direccion.indexOf("QUINTA") > 0)
			direccion= direccion.substring(0, direccion.indexOf("QUINTA"));

		return direccion;
	}


	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {
		String torre                = Commons.extract(content, "UBICADOS EN", "DEL", "EXCLUSIVA DEPARTAMENTOS").replaceAll("UBICADOS EN", "");
		if(torre.length() == 0)
			revisionManual = revisionManual + "Torre.";					

		String participacion        = Commons.extract(content, "equivalente a", "sobre", "PRIMERA");

		String montoInversion       = Commons.extract(content, "cantidad de", "(", "PRIMERA.");
		String montoInversionNum    = Commons.numericValue(montoInversion);
		String moneda               = Commons.extractMoneda(montoInversion);

		String apartado             = Commons.extract(content, "cantidad de", "por", "Previo a");
		String liquidacion          = Commons.extract(content, "valor de", "misma", "se obliga");

		String constitucion         = Commons.extract(content, "La", "que", "CUARTA");
		String vigencia             = Commons.extract(content, "estará", ",", "SEXTA");
		String plazo                = Commons.extract(content, "prorrogarse", "en", "ENTREGA DEL");

		String unidad               = Commons.extract(content, "Inmobiliaria No.", "de", "QUINTA");
		if(unidad.length() == 0)
			unidad                  = Commons.extract(content, "Unidad número:", "\n");

		String fechaDeEntrega       = Commons.extract(content, "Fecha de entrega:", "\n");
		if(fechaDeEntrega.length() == 0)
			revisionManual = revisionManual + "Fecha Entrega.";					

		String cartaRendimiento      = "NO";
		String fechaPagoRendimientos = "";
		String porcRendimientos      = "";
		String mesesRendimientos     = "";

		if(content.indexOf("Rendimiento Garantizado") > 0) {
			cartaRendimiento = "SI";

			fechaPagoRendimientos = Commons.extract(content, "mencionada", ".", "Rendimiento Garantizado");
			if(fechaPagoRendimientos.indexOf("partir") > 0)
				fechaPagoRendimientos = fechaPagoRendimientos.substring(fechaPagoRendimientos.indexOf("partir") + 7, fechaPagoRendimientos.length());

			if(fechaPagoRendimientos.indexOf(",") > 0)
				fechaPagoRendimientos = fechaPagoRendimientos.substring(0, fechaPagoRendimientos.indexOf(","));

			porcRendimientos      = Commons.extract(content, "correspondiente", "%", "Rendimiento Garantizado").replaceAll("correspondiente", "").replaceAll("al", "");
			if(porcRendimientos.length() > 0) 
				porcRendimientos+= "%";

			mesesRendimientos     = Commons.extract(content, "durante", "contados", "Rendimiento Garantizado").replaceAll("durante", "").replaceAll("un periodo de ", "");
			if(mesesRendimientos.indexOf("el pago") > 0)
				mesesRendimientos = mesesRendimientos.substring(0, mesesRendimientos.indexOf("el pago"));

			if(mesesRendimientos.indexOf(",") > 0)
				mesesRendimientos = mesesRendimientos.substring(0, mesesRendimientos.indexOf(","));			
		}

		String cartaGarantia = "NO";
		String equity        = "";
		String montoEquity   = "";
		
		if(content.indexOf("Referencia: Carta Garantía") > 0) {
			cartaGarantia = "SI";
			
			equity      = Commons.extract(content, "reconocido en un", "(", "Referencia: Carta Garantía").replaceAll("reconocido en un", "");
			montoEquity = Commons.extract(content, "es decir", "adicionales", "Referencia: Carta Garantía").replaceAll("es decir", "");
		}
		
		csvWriter.write("|");

		csvWriter.write(
				String.join("|",
						revisionManual, 

						Commons.toSingleLine(torre),

						Commons.toSingleLine(Commons.numericValue(participacion) + "%"),

						Commons.toSingleLine(montoInversionNum),
						Commons.toSingleLine(moneda),
						Commons.toSingleLine(Commons.numericValue(apartado)),
						Commons.toSingleLine(Commons.numericValue(liquidacion)),

						Commons.toSingleLine(constitucion),
						Commons.toSingleLine(vigencia),
						Commons.toSingleLine(plazo),

						Commons.toSingleLine(Commons.extraerFechaAPartirDeTexto(fechaDeEntrega)),
						
						Commons.toSingleLine(cartaRendimiento),
						Commons.toSingleLine(fechaPagoRendimientos),
						Commons.toSingleLine(porcRendimientos),
						Commons.toSingleLine(mesesRendimientos),
						
						Commons.toSingleLine(cartaGarantia),
						Commons.toSingleLine(equity),
						Commons.toSingleLine(montoEquity)

						));

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