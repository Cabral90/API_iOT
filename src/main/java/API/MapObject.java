package API;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class MapObject {

  public static JsonObject session(RoutingContext routingContext){
    String sessionId = routingContext.user().principal().getString("sessionId");
    String companyId = routingContext.request().getParam("companyId");

    System.out.println("companyId: " + companyId);

    JsonObject updateSession = new JsonObject()
      .put("sessionId", sessionId)
      .put("userId", routingContext.user().principal().getString("userId"))
      .put("companyId", companyId) // TODO: como obtener este ID ?
      .put("name", routingContext.user().principal().getString("name"))
      .put("surname", routingContext.user().principal().getString("surname"))
      .put("role", routingContext.user().principal().getString("role"))
      .put("email", routingContext.user().principal().getString("email"));

    return updateSession;
  }
}
