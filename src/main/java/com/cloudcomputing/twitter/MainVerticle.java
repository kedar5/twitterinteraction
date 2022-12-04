package com.cloudcomputing.twitter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

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
    String resp = "TeamCloud,341275167549\n";
    resp += db.parse_search(user_id,type,phrase,hashtag);
    response.putHeader("content-type", "application/json").end(resp.trim());
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
