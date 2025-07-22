package io.fair_acc.sample.chart.utils;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;


public class ReorderableSplitPane extends SplitPane {

	public ReorderableSplitPane() {
		super();
		initialize();
	}

	public ReorderableSplitPane(Node... items) {
		super(items);
		initialize();
	}

	private void initialize() {
		setupReorderSupport();

		setStyle(".split-pane > .split-pane-divider { -fx-padding: 0.75 0 0.75em 0; }");
	}

	private void setupReorderSupport() {
		// Setup drag handlers for existing items
		for (Node node : getItems()) {
			setupDragHandlers(node);
		}

		// Listen for new items
		getItems().addListener((ListChangeListener<Node>) change -> {
			while (change.next()) {
				if (change.wasAdded()) {
					for (Node node : change.getAddedSubList()) {
						setupDragHandlers(node);
					}
				}
			}
		});
	}

	private void setupDragHandlers(Node node) {
		final int[] currentIndex = {getItems().indexOf(node)};
		final boolean[] isDragging = {false};

		node.setOnDragDetected(event -> {
			isDragging[0] = true;
			currentIndex[0] = getItems().indexOf(node);
			if (getScene() != null) {
				getScene().setCursor(Cursor.CLOSED_HAND);
			}
			event.consume();
		});

		node.setOnMouseDragged(event -> {
			if (isDragging[0]) {
				double totalSize = getTotalSize();
				if (totalSize <= 0) return;

				double mousePosition = getRelativeMousePosition(event.getSceneX(), event.getSceneY());
				double[] dividerPositions = getDividerPositions();
				int itemCount = getItems().size();

				// Calculate boundaries for the current item
				double leftBound = (currentIndex[0] == 0) ? 0 : dividerPositions[currentIndex[0] - 1];
				double rightBound = (currentIndex[0] == itemCount - 1) ? 1.0 : dividerPositions[currentIndex[0]];

				// Check if we should swap left
				if (mousePosition < leftBound && currentIndex[0] > 0) {
					swapNodesAndSizes(currentIndex[0], currentIndex[0] - 1);
					currentIndex[0]--;
				}
				// Check if we should swap right
				else if (mousePosition > rightBound && currentIndex[0] < itemCount - 1) {
					swapNodesAndSizes(currentIndex[0], currentIndex[0] + 1);
					currentIndex[0]++;
				}
			}
		});

		node.setOnMouseReleased(event -> {
			isDragging[0] = false;
			if (getScene() != null) {
				getScene().setCursor(Cursor.DEFAULT);
			}
		});
	}

	private void swapNodesAndSizes(int i, int j) {
		ObservableList<Node> items = this.getItems();
		double[] savedPositions = this.getDividerPositions();

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
		this.setDividerPositions(savedPositions);
	}

	private double getTotalSize() {
		return (getOrientation() == Orientation.HORIZONTAL) ? getWidth() : getHeight();
	}

	private double getRelativeMousePosition(double sceneX, double sceneY) {
		Point2D local = sceneToLocal(sceneX, sceneY);
		double mousePos = (getOrientation() == Orientation.HORIZONTAL) ? local.getX() : local.getY();
		return mousePos / getTotalSize();
	}
}
