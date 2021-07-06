package API;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.openapi.RouterBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;

import java.util.UUID;

public class IOT_API extends AbstractVerticle {
  private HttpServerResponse response;

  private PgPool pool;
  private Router router;
  private static final UUID getUUID = UUID.randomUUID();
  private ValidateData validar = null;

  public static void main(String[] args) {

    Vertx.vertx().deployVerticle(new IOT_API());
  }

  /**
   *
   */
  @Override

  public void start() { // Promise<Void> startPromise

    validar = new ValidateData();


    System.out.println(" Init.....");
    clientDB()

      .compose(this::setUpInitialData);

    System.out.println(" Init.....");

  }


  /**
   *
   * @param vd
   * @return
   */
  public Future<Void> setUpInitialData(Void vd) {
    System.out.println(" Init2 Router.....");

    RouterBuilder.create(vertx, "src/main/resources/openApi_060721.yaml")
      .onSuccess(routerBuilder -> {
        System.out.println(" Init3 call function .....");

        // call all endpoints
        // TODO: comprobar si hay sessione primero. caso contrario no se hara ningua operacion
   /*     this.haveSession()
          .compose(this::allFunction)
        .onFailure( " message error login");*/

        allFunction(routerBuilder);

        //Router router = Router.router(vertx)
        this.router = Router.router(vertx)
          .errorHandler(400, rc -> sendError(rc, 400, rc.failure().getMessage()));

        router.mountSubRouter("/v1", routerBuilder.createRouter());

        vertx
          .createHttpServer()
          .requestHandler(router)
          .listen(8080);

        System.out.println("pasamos");
      })
      .onFailure(Throwable::printStackTrace);
    return Future.succeededFuture();

  }

  /**
   *
   * @param router
   */
  private void allFunction(RouterBuilder router) {

    // CRUD Company
    router.operation("createCompany").handler(this::createCompany);
    router.operation("updateCompany").handler(this::updateCompany);
    router.operation("deleteCompany").handler(this::deleteCompany);
    router.operation("getAllCompany").handler(this::getAllCompany);
    router.operation("getCompanyById").handler(this::getCompanyById);
    router.operation("getAllDeviceByIdCompany").handler(this::getAllDeviceByIdCompany);
    router.operation("getAllUserByIdCompany").handler(this::getAllUserByIdCompany);
    router.operation("getAvgDeviceByIdCompany").handler(this::getAvgDeviceByIdCompany);
    router.operation("exportFileByIdCompany").handler(this::exportFileByIdCompany);

    // CRUD User
    router.operation("createUser").handler(this::createUser);
    router.operation("updateUser").handler(this::updateUser);
    router.operation("deleteUser").handler(this::deleteUser);
    router.operation("getAllUser").handler(this::getAllUser);
    router.operation("getUserById").handler(this::getUserById);
    router.operation("getAllDeviceByIdUser").handler(this::getAllDeviceByIdUser);
    router.operation("getAvgDeviceByIdUser").handler(this::getAvgDeviceByIdUser);
    router.operation("exportDataUser").handler(this::exportDataUser);

    // pass User forget
    router.operation("forgetPassword").handler(this::recoverPassword);
    router.operation("updatePassword").handler(this::updatePassword);

    // Session
    router.operation("statusSession").handler(this::statusSession);
    router.operation("login").handler(this::login);
    router.operation("logout").handler(this::logout);

    // CRUD Device
    router.operation("createDevice").handler(this::createDevice);
    router.operation("updateDevice").handler(this::updateDevice);
    router.operation("deleteDevice").handler(this::deleteDevice);
    router.operation("getAllDevice").handler(this::getAllDevice);
    router.operation("getDeviceById").handler(this::getDeviceById);
    router.operation("exportFileDevice").handler(this::exportFileDevice);

    // Notifications
    router.operation("createNotification").handler(this::createNotification);
    router.operation("getAllNotifications").handler(this::getAllNotifications);
    router.operation("deleteNotification").handler(this::deleteNotification);
    router.operation("updateNotification").handler(this::updateNotification);
    router.operation("getNotificationById").handler(this::getNotificationById);
    router.operation("getExportNotifications").handler(this::getExportNotifications);

    // Events
    router.operation("getAllEvents").handler(this::getAllEvents);
    router.operation("getEventById").handler(this::getEventById);
    router.operation("exportDataEvent").handler(this::exportDataEvent);

    // Incidence
    router.operation("getAllIncidence").handler(this::getAllIncidence);
    router.operation("deleteIncidence").handler(this::deleteIncidence);
    router.operation("getIncidenceById").handler(this::getIncidenceById);
    router.operation("exportDataIncidence").handler(this::exportDataIncidence);


  }


  // ====== ============= ENTITIES =======================

  // -- company

  private void createCompany(RoutingContext routingContext) {

  }

  private void updateCompany(RoutingContext routingContext) {
  }

  private void deleteCompany(RoutingContext routingContext) {
  }

  private void getAllCompany(RoutingContext routingContext) {
  }

  private void getCompanyById(RoutingContext routingContext) {
  }

  private void getAllDeviceByIdCompany(RoutingContext routingContext) {
  }

  private void getAllUserByIdCompany(RoutingContext routingContext) {
  }

  private void getAvgDeviceByIdCompany(RoutingContext routingContext) {
  }

  private void exportFileByIdCompany(RoutingContext routingContext) {
  }

  // User
  private void createUser(RoutingContext routingContext) {
  }

  private void updateUser(RoutingContext routingContext) {
  }

  private void deleteUser(RoutingContext routingContext) {
  }

  private void getAllUser(RoutingContext routingContext) {
  }

  private void getUserById(RoutingContext routingContext) {
  }

  private void getAllDeviceByIdUser(RoutingContext routingContext) {
  }

  private void getAvgDeviceByIdUser(RoutingContext routingContext) {
  }

  private void exportDataUser(RoutingContext routingContext) {
  }

  // password
  private void recoverPassword(RoutingContext routingContext) {
  }

  private void updatePassword(RoutingContext routingContext) {
  }


  // Session
  private void statusSession(RoutingContext routingContext) {
  }

  private void login(RoutingContext routingContext) {
  }

  private void logout(RoutingContext routingContext) {
  }

  // CRUD Device
  private void createDevice(RoutingContext routingContext) {
  }

  private void updateDevice(RoutingContext routingContext) {
  }

  private void deleteDevice(RoutingContext routingContext) {
  }

  private void getAllDevice(RoutingContext routingContext) {
  }

  private void getDeviceById(RoutingContext routingContext) {
  }

  private void exportFileDevice(RoutingContext routingContext) {
  }

  // Notifications
  private void createNotification(RoutingContext routingContext) {
  }

  private void getAllNotifications(RoutingContext routingContext) {
  }

  private void deleteNotification(RoutingContext routingContext) {
  }

  private void updateNotification(RoutingContext routingContext) {
  }

  private void getNotificationById(RoutingContext routingContext) {
  }

  private void getExportNotifications(RoutingContext routingContext) {
  }

  // Events
  private void getAllEvents(RoutingContext routingContext) {
  }

  private void getEventById(RoutingContext routingContext) {
  }

  private void exportDataEvent(RoutingContext routingContext) {
  }

  // Incidence
  private void getAllIncidence(RoutingContext routingContext) {
  }

  private void deleteIncidence(RoutingContext routingContext) {
  }

  private void getIncidenceById(RoutingContext routingContext) {
  }

  private void exportDataIncidence(RoutingContext routingContext) {
  }


  public Future<Void> clientDB(

  ) {

    PgConnectOptions connectOptions = new PgConnectOptions()
      .setPort(5432)
      .setHost("54.220.101.114")
      .setDatabase("chirpstack")
      .setUser("chirpstack_user")
      .setPassword("user3344");

// Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

// Create the client pool
    this.pool = PgPool.pool(vertx, connectOptions, poolOptions);
    //return this.pool;

    return Future.succeededFuture();
  }


  public static void sendError(
    final RoutingContext rc,
    final int code,
    final String cause) {

    final String message = HttpResponseStatus.valueOf(code).reasonPhrase();

    final JsonObject json = new JsonObject()
      .put("status", code)
      .put("title", message);

    if (cause != null && !cause.startsWith("ValidationException")) {
      json.put("cause", cause);
    }

    rc.response()
      .setStatusCode(code)
      .setStatusMessage(message)
      .putHeader("Content-Type", "application/json")
      .end(json.toBuffer());

  }

  public Future<Boolean> haveRegistre(PgPool pool, String id, String colum, String entity) {

    String find = "SELECT * FROM  " + entity + " WHERE " + colum + " = '" + id + "' ";
    System.out.println("queri METODO: " + find);

    pool
      .query(find)
      .execute()
      .compose(res -> {
        if (!res.iterator().hasNext()) {
          System.out.println("Estado: " + !res.iterator().hasNext());
          return Future.succeededFuture(true);

        }
        return Future.succeededFuture(false);
      });


    return Future.succeededFuture();
  }

}
