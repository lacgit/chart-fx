package io.fair_acc.sample.chart;

//public class DraggableSplitPaneExample {
//}
import io.fair_acc.sample.chart.utils.DraggableSplitPane;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Objects;


public class DraggableSplitPaneExample extends Application {
	@Override
	public void start(Stage stage) {
		DraggableSplitPane splitPane = new DraggableSplitPane();

		Label label1 = new Label("Pane 1");
		Label label2 = new Label("Pane 2");
		Label label3 = new Label("Pane 3");

		splitPane.getItems().addAll(
				wrapInStackPane(label1),
				wrapInStackPane(label2),
				wrapInStackPane(label3)
		);

		Scene scene = new Scene(splitPane, 600, 400);
		scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("DraggableSplitPaneStyle.css")).toExternalForm());
		stage.setScene(scene);
		stage.show();
	}

	private StackPane wrapInStackPane(Node node) {
		StackPane pane = new StackPane(node);
		pane.setStyle("-fx-background-color: #e0e0e0;");
		return pane;
	}
}

