package csc2b.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ClientPane extends GridPane {
	// ADDITIONAL VARIABLES
	private Socket socket = null;

	// TEXTUAL STREAMS
	private BufferedReader in = null;
	private PrintWriter out = null;

	// BINARY STREAMS
	private InputStream IS = null;
	private OutputStream OS = null;

	// STRUCTURED DATA STREAMS
	private DataInputStream DIS = null;
	private DataOutputStream DOS = null;

	// LABELS
	private Label lblUser;
	private Label lblPass;

	// TEXTFIELD AND PASSWORDFIELD
	private TextField txtUser;
	private PasswordField txtPass;
	private TextField txtAudioID;

	// BUTTONS
	private Button btnConnect;
	private Button btnBUKAHI;
	private Button btnBUKALIST;
	private Button btnBUKAGET;
	private Button btnBUKABYE;

	private TextArea txtResponse;
	private String[] listData;

	public ClientPane(Stage stage) {
		initialize();
	}

	private void initialize() {
		createUI();
		setUp();
	}

	private void connect() {
		try {
			String host = "localhost";
			int port = 2021;
			socket = new Socket(host, port);

			IS = socket.getInputStream();
			OS = socket.getOutputStream();

			DIS = new DataInputStream(IS);
			DOS = new DataOutputStream(OS);

			in = new BufferedReader(new InputStreamReader(IS));
			out = new PrintWriter(OS);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void login() {
		out.println("BUKAHI " + txtUser.getText() + " " + txtPass.getText());
		out.flush();
		String response;
		try {
			response = in.readLine();
			txtResponse.appendText(response + "\r\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void listFile() {
		if (socket != null && socket.isConnected()) {
			out.println("BUKALIST");
			out.flush();
			String response;
			try {
				response = in.readLine();
				listData = response.split("#");

				for (int i = 0; i < listData.length; i++) {
					txtResponse.appendText(listData[i] + "\r\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void downloadFile() {
		if (socket != null && socket.isConnected()) {
			try {
				int ID = Integer.parseInt(txtAudioID.getText());
				out.println("BUKAGET " + ID);
				out.flush();

				if (ID > 0 && ID <= listData.length) {
					String fileName = listData[ID - 1].replaceFirst("^\\d+\\s+", "");
					String response = in.readLine();
					System.out.println(response);

					if (response != null && response.startsWith("YATTA")) {
						long fileSize = Long.parseLong(response.substring(6));
						File audioFile = new File("data/client/" + fileName);

						FileOutputStream fos = new FileOutputStream(audioFile);
						byte[] buffer = new byte[1024];
						int n;
						long totalBytes = 0;

						while (totalBytes != fileSize) {
							n = DIS.read(buffer, 0, buffer.length);
							if (n == -1) {
								break;
							}
							fos.write(buffer, 0, n);
							fos.flush();
							totalBytes += n;
						}

						fos.close();
						txtResponse.appendText("The file - " + fileName + " - has been downloaded successfully!\n");
						txtResponse.appendText("File Size: " + fileSize + "\n");
					} else if (response.startsWith("DAME")) {
						txtResponse.appendText("Error: " + response.substring(8));
					} else {
						txtResponse.appendText("Failed to download the image.\n");
					}
				} else {
					txtResponse.appendText("Invalid Audio File ID.\n");
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			txtResponse.appendText("User is not connected to the server.\n");
		}
	}

	private void logout() {
		out.println("BUKABYE");
		out.flush();
		String response;

		try {
			response = in.readLine();
			txtResponse.appendText(response + "\r\n");

			DIS.close();
			DOS.close();
			in.close();
			out.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void setUp() {
		btnConnect.setOnAction(e -> connect());
		btnBUKAHI.setOnAction(e -> login());
		btnBUKALIST.setOnAction(e -> listFile());
		btnBUKAGET.setOnAction(e -> downloadFile());
		btnBUKABYE.setOnAction(e -> logout());

	}

	private void createUI() {
		setHgap(10);
		setVgap(10);
		setAlignment(Pos.CENTER);

		lblUser = new Label("Username: ");
		lblPass = new Label("Password: ");

		txtUser = new TextField();
		txtPass = new PasswordField();
		txtAudioID = new TextField();
		txtAudioID.setPromptText("Enter Audio ID: ");

		txtResponse = new TextArea("Additional Information:\r\n");

		btnConnect = new Button("Connect To Server");
		btnBUKAHI = new Button("Login");
		btnBUKALIST = new Button("List Audio Files");
		btnBUKAGET = new Button("Download Audio File");
		btnBUKABYE = new Button("Log out");

		add(btnConnect, 0, 0);
		add(lblUser, 1, 0);
		add(txtUser, 2, 0);
		add(lblPass, 3, 0);
		add(txtPass, 4, 0);
		add(btnBUKAHI, 5, 0);
		add(btnBUKALIST, 0, 1);
		add(txtAudioID, 1, 1);
		add(btnBUKAGET, 2, 1);
		add(btnBUKABYE, 3, 1);
		add(txtResponse, 0, 3, 6, 1);
	}

}
