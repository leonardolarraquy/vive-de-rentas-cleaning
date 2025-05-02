package com.data.cleaning.main.hoolbam;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.data.cleaning.main.BaseParser;
import com.data.cleaning.main.Commons;

public class HoolBam04EscrituracionCopropiedad extends BaseParser {

	public String getTipoContrato() {
		return "Promesa compraventa-Escrituración en copropiedad";
	}

	public String getProyecto() {
		return "Hool Balam";
	}

	public String getFolderPath() {
		return "/Users/leonardo.larraquy/workspace-upwork/data-cleaning/hool-bam-promesa-escrituracion-copropiedad/";
	}

	public String getFieldsTitle() {
		return "PORC_PROPIEDAD|UNIDAD|PROPOSITO_DE_LA_INVERSION|DERECHO_DE_USO|MONTO_INVERSION|MONEDA|MONTO_APARTADO|MONTO_LIQUIDACION|PRORROGA_DE_ENTREGA|FECHA_ENTREGA|CARTA_RENDIMIENTO|FECHA_PAGO_RENDIMIENTOS_STR|FECHA_PAGO_RENDIMIENTOS|PORC_RENDIMIENTOS|MESES_RENDIMIENTOS";
	}

	public static void main(String[] args) {
		HoolBam04EscrituracionCopropiedad parser = new HoolBam04EscrituracionCopropiedad();
		parser.process();
	}

	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {
		String porcentaje           = Commons.extract(content, "porcentaje correspondiente", "de");
		String porcentajeNum        = extractParteDecimal(porcentaje);
		if(porcentajeNum.length() == 0)
			revisionManual = revisionManual + "Porcentaje Propiedad.";
		else {
			porcentajeNum = porcentajeNum + "%";
		}

		String unidad               = Commons.extract(content, "Unidad Inmobiliaria:", ".");
		unidad = unidad.replaceAll("Unidad Inmobiliaria:", "");
		if(unidad.length() > 24)
			unidad = unidad.substring(0,24);

		String unidadSimple         = extractUnidad(unidad);

		String proposito            = Commons.extract(content, "propósitos", "pero", "OBJETO");
		String derecho              = Commons.extract(content, "derecho a", ".", "OBJETO");

		String montoInversion       = Commons.extract(content, "la cantidad", ")", "OBJETO");
		String montoInversionNum    = Commons.numericValue(montoInversion);
		String moneda               = Commons.extractMoneda(montoInversion);
		if(montoInversionNum.length() == 0)
			revisionManual = revisionManual + "Contraprestacion.";

		String apartado             = Commons.extract(content, "la cantidad", ".", "entregó al");
		String montoLiquidacion     = Commons.extract(content, "la cantidad", ".", "se obliga");

		String prorrogaDeEntrega    = Commons.extract(content, "plazo", "en ", "SÉPTIMA");

		String fechaDeEntrega       = Commons.extract(content, "Fecha de entrega:", "\n").replaceAll("Fecha de entrega:", "");
		if(fechaDeEntrega.length() == 0)
			revisionManual = revisionManual + "Fecha Entrega.";					

		

		String fechaContrato      = Commons.toSingleLine(fechaContrato(content));
		String fechaContratoNum   = Commons.convertirFecha(fechaContrato);
		int    anoContrato        = 1970;

		if(fechaContrato.length() == 0 || fechaContratoNum.length() == 0) {
			revisionManual     = revisionManual + "Fecha Contrato.";
		}
		else {
			anoContrato = Integer.parseInt(fechaContratoNum.substring(fechaContratoNum.length() - 4));
		}

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

			if(fechaPagoRendimientos.indexOf("del ") >= 0)
				fechaPagoRendimientos = fechaPagoRendimientos.substring(fechaPagoRendimientos.indexOf("del ") + 3, fechaPagoRendimientos.length());

			porcRendimientos      = Commons.extract(content, "correspondiente", "%", "Rendimiento Garantizado").replaceAll("correspondiente", "").replaceAll("al", "") + "%";

			mesesRendimientos     = Commons.extract(content, "durante", "contados", "Rendimiento Garantizado").replaceAll("durante", "").replaceAll("un periodo de ", "");
			if(mesesRendimientos.indexOf("el pago") > 0)
				mesesRendimientos = mesesRendimientos.substring(0, mesesRendimientos.indexOf("el pago"));

			mesesRendimientos = mesesRendimientos.replaceAll("\\s*\\([^)]*\\)", "");
		}

		csvWriter.write("|");

		csvWriter.write(
				String.join("|",
						revisionManual, 

						Commons.toSingleLine(porcentajeNum),

						Commons.toSingleLine(unidadSimple),

						Commons.toSingleLine(proposito),
						Commons.toSingleLine(derecho),

						Commons.toSingleLine(montoInversionNum),
						Commons.toSingleLine(moneda),						
						Commons.toSingleLine(Commons.numericValue(apartado)),						
						Commons.toSingleLine(Commons.numericValue(montoLiquidacion)),

						Commons.toSingleLine(prorrogaDeEntrega),

						Commons.toSingleLine(Commons.extraerFechaAPartirDeTexto(fechaDeEntrega)),

						Commons.toSingleLine(cartaRendimiento),
						Commons.toSingleLine(fechaPagoRendimientos),
						Commons.toSingleLine(Commons.convertirFecha(Commons.toSingleLine(fechaPagoRendimientos).replaceAll("presente año", "" + anoContrato))),
						Commons.toSingleLine(porcRendimientos),
						Commons.toSingleLine(mesesRendimientos)

						));

	}

	public static String extractUnidad(String texto) {
		// Expresión regular mejorada
		String regex = "(?:Unidad|Departamento) número:\\s*([A-Z]*\\-?[0-9]+[A-Z]*)";

		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(texto);

		if (matcher.find()) 
			return matcher.group(1).trim();

		return "";
	}

	public static String extractParteDecimal(String content) {
		String regex = "\\s+([0-9]+(?:\\.[0-9]+)?)%";

		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(content);

		return matcher.find() ? matcher.group(1) : "";
	}
}