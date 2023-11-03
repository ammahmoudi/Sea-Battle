import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.User;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.sql.SQLOutput;
import java.util.*;

public class AuthController implements Initializable {

    @FXML
    private TextField username_field;

    @FXML
    private PasswordField password_field;

    @FXML
    private Button signIn_button;

    @FXML
    private Button signUp_button;
    @FXML
    private TextArea scores_text;

    @FXML
    void onSignInbuttonAction(ActionEvent event) {
        signIn(username_field.getText(),password_field.getText());
    }

    @FXML
    void onSignUpbuttonAction(ActionEvent event) {
        signUp(username_field.getText(),password_field.getText());

    }



    private Scene scene;
    private Stage mainStage;
    private Connection connectionToServer;
    private final Gson gson = new Gson();
private final Integer authCode=null;

    @Override
    public
    void initialize(URL location, ResourceBundle resources) {
        connectionToServer=new Connection("localhost",13862,this);
        connectionToServer.start();


    }

     //   startUp();

    public void startUp(){
        connectionToServer.sendToServer(ASBTP.SCORES);
    }

    private void signUp(String username,String password) {
        ArrayList<String> signUpPocket=new ArrayList <>();
        signUpPocket.add(username);
        signUpPocket.add(password);
        System.out.println(signUpPocket.toString());
        connectionToServer.sendToServer(ASBTP.SIGN_UP);
        connectionToServer.sendToServer(gson.toJson(signUpPocket));
    // startUp();

    }
    private void signIn(String username,String password) {
        ArrayList<String> signInPocket=new ArrayList <>();
        signInPocket.add(username);
        signInPocket.add(password);
        System.out.println(signInPocket.toString());
        connectionToServer.sendToServer(ASBTP.SIGN_IN);
        connectionToServer.sendToServer(gson.toJson(signInPocket));

    }



    //displays a simple pop up alert
    private void popupAlert(Alert.AlertType alertType, String msg) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Attention!");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.initOwner(mainStage);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.showAndWait();
    }



    //this method handles the game ready response from our server
    //then launches the game controller passes to it the necessary data to it
    public void handleServerMessage(HashMap <String, String> msg)
    {
        String responseCode = msg.get(ASBTP.ACTION_CODE);
        String body = msg.get(ASBTP.BODY);


            switch (responseCode) {
                case ASBTP.SIGN_IN_OK:

                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/startUp.fxml"));
                        Parent root = loader.load();
                        StartUpController startUpController = loader.getController();
                        connectionToServer.setStartUpController(startUpController);
                        scene = signUp_button.getScene();
                        mainStage = (Stage) scene.getWindow();
                        startUpController.initValues(connectionToServer, (Stage) scene.getWindow(),username_field.getText(),authCode);
                        scene.setRoot(root);
                    } catch (IOException e) {
                        popupException(e);
                    }
                    System.out.println("ok");
                    break;


                case ASBTP.ERROR_AUTH:

                    Platform.runLater(()->
                    {
                        popupAlert(Alert.AlertType.ERROR, body);
                    });
                    break;


        case ASBTP.SCORES:

        Platform.runLater(()->
        {
            LinkedList <User> scores= gson.fromJson(body, new TypeToken <LinkedList<User>>(){}.getType());
            scores.sort(Comparator.comparing(User::getScore).reversed());

            scores_text.setText(scores.toString());
        });
        break;
        default:
        break;
    }


    }


    //another simple popup alert that prints out the exception trace
    private void popupException(Exception ex)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("An Error Occured");
        alert.setHeaderText("Opps! something went wrong!");
        alert.setContentText("The program cought the following exception: ");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);

        alert.initModality(Modality.WINDOW_MODAL);
        alert.showAndWait();
    }
}
