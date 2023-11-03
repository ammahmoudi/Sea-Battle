
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;



public class StartUpController implements Initializable {


    @FXML
    private TextField server_textField;

    @FXML
    private TextField port_textField;



    @FXML
    private Button connect_button;
    @FXML
    private Button random_button;

    @FXML
    private GridPane client_grid;

    @FXML
    private GridPane fleet_grid;



    @FXML
    private Label timer_label;

    @FXML
    private TextField userName_label;

    @FXML
    private ProgressIndicator connectProgress_ProgressIndicator;



    /////// FXML Variable Declaration END //////

//================================================================

    /////// Variable Declaration //////

    private Scene scene;

    private Stage mainStage;

    private SeaBattleButton[][] clientGridArray;

    private SeaBattleButton selectedFleet = null;

    private int clientGridSize = 10;

    private final PseudoClass hoverClass = PseudoClass.getPseudoClass("hover");

    private int numberOfShipsPlaced = 0;

    private Connection connectionToServer;
    private final LinkedList<SeaBattleButton> ships=new LinkedList <>();
    private Integer authCode=null;

    /////// Variable Declaration END //////

//================================================================

    /////// Initialization //////

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {


        populateFleetGrid(fleet_grid);
        clientGridSize = 10;
        populateGridWithEmptyCells(client_grid, clientGridSize);

        resetFleetGrid(fleet_grid);
        autoStartTimer();

    }

    //this function is called from the startup controller after the initialization of game controller
    public void initValues(Connection connectionToServer,  Stage stage,String username,Integer authCode) {
        this.connectionToServer = connectionToServer;
        this.mainStage = stage;
        this.userName_label.setText(username);
        this.authCode=authCode;
        this.port_textField.setText(String.valueOf(connectionToServer.getServerPort()));



    }


/////// Initialization END //////

//================================================================

    /////// FXML Method Declaration //////


        boolean debug = true;
        @FXML
        void connect_buttonOnAction(ActionEvent event) {


            scene = random_button.getScene();
            mainStage = (Stage) scene.getWindow();


            if(!checkFleetSelection())
            {
                popupAlert(AlertType.ERROR, "Select some fleet by dragging them across");

                return;
            }



            String username = userName_label.getText();
            String ip = server_textField.getText();
            int port = 0;
            try {
                port = Integer.parseInt(port_textField.getText());
            } catch (Exception e) {
                popupAlert(AlertType.ERROR, "Please enter a valid port.");
            }

            //convert the board to json
            String boardJson = convertBoardToJson();

            //lunch a service and start it
            connectionToServer.setUsername(username);
            connectionToServer.setBoardJson(boardJson);
            connectionToServer.setGridSize(clientGridSize);
            connectionToServer.setStartUpController(this);
            connectionToServer.setUsername(username);
            connectionToServer.setAuthCode(authCode);
            connectionToServer.startGame();





            connectProgress_ProgressIndicator.visibleProperty().bind(connectionToServer.runningProperty());
            connect_button.setDisable(true);
            random_button.setDisable(true);
        }
        @FXML
         void random_buttonOnAction(ActionEvent event) {
            resetFleetGrid(fleet_grid);
            populateGridWithEmptyCells(client_grid,10);
            defaultGrid();
        }






    @FXML
        void onHelpMenuButtonAction(ActionEvent event) {
            popupAlert(AlertType.INFORMATION, "Developed by AmmA");
        }

        @FXML
        void onNewMenuButtonAction(ActionEvent event) {

           // gridSize_menu.setText("10 x 10");
            clientGridSize = 10;
            populateGridWithEmptyCells(client_grid, clientGridSize);
            resetFleetGrid(fleet_grid);
        }

        @FXML
        void onQuitMenuButtonAction(ActionEvent event) {
            // get the current scene this controller is set to
            // we can do this using any component inside the controller
            if(connect_button != null)
            {
                Scene scene = connect_button.getScene();
                if(scene != null)
                {
                    mainStage = (Stage) scene.getWindow();
                    mainStage.close();
                }
            }
        }

    /////// FXML Method Declaration END //////

    //================================================================

    /////// Method Declaration //////
    public void autoStartTimer() {
            java.util.Timer timer;
        final int[] interval = {20};
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if(interval[0] > 0)
                {
                    Platform.runLater(() -> timer_label.setText("Time to start: "+ interval[0]));

                    interval[0]--;
                }
                else{
                    Platform.runLater(() -> {

                        resetFleetGrid(fleet_grid);
                        populateGridWithEmptyCells(client_grid,10);
                        defaultGrid();
                         connect_button.fire();
                    });
                  //  defaultGrid();



                    timer.cancel();
                }
            }
        }, 1000,1000);
    }

    //and attaches the related listeners to them
    private void populateFleetGrid(GridPane gridPane) {

        gridPane.getStylesheets().addAll(getClass().getResource("styles/startUpchooseFleet.css").toExternalForm());

        //adding 4 soldiers
        for(int i = 0; i < 4; i++)
        {
            SeaBattleButton btn = new SeaBattleButton(SeaBattleButton.CellType.SOLDIER, i, 0);
            btn.setPrefSize(50, 50);

            btn.setOnDragDetected(onFleetGridButtonDragDetected);
            btn.setOnMouseEntered(onFleetGridButtonMouseEntered);


            btn.setId("soldier");
            gridPane.add(btn , i, 0, 1, 2);
            ships.add(btn);
        }


        //3 calvaries
        for(int i = 0; i < 3; i++)
        {
            SeaBattleButton btn = new SeaBattleButton(SeaBattleButton.CellType.CAVALRY, i, 2);
            btn.setMinSize(50, 100);

            btn.setOnDragDetected(onFleetGridButtonDragDetected);
            btn.setOnMouseEntered(onFleetGridButtonMouseEntered);

            btn.setId("cavalry");
            gridPane.add(btn , i, 1, 1, 3);
            ships.add(btn);
        }


        //2 forts
        for(int i = 0; i < 2; i++)
        {
            SeaBattleButton btn = new SeaBattleButton(SeaBattleButton.CellType.FORT, i, 4);
            btn.setMinSize(50, 150);

            btn.setOnDragDetected(onFleetGridButtonDragDetected);
            btn.setOnMouseEntered(onFleetGridButtonMouseEntered);
            btn.setId("fort");
            gridPane.add(btn , i, 3, 2, 4);
            ships.add(btn);

        }


        //a headquarters
            SeaBattleButton btn = new SeaBattleButton(SeaBattleButton.CellType.HEADQUARTERS, 0, 5);
            btn.setMinSize(50, 200);

            btn.setOnDragDetected(onFleetGridButtonDragDetected);
            btn.setOnMouseEntered(onFleetGridButtonMouseEntered);

            btn.setId("hq");

            gridPane.add(btn , 0, 5, 3, 7);
        ships.add(btn);


    }



    //makes sure that all of the available fleet are selected before connecting to the server
    private boolean checkFleetSelection() {
        return numberOfShipsPlaced == 10;
    }



    //displays a simple pop up alert
    private void popupAlert(AlertType alertType, String msg) {
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
    public void handleServerMessage(HashMap<String, String> msg)
    {
        Platform.runLater(() ->
        {
            switch (msg.get(ASBTP.ACTION_CODE)) {
                case ASBTP.GAME_READY:
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/game.fxml"));
                        Parent root = loader.load();
                        GameController gameController = loader.getController();
                        connectionToServer.setGameController(gameController);
                        gameController.initValues(connectionToServer, clientGridArray, clientGridSize, (Stage) scene.getWindow());
                        gameController.setOpponent_labelText(msg.get(ASBTP.BODY));
                        scene.setRoot(root);
                    } catch (IOException e) {
                        popupException(e);
                    }
                    break;

                default:
                    break;
            }

        });
    }



    //converts the final board to a json
    private String convertBoardToJson()
    {

        ArrayList<Cell> board  = new ArrayList<>();
        for(int i = 0; i < clientGridSize; i++)
        {
            for(int j = 0; j < clientGridSize; j++)
            {
                //i = col and j = row
                SeaBattleButton currentBtn = clientGridArray[i][j];
                board.add(currentBtn.getCell());

            }
        }
        Gson gson = new Gson();
        String json = gson.toJson(board);

		return json;
	}



    //each time the client chooses a new board size this method will be called to clear the client grid
    private void resetFleetGrid(GridPane gridPane)
    {
        gridPane.getChildren().forEach(new Consumer<Node>() {

            @Override
            public void accept(Node t) {
                SeaBattleButton btn = (SeaBattleButton) t;
                btn.setDisable(false);

            }
        });
        selectedFleet = null;
        numberOfShipsPlaced = 0;
    }


    //first makes sure that the spot is available for placement and then places the ship based on the selected orientaation
    private boolean placeShip(GridPane gridPane, SeaBattleButton[][] buttonArray, int size, SeaBattleButton clickedBtn, SeaBattleButton selectedShip)
    {
            return placeShipWithinLimits(gridPane, buttonArray,  size,  clickedBtn, selectedShip, selectedShip.getShip().getAreaRow(), selectedShip.getShip().getAreaCol());

        }

    private boolean placeShipWithinLimits(GridPane gridPane, SeaBattleButton[][] buttonArray, int size, 
    SeaBattleButton clickedBtn, SeaBattleButton selectedShip, int rowLimit, int colLimit)
    {
        int clickedBtnRow = clickedBtn.getCell().getRow();
        int clickedBtnCol = clickedBtn.getCell().getCol();
        boolean marked=true;
        for(int i = -1; i <= rowLimit; i++)
        {
            for(int j = -1; j <= colLimit; j++)
            {
                int curCol = clickedBtnCol + j;
                int curRow = clickedBtnRow + i;
                if( curCol >= clientGridSize || curRow >= clientGridSize ||curRow==-1||curCol==-1|| (buttonArray[curCol][curRow].getCellType() != SeaBattleButton.CellType.EMPTY ))
                {
                  marked=false;
                }
                if(curRow==-1||curCol==-1||curRow==10||curCol==10){
                    marked=true;
                }
                if (!marked)return false;
            }
        }

        //After the validation we now place the button
        int imageIndexCounter = 1;
        for(int i = 0; i < rowLimit; i++)
        {
            for(int j = 0; j < colLimit; j++)
            {
                SeaBattleButton currentBtn =  buttonArray[clickedBtnCol + j][clickedBtnRow + i];
                currentBtn.setShip(selectedShip.getCellType());
                currentBtn.getCell().setHeadShipCordinates(new Point(clickedBtnCol, clickedBtnRow));
                currentBtn.pseudoClassStateChanged(hoverClass, false);
                
                currentBtn.setId(currentBtn.getCellType().toString().toLowerCase() + imageIndexCounter);
                //add the child ships to the headship
                if(!currentBtn.equals(clickedBtn))
                {
                    clickedBtn.getCell().getChildShips().add(new Point(currentBtn.getCell().getCol(), currentBtn.getCell().getRow()));
                }
                imageIndexCounter++;
                
            }
        }
        return true;
    }



    //populates the client grid with empty cells and attaches the necessary listeners to it
    private void populateGridWithEmptyCells(GridPane gridPane, int size)
    {
        clientGridArray = new SeaBattleButton[size][size];
        gridPane.getStylesheets().addAll(getClass().getResource("styles/startUpClientGrid.css").toExternalForm());
        gridPane.getChildren().clear();
        
        
        for(int i = 0; i < size; i++)
        {
           for(int j = 0; j < size; j++)
           {
               SeaBattleButton btn = new SeaBattleButton(SeaBattleButton.CellType.EMPTY, i, j);
               btn.setPrefSize(80, 80);
               btn.getStyleClass().add("empty-cell");

               btn.setOnDragOver(onClientGridButtonDragOver);
               btn.setOnDragEntered(onClientGridButtonMouseDragEntered);
               btn.setOnDragExited(onClientGridButtonMouseDragExited);
               btn.setOnDragDropped(onClientGridButtonDragDropped);
               
               gridPane.add(btn, i, j);

               clientGridArray[i][j] = btn;
           }

        } 

    }

    
    //another simple popup alert that prints out the exception trace
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

        alert.initModality(Modality.WINDOW_MODAL);
        alert.showAndWait();
    }

    //marks the client grid buttons based on the selected button
    //if mark is true it changes the pseudoclass to true and if it is false it changes it back to false
    private void clientGridButtonHoverMark(SeaBattleButton hoveredBtn, int rowLimit, int colLimit, boolean mark)
    {
        boolean isPlaceable = true;
        //check whether the button is placeable here
        int clickedBtnRow = hoveredBtn.getCell().getRow();
        int clickedBtnCol = hoveredBtn.getCell().getCol();


        for(int i = -1; i <= rowLimit; i++)
        {
            for(int j = -1; j <= colLimit; j++)
            {
                int curCol = clickedBtnCol + j;
                int curRow = clickedBtnRow + i;

                if(curRow==-1||curCol==-1||curRow==10||curCol==10){
                    isPlaceable=true;
                }
                if( curCol >= clientGridSize || curRow >= clientGridSize ||curRow==-1||curCol==-1|| (clientGridArray[curCol][curRow].getCellType() != SeaBattleButton.CellType.EMPTY ))
                {
                    isPlaceable=false;
                }


            }
        }
        if(isPlaceable)
        {


            for(int i = 0; i < rowLimit; i++)
            {
                for(int j = 0; j < colLimit; j++)
                {
                    SeaBattleButton currentBtn =  clientGridArray[clickedBtnCol + j][clickedBtnRow + i];
                    
                    currentBtn.pseudoClassStateChanged(hoverClass, mark);
                                        
                }
            }
        }
    }
    @FXML
    private void defaultGrid(){
            Collections.shuffle(ships);
        for (Iterator <SeaBattleButton> iterator = ships.iterator(); iterator.hasNext(); ) {
            SeaBattleButton selectedFleet = iterator.next();

            h:   for (int i = 0; i < clientGridSize; i++) {
             int rand=new Random().nextInt(2);

                if (((i == 0) || (i == 3) || (i == 6)|| (i == 5) || (i == 7)|| (i == 7)) &&rand==1)continue ;
                for (int j = 0; j < clientGridSize; j++) {
                    //i = col and j = row
                    if (((j == 0) || (i == 3)) &&rand==0)continue ;
                    SeaBattleButton btn = clientGridArray[i][j];
                    if (placeShip(client_grid, clientGridArray, clientGridSize, btn, selectedFleet) && !selectedFleet.isDisabled()) {
                        selectedFleet.setDisable(true);
                        numberOfShipsPlaced++;
                      //  iterator.remove();
                        break h;
                    }

                }

            }


        }
    }
    /////// Method Declaration END//////
    
    //================================================================
    
    /////// Event Handlers Declaration //////
    

    //starts the drag and drop procedure
    private final EventHandler<MouseEvent> onFleetGridButtonDragDetected = (MouseEvent event) -> {

        SeaBattleButton btn = (SeaBattleButton) event.getSource();
        selectedFleet = btn;
        //chosenUnit_label.setText(selectedFleet.getCellType().toString());
        Dragboard db = btn.startDragAndDrop(TransferMode.ANY);
        SnapshotParameters param = new SnapshotParameters();
        param.setFill(Color.TRANSPARENT);
        db.setDragView(btn.snapshot(param, null));
        ClipboardContent content = new ClipboardContent();
        content.putString(btn.getCellType().toString());
        db.setContent(content);
        event.consume();
             
    };

    //when mouse enters a fleet button it changes its curson image to hand to indicate it's draggable
    private final EventHandler<MouseEvent> onFleetGridButtonMouseEntered = (MouseEvent e) -> {
            SeaBattleButton button = (SeaBattleButton) e.getSource();
            button.setCursor(Cursor.HAND);
        
    };


    //after the drop event places the ship if possible
    private final EventHandler<DragEvent> onClientGridButtonDragDropped = (DragEvent event) -> {
        
        SeaBattleButton btn = (SeaBattleButton) event.getSource();

        if (selectedFleet != null) {
            if (placeShip(client_grid, clientGridArray, clientGridSize, btn, selectedFleet)) {
                selectedFleet.setDisable(true);
                numberOfShipsPlaced++;

            } else {

                selectedFleet.setDisable(false);
                selectedFleet = null;
            }

            selectedFleet = null;
        }
        event.consume();
    };


    //marks the possition of the selected ship on the client grid
    private final EventHandler<DragEvent> onClientGridButtonMouseDragEntered = (DragEvent event) ->
    {
        SeaBattleButton hoveredBtn = (SeaBattleButton) event.getSource();

        if(selectedFleet != null && selectedFleet.getCellType() != SeaBattleButton.CellType.EMPTY)
        {


                clientGridButtonHoverMark(hoveredBtn, selectedFleet.getShip().getAreaRow(), selectedFleet.getShip().getAreaCol(), true);


        }else 
        {
            hoveredBtn.pseudoClassStateChanged(hoverClass, true);
        }
        event.consume();

    };


    //this method makes the button accept drag events
    private final EventHandler<DragEvent> onClientGridButtonDragOver = (DragEvent event ) -> {

        SeaBattleButton hoveredBtn = (SeaBattleButton) event.getSource();

        if (event.getGestureSource() != hoveredBtn && event.getDragboard().hasString()) {

            event.acceptTransferModes(TransferMode.ANY);
        }

        event.consume();
    };


    //on the drag exit remove the selected ship's mark from client grid
    private final EventHandler<DragEvent> onClientGridButtonMouseDragExited = (DragEvent event) ->
    {

        SeaBattleButton hoveredBtn = (SeaBattleButton) event.getSource();
        if(selectedFleet != null && selectedFleet.getCellType() != SeaBattleButton.CellType.EMPTY)
        {

                clientGridButtonHoverMark(hoveredBtn, selectedFleet.getShip().getAreaRow(), selectedFleet.getShip().getAreaCol(), false);

        }else 
        {
            hoveredBtn.pseudoClassStateChanged(hoverClass, false);
        }
        event.consume();


    };
    
    /////// Event Handlers Declaration END//////



}
