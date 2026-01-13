// package com.apocalipsebr.zomboid.server.manager.application.service;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Service;

// import java.io.File;
// import java.sql.*;
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.logging.Logger;

// @Service
// public class SQLiteService {
//     private static final Logger logger = Logger.getLogger(SQLiteService.class.getName());

//     @Value("${zomboid.server.path:C:\\Users\\ricar\\Zomboid\\db\\servertest.db}")
//     private String serverPath;

//     public Connection getConnection(String dbPath) throws SQLException {
//         String url = "jdbc:sqlite:" + dbPath;
//         return DriverManager.getConnection(url);
//     }

//     public List<Map<String, Object>> executeQuery(String dbPath, String query) throws SQLException {
//         List<Map<String, Object>> results = new ArrayList<>();
        
//         try (Connection conn = getConnection(dbPath);
//              Statement stmt = conn.createStatement();
//              ResultSet rs = stmt.executeQuery(query)) {
            
//             ResultSetMetaData metaData = rs.getMetaData();
//             int columnCount = metaData.getColumnCount();
            
//             while (rs.next()) {
//                 Map<String, Object> row = new HashMap<>();
//                 for (int i = 1; i <= columnCount; i++) {
//                     String columnName = metaData.getColumnName(i);
//                     Object value = rs.getObject(i);
//                     row.put(columnName, value);
//                 }
//                 results.add(row);
//             }
//         }
        
//         logger.info("Executed query on " + dbPath + ": " + query + " - Results: " + results.size());
//         return results;
//     }

//     public int executeUpdate(String dbPath, String sql) throws SQLException {
//         try (Connection conn = getConnection(dbPath);
//              Statement stmt = conn.createStatement()) {
            
//             int rowsAffected = stmt.executeUpdate(sql);
//             logger.info("Executed update on " + dbPath + ": " + sql + " - Rows affected: " + rowsAffected);
//             return rowsAffected;
//         }
//     }

//     public List<String> listDatabaseFiles() {
//         List<String> databases = new ArrayList<>();
//         File serverDir = new File(serverPath);
        
//         if (serverDir.exists() && serverDir.isDirectory()) {
//             findSQLiteFiles(serverDir, databases);
//         }
        
//         return databases;
//     }

//     private void findSQLiteFiles(File directory, List<String> databases) {
//         File[] files = directory.listFiles();
//         if (files != null) {
//             for (File file : files) {
//                 if (file.isFile() && (file.getName().endsWith(".db") || file.getName().endsWith(".sqlite"))) {
//                     databases.add(file.getAbsolutePath());
//                 } else if (file.isDirectory() && !file.getName().startsWith(".")) {
//                     // Recursively search subdirectories (max depth 3)
//                     findSQLiteFiles(file, databases);
//                 }
//             }
//         }
//     }

//     public List<String> listTables(String dbPath) throws SQLException {
//         List<String> tables = new ArrayList<>();
//         String query = "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name";
        
//         List<Map<String, Object>> results = executeQuery(dbPath, query);
//         for (Map<String, Object> row : results) {
//             tables.add((String) row.get("name"));
//         }
        
//         return tables;
//     }

//     public boolean databaseExists(String dbPath) {
//         File dbFile = new File(dbPath);
//         return dbFile.exists() && dbFile.isFile();
//     }
// }
