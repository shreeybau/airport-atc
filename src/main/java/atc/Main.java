package atc;

import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.awt.Font;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        FlatLightLaf.setup();
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("Component.focusColor", new Color(30, 90, 160));
        UIManager.put("TabbedPane.selectedBackground", Color.WHITE);
        UIManager.put("TabbedPane.tabHeight", 36);
        UIManager.put("Table.alternateRowColor", new Color(244, 247, 252));
        UIManager.put("TableHeader.background", new Color(30, 58, 95));
        UIManager.put("TableHeader.foreground", Color.WHITE);
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}