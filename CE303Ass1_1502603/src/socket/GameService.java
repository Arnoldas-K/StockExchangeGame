package socket;

import logic.Game;
import logic.Stock;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static logic.Game.endedTurn;

public class GameService implements Runnable {
    private Scanner in;
    private PrintWriter out;
    private int playerid;
    private boolean joined;
    private Game game;
    private static int lobbySize = 0;
    private static int activePlayers = 0;
    public static int amountOfBots = 0;

    public static int round = 1;
    private static ArrayList<PrintWriter> serverOutput = new ArrayList<>();

    /* Constructor for the GameService which is used
     * by clients. */
    public GameService(Game game, Socket socket) {
        this.game = game;
        playerid = -1;
        joined = false;
        try {
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);
            serverOutput.add(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* Constructor used for the bots.*/
    public GameService(Game game) { // constructor for bot
        this.game = game;
        playerid = activePlayers;
        joined = true;
        game.addPlayer(true);
        activePlayers++;
        serverOutput.add(null);
    }

    /* Control of the users input/output. Controlling
    * the state of the game.*/
    @Override
    public void run() {
        if (this.playerid == -1) {
            join();
            while (joined) {
                try {
                    Request request = Request.parse(in.nextLine());
                    if (activePlayers == lobbySize) {
                        if (!game.playerHasEndedTurn(playerid)) {
                            String response = execute(game, request);
                            out.println(response + "\r\n");
                            if (endedTurn == lobbySize - amountOfBots) {
                                endedTurn = 0;
                                startNextRound();
                            }
                        } else {
                            out.println("Waiting for other players to end the turn - " + endedTurn + "/" + lobbySize);
                        }
                    } else {
                        out.println("Players are still connecting.");
                    }
                } catch (NoSuchElementException e) {
                    joined = false;
                }
            }
            disconnect();
        }
    }

    /* First joined player made as an administrator. Lobby size as well as amount of bots
    * are decided. If there is space, players are connected.*/
    private void join() {
        if (activePlayers == 0) {
            out.println("You are lobby admin.");
            out.println("How many players will play?");
            String input = in.nextLine().trim();
            try {
                lobbySize = Integer.parseInt(input);
                if (lobbySize >= 1 && lobbySize <= 4) {
                    joinGame();
                    out.println("You successfully joined.");
                    if(lobbySize > 1) {
                        out.println("Do you want to add bots to the game? Enter the amount between " + (activePlayers) + " and " + (lobbySize-1) + ".");
                        String botAmount = in.nextLine().trim();
                        try {
                            amountOfBots = Integer.parseInt(botAmount);
                            for (int i = 0; i < amountOfBots; i++) {
                                GameService service = new GameService(this.game); // Add bot
                                new Thread(service).start();
                            }

                        } catch (Exception e) {
                            out.println("Incorrect input.");
                        }
                    }
                }
            } catch (Exception e) {
                out.println("Wrong input. Lobby size 1 - 4");
            }
        } else if (activePlayers < 4) {
            joinGame();
            printToUser("You successfully joined.", this.playerid);
        } else {
            printToUser("Lobby is full", this.playerid);
            joined = false;
        }

        if (activePlayers < lobbySize) {
            printToAll("Waiting for other players.. " + activePlayers + "/" + lobbySize);
        } else if (activePlayers == lobbySize) { // If the last player joined
            printToAll("Starting the game...");
            printToAll("Commands:");
            printToAll("To buy shares enter command 'BUY', first letter of stock name and amount ex. - 'BUY A 4'.");
            printToAll("To sell shares enter command 'SELL', first letter of stock name and amount ex. - 'SELL C 2'.");
            printToAll("To vote for cards enter command 'VOTE', first letter of stock name and 'true' - to activate card or 'false' to remove card ex. - 'VOTE E TRUE'.");
            printToAll("End the turn with command 'END'. After all players have ended their turn, votes are counted and executed, next round started.\n");
            printRoundInfo();
        }
    }

    private void joinGame() {
        joined = true;
        this.playerid = activePlayers;
        activePlayers++;
        game.addPlayer(false);
    }

    public void printToAll(String message) {
        for (int i = 0; i < serverOutput.size(); i++) {
            if (!game.ifPlayerIsBot(i)) {
                serverOutput.get(i).println(message);
            }
        }
    }

    private void printToUser(String message, int id) {
        if (!game.ifPlayerIsBot(id)) {
            out.println(message);
        }
    }

    /* Closing input/output socket to disconnect the user. */
    private void disconnect() {
        try {
            out.println("You have been disconnected.");
            Thread.sleep(2000);
            in.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Receive a game instance and request (converted user command) */
    public String execute(Game game, Request request) {
        try {
            switch (request.type) {
                case BUY:
                    Stock buyStock = Stock.parse(request.params[0]);
                    int buyAmount = Integer.parseInt(request.params[1]);
                    return game.buy(playerid, buyStock, buyAmount);
                case SELL:
                    Stock sellStock = Stock.parse(request.params[0]);
                    int sellAmount = Integer.parseInt(request.params[1]);
                    return game.sell(playerid, sellStock, sellAmount);
                case BALANCE:
                    return String.valueOf(game.getCash(playerid));
                case VOTE:
                    Stock voteStock = Stock.parse(request.params[0]);
                    boolean vote = Boolean.parseBoolean(request.params[1]);
                    return game.vote(playerid, voteStock, vote);
                case END:
                    return game.endTurn(playerid);
                case INVALID:
                    return "Invalid command, please use correct format.";
                default:
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /* Rounds management. Prints round info, if game is over
     * game results. */
    public void startNextRound() {
        round++;
        if (round <= 5) {
            printRoundInfo();
        } else {
            endTheGame();
        }
    }

    private void endTheGame() {
        printToAll("End of the game.");
        int winner = 0;
        if (activePlayers > 1) { // find the winner
            for (int i = 0; i < activePlayers; i++) {
                if (game.sellShares(i) > game.sellShares(winner)) {
                    winner = i;
                }
            }
        }
        printToAll("Scoreboard:");
        System.out.println("Scoreboard:");
        for (int i = 0; i < activePlayers; i++) {
            printToAll(i + " player - " + game.sellShares(i));
            System.out.println(i + " player - " + game.sellShares(i));
        }
        printToAll("The winner is player - " + winner + " , with most valuable shares, worth of - " + game.sellShares(winner));
        System.out.println("The winner is player - " + winner + " , with most valuable shares, worth of - " + game.sellShares(winner));
    }

    public void printRoundInfo() {
        printToAll("\n--- Round " + round + " ---\n");
        printToAll("Prices: " + game.getSharePrices());
        printToAll("Cards: " + game.getCardsString() + "\n");
        for (int i = 0; i < activePlayers; i++) {
            if (!game.ifPlayerIsBot(i)) {
                serverOutput.get(i).println(game.returnPlayerInfo(i));
            }
        }
    }
}
