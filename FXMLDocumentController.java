/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import java.net.*;
import java.io.*;

public class FXMLDocumentController implements Initializable {
    
    private boolean authorized;
    
    public void setAthorized(boolean authorized)
    {
        this.authorized = authorized;
        if(authorized)//???????????
        {
          authPanel.setVisible(false);
          authPanel.setManaged(false);
          msgPanel.setVisible(true);
          msgPanel.setManaged(true);
        }
        else
        {
          authPanel.setVisible(true);
          authPanel.setManaged(true);
          msgPanel.setVisible(false);
          msgPanel.setManaged(false);
        }
    }

    private Socket socket;
    private DataInputStream  in;
    private DataOutputStream out;

    @FXML
    private Button sendButton, connectBtn;

    @FXML
    private TextArea TextArea;

    @FXML
    private TextField TextField, loginField;

    @FXML
    private PasswordField passwordField;

    @FXML
    HBox authPanel, msgPanel;

    @FXML
    AnchorPane MainAnchorPane;

    public void sendMsg() {

        try
        {
            String str = TextField.getText();
			try{
                FileOutputStream f=new FileOutputStream("logfile.txt", true);
                byte b[]=(System.lineSeparator()+str).getBytes();//converting string into byte array
                f.write(b);
                f.close();
            }
			catch(Exception e)
				System.out.println(e);
			
            out.writeUTF(str);
            TextField.clear();
            TextField.requestFocus();
        }
        catch (IOException ex)
        {
            ex.getStackTrace();
        }
    }

    public void sendMsg(String msg) {

        try
        {
            out.writeUTF(msg);
        }
        catch (IOException ex)
        {
            ex.getStackTrace();
        }
    }

    public void sendAuth() {
        connect();
        sendMsg("/auth " + loginField.getText() + " " + passwordField.getText());
        loginField.clear();
        passwordField.clear();
    }

    public void connect()
    {
       if(socket==null || socket.isClosed())
       {
        try
        {
          socket = new Socket("localhost", 12345);
          in  = new DataInputStream(socket.getInputStream());
          out = new DataOutputStream(socket.getOutputStream());
		  
          String sFromFile="";
          try{
				BufferedReader in = new BufferedReader(new FileReader("logfile.txt"));
				String s = "";
				while( (s = in.readLine()) != null) {
                  String sUTF8Str = new String(s.getBytes(),"UTF-8");
                  sFromFile+=(System.lineSeparator()+sUTF8Str);
				}
				
				//read last qty=5 lines from the log file of history of messages and display in chat window
                int qty=5;
                int lastPositionOfSeparator=sFromFile.length();
                int j =0;
                for (j =0; j<qty ; j++) {
                    int l = sFromFile.substring(0,lastPositionOfSeparator).lastIndexOf(System.lineSeparator());
                    if(l>=0) {
                        lastPositionOfSeparator=l;
                        if (j==(qty-1)) {
                            sFromFile=sFromFile.substring(lastPositionOfSeparator);
                            break;
                        }
                    }
                    else break;
                }
                byte[] utf8Bytes = sFromFile.getBytes("UTF8");
                sFromFile = new String(utf8Bytes, "UTF8");
          }catch(Exception e){System.out.println(e);}
          out.writeUTF(sFromFile);
		  
          new Thread(new Runnable(){

          @Override
          public void run()
          {
                 try {
                        while(true)
                        {
                            String str = in.readUTF();
                            if(str.equals("/authok"))
                            {
                                setAthorized(true);
                                break;
                            }
                        }
                        while(true)
                        {
                                String str = in.readUTF();
                                TextArea.appendText(str);
                                TextArea.appendText("\n");
                        }
                 } catch (IOException ex) {
                     ex.printStackTrace();
                 }
                 finally
                         {
                            try
                            {
                                in.close();
                            }
                            catch(IOException ex)
                            {
                                ex.printStackTrace();
                            }
                            try
                            {
                               out.close();
                            }
                            catch(IOException ex)
                            {
                                ex.printStackTrace();
                            }
                            try
                            {
                                socket.close();
                            }
                            catch(IOException ex)
                            {
                                ex.printStackTrace();
                            }

                            setAthorized(false);

                         }//finally
          }
          }).start();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
       }//if
    }//public void connect()


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //URL              - *.fxml
        //ResourceBundle   -
        setAthorized(false);

            Platform.runLater(() -> {

                ((Stage)MainAnchorPane.getScene().getWindow()).setOnCloseRequest(new EventHandler<WindowEvent>(){

                    @Override
                    public void handle(WindowEvent t)
                    {
                        sendMsg("/end");
                        Platform.exit();
                        System.exit(0);
                    }
                });
        } //run
        );// Platform.runLater
    }//public void initialize
}//public class FXMLDocumentController
