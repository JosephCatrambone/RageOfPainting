package com.josephcatrambone.rageofpainting.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;
import java.util.Stack;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;

public class ImageToolkit {
	private static final int X = 0;
	private static final int Y = 1;
	private static final int W = 2;
	private static final int H = 3;
	private static final int C = 4;
	
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
	
	/*** reduceColors
	 * Restricts the input image to the given palette, substituting nearest color.
	 * Safe to pass the same image to input and output.
	 * @param img
	 * @param palette
	 * @param out
	 */
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
	
	public static int getNearestPaletteIndex(int[] palette, int color) {
		int nearestIndex = 0;
		int nearestDist = 0x0FFFFFFF;
		for(int i=0; i < palette.length; i++) {
			int dist = getColorDistanceSquared(palette[i], color);
			if(dist < nearestDist) {
				nearestDist = dist;
				nearestIndex = i;
			}
		}
		return nearestIndex;
	}
	
	public static int makeColor(int r, int g, int b) {
		return ((0xFF&r) << 24) | ((0xFF&g) << 16) | ((0xFF&b) << 8) | 0x000000FF;
	}
	
	public static int byteToUInt(byte b) {
		return (int)b & 0xFF;
	}
	
	/*** approximateImage
	 * Calculate a mass of pixels to be painted to the screen.
	 * Returns an array of steps.  [[index, x, y, x, y, x, y, ...], [index, x, y, x, y, x, y], ...]
	 * @param img
	 * @param palette
	 * @return
	 */
	public static int[][] approximateImage(Pixmap img, int[] palette) {
		ArrayList<int[]> steps = new ArrayList<int[]>();
		int fillColor = makeColor(255, 0, 255);
		
		// First, detect blobs of pixels using bucket fill.
		Pixmap temp = new Pixmap(img.getWidth(), img.getHeight(), Format.RGBA8888);
		temp.drawPixmap(img, 0, 0);
		for(int y=0; y < temp.getHeight(); y++) {
			for(int x=0; x < temp.getWidth(); x++) {
				int currentPixel = temp.getPixel(x, y);
				if(currentPixel != fillColor) {
					System.out.println("Pixel " + x + ", " + y + " is unvisited.");
					
					// Clear all the regions with this pixel and get the pixels used to paint it.
					int[][] pixels = floodFillSelect(temp, x, y, fillColor);
					int[] step = new int[pixels.length*2 + 1]; // Space for color index, 2*(xy).
					// Convert the selected pixels into a painting step
					step[0] = getNearestPaletteIndex(palette, currentPixel);
					for(int i=0; i < pixels.length; i++) {
						step[1+(2*i)+0] = pixels[i][0];
						step[1+(2*i)+1] = pixels[i][1];
					}
					// Append that to our steps.
					steps.add(step);
				}
			}
		}
		
		// Sort the steps so we add big blogs before fine details.
		// If we have two blobs of the same size, draw the like palette together.
		int[][] finalSteps = steps.toArray(new int[][]{});
		Arrays.sort(finalSteps, new Comparator<int[]>() {
		    public int compare(int[] a, int[] b) {
		    	if(b.length == a.length){ 
		    		return (int)Math.signum(a[0] - b[0]);
		    	} else {
		    		return (int)Math.signum(b.length - a.length);
		    	}
		    } 
		});
		return finalSteps;
	}
	
	/*** floodFillSelect
	 * Flood-fills the img with the desired color, returning an array of the pixels filled.
	 * @param img
	 * @param x
	 * @param y
	 * @param matchColor
	 * @param targetColor
	 * @return
	 */
	public static int[][] floodFillSelect(Pixmap img, int startX, int startY, int targetColor) {
		return floodFillSelect(img, startX, startY, targetColor, 3);
	}
	
	public static int[][] floodFillSelect(Pixmap img, int startX, int startY, int targetColor, int connectivity) {
		// TODO: Scanline fill.
		boolean[] visited = new boolean[img.getWidth()*img.getHeight()];
		Stack <int[]> candidates = new Stack<int[]>(); // Holds the candidate point.
		ArrayList <int[]> selection = new ArrayList<int[]>(); // Holds the final points.
		int matchColor = img.getPixel(startX, startY);
		
		// Set the draw colors
		img.setColor(targetColor);
		
		candidates.add(new int[] {startX, startY});
		while(!candidates.isEmpty()) {
			int[] current = candidates.pop();
			int x = current[0];
			int y = current[1];
			System.out.println("DEBUG: Visiting " + x + "," + y);
			if(img.getPixel(x, y) == matchColor && !visited[x+y*img.getWidth()]) { // If the colors match
				// Mark this pixel and add it to the selection.
				selection.add(new int[] {x, y});
				img.drawPixel(x, y);
				visited[x+y*img.getWidth()] = true;
				
				// Add adjacent pixels.
				if(connectivity >= 0) { // Basic 4-connctivity
					if(isInsideImage(img, x-1, y)) { candidates.add(new int[] {x-1, y}); }
					if(isInsideImage(img, x+1, y)) { candidates.add(new int[] {x+1, y}); }
					if(isInsideImage(img, x, y-1)) { candidates.add(new int[] {x, y-1}); }
					if(isInsideImage(img, x, y+1)) { candidates.add(new int[] {x, y+1}); }
				} else if(connectivity >= 1) { // 8-connectivity
					if(isInsideImage(img, x-1, y)) { candidates.add(new int[] {x-1, y}); }
					if(isInsideImage(img, x+1, y)) { candidates.add(new int[] {x+1, y}); }
					if(isInsideImage(img, x, y-1)) { candidates.add(new int[] {x, y-1}); }
					if(isInsideImage(img, x, y+1)) { candidates.add(new int[] {x, y+1}); }
					
					if(isInsideImage(img, x-1, y-1)) { candidates.add(new int[] {x-1, y-1}); }
					if(isInsideImage(img, x+1, y-1)) { candidates.add(new int[] {x+1, y-1}); }
					if(isInsideImage(img, x-1, y+1)) { candidates.add(new int[] {x-1, y+1}); }
					if(isInsideImage(img, x+1, y+1)) { candidates.add(new int[] {x+1, y+1}); }
				} else if(connectivity >= 2) { // Jump-connectivity
					for(int i=-2; i < 3; i++) { // Top
						for(int j=-2; j < 3; j++) {
							if(i == 0 && j == 0) { continue; }
							if(isInsideImage(img, x+i, y+j)) { candidates.add(new int[] {x+i, y+j}); }
						}
					}
				}
			}
		}
		
		return selection.toArray(new int[][]{});
	}
	
	public static boolean isInsideImage(Pixmap img, int x, int y) {
		return x >= 0 && y >= 0 && x < img.getWidth() && y < img.getHeight();
	}
	
	public static int[][] approximateImageWithRectangles(Pixmap img, int[] palette, int numChildren, int numRectangles, int numGenerations) {
		final int ODDS_OF_MUTATION = 2; // One in this many will mutate.
		final int GENETIC_INSTABILITY = 200; // Range in which rectangles will move
		final int PALETTE_SWITCH_LIMIT = 180; // If random number between zero and GENETIC INSTABILITY greater than this, palette change
		Random random = new Random();
		int[][][] children = new int[numChildren][][];
		final HashMap<int[][], Double> fitness = new HashMap<int[][], Double>();
		Pixmap testCanvas = new Pixmap(img.getWidth(), img.getHeight(), Format.RGBA8888);
		
		// Initi first generation
		for(int i=0; i < numChildren; i++) {
			int[][] rects = new int[numRectangles][C+1]; // [[x, y, w, h, i], [...], ...]
			
			// Randomize the initial conditions
			for(int j=0; j < rects.length; j++) {
				rects[j][X] = random.nextInt(img.getWidth());
				rects[j][Y] = random.nextInt(img.getHeight());
				rects[j][W] = random.nextInt(img.getWidth());
				rects[j][H] = random.nextInt(img.getHeight());
				rects[j][C] = random.nextInt(palette.length);
			}
			
			children[i] = rects;
		}
		
		// Run the genetic programming algo
		for(int i=0; i < numGenerations; i++) {
			fitness.clear();

			// Find the fitness of this generation.
			for(int j=0; j < numChildren; j++) {
				int[][] child = children[j];
				
				// Sort rectangles.
				// We want the fine details to be last.
				/*
				Arrays.sort(child, new Comparator<int[]>() {
				    public int compare(int[] a, int[] b) { return (int)Math.signum(b[W]*b[H] - a[W]*a[H]); } 
				});
				*/
				
				// Draw the rectangles
				drawRectanglesToImage(testCanvas, child, palette, -1);
				
				fitness.put(child, getDistance(img, testCanvas));
			}
			
			// Sort the children by the most fit (lowest distance) at the top.
			Arrays.sort(children, new Comparator<int[][]>() {
			    public int compare(int[][] a, int[][] b) { return (int)Math.signum(fitness.get(a) - fitness.get(b)); }
			});
			
			// Randomly cross the two most capable children.
			int[][] parentA = children[0];
			int[][] parentB = children[1];
			children = new int[numChildren][][];
			children[0] = parentA;
			children[1] = parentB;
			for(int j=2; j < numChildren; j++) {
				children[j] = new int[parentA.length][parentA[0].length];
				
				// Cross the parents
				for(int k=0; k < parentA.length; k++) {
					children[j][k] = new int[parentA[k].length];
					if(random.nextBoolean()) {
						for(int x=0; x < parentA[k].length; x++) {
							children[j][k][x] = parentA[k][x];
						}
					} else {
						for(int x=0; x < parentA[k].length; x++) {
							children[j][k][x] = parentB[k][x];
						}
					}
				}
				
				// Mutate a gene at random with a boundary check.
				// NOTE: ONLY THE EVEN CHILDREN ARE BEING MUTATED.
				if(random.nextInt(ODDS_OF_MUTATION) == 0) {
					int gene = random.nextInt(parentA.length);
					children[j][gene][X] = children[j][gene][X] + (GENETIC_INSTABILITY/2) - random.nextInt(GENETIC_INSTABILITY);
					children[j][gene][Y] = children[j][gene][Y] + (GENETIC_INSTABILITY/2) - random.nextInt(GENETIC_INSTABILITY);
					children[j][gene][W] = children[j][gene][W] + (GENETIC_INSTABILITY/2) - random.nextInt(GENETIC_INSTABILITY);
					children[j][gene][H] = children[j][gene][H] + (GENETIC_INSTABILITY/2) - random.nextInt(GENETIC_INSTABILITY);
					if(random.nextInt(GENETIC_INSTABILITY) > PALETTE_SWITCH_LIMIT) { children[j][gene][C] = random.nextInt(palette.length); }
					children[j][gene][X] = Math.max(children[j][gene][X], 0);
					children[j][gene][X] = Math.min(children[j][gene][X], img.getWidth());
					children[j][gene][Y] = Math.max(children[j][gene][Y], 0);
					children[j][gene][Y] = Math.min(children[j][gene][Y], img.getHeight());
					children[j][gene][W] = Math.max(children[j][gene][W], 0);
					children[j][gene][H] = Math.max(children[j][gene][H], 0);
				}
			}
			System.out.println("Best distance: " + fitness.get(children[0]));
		}
		
		return children[0];
	}
	
	/*** drawRectanglesToImage
	 * Draw the array of rectangles to the image up to (but not including) the step limit.
	 * If step limit is -1, will draw all rectangles.
	 * @param img
	 * @param rectangles
	 * @param palette
	 * @param stepLimit
	 */
	public static void drawRectanglesToImage(Pixmap img, int[][] rectangles, int[] palette, int stepLimit) {
		if(stepLimit == -1) { stepLimit = rectangles.length; }
		stepLimit = Math.min(stepLimit, rectangles.length); // We don't want to try and draw more rectangles than there are.
		
		img.setColor(Color.WHITE);
		img.fillRectangle(0, 0, img.getWidth(), img.getHeight());
		
		for(int i=0; i < stepLimit; i++) {
			img.setColor(palette[rectangles[i][C]]);
			img.fillRectangle(rectangles[i][X], rectangles[i][Y], rectangles[i][W], rectangles[i][H]);
		}
	}
	
	public static void drawPixelsToImage(Pixmap img, int[][] steps, int[] palette, int stepLimit) {
		if(stepLimit == -1) { stepLimit = steps.length; }
		stepLimit = Math.min(stepLimit, steps.length); // We don't want to try and draw more rectangles than there are.
		
		// Clear canvas
		img.setColor(Color.WHITE);
		img.fillRectangle(0, 0, img.getWidth(), img.getHeight());
		
		for(int i=0; i < stepLimit; i++) {
			img.setColor(palette[steps[i][0]]);
			for(int j=0; j < (int)Math.ceil((steps[i].length-1)/2.0f); j++) {
				img.drawPixel(steps[i][1+j*2], steps[i][1+j*2+1]);
			}
		}
	}
	
	/*** getDistance
	 * Returns the relative distance of the two pixmaps.  This sums the square of pixel distances.
	 * NOTE: ASSUMES THE IMAGES ARE THE SAME SIZE.
	 * @param target
	 * @param candidate
	 * @return
	 */
	public static double getDistance(Pixmap target, Pixmap candidate) {
		assert(target.getWidth() == candidate.getWidth() && target.getHeight() == candidate.getHeight());
		double mismatches = 0;
		for(int y=0; y < target.getHeight(); y++) {
			for(int x=0; x < target.getWidth(); x++) {
				int targetPixel = target.getPixel(x, y);
				int candidatePixel = candidate.getPixel(x, y);
				if(targetPixel != candidatePixel) {
					//int dist = getColorDistanceSquared(targetPixel, candidatePixel);
					//mismatches += dist/(3.0*255.0 * 3.0*255.0); // Max R + Max G + Max B squared
					mismatches += 1.0;
				}
			}
		}
		return mismatches/(target.getWidth()*target.getHeight());
	}
	
	public static void printPalette(int[] palette) {
		System.out.println("Palette [" + palette.length + " colors]: ");
		for(int i=0; i < palette.length; i++) {
			System.out.println(i + " - (" + getChannel(palette[i], 'r') + "," + getChannel(palette[i], 'g') + "," + getChannel(palette[i], 'b') + "," + getChannel(palette[i], 'a') + ")");
		}
	}
}
