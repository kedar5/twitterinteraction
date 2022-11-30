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
    .setHost("m3db.c2dez1aoybbc.us-east-1.rds.amazonaws.com")
    .setDatabase("m3")
    .setUser("admin")
    .setPassword("yyp31234");

  // Pool options
  PoolOptions poolOptions = new PoolOptions()
    .setMaxSize(5);

  // Create the client pool
  SqlClient client = MySQLPool.pool(vertx, connectOptions, poolOptions);
//  MySQLPool pool = MySQLPool.pool(vertx, connectOptions, poolOptions);
  public String parse_search(String user_id,String type, String phrase, String hashtag) {
    String values = (user_id +","+ type+","+phrase+","+hashtag);
    System.out.println(values);
    client
      .query("SELECT * FROM Data WHERE id2 = 582276888")
      .execute(ar -> {
        if (ar.succeeded()) {
          RowSet<Row> result = ar.result();
          System.out.println("Got " + result.size() + " rows ");
          for (Row row : result) {
            System.out.println("Row " + row);
          }
        } else {
          System.out.println("Failure: " + ar.cause().getMessage());
        }
      });

    return values;
  }
}
