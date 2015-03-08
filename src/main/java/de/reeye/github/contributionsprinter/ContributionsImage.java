package de.reeye.github.contributionsprinter;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.imageio.ImageIO;

/**
 * @author Christoph Schmid
 * 
 * Read an image, generate the pattern (number of commits for a given day) to generate the read image
 * in the GitHub Public Contributions Overview.
 *
 */
public class ContributionsImage {

	private int[] commitsForColor;
	private int[] pattern;
	
	/**
	 * Use an image as source of the pattern.
	 * 
	 * @param image The image. 7 pixel high, at most 5 different colors.
	 * @param commitsForColor Number of commits assigned to each of the 5 colors (darkest to brightest)
	 */
	public ContributionsImage(File image, int[] commitsForColor) {
		
		this.commitsForColor = commitsForColor;
		BufferedImage img = null;
		SortedSet<Integer> colorBrighnesses = new TreeSet<Integer>();
		try {
			img = ImageIO.read(image);
			int height = img.getHeight();
			int width = img.getWidth();

			if (height != 7)
				throw new IOException("Image invalid. Must be 7 px high!");

			pattern = new int[height * width];
			int position = 0;
			
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					Color c = new Color(img.getRGB(i, j));
					
					int red = c.getRed();
                    int green = c.getGreen();
                    int blue = c.getBlue();
					
                    int brightness = (int)(red * 0.299 + green * 0.587 + blue * 0.114);
					
					pattern[position++] = brightness;
					colorBrighnesses.add(brightness);
				}
			}
			if (colorBrighnesses.size() > 5)
				throw new IOException("Image invalid. Image does have more than 5 colors");
			if (colorBrighnesses.size() > commitsForColor.length)
				throw new IOException("Image invalid. There are less CommitsForColors defined than there are colors.");
			
			Map<Integer, Integer> tmap = generateTranslationMap(colorBrighnesses.toArray(new Integer[colorBrighnesses.size()]));
			
			for(int i=0; i<pattern.length; i++) {
				pattern[i] = tmap.get(pattern[i]);
			}			
			
		} catch (IOException e) {
		}

	}

	/**
	 * Generate a translation map that has the colors as keys, and the number of commits as values	
	 * @param colorBrighnesses
	 * @return
	 */
	private HashMap<Integer, Integer> generateTranslationMap(Integer[] colorBrighnesses) {
		
		HashMap<Integer, Integer> translationMap = new HashMap<Integer, Integer>();
		
		for(int i=0; i<colorBrighnesses.length; i++) {
			translationMap.put(colorBrighnesses[i], commitsForColor[i]);
		}
		
		return translationMap;
	}	

	/**
	 * Return the generated pattern.
	 * @return
	 */
	public int[] getPattern() {
		return pattern;
	}
}
