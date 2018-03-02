package logic;

import org.glassfish.jersey.client.ClientConfig;
import socket.Request;
import webserver.GsonMessageBodyHandler;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.util.*;

import static socket.GameService.amountOfBots;
import static socket.GameService.round;

public class Game {

	public static final int companies = Stock.values().length;
	public static final int transactionFee = 3;
	public static int endedTurn = 0;

	private static ArrayList<Player> players;
	public static ArrayList<Deck> decks;
	public static int[] marketShares;
	public static ArrayList<Vote> allVotes;

	// create a random game 
	public Game() {
		players = new ArrayList<>();
		decks = new ArrayList<>();
		allVotes = new ArrayList<>();
		marketShares = new int[]{100, 100, 100, 100, 100};
		distributeCards();
	}

	// used for unit testing 
	public Game(Deck[] dec, int[][] shares) {
		players = new ArrayList<>();
		decks = new ArrayList<>();
		marketShares = new int[]{100, 100, 100, 100, 100};

		decks.addAll(Arrays.asList(dec));
		for(int i = 0; i < shares.length; i++){
			Player newPlayer = new Player(false);
			newPlayer.setShares(shares[i]);
			players.add(newPlayer);
		}
		allVotes = new ArrayList<>();
	}

	public void addPlayer(boolean isBot){
		players.add(new Player(isBot));
	}

	private void distributeCards(){
		for(Stock s : Stock.values()){
			decks.add(new Deck(s));
		}
	}

	public String getSharePrices(){
		StringBuilder result = new StringBuilder();
		for(Stock s: Stock.values()){
			result.append(" [").append(s).append(" - ").append(marketShares[s.ordinal()]).append("]");
		}
		return result.toString();
	}

	public int getCash(int playerId) {
		return players.get(playerId).getMoney();
	}

	public int[] getShares(int playerId) {  return players.get(playerId).getShares(); }

	public int sellShares(int playerid) { return players.get(playerid).sellShares(); }

	public String returnPlayerInfo(int playerid){
		return players.get(playerid).toString();
	}

	public int[] getPrices() {
		return marketShares;
	}

	public Card[] getCards() {
		Card[] topCards = new Card[companies];
		for(int i = 0; i < companies; i++){
			topCards[i] = decks.get(i).cards.get(0);
		}
		return topCards;
	}

	public String getCardsString(){
		StringBuilder result = new StringBuilder();
		for(Stock s: Stock.values()){
			result.append(" [").append(s).append(" - ").append(Arrays.asList(getCards()).get(s.ordinal())).append("]");
		}
		return result.toString();
	}

	public synchronized String buy(int id, Stock s, int amount)
	{
		int sum =  marketShares[s.ordinal()] * amount + (amount * transactionFee);
		if(players.get(id).getMoney() - sum >= 0 && players.get(id).getMadeTransactions() < 2){
			players.get(id).buyShare(s.ordinal(), amount);
			return "You successfully bought " + amount + " of " + s + " shares. Your new balance " + getCash(id);
		}
		return "Failed to buy";
	}

	public synchronized String sell(int id, Stock s, int amount) {
		if(players.get(id).getShares()[s.ordinal()] >= amount && players.get(id).getMadeTransactions() < 2){
			players.get(id).sellShare(s.ordinal(), amount);
			return "You successfully sold " + amount + " of " + s + " shares. Your new balance " + getCash(id);
		}
		return "Failed to sell";
	}

	public synchronized String vote(int id, Stock s, boolean yes) {
		if(players.get(id).getVotes().isEmpty()){
			players.get(id).addVote(new Vote(id, s, yes));
			return "You have successfully voted for " + s + ", decision - " + yes;
		}
		else if(players.get(id).getVotes().size() == 1){
			if(players.get(id).getVotes().get(0).getStock().ordinal() != s.ordinal()){
				players.get(id).addVote(new Vote(id, s, yes));
				return "You have successfully voted for " + s + ", decision - " + yes;
			} else {
				return "You have already voted for this card";
			}
		}
		return "You have voted twice already. Your votes " + players.get(id).getVotes().toString();
	}

	public void executeVotes() {
		for(Player p : players){ allVotes.addAll(p.getVotes()); }
		Map<Stock, Integer> results = new HashMap<>();
		// count votes
		for(Vote v : allVotes){
			int currentVal;
			if(results.containsKey(v.getStock())) {
				currentVal = results.get(v.getStock());
				if(v.isYes()) {
					currentVal++;
				} else {
					currentVal--;
				}
				results.put(v.getStock(), currentVal);
			} else {
				if(v.isYes()) {
					currentVal = 1;
				} else {
					currentVal = -1;
				}
				results.put(v.getStock(), currentVal);
			}
		}
		// execute votes
		Card[] currentShares = getCards();
		for(Map.Entry<Stock, Integer> entry : results.entrySet()){
			if(entry.getValue() > 0){
				marketShares[entry.getKey().ordinal()] += currentShares[entry.getKey().ordinal()].effect;
				decks.get(entry.getKey().ordinal()).cards.remove(0);
			} else if (entry.getValue() < 0) {
				decks.get(entry.getKey().ordinal()).cards.remove(0);
			}
		}
		allVotes = new ArrayList<>();
	}

	public boolean playerHasEndedTurn(int id){
		return players.get(id).isEndTurn();
	}

	public synchronized String endTurn(int playerid){
		if(!players.get(playerid).isEndTurn()) {
			players.get(playerid).setEndTurn(true);
			endedTurn++;
		}
		if(endedTurn == players.size() - amountOfBots) {
			botMove(); // get a response from bots
			executeVotes(); // execute cards, null votes of players

			//null values
			for(Player player: players){
				player.setMadeTransactions(0);
				player.setVotes(new ArrayList<>());
				player.setEndTurn(false);
			}
			return "Round has ended.";
		}
		return "You have ended your turn. Waiting for other players... ["+endedTurn+"/"+players.size()+"]";
	}

	public boolean ifPlayerIsBot(int id){
		return players.get(id).isBot();
	}

	public synchronized void botMove(){
		System.out.println("ROUND " + round);
		System.out.println("Prices: " + getSharePrices());
		System.out.println("Cards: " + getCardsString());
		for(int i = 0; i < players.size(); i++){
			if(players.get(i).isBot()){
				botAction(i);
			}
		}
	}

	/* Connects to WEB API, sends java class converted to JSON, and waits for answer (bots commands).*/
	public synchronized void botAction(int id){
        ClientConfig config = new ClientConfig(GsonMessageBodyHandler.class);
		WebTarget target = ClientBuilder.newClient(config).target("http://localhost:8080/market/api/bot");
        String post = target.request().post(Entity.entity(new BotData(marketShares, players.get(id).getShares(), players.get(id).getMoney(), getCards()), MediaType.APPLICATION_JSON))
                .readEntity(String.class);
        String[] commands = post.split("\n");
		System.out.println("BOT " + id + ":" + players.get(id).toString());
		for(String s : commands){
			Request request = Request.parse(s);
			try{
			switch (request.type){
				case BUY:
					System.out.println("BOT " + id + " : " + s);
					buy(id, Stock.parse(request.params[0]), Integer.parseInt(request.params[1]));
					break;
				case SELL:
					System.out.println("BOT " + id + " : " + s);
					sell(id, Stock.parse(request.params[0]), Integer.parseInt(request.params[1]));
					break;
				case VOTE:
					System.out.println("BOT " + id + " : " + s);
					vote(id, Stock.parse(request.params[0]), Boolean.parseBoolean(request.params[1]));
					break;
					default:
			}} catch (Exception e){
				e.printStackTrace();
			}
		}
    }
}
