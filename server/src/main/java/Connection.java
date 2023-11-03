
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import db.Context;
import model.User;

public class Connection extends Thread {

    //================================================================

    /////// Variable Declaration //////

    public static int MAX_PLAYERS = 2;

    private Socket socket;
    private SeaBattleServer mainServer;

    private int gridSize;
    private String username;
    
    Gson gson = new Gson();

    // the game this connection is associated with
    private Game game;

    private BufferedReader in;
    private PrintWriter out;
    private Context context=new Context();


    /////// Variable Declaration END //////




    Connection(SeaBattleServer mainServer, Socket socket) {

        this.socket = socket;
        this.mainServer = mainServer;

    }




    @Override
    public void run() {


        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            String input;


            // terminates if input == null (client disconnects)
            while ((input = in.readLine()) != null) {

                
                if (input.matches("^[A-Z1-9]{1,20}$")) {
                    switch (input) {
                        case ASBTP.SCORES:{
                            sendToClient(ASBTP.SCORES);
                           sendToClient(gson.toJson(context.Users.all()));
                            break;}

                        case ASBTP.SIGN_UP:{
                            input=in.readLine();

                            ArrayList<String> signUpPocket = gson.fromJson(input, new TypeToken<ArrayList<String>>(){}.getType());
                            if(signUpPocket.get(0).isBlank()||signUpPocket.get(1).isBlank()){

                                sendToClient(ASBTP.ERROR_AUTH);
                                sendToClient("Please Fill the fields!");
                                System.out.println("fill");

                            }else {
                                System.out.println("searching user");
                                User user=null;
                                try {
                                   user = context.Users.getByuserName(signUpPocket.get(0));
                                }catch (Exception e){
                                    System.out.println(e.toString());
                                }

                                if (user == null) {
                                    System.out.println(signUpPocket.get(0));
                                    user = new User(signUpPocket.get(0), signUpPocket.get(1));
                                    context.Users.add(user);
                                    sendToClient(ASBTP.SIGN_IN_OK);
                                    sendToClient(gson.toJson(signUpPocket));
                                } else {
                                    sendToClient(ASBTP.ERROR_AUTH);
                                    sendToClient("This Username is Already Taken!");
                                    System.out.println("taken");
                                }
                            }
                            break;}
                        case ASBTP.SIGN_IN:{
                            input=in.readLine();

                            ArrayList<String> signInPocket = gson.fromJson(input, new TypeToken<ArrayList<String>>(){}.getType());
                            if(signInPocket.get(0).isBlank()||signInPocket.get(1).isBlank()){

                                sendToClient(ASBTP.ERROR_AUTH);
                                sendToClient("Please Fill the fields!");
                                System.out.println("fill");

                            }else {
                                System.out.println("searching user");
                                User user=null;
                                try {
                                    user = context.Users.getByuserName(signInPocket.get(0));
                                }catch (Exception e){
                                    System.out.println(e.toString());
                                }
                                //System.out.println("useris"+user.getUsername());
                                if (user != null) {
                                    System.out.println(user.getPassword());

                                    if (user.getPassword().equals(signInPocket.get(1))) {

                                        SecureRandom random = new SecureRandom();
                                        int authToken = random.nextInt(1399);
                                        signInPocket.add(String.valueOf(authToken));
                                        sendToClient(ASBTP.SIGN_IN_OK);
                                        sendToClient(gson.toJson(signInPocket));
                                    } else {
                                        sendToClient(ASBTP.ERROR_AUTH);
                                        sendToClient("Invalid  password!");
                                        System.out.println("invalidp");
                                    }
                                } else {
                                    sendToClient(ASBTP.ERROR_AUTH);
                                    sendToClient("Invalid Username!");
                                    System.out.println("invalid u");
                                }
                            }
                            break;}
                        // join
                        case ASBTP.JOIN:

                            // read the user name
                            input = in.readLine();
                            this.username = input;
                            System.out.println("Player: " + input + " has connected.");

                            // read the gridsize
                            input = in.readLine();
                            this.gridSize = Integer.parseInt(input);

                            // read the board in json
                            input = in.readLine();
                            // convert the received json board to an array of cells;

                            // parse json
                            ArrayList<Cell> joinBoard = jsonToBoard(input);


                            // signal to both players
                            this.game = joinOrCreateGame(joinBoard);
                            break;

                        case ASBTP.ATTACK:


                            input = in.readLine();
                            Point targetCordinates = gson.fromJson(input, Point.class);


                            game.attack(this,targetCordinates);
                            break;
                        case ASBTP.ATTACK_DONE:
                             input = in.readLine();
                            Type collectionType = new TypeToken<ArrayList<Cell>>() {}.getType();

                            //parse the json
                            ArrayList<Cell> secondHandDestroyedCells = gson.fromJson(input, collectionType);


                            game.attackDone(this,secondHandDestroyedCells);
                            break;

                        case ASBTP.LOSE_TURN:
                        {
                            game.changeTurn(true);
                            break;
                        }


                        case ASBTP.CHAT:
                        {
                            input = in.readLine();

                            game.chat(this.username, input);
                            break;
                        }

                        

                        default:

                            break;

                    }

                } else {
                    sendToClient(ASBTP.ERROR_AUTH);
                    sendToClient("heh");
                }

            }

        } catch (Exception e) {


            closeSocket();


            return;
        }

        
        closeSocket();

        return;

    }



    public void closeSocket() {

        try {
            if(this.socket != null && !this.socket.isClosed())
            {  
                System.out.println("Player: " + this.username + " has disconnected");
                if (game != null) {
                    game.connectionDisconnected(this);
                }
                this.socket.close();                
            }
        } catch (IOException e) {

            System.out.println("Something went wrong while closing the socket");
        }
    }
    public synchronized void sendToClient(String msg)
    {
        out.println(msg);
    }


    ArrayList<Cell> jsonToBoard(String json)
    {
        Type collectionType = new TypeToken<ArrayList<Cell>>(){}.getType();
        ArrayList<Cell> board = gson.fromJson(json, collectionType);
        return board;
    }


    //creates or joins a game
    Game joinOrCreateGame(ArrayList<Cell> board)
    {
        Game g = mainServer.searchQueuedGames(this.gridSize);

        if(g != null)
        {
            g.addPlayer(this, board);
            System.out.println("Player: " + this.username + " joined game with ID: " + g.getGameID());
            
            //if the game has reached two players simply remove it from the queued games
            if(g.getNumberOfPlayers() >= MAX_PLAYERS)
            {
                //sends a signal to all the players in the game that the game is about to start
                g.gameReadySignal();
                mainServer.removeQueuedGame(g);
            }
            return g;

        }

        //if no games with this gridsize were found create a new game
        Game newGame = new Game(this.gridSize);

        newGame.setGridSize(this.gridSize);
        newGame.addPlayer(this, board);
        newGame.setGameID(this.getId());
        this.game = newGame;
        mainServer.addQueuedGame(newGame);

        System.out.println("Game with ID: " + newGame.getGameID() + " created.");
        System.out.println("Player: " + this.username + " joined game with ID: " + newGame.getGameID());


        return newGame;
        
    }


    public SeaBattleServer getMainServer() {
        return mainServer;
    }

    public int getGridSize() {
        return gridSize;
    }
    public String getUsername() {
        return username;
    }


    public void setGame(Game game) {
        this.game = game;
    }

   
}