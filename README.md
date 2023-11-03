# Sea Battle

Sea Battle is a two-player strategy game where each player tries to sink the enemy's ships by bombing their grid. The game is implemented in Java, JavaFX, and Hibernate ORM, using sockets for network communication. It was a project for the Advanced Programming course with Java at Sharif University of Technology. the code may have bugs and some features may have not been implemented completely.

![main gameplay](/images/main.png)

## Game Logic

The game is played on two 10x10 grids, one for each player. Each player has a fleet of ships of different sizes and shapes: one battleship (4x1), two cruisers (3x1), three destroyers (2x1), and four frigates (1x1). The ships are placed randomly on the grid by the server, with no adjacent ships. Each player can request a new placement up to three times. Each player has 30 seconds to check their placement and request a new one if needed. There is also a button to declare readiness for the game. The game starts when both players are ready.

![waiting](/images/waiting.png)

The game is turn-based. Each player has 25 seconds to bomb a cell on the enemy's grid that has not been bombed before. If the bomb hits a ship, the player gets another turn. A ship is sunk when all its cells are bombed. In that case, all the adjacent cells are also bombed. The player who sinks all the enemy's ships first wins the game.

![main gameplay](/images/ending.png)


## Game Structure

The game has a client-server architecture. The server manages the network connections and the user accounts. The client connects to the server automatically and provides a user interface for creating or logging into an account. The account information includes the username, password, number of wins, number of losses, and score. The score is the difference between the number of wins and losses.

![server logs](/images/server.png)

The game menu has options for starting a new game, watching other games, viewing the score table, and viewing the user information. The server matches the players who request a new game and assigns them randomly as the first or second player. The server also notifies the players of their turn and the game result. The game screen shows the grids of both players (without revealing the enemy's ships) and the current turn.

The game supports multiple concurrent games. The players can watch other games in progress and see some details of the game, such as the number of moves, the number of sunk or remaining ships, and the number of bombs that hit the ships. The score table shows the online or offline status and the score of each user in descending order. The user information shows the username, number of wins, number of losses, and score of the current user.

![both gameplay](/images/playing.png)

## Data Persistence

The server saves the information of each account in a database using Hibernate ORM. The information is preserved after the user logs out and can be retrieved when the user logs in again(config your Postgresql info in hibernate.xml file on the server resources folder).

![login](/images/login.png)

## Error Handling

The game handles errors in different situations. If a player disconnects during a game, the server gives them one minute to reconnect and resume the game. The other player is informed of the disconnection and the waiting time. If the disconnected player does not reconnect within one minute, the other player is declared the winner. The client also shows an appropriate message when disconnected from the server and tries to reconnect. If the client is closed completely, the user has to log in again to resume the game. The game also handles errors related to creating or logging into an account, such as duplicate usernames or wrong passwords, and shows an appropriate error message.

## Assets
 Most of the graphical assets of this game are borrowed from the original game, [SeaBattle2](https://play.google.com/store/apps/details?id=com.byril.seabattle2&hl=en&gl=US) and All rights reserved to [Byril](https://byril.com).