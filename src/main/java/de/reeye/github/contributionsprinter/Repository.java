package de.reeye.github.contributionsprinter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * @author Christoph Schmid
 *
 */
public class Repository {

	private static String changeFile = "counter.txt";

	private String author;
	private String email;
	private File repoDirectory;
	private String remoteUrl;
	private Calendar startDate;

	private int counter = 0;

	/**
	 * 
	 * 
	 * @param author
	 * @param email
	 * @param repoDirectory
	 * @param remoteUrl
	 * @param startDate
	 *            Actual start date will be the first sunday after
	 */
	public Repository(String author, String email, File repoDirectory, String remoteUrl, Calendar startDate) {
		super();
		this.author = author;
		this.email = email;
		this.repoDirectory = repoDirectory;
		this.remoteUrl = remoteUrl;
		this.startDate = startDate;
	}

	/**
	 * Change something in the repository to justify a commit
	 * 
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 */
	public void change() throws FileNotFoundException, UnsupportedEncodingException {
		File f = new File(repoDirectory, changeFile);
		PrintWriter pw = new PrintWriter(f, "UTF-8");
		pw.println(++counter);
		pw.close();
	}

	/**
	 * Add changes to the git repository
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void add() throws IOException, InterruptedException {
		execute(new String[] { "git", "add", "-A" });
	}

	/**
	 * Commit the changes
	 * 
	 * @param date
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void commit(Calendar c) throws IOException, InterruptedException {
		String date = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(c.getTime());
		execute(new String[] { "git", "commit", "--date=\"" + date + "\"", "-m \"commit" + counter + "\"" });
	}

	public void gitClone() throws IOException, InterruptedException {
		execute(new String[] { "git", "clone", remoteUrl, repoDirectory.getAbsolutePath() });

		execute(new String[] { "git", "config", "user.name", "\"" + author + "\"" });
		execute(new String[] { "git", "config", "user.email", "\"" + email + "\"" });

	}

	private void execute(String[] command) throws IOException, InterruptedException {
		System.out.println(arrayToString(command));
		Process proc = Runtime.getRuntime().exec(command, null, repoDirectory);

		BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

		proc.waitFor();

		// read the output from the command
		String s = null;
		while ((s = stdInput.readLine()) != null) {
			System.out.println(s);
		}

		// read any errors from the attempted command
		while ((s = stdError.readLine()) != null) {
			System.out.println("Error: " + s);
		}

		if (proc.exitValue() == 1) {
			throw new RuntimeException("Error executing command.");
		}
	}

	private String arrayToString(String[] array) {
		String s = "";
		for (String string : array) {
			s += string + " ";
		}
		return s;
	}
}
