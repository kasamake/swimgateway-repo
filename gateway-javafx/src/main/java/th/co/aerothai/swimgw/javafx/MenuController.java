package th.co.aerothai.swimgw.javafx;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class MenuController {
	

    
    // Reference to the main application.
    private AppClient appClient;
 
    /**
     * The constructor.
     * The constructor is called before the initialize() method.
     */
    public MenuController() {
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
    private void showSetupView() {
        System.out.println("showSetupView");
        appClient.showGatewaySetup();
    }
 
//    @FXML
//    private void showConsoleView() {
//        System.out.println("showConsoleView");
//        appClient.showConsoleView();
//    }
//    
    
}
