package th.co.aerothai.swimgw.javafx;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.isode.x400api.X400_att;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import th.co.aerothai.swimgw.models.Msgbox;
import th.co.aerothai.swimgw.models.MsgboxBean;
import th.co.aerothai.swimgw.services.amhs.ConvertorBean;
import th.co.aerothai.swimgw.services.amhs.RcvUtils;
import th.co.aerothai.swimgw.services.amhs.X400UtilsException;
import th.co.aerothai.swimgw.services.jms.Consumer;
import th.co.aerothai.swimgw.services.jms.Producer;

public class ServiceController {

	// Reference to the main application.
	private AppClient appClient;

	@FXML
	private TextField orAdress;

	@FXML
	private TextField dn;

	@FXML
	private TextField pa;

	@FXML
	private PasswordField credential;

	@FXML
	private TextField broker;

	@FXML
	private TextField client;

	@FXML
	private TextField username;

	@FXML
	private PasswordField password;
	
	@FXML
	private Label connectionError;

	@FXML
	private Label connectionStop;
	
	@FXML
	private Circle amhsStatus;
	
	@FXML
	private Circle swimStatus;
	
	@FXML
	private Button startButton;
	
	@FXML
	private Button stopButton;
	
	@FXML
	private AnchorPane amhsToSwimLog;
	
	@FXML
	private AnchorPane swimToAmhsLog;
	
	private boolean swimConnected;
	private boolean amhsConnected;
	
	private boolean serviceStarted;
	
	private Producer producer = new Producer();
	private Consumer consumer = new Consumer();
	
	private Logger logger = Logger.getLogger(ServiceController.class);

	private Logger producerLogger = Logger.getLogger(Producer.class);
	private Logger consumerLogger = Logger.getLogger(Consumer.class);
//	private BooleanProperty amhsProperty = new SimpleBooleanProperty(amhsConnected);
	/**
	 * The constructor. The constructor is called before the initialize()
	 * method.
	 */
	public ServiceController() {

	}

	/**
	 * Initializes the controller class. This method is automatically called
	 * after the fxml file has been loaded.
	 */
	@FXML
	private void initialize() {
		// -- Initialize the application with all default value
		amhsStatus.setFill(Color.RED);
		swimStatus.setFill(Color.RED);
		
		startButton.setDisable(false);
		stopButton.setDisable(true);
		
		amhsConnected = false;
		swimConnected = false;
		
		if(serviceStarted){
			serviceStarted = false;
			Platform.runLater(new Runnable() {
			    @Override
			    public void run() {
					connectionError.setText("Service has stopped successfully");
					connectionError.setTextFill(Color.RED);
			    }
			});	
		}
		
		return;
//		BooleanProperty amhsProperty = new SimpleBooleanProperty(amhsConnected);
//		amhsProperty.bind(amhsProperty);
//		amhsProperty.addListener(new ChangeListener<Boolean>() {
//			@Override
//			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
//				// TODO Auto-generated method stub
//				System.out.println("Change!");
//                if (newValue) {
//                    amhsStatus.setFill(Color.LIMEGREEN);
//                } else {
//                	amhsStatus.setFill(Color.RED);
//                }
//			}
//        });
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
	private void startService() {
		//-- Trying to connect with AMHS ans SWIM
//		System.out.println("Start Service");
		logger.info("Trying to connect with AMHS and SWIM");
		startButton.setDisable(true);
		stopButton.setDisable(false);
		
		if(RcvUtils.testConnection(orAdress.getText(), dn.getText(), pa.getText(), credential.getText()) != X400_att.X400_E_NOERROR) {
			//-- Connecting to AMHS failed
			logger.info("AMHS connecting failed");
			amhsConnected = false;
			amhsStatus.setFill(Color.RED);
			if(!serviceStarted){
				initialize();
				Platform.runLater(new Runnable() {
				    @Override
				    public void run() {
				    	connectionError.setText("Failed to connect to Message Store (AMHS). Please try it again.");
						connectionError.setTextFill(Color.RED);
				    }
				});	
				
				return;
			} else {
				Platform.runLater(new Runnable() {
				    @Override
				    public void run() {
						connectionError.setText("Failed to connect to Message Store (AMHS). The system is attempting to reconnect...");
						connectionError.setTextFill(Color.RED);
				    }
				});	

				if(producer.testConnection(broker.getText(), client.getText()+"-producer", username.getText(), password.getText())) {
					swimConnected = true;
					swimStatus.setFill(Color.LIMEGREEN);
				}

				restartService();
			}
		} else {
			//-- AMHS is okay
			logger.info("AMHS connection was succesfully established");
			amhsConnected = true;
			amhsStatus.setFill(Color.LIMEGREEN);
			Platform.runLater(new Runnable() {
			    @Override
			    public void run() {
					connectionError.setText("");
					connectionError.setTextFill(Color.GREEN);
			    }
			});	

			try {
				producer.create(broker.getText(), client.getText()+"-producer", username.getText(), password.getText());
				consumer.startListening(broker.getText(), client.getText()+"-consumer", username.getText(), password.getText(), "AMHS", orAdress.getText(), dn.getText(), pa.getText(), credential.getText());
				//-- SWIM is okay
				logger.info("SWIM connection was succesfully established");
				swimConnected = true;
				swimStatus.setFill(Color.LIMEGREEN);
				
				//-- Service is started
				serviceStarted = true;
				
				Platform.runLater(new Runnable() {
				    @Override
				    public void run() {
				        connectionError.setText("Service is connected successfully");
				        connectionError.setTextFill(Color.GREEN);
				    }
				});	
				
				startReceivingMessage();

			} catch (JMSException e) {
				//-- Connecting to SWIM failed 
				logger.info("SWIM connecting failed");
				swimConnected = false;
				swimStatus.setFill(Color.RED);
				if(!serviceStarted){
					Platform.runLater(new Runnable() {
					    @Override
					    public void run() {
					    	connectionError.setText("Failed to connect to ActiveMQ (SWIM). Please try it again.");
							connectionError.setTextFill(Color.RED);
					    }
					});	
					initialize();
					return;
				} else {
					Platform.runLater(new Runnable() {
					    @Override
					    public void run() {
							connectionError.setText("Failed to connect to ActiveMQ (SWIM). The system is attempting to reconnect...");
							connectionError.setTextFill(Color.RED);
					    }
					});	

//					try {
//						Thread.sleep(5000);
//					} catch (InterruptedException e2) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
					restartService();
				}
			}
		}

		
	}
	
	
	private void restartService(){
		// shutdown all threads
		execAmhsToSwimService.shutdown();
		execSwimToAmhsService.shutdown();
		if(producer!=null) {
			try {
				producer.closeConnection();
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(consumer!=null){
			try {
				consumer.closeConnection();
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			Thread.sleep(5000L);
			startService();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void startReceivingMessage() {
		// start all threads 
		logger.info("AMHS/SWIM gateway service started running");
		Platform.runLater(new Runnable() {
		    @Override
		    public void run() {
		        connectionError.setText("Service is connected successfully");
		        connectionError.setTextFill(Color.GREEN);
		    }
		});	
		producerLogger.info("Start recieving messages from AMHS to SWIM");
		amhsToSwimService();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		consumerLogger.info("Start recieving messages from SWIM to AMHS");
		swimToAmhsService();
	}
	
	ScheduledExecutorService execAmhsToSwimService;
	private void amhsToSwimService() {
		execAmhsToSwimService = Executors.newScheduledThreadPool(5);
		execAmhsToSwimService.scheduleAtFixedRate(() -> {
			System.out.println("execAmhsToSwimService at: " + new java.util.Date());

			if (producer.isConnectionLost()) {
				Platform.runLater(new Runnable() {
				    @Override
				    public void run() {
				    	swimConnected = false;
				        swimStatus.setFill(Color.RED);
				        connectionError.setText("Failed to connect to ActiveMQ (SWIM). The system is attempting to reconnect...");
				        connectionError.setTextFill(Color.RED);
				    }
				});	
				restartService();
				
			} else {
				List<Msgbox> msgboxes = new ArrayList<>();
				try {

					msgboxes = RcvUtils.getMsgboxBeanList(orAdress.getText(), dn.getText(), pa.getText(),
							credential.getText());

					if (msgboxes.size() > 0) {
						
						for (Msgbox msgbox : msgboxes) {
							String type = ConvertorBean.checkMessageType(msgbox.getMsgText());
							
							msgbox.setMsgType(type);
							MsgboxBean.addMsgbox(msgbox);
							String xml;
							try {
								xml = ConvertorBean.convertMsgboxToXml(msgbox);
								switch (type) {
								case "FPL":
								case "DEF":
								case "ARR":
								case "CHG":
								case "DLA":
								case "CNL":
									try {
										producer.sendMessage(xml, "FIXM");
										
										producerLogger.info("Message" + "(" + msgbox.getId() + "): " + msgbox.getMsgboxToSwimDetail()
										+ " [FIXM]");
//												+ " has been sent to FIXM successfully");
									} catch (JMSException e) {
										producerLogger.error("Message" + "(" + msgbox.getId() + "): " + msgbox.getMsgboxToSwimDetail()
												+ " failed to be sent to FIXM");
										producer.setConnectionLost(true);
										// --messages cannot be sent to SWIM (it was already pulled out of Message Store) --> The message needs to be logged
									}
									break;
								case "NOTAM":
									try {
										producer.sendMessage(xml, "AIXM");
//										MsgboxBean.addMsgbox(msgbox);
										producerLogger.info("Message" + "(" + msgbox.getId() + "): " + msgbox.getMsgboxToSwimDetail()
										+ " [AIXM]");
//												+ " has been sent to AIXM successfully");
									} catch (JMSException e) {
										producerLogger.error("Message" + "(" + msgbox.getId() + "): " + msgbox.getMsgboxToSwimDetail()
												+ " failed to be sent to AIXM");
										producer.setConnectionLost(true);
										// --messages cannot be sent to AIXM (it was already pulled out of Message Store) --> The message needs to be logged
									}
									break;
								case "SA":
								case "SP":
								case "WS":
								case "FT":
								case "FC":
								case "FK":
								case "FV":
								case "WV":
								case "WC":
								case "WO":
								case "UA":
								case "NO":
									try {
										producer.sendMessage(xml, "WXXM");
										producerLogger.info("Message" + "(" + msgbox.getId() + "): " + msgbox.getMsgboxToSwimDetail()
										+ " [WXXM]");
//												+ " has been sent to WXXM successfully");
									} catch (JMSException e) {
										producerLogger.error("Message" + "(" + msgbox.getId() + "): " + msgbox.getMsgboxToSwimDetail()
												+ " failed to be sent to WXXM");
										// --messages cannot be sent to WXXM (it was already pulled out of Message Store) --> The message needs to be logged
										producer.setConnectionLost(true);
									}
									break;
								default:
									logger.info("Message" + "(" + msgbox.getId() + "): " + msgbox.getMsgboxToSwimDetail()
									+ " cannot be categorized to AIXM/FIXM/WXXM");
									// --messages cannot be categorized --> The message needs to be logged
									break;
								}
							} catch (JAXBException e1) {
								producerLogger.error("Message: " + msgbox.getMsgboxToSwimDetail()
								+ " cannot be converted to XML format");
							}

						}
					}
				} catch (X400UtilsException e) {
					amhsConnected = false;
					Platform.runLater(new Runnable() {
					    @Override
					    public void run() {
					        amhsStatus.setFill(Color.RED);
					        connectionError.setText("Failed to connect to Message Store (AMHS). The system is attempting to reconnect...");
					        connectionError.setTextFill(Color.RED);
					    }
					});	
					restartService();
				}
				
			}

		}, 0, 5000L, TimeUnit.MILLISECONDS);
	}

	ScheduledExecutorService execSwimToAmhsService;
	private void swimToAmhsService() {
		execSwimToAmhsService = Executors.newScheduledThreadPool(5);
		execSwimToAmhsService.scheduleAtFixedRate(() -> {
			// The repetitive task, say to update Database
			System.out.println("swimToAmhsService at: " + new java.util.Date());
			if (consumer.isSwimConnectionLost()) {
				Platform.runLater(new Runnable() {
				    @Override
				    public void run() {
				        swimStatus.setFill(Color.RED);
				        connectionError.setText("Failed to connect to ActiveMQ (SWIM). The system is attempting to reconnect...");
				        connectionError.setTextFill(Color.RED);
				    }
				});	
				restartService();
				
			} else if(consumer.isAmhsConnectionLost()) {
				// --messages cannot be sent to Message stores (it was already pulled out of Message Store) --> The message needs to be logged
				Platform.runLater(new Runnable() {
				    @Override
				    public void run() {
				        amhsStatus.setFill(Color.RED);
				        connectionError.setText("Failed to connect to Message Store (AMHS). The system is attempting to reconnect...");
				        connectionError.setTextFill(Color.RED);
				    }
				});	
				restartService();
			}
			

		}, 0, 20000L, TimeUnit.MILLISECONDS);
	}
	/**
	 * Called when the user clicks on the stop button.
	 */
	@FXML
	private void stopService() {
		System.out.println("Stop Service");
		// shutdown all threads
				execAmhsToSwimService.shutdown();
				producerLogger.info("Stop recieving messages from AMHS to SWIM");
				
				execSwimToAmhsService.shutdown();
				consumerLogger.info("Stop recieving messages from SWIM to AMHS");
				if(producer!=null) {
					try {
						producer.closeConnection();
					} catch (JMSException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(consumer!=null){
					try {
						consumer.closeConnection();
					} catch (JMSException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				initialize();
	}
	
	@FXML
	private void clearAmhsToSwimlog() {
		appClient.clearAmhsToSwimView();
	
	}
	@FXML
	private void clearSwimToAmhslog() {
		appClient.clearSwimToAmhsView();
	
	}
}
