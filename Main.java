import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;

public class Main extends JFrame {
    private final AccountStore store = new AccountStore();

    public Main() {
        setTitle("Electric Cooperative Billing System");
        setSize(1050, 700);
        setMinimumSize(new Dimension(800, 500));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(createHeaderBanner(), BorderLayout.NORTH);
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(tabbedPane.getFont().deriveFont(Font.BOLD, 13f));
        tabbedPane.addTab("1. Consumers", new ConsumerPanel(store));
        tabbedPane.addTab("2. Public Facilities", new FacilityPanel(store));
        tabbedPane.addTab("3. Billing", new BillingPanel(store));
        add(tabbedPane, BorderLayout.CENTER);
        loadSavedAccounts();
    }
    private JPanel createHeaderBanner() {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(UIHelper.GREEN);
        banner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 4, 0, UIHelper.YELLOW),
                BorderFactory.createEmptyBorder(14, 20, 14, 20)
        ));

        JLabel title = new JLabel("⚡ Electric Cooperative Billing System");
        title.setForeground(UIHelper.YELLOW);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));

        JLabel subtitle = new JLabel("Consumers  |  Public Facilities  |  Billing");
        subtitle.setForeground(Color.WHITE);
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 2));
        text.setOpaque(false);
        text.add(title);
        text.add(subtitle);
        banner.add(title, BorderLayout.WEST);
        return banner;
    }
    private void loadSavedAccounts() {
        try {
            store.load();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not load data: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        store.fireChanged();
    }

    private void confirmExit() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to exit?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        UIHelper.install();
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}

