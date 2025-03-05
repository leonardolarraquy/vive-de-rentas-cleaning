package com.data.cleaning.main.vivastorage.cancun;

import java.io.BufferedWriter;
import java.io.IOException;

import com.data.cleaning.main.BaseParser;
import com.data.cleaning.main.Commons;

public class CancunDerechosFideicomisarios extends BaseParser {

	public String getTipoContrato()  {
		return "Promesa compraventa-Derechos fideicomisarios-m2";
	}

	public String getProyecto() {
		return "Vive Storage Cancún";
	}

	public String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/cancun-promesa-compra-venta-derecho-fideicomisarios/";
	}

	public String getFieldsTitle() {
//		return "Ubicacion|Propiedad|Contraprestacion|Contraprestacion Num|Moneda|Terminacion|Vigencia|Prorroga|Entrega|Fecha Entrega Num|Plazo Rendimiento Garantizado|Rentabilidad Anual|Fecha a partir que recibe rendimientos";
		return "UBICACION_PROPIEDAD|M2_MINIBODEGAS|MONTO_INVERSION|MONEDA|APARTADO|LIQUIDACION|DEVOLUCION_POR_TERMINACION_DE_CONTRATO|VIGENCIA_DE_CONTRATO|PRORROGA_DE_ENTREGA|FECHA_DE_ENTREGA|NR_MENSUALIDADES|TASA_DE_INTERES_ANUAL|FECHA_COMIENZO_RENDIMIENTOS|RENDIMIENTO_GARANTIZADO";
	}

	public static void main(String[] args) {
		CancunDerechosFideicomisarios parser = new CancunDerechosFideicomisarios();
		parser.process();
	}

	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {
		String ubicacion            = Commons.extract(content, "ubicado", "(", "SEGUNDO").replaceAll("ubicado en", "");
		if(ubicacion.length() == 0)
			revisionManual = revisionManual + "Ubicacion.";					


		String m2                   = Commons.numericValue(Commons.extract(content, "correspondientes", "ubicada", "SEGUNDO"));

		String montoInversion       = Commons.extract(content, "cantidad de", "(", "SEGUNDA");
		String montoInversionNum    = Commons.numericValue(montoInversion);
		String moneda               = Commons.extractMoneda(montoInversion);
		
		String apartado             = Commons.extract(content, "cantidad de", "(", "entrego");
		String liquidacion          = Commons.extract(content, "se obliga a pagar", "(", "SEGUNDA");

		String terminacion          = Commons.extract(content, "En caso", ".", "CUARTA");
		String vigencia             = Commons.extract(content, "estar", " a ", "SEXTA");

		String fechaDeEntrega       = Commons.extract(content, "realizar", ".", "La entrega");
		if(fechaDeEntrega.length() == 0)
			revisionManual = revisionManual + "Fecha Entrega.";					

		String prorroga             = Commons.extract(content, "prorrogarse", "en", "ENTREGA DE");

		String plazoRendimiento     = Commons.extract(content, "durante", "contados", "Al respecto");
		if(plazoRendimiento.length() == 0)
			revisionManual = revisionManual + "NR Mensualidades.";
		else {
			
			if(plazoRendimiento.indexOf("años") > 0)
				 plazoRendimiento = "" + Integer.parseInt(Commons.numericValue(plazoRendimiento)) * 12;
			else plazoRendimiento = Commons.numericValue(plazoRendimiento);
		}

		String rentabilidadAnual    = Commons.extract(content, "correspondiente", "rentabilidad", "Al respecto");
		if(rentabilidadAnual.length() > 0)
			rentabilidadAnual = Commons.extractParteDecimal(rentabilidadAnual) + "%";
					
		String aPartir              = Commons.extract(content, "partir", ".", "Al respecto").replaceAll("partir", "");
		if(aPartir.indexOf(",") > 0)
			aPartir = aPartir.substring(0, aPartir.indexOf(","));

		if(aPartir.indexOf("el pago") > 0)
			aPartir = aPartir.substring(0, aPartir.indexOf("el pago"));

    	if(aPartir.indexOf("presente") > 0) {
    		String fechaContrato = this.fechaContrato(content);
    		int ano = Integer.parseInt( fechaContrato.substring(fechaContrato.length()- 4));

    		aPartir = Commons.extraerFechaAPartirDeTexto(aPartir, ano);
    	}
    	else aPartir = Commons.convertirFecha(aPartir);

    	boolean rendimientoGarantizado = content.indexOf("rendimiento garantizado") > 0;
		
		csvWriter.write("|");

		csvWriter.write(
				String.join("|",
						revisionManual, 

						Commons.toSingleLine(ubicacion),

						Commons.toSingleLine(m2),

//						Commons.toSingleLine(montoInversion),
						Commons.toSingleLine(montoInversionNum),
						Commons.toSingleLine(moneda),
						Commons.toSingleLine(Commons.numericValue(apartado)),
						Commons.toSingleLine(Commons.numericValue(liquidacion)),

						Commons.toSingleLine(terminacion),
						Commons.toSingleLine(vigencia),
						Commons.toSingleLine(prorroga),

//						Commons.toSingleLine(fechaDeEntrega),
						Commons.toSingleLine(Commons.extraerFechaAPartirDeTexto(fechaDeEntrega)),

						Commons.toSingleLine(plazoRendimiento),
						Commons.toSingleLine(rentabilidadAnual),
						Commons.toSingleLine(aPartir),
						
						Commons.toSingleLine(rendimientoGarantizado ? "SI" : "NO")
						
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