package com.data.cleaning.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Commons {
	
	public static File[] getFiles(String folderPath) {
		File folder = new File(folderPath);

		// Verificar si la ruta es válida y es una carpeta
		if (!folder.exists() || !folder.isDirectory()) {
			System.err.println("La ruta proporcionada no es válida o no es una carpeta.");
			throw new RuntimeException("La ruta proporcionada no es válida o no es una carpeta.");
		}

		// Obtener todos los archivos .txt en la carpeta
		File[] txtFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));

		if (txtFiles == null || txtFiles.length == 0) {
			throw new RuntimeException("No se encontraron archivos .txt en la carpeta.");
		}

		Arrays.sort(txtFiles, (file1, file2) -> file1.getName().compareToIgnoreCase(file2.getName()));
		
		return txtFiles;

	}

	public static String fechaContrato(String texto) {
		try {

			int index  = texto.indexOf("en dos tanto");
			if(index == -1)
				return "";
			
			int index2 = texto.indexOf(".", index + 10);
			if(index2 != -1)
				texto = texto.substring(index + 36, index2);

			index2 = texto.indexOf("EL");
			if(index2 != -1)
				texto = texto.substring(0, index2);

			index2 = texto.indexOf("“");
			if(index2 != -1)
				texto = texto.substring(0, index2);
			
			String res = texto.replaceAll("dias ", "").replaceAll("días ", "").replaceAll("dia ", "").replaceAll("día ", "").replaceAll("co ", "").replaceAll("a los", "").replaceAll("los", "").replaceAll("al ", "").replaceAll("a ", "").replaceAll("de ", "").replaceAll("del ", "").trim();
			if(res.length() < 40)
				return res;

			String substr = texto.substring(index, texto.length());

			Pattern pattern = Pattern.compile("\\b(\\d{1,2})\\s+de\\s+(enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|octubre|noviembre|diciembre)\\s+del\\s+(\\d{4})\\b", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(substr);

			if(matcher.find())
				return matcher.group().replaceAll("mes", "").replaceAll("dias", "").replaceAll("el ", "");
		}
		catch(Exception e) {
		}

		return "";
	}

	public static String extraerNacionalidad(String contenidoArchivo) {
		if (contenidoArchivo == null || contenidoArchivo.isEmpty()) {
			return "";
		}

		List<String> lineas = Arrays.asList(contenidoArchivo.split("\\r?\\n"));
		HashSet<String> nacionalidades = new HashSet<>();

        Pattern patronNacionalidad = Pattern.compile("de nacionalidad\\s*,?\\s*([\\p{L}]+(?:\\s+(?!con)[\\p{L}]+)?)");
//		Pattern patronNacionalidad = Pattern.compile("de nacionalidad\\s*,?\\s*([\\p{L}]+(?:\\s+[\\p{L}]+)?)");

		for (String linea : lineas) {
			Matcher matcher = patronNacionalidad.matcher(linea);
			if (matcher.find()) {
				nacionalidades.add(matcher.group(1).trim().replaceAll(" y", "")); // Extrae el grupo 1 (la nacionalidad) y elimina espacios
			}
		}
		
        return String.join("-", nacionalidades); // Une las nacionalidades con guiones
	}
	
    public static String extraerEstadoCivil(String contenidoArchivo) {
        if (contenidoArchivo == null || contenidoArchivo.isEmpty()) {
            return "";
        }

        List<String> lineas = Arrays.asList(contenidoArchivo.split("\\r?\\n"));
        Set<String> estadosCiviles = new HashSet<>();
        Pattern patronEstadoCivil = Pattern.compile("(soltero|soltera|casado|casada)\\b(?:\\s*por\\s*(el\\s*régimen\\s*de\\s*separación\\s*de\\s*bienes|sociedad\\s*conyugal))?");
        // Mejorada para capturar variaciones y régimen matrimonial

        for (String linea : lineas) {
            Matcher matcher = patronEstadoCivil.matcher(linea);
            if (matcher.find()) {
                String estadoCivil = matcher.group();
                estadosCiviles.add(estadoCivil.trim());
            }
        }
        return String.join("-", estadosCiviles);
    }

    public static String extraerCorreosUnicos(String contenidoArchivo) {
        Set<String> correosUnicos = new HashSet<>();
        Pattern patronCorreo = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");

        if (contenidoArchivo == null || contenidoArchivo.isEmpty()) {
            // Si el contenido está vacío, devolvemos tres separadores
            return "|||";
        }

        List<String> lineas = Arrays.asList(contenidoArchivo.split("\\r?\\n"));

        for (String linea : lineas) {
            Matcher matcher = patronCorreo.matcher(linea);
            while (matcher.find()) {
                String correo = matcher.group();
                if (!correo.endsWith("@vivedelasrentas.com") && !correo.endsWith("@lgc.com.mx")) {
                    correosUnicos.add(correo);
                }
            }
        }

        StringBuilder resultado = new StringBuilder();
        List<String> listaCorreos = new ArrayList<>(correosUnicos); // Convertimos el Set a List para acceder por índice
        for (int i = 0; i < 3; i++) { // Iteramos 3 veces para asegurar 3 separadores
          if (i < listaCorreos.size()) {
            resultado.append(listaCorreos.get(i));
          }
          
          if(i < 2)
        	  resultado.append("|");
        }

        return resultado.toString();
    }

	public static String getRFC(String texto) {
		return getRFC(texto, null);
	}

	public static String getRFC(String texto, String origin) {
		if(origin == null)
			origin = "Adquirente";

		String RFC                  = Commons.extract(texto, "clave", ".", origin);
		if(RFC.indexOf(".") > 20)
			RFC = RFC.substring(0, RFC.indexOf("."));

		if(RFC.indexOf("\n", 10) > 0)
			RFC = RFC.substring(0, RFC.indexOf("\n", 10));

		return Commons.toSingleLine(RFC);
	}

	public static String getRFCLimpio(String texto) {
		texto = toSingleLine(texto);

		String regex = "(?:clave:\\s*)?([A-Z0-9]{13})";

		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(texto);

		if (matcher.find()) 
			return matcher.group(1).trim();
		
		regex = "(?:clave:\\s*)?([A-Z0-9]{12})";

		pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(texto);

		if (matcher.find()) 
			return matcher.group(1).trim();

		return "";

	}

	public static String getCURP(String texto) {
		texto = toSingleLine(texto);

		String CURP                 = Commons.extract(texto, "de Población", ".");
		if(CURP.length() == 0)
			CURP                 = Commons.extract(texto, "de Poblacion", ".");
		
		if(CURP.length() > 32)
			CURP = CURP.substring(0, 32);
		
		return CURP.replaceAll(",", "");
	}
	
	public static String getCURPLimpio(String texto) {
		texto = toSingleLine(texto);

//		String regex = "(?:Registro de Población:\\s*)?([A-Z0-9]{18})";
		String regex = "([A-Z0-9]{18})$";
		
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(texto);

		if (matcher.find()) 
			return matcher.group(1).trim();

		return "";

	}

	public static String toSingleLine(String content) {
		if(content == null)
			return "";

		return content.replaceAll("\\s*\\n\\s*", " ").trim();
	}

	public static String extraerUnidadAbrev(String texto) {
//		String regex = "(?:Unidad(?: Inmobiliaria)?|Departamento|unidad|unidad No\\.?\\s*)?\\s*[A-Z](?:\\s*[\\.\\- ]\\s*)?[0-9]{3}";

		
		String regex = "([A-Z])(?:\\s*[- ]\\s*)?([0-9]{3})";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(texto);

        if (matcher.find()) {
            String letra = matcher.group(1);
            String numeros = matcher.group(2);

            return letra + "-" + numeros;
        }
        
        return "";
	}

	public static String convertirFecha(String textoFecha) {
		try {
			textoFecha = toSingleLine(textoFecha);

			textoFecha = textoFecha.replaceAll("primer ", "");
			textoFecha = textoFecha.replaceAll("pago ", "");
			textoFecha = textoFecha.replaceAll("del ", "");
			textoFecha = textoFecha.replaceAll("los ", "");
			textoFecha = textoFecha.replaceAll("de ", "");
			textoFecha = textoFecha.replaceAll("mes ", "");
			textoFecha = textoFecha.replaceAll("año ", "");
			textoFecha = textoFecha.replaceAll("dias ", "");
			textoFecha = textoFecha.replaceAll("días ", "");
			textoFecha = textoFecha.replaceAll("día ", "");
			textoFecha = textoFecha.replaceAll("el ", "");
			textoFecha = textoFecha.replaceAll("a ", "");
			textoFecha = textoFecha.replaceAll("s ", "");
			textoFecha = textoFecha.replaceAll("\\.", "");
			textoFecha = textoFecha.replaceAll("realizará", "");
			textoFecha = textoFecha.replaceAll("en ", "");

			// 1. Definir el formato de entrada
			SimpleDateFormat formatoEntrada = new SimpleDateFormat("dd MMMM yyyy", new Locale("es", "ES"));
			formatoEntrada.setLenient(false); // Importante para evitar fechas inválidas como 30 de febrero

			// 2. Parsear el texto a un objeto Date
			Date fecha = formatoEntrada.parse(textoFecha);

			// 3. Definir el formato de salida
			SimpleDateFormat formatoSalida = new SimpleDateFormat("dd/MM/yyyy");

			// 4. Formatear la fecha al formato de salida
			return formatoSalida.format(fecha);

		} catch (ParseException e) {
			return ""; // O lanzar una excepción personalizada si lo prefieres
		}
	}

	public static String[] readLines(String rutaArchivo) {
		List<String> lineas = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
			String linea;
			while ((linea = br.readLine()) != null) {
				lineas.add(linea);
			}
		} catch (IOException e) {
			System.err.println("Error al leer el archivo: " + e.getMessage());
			return null; // O lanzar una excepción, según prefieras
		}

		return lineas.toArray(new String[0]); // Convierte la lista a un array
	}

	public static String extractPromitenteEnajenante(String content) {
		if(content.indexOf("WE STORAGE") > 0)
			return "WE STORAGE S.A.P.I DE C.V.";

		if(content.indexOf("RENTIUX S") > 0)
			return "RENTIUX, S. DE R.L. DE C.V.";

		if(content.indexOf("RENTIUX M") > 0)
			return "RENTIUX MANAGEMENT S.A.P.I DE C.V.";

		if(content.indexOf("RENTIUX, S.") > 0)
			return "RENTIUX, S. DE R.L. DE C.V.";

		return "";
	}

	public static String extractPromitenteAdquiriente(String content) {
		try {
			if(content.indexOf("MODIFICATORIO") > 0) {
				String start = "POR UNA PARTE";
				String end = "(";

				int startIndex = content.indexOf(start);
				int endIndex = content.indexOf(end);

				if (startIndex != -1 && endIndex != -1) {
					return content.substring(startIndex + start.length(), endIndex).trim();
				}
			}


			Pattern pattern = Pattern.compile("OTRA PARTE\\s+([^,]+)\\s+POR", Pattern.MULTILINE);
			Matcher matcher = pattern.matcher(content);
			if(matcher.find()) {
				String extracted = matcher.group(1);
				extracted = Commons.toSingleLine(extracted);
				
				return extracted.replaceAll("EL SEÑOR", "").replaceAll("EL SENOR", "").replaceAll("LA SEÑORA", "").replaceAll("LA SEÑORITA", "").replaceAll("LA PERSONA MORAL DENOMINADA", "").replaceAll("REPRESENTADA EN ESTE ACTO", "").replaceAll(",", "").trim();
			}

			int index  = content.indexOf("OTRA ");
			if(index == -1)
				index  = content.indexOf(" Y ", content.indexOf(";")) + 2;

			int index2 = content.indexOf(" POR ", index);
			int index3 = content.indexOf("A QUIEN", index);
			
			if(index3 != -1 && (index3 < index2))
				index2 = index3;

			String substr = content.substring(index + 11, index2);
			substr = Commons.toSingleLine(substr);

			return substr.replaceAll("EL SEÑOR", "").replaceAll("EL SENOR", "").replaceAll("LA SEÑORA", "").replaceAll("LA SEÑORITA", "").replaceAll("LA PERSONA MORAL DENOMINADA", "").replaceAll("REPRESENTADA EN ESTE ACTO", "").replaceAll(",", "").trim();
		}
		catch(Exception e) {

		}

		return "";
	}

	public static String extractFase(String content) {
		Pattern pattern = Pattern.compile("(Fase|Etapa)\\s+\\d+", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(content);

		return matcher.find() ? matcher.group().replaceAll("Etapa ", "Fase ").trim() : "";
	}

	public static String extractMonto(String content) {
		if(content.indexOf("MODIFICATORIO") > 0) 
			return "";

		try {
			int index  = content.indexOf("SEGUNDA");
			int index2  = content.indexOf("A. ", index + 10);
			if(index2 == -1) {
				index2 = content.indexOf("A ", index + 10);

				if(index == -1) {
					index2 = content.indexOf("a. ", index + 10);

					if(index == -1) {
						index2 = content.indexOf("TERCERA. ", index + 10);
					}
				}
			}

			content = content.substring(index, index2);

			//			Pattern pattern = Pattern.compile("\\$([0-9]{1,3}([,.][0-9]{3})*)([.][0-9]{2})?\\b", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
			Pattern pattern = Pattern.compile("\\$([\\d.,]+)(\\.\\d{2})?\\s\\(.*?\\)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

			Matcher matcher = pattern.matcher(content);
			if(matcher.find()) {
				String cantidadConParentesis = matcher.group(0);
				int indiceFin = cantidadConParentesis.indexOf(" la cual");

				if (indiceFin == -1) {
					indiceFin = cantidadConParentesis.indexOf(" misma que");
				}
				if (indiceFin == -1) {
					indiceFin = cantidadConParentesis.indexOf("]");
					if(indiceFin != -1) {
						indiceFin++; //incluir el ]
					}
				}

				if (indiceFin != -1 && indiceFin > cantidadConParentesis.indexOf(cantidadConParentesis)) {
					return cantidadConParentesis.substring(cantidadConParentesis.indexOf(cantidadConParentesis), indiceFin).trim();
				} 
				else {
					return cantidadConParentesis; // Devuelve la coincidencia original si no se encuentra "la cual"
				}
			}

			index  = content.indexOf("cantidad");
			index2 = content.indexOf(",", index + 30);

			int index3 = content.indexOf("]", index + 30);
			if(index3 != -1 && index2 > index3)
				index2 = index3 + 2;

			return content.substring(index, index2 - 1);
		}
		catch(Exception e) {
		}

		return "";
	}

	public static String extractLote(String content) {
		Pattern pattern = Pattern.compile("(?:Lotes? )?No\\.? [^,]+, Manzana\\.? \\d+", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

		Matcher matcher = pattern.matcher(content);
		if(matcher.find())
			return matcher.group().replaceAll("Lote ", "").trim();

		return Commons.extract(content, "Lote", " con", "PRIMERA").replaceAll("Lote ", "").trim();
	}

	public static String extractMoneda(String content) {
		String moneda = "MXN";

		if(content.indexOf("€") > 0)
			moneda = "EUR";

		if(content.indexOf("USD") > 0 || content.indexOf("USO") > 0)
			moneda = "USD";

		return moneda;
	}

	public static String extractMontoLiquidacion(String content) {
		try {

			int index  = content.indexOf("SEGUNDA. ") + 10;
			int index2 = content.indexOf("TERCERA. ");

			content = content.substring(index, index2);

			index  = content.indexOf("B. ");
			if(index == -1) {
				index = content.indexOf("B ");

				if(index == -1) {
					index = content.indexOf("b. ");
				}
			}

			if(index == -1) 
				return "";

			index  = content.indexOf("cantidad", index);//llego a cantidad dentro de clausula B

			index2 = content.indexOf("C. ");
			if(index2 == -1) {
				index2 = content.indexOf("C ");

				if(index2 == -1) {
					index2 = content.indexOf("c. ");

					if(index2 == -1) 
						index2 = content.length();
				}
			}

			int index3  = content.indexOf(")", index) + 2;//llego al fin del parentesis
			if(index3 < index2)
				index2 = index3;

			return content.substring(index, index2);			
		}
		catch(Exception e) {
		}

		return "";
	}

	public static String numericValue(String texto) {
		//		Pattern pattern = Pattern.compile("\\$([0-9]{1,3}([,.][0-9]{3})*)([.][0-9]{2})?\\b", Pattern.CASE_INSENSITIVE | Pattern.DOTALL); //expresion mejorada
		Pattern pattern = Pattern.compile("([0-9]{1,3}(?:[,.][0-9]{3})*)(?:[,.][0-9]{2})?(?:€)?\\b", Pattern.CASE_INSENSITIVE | Pattern.DOTALL); //expresion mejorada

		//		Pattern pattern = Pattern.compile("\\$[\\d.,]+\\.\\d{2}");
		Matcher matcher = pattern.matcher(texto);

		if (matcher.find()) {
			String match = matcher.group(0);

			if(match.endsWith(",00") || match.endsWith(".00"))
				match = match.replaceAll("(\\.\\d{2}|,\\d{2})$", "");


			return match.replaceAll(",", "");
		} 
		else {
			if(texto.indexOf("65,000") > 0)
				return "$65000";

			return "";

		}
	}	

	public static String extraerFechaAPartirDeTexto(String texto) {
		return extraerFechaAPartirDeTexto(texto, 1970);
	}

	public static String extraerFechaAPartirDeTexto(String texto, int anoDefault) {
		if(texto.length() == 0)
			return "";
		
		texto = Commons.toSingleLine(texto);
		texto = texto.toLowerCase();

		if(!texto.startsWith("el "))
			texto = "el " + texto;

		texto = texto.replaceAll("efectuada", "").replaceAll("realizara", "").replaceAll("realizará", "").replaceAll("será", "").replaceAll("del ", "de ").replaceAll("de ", "").replaceAll("la ", "").replaceAll("\"", "").replaceAll("en ", "").replaceAll(" el", "").replaceAll("año", "").replaceAll("entrega", "").replaceAll("unidad", "").replaceAll("departamento", "").replaceAll("se ", "").replaceAll("mes ", "").replaceAll("días ", "").replaceAll("\\.", "").replaceAll("primer ", "").replaceAll("pago ", "").replaceAll("fecha ", "");

		String regex = "(?:\\s+(?:en\\s+el\\s+mes\\s+de|el\\s+día|mes))?\\s+([a-z]+)\\s*(?:de\\s*)?(?:([0-9]{4}))?\\b";

		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

		Matcher matcher = pattern.matcher(texto);
		if (matcher.find()) {
			String mesString = matcher.group(1);
			String anioString = matcher.group(2);

			try {
				Month mes = null;
				for (Locale locale : new Locale[] { Locale.getDefault(), new Locale("es"), Locale.ENGLISH }) {
					try {
						mes = Month.valueOf(mesString.toUpperCase(locale));
						break;
					} catch (IllegalArgumentException e) {
						try{
							mes = Month.from(DateTimeFormatter.ofPattern("MMMM", locale).parse(mesString));
							break;
						} catch (DateTimeParseException ex){
							//Intento con abreviatura de mes en ingles o español
							try {
								mes = Month.from(DateTimeFormatter.ofPattern("MMM", locale).parse(mesString));
								break;
							} catch (DateTimeParseException exc){
								continue;
							}
						}
					}
				}
				if (mes == null) {
					System.err.println("Mes no reconocido: " + mesString);
					return null;
				}

				int anio = (anioString != null) ? Integer.parseInt(anioString) : anoDefault;

				LocalDate fecha = LocalDate.of(anio, mes, 1);
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
				return fecha.format(formatter); // Formatear a String
			} catch (NumberFormatException e) {
				System.err.println("Año no válido: " + anioString);
				return null;
			}
		} else {
			System.err.println("No se encontró una fecha válida en: " + texto);
			return null;
		}
	}

	public static String extract(String content, String inicio, String fin) {
		try {
			int index = content.indexOf(inicio);

			int index2 = content.indexOf(fin, index);

			return content.substring(index, index2);

		}
		catch(Exception e) {
		}

		return "";
	}

	public static String extract(String content, String inicio, String fin, String origen) {
		try {
			int init = content.indexOf(origen);
			if(init == -1)
				return "";

			int index = content.indexOf(inicio, init);

			int index2 = content.indexOf(fin, index);

			return content.substring(index, index2).trim();
		}
		catch(Exception e) {
		}

		return "";
	}

	public static String tags(String contenido) {
		// Palabras reservadas (puedes modificarlas aquí)
		Set<String> palabrasBuscadas = new HashSet<>(Arrays.asList(
				"copropiedad", "fideicomiso", "fideicomisos", "adhesión", "adhesion", "mutuo",
				"promesa", "rendimiento", "renta anticipada", "rentas anticipadas", "sin interes", "interés", "interes",
				"recompra", "salida", "escrituración", "escrituracion", "amueblado", "parcialidad", "credito"
				));

		List<String> palabrasEncontradas = new ArrayList<>();

		contenido = normalizarTexto(contenido); // Normalizar el contenido

		if (contenido.contains("sin interes") || contenido.contains("sin interés")) {
			palabrasBuscadas.remove("interes");
			palabrasBuscadas.remove("interés");
		}

		for (String palabraBuscada : palabrasBuscadas) {
			if (buscarPalabra(contenido, palabraBuscada)) {
				palabrasEncontradas.add(palabraBuscada);
			}
		}

		return String.join("-", palabrasEncontradas);
	}

	private static String normalizarTexto(String texto) {
		texto = texto.toLowerCase(java.util.Locale.getDefault());
		texto = Normalizer.normalize(texto, Normalizer.Form.NFD);
		texto = texto.replaceAll("\\p{M}", "");
		return texto;
	}

	private static boolean buscarPalabra(String texto, String palabraBuscada) {
		// Manejo de plurales simples (añadiendo "s" o "es")
		String regex = "\\b(" + Pattern.quote(palabraBuscada) + "|"+ Pattern.quote(palabraBuscada + "s") + "|" + Pattern.quote(palabraBuscada + "es") + ")\\b";
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(texto);
		return matcher.find();
	}

	public static String extractParteDecimal(String content) {
		String regex = "\\s+([0-9]+(?:\\.[0-9]+)?)%";

		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(content);

		return matcher.find() ? matcher.group(1) : "";
	}
}