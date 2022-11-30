package com.cloudcomputing.twitter;

import io.vertx.core.Vertx;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;

public class TweetHandler {
  Vertx vertx = Vertx.vertx();
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
  SqlClient client = MySQLPool.pool(vertx, connectOptions, poolOptions);
//  MySQLPool pool = MySQLPool.pool(vertx, connectOptions, poolOptions);
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
//    pool.getConnection().compose(conn -> {
//      System.out.println("Got a connection from the pool");
//
//      // All operations execute on the same connection
//      return conn
//        .query("SELECT * FROM 'final'")
//        .execute()
//        .onComplete(ar -> {
//          // Release the connection to the pool
//          conn.close();
//        });
//    }).onComplete(ar -> {
//      if (ar.succeeded()) {
//
//        System.out.println("Done");
//      } else {
//        System.out.println("Something went wrong " + ar.cause().getMessage());
//      }
//    });

    return input;
  }
}
