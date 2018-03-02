package bot;

import logic.BotData;
import logic.Card;
import logic.Stock;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/bot")
public class BotServer {

    int[] marketShares;
    int[] playerShares;
    Card[] cards;
    int money;

    public BotServer() {
    }

    /* Receives POST requests - json data.
    * GsonMessageBodyHandler helps to convert it
    * to java objects and perfom actions with it*/
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String getData(BotData data) {
        // Retrieving data
        this.marketShares = data.marketShares;
        this.playerShares = data.playerShares;
        this.cards = data.cards;
        this.money = data.money;

        // Logic of the bot
        String command = "";
        command += sellTheStockWithLowestValue();
        command += buyTheStockWithHighestValue();

        // Sending commands to the server
        return command;
    }

    /* Sells shares with the lowest amount and after selling them,
    votes true for cards with negative cards,
    *  and false for positive effects */
    private String sellTheStockWithLowestValue() {
        String sell = "";
        sell += "SELL " + Stock.values()[indexOfSmallestValue(playerShares)] + " 1\n"; // sell share with the least amount and vote yes for reducing it price in next round
        if (cards[indexOfSmallestValue(playerShares)].effect < 0) {
            sell += "VOTE " + Stock.values()[indexOfSmallestValue(playerShares)] + " true\n";
        } else {
            sell += "VOTE " + Stock.values()[indexOfSmallestValue(playerShares)] + " false\n";
        }
        return sell;
    }

    /* Buys shares with highest card on the table.
    * If highest card was not found, meaning that player does
    * not have any shares for it, he is voting for the cards
    * for which he has a biggest amount of shares. True if
    * it is positive effect for his shares and false if negative*/
    private String buyTheStockWithHighestValue() {
        String buy = "";
        int highestCard = indexOfHighestCard(cards);
        if (highestCard != -1) {
            if (money > 200) {
                int amount = money / marketShares[highestCard];
                buy += "BUY " + Stock.values()[highestCard] + " " + amount + "\n";
            } else if (money > marketShares[highestCard]) { // if have enough money
                buy += "BUY " + Stock.values()[highestCard] + " 1\n"; // buy share with biggest card and vote for it
            }
        }

        if (cards[indexOfHighestValue(playerShares)].effect < 0) { // if the card for the biggest player's amount of shares is negative vote false, and true if positive
            buy += "VOTE " + Stock.values()[indexOfHighestValue(playerShares)] + " false\n";
        } else {
            buy += "VOTE " + Stock.values()[indexOfHighestValue(playerShares)] + " true\n";
        }
        return buy;
    }

    private int indexOfSmallestValue(int[] values) {
        int smallest = 0;
        for (int i = 1; i < values.length; i++) {
            if (values[i] < values[smallest] && values[i] != 0) {
                smallest = i;
            }
        }
        return smallest;
    }

    private int indexOfHighestValue(int[] values) {
        int highest = 0;
        for (int i = 1; i < values.length; i++) {
            if (values[i] > values[highest]) {
                highest = i;
            }
        }
        return highest;
    }

    private int indexOfHighestCard(Card[] cards) {
        int highest = 0;
        for (int i = 0; i < cards.length; i++) {
            if (cards[i].effect > cards[highest].effect) {
                if (indexOfHighestValue(playerShares) == i && cards[i].effect > 0) { // if player has most shares for that stock and effect is positive take that card
                    return i;
                }
                if (playerShares[i] > 1 && cards[i].effect > 0) { // if player has shares for that card
                    highest = i;
                }
            }
        }
        if (cards[highest].effect < 0) {
            return -1;
        }
        return highest;
    }

}