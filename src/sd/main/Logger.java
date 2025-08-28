import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
public class Logger {
    private static Logger instance;
    private PrintWriter writer;
    private static JTextArea logArea;

    private Logger(String fileName) {
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(fileName, false)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized Logger getInstance(String fileName) {
        if (instance == null) {
            instance = new Logger(fileName);
        }
        return instance;
    }

    public static synchronized void setLogArea(JTextArea logArea) {
        Logger.logArea = logArea;
    }

    public synchronized void log(String message) {
        writer.println(message);    
        writer.flush();

        if(logArea != null){
            SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
        }
    }

    public synchronized void close() {
        writer.close();
    }
}
