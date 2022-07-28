package API;

import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Optional;

public class QueryDB {

  public static String sqlUpdateCompany(RoutingContext routingContext){
    JsonObject company = routingContext.getBodyAsJson();
    String id = routingContext.request().getParam("companyId");

    final String sql = " UPDATE app_chirpstack_user.company SET  owner_id = '" + company.getString("adminId") + "', " +
      " updated_at = default, name = '" + company.getString("name") + "' WHERE id = '" + id + "' ";
    return sql;
  }

  public static String sqlGetCompanyById(RoutingContext routingContex){

    String id = routingContex.request().getParam("companyId");

    String  sql =
      "SELECT " +
        "t1.id AS company_id, " +
        "t1.owner_id AS admin_id, \n" +
        "t1.name, t2.id AS supervisor_id, \n" +
        "(SELECT COUNT(*) FROM app_chirpstack_user.company_app " +
        "WHERE company_id = t1.id) AS  total_app \n" +
        "FROM app_chirpstack_user.company t1 " +
        "INNER JOIN app_chirpstack_user.admin_company tX \n" +
        "ON t1.id = tX.company_id \n" +
        "INNER JOIN app_chirpstack_user.admin t2 " +
        "ON tX.admin_id = t2.id  \n" +
        "WHERE t1.id = '" + id + "' ";

    return sql;
  }

  public static String sqlCountAppByIdCompany(RoutingContext routingContex){
    String id = routingContex.request().getParam("companyId");
    String sql =
      "SELECT  COUNT(*) " +
        "FROM app_chirpstack_user.company_app t1 \n" +
        "INNER JOIN app_chirpstack_user.app  t2 \n" +
        " ON t1.app_id = t2.id \n" +
        "INNER JOIN app_chirpstack_user.company tx " +
        "ON t1.company_id = tx.id \n" +
        "WHERE t1.company_id = '" + id + "'";
    return sql;
  }


  public static String sqlAddAppToCompany(RoutingContext routingContext){
    String companyId = routingContext.request().getParam("companyId");
    String appId = routingContext.request().getParam("applicationId");

    String sql = "INSERT INTO " +
      "app_chirpstack_user.company_app" +
      "(app_id, company_id) " +
      "VALUES ( '" + appId + "', " +
      "'" + companyId + "' )";
    return sql;
  }

  public static String sqlRemoveAppToCompany(RoutingContext routingContext){


    String companyId = routingContext.request().getParam("companyId");
    String appId = routingContext.request().getParam("applicationId");

    String sql = "DELETE FROM app_chirpstack_user.company_app WHERE  app_id = '" + appId + "' AND company_id = '" + companyId + "' ";
    return sql;
  }

  public static String sqlGetAllDeviceByIdApp(RoutingContext routingContex){
    String id = routingContex.request().getParam("applicationId");

    String sql =
    "SELECT COUNT(*) " +
      "FROM app_chirpstack_user.device t1 " +
      "INNER JOIN app_chirpstack_user.app t2 ON t1.app_id = t2.id " +
      " WHERE t2.id = '" + id + "'";
    return sql;
  }

  public static String sqlInsertSessionData(JsonObject session, String sessionId){
    String sql = "INSERT INTO app_chirpstack_user.session_up2  " +
      " (id, user_id, email, name, role, token) " +
      "VALUES ( " +
      "'" + sessionId + "'," +
      " '" + session.getJsonObject("user").getString("id") + "', " +
      "'" + session.getJsonObject("user").getString("email") + "'," +
      "'" + session.getJsonObject("user").getString("name") + "'," +
      " '" + session.getJsonObject("user").getString("role") + "', " +
      " '" + session.getString("token") + "') ";

    return sql;
  }

  public static String sqlUpdateLastSession(JsonObject session){
    String sql = " UPDATE app_chirpstack_user.user SET last_seen = 'NOW()' " +
      "WHERE id = '" + session.getJsonObject("user").getString("id") + "'";
    return sql;
  }

  public static String sqlFilterDevice(RoutingContext routingContext) {
    final StringBuilder filter = new StringBuilder();

    filter.append(
      "SELECT \n" +
        "t1.id, " +
        "t1.app_id, " +
        "t1.serial, " +
        "t2.name  \n" +
        "FROM app_chirpstack_user.device t1 \n" +
        "INNER JOIN app_chirpstack_user.user_device tx " +
        "ON t1.id = tx.device_id \n" +
        "INNER JOIN app_chirpstack_user.app t2 ON t1.app_id = t2.id ");

    MultiMap map = routingContext.request().params();

    Optional.ofNullable(map.get("dateFrom"))
      .ifPresent(v -> filter.append(" WHERE t4.received_at BETWEEN " + "  '" + v + "' \n"));
    Optional.ofNullable(map.get("dateTo"))
      .ifPresent(v -> filter.append(" AND " + "  '" + v + "' "));

    Optional.ofNullable(map.get("temperature"))
      .ifPresent(v -> filter.append(" AND  t5.object->'temperatura' = " + "  '" + v + "' \n"));
    Optional.ofNullable(map.get("humidity"))
      .ifPresent(v -> filter.append(" AND t5.object->'humedad' = " + "  '" + v + "' \n"));

    Optional.ofNullable(map.get("sortBy"))
      .ifPresent(v -> filter.append(" ORDER BY t4.name " + "  " + v + " \n"));
    Optional.ofNullable(map.get("limit"))
      .ifPresent(v -> filter.append(" LIMIT " + v));

    return filter.toString();
  }

  public static String sqlGetDeviceById(RoutingContext routingContext){
    String id = routingContext.request().getParam("deviceId");

    String sql =
      " SELECT " +
        " t1.id, t1.app_id, t1.serial, t1.name, \n" +
        "(SELECT name from app_chirpstack_user.app where id = t1.app_id) AS app_name \n" +
        " FROM app_chirpstack_user.device t1  " +
        " INNER JOIN app_chirpstack_user.company_app  tx ON t1.app_id = tx.app_id \n" +
        "WHERE t1.id = '" + id + "' ";

    return sql;
  }


  public static String sqlFilterDeviceCompany(RoutingContext routingContext) {
    final StringBuilder filter = new StringBuilder();

    String id = routingContext.request().getParam("companyId");
    System.out.println("jdjdk");

    filter.append(
      " SELECT " +
        "t1.id, " +
        "t1.app_id, " +
        "t1.serial, " +
        "t1.name AS device_name, " +
        "(SELECT name from app_chirpstack_user.app where id = t1.app_id) AS app_name " +
        " FROM app_chirpstack_user.device t1  " +
        " INNER JOIN app_chirpstack_user.company_app  tx " +
        "ON t1.app_id = tx.app_id " +
        "WHERE tx.company_id = '" + id + "' ");

    MultiMap map = routingContext.request().params();

    Optional.ofNullable(map.get("sortBy"))
      .ifPresent(v -> filter.append(" ORDER BY t1.name " + "  " + v + " \n"));
    Optional.ofNullable(map.get("limit"))
      .ifPresent(v -> filter.append(" LIMIT " + v));


    return filter.toString();
  }

  public static String getSqlCountDeviceCompany(RoutingContext routingContext) {
    String id = routingContext.request().getParam("companyId");

    String count = " SELECT " +
      " COUNT (*) " +
      " FROM app_chirpstack_user.device t1 " +
      "INNER JOIN app_chirpstack_user.company_app  tx " +
      "ON t1.app_id = tx.app_id " +
      "WHERE tx.company_id = '" + id + "' ";
    return count;
  }


  public static String sqlFilterApp(RoutingContext routingContext) {

    final StringBuilder filter = new StringBuilder();

    filter.append(
      "SELECT " +
        "t1.id, " +
        "t1.name, " +
        "t1.serial, " +
        "t2.id AS applicationId  " +
        "FROM app_chirpstack_user.device t1 " +
        "INNER JOIN app_chirpstack_user.app t2 " +
        "ON t1.app_id = t2.id ");

    MultiMap map = routingContext.request().params();
    Optional.ofNullable(map.get("applicationId")).ifPresent(v -> filter.append(" WHERE t2.id =" + "  '" + v + "' "));
    Optional.ofNullable(map.get("name")).ifPresent(v -> filter.append(" AND  t1.name = " + "  '" + v + "' "));
    Optional.ofNullable(map.get("sortBy")).ifPresent(v -> filter.append(" ORDER BY t1.name " + v));
    Optional.ofNullable(map.get("limit")).ifPresent(v -> filter.append(" LIMIT " + v));


    return filter.toString();
  }

  public static String sqlLogin(RoutingContext routingContext) {

    String email = routingContext.request().getParam("email");
    String password = routingContext.request().getParam("password");
    String login =
      " SELECT " +
        "t1.id, t1.company_id, " +
        "t1.created_at, " +
        "Tx.owner_id as role_id, \n" +
        "t1.last_seen, " +
        "t1.name, t1.surname, t1.email, \n" +
        "t1.password " +
        "FROM app_chirpstack_user.user t1 \n" +
        "INNER JOIN app_chirpstack_user.user_device t2 " +
        "ON t1.id = t2.user_id \n" +
        "INNER JOIN app_chirpstack_user.company Tx " +
        "ON t1.id = Tx.owner_id  \n" +
        "WHERE email = '" + email + "' " +
        "AND password = '" + password + "' LIMIT 1";
    return login;

  }

  public static String sqlSession(RoutingContext routingContext) {
    String id = routingContext.user().principal().getString("sessionId");


    String session = "SELECT id FROM app_chirpstack_user.session_up2  WHERE id = '" + id + "' ";
    return session;
  }


  public static String sqlFilterCompany(RoutingContext routingContext) {

    final StringBuilder filter = new StringBuilder();

    filter.append(
      "SELECT " +
        "t1.id AS company_id, " +
        "t1.owner_id AS admin_id, \n " +
        "t1.name, t2.id AS supervisor_id, " +
        "(SELECT COUNT(*) FROM app_chirpstack_user.company_app \n " +
        "WHERE company_id = t1.id) AS  total_app " +
        "FROM app_chirpstack_user.company t1 " +
        "INNER JOIN app_chirpstack_user.admin_company tX \n " +
        "ON t1.id = tX.company_id " +
        "INNER JOIN app_chirpstack_user.admin t2 " +
        "ON tX.admin_id = t2.id ");

    MultiMap map = routingContext.request().params();

    Optional.ofNullable(map.get("name")).ifPresent(vd -> filter.append(" WHERE t1.name LIKE " + "  '%" + vd + "%' "));
    Optional.ofNullable(map.get("sortBy")).ifPresent(vd -> filter.append(" ORDER BY t1.name " + vd));
    Optional.ofNullable(map.get("limit")).ifPresent(vd -> filter.append(" LIMIT " + vd));

    return filter.toString();
  }

  public static String getRole(String userId, String companyId) {

    String role = null;

    if (userId != companyId) {
      role = "User";
    } else {
      role = "Admin";
    }

    return role;

  }

  public static String sqlFilterAppToCompany(RoutingContext routingContext) {

    MultiMap map = routingContext.request().params();
    String id = routingContext.request().getParam("companyId");
    final StringBuilder filter = new StringBuilder();

    filter.append(
      "SELECT t2.id, t2.created_at, \n" +
        "t2.updated_at , t2.name \n" +
        "FROM app_chirpstack_user.company_app t1 \n" +
        "INNER JOIN app_chirpstack_user.app  t2 \n" +
        "ON t1.app_id = t2.id \n" +
        "INNER JOIN app_chirpstack_user.company tx ON t1.company_id = tx.id \n" +
        "WHERE t1.company_id = '" + id + "'");

    Optional.ofNullable(map.get("dateTo")).ifPresent(v -> filter.append(" AND t2.created_at BETWEEN '" + v + "' "));
    Optional.ofNullable(map.get("dateFrom")).ifPresent(v -> filter.append(" AND '" + v + "' "));

    Optional.ofNullable(map.get("sortBy")).ifPresent(v -> filter.append(" ORDER BY t1.name " + v));
    Optional.ofNullable(map.get("limit")).ifPresent(v -> filter.append(" LIMIT " + v));

    return filter.toString();
  }
}
