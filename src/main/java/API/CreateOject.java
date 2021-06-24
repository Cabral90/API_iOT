package API;

import io.vertx.core.json.JsonObject;

public class CreateOject {

  public static JsonObject Task(JsonObject task){
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
}
