package com.data.cleaning.main.hoolbam;

import java.io.BufferedWriter;
import java.io.IOException;

import com.data.cleaning.main.BaseParser;
import com.data.cleaning.main.Commons;

public class HoolBam02Completo extends BaseParser{

	public String getTipoContrato() {
		return "Promesa compraventa-Derechos fideicomisarios-Completo";
	}

	public String getProyecto() {
		return "Hool Balam";
	}

	public String getFolderPath() {
		return "/Users/leonardo.larraquy/workspace-upwork/data-cleaning/hool-bam-completo/";
	}

	public String getFieldsTitle() {
		return "PARTICIPACION_NUM|UNIDAD|DERECHO_DE_USO|MONTO_INVERSION|MONEDA|OBLIGACIONES_ENAJENANTE|DEVOLUCION_POR_TERMINACION_DE_CONTRATO|TIEMPO_DE_ENTREGA|FECHA_DE_ENTREGA|PRORROGA_DE_ENTREGA|RENDIMIENTO_GARANTIZADO|FECHA_PAGO_RENDIMIENTOS_STR|FECHA_PAGO_RENDIMIENTOS|PORC_RENDIMIENTOS|MESES_RENDIMIENTOS";
	}

	public static void main(String[] args) {
		HoolBam02Completo parser = new HoolBam02Completo();
		parser.process();
	}

	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {
		String participacion        = Commons.extract(content, "participación equivalente", "(");
		String participacionNum     = Commons.extractParteDecimal(participacion);
		if(participacionNum.length() > 0) 
			revisionManual   = revisionManual + "Participacion.";

		String unidad               = Commons.extract(content, "Departamento número:", "\n");
		if(unidad.length() == 0)
			unidad                  = Commons.extract(content, "Unidad número:", "\n");

		String unidadSimple         = Commons.extraerUnidadAbrev(unidad);

		String derecho              = Commons.extract(content, "tendrá derecho", ".", "PRIMERA");
		if(derecho.indexOf(";") > 0)
			derecho = derecho.substring(0, derecho.indexOf(";"));

		String montoInversion       = extractContraprestacion(content);
		String montoInversionNum    = Commons.numericValue(montoInversion);
		String moneda               = Commons.extractMoneda(montoInversion);

		if(montoInversionNum.length() == 0) {
			montoInversion = "";
			revisionManual   = revisionManual + "Monto Inversion.";
			moneda           = "";
		}

		String obligacionesEnajenante = Commons.extract(content, "La constitución", ",", "CUARTA");

		String devolucion           = Commons.extract(content, "devolverá", "naturales", "CUARTA");

		String entrega              = Commons.extract(content, "La entrega de", ".", "SÉPTIMA");
		if(entrega.indexOf("de acuerdo") > 0)
			entrega = entrega.substring(0, entrega.indexOf("de acuerdo"));

		if(entrega.indexOf(",", 30) > 0)
			entrega = entrega.substring(0, entrega.indexOf(",", 30));

		String entregaNum           = Commons.extraerFechaAPartirDeTexto(entrega);



		String fechaContrato      = Commons.toSingleLine(fechaContrato(content));
		String fechaContratoNum   = Commons.convertirFecha(fechaContrato);
		int    anoContrato        = 1970;

		if(fechaContrato.length() == 0 || fechaContratoNum.length() == 0) {
			revisionManual     = revisionManual + "Fecha Contrato.";
		}
		else {
			anoContrato = Integer.parseInt(fechaContratoNum.substring(fechaContratoNum.length() - 4));
		}

		String plazo                = Commons.toSingleLine(Commons.extract(content, "plazo", "en ", "ENTREGA DEL")).replaceAll("plazo de hasta", "").replaceAll("\\s*\\([^)]*\\)", "");

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

						Commons.toSingleLine(participacionNum),

						Commons.toSingleLine(unidadSimple),

						Commons.toSingleLine(derecho),

						Commons.toSingleLine(montoInversionNum),
						Commons.toSingleLine(moneda),

						Commons.toSingleLine(obligacionesEnajenante),
						Commons.toSingleLine(devolucion),

						Commons.toSingleLine(entrega),
						Commons.toSingleLine(entregaNum),

						Commons.toSingleLine(plazo),

						Commons.toSingleLine(cartaRendimiento),
						Commons.toSingleLine(fechaPagoRendimientos),
						Commons.toSingleLine(Commons.convertirFecha(Commons.toSingleLine(fechaPagoRendimientos).replaceAll("presente año", "" + anoContrato))),
						Commons.toSingleLine(porcRendimientos),
						Commons.toSingleLine(mesesRendimientos)
						));

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
}