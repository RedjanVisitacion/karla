import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.JOptionPane;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Main {
    public static void main(String[] args) {
        // Catch any uncaught exceptions on the EDT and show a dialog
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            JOptionPane.showMessageDialog(null, sw.toString(), "Unexpected Error", JOptionPane.ERROR_MESSAGE);
        });
        SwingUtilities.invokeLater(() -> {
            try {
                for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ignore) {
                // Fallback to default LAF if Nimbus is not available
            }
            try {
                new LoginFrame().setVisible(true);
            } catch (Throwable ex) {
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                JOptionPane.showMessageDialog(null, sw.toString(), "Failed to start UI", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
