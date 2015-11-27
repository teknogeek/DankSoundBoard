package pw.tekno.soundboard;

import javax.swing.*;
import java.awt.*;

class drawBackgroundImage extends JComponent {
	private Image image;
	public drawBackgroundImage(Image image) {
		this.image = image;
	}
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(image, 0, 0, this);
	}
}