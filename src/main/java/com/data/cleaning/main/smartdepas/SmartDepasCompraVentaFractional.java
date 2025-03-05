package com.data.cleaning.main.smartdepas;

import java.io.BufferedWriter;
import java.io.IOException;

import com.data.cleaning.main.BaseParser;
import com.data.cleaning.main.Commons;

public class SmartDepasCompraVentaFractional extends BaseParser{
	
	public String getTipoContrato() {
		return "Promesa compraventa-Derechos fideicomisarios-Fractional";
	}
	
	public String getProyecto() {
		return "Smart Depas Tulum";
	}
	
	public String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/smart-depas-promesa-compraventa-fractional/";
	}
	
	public String getFieldsTitle() {
		return "TIPO_DE_CONTRATO|PORC_PROPIEDAD|UNIDAD|MONTO_INVERSION|VIGENCIA_DE_CONTRATO|FECHA_ENTREGA|CARTA_GARANTIA|PERIODO_PAGO_RENDIMIENTOS_GARANTIZADOS|FECHA_INICIO_COMPUTO_RENDIMIENTOS|RENDIMIENTO_GARANTIZADO_TASA_ANUAL|FECHA_COMIENZO_PAGO_RENDIMIENTOS|GARANTIA_DE_RECOMPRA";
	}
	public static void main(String[] args) {
		SmartDepasCompraVentaFractional parser = new SmartDepasCompraVentaFractional();
		parser.process();
	}

	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {
		String tipoDeContrato       = tipoDeContrato(content);
		if(tipoDeContrato.length() == 0)
			revisionManual = revisionManual + "Tipo Contrato.";
		
		String porcentaje           = Commons.extract(content, "equivalente", ")", "OBJETO");
		String porcentajeNum        = Commons.extractParteDecimal(porcentaje);
		if(porcentajeNum.length() == 0)
			 revisionManual = revisionManual + "Porcentaje.";
		else porcentajeNum += "%";

//		String participacion        = extractParticipacion(content);
//		String participacionNum     = extractParticipacionNum(participacion);

		String unidad               = Commons.extract(content, "unidad", "(", "PRIMERA");
		if(unidad.length() > 40)
			unidad = extractUnidad(content);
		
		String unidadAbrev          = Commons.extraerUnidadAbrev(unidad);

		String montoInversion       = Commons.extract(content, "la cantidad", ")", "SEGUNDA") + ")";
		String montoInversionNum    = Commons.numericValue(montoInversion);
		if(montoInversionNum.length() == 0)
			revisionManual = revisionManual + "Contraprestacion.";

		String vigencia             = extractVigencia(content);
		if(vigencia.length() == 0)
			revisionManual = revisionManual + "Vigencia.";
		
		String entrega              = extractEntrega(content);
		String entregaNum           = Commons.extraerFechaAPartirDeTexto(Commons.toSingleLine(entrega));
	    if(entregaNum == null || entregaNum.length() == 0)
			revisionManual = revisionManual + "Fecha Entrega.";
	    
	    boolean cartaGarantia        = content.indexOf("Referencia: Rendimiento") > 0;

	    String periodoPago         = "";
	    String inicioComputo       = "";
	    String rendimiento         = "";
	    String fechaComienzoPago   = "";
	    String garantiaRecompra    = "";
	    
	    if(cartaGarantia) {
	    	periodoPago  = Commons.extract(content, "durante", "contados", "Referencia: Rendimiento");
	    	
	    	inicioComputo = Commons.toSingleLine(Commons.extract(content, "partir", ",", "Referencia: Rendimiento").replaceAll("partir", ""));
	    	if(inicioComputo.indexOf("el pago") > 0)
	    		inicioComputo = inicioComputo.substring(0, inicioComputo.indexOf("el pago"));

	    	if(inicioComputo.indexOf(".") > 0)
	    		inicioComputo = inicioComputo.substring(0, inicioComputo.indexOf("."));

	    	if(inicioComputo.indexOf("presente") > 0) {
	    		String fechaContrato = this.fechaContrato(content);
	    		int ano = Integer.parseInt( fechaContrato.substring(fechaContrato.length()- 4));

		    	inicioComputo = Commons.extraerFechaAPartirDeTexto(inicioComputo, ano);
	    	}
	    	else inicioComputo = Commons.convertirFecha(inicioComputo);
	    		
	    	
	    	rendimiento  = Commons.extract(content, "correspondiente", ")", "Referencia: Rendimiento");
	    	rendimiento  = Commons.numericValue(rendimiento) + "%";
	    	
	    	fechaComienzoPago  = Commons.toSingleLine(Commons.extract(content, "mencionada", ".", "Referencia: Rendimiento"));
	    	fechaComienzoPago  = fechaComienzoPago.substring(fechaComienzoPago.indexOf("del"), fechaComienzoPago.length());
	    	
	    	if(fechaComienzoPago.indexOf(",") > 0)
	    		fechaComienzoPago = fechaComienzoPago.substring(0, fechaComienzoPago.indexOf(","));
	    	
	    	if(fechaComienzoPago.indexOf("En") > 0)
	    		fechaComienzoPago = fechaComienzoPago.substring(0, fechaComienzoPago.indexOf("En"));

	    	if(fechaComienzoPago.indexOf("Una") > 0)
	    		fechaComienzoPago = fechaComienzoPago.substring(0, fechaComienzoPago.indexOf("Una"));


	    	if(fechaComienzoPago.indexOf("presente") > 0) {
	    		String fechaContrato = this.fechaContrato(content);
	    		int ano = Integer.parseInt( fechaContrato.substring(fechaContrato.length()- 4));

	    		fechaComienzoPago = Commons.extraerFechaAPartirDeTexto(fechaComienzoPago, ano);
	    	}
	    	else fechaComienzoPago = Commons.convertirFecha(fechaComienzoPago);

	    	
	    	garantiaRecompra  = Commons.extract(content, "se compromete", ".", "Referencia: Rendimiento");
	    }
	    
		csvWriter.write("|");

		csvWriter.write(
				String.join("|",
						revisionManual, 

						Commons.toSingleLine(tipoDeContrato),

//						Commons.toSingleLine(porcentaje),
						Commons.toSingleLine(porcentajeNum),

//						Commons.toSingleLine(participacion),
//						Commons.toSingleLine(participacionNum),
						
//						Commons.toSingleLine(unidad),
						Commons.toSingleLine(unidadAbrev),
						
//						Commons.toSingleLine(montoInversion),
						Commons.toSingleLine(montoInversionNum),

						Commons.toSingleLine(vigencia),

//						Commons.toSingleLine(entrega),
						Commons.toSingleLine(entregaNum),
						
						Commons.toSingleLine(cartaGarantia ? "SI" : "NO"),
						
						Commons.toSingleLine(periodoPago),
						Commons.toSingleLine(inicioComputo),
						Commons.toSingleLine(rendimiento),
						Commons.toSingleLine(fechaComienzoPago),
						Commons.toSingleLine(garantiaRecompra)
						
						));

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
	
	public static String extractFechaContrato(String texto) {
		try {

			int index  = texto.indexOf("día", texto.indexOf("firman de conformidad"));
			int index2 = texto.indexOf("EL", index);
			
			int index3 = texto.indexOf(".", index);
			if(index3 != -1 && index3 < index2)
				index2 = index3;

			return texto.substring(index - 4, index2);
		}
		catch(Exception e) {}

		return "";
	}
	public static String extractVigencia(String content) {
		try {
			int index = content.indexOf("estará", content.indexOf("SEXTA. "));
			int index2 = content.indexOf(",", index);
						
			return content.substring(index, index2).replaceAll("“", "").replaceAll("”", "");
			
		}
		catch(Exception e) {}

		return "";
	}

	public static String extractEntrega(String content) {
		try {
			int base = content.indexOf("ENTREGA DEL");
			if(base == -1)
				return "";
			
			int index = content.indexOf("se realizar", base);
			int index2 = content.indexOf(".", base);//buscar la coma despues de la coma del monto
			
			int index3 = content.indexOf("La ", base + 30);
			if(index3 != -1 && index3 < index2)
				index2 = index3;
					
			String res = content.substring(index, index2);
			if(res.indexOf(",") > 0)
				res = res.substring(0, res.indexOf(","));
				
			return res;
			
		}
		catch(Exception e) {}

		return "";
	}

	public static String extractParticipacionNum(String content) {
		try {
			int index = content.indexOf("un ");
			int index2 = content.indexOf("(");
			
			return content.substring(index + 3, index2);
			
		}
		catch(Exception e) {

		}

		return "";
	}

	public static String tipoDeContrato(String content) {
		try {
			int index = content.indexOf("celebrar un");
			int index2 = content.indexOf("corresp", index) - 1;

			int index3 = content.indexOf(",", index);
			if(index3 != -1 && index3 < index2)
				index2 = index3;

			return content.substring(index, index2).replaceAll("celebrar un", "") + " - FRACTIONAL";
			
		}
		catch(Exception e) {

		}

		return "";
	}

	public static String extractParticipacion(String content) {
		try {
			int index = content.indexOf("equivalente", content.indexOf("PRIMERA. "));
			int index2 = content.indexOf("correspondientes", index);
			
			return content.substring(index, index2);
			
		}
		catch(Exception e) {

		}

		return "";
	}
}