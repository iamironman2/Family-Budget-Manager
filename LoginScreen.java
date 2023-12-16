import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class LoginScreen extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginScreen() {
        setTitle("Login");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel usernameLabel = new JLabel("Username:");
        panel.add(usernameLabel);

        usernameField = new JTextField();
        panel.add(usernameField);

        JLabel passwordLabel = new JLabel("Password:");
        panel.add(passwordLabel);

        passwordField = new JPasswordField();
        panel.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                char[] password = passwordField.getPassword();

                // Integrated authentication logic
                User user = loadUser(username);
                if (user != null && user.authenticate(username, password)) {
                    dispose();
                    new ExpenseTrackerApp(new User(username, new String(password)));
                } else {
                    JOptionPane.showMessageDialog(LoginScreen.this, "Incorrect username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                }

                // Clear the password field after login attempt
                passwordField.setText("");

            }
        });
        panel.add(loginButton);

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                char[] password = passwordField.getPassword();
                UserRegistration.registerUser(username, password);

                // Clear the password field after registration
                passwordField.setText("");
            }
        });
        panel.add(registerButton);

        add(panel);
        setVisible(true);
    }

    private User loadUser(String username) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(username + "_userdata.dat"))) {
            return (User) ois.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            return null;
        }
    }
}
