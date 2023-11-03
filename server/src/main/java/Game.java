
import java.util.ArrayList;
import java.util.Iterator;

import com.google.gson.Gson;
import db.Context;
import model.User;
import ship.*;

public class Game {

    /////// Variable Declaration //////

    private int numberOfPlayers = 0;

    private Player[] players = new Player[2];

    private long gameID = 0;
    
    private int gridSize;

    private Gson gson = new Gson();
    private Context context=new Context();

    /////// Variable Declaration END//////

    //================================================================

    Game(int gridSize) {

        this.gridSize = gridSize;
    }

    //================================================================

    public void addPlayer(Connection connection, ArrayList<Cell> board) {

        if (players[0] == null) {
            players[0] = new Player(connection, board);
            numberOfPlayers++;

        } else if (players[1] == null) {
            players[1] = new Player(connection, board);
            numberOfPlayers++;

        }
    }



    public void chat(String username, String msg)
    {
        if(msg.length() > 20)
        {
            msg = msg.substring(0, 20);
        }

        for(Player p : players)
        {
            p.getConnection().sendToClient(ASBTP.CHAT);
            p.getConnection().sendToClient(username);
            p.getConnection().sendToClient(msg);
        }
    }



    public Player changeTurn(boolean shouldChangeTurn) {
        
        //check for winner before every turn change
        checkForWinner();
        
        
        Player currentPlayerTurn = null;
        //if the turns should be changeed
        if(shouldChangeTurn)
        {

            for (Player pl : players) {
                if (pl.isTurn()) {
                    pl.setTurn(false);
                } else {
                    pl.setTurn(true);
                    currentPlayerTurn = pl;
                }
            }

        }else
        //else dont change the turn
        {
            for (Player pl : players) {
                if (pl.isTurn()) {
                    pl.setTurn(true);
                    currentPlayerTurn = pl;
                } else {
                    pl.setTurn(false);
                }
            }
        }


        calculateColdDowns();

        return currentPlayerTurn;
    }




    private void checkForWinner() {

        Player winningPlayer = null;
        Player losingPlayer = null;
        for(Player p : players)
        {
            if(p.getNumberOfShipCells() < 1)
            {
                losingPlayer = p;
                break;
            }
        }


        //determine the winning player
        if(losingPlayer != null)
        {
            if (players[0].equals(losingPlayer)) {
                winningPlayer = players[1];
            } else {
                winningPlayer = players[0];
            }
            User winner=context.Users.getByuserName(winningPlayer.getUsername());
            winner.setScore(winner.getScore()+1);
            context.Users.update(winner);
            User loser=context.Users.getByuserName(losingPlayer.getUsername());
            loser.setScore(loser.getScore()-1);
            context.Users.update(loser);

            winningPlayer.getConnection().sendToClient(ASBTP.GAME_END);
            winningPlayer.getConnection().sendToClient(ASBTP.WON);

            losingPlayer.getConnection().sendToClient(ASBTP.GAME_END);
            losingPlayer.getConnection().sendToClient(ASBTP.LOST);

        }
    }



    //closes all the connections removes the game from the game list and closes the threads
    public void endGame(Player disconnectingPlayer)
    {
        disconnectingPlayer.getConnection().getMainServer().removeGame(this);
        disconnectingPlayer.getConnection().setGame(null);

    }


    //if a connection is disconnected this method ends the game and sends a message to the other connection
    public void connectionDisconnected(Connection connection) {

        //determine the other player that hasnt disconnected yet
        Player stillConnedtedPlayer;
        Player disconnectingPlayer;
        // determine which player is moving
        if (players[0].getConnection().equals(connection)) {

            disconnectingPlayer = players[0];
            stillConnedtedPlayer = players[1];
        } else {

            disconnectingPlayer = players[1];
            stillConnedtedPlayer = players[0];
        }



        if(stillConnedtedPlayer != null)
        {
            stillConnedtedPlayer.getConnection().sendToClient(ASBTP.GAME_END);
            stillConnedtedPlayer.getConnection().sendToClient(ASBTP.OPPONENT_DISCONNECTED);
            stillConnedtedPlayer.getConnection().setGame(null);
            stillConnedtedPlayer.getConnection().closeSocket();

        }else
        {
            //there is only one player in the game and player has disconnected
            //then nothing to do here 
            // we simply end the game
        }

        endGame(disconnectingPlayer);

	}




    public void gameReadySignal() {

        countPlayersShips();
        
        //send the signal and the username of the other player
        players[0].getConnection().sendToClient(ASBTP.GAME_READY);
        players[0].getConnection().sendToClient(players[1].getUsername());

        players[1].getConnection().sendToClient(ASBTP.GAME_READY);
        players[1].getConnection().sendToClient(players[0].getUsername());
        

        // start the match by giving the first player the turn
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {

            e.printStackTrace();
        }
        players[0].setTurn(true);
        System.out.println("Game with ID: " + this.getGameID() + " started.");
    }

    private void countPlayersShips() {
        for(Player p : players)
        {
            int notDestroyedShips = 0;
            for(Cell c :p.getBoard())
            {
                if(c.getCellType() != ASBTP.EMPTY && !c.isDestroyed())
                {
                    notDestroyedShips += 1;
                }
            }
            p.setNumberOfShipCells(notDestroyedShips);

        }
    }









    //get the ship based on the ship type
    private Ship getShip(int shipType)
    {
        switch (shipType) {
            case ASBTP.SOLDIER:
                return new Soldier();
            case ASBTP.CAVALRY:
                
                return new Cavalry();
            case ASBTP.FORT:
                
            return new Fort();
            case ASBTP.HEADQUARTERS:
                
            return new HeadQuarters();
            default:
                return null;
        }
    }




    public void attack(Connection connection, Point targetCordinates) {

        Player attackerPlayer, targetPlayer;

        // determine which connection is attacking and which one is the target
        if (players[0].getConnection().equals(connection)) {
            attackerPlayer = players[0];
            targetPlayer = players[1];
        } else {
            attackerPlayer = players[1];
            targetPlayer = players[0];
        }


        Cell targetCell = targetPlayer.getCell(targetCordinates.getCol(), targetCordinates.getRow());


        ArrayList<Cell> targetCells = composeListOfAttackedCells( targetCell);


        // destroy the attacked cells
        for (Cell c : targetCells) {
            c.setDestroyed(true);
        }



        String json = gson.toJson(targetCells);
        
        attackerPlayer.getConnection().sendToClient(ASBTP.ATTACK_OK);
        attackerPlayer.getConnection().sendToClient(json);
        
        targetPlayer.getConnection().sendToClient(ASBTP.ATTACK);
        targetPlayer.getConnection().sendToClient(json);
        
        //if the attacker player has managed to destroy a ship then they wont lose their turn
        //and if they have managed to destroy a ship add money to their balance
        boolean hasDestroyedNonEmptyCell = false;
        for(Cell c : targetCells)
        {
            if(c.getCellType() != ASBTP.EMPTY)
            {
                addBalance(attackerPlayer, targetPlayer, c);
                targetPlayer.removeShipCell(1);
                hasDestroyedNonEmptyCell = true;
            }
        }

        changeTurn(!hasDestroyedNonEmptyCell);

    }


    public void attackDone(Connection connection, ArrayList<Cell>arrayList) {

        Player attackerPlayer, targetPlayer;

        // determine which connection is attacking and which one is the target
        if (players[0].getConnection().equals(connection)) {
            targetPlayer = players[0];
            attackerPlayer = players[1];
        } else {
           targetPlayer = players[1];
            attackerPlayer= players[0];
        }




        // send this list of attacked cells to both the attacker player and the target
        // player to update their boards

        String json = gson.toJson(arrayList);

        attackerPlayer.getConnection().sendToClient(ASBTP.ATTACK_DONE);
        attackerPlayer.getConnection().sendToClient(json);



    }




    private void addBalance(Player attackerPlayer, Player targetPlayer, Cell destroyedCell) {


        attackerPlayer.addBalance(5);

        //checks to see whther the whole ship has been destroyed or not
        Cell destroyedCellHeadShip = targetPlayer.getCell(destroyedCell.getHeadShipCordinates().getCol(), destroyedCell.getHeadShipCordinates().getRow());
        

        if(destroyedCellHeadShip.getBlood() > -1)
        {
            
            boolean hasDestroyedEntireShip = true;
            for(Point p : destroyedCellHeadShip.getChildShips())
            {
                Cell childCell = targetPlayer.getCell(p.getCol(), p.getRow());
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
                switch (destroyedCell.getCellType()) {
                    case ASBTP.CAVALRY:
                        attackerPlayer.addBalance(10);
                        
                        break;
                    case ASBTP.FORT:
                        attackerPlayer.addBalance(15);
                        
                        break;
                    case ASBTP.HEADQUARTERS:
                        attackerPlayer.addBalance(20);
                        
                    default:
                        break;
                }

                //just a flag not no add the bounty for this whole ship twice or more
                for(Point p : destroyedCellHeadShip.getChildShips())
                {
                    Cell childCell = targetPlayer.getCell(p.getCol(), p.getRow());
          
                    childCell.setBlood(-1);
                
                }
                destroyedCellHeadShip.setBlood(-1);

                //if the ship that has completely been destroyed is a fort then decrease the number of target player forts
                if(destroyedCellHeadShip.getCellType() == ASBTP.FORT)
                {
                    targetPlayer.addFort(-1);
                }
            }

        }


    }


    //substract one from all the non empty cells
    private void calculateColdDowns()
    {
        for(Player pl : players)
        {
            for(Cell c : pl.getBoard())
            {
                if(c.getCellType() != ASBTP.EMPTY && c.getBlood() > 0)
                {
                    c.setBlood(c.getBlood() - 1);
                }
            }
        }
    }

   





    //creates a list of attacked cells based on the attacking ships type
    private ArrayList<Cell> composeListOfAttackedCells(Cell targetCell)
    {

        ArrayList<Cell> targetCells = new ArrayList<>();
        targetCells.add(targetCell);
        //remove the cell from the list if it had already been destroyed
        Iterator<Cell> itr = targetCells.iterator();
        while(itr.hasNext())
        {
            Cell c = itr.next();
            if(c.isDestroyed())
            {
                itr.remove();
            }
        }
        
        return targetCells;
    }


    //makes sure the attack is valid
    private boolean isAttackPossbile(Player attackerPlayer, Player targetPlayer, Cell attackingCell, Cell targetCell) {

        //make sure the attack is not out of the range of the size of the grid
        return true;
    }


    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    public void setNumberOfPlayers(int numberOfPlayers) {
        this.numberOfPlayers = numberOfPlayers;
    }

    public int getGridSize() {
        return gridSize;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
    }



    public void setGameID(long gameID) {
        this.gameID = gameID;
    }

    public long getGameID() {
        return gameID;
    }

}