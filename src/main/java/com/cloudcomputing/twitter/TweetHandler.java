package com.cloudcomputing.twitter;

import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlClient;

public class TweetHandler {
  MySQLConnectOptions connectOptions = new MySQLConnectOptions()
    .setPort(3306)
    .setHost("m3db.cz0wq66lu07w.us-east-1.rds.amazonaws.com")
    .setDatabase("m3")
    .setUser("user")
    .setPassword("secret");

  // Pool options
  PoolOptions poolOptions = new PoolOptions()
    .setMaxSize(5);

  // Create the client pool
  SqlClient client = MySQLPool.client(connectOptions, poolOptions);

  public String parse_search(String input) {

    return input;
  }
}
