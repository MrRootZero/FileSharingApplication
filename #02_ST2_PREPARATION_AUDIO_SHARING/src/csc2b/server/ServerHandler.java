package csc2b.server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

public class ServerHandler implements Runnable {
	private Socket connectionToClient;

	// TEXTUAL STREAMS
	private BufferedReader in;
	private PrintWriter out;

	// BINARY STREAMS
	private InputStream IS;
	private OutputStream OS;

	// STRUCTURED DATA STREAMS
	private DataInputStream DIS;
	private DataOutputStream DOS;

	private boolean authenticated;
	private boolean processing;

	public ServerHandler(Socket connection) {
		this.connectionToClient = connection;
		try {
			IS = connection.getInputStream();
			OS = connection.getOutputStream();

			DIS = new DataInputStream(IS);
			DOS = new DataOutputStream(OS);

			in = new BufferedReader(new InputStreamReader(IS));
			out = new PrintWriter(OS);

			System.out.println("Streams have all been set up successflly!");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		System.out.println("\nServer is ready to start processing client requests!");
		processing = true;
		try {
			while (processing) {
				String request = in.readLine();
				System.out.println("Got Request: " + request);
				StringTokenizer st = new StringTokenizer(request);
				String command = st.nextToken();

				switch (command) {
				case "BUKAHI":
					// Handles logging in the user to the server
					String userName = st.nextToken();
					String passWord = st.nextToken();

					if (matchUser(userName, passWord)) {
						authenticated = true;
						out.println("YATTA User has logged in successfully!");
						out.flush();
					} else {
						out.println("DAME Unsuccessful Login, please recheck your details");
						out.flush();
					}
					break;
				case "BUKALIST":
					// Handles returning the list of files
					if (authenticated) {
						ArrayList<String> fileArrList = getFileList();
						String send = "";
						for (String s : fileArrList) {
							send += s + "#";
						}
						out.println(send);
						out.flush();
					} else {
						out.println("DAME User is not authenticated");
						out.flush();
					}
					break;
				case "BUKAGET":
					// Handles uploading the file to server so client can download
					if (authenticated) {
						try {
							int ID = Integer.parseInt(st.nextToken());
							String fileName = idToFile(Integer.toString(ID));

							if (fileName != null) {
								File audioFile = new File("data/server/" + fileName);
								if (audioFile.exists()) {
									long fileSize = audioFile.length();
									out.println("YATTA " + fileSize);
									out.flush();

									try {
										FileInputStream fis = new FileInputStream(audioFile);
										byte[] buffer = new byte[1024];
										int n;
										while ((n = fis.read(buffer)) != -1) {
											DOS.write(buffer, 0, n);
											DOS.flush();
										}
										fis.close();
										System.out.println("File has been sent successfully!");
										out.flush();
									} catch (IOException e) {
										e.printStackTrace();
									}
								} else {
									out.println("DAME File not found");
									out.flush();
								}
							} else {
								out.println("DAME Invalid file ID");
								out.flush();
							}
						} catch (NumberFormatException e) {
							e.printStackTrace();
						}
					} else {
						out.println("DAME User is not authenticated");
						out.flush();
					}
					break;
				case "BUKABYE":
					// handles logging out the user
					if (authenticated) {
						authenticated = false;
						out.println("YATTA User has logged out succesfully!");
						out.flush();

						// close all the streams
						DIS.close();
						DOS.close();
						in.close();
						out.close();
						connectionToClient.close();
						processing = false;
					} else {
						out.println("DAME User is not authenticated");
						out.flush();
					}
					break;

				default:
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean matchUser(String username, String password) {
		boolean found = false;
		File userFile = new File("data/server/users.txt");
		try {
			// Code to search users.txt file for match with username and password
			Scanner scan = new Scanner(userFile);
			while (scan.hasNextLine() && !found) {
				String line = scan.nextLine();
				String lineSec[] = line.split("\\s");

				if (lineSec.length >= 2) {
					if (lineSec[0].equals(username) && lineSec[1].equals(password)) {
						found = true;
					}
				}
			}
			scan.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return found;
	}

	private ArrayList<String> getFileList() {
		ArrayList<String> result = new ArrayList<String>();
		// Code to add list text file contents to the arraylist.
		File lstFile = new File("data/server/List.txt");
		try {
			Scanner scan = new Scanner(lstFile);
			while (scan.hasNext()) {
				String line = scan.nextLine();
				result.add(line);
			}

			scan.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return result;
	}

	private String idToFile(String ID) {
		String result = "";
		// Code to find the file name that matches strID
		File lstFile = new File("data/server/List.txt");
		try {
			Scanner scan = new Scanner(lstFile);
			while (scan.hasNext()) {
				String line = scan.nextLine();
				StringTokenizer st = new StringTokenizer(line);
				String strID = st.nextToken();
				String fileName = st.nextToken();

				if (strID.equals(ID)) {
					result = fileName;
					break;
				}
			}
			scan.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return result;
	}

}
