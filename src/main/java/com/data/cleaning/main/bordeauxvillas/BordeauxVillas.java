package com.data.cleaning.main.bordeauxvillas;

import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.data.cleaning.main.BaseParser;
import com.data.cleaning.main.Commons;

public class BordeauxVillas extends BaseParser {
	
	public String getTipoContrato() {
		return "Fractional contract";
	}
	
	public String getProyecto() {
		return "Bourdeaux Villas (Chantle)";
	}
	
	public String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/bordeaux-villas/";
	}
	
	public String getFieldsTitle() {
//		return "Monto|Monto Num|Moneda|Interes Anual|Fecha fin contrato|Condiciones pago|";
		return "MONTO_INVERSION|MONEDA|TASA_INTERES_ANUAL|PLAZO_MESES|CLAUSULAS|CUOTA_MENSUAL|MONEDA_CUOTA";
	}
	
	@Override
	public String getEnajenante(String content) {
		return "SMART CAPITAL TEXAS LLC";
	}
	
	@Override
	public String getAdquiriente(String content) {
		return Commons.extract(content, "AND", "Promissory").replaceAll("AND ", "");
	}
	
	@Override
	public String fechaContrato(String texto) {
		return Commons.extract(texto, "Date:", "Borrower").replaceAll("Date:", "");		
	}
	
	@Override
	public String fechaContratoNum(String texto) {
		texto = texto.trim();
		
        DateTimeFormatter formatoEntrada = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);
        DateTimeFormatter formatoSalida = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return LocalDate.parse(texto, formatoEntrada).format(formatoSalida);
    }
	
	@Override
	public boolean isPersonaFisica(String content) {
		return true;
	}
	
	@Override
	public String getDireccionAdquirente(String content) {
		return Commons.extract(content, "Lender’s Mailing Address:", "Obligation").replaceAll("Lender’s Mailing Address:", "");
	}

	public static void main(String[] args) {
		BordeauxVillas parser = new BordeauxVillas();
		parser.process();
	}

	public void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException {
		String monto                = Commons.extract(content, "Principal Amount:", "Annual");
		String montoNum             = Commons.numericValue(monto);
		String moneda               = Commons.extractMoneda(monto);

		String interes              = Commons.extract(content, "Annual Interest Rate:", "Maturity").replaceAll("Annual Interest Rate:", "");
				
//		String fechaFin             = Commons.extract(content, "Maturity date:", "Annual");
		
		String condiciones          = Commons.extract(content, "installments of", "This", "Terms of Payment");
		
		String cuota                = Commons.extract(condiciones, "$", ")").substring(1);
		
		csvWriter.write("|");

		csvWriter.write(
				String.join("|",
						revisionManual, 

//						Commons.toSingleLine(monto),
						Commons.toSingleLine(montoNum),
						Commons.toSingleLine(moneda),

						Commons.toSingleLine(interes),

						Commons.toSingleLine("60"),

						Commons.toSingleLine(condiciones),
						Commons.toSingleLine(cuota),
						Commons.toSingleLine("USD")
						
						));

	}
}