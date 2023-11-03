
import java.util.ArrayList;

public class Player {


    //================================================================
    /////// Variable Declaration //////

    private Connection connection;

    private int gridSize;

    private int numberOfForts = 2;

    private String username;

    private ArrayList<Cell> board;

    private boolean turn = false;

    private int balance = 0;

    //if this reaches to zero; the player loses the game
    private int numberOfShipCells;

    /////// Variable Declaration END//////
    //================================================================


    Player(Connection connection, ArrayList<Cell> board)
    {
        this.connection = connection;
        this.gridSize = connection.getGridSize();
        this.board = board;
        this.username = connection.getUsername();
    }

    //================================================================

    //after each call send the state of turn to the client
    public void setTurn(boolean turn) {
        this.turn = turn;
        connection.sendToClient(ASBTP.CLIENT_TURN);
        if(turn)
        {
            connection.sendToClient(ASBTP.TRUE);
        } else
        {
            connection.sendToClient(ASBTP.FALSE);
        }

        //send the balance to the player
        connection.sendToClient(ASBTP.BALANCE);
        connection.sendToClient(Integer.toString(this.balance));
    }

    public void setNumberOfShipCells(int numberOfShipCells) {
        this.numberOfShipCells = numberOfShipCells;
    }

    public void removeShipCell(int n)
    {
        this.numberOfShipCells -= n;
    }

    public void addShipCell(int n)
    {
        this.numberOfShipCells += n;
    }

    public int getNumberOfShipCells() {
        return numberOfShipCells;
    }

    


    public int getBalance() {
        return balance;
    }

    public void addBalance(int balance) {
        this.balance += balance;
    }

    public boolean isTurn() {
        return turn;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setBoard(ArrayList<Cell> board) {
        this.board = board;
    }
    public void setConnection(Connection connection) {
        this.connection = connection;
    }
    public ArrayList<Cell> getBoard() {
        return board;
    }
    public Connection getConnection() {
        return connection;
    }

    public Cell getCell(int col, int row)
    {
        return this.board.get( col * gridSize + row);
    }

	public void addFort(int i) {
        this.numberOfForts += i;
    }

    public int getNumberOfForts() {
        return numberOfForts;
    }
}