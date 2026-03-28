import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class FXTester extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Create a simple green circle to prove graphics work
        Circle testCircle = new Circle(50, Color.LIMEGREEN);
        Text statusText = new Text("JavaFX is working!");
        statusText.setFill(Color.WHITE);

        // A StackPane centers everything automatically
        StackPane root = new StackPane();
        root.getChildren().addAll(testCircle, statusText);

        Scene scene = new Scene(root, 400, 400, Color.BLACK);

        primaryStage.setTitle("JavaFX Download Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

