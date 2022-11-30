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
    TweetHandler th = new TweetHandler();
    logger.info(user_id);
    logger.info(type);
    logger.info(phrase);
    logger.info(hashtag);
    String resp = "TeamCloud,341275167549\n";
    resp += th.parse_search(user_id,type,phrase,hashtag);
    response.putHeader("content-type", "application/json").end(resp.trim());
  }

  private void getCC(RoutingContext routingContext) {
    String cc = routingContext.request().getParam("cc");
    HttpServerResponse response = routingContext.response();

    if (cc == null) {
      sendError(400, response);
    }
    //strParser sp = new strParser();
    //String ip = "eJyFk9tum0AQht9lr7mYw85heZWqigBDbClxqxg1lSK_ewcCBddJuldoF3a--f7hLXXH5nRO9be31Dw9PYy_58eXvvuVai-SRd3Ji1WpeR5TLbCsKo2n5z7VCcVJCDJsK1Xp2FyOcZhbMnOFdP1epZ8_XmNrOj0dUg1_XwLrilu2OBibl8d-nG5N1-qG6HJ6TDUKcRCwBVepVkpGViRAp9gb-oCiDL4AI6lnL1nvgKc7TAiLaVS-9OfDvx0vfB1k46Y9zEjvNZUwxx1MbOU_ZhCA0OMGl4KbmaYVHfDgOzMEtLjBzQ2IDAeUvRv4WE60D0ScobCtbgqIFQgHBVc35kiyMGc3LYT3xIWyUg4g2dTcaF7z1VZx8H4GmimoCDmia5kieKdAKOY5JiSMrRiwIDgbSfF7BlSPtDFagq_jabXDFmHYxXPb9lyH5JN4FBhIVQ2Ut3haMAn1sosH13Rolw65N-w36TTzN-f-9WHbg2m45639NEfpQgaCRKsqFVcjN2bgxRQL69IEZ1K2fCdLWZkMgxf168BwaLN3hjtZH_zln9bJYjm7ksI0uNc_g4cQeA==";
    String resp = "TeamCloud,341275167549\n";
    //resp += sp.parse_input(cc);
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
