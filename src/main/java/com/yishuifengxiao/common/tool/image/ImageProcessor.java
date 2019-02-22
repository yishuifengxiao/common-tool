/**
 * 
 */
package com.yishuifengxiao.common.tool.image;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.springframework.util.Assert;

import com.yishuifengxiao.common.tool.image.entity.ImageMeta;

/**
 * @author yishui
 * @date 2019年2月22日
 * @version 0.0.1
 */
public class ImageProcessor {

	public OutputStream convert2Stream(ImageMeta imageMeta) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ImageIO.write(this.convert2Image(imageMeta), "jpg", outputStream);
		return outputStream;
	}

	public BufferedImage convert2Image(ImageMeta imageMeta) throws IOException {
		Assert.notNull(imageMeta, "参数不能为空");
		BufferedImage image = new BufferedImage(imageMeta.getImageWidth(), imageMeta.getImageHeight(),
				BufferedImage.TYPE_INT_RGB);
		Image src = ImageIO.read(imageMeta.getInputStream());
		Graphics2D g = image.createGraphics();
		// 如果不考虑补偿宽度和补偿高度，绘制的图片会有黑色边框
		g.drawImage(src, 0, 0, imageMeta.getImageWidth() + imageMeta.getCompensateWidth(),
				imageMeta.getImageHeight() + imageMeta.getCompensateHeight(), null);
		if (imageMeta.getFileMetas() != null) {
			imageMeta.getFileMetas().parallelStream().filter(t -> t != null).forEach(t -> {
				if (t.onlyText()) {
					g.setColor(t.getColor());
					g.setFont(t.getFont());
					g.drawString(t.getText(), t.getxPoint(), t.getyPoint());
				} else {
					g.drawImage(image, t.getxPoint(), t.getyPoint(), t.getFillWidth(), t.getFillHeight(), null);
				}
			});

		}
		g.dispose();
		return image;
	}

}
