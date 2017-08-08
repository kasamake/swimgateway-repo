package th.co.aerothai.swimgw.client.javafx;

import java.util.List;

import javax.ejb.EJB;
import javax.xml.registry.infomodel.EmailAddress;

import javafx.fxml.FXML;
import th.co.aerothai.swimgw.models.Msgbox;
import th.co.aerothai.swimgw.services.api.IX400Utils;

public class AmhsMessageController {
	
    @EJB
    private static IX400Utils x400UtilsBean;
    
    // Reference to the main application.
    private AppClient appClient;

    /**
     * The constructor.
     * The constructor is called before the initialize() method.
     */
    public AmhsMessageController() {
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
    private void startReceivingMessage() {
        System.out.println("Start Receiving Message");
        try {
			EJBClient.startReceivingMessage();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
 
    /**
     * Called when the user clicks on the start button.
     */
    @FXML
    private void stopReceivingMessage() {
        System.out.println("Stop Receiving Message");
        try {
			EJBClient.stopReceivingMessage();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @FXML
    private void receiveMessage() {
        System.out.println("receive messages from VTBBSWIM");
        try {
			EJBClient.receiveMessage();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
}
