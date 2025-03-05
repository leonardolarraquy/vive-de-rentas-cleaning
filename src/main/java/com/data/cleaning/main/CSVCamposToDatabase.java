package com.data.cleaning.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class CSVCamposToDatabase {
    public static void main(String[] args) {
        String csvFilePath = "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/campos.csv";
        String jdbcURL = "jdbc:mysql://localhost:3306/betting";
        String dbUser = "root";
        String dbPassword = "[SL1cJS=$4atpx/]9i6p";

        String insertSQL = "INSERT INTO campos_files (nombre_archivo, nombre_columna) VALUES (?, ?);";
        
        try (Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword);
             PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
             BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length > 1) { // Asegurar que hay más de una columna
                    String nombreArchivo = values[0].trim();
                    
                    for (int i = 1; i < values.length; i++) {
                        String nombreColumna = values[i].trim();
                        
                        if (!nombreColumna.isEmpty()) { // Evitar inserts innecesarios
                            preparedStatement.setString(1, nombreArchivo);
                            preparedStatement.setString(2, nombreColumna);
                            preparedStatement.addBatch();
                        }
                    }
                }
            }
            
            preparedStatement.executeBatch(); // Ejecutar todos los inserts en lote
            System.out.println("Inserción completada.");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
