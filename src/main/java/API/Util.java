package API;
import java.math.BigInteger;
import java.security.MessageDigest;

public class Util {

  public static String encriptyPass(String pass) {
    String pwd = null;

    try {
      byte[] data = pass.getBytes();
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] hash1 = md.digest(data);

      BigInteger bin = new BigInteger(1, hash1);
      pwd = String.format("%0" + (data.length << 1) + "x", bin);

    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return pwd;
  }


}
