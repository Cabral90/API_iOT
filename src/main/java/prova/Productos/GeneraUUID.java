package prova.Productos;

import java.security.SecureRandom;
import java.util.UUID;

public class GeneraUUID {
  private UUID uuid;
/*  private static volatile SecureRandom numberGenerator = null;
  private static final long MSB = 0x8000000000000000L;*/



/*  public static void main(String[] args) {
    System.out.println(generaUUID());

  }*/

 public static UUID generaUUID(){
    UUID uuid = UUID.randomUUID();

    System.out.println("uuid: "+uuid);
   return uuid;
 }


}
