package atc;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LoginFrame extends JFrame {
    private final JTextField user = new JTextField(16);
    private final JPasswordField pass = new JPasswordField(16);

    public LoginFrame() {
        super("Airport ATC & Gate Allocation - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JLabel title = new JLabel("Airport Control System", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setForeground(new Color(30, 58, 95));
        JButton login = new JButton("Login");
        login.addActionListener(e -> doLogin());
        getRootPane().setDefaultButton(login);

        JLabel hint = new JLabel("<html><center><small>Demo: admin/admin123 &nbsp; atc1/atc123 &nbsp; gate1/gate123</small></center></html>",
                SwingConstants.CENTER);
        JPanel south = new JPanel(new BorderLayout(0, 8));
        south.add(login, BorderLayout.NORTH);
        south.add(hint, BorderLayout.SOUTH);

        JPanel root = new JPanel(new BorderLayout(10, 14));
        root.setBorder(new EmptyBorder(24, 30, 20, 30));
        root.add(title, BorderLayout.NORTH);
        root.add(Ui.form(new String[]{"Username", "Password"}, new JComponent[]{user, pass}, 1), BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);
        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
    }

    private void doLogin() {
        if (user.getText().trim().isEmpty() || pass.getPassword().length == 0) {
            Ui.msg(this, "Enter username and password");
            return;
        }
        try {
            String role = UserDAO.login(user.getText().trim(), new String(pass.getPassword()));
            if (role == null) {
                Ui.msg(this, "Invalid username or password");
                return;
            }
            new MainFrame(user.getText().trim(), role).setVisible(true);
            dispose();
        } catch (Exception ex) {
            Ui.err(this, ex);
        }
    }
}