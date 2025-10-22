package com.workato;

import java.sql.*;
import java.util.Properties;

/**
 * Command-line utility to execute Snowflake SQL queries.
 * 
 * Usage: java com.workato.SnowflakeQueryRunner 
 *        --account <account_url> 
 *        --user <username> 
 *        --password <password> 
 *        --database <database> 
 *        --schema <schema> 
 *        --warehouse <warehouse> 
 *        --query "<sql_query>"
 */
public class SnowflakeQueryRunner {
    
    private static final String SNOWFLAKE_DRIVER = "net.snowflake.client.jdbc.SnowflakeDriver";
    static {
        System.setProperty(
                "net.snowflake.jdbc.loggerImpl",
                "net.snowflake.client.log.SLF4JLogger"
        );
   }
    
    public static void main(String[] args) {
        if (args.length < 14) {
            printUsage();
            System.exit(1);
        }
        
        ConnectionParams params = parseArguments(args);
        
        if (params == null) {
            printUsage();
            System.exit(1);
        }
        
        try {
            executeQuery(params);
        } catch (Exception e) {
            System.err.println("Error executing query: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static ConnectionParams parseArguments(String[] args) {
        ConnectionParams params = new ConnectionParams();
        
        for (int i = 0; i < args.length; i += 2) {
            if (i + 1 >= args.length) {
                return null; // Invalid arguments
            }
            
            String flag = args[i];
            String value = args[i + 1];
            
            switch (flag) {
                case "--account":
                    params.account = value;
                    break;
                case "--user":
                    params.user = value;
                    break;
                case "--password":
                    params.password = value;
                    break;
                case "--database":
                    params.database = value;
                    break;
                case "--schema":
                    params.schema = value;
                    break;
                case "--warehouse":
                    params.warehouse = value;
                    break;
                case "--query":
                    params.query = value;
                    break;
                default:
                    System.err.println("Unknown flag: " + flag);
                    return null;
            }
        }
        
        // Validate required parameters
        if (params.account == null || params.user == null || params.password == null || 
            params.database == null || params.schema == null || params.warehouse == null || 
            params.query == null) {
            return null;
        }
        
        return params;
    }
    
    private static void executeQuery(ConnectionParams params) throws SQLException, ClassNotFoundException {
        // Load the Snowflake JDBC driver
        Class.forName(SNOWFLAKE_DRIVER);
        
        // Build connection URL
        String url = buildConnectionUrl(params);
        
        // Set connection properties
        Properties properties = new Properties();
        properties.put("user", params.user);
        properties.put("password", params.password);
        properties.put("warehouse", params.warehouse);
        properties.put("db", params.database);
        properties.put("schema", params.schema);
        
        System.out.println("Connecting to Snowflake...");
        System.out.println("Account: " + params.account);
        System.out.println("Database: " + params.database);
        System.out.println("Schema: " + params.schema);
        System.out.println("Warehouse: " + params.warehouse);
        System.out.println("User: " + params.user);
        System.out.println();
        
        try (Connection connection = DriverManager.getConnection(url, properties)) {
            System.out.println("Connected successfully!");
            System.out.println("Executing query: " + params.query);
            System.out.println();
            
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(params.query)) {
                
                // Get metadata to print column headers
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                // Print column headers
                for (int i = 1; i <= columnCount; i++) {
                    System.out.printf("%-20s", metaData.getColumnName(i));
                    if (i < columnCount) {
                        System.out.print(" | ");
                    }
                }
                System.out.println();
                
                // Print separator
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print("--------------------");
                    if (i < columnCount) {
                        System.out.print("-+-");
                    }
                }
                System.out.println();
                
                // Print rows
                int rowCount = 0;
                while (resultSet.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        String value = resultSet.getString(i);
                        if (value == null) {
                            value = "NULL";
                        }
                        // System.out.printf("%-20s", value.length() > 20 ? value.substring(0, 17) + "..." : value);
                        if (i < columnCount) {
                            //System.out.print(" | ");
                        }
                    }
                    // System.out.println();
                    rowCount++;
                }
                
                System.out.println();
                System.out.println("Query completed. Rows returned: " + rowCount);
            }
        }
    }
    
    private static String buildConnectionUrl(ConnectionParams params) {
        // Snowflake JDBC URL format: jdbc:snowflake://<account>.snowflakecomputing.com
        String account = params.account;
        if (!account.contains(".")) {
            account = account + ".snowflakecomputing.com";
        }
        return "jdbc:snowflake://" + account;
    }
    
    private static void printUsage() {
        System.out.println("Snowflake Query Runner");
        System.out.println("Usage: java com.workato.SnowflakeQueryRunner [OPTIONS]");
        System.out.println();
        System.out.println("Required Options:");
        System.out.println("  --account <account>      Snowflake account (e.g., 'myaccount' or 'myaccount.region.cloud')");
        System.out.println("  --user <username>        Snowflake username");
        System.out.println("  --password <password>    Snowflake password");
        System.out.println("  --database <database>    Database name");
        System.out.println("  --schema <schema>        Schema name");
        System.out.println("  --warehouse <warehouse>  Warehouse name");
        System.out.println("  --query <sql>           SQL query to execute (use quotes for multi-word queries)");
        System.out.println();
        System.out.println("Example:");
        System.out.println("  java com.workato.SnowflakeQueryRunner \\");
        System.out.println("    --account myaccount \\");
        System.out.println("    --user myuser \\");
        System.out.println("    --password mypassword \\");
        System.out.println("    --database MYDB \\");
        System.out.println("    --schema PUBLIC \\");
        System.out.println("    --warehouse COMPUTE_WH \\");
        System.out.println("    --query \"SELECT * FROM my_table LIMIT 10\"");
    }
    
    private static class ConnectionParams {
        String account;
        String user;
        String password;
        String database;
        String schema;
        String warehouse;
        String query;
    }
}
