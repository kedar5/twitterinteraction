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

import java.util.*;

import static com.cloudcomputing.twitter.TweetHandler.phrasechecker;
import static com.cloudcomputing.twitter.TweetHandler.sortByValue;
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
  private void getTweets(RoutingContext routingContext) throws NullPointerException{
    String user_id = routingContext.request().getParam("user_id");
    String type = routingContext.request().getParam("type");
    String phrase = routingContext.request().getParam("phrase");
    String hashtag = routingContext.request().getParam("hashtag");
    HttpServerResponse response = routingContext.response();
    if (user_id == null ||type == null || phrase == null|| hashtag == null) {
      sendError(400, response);
    }
//    TweetHandler th = new TweetHandler();
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
    String sql ="SELECT * FROM Data WHERE uid2= "+user_id+" and uid2 IS NOT NULL or uid1= "+user_id+" and uid2 IS NOT NULL;";
    Future<RowSet<Row>> res1 = client.query(sql).execute();
    res1.onComplete(ar ->{
      if (ar.succeeded()){
        String resp = "TeamCloud,341275167549\n";
//        System.out.println(ar.result());
        RowSet<Row> result = ar.result();
        // Get list of all User Ids
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
        System.out.println("All Ids: "+alluserids.size());
        HashSet<String> hset = new HashSet<String>(alluserids);
        System.out.println("New Ids"+ hset.size());
        // Iterate through all user ids
        for (String u_id : hset){
          int rp_counter = 0;
          int rt_counter = 0;

          for (Row row : result){
            int uid1 = row.getInteger(0);
            int uid2 = row.getInteger(1);
            String rt_txt =row.getString(2);
            String rp_txt =row.getString(3);

            if (String.valueOf(uid1).equals(u_id) || String.valueOf(uid2).equals(u_id)){
              //System.out.println("RT TEXTTTTTTTTTTT"+rt_txt+"\n"+rp_txt);
              if (!rt_txt.equals("\"\"") && rt_txt != null ){rt_counter++;}
              else  if (!rp_txt.equals("\"\"") && rp_txt != null){ rp_counter ++;}
            }
          }
          //System.out.println("Scores : "+rt_counter+","+rp_counter);
          double interaction_score = log(1 + 2 * rp_counter + rt_counter);
          //System.out.println("INTER : "+ interaction_score);
          interaction_score_map.put(u_id,interaction_score);
        }

        for (Row row : result) {
          int uid1 = row.getInteger(0);
          int uid2 = row.getInteger(1);
          String all_hashtags_1 = row.getString(7);
          String all_hashtags_2 = row.getString(9);
          int counter =0;
          double hashtag_score;
          if (all_hashtags_1 == null|| all_hashtags_2 == null){
            hashtag_score=1;
          }
          else{
            List<String> hashtags_1 = Arrays.asList(all_hashtags_1.split(" "));
            List<String> hashtags_2 = Arrays.asList(all_hashtags_2.split(" "));
            for (String hash: hashtags_1){
              if (hashtags_2.contains(hash)){
                counter ++;
              }
            }
            if (counter > 10){
              hashtag_score =  (1 + log(1 + counter - 10));
            }
            else{
              hashtag_score =1;
            }
          }
          if (String.valueOf(uid1).equals(user_id)){
            hashing_score_map.put(String.valueOf(uid2),hashtag_score);
            //System.out.println("CORRECT" + String.valueOf(uid2) +","+ hashtag_score);
          }
          else if (String.valueOf(uid2).equals(user_id)){
            hashing_score_map.put(String.valueOf(uid1),hashtag_score);
            //System.out.println("CORRECT" + String.valueOf(uid1) +","+ hashtag_score);
          }
        }
        for (Row row : result) {
          int uid1 = row.getInteger(0);
          int uid2 = row.getInteger(1);
          String rt_txt =row.getString(2);
          String rp_txt =row.getString(3);
          String info_1 = row.getString(6);
          String info_2 = row.getString(8);
          String all_hashtags_1 = row.getString(7);
          String all_hashtags_2 = row.getString(9);
          if (type.equals("reply")){
            List<String> keywordList = phrasechecker(rp_txt,phrase,user_id,uid1,uid2,all_hashtags_1,all_hashtags_2,hashtag,info_1,info_2 );
            double keyword_score= Double.valueOf(keywordList.get(0));
            if (String.valueOf(uid1).equals(user_id)){
              keyword_score_map.put(String.valueOf(uid2),keyword_score);
              //System.out.println("CORRECT" + String.valueOf(uid2) +","+ hashtag_score);
              outputmap.put(String.valueOf(uid2),keywordList);
            }
            else if (String.valueOf(uid2).equals(user_id)){
              keyword_score_map.put(String.valueOf(uid1),keyword_score);
              //System.out.println("CORRECT" + String.valueOf(uid1) +","+ hashtag_score);
              outputmap.put(String.valueOf(uid2),keywordList);
            }
          }
          else if (type.equals("retweet")){
            List<String> keywordList = phrasechecker(rt_txt,phrase,user_id,uid1,uid2,all_hashtags_1,all_hashtags_2,hashtag,info_1,info_2);
            double keyword_score= Double.valueOf(keywordList.get(0));
            if (String.valueOf(uid1).equals(user_id)){
              keyword_score_map.put(String.valueOf(uid2),keyword_score);
              outputmap.put(String.valueOf(uid2),keywordList);
              //System.out.println("CORRECT" + String.valueOf(uid2) +","+ hashtag_score);
            }
            else if (String.valueOf(uid2).equals(user_id)){
              keyword_score_map.put(String.valueOf(uid1),keyword_score);
              outputmap.put(String.valueOf(uid2),keywordList);
              //System.out.println("CORRECT" + String.valueOf(uid1) +","+ hashtag_score);
            }
          }
          else if (type.equals("both")){
            List<String> keywordList = phrasechecker(rt_txt+rp_txt,phrase,user_id,uid1,uid2,all_hashtags_1,all_hashtags_2,hashtag,info_1,info_2);
            double keyword_score= Double.valueOf(keywordList.get(0));
            if (String.valueOf(uid1).equals(user_id)){
              keyword_score_map.put(String.valueOf(uid2),keyword_score);
              outputmap.put(String.valueOf(uid2),keywordList);
              //System.out.println("CORRECT" + String.valueOf(uid2) +","+ hashtag_score);
            }
            else if (String.valueOf(uid2).equals(user_id)){
              keyword_score_map.put(String.valueOf(uid1),keyword_score);
              outputmap.put(String.valueOf(uid2),keywordList);
              //System.out.println("CORRECT" + String.valueOf(uid1) +","+ hashtag_score);
            }
          }
        }
        //for (String key : hashing_score_map.keySet()){
        for (String key : alluserids){
          if (hashing_score_map.keySet().contains(key) && keyword_score_map.keySet().contains(key) && interaction_score_map.keySet().contains(key)){
            double finalscore = hashing_score_map.get(key) * keyword_score_map.get(key) * interaction_score_map.get(key);
            final_score_map.put(key, finalscore);
          }
        }

        HashMap<String, Double> final_map = sortByValue(final_score_map);
//        System.out.println("HashScoreMap in: "+Arrays.asList(hashing_score_map));
//        System.out.println("KeywordScoreMap in: "+Arrays.asList(keyword_score_map));
//        System.out.println("FinalMap in: "+Arrays.asList(final_score_map));
//        System.out.println("Sorted FinalMap in: "+Arrays.asList(final_map));
//        System.out.println("OutputMap in: "+Arrays.asList(outputmap));
        StringBuilder sb = new StringBuilder();

        for (String k : final_map.keySet()){
          if ( outputmap.get(k).get(1) == null){
            System.out.println("OMEGALUL BROOOOOOO");
          }
          String uid = outputmap.get(k).get(1);
          String username = outputmap.get(k).get(2);
          String description = outputmap.get(k).get(3);
          String texter = outputmap.get(k).get(4);
          String line = uid + '\t' +username+ '\t' +description+ '\t' +texter+ '\n';
          sb.append(line);

        }
        resp += sb;
        response.putHeader("content-type","application/json").end(resp.trim());

      }
      else {
        System.out.println("Failure: " + ar.cause().getMessage());


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
