package com.data.cleaning.main.aurumtulum;

import java.io.BufferedWriter;
import java.io.IOException;

import com.data.cleaning.main.BaseParser;
import com.data.cleaning.main.Commons;

public class AurumTulumPlazos extends BaseParser {

	public String getTipoContrato()  {
		return "Promesa compraventa-Derechos fideicomisarios-Plazos";
	}

	public String getProyecto() {
		return "Aurum Tulum Plazos";
	}

	public String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/aurum-tulum-plazos/";
	}

	public String getFieldsTitle() {
		//		return "Ubicacion|Fecha Entrega|Contraprestacion|Contraprestacion Num|Moneda|Constitucion|Terminacion|Vigencia|Unidad Inmobiliaria";
		return "N_LOTE|N_MANZANA|SUPERFICIE|PRECIO_LOTE|PRECIO_LOTE_ESCRITO|APARTADO|ENGANCHE_RESTANTE|ENGANCHE_RESTANTE_ESCRITO|MONEDA|MES_PRIMERA_PARCIALIDAD|N_DE_PARCIALIDADES|VALOR_A_CUBRIR|MONTO_PARCIALIDADES|MONTO_PAGO_FINAL|MONTO_PAGO_FINAL_ESCRITO";
	}

	public static void main(String[] args) {
		AurumTulumPlazos parser = new AurumTulumPlazos();
		parser.process();
	}

	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {
		String manzana            = Commons.extract(content, "Manzana.", ",", "OBJETO").replaceAll("Manzana.", "");
		String lote               = Commons.extract(content, "Lote No. ", ",", "OBJETO").replaceAll("Lote No. ", "");

		String superficie         = Commons.extract(content, "superficie de", "metros", "OBJETO").replaceAll("superficie de", "");


		String montoInversion       = Commons.extract(content, "cantidad de", "(", "SEGUNDA").replaceAll("cantidad de", "");
		if(montoInversion.length() == 0)
			revisionManual = revisionManual + "Contraprestacion.";

		String montoInversionEsc    = Commons.extract(content, "(", ")", "SEGUNDA") + ")";

		//		String apartado             = Commons.extract(content, "cantidad de", "(", "entregó").replaceAll("cantidad de", "");
		//		String apartadoEscrito      = Commons.extract(content, "(", ")", "entregó").replace("(", "");

		String apartado             = Commons.extract(content, "la cantidad", ".", "entregó").replaceAll("la cantidad de", "");
		String enganche             = Commons.extract(content, "cantidad de", "(", "entregará").replaceAll("cantidad de", "");
		String engancheEsc          = Commons.extract(content, "(", ")", "entregará");
		if(engancheEsc.length() > 0)
			engancheEsc += ")";

		String primerPago           = Commons.extract(content, " mes", "hasta", "C. ");
		if(primerPago.indexOf(",") > 0)
			primerPago = primerPago.substring(0, primerPago.indexOf(","));

		if(primerPago.length() == 0)
			revisionManual = revisionManual + "Primer Pago.";

		String mensualidades        = Commons.extract(content, "el pago de", "(", "C. ").replaceAll("el pago de","");
		String valorACubrir         = Commons.extract(content, "cantidad total de", "(", "C. ").replaceAll("cantidad total de", "");
		if(mensualidades.length() == 0)
			revisionManual = revisionManual + "Mensualidades.";

		String montoCuota           = Commons.extract(content, "cantidad de", "(", "mensualidades").replaceAll("cantidad de", "");
		if(montoCuota.length() == 0)
			revisionManual = revisionManual + "Monto Cuota.";

		String totalParcialidades   = Commons.extract(content, "de ", "(", "el monto restante").replaceAll("de ", "");
		String totalParcialidadesEsc = Commons.extract(content, "(", ")", "el monto restante") + ")";

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

						Commons.toSingleLine(enganche),
						Commons.toSingleLine(engancheEsc),
						Commons.toSingleLine(moneda),

						Commons.toSingleLine(primerPago),
						Commons.toSingleLine(mensualidades),
						Commons.toSingleLine(valorACubrir),
						Commons.toSingleLine(montoCuota),
						Commons.toSingleLine(totalParcialidades),
						Commons.toSingleLine(totalParcialidadesEsc)

						));
	}

	public String getBeneficiario(String content) {
		String beneficiario = super.getBeneficiario(content);

		int index = beneficiario.indexOf("C.");
		if(index > 0)
			beneficiario = beneficiario.substring( index + 3);

		return beneficiario;
	}
}