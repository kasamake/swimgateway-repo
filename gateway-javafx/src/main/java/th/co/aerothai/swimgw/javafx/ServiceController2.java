//package th.co.aerothai.swimgw.javafx;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.ScheduledFuture;
//import java.util.concurrent.TimeUnit;
//
//import javax.jms.JMSException;
//
//import org.apache.log4j.Logger;
//
//import com.isode.x400api.X400_att;
//
//import javafx.application.Platform;
//import javafx.fxml.FXML;
//import javafx.scene.control.Button;
//import javafx.scene.control.Label;
//import javafx.scene.control.PasswordField;
//import javafx.scene.control.TextField;
//import javafx.scene.paint.Color;
//import javafx.scene.shape.Circle;
//import th.co.aerothai.swimgw.models.Msgbox;
//import th.co.aerothai.swimgw.models.MsgboxBean;
//import th.co.aerothai.swimgw.services.amhs.ConvertorBean;
//import th.co.aerothai.swimgw.services.amhs.RcvUtils;
//import th.co.aerothai.swimgw.services.amhs.X400UtilsException;
//import th.co.aerothai.swimgw.services.jms.Consumer;
//import th.co.aerothai.swimgw.services.jms.Producer;
//
//public class ServiceController2 {
//
//	// Reference to the main application.
//	private AppClient appClient;
//
//	@FXML
//	private TextField orAdress;
//
//	@FXML
//	private TextField dn;
//
//	@FXML
//	private TextField pa;
//
//	@FXML
//	private PasswordField credential;
//
//	@FXML
//	private TextField broker;
//
//	@FXML
//	private TextField client;
//
//	@FXML
//	private TextField username;
//
//	@FXML
//	private PasswordField password;
//	
//	@FXML
//	private Label connectionError;
//
//	@FXML
//	private Label connectionStop;
//	
//	@FXML
//	private Circle fromAmhs;
//	
//	@FXML
//	private Circle toSwim;
//	
//	@FXML
//	private Circle fromSwim;
//	
//	@FXML
//	private Circle toAmhs;
//	
//	private boolean swimConnected;
//	private boolean amhsConnected;
//	
//	private boolean startService;
//	
//	private Producer producer = new Producer();
//	private Consumer consumer = new Consumer();
//	
//	private Logger logger = Logger.getLogger(ServiceController2.class);
//
//	/**
//	 * The constructor. The constructor is called before the initialize()
//	 * method.
//	 */
//	public ServiceController2() {
//	}
//
//	/**
//	 * Initializes the controller class. This method is automatically called
//	 * after the fxml file has been loaded.
//	 */
//	@FXML
//	private void initialize() {
//		// Initialize
//
//		fromAmhs.setFill(Color.RED);
//		toSwim.setFill(Color.RED);
//		fromSwim.setFill(Color.RED);
//		toAmhs.setFill(Color.RED);
////		Button button = new Button("Red");
////		button.setOnAction(e -> fromAmhs.setFill(Color.red));
//	}
//
//	/**
//	 * Is called by the main application to give a reference back to itself.
//	 * 
//	 * @param mainApp
//	 */
//	public void setAppClient(AppClient appClient) {
//		this.appClient = appClient;
//	}
//
//	/**
//	 * Called when the user clicks on the start button.
//	 */
//	@FXML
//	private void startReceivingMessage() {
//		System.out.println("Start Receiving Message");
//
////		try {
////			producer.create(broker.getText(), client.getText()+"-producer", username.getText(), password.getText());
////			swimConnected = true;
////
////			int status = RcvUtils.testConnection(orAdress.getText(), dn.getText(), pa.getText(), credential.getText());
////			if(status != X400_att.X400_E_NOERROR) {
////				amhsConnected = false;
////				// disconnect to JMS
////				producer.closeConnection();
////				
////				// set error message on screen to say that there is a problem with AMHS connection
////				connectionError.setText("Failed to connect to Message Store (AMHS). Please try it again.");
////				fromAmhs.setFill(Color.RED);
////				return;
////			} else {
////				amhsConnected = true;
////				startService = true;
////				connectionError.setText("");
//////				connectionError.setTextFill(Color.GREEN);
////				fromAmhs.setFill(Color.LIMEGREEN);
////				toSwim.setFill(Color.LIMEGREEN);
////			}
////			amhsToSwimService();
////
////		} catch (JMSException e) {
////			// TODO Auto-generated catch block
//////			e.printStackTrace();
////			swimConnected = false;
////			//set error message on screen to say that there is a problem with JMS connection
////			connectionError.setText("Failed to connect to ActiveMQ (SWIM). Please try it again.");
////			toSwim.setFill(Color.RED);
////			return;
////		}
//		
//		try {
//			consumer.startListening(broker.getText(), client.getText()+"-consumer", username.getText(), password.getText(), "AMHS", orAdress.getText(), dn.getText(), pa.getText(), credential.getText());
//			
//			fromSwim.setFill(Color.LIMEGREEN);
//	    	toAmhs.setFill(Color.LIMEGREEN);
//			// Edit here***********
//			swimToAmhsService();
//		} catch (JMSException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}
//	
//	ScheduledExecutorService execAmhsToSwimService;
//	private void amhsToSwimService() {
//		execAmhsToSwimService = Executors.newScheduledThreadPool(5);
//		execAmhsToSwimService.scheduleAtFixedRate(() -> {
//			// The repetitive task, say to update Database
//			System.out.println("hi there at: " + new java.util.Date());
//
//			List<Msgbox> msgboxes = new ArrayList<>();
//
//			if (producer.isConnectionLost()) {
//				Platform.runLater(new Runnable() {
//				    @Override
//				    public void run() {
//				        toSwim.setFill(Color.RED);
//				        fromAmhs.setFill(Color.RED);
//				        connectionError.setText("Failed to connect to ActiveMQ (SWIM). The system is attempting to reconnect...");
//				    }
//				});	
//				
//				try {
//					producer.create(broker.getText(), client.getText()+"-producer", username.getText(), password.getText());
//				} catch (JMSException e) {
//					producer.setConnectionLost(true);
//				}
//				
//			} else {
//				Platform.runLater(new Runnable() {
//				    @Override
//				    public void run() {
//				        toSwim.setFill(Color.LIMEGREEN);
//				        connectionError.setText("");
//				    }
//				});	
//				try {
//
//					msgboxes = RcvUtils.getMsgboxBeanList(orAdress.getText(), dn.getText(), pa.getText(),
//							credential.getText());
//					amhsConnected = true;
//					Platform.runLater(new Runnable() {
//					    @Override
//					    public void run() {
//					        fromAmhs.setFill(Color.LIMEGREEN);
//					        connectionError.setText("");
//					    }
//					});	
//					
//					if (msgboxes.size() > 0) {
//
//						for (Msgbox msgbox : msgboxes) {
//							String type = ConvertorBean.checkMessageType(msgbox.getMsgText());
//							msgbox.setMsgType(type);
//							// System.out.println("Type: "+type);
//							String xml = ConvertorBean.convertMsgboxToXml(msgbox);
//							// System.out.println("XML: "+xml);
//							switch (type) {
//							case "FPL":
//							case "DEF":
//							case "ARR":
//							case "CHG":
//							case "DLA":
//							case "CNL":
//								try {
//									producer.sendMessage(xml, "FIXM");
//									MsgboxBean.addMsgbox(msgbox);
//									logger.info("Message: " + msgbox.getMsgboxToSwimDetail()
//											+ " has been sent to FIXM successfully");
//								} catch (JMSException e) {
//									logger.info("Message: " + msgbox.getMsgboxToSwimDetail()
//											+ " failed to be sent to FIXM");
//									producer.setConnectionLost(true);
//								}
//								break;
//							case "NOTAM":
//								try {
//									producer.sendMessage(xml, "AIXM");
//									MsgboxBean.addMsgbox(msgbox);
//									logger.info("Message: " + msgbox.getMsgboxToSwimDetail()
//											+ " has been sent to AIXM successfully");
//								} catch (JMSException e) {
//									logger.info("Message: " + msgbox.getMsgboxToSwimDetail()
//											+ " failed to be sent to FIXM");
//									producer.setConnectionLost(true);
//									// LOGGER.error("Message: "+
//									// msgbox.getMsgboxToSwimDetail()+" cannot
//									// be sent to AIXM", e);
//								}
//								break;
//							case "SA":
//							case "SP":
//							case "WS":
//							case "FT":
//							case "FC":
//							case "FK":
//							case "FV":
//							case "WV":
//							case "WC":
//							case "WO":
//							case "UA":
//							case "NO":
//								try {
//									producer.sendMessage(xml, "WXXM");
//									MsgboxBean.addMsgbox(msgbox);
//									logger.info("Message: " + msgbox.getMsgboxToSwimDetail()
//											+ " has been sent to WXXM successfully");
//								} catch (JMSException e) {
//									logger.info("Message: " + msgbox.getMsgboxToSwimDetail()
//											+ " failed to be sent to WXXM");
//									producer.setConnectionLost(true);
//								}
//								break;
//							default:
//
//								// messages cannot be categorized --> This
//								// incidence needs to be logged
//								// producer.sendMessage(xml);
//								break;
//							}
//
//						}
//					}
//				} catch (X400UtilsException e) {
//					// TODO Auto-generated catch block
//					amhsConnected = false;
//					Platform.runLater(new Runnable() {
//					    @Override
//					    public void run() {
//					        fromAmhs.setFill(Color.RED);
//					        connectionError.setText("Failed to connect to Message Store (AMHS). The system is attempting to reconnect...");
//					    }
//					});	
//				}
//				
//			}
//
//		}, 0, 5000L, TimeUnit.MILLISECONDS);
//	}
//
//	/**
//	 * Called when the user clicks on the stop button.
//	 */
//	@FXML
//	private void stopReceivingMessage() {
//		System.out.println("Stop Receiving Message");
//		startService = false;
////		if(producer!=null) {
////			try {
////				producer.closeConnection();
////				if(execAmhsToSwimService!=null){
////					execAmhsToSwimService.shutdown();
////				}
////				System.out.println("Service has stopped successfully");
////				connectionError.setText("Service has stopped successfully");
////				
////				fromAmhs.setFill(Color.RED);
////				toSwim.setFill(Color.RED);
////			} catch (JMSException e) {
////				// TODO Auto-generated catch block
////				connectionError.setText("Failed to close connection to ActiveMQ (SWIM). Please try it again.");
////			}
////		}
//		
//		if(consumer!=null) {
//			try {
//				consumer.closeConnection();
//				if(execSwimToAmhsService!=null){
//					execSwimToAmhsService.shutdown();
//				}
//				System.out.println("Service has stopped successfully");
//				connectionError.setText("Service has stopped successfully");
//				
//				fromSwim.setFill(Color.RED);
//				toAmhs.setFill(Color.RED);
//			} catch (JMSException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}
//
//	ScheduledExecutorService execSwimToAmhsService;
//	private void swimToAmhsService() {
//		execSwimToAmhsService = Executors.newScheduledThreadPool(5);
//		execSwimToAmhsService.scheduleAtFixedRate(() -> {
//			// The repetitive task, say to update Database
//			System.out.println("hi there swimToAmhsService at: " + new java.util.Date());
////			System.out.println(consumer.isSwimConnectionLost()+ "   ++   "+consumer.isAmhsConnectionLost());
//			if (consumer.isSwimConnectionLost()) {
//				Platform.runLater(new Runnable() {
//				    @Override
//				    public void run() {
//				        fromSwim.setFill(Color.RED);
//				    	toAmhs.setFill(Color.RED);
//				        connectionError.setText("Failed to connect to ActiveMQ (SWIM). The system is attempting to reconnect...");
//				    }
//				});	
//				
//				try {
//					consumer.startListening(broker.getText(), client.getText()+"-consumer", username.getText(), password.getText(), "AMHS", orAdress.getText(), dn.getText(), pa.getText(), credential.getText());
//					
//					fromSwim.setFill(Color.LIMEGREEN);
//			    	toAmhs.setFill(Color.LIMEGREEN);
//				} catch (JMSException e) {
//					consumer.setSwimConnectionLost(true);
//					fromSwim.setFill(Color.RED);
//			    	toAmhs.setFill(Color.RED);
//			    	connectionError.setText("Failed to connect to ActiveMQ (SWIM). The system is attempting to reconnect...");
//				}
//			} else if(consumer.isAmhsConnectionLost()) {
//				connectionError.setText("Failed to connect to Message Store (AMHS). The system is attempting to reconnect...");
//				// xxxx
//			}
//			
//
//		}, 0, 20000L, TimeUnit.MILLISECONDS);
//	}
//}
