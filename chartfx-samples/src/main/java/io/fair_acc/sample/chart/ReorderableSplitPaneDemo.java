package io.fair_acc.sample.chart;

import io.fair_acc.sample.chart.utils.ReorderableSplitPane;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ReorderableSplitPaneDemo extends Application {

	@Override
	public void start(Stage stage) {
		ReorderableSplitPane splitPane = new ReorderableSplitPane();
//		SplitPaneReorderUtil.makeReorderable(splitPane);
		splitPane.setOrientation(Orientation.VERTICAL);
		splitPane.getItems().addAll(
				createColoredPane("Pane 1", "#FFCCCB"),
				createColoredPane("Pane 2", "#CBFFCC"),
				createColoredPane("Pane 3", "#CBCCFF"),
				createColoredPane("Pane 4", "#DDCCFF")
		);

		// Enable reordering for all items

		Scene scene = new Scene(splitPane, 600, 400);
		stage.setScene(scene);
		stage.setTitle("Draggable SplitPane Items");
		stage.show();
	}

	private StackPane createColoredPane(String text, String color) {
		StackPane pane = new StackPane(new Label(text));
		pane.setStyle("-fx-background-color: " + color + "; -fx-padding: 20;");
		return pane;
	}

	public static void main(String[] args) {
		launch(args);
	}
}

class SplitPaneReorderUtil {
	public static void makeReorderable(SplitPane splitPane) {
		for (Node node : splitPane.getItems()) {
			setupDragHandlers(splitPane, node);
		}

		// Update handlers if items change dynamically
		splitPane.getItems().addListener((javafx.collections.ListChangeListener.Change<? extends Node> change) -> {
			while (change.next()) {
				if (change.wasAdded()) {
					for (Node node : change.getAddedSubList()) {
						setupDragHandlers(splitPane, node);
					}
				}
			}
		});
	}

	private static void setupDragHandlers(SplitPane splitPane, Node node) {
		final int[] currentIndex = {splitPane.getItems().indexOf(node)};
		final boolean[] isDragging = {false};

		node.setOnDragDetected(event -> {
			isDragging[0] = true;
			currentIndex[0] = splitPane.getItems().indexOf(node);
			node.getScene().setCursor(Cursor.CLOSED_HAND);
			event.consume();
		});

		node.setOnMouseDragged(event -> {
			if (isDragging[0]) {
				double totalSize = getTotalSize(splitPane);
				if (totalSize <= 0) return;

				double mousePosition = getRelativeMousePosition(splitPane, event.getSceneX(), event.getSceneY());
				double[] dividerPositions = splitPane.getDividerPositions();

				// Check left/above swap
				if (currentIndex[0] > 0 && mousePosition < dividerPositions[currentIndex[0] - 1]) {
					swapNodes(splitPane, currentIndex[0], currentIndex[0] - 1);
					currentIndex[0]--;
				}
				// Check right/below swap
				else if (currentIndex[0] < splitPane.getItems().size() - 1 && mousePosition > dividerPositions[currentIndex[0]]) {
					swapNodes(splitPane, currentIndex[0], currentIndex[0] + 1);
					currentIndex[0]++;
				}
			}
		});

		node.setOnMouseReleased(event -> {
			isDragging[0] = false;
			node.getScene().setCursor(Cursor.DEFAULT);
		});

		node.setOnMouseExited(event -> {
			if (!isDragging[0]) {
				node.getScene().setCursor(Cursor.DEFAULT);
			}
		});
	}

	private static void swapNodes(SplitPane splitPane, int i, int j) {
		ObservableList<Node> items = splitPane.getItems();
		double[] savedPositions = splitPane.getDividerPositions();

		// Swap nodes
		Node temp1 = items.get(i);
		Node temp2 = items.get(j);
		double	relativeSizeI	=	(i<savedPositions.length ? savedPositions[i] : 1.0) - (i>0 ? savedPositions[i-1] : 0.0);
		double	relativeSizeJ	=	(j<savedPositions.length ? savedPositions[j] : 1.0) - (j>0 ? savedPositions[j-1] : 0.0);
		//	Seems the behavior os splitPane has to assign larger item first
		//	below fix the issue when drag from higher item to lower item ok but when drag from lower item to higher item not ok
		if	(i>j) {
			items.set(i, temp2);
			items.set(j, temp1);
			savedPositions[j] = savedPositions[j] + (relativeSizeI - relativeSizeJ);
		} else {
			items.set(j, temp1);
			items.set(i, temp2);
			savedPositions[i] = savedPositions[i] + (relativeSizeJ - relativeSizeI);
		}

		// Restore divider positions
		splitPane.setDividerPositions(savedPositions);
	}

	private static double getTotalSize(SplitPane splitPane) {
		return (splitPane.getOrientation() == Orientation.HORIZONTAL)
				? splitPane.getWidth()
				: splitPane.getHeight();
	}

	private static double getRelativeMousePosition(SplitPane splitPane, double sceneX, double sceneY) {
		Point2D local = splitPane.sceneToLocal(sceneX, sceneY);
		double mousePos = (splitPane.getOrientation() == Orientation.HORIZONTAL)
				? local.getX()
				: local.getY();
		return mousePos / getTotalSize(splitPane);
	}
}

