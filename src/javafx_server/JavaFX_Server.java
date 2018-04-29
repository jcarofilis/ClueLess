package javafx_server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class JavaFX_Server extends Application {

    TextField textTitle;
    Label labelSys, labelPort, labelIp;
    TextArea textAreaMsg;
    CheckBox optWelcome;

    ServerSocket serverSocket;

    @Override
    public void start(Stage primaryStage) {

        textTitle = new TextField();
        labelSys = new Label();
        labelPort = new Label();
        labelIp = new Label();
        textAreaMsg = new TextArea();
        
        //Auto scroll to bottom
        textAreaMsg.setEditable(false);
        textAreaMsg.textProperty().addListener(new ChangeListener(){

            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                textAreaMsg.setScrollTop(Double.MAX_VALUE); 
            }
        });
        
        optWelcome = new CheckBox("Send Welcome when connect");
        optWelcome.setSelected(true);

        labelSys.setText(
            System.getProperty("os.arch") + "/"
            + System.getProperty("os.name"));
        labelIp.setText(getIpAddress());

        VBox mainLayout = new VBox();
        mainLayout.setPadding(new Insets(5, 5, 5, 5));
        mainLayout.setSpacing(5);
        mainLayout.getChildren().addAll(textTitle,
            labelSys, labelPort, labelIp,
            optWelcome, textAreaMsg);

        StackPane root = new StackPane();
        root.getChildren().add(mainLayout);

        Scene scene = new Scene(root, 300, 400);

        primaryStage.setTitle("Android-er: JavaFX Server");
        primaryStage.setScene(scene);
        primaryStage.show();

        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.setDaemon(true); //terminate the thread when program end
        socketServerThread.start();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private class SocketServerThread extends Thread {

        static final int SocketServerPORT = 8080;
        int count = 0;

        @Override
        public void run() {
            try {
                Socket socket = null;
                
                serverSocket = new ServerSocket(SocketServerPORT);
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        labelPort.setText("I'm waiting here: "
                            + serverSocket.getLocalPort());
                    }
                });

                while (true) {
                    socket = serverSocket.accept();
                    count++;
                    
                    //Start another thread 
                    //to prevent blocked by empty dataInputStream
                    Thread acceptedThread = new Thread(
                        new ServerSocketAcceptedThread(socket, count));
                    acceptedThread.setDaemon(true); //terminate the thread when program end
                    acceptedThread.start();

                }
            } catch (IOException ex) {
                Logger.getLogger(JavaFX_Server.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }

    }

    private class ServerSocketAcceptedThread extends Thread {

        Socket socket = null;
        DataInputStream dataInputStream = null;
        DataOutputStream dataOutputStream = null;
        int count;

        ServerSocketAcceptedThread(Socket s, int c) {
            socket = s;
            count = c;
        }

        @Override
        public void run() {
            try {
                dataInputStream = new DataInputStream(
                    socket.getInputStream());
                dataOutputStream = new DataOutputStream(
                    socket.getOutputStream());
 
                //If dataInputStream empty, 
                //this thread will be blocked by readUTF(),
                //but not the others
                String messageFromClient = dataInputStream.readUTF();

                String newMessage = "#" + count + " from " + socket.getInetAddress()
                    + ":" + socket.getPort() + "\n"
                    + "Msg from client: " + messageFromClient + "\n";
                
                Platform.runLater(new Runnable() {
                    
                    @Override
                    public void run() {
                        textAreaMsg.appendText(newMessage);
                    }
                });
                
                if (optWelcome.isSelected()) {
                    
                    String msgReply = count + ": " + textTitle.getText();
                    dataOutputStream.writeUTF(msgReply);
                }
                
            } catch (IOException ex) {
                Logger.getLogger(JavaFX_Server.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException ex) {
                        Logger.getLogger(JavaFX_Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (IOException ex) {
                        Logger.getLogger(JavaFX_Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.close();
                    } catch (IOException ex) {
                        Logger.getLogger(JavaFX_Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

    }

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                    .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                    .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "SiteLocalAddress: "
                            + inetAddress.getHostAddress() + "\n";
                    }
                }
            }
        } catch (SocketException ex) {
            Logger.getLogger(JavaFX_Server.class.getName()).log(Level.SEVERE, null, ex);
        } 

        return ip;
    }

}
