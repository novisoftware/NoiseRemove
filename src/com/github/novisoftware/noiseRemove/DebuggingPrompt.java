package com.github.novisoftware.noiseRemove;

import java.util.NoSuchElementException;
import java.util.Scanner;

/**
*
*
* @author Shiro SADO (sh.sado@gmail.com)
*
*/
public class DebuggingPrompt  {
	/**
	 * 16進数表示でダンプします。
	 *
	 * @param b
	 * @param start
	 */

	static void dump(byte [] b, int start) {
		int printLength = 60;

		for (int i = 0; (start + i) < b.length && i < printLength; i++) {
			System.out.print(String.format(" %02X ", b[i]));
			if (i % 16 == 15) {
				System.out.println();
			}
		}
		System.out.println();
	}

	/**
	 * 秒を表記した文字列を 秒に変換します
	 *
	 * @param in  "hh:mm:ss.sss" 表記の秒
	 * @return 秒
	 */
	static Double stringToSec(String in) {
		String[] arr = in.split(":");
		if (arr.length > 3) {
			return null;
		}

		double ret = 0;
		try {
			for (String s : arr) {
				ret *= 60;
				ret += Double.parseDouble(s);
			}
		} catch(NumberFormatException e) {
			return null;
		}
		return ret;
	}

	static void debug(Main n, String inputFile) {
		DebuggingWindow debug = new DebuggingWindow(n, inputFile);

        Scanner stdin = new Scanner(System.in);
        try {
	        while (true) {
	        	System.err.print("debug: ");
		        String str = stdin.nextLine();
		        if (str == null) break;
		        String[] command = str.split(" ");
		        if (command.length == 0) {
		        	// none
		        }
		        else if (command.length == 1) {
			        if (command[0].equals("d")) { // dump
			        	dump(n.data, debug.getStartPosition());

			        }
			        else if (command[0].equals("p")) { // print
			        	dump(n.data, debug.getStartPosition());
			        }
			        else {
			        	System.err.printf("ignore(%s)\n", command[0]);
			        }
		        }
		        else if (command.length >= 2) {
		        	// time set
		        	// 表示開始時刻を設定します。
			        if (command[0].equals("t")) {
			        	Double t = stringToSec(command[1]);
			        	if (t != null) {
				        	int newPos =  (int) Math.round(t *  n.nSamplesPerSec);
				        	debug.setStartPosition(newPos);
				        	debug.repaint();

				        	System.err.printf("time set: %.8f %d \n", t, newPos);
			        	}
			        	else {
				        	System.err.printf("ignore(%s %s)\n", command[0], command[1]);
			        	}
			        }
			        else {
			        	System.err.printf("ignore(%s)\n", command[0]);
			        }
		        }
		        else {
		        	System.err.printf("ignore(%s)\n", command[0]);
		        }
	        }
		}
        catch (NoSuchElementException e) {
        }

        stdin.close();
	}
}
