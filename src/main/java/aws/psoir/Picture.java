package aws.psoir;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class Picture {
	
	private BufferedImage picture;
	private String pictureName;
	
	public Picture(InputStream input, String pictureName) throws IOException {
		this.picture =  ImageIO.read(input);
		this.pictureName = pictureName;
	}
	
	public void transferPicture() {
		AffineTransform tx = new AffineTransform();
		tx.rotate(0.5, picture.getWidth() / 2, picture.getHeight() / 2);

		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
		picture = op.filter(picture, null);
	}

	public BufferedImage getPicture() {
		return picture;
	}

	public void setPicture(BufferedImage picture) {
		this.picture = picture;
	}

	public String getPictureName() {
		return pictureName;
	}

	public void setPictureName(String pictureName) {
		this.pictureName = pictureName;
	}
	

}
