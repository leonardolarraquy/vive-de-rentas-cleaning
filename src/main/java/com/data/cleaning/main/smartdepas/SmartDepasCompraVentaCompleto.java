package com.data.cleaning.main.smartdepas;

import java.io.BufferedWriter;
import java.io.IOException;

import com.data.cleaning.main.BaseParser;
import com.data.cleaning.main.Commons;

public class SmartDepasCompraVentaCompleto extends BaseParser{

	public String getTipoContrato() {
		return "Promesa compraventa-Derechos fideicomisarios-Completo";
	}

	public String getProyecto() {
		return "Smart Depas Tulum";
	}

	public String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/smart-depas-promesa-compraventa-completo/";
	}

	public String getFieldsTitle() {
		return "Objeto|Contraprestacion|Contraprestacion Num|Moneda|Clausulas|Vigencia|Entrega|Entrega Num|Prorroga|Unidad|Unidad Abreviada|Forma de Pago";
	}

	public static void main(String[] args) {
		SmartDepasCompraVentaCompleto parser = new SmartDepasCompraVentaCompleto();
		parser.process();
	}

	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {
		String objeto               = extractObjeto(content);
		if(objeto == null || objeto.length() == 0)
			revisionManual = revisionManual + "Objeto.";

		String contraprestacion     = extractContraprestacion(content);
		if(contraprestacion == null || contraprestacion.length() == 0)
			revisionManual = revisionManual + "Contraprestacion.";

		String contraprestacionNum  = Commons.numericValue(contraprestacion);
		String moneda               = Commons.extractMoneda(contraprestacion);

		String obligacion           = extractObligacion(content);
		String vigencia             = extractVigencia(content);
		String entrega              = extractEntrega(content);
		if(entrega.indexOf("La ") > 0)
			entrega = entrega.substring(0, entrega.indexOf("La "));

		String entregaNum           = Commons.extraerFechaAPartirDeTexto(entrega.replaceAll("Fecha de entrega:", ""));
		if(entregaNum == null || entregaNum.length() == 0)
			revisionManual = revisionManual + "Fecha Entrega.";

		String prorroga             = extractProrroga(content);

		String unidad               = extractUnidad(content);
		String unidadAbrev          = Commons.extraerUnidadAbrev(unidad);

		if(unidadAbrev.length() == 0)
			revisionManual = revisionManual + "Unidad.";

		String formaDePago          = extractFormaDePago(content);

		csvWriter.write("|");

		csvWriter.write(
				String.join("|",
						revisionManual, 

						Commons.toSingleLine(objeto),

						Commons.toSingleLine(contraprestacion),
						Commons.toSingleLine(contraprestacionNum),
						Commons.toSingleLine(moneda),

						Commons.toSingleLine(obligacion),
						Commons.toSingleLine(vigencia),
						Commons.toSingleLine(entrega),
						Commons.toSingleLine(entregaNum),
						Commons.toSingleLine(prorroga),

						Commons.toSingleLine(unidad),
						Commons.toSingleLine(unidadAbrev),

						Commons.toSingleLine(formaDePago)));

	}

	public static String extractFormaDePago(String texto) {
		try {
			int index = texto.indexOf("Forma de Pago:");
			int index2 = texto.indexOf("EL", index);

			int index3 = texto.indexOf(".", index);
			if(index3 != -1 && index3 < index2)
				index2 = index3;

			index3 = texto.indexOf("\n", index + 20);
			if(index3 != -1 && index3 < index2)
				index2 = index3;

			return texto.substring(index, index2);

		}
		catch(Exception e) {}

		return "";
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

	public static String extractDomicilioAdquiriente(String texto) {
		try {
			int index = texto.indexOf("ADQUIRENTE", texto.indexOf("NOVENA. ")) + 12;

			int index2 = texto.indexOf("Cualquiera ", index);

			int index3 = texto.indexOf("México", index);
			if(index3 != -1 && index3 < index2)
				index2 = index3 + 6;

			index3 = texto.indexOf("DÉCIMA", index);
			if(index3 != -1 && index3 < index2)
				index2 = index3 + 6;

			return texto.substring(index, index2);

		}
		catch(Exception e) {}

		return "";
	}

	public static String extractProrroga(String texto) {
		try {
			int index = texto.indexOf("plazo", texto.indexOf("ENTREGA DEL"));
			int index2 = texto.indexOf("en ", index);

			int index3 = texto.indexOf(",", index);
			if(index3 != -1 && index3 < index2)
				index2 = index3;

			return texto.substring(index, index2);

		}
		catch(Exception e) {}

		return "";
	}

	public static String extractVigencia(String content) {
		try {
			int index = content.indexOf("estará", content.indexOf("QUINTA. "));
			int index2 = content.indexOf(" a", index);

			int index3 = content.indexOf(",", index);
			if(index3 != -1 && index3 < index2)
				index2 = index3;

			return content.substring(index, index2).replaceAll("“", "").replaceAll("”", "");

		}
		catch(Exception e) {}

		return "";
	}

	public static String extractEntrega(String content) {
		try {
			int index2 = 0;

			int index = content.indexOf("realizar", content.indexOf("ENTREGA DE"));
			if(index == -1) {
				index  = content.indexOf("Fecha de entrega:");

				if(index == -1)
					return "";

				index2 = content.indexOf("\n", index + 20);
			}
			else index2 = content.indexOf(".", index);//buscar la coma despues de la coma del monto

			return content.substring(index, index2);

		}
		catch(Exception e) {}

		return "";
	}

	public static String extractContraprestacion(String content) {
		try {
			int index = content.indexOf("cantidad", content.indexOf("SEGUNDA. CONTRAPRESTA"));
			int index2 = content.indexOf(")", index + 30);//buscar la coma despues de la coma del monto

			return content.substring(index, index2 + 2);

		}
		catch(Exception e) {

		}

		return "";
	}

	public static String extractObjeto(String content) {
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

	public static String extractObligacion(String content) {
		try {
			int a      = content.indexOf("alor de los", content.indexOf("SEGUNDA"));
			if(a == -1)
				return "";

			int index  = content.indexOf("EL", a);
			int index4 = content.indexOf("El ", a);

			if(index == -1 || (index4 != -1 && index4 < index))
				index = index4;

			int index2 = content.indexOf("TERCERA", index);
			int index3 = content.indexOf("autoriza en este", index);

			if(index3 != -1 && index3 < index2)
				index2 = index3 - 27;

			return content.substring(index, index2);

		}
		catch(Exception e) {
		}

		return "";
	}

}