package csc2b.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	// ADDITIONAL ATTRIBUTES
	private ServerSocket ss;
	private boolean connected;

	public Server(int port) {
		try {
			ss = new ServerSocket(port);
			System.out.println("Server has connected successfully on port: " + port);
			connected = true;
			System.out.println("Waiting for clients to connect....");

			while (connected) {
				Socket connect = ss.accept();
				System.out.println("A new client has connected!");
				Thread th = new Thread(new ServerHandler(connect));
				th.start();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Server myServer = new Server(2021);
	}
}
