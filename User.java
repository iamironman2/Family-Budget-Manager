import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private byte[] passwordHash;

    public User(String username, String password) {
        this.username = username;
        this.passwordHash = hashPassword(password);
    }

    public String getUsername() {
        return username;
    }


    public boolean authenticate(String inputUsername, char[] inputPassword) {
        return username.equals(inputUsername) && validatePassword(inputPassword);
    }

    private boolean validatePassword(char[] inputPassword) {
        byte[] inputHash = hashPassword(new String(inputPassword));
        return Arrays.equals(inputHash, passwordHash);
    }

    private byte[] hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(password.getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
}
