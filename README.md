# DB1614
Program to run snowflake query

## To build.

java >= 8 and maven should be available.

```
mvn package
```

## To run

```
java --add-opens=java.base/java.nio=ALL-UNNAMED -Dlogging.config=src/test/resources/logback.xml -jar target/snowflakequery-1.0-SNAPSHOT.jar \
--account uya41974 --user garrethintegrator --password password --database TESTING_DB --schema PUBLIC --warehouse COMPUTE_WH --query "SELECT 'hello1','hello2','hello3' FROM long_table LIMIT 50000"
```
