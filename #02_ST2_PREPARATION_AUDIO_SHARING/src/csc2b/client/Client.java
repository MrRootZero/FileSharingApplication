package csc2b.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Client extends Application{

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("BUKA Audio Sharing Application");
		ClientPane root = new ClientPane(primaryStage);
		Scene scene = new Scene(root, 800, 500);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}

