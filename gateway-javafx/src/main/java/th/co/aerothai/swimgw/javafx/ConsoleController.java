package th.co.aerothai.swimgw.javafx;

import org.apache.log4j.Logger;

import javafx.fxml.FXML;

public class ConsoleController {

	// Reference to the main application.
	private AppClient appClient;
	Logger logger = Logger.getLogger(ConsoleController.class);

	/**
	 * The constructor. The constructor is called before the initialize()
	 * method.
	 */
	public ConsoleController() {
	}

	/**
	 * Initializes the controller class. This method is automatically called
	 * after the fxml file has been loaded.
	 */
	@FXML
	private void initialize() {
		// Initialize
	}

	/**
	 * Is called by the main application to give a reference back to itself.
	 * 
	 * @param mainApp
	 */
	public void setAppClient(AppClient appClient) {
		this.appClient = appClient;
	}

	/**
	 * Called when the user clicks on the start button.
	 */
	@FXML
	private void addTestMessage() {
		System.out.println("addTestMessage");
		logger.info("Test adding info");
		logger.error("Test adding error long long long long long long long long long long long long long"
				+ " long long long long long long long long");
	}

}
