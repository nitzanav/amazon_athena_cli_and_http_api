package athena_connector;

import java.util.Scanner;

public class StdInReader {
	public static String read() {
		StringBuilder input = new StringBuilder();
		Scanner scan = new Scanner(System.in);
		try {
			while (scan.hasNextLine()) {
				String line = scan.nextLine();
				input = input.append(line).append("\n");
			}
		} finally {
			scan.close();
		}

		return input.toString();
	}
}
