package logic;

/* Wrapper for the data which is sent to the API.*/
public class BotData {

    public int[] marketShares;
    public int[] playerShares;
    public int money;
    public Card[] cards;

    public BotData(int[] marketShares, int[] playerShares, int money, Card[] cards){
        this.marketShares = marketShares;
        this.playerShares = playerShares;
        this.money = money;
        this.cards = cards;
    }

    public BotData(){}

}
