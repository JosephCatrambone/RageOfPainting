package com.josephcatrambone.rageofpainting.handlers;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import com.badlogic.gdx.graphics.Pixmap;

public class ImageToolkit {
	
	public static int[] getPalette(Pixmap imgData, int numColors, int iterations) {
		Random random = new Random();
		int[] palette = new int[numColors];
		int[] positionsX = new int[numColors];
		int[] positionsY = new int[numColors];
		
		// Pick random centroids.
		for(int i=0; i < numColors; i++) {
			positionsX[i] = random.nextInt(imgData.getWidth());
			positionsY[i] = random.nextInt(imgData.getHeight());
			palette[i] = makeColor(random.nextInt(255), random.nextInt(255), random.nextInt(255));
		}
		
		// Repeat until convergence
		int[] parent = new int[imgData.getWidth()*imgData.getHeight()]; // We want a parent for each pixel
		for(int i=0; i < iterations; i++) {
			// Assign each pixel to its nearest parent.
			for(int y=0; y < imgData.getHeight(); y++) {
				for(int x=0; x < imgData.getWidth(); x++) {
					int nearestParent = 0;
					int nearestDist = getColorDistanceSquared(imgData.getPixel(x, y), palette[0]);
					for(int p=1; p < palette.length; p++) {
						int dist = getColorDistanceSquared(imgData.getPixel(x, y), palette[p]);
						if(dist < nearestDist) {
							nearestParent = p;
							nearestDist = dist;
						}
					}
					parent[x+y*imgData.getWidth()] = nearestParent;
				}
			}
			
			// Move parents to center of children.
			int[] rAccum = new int[numColors];
			int[] gAccum = new int[numColors];
			int[] bAccum = new int[numColors];
			int[] count = new int[numColors];
			for(int p=0; p < parent.length; p++) {
				int pixel = imgData.getPixel(p%imgData.getWidth(), p/imgData.getWidth());
				rAccum[parent[p]] += getChannel(pixel, 'r');
				gAccum[parent[p]] += getChannel(pixel, 'g');
				bAccum[parent[p]] += getChannel(pixel, 'b');
				count[parent[p]] += 1;
			}
			
			for(int p=0; p < numColors; p++) {
				if(count[p] == 0) {
					palette[p] = makeColor(random.nextInt(255), random.nextInt(255), random.nextInt(255));
				} else {
					palette[p] = makeColor((byte)(rAccum[p]/count[p]), (byte)(gAccum[p]/count[p]), (byte)(bAccum[p]/count[p]));
				}
			}
		}
		
		return palette;
	}
	
	public static void reduceColors(Pixmap img, int[] palette, Pixmap out) {
		for(int y=0; y < img.getHeight(); y++) {
			for(int x=0; x < img.getWidth(); x++) {
				int nearestParent = 0;
				int nearestDist = getColorDistanceSquared(img.getPixel(x, y), palette[0]);
				for(int p=1; p < palette.length; p++) {
					int dist = getColorDistanceSquared(img.getPixel(x, y), palette[p]);
					if(dist < nearestDist) {
						nearestParent = p;
						nearestDist = dist;
					}
				}
				out.setColor(palette[nearestParent]);
				out.drawPixel(x, y);
			}
		}
	}
	
	public static int getColorDistanceSquared(int c1, int c2) {
		int dr = getChannel(c2, 'r') - getChannel(c1, 'r');
		int dg = getChannel(c2, 'g') - getChannel(c1, 'g');
		int db = getChannel(c2, 'b') - getChannel(c1, 'b');
		return dr*dr + dg*dg + db*db;
	}
	
	/*** getChannel
	 * Returns a byte from the pixel by channel.
	 * @param pixel
	 * @param channel
	 * @return
	 */
	public static int getChannel(int pixel, char channel) {
		if(channel == 'r' || channel == 'R') {
			return byteToUInt((byte)((pixel & 0xFF000000) >>> 24));
		} else if(channel == 'g' || channel == 'G') {
			return byteToUInt((byte)((pixel & 0x00FF0000) >>> 16));
		} else if(channel == 'b' || channel == 'B') {
			return byteToUInt((byte)((pixel & 0x0000FF00) >>> 8));
		} else if(channel == 'a' || channel == 'A') {
			return byteToUInt((byte)(pixel & 0x000000FF));
		} else {
			System.err.println("Unrecognized channel: " + channel);
			return 0;
		}
	}
	
	public static int makeColor(int r, int g, int b) {
		return (r << 24) | (g << 16) | (b << 8) | 0x000000FF;
	}
	
	public static int byteToUInt(byte b) {
		return (int)b & 0xFF;
	}
	
	public static int[][] approximateImage(Pixmap img, int[] palette, int numRectangles) {
		Random random = new Random();
		int[][] rects = new int[numRectangles][5]; // [[x, y, w, h, i], [...], ...]
		
		// Randomize the initial conditions
		for(int i=0; i < rects.length; i++) {
			rects[i][0] = random.nextInt(img.getWidth());
			rects[i][1] = random.nextInt(img.getHeight());
			rects[i][2] = random.nextInt(img.getWidth());
			rects[i][3] = random.nextInt(img.getHeight());
			rects[i][4] = random.nextInt(palette.length);
		}
		
		return rects;
	}
	
	public static void drawRectanglesToImage(Pixmap img, int[][] rectangles, int[] palette, int stepLimit) {
		Arrays.sort(rectangles, new Comparator<int[]>() {
		    public int compare(int[] a, int[] b) { return b[2]*b[3] - a[2]*a[3]; }
		});
	}
}
