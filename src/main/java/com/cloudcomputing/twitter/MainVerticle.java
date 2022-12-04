package com.cloudcomputing.twitter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static java.lang.Math.log;

public class MainVerticle extends AbstractVerticle {
  private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    router.get("/").handler(this::home);
    router.get("/twitter").handler(this::getTweets);
    vertx.createHttpServer().requestHandler(router).listen(8887, http -> {
      if (http.succeeded()) {
//        startPromise.complete();
        logger.info("HTTP server started on port 8887");
        System.out.println("HTTP server started on port 8887");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }
  private void getTweets(RoutingContext routingContext) {
    String user_id = routingContext.request().getParam("user_id");
    String type = routingContext.request().getParam("type");
    String phrase = routingContext.request().getParam("phrase");
    String hashtag = routingContext.request().getParam("hashtag");
    HttpServerResponse response = routingContext.response();
    if (user_id == null ||type == null || phrase == null|| hashtag == null) {
      sendError(400, response);
    }
    TweetHandler th = new TweetHandler();
    DbHandler db = new DbHandler();
    logger.info(user_id);
    logger.info(type);
    logger.info(phrase);
    logger.info(hashtag);



    MySQLConnectOptions connectOptions = new MySQLConnectOptions()
      .setPort(3306)
      .setHost("m3db.c2dez1aoybbc.us-east-1.rds.amazonaws.com")
      .setDatabase("m3")
      .setUser("admin")
      .setPassword("yyp31234");
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);
    SqlClient client = MySQLPool.pool(vertx, connectOptions, poolOptions);
    HashMap<String, Double> hashing_score_map = new HashMap<>();
    HashMap<String, Double> interaction_score_map = new HashMap<>();
    HashMap<String, Double> keyword_score_map = new HashMap<>();
    HashMap<String, Double> final_score_map = new HashMap<>();
    HashMap<String, List<String>> outputmap = new HashMap<>();
    ArrayList<String> alluserids = new ArrayList<String>();
    String sql ="SELECT DISTINCT uid1, uid2 FROM Data WHERE uid2=  "+user_id+" or uid1= "+user_id+" ;";
    Future<RowSet<Row>> res1 = client.query(sql).execute();
    res1.onComplete(ar ->{
      if (ar.succeeded()){
        String resp = "TeamCloud,341275167549\n";
        System.out.println(ar.result());
        RowSet<Row> result = ar.result();
        for (Row row : result) {
          int uid1 = row.getInteger(0);
          int uid2 = row.getInteger(1);
          if (String.valueOf(uid1).equals(user_id)){
            alluserids.add(String.valueOf(uid2));
          }
          else if (String.valueOf(uid2).equals(user_id)){
            alluserids.add(String.valueOf(uid1));
          }
        }
        for (String u_id : alluserids){
          int rp_counter = 0;
          int rt_counter = 0;

          for (Row row : result){
            int uid1 = row.getInteger(0);
            int uid2 = row.getInteger(1);
            String rt_txt =row.getString(2);
            String rp_txt =row.getString(3);
            if (String.valueOf(uid1).equals(u_id) || String.valueOf(uid2).equals(u_id)){
              //System.out.println("RT TEXTTTTTTTTTTT"+rt_txt+"\n"+rp_txt);
              if (!rt_txt.equals("\"\"")){rt_counter++;}
              else  if (!rp_txt.equals("\"\"")){ rp_counter ++;}
            }
          }
          //System.out.println("Scores : "+rt_counter+","+rp_counter);
          double interaction_score = log(1 + 2 * rp_counter + rt_counter);
          //System.out.println("INTER : "+ interaction_score);
          interaction_score_map.put(u_id,interaction_score);
        }
        resp += Arrays.asList(interaction_score_map);
        response.putHeader("content-type","application/json").end(resp.trim());

      }
    });



  }


  private void home(RoutingContext routingContext) {
    String home = "Welcome to 15-619 Team Clear Project!\n\nReference Server Usage:\nMicroservice 3: /twitter?user_id=<USER ID>&type=<retweet>&phrase=<hello%20cc>&hashtag=<cmu>";
    HttpServerResponse response = routingContext.response();
    response.putHeader("content-type", "application/json").end(home);
  }


  private void sendError(int statusCode, HttpServerResponse response) {
    response.setStatusCode(statusCode).end();
  }


}
