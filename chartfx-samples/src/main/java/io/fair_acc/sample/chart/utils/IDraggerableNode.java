package io.fair_acc.sample.chart.utils;

import javafx.scene.input.MouseEvent;

public interface IDraggerableNode {
	void	mouseReleased(MouseEvent mouseEvent);
	void	mouseDragged(MouseEvent mouseEvent);
//	void	mouseDragResized(MouseEvent mouseEvent);
	void	mouseDragNResized(MouseEvent mouseEvent);
	void	mouseDragSResized(MouseEvent mouseEvent);
	void	mouseDragEResized(MouseEvent mouseEvent);
	void	mouseDragWResized(MouseEvent mouseEvent);
	void	mouseDragNEResized(MouseEvent mouseEvent);
	void	mouseDragNWResized(MouseEvent mouseEvent);
	void	mouseDragSEResized(MouseEvent mouseEvent);
	void	mouseDragSWResized(MouseEvent mouseEvent);
	boolean	isBound();
	boolean	isLockPos();
	boolean	isLockSize();
	boolean	isLockAspect();
	double	getAspectRatio();
	double	getAspectDir();

	/**
	 *	lc190402 -	needs to be called after resizeRelocate by user
	 */
	void	lockAspect();
}
