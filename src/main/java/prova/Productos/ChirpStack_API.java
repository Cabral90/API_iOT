package prova.Productos;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.*;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.SqlClient;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ChirpStack_API extends AbstractVerticle {
  private JDBCClient client;
  private JDBCPool pool;

  //ChirpStack_API that = this;
  final JsonObject loadedConfig = new JsonObject();

  public static void main(String[] args) {
    Vertx.vertx().deployVerticle(new ChirpStack_API());
  }

  @Override
  public void start(Promise<Void> start) {

    //Router router = Router.router(vertx);
    myConetion()
      .compose(this::myConnetion);
//      .compose(this::myDATA)
//      .compose(this::myConnetion);
//      //.compose(this::startHttpServer);

  }




  public Future<JDBCClient> myConetion() {

    client = JDBCClient.createShared(vertx, new JsonObject()
//      .put("url", "jdbc:hsqldb:mem:test?shutdown=true")
//      .put("driver_class", "org.hsqldb.jdbcDriver"));

      .put("url", "jdbc:postgresql://54.220.101.114:5432/chirpstack")
      .put("driver_class", "org.postgresql.Driver")
      .put("user","postgres")
      .put("password", "admin4433"));


    return Future.succeededFuture();
  }


  public Future<Router> myRouter(Void unused) { // Router router
//    Router router = Router.router(this.vertx);
//    SessionStore store = LocalSessionStore.create(this.vertx);
//    router.route().handler(LoggerHandler.create());
//    router.route().handler(SessionHandler.create(store));
//    router.route().handler(CorsHandler.create("localhost"));

    Router router = Router.router(vertx);




    router.route().handler(BodyHandler.create());

    // metodos/verbos

    router.get("/products/:productID").handler(this::handleGetProduct);

    //return Promise.succeededPromise(router).future();

    return Future.succeededFuture();
  }


  private Future<Router> myConnetion(JDBCClient client) { // , Handler handler

    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    //routingContex = null;
    router.route("/products*").handler(routingContext ->client.getConnection(res -> {
    if (res.failed()) {

      routingContext.fail(res.cause());
    } else {
      SQLConnection conn = res.result();

      // save the connection on the context
      routingContext.put("conn", conn);

      // we need to return the connection back to the jdbc pool. In order to do that we need to close it, to keep
      // the remaining code readable one can add a headers end handler to close the connection.
      routingContext.addHeadersEndHandler(done -> conn.close(v -> { }));

      routingContext.next();
    }
  }))

    .failureHandler(routingContext -> {
    SQLConnection conn = routingContext.get("conn");
    if (conn != null) {
      conn.close(v -> {
      });
    }
  });

    router.get("/products/:productID").handler(this::handleGetProduct);
//    router.post("/products").handler(that::handleAddProduct);
//    router.get("/products").handler(that::handleListProducts);

    this.vertx.createHttpServer().requestHandler(router).listen(8080);

    return Future.succeededFuture();
  }
  private void handleGetProduct(RoutingContext routingContext) {
    String productID = routingContext.request().getParam("productID");
    HttpServerResponse response = routingContext.response();
    if (productID == null) {
      sendError(400, response);
    } else {
      SQLConnection conn = routingContext.get("conn");

      conn.queryWithParams("SELECT id, name, price, weight FROM products where id = ?", new JsonArray().add(Integer.parseInt(productID)), query -> {
        if (query.failed()) {
          sendError(500, response);
        } else {
          if (query.result().getNumRows() == 0) {
            sendError(404, response);
          } else {
            response.putHeader("content-type", "application/json").end(query.result().getRows().get(0).encode());
          }
        }
      });
    }

  }


  private void sendError(
    int statusCode, HttpServerResponse response
  ) {
    response.setStatusCode(statusCode).end();
  }

//  public Future<HttpServer> startHttpServer(Router router) {
//    JsonObject http = loadedConfig.getJsonObject("http");
//    int httpPort = http.getInteger("port");
//    HttpServer server = vertx.createHttpServer().requestHandler(router);
//
//    return Future.<HttpServer>future(promise -> server.listen(httpPort, promise));
//  }

  public Future<Void> storeConfig(JsonObject config) {
    loadedConfig.mergeIn(config);
    return Promise.<Void>promise().future();
    //return Future.succeededFuture();
  }


  public Future<JsonObject> doConfig() {

//    ConfigStoreOptions defaultConfig = new ConfigStoreOptions()
//      .setType("file")
//      .setFormat("json")
//      .setConfig(new JsonObject().put("path", "config.json"));

    ConfigStoreOptions cliConfig = new ConfigStoreOptions()
      .setType("json")
      .setConfig(config());



    ConfigRetrieverOptions opts = new ConfigRetrieverOptions()
      //.addStore(defaultConfig)
      .addStore(cliConfig);

    ConfigRetriever cfgRetriever = ConfigRetriever.create(this.vertx, opts);

    return Future.future(cfgRetriever::getConfig);
    //return Future.succeededFuture();
  }


  @Override
  public void stop(Promise<Void> stopPromise) {
    this.execStop().onComplete(stopPromise);
  }

  public @NotNull Future<Void> execStop() {
    return Optional
      .ofNullable(this.pool)
      .map(SqlClient::close)
      .orElseGet(Future::succeededFuture);
  }

  public Future<Void> myDATA(Void unused) {

    JsonObject dbConfig = loadedConfig.getJsonObject("db", new JsonObject()
      .put("url", "jdbc:postgresql://54.220.101.114:5432/chirpstack")
      .put("driver_class", "org.postgresql.Driver")
      .put("user", "postgres")
      .put("password", "admin4433")
      .put("max_pool_size", 1)
      .put("initial_pool_size", 1)
      .put("min_pool_size", 1)
      .put("max_statements", 0)
      .put("max_statements_per_connection", 0)
      .put("max_idle_time", 0));


    //return Promise.<Void>succeededPromise().future();
    this.client = JDBCClient.createShared(vertx, dbConfig);
    return Promise.<Void>promise().future();
    //return Future.succeededFuture();
  }





}






