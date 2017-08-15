package th.co.aerothai.swimgw.client.javafx;

import java.util.List;

import javax.ejb.EJB;
import javax.xml.registry.infomodel.EmailAddress;

import org.apache.log4j.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import th.co.aerothai.swimgw.models.Msgbox;
import th.co.aerothai.swimgw.services.api.IX400Utils;

public class ConsoleController {
	
    // Reference to the main application.
    private AppClient appClient;
    private Logger logger = Logger.getLogger(ConsoleController.class);
    /**
     * The constructor.
     * The constructor is called before the initialize() method.
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
        logger.info("Add add add logger info");
    }
 
    
    
}
