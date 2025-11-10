import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

import static javafx.application.Application.launch;

public class Main extends Application {

    /**
     * The main entry point for all JavaFX applications.
     * @param primaryStage The primary stage for this application, onto which
     * the application scene can be set.
     */
    @Override
    public void start (Stage primaryStage) {
        try {
            URL fxmlUrl = Thread.currentThread().getContextClassLoader().getResource("fxml/login_scene.fxml");
            if (fxmlUrl == null) {
                throw new IOException("FXML resource not found. Ensure 'resources' folder is marked as 'Resources Root'. Path checked: fxml/login_scene.fxml");
            }

            // Load the FXML from the reliable URL
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // 2. Set up the primary stage (the main window)
            primaryStage.setTitle("Car Park Reservation System");
            primaryStage.setScene(new Scene(root, 1200, 800)); // Set window size
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Failed to load the login scene (login_scene.fxml)");
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        launch(args);
    }
}
