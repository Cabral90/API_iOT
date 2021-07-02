package API;

import io.vertx.core.json.JsonObject;

import java.util.concurrent.atomic.AtomicReference;

import static API.ValidateData.setRolTypeTXT;

public class createObject {

  public static JsonObject getTask(JsonObject task){
    JsonObject thisTask = new JsonObject();

    thisTask
      .put("id", task.getString("id"))
      .put("userId", task.getString("user_id"))
      .put("deviceName", task.getString("device_name"))
      .put("taskName", task.getString("task_name"))
      .put("ifTemperature", task.getFloat("if_temperature"))
      .put("ifHumility", task.getFloat("if_humidity"))
      .put("email", task.getString("email"));

    return thisTask;
  }

  public static JsonObject getCompany(JsonObject company){
    JsonObject thisCompany = new JsonObject();

    thisCompany
      .put("id", company.getString("id"))
      .put("companyName", company.getString("company_name"))
      .put("nif", company.getString("NIF"))
      .put("address",  company.getString("address"))
      .put("codePostal",  company.getString("code_postal"))
      .put("phone",  company.getString("phone"))
      .put("email",  company.getString("email"))
      .put("web",  company.getString("web"));

    return thisCompany;

  }

  public static JsonObject getUser(JsonObject user, AtomicReference<String> rolTxt, int totalDevice){

    JsonObject thisUser = new JsonObject();
    String roll = rolTxt.toString();
    System.out.println("usuarios metodos");
    System.out.println("role :" + roll);
    System.out.println(user.putNull("items").put("userID", user.getString("userId")));
    System.out.println("-----------------------------------");

    thisUser
      .put("userId", user.getString("id"))
      .put("companyId", user.getString("company_id"))
      .put("createAt", user.getString("create_at"))
      .put("role", setRolTypeTXT(roll))
      .put("name", user.getString("name"))
      .put("nicknames", user.getString("nicknames"))
      .put("email", user.getString(""))
      .put("totalDevice", totalDevice);

    System.out.println("this user");
    System.out.println(thisUser.encodePrettily());
    System.out.println("------------ thisUser -------------- ");

    return thisUser;

  }


  public static JsonObject getLoginOk( JsonObject user, String rolTxt, int totalDevice){
    JsonObject thisUser = new JsonObject();

    thisUser
      .put("userId", user.getString("id"))
      .put("companyId", user.getString("company_id"))
      .put("createAt", user.getString("create_at"))
      .put("roleId", user.getString("role_id"))
      .put("role", setRolTypeTXT(rolTxt))
      .put("name", user.getString("name"))
      .put("nicknames", user.getString("nicknames"))
      .put("email", user.getString(""))
      .put("status", true)
      .put("totalDevice", totalDevice);

    return thisUser;

  }

}
