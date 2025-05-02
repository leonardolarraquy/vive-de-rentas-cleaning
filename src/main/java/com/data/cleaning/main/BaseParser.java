package com.data.cleaning.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseParser {

	public static final String BASE_TITLE = "TIPO_CONTRATO|NOMBRE_PROYECTO|NOMBRE_ARCHIVO|LINK_A_ARCHIVO|TAGS|ENAJENANTE|RAZON_SOCIAL|NOMBRE|APELLIDO_PATERNO|APELLIDO_MATERNO|P_FISICA|P_MORAL|CLAVE_UNICA|CURP|CLAVE_RFC|RFC|FECHA_NACIMIENTO|DIRECCION|NACIONALIDAD|ESTADO_CIVIL|MAIL_1|MAIL_2|MAIL_3|BENEFICIARIO|FECHA_CONTRATO_STR|FECHA_CONTRATO|REVISION_MANUAL|";

	public abstract String getTipoContrato();

	public abstract String getProyecto();

	public abstract String getFolderPath();

	public abstract void addOtherFields(BufferedWriter csvWriter, String content, String revisionManual) throws IOException;

	public abstract String getFieldsTitle();

	public String getCURP(String content) {
		return Commons.getCURP(content);
	}

	public String getRFC(String content, String init) {
		return Commons.getRFC(content, init);
	}

	public boolean isPersonaFisica(String content) {
		String regex = "(?i)\\b(personas?\\s+f[ií]sicas?)\\b";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);
		return matcher.find();
	}

	public String getDireccionAdquirente(String content) {
		String direccionAdquirente  = Commons.extract(content, "ADQUIRENTE", "Cualquiera", "OCTAVA");
		if(direccionAdquirente.length() == 0)		
			direccionAdquirente  = Commons.extract(content, "EL “PROMITENTE ADQUIRENTE”", "/", "OCTAVA").replaceAll("EL “PROMITENTE ADQUIRENTE”:", "");
		else {
			if(direccionAdquirente.length() > 13)
				direccionAdquirente = direccionAdquirente.substring(13);

			if(direccionAdquirente.indexOf("EL ") > 0)
				direccionAdquirente = direccionAdquirente.substring(0, direccionAdquirente.indexOf("EL "));	

			if(direccionAdquirente.indexOf("/") > 0)
				direccionAdquirente = direccionAdquirente.substring(0, direccionAdquirente.indexOf("/"));	

			if(direccionAdquirente.indexOf("DÉCIMA") > 0)
				direccionAdquirente = direccionAdquirente.substring(0, direccionAdquirente.indexOf("DÉCIMA"));				
		}
		

		return Commons.toSingleLine(direccionAdquirente);
	}


	public static String[] getNames(String nombreCompleto) {
		final Set<String> EXCEPCIONES = new HashSet<>(Arrays.asList(
				"DE", "DEL", "LA", "LOS", "Y", "MAC", "VAN", "VON"
				));


		if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
			return new String[]{"", "", ""}; // Caso vacío
		}

		List<String> partes = Arrays.asList(nombreCompleto.trim().split("\\s+"));

		if (partes.size() == 1) {
			return new String[]{partes.get(0), "", ""}; // Solo un nombre
		}

		if (partes.size() == 2) {
			return new String[]{partes.get(0), partes.get(1), ""}; // Nombre y apellido paterno
		}

		// Caso de tres o más palabras
		int i = 1;
		while (i < partes.size() - 2 && EXCEPCIONES.contains(partes.get(i).toUpperCase())) {
			i++;
		}

		String nombre = String.join(" ", partes.subList(0, i));
		String apellidoPaterno = partes.get(i);
		String apellidoMaterno = (i + 1 < partes.size()) ? String.join(" ", partes.subList(i + 1, partes.size())) : "";

		return new String[]{nombre, apellidoPaterno, apellidoMaterno};
	}

	public String getFechaNacimiento(String curp, String rfc) {
		String documento = curp;
		if (documento == null || documento.length() < 10) {
			documento = rfc;
		}
		if (documento == null || documento.length() < 10) {
			return "";
		}

		String fechaStr = documento.substring(4, 10);
		try {
			// Determinar el siglo correcto
			int year = Integer.parseInt(fechaStr.substring(0, 2));
			int month = Integer.parseInt(fechaStr.substring(2, 4));
			int day = Integer.parseInt(fechaStr.substring(4, 6));

			// Si el año está por debajo de 2000, asumimos que es del siglo XX, de lo contrario, XXI
			year += (year >= 0 && year <= 24) ? 2000 : 1900;

			return String.format("%02d/%02d/%d", day, month, year);        	
		}
		catch(Exception e) {
			System.out.println("Fecha nacimiento invalida: " + fechaStr);

			return "";
		}

	}
	
	public String getTags(String content) {
		return Commons.tags(content);
	}
	
	public String getEnajenante(String content) {
		return Commons.extractPromitenteEnajenante(content);
	}

	public String getAdquiriente(String content) {
		return Commons.extractPromitenteAdquiriente(content);
	}

	public String getBeneficiario(String content) {
		String beneficiario       = Commons.extract(content, " a ", ",", "como su");

		if(beneficiario.length() > 0)
			beneficiario = beneficiario.substring(2, beneficiario.length());

		if(beneficiario.indexOf("llevando") > 0)
			beneficiario = beneficiario.substring(0, beneficiario.indexOf("llevando"));

		if(beneficiario.indexOf("fallec") > 0)
			beneficiario = beneficiario.substring(0, beneficiario.indexOf("fallec"));

		return beneficiario;
	}
	
	
	public void process() {
		String folderPath = getFolderPath();

		String[] rutas = Commons.readLines(folderPath + "_links");

		String csvOutputPath = folderPath + "/output.csv";

		int i = 0;

		// Crear el archivo CSV de salida
		try (BufferedWriter csvWriter = new BufferedWriter(new FileWriter(csvOutputPath))) {        	
			// Escribir encabezados en el archivo CSV
			csvWriter.write(BASE_TITLE + getFieldsTitle() + "\n");

			// Obtener todos los archivos .txt en la carpeta
			File[] txtFiles =  Commons.getFiles(folderPath);


			for (File txtFile : txtFiles) {
				System.out.println("processing: " + txtFile.getName());

				String ruta               = rutas[i];
				i++;

				String content              = Files.readString(txtFile.toPath());
				String promitenteEnajenante = this.getEnajenante(content);

				String revisionManual     = "";

				String tags                 = this.getTags(content);

				String promitenteAdquirente = this.getAdquiriente(content);
				boolean personaFisica       = this.isPersonaFisica(content);

				String CURP                 = this.getCURP(content);
				String CURPLimpio           = Commons.getCURPLimpio(CURP);

				if(CURP.length() == 0 )
					revisionManual = revisionManual + "CURP.";
				else {
					if(CURPLimpio.length() != 18)
						revisionManual = revisionManual + "CURP Invalido.";
				}

				String RFC                  = this.getRFC(content, "inscrito");
				String RFCLimpio            = Commons.getRFCLimpio(RFC);

				if(RFC.length() == 0 )
					revisionManual = revisionManual + "RFC.";
				else {
					if(RFCLimpio.length() != 13 && RFCLimpio.length() != 12)
						revisionManual = revisionManual + "RFC Invalido.";					
				}

				String beneficiario       = this.getBeneficiario(content);
						
				String fechaContrato      = this.fechaContrato(content);
				String fechaContratoNum   = this.fechaContratoNum(fechaContrato);
				
				if(fechaContratoNum.length() == 0)
					revisionManual = revisionManual + "Fecha Contrato.";

				String[] nombres          = getNames(promitenteAdquirente);
				
				String fechaNacimiento    = this.getFechaNacimiento(CURPLimpio, RFCLimpio);
				if(fechaNacimiento.length() == 0)
					revisionManual = revisionManual + "Fecha Nacimiento.";
				
				String direccion          = this.getDireccionAdquirente(content);
				if(direccion.length() == 0)
					revisionManual = revisionManual + "Direccion.";

				// Escribir una fila en el archivo CSV
				csvWriter.write(String.join("|",
						Commons.toSingleLine(getTipoContrato()),
						Commons.toSingleLine(getProyecto()),
						Commons.toSingleLine(txtFile.getName().replaceAll(".txt", ".pdf")),
						Commons.toSingleLine(ruta),
						tags,						

						//ADQUIRENTE
						Commons.toSingleLine(promitenteEnajenante),
						Commons.toSingleLine((!personaFisica ? promitenteAdquirente : "")),
						Commons.toSingleLine((personaFisica ? nombres[0] : "")),
						Commons.toSingleLine((personaFisica ? nombres[1] : "")),
						Commons.toSingleLine((personaFisica ? nombres[2] : "")),

						Commons.toSingleLine((personaFisica  ? "X" : "")),
						Commons.toSingleLine((!personaFisica ? "X" : "")),

						Commons.toSingleLine(CURP),
						Commons.toSingleLine(CURPLimpio),
						Commons.toSingleLine(RFC),
						Commons.toSingleLine(RFCLimpio),

						fechaNacimiento,
						Commons.toSingleLine(direccion),

						Commons.extraerNacionalidad(content),
						Commons.extraerEstadoCivil(content),
						Commons.extraerCorreosUnicos(content),

						Commons.toSingleLine(beneficiario),
						Commons.toSingleLine(fechaContrato),
						Commons.toSingleLine(fechaContratoNum)));

				this.addOtherFields(csvWriter, content, revisionManual);


				csvWriter.write("\n");
			}

			System.out.println("Archivo CSV generado en: " + csvOutputPath);

		} catch (IOException e) {
			System.err.println("Ocurrió un error al procesar los archivos: " + e.getMessage());
		}
	}

	public String fechaContratoNum(String texto) {
		return Commons.convertirFecha(texto);
	}
	
	public String fechaContrato(String texto) {
		try {

			int index  = texto.indexOf("en dos tanto en el Estado de México");
			if(index > 0) {
				int index2 = texto.indexOf(".", index + 10);

				if((index2 - (index + 36)) > 40)
					index2 = texto.indexOf("EL", index + 10) - 1;

				String res =  Commons.toSingleLine(texto.substring(index + 36, index2)).replaceAll("a los", "").replaceAll("al ", "").replaceAll("a ", "").replaceAll("días ", "").replaceAll("de ", "").replaceAll("del ", "").trim();
				if(res.indexOf(".") > 0)
					res = res.substring(0, res.indexOf("."));
				
				return res;
			}
			
			index  = texto.indexOf("día", texto.indexOf("lo firman de conformidad"));
			int index2 = texto.indexOf("E", index);

			String res = texto.substring(index - 4, index2);
			if(res.indexOf(".") > 0)
				res = res.substring(0, res.indexOf("."));

			return res;
		}
		catch(Exception e) {
		}

		return "";
	}
}
