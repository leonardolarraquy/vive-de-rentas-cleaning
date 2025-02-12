package com.data.cleaning.main;

import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class PdfToTextConverter {

	public static void main(String[] args) {
		// Ruta de la carpeta que contiene los archivos PDF
//		String folderPath = "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/parcialidades-promesa-compra-venta/";
//		String folderPath = "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/contado-promesa-compraventa/";
//		String folderPath = "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/smart-depas-experiencias/";
//		String folderPath = "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/smart-depas-promesa-compraventa-fractional/";
//		String folderPath = "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/smart-depas-promesa-compraventa-completo/";
//		String folderPath = "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/hool-bam-fractional/";
//		String folderPath = "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/hool-bam-completo/";
//		String folderPath = "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/hool-bam-financiado/";
//		String folderPath = "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/hool-bam-promesa-escrituracion-copropiedad";
//		String folderPath = "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/hool-bam-promesa-garantia-con-mutuo-interes/";
//		String folderPath = "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/hool-bam-ganancia-capital/";
//		String folderPath = "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/hool-bam-promesa-compra-venta-derecho-fideicomisarios/";
//		String folderPath = "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/hool-bam-contrato-mutuo-interes/";
//		String folderPath = "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/viva-storage-naucalpan/";
//		String folderPath = "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/live-rivera-promesa-compraventa-fractional/";
//		String folderPath = "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/live-rivera-promesa-compraventa-completo/";
//		String folderPath = "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/lofts-la-paz-completo";
//		String folderPath = "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/living-alameda-completo";
//		String folderPath = "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/cancun-promesa-compra-venta-derecho-fideicomisarios";
//		String folderPath = "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/cancun-promesa-compra-venta-derecho-fideicomisarios-mod2/";
//		String folderPath = "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/living-universidad-convenio-adhesion/";
//		String folderPath = "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/torre-monterrey";
//		String folderPath = "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/toledo-suites";
//		String folderPath = "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/living-up-convenio-adhesion";
//		String folderPath = "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/edificio-burjassot";
//		String folderPath = "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/living-universidad-2";
//		String folderPath = "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/lofts-la-paz-convenio-adhesion";
		String folderPath = "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/merida-montejo";
		
		File folder = new File(folderPath);

		// Verificar si la ruta es válida y es una carpeta
		if (!folder.exists() || !folder.isDirectory()) {
			System.err.println("La ruta proporcionada no es válida o no es una carpeta.");
			return;
		}

		// Obtener todos los archivos PDF en la carpeta
		File[] pdfFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));

		if (pdfFiles == null || pdfFiles.length == 0) {
			System.out.println("No se encontraron archivos PDF en la carpeta.");
			return;
		}

		for (File pdfFile : pdfFiles) {
			
			try {
				// Cargar el documento PDF
				PDDocument document = PDDocument.load(pdfFile);

				// Verificar si el documento no está cifrado
				if (!document.isEncrypted()) {
					// Crear el archivo de texto en la misma carpeta
					String txtFileName = pdfFile.getAbsolutePath().replace(".pdf", ".txt");
					File txtFile = new File(txtFileName);
					if(txtFile.exists()) {
						System.out.println("Ya existe el archivo: " + txtFileName);
						continue;
					}

					// Extraer texto del PDF
					PDFTextStripper pdfStripper = new PDFTextStripper();
					String text = pdfStripper.getText(document);

					// Guardar el texto extraído en el archivo .txt
					java.nio.file.Files.writeString(txtFile.toPath(), text);
					System.out.println("Texto extraído y guardado en: " + txtFileName);
				} else {
					System.err.println("El documento está cifrado y no se puede procesar: " + pdfFile.getName());
				}

				// Cerrar el documento
				document.close();
			} catch (IOException e) {
				System.err.println("Error al procesar el archivo PDF: " + pdfFile.getName());
				e.printStackTrace();
			}
		}

		System.out.println("Proceso completado.");
	}
}
