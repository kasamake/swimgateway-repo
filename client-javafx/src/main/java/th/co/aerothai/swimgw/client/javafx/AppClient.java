package th.co.aerothai.swimgw.client.javafx;

import java.io.IOException;

import javax.ejb.EJB;

import org.apache.log4j.WriterAppender;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import th.co.aerothai.swimgw.services.api.IX400Utils;
import th.co.aerothai.swimgw.services.impl.X400UtilsBean;

public class AppClient extends Application{
    public AppClient() {
    	TextAreaAppender.setTextArea(loggingView);
	}

	private Stage primaryStage;
    private BorderPane rootLayout;
    
    private final TextArea loggingView = new TextArea();
    
	@Override
	public void start(Stage primaryStage) {
		
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("AMHS/SWIM Gateway");
        
        initRootLayout();
        
        showGatewaySetup();
        
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
            MenuController controller = loader.getController();
            controller.setAppClient(this);
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
//            loader.setLocation(getClass().getResource("MainView.fxml"));
            
            
            AnchorPane gatewaySetupView = (AnchorPane) loader.load();

            // Set person overview into the center of root layout.
            rootLayout.setCenter(gatewaySetupView);
            
            // Give the controller access to the main app.
            AmhsMessageController controller = loader.getController();
            controller.setAppClient(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void showConsoleView() {
        try {
            // Load person overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/Console.fxml"));
//            loader.setLocation(getClass().getResource("MainView.fxml"));
            
            
            AnchorPane consoleView = (AnchorPane) loader.load();
            setupLogginView();
            consoleView.getChildren().add(loggingView);
            // Set person overview into the center of root layout.
            rootLayout.setCenter(consoleView);
            
            // Give the controller access to the main app.
            ConsoleController controller = loader.getController();
            controller.setAppClient(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void setupLogginView() {
        loggingView.setLayoutX(17);
        loggingView.setLayoutY(64);
        loggingView.setPrefWidth(723);
        loggingView.setPrefHeight(170);
        loggingView.setWrapText(true);
        loggingView.appendText("Starting Application");
        loggingView.setEditable(false);
    }
//	public static void main(String[] args) {
//		launch(args);
//	}
}
