package com.yishuifengxiao.common.tool.image.entity;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;

/**
 * 图片填充工具实体类
 * 
 * @author yishui
 * @date 2019年2月22日
 * @version 0.0.1
 */
public class ImageMeta {
	/**
	 * 需要填充的图片流
	 */
	private InputStream inputStream;
	/**
	 * 模板照片的宽度
	 */
	private Integer imageWidth;
	/**
	 * 模板照片的高度
	 */
	private Integer imageHeight;
	/**
	 * 补偿宽度
	 */
	private Integer compensateWidth = 0;
	/**
	 * 补偿高度
	 */
	private Integer compensateHeight = 0;
	/**
	 * 填充内容
	 */
	private List<FileMeta> fileMetas;

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public Integer getImageWidth() {
		return imageWidth;
	}

	public void setImageWidth(Integer imageWidth) {
		this.imageWidth = imageWidth;
	}

	public Integer getImageHeight() {
		return imageHeight;
	}

	public void setImageHeight(Integer imageHeight) {
		this.imageHeight = imageHeight;
	}

	public Integer getCompensateWidth() {
		return compensateWidth;
	}

	public void setCompensateWidth(Integer compensateWidth) {
		this.compensateWidth = compensateWidth;
	}

	public Integer getCompensateHeight() {
		return compensateHeight;
	}

	public void setCompensateHeight(Integer compensateHeight) {
		this.compensateHeight = compensateHeight;
	}

	public List<FileMeta> getFileMetas() {
		return fileMetas;
	}

	public void setFileMetas(List<FileMeta> fileMetas) {
		this.fileMetas = fileMetas;
	}

	public static class FileMeta {
		/**
		 * 填充文字的内容
		 */
		private String text;
		/**
		 * 需要填充的图片
		 */
		private BufferedImage image;
		/**
		 * 填充的颜色
		 */
		private Color color;
		/**
		 * 填充的字体
		 */
		private Font font;
		/**
		 * 填充的内容的X坐标
		 */
		private int xPoint;
		/**
		 * 填充的内容的y坐标
		 */
		private int yPoint;
		/**
		 * 填充的内容的填充宽度
		 */
		private int fillWidth;
		/**
		 * 填充的内容的填充高度
		 */
		private int fillHeight;

		public Boolean onlyText() {
			return this.text == null;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public BufferedImage getImage() {
			return image;
		}

		public void setImage(BufferedImage image) {
			this.image = image;
		}

		public Color getColor() {
			return color;
		}

		public void setColor(Color color) {
			this.color = color;
		}

		public Font getFont() {
			return font;
		}

		public void setFont(Font font) {
			this.font = font;
		}

		public int getxPoint() {
			return xPoint;
		}

		public void setxPoint(int xPoint) {
			this.xPoint = xPoint;
		}

		public int getyPoint() {
			return yPoint;
		}

		public void setyPoint(int yPoint) {
			this.yPoint = yPoint;
		}

		public int getFillWidth() {
			return fillWidth;
		}

		public void setFillWidth(int fillWidth) {
			this.fillWidth = fillWidth;
		}

		public int getFillHeight() {
			return fillHeight;
		}

		public void setFillHeight(int fillHeight) {
			this.fillHeight = fillHeight;
		}

		@Override
		public String toString() {
			return "FileMeta [text=" + text + ", color=" + color + ", font=" + font + ", xPoint=" + xPoint + ", yPoint="
					+ yPoint + ", fillWidth=" + fillWidth + ", fillHeight=" + fillHeight + "]";
		}

		public FileMeta(String text, BufferedImage image, Color color, Font font, int xPoint, int yPoint, int fillWidth,
				int fillHeight) {
			this.text = text;
			this.image = image;
			this.color = color;
			this.font = font;
			this.xPoint = xPoint;
			this.yPoint = yPoint;
			this.fillWidth = fillWidth;
			this.fillHeight = fillHeight;
		}

		public FileMeta() {

		}

	}

}
