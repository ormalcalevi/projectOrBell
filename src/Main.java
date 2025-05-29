import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {



    public static void main(String[] args) {
        System.out.println("--- Running SimulationController WITHOUT GUI ---");

        // יצירת SimulationController והעברת null בתור ה-Visualizer
        SimulationController controllerNoGui = new SimulationController(null);

        // הרצת שלבי הסימולציה כרגיל
        controllerNoGui.loadAndSetupScenario();

        // (אופציונלי) בדיקה אם התרחיש נטען בהצלחה לפני שממשיכים
        // if (controllerNoGui.isScenarioLoadedSuccessfully()) { // אם הוספת מתודה כזו
        controllerNoGui.runAllocationAlgorithm();
        // if (controllerNoGui.isAllocationDoneSuccessfully()) {
        controllerNoGui.initializeSimulationEngine();
        // if (controllerNoGui.isEngineInitializedSuccessfully()) {
        controllerNoGui.runFullSimulation();

        System.out.println("\n--- SimulationController run (without GUI) finished. ---");
    }
}