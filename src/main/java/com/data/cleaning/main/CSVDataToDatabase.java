package com.data.cleaning.main;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class CSVDataToDatabase {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/betting";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "[SL1cJS=$4atpx/]9i6p";
    private static final String CSV_FOLDER = "/Users/leonardo.larraquy/workspace-upwork/data-cleaning/outputs/";

    public static void main(String[] args) {
        File folder = new File(CSV_FOLDER);
        File[] csvFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
        Arrays.sort(csvFiles, Comparator.comparing(File::getName));

        if (csvFiles != null) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                for (File file : csvFiles) {
                    processCSV(file, conn);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void processCSV(File file, Connection conn) {
        try (Reader reader = Files.newBufferedReader(file.toPath());
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            List<String> columnNames = new ArrayList<>(csvParser.getHeaderMap().keySet());
            String insertSQL = generateInsertSQL("listado2", columnNames);
            
            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                for (CSVRecord record : csvParser) {
                    for (int i = 0; i < columnNames.size(); i++) {
                        pstmt.setString(i + 1, record.get(columnNames.get(i)));
                    }
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
            System.out.println("Archivo procesado: " + file.getName());
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private static String generateInsertSQL(String tableName, List<String> columns) {
        String columnNames = String.join(", ", columns);
        String placeholders = String.join(", ", Collections.nCopies(columns.size(), "?"));
        return "INSERT INTO " + tableName + " (" + columnNames + ") VALUES (" + placeholders + ")";
    }
}
