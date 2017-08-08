package th.co.aerothai.swimgw.client.javafx;

import java.io.IOException;

import javax.ejb.EJB;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import th.co.aerothai.swimgw.services.api.IX400Utils;
import th.co.aerothai.swimgw.services.impl.X400UtilsBean;

public class AppClient extends Application{
    private Stage primaryStage;
    private BorderPane rootLayout;
    
	@Override
	public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("TestApp");
        
        initRootLayout();
        
        showMainview();
	}

    /**
     * Initializes the root layout.
     */
    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(AppClient.class.getResource("RootLayout.fxml"));
//            loader.setLocation(getClass().getResource("RootLayout.fxml"));
            
            rootLayout = (BorderPane) loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    /**
     * Shows the main view inside the root layout.
     */
    public void showMainview() {
        try {
            // Load person overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(AppClient.class.getResource("MainView.fxml"));
//            loader.setLocation(getClass().getResource("MainView.fxml"));
            
            
            AnchorPane mainView = (AnchorPane) loader.load();

            // Set person overview into the center of root layout.
            rootLayout.setCenter(mainView);
            
            // Give the controller access to the main app.
            AmhsMessageController controller = loader.getController();
            controller.setAppClient(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
	public static void main(String[] args) {
		launch(args);
	}
}
