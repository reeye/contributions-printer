package de.reeye.github.contributionsprinter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * GitHub Public Contributions Overview Image Printer
 * 
 * @author Christoph Schmid
 *
 */
public class App {

	/**
	 * Image to print. Must be 7 pixel high and have at most 5 differently
	 * bright colors (hint: just use grey scale). See images for examples.
	 **/
	static File printImange = new File("images/turtle.png");

	/**
	 * Use your user an email as configured in github, or else it will not appear.
	 **/
	static String user = "reeye";
	static String email = "github@ch-schmid.de";
	static File localRepoDirectory = new File("/home/reeze/contributions-printer/temp/");

	/**
	 * Repository must exist and contain entries, so generate a new repo and
	 * initialize it with a readme
	 */
	static String remoteRepoUrl = "https://github.com/reeye/paper.git";

	/**
	 * Use exactly one year ago if you want the full 52 pixel width. Use today
	 * if you want an image to appear like a really slow live ticker.
	 */
	static String startDateString = "2014-06-01";

	/**
	 * Commits used to produces colors. Example: {4,3,2,1,0} => 4 commits for
	 * darkest green, 0 for white. If you have many commits already, you need
	 * higher numbers go get the colors you want, and to make the existing
	 * pattern less dominant. Working configurations (examples): {4,3,2,1,0},
	 * {15,10,5,1,0} Remove values if your image has less than 5 colors.
	 * If your image has only 2 colors, use something like {4,0}
	 */
	static int[] commitsForColor = {4,3,2,1,0};

	public static void main(String[] args) throws Exception {

		localRepoDirectory.mkdirs();
		if (localRepoDirectory.list().length != 0) {
			System.out.println("Target directory not empty! " + localRepoDirectory.getAbsolutePath());
			return;
		}
		if (!printImange.exists()) {
			System.out.println("Image" + printImange.getAbsolutePath() + " not found.");
			return;
		}

		ContributionsImage image = new ContributionsImage(printImange, commitsForColor);

		Calendar startDate = Calendar.getInstance();
		startDate.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(startDateString));

		Repository gitRepo = new Repository(user, email, localRepoDirectory, remoteRepoUrl, startDate);

		generatePattern(gitRepo, startDate, image.getPattern());

		System.out.println();
		System.out.println();
		System.out.println("All done! Now push the repository to GitHub using the command");
		System.out.println("git -C " + localRepoDirectory.getAbsolutePath() + " push");
	}

	/**
	 * @param activityPattern
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private static void generatePattern(Repository gitRepo, Calendar startDate, int[] activityPattern) throws IOException, InterruptedException {

		gitRepo.gitClone();

		Calendar date = (Calendar) startDate.clone();

		// Skip to the first sunday to align the pattern
		while (date.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
			date.add(Calendar.DATE, 1);
		}

		for (int numberOfCommits : activityPattern) {

			for (int i = 0; i < numberOfCommits; i++) {
				gitRepo.change();
				gitRepo.add();
				gitRepo.commit(date);
			}

			date.add(Calendar.DATE, 1);
		}
	}
}
