package th.co.aerothai.swimgw.javafx;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.WriterAppender;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import th.co.aerothai.swimgw.services.jms.Consumer;
import th.co.aerothai.swimgw.services.jms.Producer;

public class AppClient extends Application{
    public AppClient() {
    	ProducerAppender.setTextArea(amhsToSwimView);
    	ConsumerAppender.setTextArea(swimToAmhsView);
	}

	private Stage primaryStage;
    private BorderPane rootLayout;
    
    private final TextArea amhsToSwimView = new TextArea();
    private final TextArea swimToAmhsView = new TextArea();
    
    private Logger logger = Logger.getLogger(AppClient.class);
	@Override
	public void start(Stage primaryStage) {
		
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("AMHS/SWIM Gateway Client");
        
        initRootLayout();
        
        showGatewaySetup();
        
        logger.info("AMHS/SWIM Gateway Client Application started");
	}

    /**
     * Initializes the root layout.
     */
    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/RootLayout.fxml"));
//            loader.setLocation(getClass().getResource("RootLayout.fxml"));
            
            rootLayout = (BorderPane) loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
            
            // Give the controller access to the main app.
//            MenuController controller = loader.getController();
//            controller.setAppClient(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    /**
     * Shows the main view inside the root layout.
     */
    public void showGatewaySetup() {
        try {
            // Load person overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/GatewaySetup.fxml"));

            AnchorPane gatewaySetupView = (AnchorPane) loader.load();

            // Set person overview into the center of root layout.
            rootLayout.setCenter(gatewaySetupView);
            
            AnchorPane amhsToSwimAnchor = (AnchorPane)rootLayout.lookup("#amhsToSwimLog");
            setupAmhsToSwimView();
            amhsToSwimAnchor.getChildren().add(amhsToSwimView);
            
            AnchorPane swimToAmhsAnchor = (AnchorPane)rootLayout.lookup("#swimToAmhsLog");
            setupSwimToAmhsView();
            swimToAmhsAnchor.getChildren().add(swimToAmhsView);
            
            ServiceController controller = loader.getController();
            controller.setAppClient(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
//    public void showConsoleView() {
//        try {
//            // Load person overview.
//            FXMLLoader loader = new FXMLLoader();
//            loader.setLocation(getClass().getResource("/fxml/Console.fxml"));
////            loader.setLocation(getClass().getResource("MainView.fxml"));
//            
//            
//            AnchorPane consoleView = (AnchorPane) loader.load();
//            setupLogginView();
//            consoleView.getChildren().add(loggingView);
//            // Set person overview into the center of root layout.
//            rootLayout.setCenter(consoleView);
//            
//            // Give the controller access to the main app.
//            ConsoleController controller = loader.getController();
//            controller.setAppClient(this);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
    private void setupAmhsToSwimView() {
    	amhsToSwimView.setLayoutX(15);
    	amhsToSwimView.setLayoutY(15);
    	amhsToSwimView.setPrefWidth(770);
    	amhsToSwimView.setPrefHeight(260);
    	amhsToSwimView.setWrapText(true);
//    	amhsToSwimView.appendText("Starting recieving messages from AMHS to SWIM\n");
    	amhsToSwimView.setEditable(false);
    }
    private void setupSwimToAmhsView() {
    	swimToAmhsView.setLayoutX(15);
    	swimToAmhsView.setLayoutY(15);
    	swimToAmhsView.setPrefWidth(770);
    	swimToAmhsView.setPrefHeight(260);
    	swimToAmhsView.setWrapText(true);
//    	swimToAmhsView.appendText("Starting recieving messages from SWIM to AMHS\n");
    	swimToAmhsView.setEditable(false);
    }
	public static void main(String[] args) {
		launch(args);
	}
}
