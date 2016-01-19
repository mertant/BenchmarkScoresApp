package main;

/*
 * A simple command line application for connecting to a server, sending user-defiend HTTP requests, and
 * printing the responses.
 * 
 * 
 * @author Teo Mertanen
 */

import java.util.Scanner;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.MalformedURLException;

public class Main {
	public static void main(String[] args) {
		// Host hardcoded for purpose of the current problem, though we could just as easily take it as an argument
		String host = "http://localhost:8080/"; 
		Scanner scan = new Scanner(System.in);
		System.out.println("[INFO] Attempting to connect to server...");

		// The first time we connect, get the list of available commands from the server
		receiveFromURL(host + "?type=help");
		
		System.out.print(">");
		String command = scan.nextLine();

		// Simple loop for making repeat requests
		while (!command.equals("exit")) {
			receiveFromURL(host + "?type=" + command);
			System.out.print(">");
			command = scan.nextLine();
		}

		scan.close();
		System.out.println("[INFO] Quitting.");
	}

	// Create URL from a string, making sure the format is correct, then read from it
	static void receiveFromURL(String string) {
		try {
			URL url = new URL(string);
			receiveFrom(url);
		} catch (MalformedURLException e) {
			System.out.println("[ERROR] " + e.getMessage());
		}
	}

	// Attempt to open an input stream from the URL and receive and print any response, or print the error
	static void receiveFrom(URL url) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			String receivedLine;
			while ((receivedLine = in.readLine()) != null)
				System.out.println("[SERVER] " + receivedLine);
			in.close();
		} catch (IOException e) {
			System.out.println("[ERROR] " + e.getMessage());
		}
	}
}
