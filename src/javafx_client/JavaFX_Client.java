package javafx_client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class JavaFX_Client extends Application 
{

    Label labelSys;
    TextField welcomeMsg;
    Label labelAddress;
    TextField textAddress;
    Label labelPort;
    TextField textPort;
    Button buttonConnect;
    Button buttonClear;
    Label textResponse;

    @Override
    public void start(Stage primaryStage) 
    {

        labelSys = new Label();
        welcomeMsg = new TextField();
        labelAddress = new Label("IP Address");
        textAddress = new TextField();
        labelPort = new Label("Port");
        textPort = new TextField();
        buttonConnect = new Button("Connect");
        buttonClear = new Button("Clear");
        textResponse = new Label();

        labelSys.setText(
                System.getProperty("os.arch") + "/"
                + System.getProperty("os.name"));

        HBox buttonbox = new HBox();
        buttonbox.setSpacing(5);
        HBox.setHgrow(buttonConnect, Priority.ALWAYS);
        HBox.setHgrow(buttonClear, Priority.ALWAYS);
        buttonConnect.setMaxWidth(Double.MAX_VALUE);
        buttonClear.setMaxWidth(Double.MAX_VALUE);
        buttonbox.getChildren().addAll(buttonConnect, buttonClear);

        buttonConnect.setOnAction(buttonConnectEventHandler);
        
        buttonClear.setOnAction(new EventHandler<ActionEvent>() 
        {

            @Override
            public void handle(ActionEvent event) 
            {
                textResponse.setText("");
            }
        });

        VBox mainLayout = new VBox();
        mainLayout.setPadding(new Insets(5, 5, 5, 5));
        mainLayout.setSpacing(5);
        mainLayout.getChildren().addAll(labelSys, welcomeMsg,
                labelAddress, textAddress, labelPort, textPort,
                buttonbox, textResponse);

        StackPane root = new StackPane();
        root.getChildren().add(mainLayout);

        Scene scene = new Scene(root, 300, 400);

        String myPID = ManagementFactory.getRuntimeMXBean().getName();
        primaryStage.setTitle(myPID);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        //Prepare and send preset setting
        String presetMsg = "Hello from " + myPID;
        welcomeMsg.setText(presetMsg);
        textAddress.setText("192.168.1.103");
        textPort.setText("8080");
        RunnableClient presetClient
            = new RunnableClient(textAddress.getText(), 
                    Integer.parseInt(textPort.getText()),
                    presetMsg);
            
        new Thread(presetClient).start();
    }

    public static void main(String[] args) 
    {
        launch(args);
    }

    EventHandler<ActionEvent> buttonConnectEventHandler
        = new EventHandler<ActionEvent>() 
    {

        @Override
        public void handle(ActionEvent event) 
        {
            String tMsg = welcomeMsg.getText();
            if (tMsg.equals("")) 
            {
                tMsg = null;
            }

            RunnableClient runnableClient
                = new RunnableClient(textAddress.getText(), 
                    Integer.parseInt(textPort.getText()),
                    tMsg);
            
            new Thread(runnableClient).start();
        }
    };

    class RunnableClient implements Runnable
    {
        
        String dstAddress;
        int dstPort;
        String response = "";
        String msgToServer;
        
        public RunnableClient(String addr, int port, String msgTo) 
        {
            dstAddress = addr;
            dstPort = port;
            msgToServer = msgTo;
        }

        @Override
        public void run() 
        {
            Socket socket = null;
            DataOutputStream dataOutputStream = null;
            DataInputStream dataInputStream = null;

            try 
            {
                socket = new Socket(dstAddress, dstPort);
                dataOutputStream = new DataOutputStream(
                    socket.getOutputStream());
                dataInputStream = new DataInputStream(socket.getInputStream());

                if(msgToServer != null)
                {
                    dataOutputStream.writeUTF(msgToServer);
                }
    
                response = dataInputStream.readUTF();
   

            } 
            catch (IOException ex) 
            { 
                Logger.getLogger(JavaFX_Client.class.getName()).log(Level.SEVERE, null, ex);
            } 
            finally 
            {
                
                Platform.runLater(new Runnable()
                {

                    @Override
                    public void run() 
                    {
                        textResponse.setText(response);
                    }
                    
                });
                
                if (socket != null) 
                {
                    try 
                    {
                        socket.close();
                    } 
                    catch (IOException ex) 
                    {
                        Logger.getLogger(JavaFX_Client.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                if (dataOutputStream != null) 
                {
                    try 
                    {
                        dataOutputStream.close();
                    } 
                    catch (IOException ex) 
                    {
                        Logger.getLogger(JavaFX_Client.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                if (dataInputStream != null) 
                {
                    try 
                    {
                        dataInputStream.close();
                    } 
                    catch (IOException ex) 
                    {
                        Logger.getLogger(JavaFX_Client.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } //endFinally
        } //endRun
    }//endRunnable

}