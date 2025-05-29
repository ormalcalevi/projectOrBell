import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.application.Application;

public class Main {
    public static void main(String[] args) {
        // משגר את מחלקת ה-Visualizer על FX application thread
        Application.launch(Visualizer.class, args);
    }
}

