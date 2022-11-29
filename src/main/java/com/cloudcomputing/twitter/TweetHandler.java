package com.cloudcomputing.twitter;

import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;

public class TweetHandler {
  MySQLConnectOptions connectOptions = new MySQLConnectOptions()
    .setPort(3306)
    .setHost("testdb.cz0wq66lu07w.us-east-1.rds.amazonaws.com")
    .setDatabase("testdb")
    .setUser("admin")
    .setPassword("mh20may1996");

  // Pool options
  PoolOptions poolOptions = new PoolOptions()
    .setMaxSize(5);

  // Create the client pool
  SqlClient client = MySQLPool.client(connectOptions, poolOptions);

  public String parse_search(String input) {
    System.out.println(input);
    client
      .query("SELECT * FROM 'final'")
      .execute(ar -> {
        if (ar.succeeded()) {
          RowSet<Row> result = ar.result();
          System.out.println("Got " + result.size() + " rows ");
          for (Row row : result) {
            System.out.println("User " + row.getString(0) + " " + row.getString(1));
          }
        } else {
          System.out.println("Failure: " + ar.cause().getMessage());
        }
      });
    return input;
  }
}
