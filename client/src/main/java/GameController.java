
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

public class GameController implements Initializable {



    /////// FXML Variable Declaration //////
    @FXML
    private Label clientUsername_label;

    @FXML
    private Label money_label;



    @FXML
    private volatile Arc circle_timer;

    @FXML
    private Label opponent_label;

    @FXML
    private volatile Label turnTimer_label;
    @FXML
    private GridPane client_grid;

    @FXML
    private GridPane opponent_grid;

    @FXML
    private ImageView clientAvatar;

    @FXML
    private ImageView opponantAvatar;


    @FXML
    private Button loseTurn_button;

    @FXML
    private MenuButton chat_menuButton;

    @FXML
    private VBox chat_vbox;


    /////// FXML Variable Declaration  END//////

    //================================================================

    /////// Variable Declaration //////

    private Connection connectionToServer;
    private SeaBattleButton[][] clientGridArray;
    private SeaBattleButton[][] opponentGridArray;
    private int clientGridSize;

    private final Gson gson = new Gson();
    
    private boolean turn = false;
    
    private ActionType actionType = ActionType.NOTHING;
    Timer timer;
    private final PseudoClass markClass = PseudoClass.getPseudoClass("mark");

    private final PseudoClass destroyedClass = PseudoClass.getPseudoClass("destroyed");
    private final PseudoClass attackClass = PseudoClass.getPseudoClass("attack");

    
    private ArrayList<SeaBattleButton> targetCells = null;


    
    private Stage mainStage = null;
    
    private int numberOfShips = 10;

    /////// Variable Declaration END//////

    //================================================================

    /////// Initialization //////
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {

        client_grid.getStylesheets()
                .addAll(getClass().getResource("styles/gameClientGrid.css").toExternalForm());
        opponent_grid.getStylesheets()
                .addAll(getClass().getResource("styles/gameClientGrid.css").toExternalForm());

    }

    //this function is called from the startup controller after the initialization of game controller
    public void initValues(Connection connectionToServer, SeaBattleButton[][] clientGridArray, int clientGridSize, Stage stage) {
        this.connectionToServer = connectionToServer;
        this.clientGridArray = clientGridArray;
        this.clientGridSize = clientGridSize;
        this.mainStage = stage;

        startUp();
    }


    // starts after initValues()
    public void startUp() {
        populateClientGrid(client_grid, clientGridSize);
        populateOpponantGridWithEmptyCells(opponent_grid, clientGridSize);

        clientUsername_label.setText(connectionToServer.getUsername());

        changeTurnGUI(turn);


        setupChatMenuButton();

    }

    /////// Initialization END //////

    //================================================================

    /////// FXML Method Declaration //////

    @FXML
    void onLoseTurnActionButton(ActionEvent event) {
        if(turn)
        {
            connectionToServer.sendToServer(ASBTP.LOSE_TURN);
            cancelActionType();

        }
    }

    @FXML
        void onHelpMenuButtonAction(ActionEvent event) {
            popupAlert(AlertType.INFORMATION, "Developed by AmmA");
        }


    @FXML
    void onQuitMenuButtonAction(ActionEvent event) {

        if(mainStage != null)
        {
            mainStage.close();
        }
    }

    /////// FXML Method Declaration END//////

    //================================================================


    ///////  Method Declaration //////


    synchronized public void  turnTimer(boolean turn) {
        if (turn) {

            circle_timer.setLength(360);
            final int[] interval = {30};
            if (timer == null) timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                public
                void run() {
                    if (interval[0] > 0) {
                        Platform.setImplicitExit(false);
                        Platform.runLater(() -> {
                            turnTimer_label.setText("Time to start: " + interval[0]);
                            circle_timer.setLength(circle_timer.getLength() - 12);
                        });
                        interval[0]--;
                    } else {
                        Platform.setImplicitExit(false);
                        Platform.runLater(() -> {
                            circle_timer.setLength(360);
                            loseTurn_button.fire();
                            timer.cancel();
                        });
                    }
                }
            }, 1000, 1000);
        }
    else if(timer!=null){
        circle_timer.setLength(360);
        timer.cancel();
    }


    }


    public void handleServerMessage(HashMap<String, String> msg) {


        String responseCode = msg.get(ASBTP.ACTION_CODE);
        String body = msg.get(ASBTP.BODY);

        switch (responseCode) {

            //indicates who's turn it is
            case ASBTP.CLIENT_TURN: {

                turn = body.equals(ASBTP.TRUE);




                Platform.runLater(() -> changeTurnGUI(turn));
                break;
            }

            //this response code means that this player has been attacked by the other player
            //it receives a list of attacked cells and updates client grid occurdingly
            case ASBTP.ATTACK: {

                String json = body;
                Type collectionType = new TypeToken<ArrayList<Cell>>() {}.getType();
                
                //parse the json
                ArrayList<Cell> clientAttackedCells = gson.fromJson(json, collectionType);

                //update the client board
                Platform.runLater(() -> 
                {

                    //for each attacked cell, set the pseudoclass to destroyed if it's a nont empty cell
                    //if it was an empty cell set it to nothingdestroyed
                    for (Cell c : clientAttackedCells) {
                        SeaBattleButton btn = clientGridArray[c.getCol()][c.getRow()];
                        btn.getCell().setDestroyed(true);
                        if (btn.getCellType() != SeaBattleButton.CellType.EMPTY) {
                            btn.pseudoClassStateChanged(destroyedClass, true);
                        } else {
                            btn.setId("nothingdestroyed");

                        }
                    }

                    ArrayList<Cell> secondHandDestroyedCells=checkIfShipIsDestroyed(clientAttackedCells,clientGridArray);
                       if(!secondHandDestroyedCells.isEmpty()) attackDone(secondHandDestroyedCells);
                });
                break;
            }
            case ASBTP.ATTACK_DONE:


                // this means that the attack the client just did was a success
                //updates the opponant grid based on the received cells from the server
            case ASBTP.ATTACK_OK: {

                String json = body;
                Type collectionType = new TypeToken<ArrayList<Cell>>() {}.getType();

                //parse the json
                ArrayList<Cell> secondHandDestroyedCells = gson.fromJson(json, collectionType);

                //update the client board
                Platform.runLater(() -> {

                    //update the opponant grid
                    for (Cell c : secondHandDestroyedCells) {
                        SeaBattleButton btn = opponentGridArray[c.getCol()][c.getRow()];
                        btn.setCell(c);
                        btn.getCell().setDestroyed(true);
                        if (btn.getCell().getCellType() != ASBTP.EMPTY) {
                            btn.setId("destroyed");
                        } else {
                            btn.setId("nothingdestroyed");

                        }

                    }


                });
                break;
            }

            //the client updates its balance
            case ASBTP.BALANCE: {
                Platform.runLater(() -> {
                    money_label.setText(body);
                });
                break;
            }




            //on game end simply popup a dialogue
            case ASBTP.GAME_END:
            {

                Platform.runLater(()-> 
                {
                    endGameAlert(body);
                });
                break;
            }



            //if an error happened, display a pop up
            case ASBTP.ERROR:

                Platform.runLater(()-> 
                {
                    popupAlert(AlertType.ERROR,"something is wrong!");
                });
                break;

            default:
                break;
        }

    }



    private void populateClientGrid(GridPane gridpane, int size)
    {
        gridpane.getChildren().clear();
        for(int i = 0; i < size; i++)
        {
           for(int j = 0; j < size; j++)
           {
               SeaBattleButton btn = clientGridArray[i][j];

               gridpane.add(btn, i, j);
           }

        } 

    }



    private void populateOpponantGridWithEmptyCells(GridPane gridpane, int size)
    {
        opponentGridArray = new SeaBattleButton[size][size];
        gridpane.getChildren().clear();
        for(int i = 0; i < size; i++)
        {
           for(int j = 0; j < size; j++)
           {
               SeaBattleButton btn = new SeaBattleButton(SeaBattleButton.CellType.EMPTY, i, j);
               btn.setPrefSize(80, 80);

               btn.getStyleClass().add("empty-cell");
               btn.setOnAction(onOpponantGridButtonClick);
               btn.setOnMouseEntered(onOpponantGridButtonMouseHover);
               btn.setOnMouseExited(onOpponantGridButtonMouseHoverExit);
               gridpane.add(btn, i, j);
               opponentGridArray[i][j] = btn;
           }

        } 

    }


    //////// ATTACK Methods ////////


    // private void attack(SeaBattleButton targetButton, SeaBattleButton attackingButton) {
     private void attack(SeaBattleButton targetButton) {

        Point targetCell = new Point(targetButton.getCell().getCol(), targetButton.getCell().getRow());

        connectionToServer.sendToServer(ASBTP.ATTACK);

        connectionToServer.sendToServer(gson.toJson(targetCell));


        cancelActionType();


    }
    private void attackDone(ArrayList<Cell> secondHandDestroyedCells) {

        connectionToServer.sendToServer(ASBTP.ATTACK_DONE);
        connectionToServer.sendToServer(gson.toJson(secondHandDestroyedCells));

        cancelActionType();


    }



    private ArrayList<Cell> checkIfShipIsDestroyed(ArrayList<Cell> attackedCells,SeaBattleButton[][] gridArray)
    {
        ArrayList<Cell>secondHandDestroyedCells=new ArrayList <>();
        for (Cell c : attackedCells)
        {
            if(c.getCellType() != ASBTP.EMPTY)
            {

                Cell destroyedCellHeadShip = gridArray[c.getHeadShipCordinates().getCol()][c.getHeadShipCordinates().getRow()].getCell();


                if(destroyedCellHeadShip.getBlood() > -1)
                {

                    boolean hasDestroyedEntireShip = true;
                    for(Point p : destroyedCellHeadShip.getChildShips())
                    {
                        Cell childCell = gridArray[p.getCol()][p.getRow()].getCell();

                        if(!childCell.isDestroyed())
                        {
                            hasDestroyedEntireShip = false;
                        }
                    }
                    if(!destroyedCellHeadShip.isDestroyed())
                    {
                        hasDestroyedEntireShip = false;
                    }

                    if(hasDestroyedEntireShip)
                    {
                        SeaBattleButton destroyedBtn=gridArray[destroyedCellHeadShip.getCol()][destroyedCellHeadShip.getRow()];
                        int colLimit=destroyedBtn.getShip().getAreaCol();
                        int rowLimit=destroyedBtn.getShip().getAreaRow();

                        for(int i = -1; i <= rowLimit; i++)
                        {
                            for(int j = -1; j <= colLimit; j++)
                            {
                                int curCol = destroyedCellHeadShip.getCol() + j;
                                int curRow = destroyedCellHeadShip.getRow() + i;
                                if(curCol>-1 && curCol<clientGridSize &&curRow>-1 && curRow<clientGridSize && !gridArray[curCol][curRow].getCell().isDestroyed()  ){
                                    SeaBattleButton curBtn= gridArray[curCol][curRow];
                                    curBtn.setId("nothingdestroyed");
                                    secondHandDestroyedCells.add(curBtn.getCell());

                                }

                            }
                        }






                        for(Point p : destroyedCellHeadShip.getChildShips())
                        {
                            Cell childCell = clientGridArray[p.getCol()][p.getRow()].getCell();

                            childCell.setBlood(-1);

                        }
                         destroyedCellHeadShip.setBlood(-1);

                        //if the ship that has completely been destroyed is a fort then decrease the number of target player forts

                        numberOfShips -= 1;

                    }

                }
            }
        }
   return secondHandDestroyedCells; }



    ////// ATTACK Methods END ////////










    //mark the client grid buttons based on the given parameters
    private void clientGridButtonHoverMark(SeaBattleButton hoveredBtn, int rowLimit, int colLimit, boolean mark) {

        boolean isPlaceable = true;
        // check whether the button is placeable here
        int clickedBtnRow = hoveredBtn.getCell().getRow();
        int clickedBtnCol = hoveredBtn.getCell().getCol();

        for (int i = 0; i < rowLimit; i++) {
            for (int j = 0; j < colLimit; j++) {
                int curCol = clickedBtnCol + j;
                int curRow = clickedBtnRow + i;
                if (curCol >= clientGridSize || curRow >= clientGridSize
                        || (clientGridArray[curCol][curRow].getCellType() != SeaBattleButton.CellType.EMPTY)) {
                    isPlaceable = false;
                }
            }
        }
        if (isPlaceable) {

            // After the validation we now mark the button
            for (int i = 0; i < rowLimit; i++) {
                for (int j = 0; j < colLimit; j++) {
                    SeaBattleButton currentBtn = clientGridArray[clickedBtnCol + j][clickedBtnRow + i];

                    currentBtn.pseudoClassStateChanged(markClass, mark);

                }
            }
        }

    }



    //sets up the chat menu button and attaches listeners to its items
    private void setupChatMenuButton() {

        chat_menuButton.getItems().forEach(new Consumer<MenuItem>() {

            @Override
            public void accept(MenuItem t) {
                t.setOnAction((ActionEvent event) -> {
                    MenuItem mi = (MenuItem) event.getSource();

                    //send the message to server
                    try {
                        if(turn)
                        {
                            connectionToServer.sendToServer(ASBTP.CHAT);
                            connectionToServer.sendToServer(mi.getText());
                        }

                    } catch (Exception e) {
                        popupException(e);
                    }
                });

            }
        });
    }


    //a simple popup alert
    private void popupAlert(AlertType alertType, String msg) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Attention!");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.initOwner(mainStage);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.showAndWait();
    }


    //the end game alert
    private void endGameAlert(String result) {
        Alert alert = new Alert(AlertType.INFORMATION);
        String title = "";
        String msg = "";
        switch (result) {
            case ASBTP.WON: {
                title = "Winner Winner, Chicken Dinner!";
                msg = "You Won!\nThanks for playing.\nClick OK to return to the main menu";
                break;
            }

            case ASBTP.LOST: {
                title = "You Lost!";
                msg = "I'm sorry you lost.\nThanks for playing; Better luck next time.\nClick OK to return to the main menu";

                break;
            }

            case ASBTP.OPPONENT_DISCONNECTED: {
                title = "Opponent Left";
                msg = "Opponent Disconnected\nClick OK to return to the main menu";

                break;
            }

            case ASBTP.CLIENT_DISCONNECTED: {
                title = "Lost Connection!";
                msg = "You lost your connection to the server.\nClick OK to return to the main menu";

                break;
            }
            default:
                title = "Game Ended!";
                msg = "Click OK to return to the main menu";
                break;
        }

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.initOwner(mainStage);
        alert.initModality(Modality.WINDOW_MODAL);
        Optional<ButtonType> alertResult = alert.showAndWait();

        if (!alertResult.isPresent()) {
            // end the program
            connectionToServer.closeSocket();
            mainStage.close();

        } else if (alertResult.get() == ButtonType.OK) {
            connectionToServer.closeSocket();
            mainStage.close();

        }else if(alertResult.get() == ButtonType.CANCEL)
        {
            // end the program
            connectionToServer.closeSocket();

            mainStage.close();
        }
    }



    //sets the actiontype to nothing
    //and updates the ation buttons GUI
    private void cancelActionType()
    {
        actionType = ActionType.ATTACK;
      //  actionType = ActionType.NOTHING;
        actionType.setActionButton(null);

    }





    //sets the buttons enable/disable based on the turn
    private void changeTurnGUI(boolean clientTurn)
    {
        // turnTimer(turn);


        double clientAvatarOpacity = clientTurn ? 1: 0.5;
        double opponantAvatarOpacity = clientTurn ? 0.5: 1.0;
    

        clientUsername_label.setDisable(!clientTurn);

        money_label.setDisable(!clientTurn);
        loseTurn_button.setDisable(!clientTurn);
        chat_vbox.setDisable(!clientTurn);
        chat_menuButton.setDisable(!clientTurn);

        clientAvatar.setOpacity(clientAvatarOpacity);
        opponantAvatar.setOpacity(opponantAvatarOpacity);
   
    }





    public void setOpponent_labelText(String text) {
        opponent_label.setText(text);
    }



    public enum ActionType {
        BUY,
        MOVE,
        ATTACK,
        NOTHING;

        private SeaBattleButton actionButton;
        private SeaBattleButton.CellType actionButtonCellType;
        /**
         * @param actionButton the actionButton to set
         */
        public void setActionButton(SeaBattleButton actionButton) {
            this.actionButtonCellType = null;
            this.actionButton = actionButton;
        }
        /**
         * @return the actionButton
         */
        public SeaBattleButton getActionButton() {
            return actionButton;
        }

        /**
         * @param actionButtonCellType the actionButtonCellType to set
         */
        public void setActionButtonCellType(SeaBattleButton.CellType actionButtonCellType) {
            this.actionButtonCellType = actionButtonCellType;
        }
        /**
         * @return the actionButtonCellType
         */
        public SeaBattleButton.CellType getActionButtonCellType() {
            return actionButtonCellType;
        }
    }


    private void popupException(Exception ex)
    {
        Alert alert = new Alert(AlertType.ERROR);
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

        alert.initOwner(mainStage);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.showAndWait();


    }

    //================================================================



    private final EventHandler<ActionEvent> onOpponantGridButtonClick = (ActionEvent event) -> {

        if (turn) {
            SeaBattleButton btn = (SeaBattleButton) event.getSource();

            attack(btn);

            if(targetCells != null)
        {
            for(SeaBattleButton sbb : targetCells)
            {

                sbb.pseudoClassStateChanged(attackClass, false);
            }
        }
            cancelActionType();

        }

    };


    //mark the opponants grid cells based on the selected attacking ship
    private final EventHandler<Event> onOpponantGridButtonMouseHover = (Event event) -> {
        SeaBattleButton hoveredBtn = (SeaBattleButton) event.getSource();

        targetCells = new ArrayList<>();
  Cell targetCell = hoveredBtn.getCell();

             targetCells.add(hoveredBtn);
        for (SeaBattleButton sb : targetCells) {
              sb.pseudoClassStateChanged(attackClass, true);
              }
    };



    private final EventHandler<Event> onOpponantGridButtonMouseHoverExit = (Event event) -> {

        if (targetCells != null) {
            for (SeaBattleButton sb : targetCells) {
                sb.pseudoClassStateChanged(attackClass, false);
            }
            targetCells = null;
        }
    };
    /////// Event Handlers END //////

    //================================================================
}