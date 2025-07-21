package io.fair_acc.sample.chart.utils;

import javafx.collections.ListChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import java.util.ArrayList;
import java.util.List;

public class DraggableSplitPane extends SplitPane {

	private Node draggedNode;
	private int originalIndex;
	private Region placeholder;
	private List<Double> originalFractions = new ArrayList<>();
	private int placeholderIndex = -1;

	public DraggableSplitPane() {
		setupPlaceholder();
		setupListeners();
	}

	private void setupPlaceholder() {
		placeholder = new Region();
		placeholder.getStyleClass().add("split-pane-placeholder");
		placeholder.setMinSize(2, 2);
		placeholder.setMaxSize(2, 2);
	}

	private void setupListeners() {
		// Apply event handlers to new children
		getItems().addListener((ListChangeListener<Node>) change -> {
			while (change.next()) {
				for (Node child : change.getAddedSubList()) {
					if (child != placeholder) {
						child.setOnMousePressed(this::handleMousePressed);
						child.setOnMouseDragged(this::handleMouseDragged);
						child.setOnMouseReleased(this::handleMouseReleased);
					}
				}
			}
		});

		// Initialize existing children
		for (Node child : getItems()) {
			child.setOnMousePressed(this::handleMousePressed);
			child.setOnMouseDragged(this::handleMouseDragged);
			child.setOnMouseReleased(this::handleMouseReleased);
		}
	}

	private void handleMousePressed(MouseEvent event) {
		draggedNode = (Node) event.getSource();
		originalIndex = getItems().indexOf(draggedNode);
		storeOriginalSizes();
	}

	private void handleMouseDragged(MouseEvent event) {
		if (draggedNode == null) return;

		double mousePos = getOrientation() == Orientation.HORIZONTAL
				? event.getSceneX() : event.getSceneY();

		int newIndex = findInsertionIndex(mousePos);
		if (newIndex != placeholderIndex && newIndex != -1) {
			removePlaceholder();
			getItems().add(newIndex, placeholder);
			placeholderIndex = newIndex;
		}
	}

	private void handleMouseReleased(MouseEvent event) {
		if (draggedNode == null) return;

		removePlaceholder();
		if (placeholderIndex != -1) {
			moveDraggedNode();
		}
		draggedNode = null;
		placeholderIndex = -1;
	}

	private void storeOriginalSizes() {
		originalFractions.clear();
		double totalSize = getTotalSize();
		for (Node node : getItems()) {
			double size = getNodeSize(node);
			originalFractions.add(size / totalSize);
		}
	}

	private double getTotalSize() {
		return getOrientation() == Orientation.HORIZONTAL
				? getWidth() : getHeight();
	}

	private double getNodeSize(Node node) {
		return getOrientation() == Orientation.HORIZONTAL
				? node.getBoundsInParent().getWidth()
				: node.getBoundsInParent().getHeight();
	}

	private int findInsertionIndex(double mousePos) {
		double posInParent = getOrientation() == Orientation.HORIZONTAL
				? screenToLocal(mousePos, 0).getX()
				: screenToLocal(0, mousePos).getY();

		int index = 0;
		for (Node child : getItems()) {
			if (child == draggedNode) continue;

			double childStart = getOrientation() == Orientation.HORIZONTAL
					? child.getBoundsInParent().getMinX()
					: child.getBoundsInParent().getMinY();

			if (posInParent < childStart) {
				return index;
			}
			index++;
		}
		return getItems().size(); // Insert at end
	}

	private void removePlaceholder() {
		if (placeholderIndex != -1) {
			getItems().remove(placeholder);
			placeholderIndex = -1;
		}
	}

	private void moveDraggedNode() {
		getItems().remove(draggedNode);
		int insertIndex = placeholderIndex > originalIndex
				? placeholderIndex - 1 : placeholderIndex;
		getItems().add(insertIndex, draggedNode);
		adjustDividers(insertIndex);
	}

	private void adjustDividers(int newIndex) {
		// Reorder size fractions to match new node positions
		Double draggedFraction = originalFractions.remove(originalIndex);
		originalFractions.add(newIndex, draggedFraction);

		// Apply new divider positions
		double accumulated = 0;
		for (int i = 0; i < originalFractions.size() - 1; i++) {
			accumulated += originalFractions.get(i);
			if (i < getDividers().size()) {
				getDividers().get(i).setPosition(accumulated);
			}
		}
	}
}
