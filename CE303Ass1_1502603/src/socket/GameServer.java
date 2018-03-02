package socket;

import logic.Game;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/* Create an instance of game. And ServerSocket server
* which accepts connections. Once the connection is
* established GameService is created, which controls
* the game state, controls socket input/output.*/
public class GameServer {

    public static final int PORT = 8888;

    public static void main(String[] args) throws IOException {
        Game game = new Game();
        ServerSocket server = new ServerSocket(PORT);
        System.out.println("Server started at port " + PORT + ", waiting for clients...");

        while (true){
            Socket socket = server.accept();
            System.out.println("Client connected");
            GameService service = new GameService(game, socket);
            new Thread(service).start();
        }
    }
}
