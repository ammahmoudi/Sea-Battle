
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import javafx.concurrent.Service;
import javafx.concurrent.Task;




public class Connection extends Service<HashMap<String, String>> {

    private String serverIP;
    private int serverPort;
    private Socket socket;

    private BufferedReader in;
    private PrintWriter out;

    //a reference to the startup controller and game controller for future callbacks
    private GameController gameController;
    private StartUpController startUpController;

    public
    Connection setSocket(Socket socket) {
        this.socket = socket;
        return this;
    }

    public
    Connection setUsername(String username) {
        this.username = username;
        return this;
    }

    public
    Connection setGridSize(int gridSize) {
        this.gridSize = gridSize;
        return this;
    }

    public
    Connection setBoardJson(String boardJson) {
        this.boardJson = boardJson;
        return this;
    }

    public
    AuthController getAuthController() {
        return authController;
    }

    public
    StartUpController getStartUpController() {
        return startUpController;
    }

    public
    Connection setStartUpController(StartUpController startUpController) {
        this.startUpController = startUpController;
        return this;
    }

    public
    Connection setAuthController(AuthController authController) {
        this.authController = authController;
        return this;
    }

    private AuthController authController;

    private String username;
    private int gridSize;
    private String boardJson;

    public
    Integer getAuthCode() {
        return authCode;
    }

    public
    Connection setAuthCode(Integer authCode) {
        this.authCode = authCode;
        return this;
    }

    private Integer authCode=null;

    Connection(String ip, int port, String username, int gridSize, String boardJson, StartUpController startUpController,Integer authCode) {

        this.username = username;
        this.serverIP = ip;
        this.serverPort = port;
        this.gridSize = gridSize;
        this.boardJson = boardJson;
        this.startUpController = startUpController;
        this.authCode=authCode;
    }
    Connection(String ip, int port, AuthController authController) {


        this.serverIP = ip;
        this.serverPort = port;
        this.authController=authController;

    }



    @Override
    protected Task<HashMap<String, String>> createTask() {

        return new Task<HashMap<String, String>>() {

            @Override
            protected HashMap<String, String> call() throws Exception {

                HashMap<String, String> msg = new HashMap<>();
                try
                {


                    socket = new Socket(serverIP, serverPort);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = new PrintWriter(socket.getOutputStream(), true);


                    String responseCode = "";
                    String body = "";


                    msg.put(ASBTP.ACTION_CODE, responseCode);
                    msg.put(ASBTP.BODY, body);

                    while ((responseCode = in.readLine()) != null) {
                        switch (responseCode) {

                            case ASBTP.GAME_READY:
                                Thread.sleep(1000);
                                msg.put(ASBTP.ACTION_CODE, ASBTP.GAME_READY);
                                body = in.readLine();
                                msg.put(ASBTP.BODY, body);
                                startUpController.handleServerMessage(msg);
                                break;

                            case ASBTP.CLIENT_TURN: {
                                msg.put(ASBTP.ACTION_CODE, ASBTP.CLIENT_TURN);
                                body = in.readLine();
                                msg.put(ASBTP.BODY, body);
                                gameController.handleServerMessage(msg);
                                break;
                            }

                            case ASBTP.ATTACK: {
                                msg.put(ASBTP.ACTION_CODE, ASBTP.ATTACK);
                                body = in.readLine();
                                msg.put(ASBTP.BODY, body);
                                gameController.handleServerMessage(msg);
                                break;
                            }

                            case ASBTP.ATTACK_OK: {
                                msg.put(ASBTP.ACTION_CODE, ASBTP.ATTACK_OK);
                                body = in.readLine();
                                msg.put(ASBTP.BODY, body);
                                gameController.handleServerMessage(msg);
                                break;
                            }
                            case ASBTP.ATTACK_DONE: {
                                msg.put(ASBTP.ACTION_CODE, ASBTP.ATTACK_DONE);
                                body = in.readLine();
                                msg.put(ASBTP.BODY, body);
                                gameController.handleServerMessage(msg);
                                break;
                            }


                            case ASBTP.BALANCE: {
                                msg.put(ASBTP.ACTION_CODE, ASBTP.BALANCE);
                                body = in.readLine();
                                msg.put(ASBTP.BODY, body);
                                gameController.handleServerMessage(msg);
                                break;
                            }
                            case ASBTP.SIGN_IN_OK: {
                                msg.put(ASBTP.ACTION_CODE, ASBTP.SIGN_IN_OK);
                                body = in.readLine();
                                msg.put(ASBTP.BODY, body);
                                authController.handleServerMessage(msg);
                                break;
                            }


                            case ASBTP.GAME_END: {
                                msg.put(ASBTP.ACTION_CODE, ASBTP.GAME_END);
                                body = in.readLine();
                                msg.put(ASBTP.BODY, body);
                                gameController.handleServerMessage(msg);
                                break;
                            }

                            case ASBTP.ERROR_AUTH: {
                                msg.put(ASBTP.ACTION_CODE, ASBTP.ERROR_AUTH);
                                body = in.readLine();
                                msg.put(ASBTP.BODY, body);
                                authController.handleServerMessage(msg);
                                System.out.println(msg.toString());
                                break;
                            }

                            case ASBTP.CHAT: {
                                msg.put(ASBTP.ACTION_CODE, ASBTP.CHAT);
                                body = in.readLine();
                                body += System.getProperty("line.separator");
                                body += in.readLine();
                                msg.put(ASBTP.BODY, body);
                                gameController.handleServerMessage(msg);

                                break;
                            }
                            case ASBTP.SCORES: {
                                msg.put(ASBTP.ACTION_CODE, ASBTP.CHAT);
                                body = in.readLine();
                                msg.put(ASBTP.BODY, body);
                             authController.handleServerMessage(msg);

                                break;
                            }

                            default:
                                break;
                        }

                        System.out.println("Code received form server: " + responseCode);
                         System.out.println("Body received form server: " + body);
                        if (isCancelled()) {
                            break;
                        }

                    }

                } catch (Exception e) {

                    msg.put(ASBTP.ACTION_CODE, ASBTP.GAME_END);
                    msg.put(ASBTP.BODY, ASBTP.CLIENT_DISCONNECTED);
                    gameController.handleServerMessage(msg);
                    closeSocket();
                    return null;

                }
                // if the server shuts down the game ends
                msg.put(ASBTP.ACTION_CODE, ASBTP.GAME_END);
                msg.put(ASBTP.BODY, ASBTP.CLIENT_DISCONNECTED);
                gameController.handleServerMessage(msg);
                closeSocket();
                return null;
            }

        };

    }

    public void closeSocket() {

        try {
            if(this.socket != null && !this.socket.isClosed())
            {
                this.socket.close();
                this.cancel();
            }
        } catch (IOException e) {

            System.out.println("Something went wrong while closing the socket");
        }
    }


    public String getUsername() {
        return username;
    }


    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }


    public String getServerIP() {
        return serverIP;
    }


    public int getServerPort() {
        return serverPort;
    }


    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }


    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
public void startGame(){
    sendToServer(ASBTP.JOIN);
   sendToServer(username);
    sendToServer(String.valueOf(gridSize));
    sendToServer(boardJson);
}
    public void sendToServer(String msg) {
        out.println(msg);
    }

    public void disconnectFromServer() {
        try
        {
            if(this.socket != null && !this.socket.isClosed())
            {
                this.socket.close();
            }
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

}