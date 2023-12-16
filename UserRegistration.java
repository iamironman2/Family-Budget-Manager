import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserRegistration {
    private static List<User> registeredUsers;

    static {
        // Load registered users from file when the class is loaded
        registeredUsers = loadRegisteredUsersFromFile();
    }

    public static void registerUser(String username, char[] password) {
        User newUser = new User(username, new String(password));
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(username + "_userdata.dat"))) {
            oos.writeObject(newUser);
            JOptionPane.showMessageDialog(null, "User registered successfully.", "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
            registeredUsers.add(newUser);
            saveRegisteredUsersToFile();  // Save the updated list to file
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error registering user.", "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static List<User> getRegisteredUsers() {
        return registeredUsers;
    }

    private static List<User> loadRegisteredUsersFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("FamilyMembers.dat"))) {
            return (List<User>) ois.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            return new ArrayList<>();
        }
    }

    private static void saveRegisteredUsersToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("FamilyMembers.dat"))) {
            oos.writeObject(registeredUsers);
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error saving registered users.", "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
