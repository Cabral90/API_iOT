package prova;

import connectionDB.MainVerticleDB;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

public class Prava extends AbstractVerticle {

  private HttpServerResponse response;
  final Router router = Router.router(vertx);

  public static void main(String[] args) {
    Vertx.vertx().deployVerticle(new Prava());
  }


  @Override
  public void start() {
    // aqui arraca la faena
    probarRout();
  }

  public HttpServerResponse Router() {
    Router router = null;
    // Route route = router.route(path);
    Route route = router.get();
    route.handler(ctx -> {

      response = ctx.response();
      // enable chunked responses because we will be adding data as
      // we execute over other handlers. This is only required once and
      // only if several handlers do output.
      response.setChunked(true);

      //response.write("route1\n");

      // Call the next matching route after a 5 second delay
      ctx.vertx().setTimer(5000, tid -> ctx.next());
      ctx.response().end(); //
    });

    return response;
  }

  public void probarRout() {

    router.get("/sales/:saleID").handler(ctx -> {
      String saleID = ctx.request().getParam("saleID");
      HttpServerResponse response = ctx.response();
      if (saleID == null) {
        ctx.fail(400);
        System.out.println("code: 400 ");
        return;
      }
      response.setStatusCode(201).end();
      vertx.createHttpServer().requestHandler(router).listen(8080);

    });

  }
}

