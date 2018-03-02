package logic;

import java.util.ArrayList;

import static logic.Game.companies;
import static logic.Game.marketShares;
import static logic.Game.transactionFee;

public class Player{
    private int money;
    private int[] shares = new int[companies];
    private ArrayList<Vote> votes;
    private boolean endTurn;
    private int madeTransactions;
    private boolean isBot;

    public Player(boolean isBot){
        this.money = 500;
        this.shares = distributeShares();
        this.votes = new ArrayList<>();
        this.endTurn = false;
        this.madeTransactions = 0;
        this.isBot = isBot;
    }

    public int getMoney(){
        return money;
    }

    public boolean isBot() {
        return isBot;
    }

    public void setMoney(int money){
        this.money = money;
    }

    public int[] getShares(){
        return shares;
    }

    public void buyShare(int id, int amount){
        this.madeTransactions++;
        this.shares[id] += amount;
        this.money -= marketShares[id] * amount + (amount * transactionFee);
    }

    public void sellShare(int id, int amount){
        this.madeTransactions++;
        this.shares[id] -= amount;
        this.money += marketShares[id] * amount;
    }

    public int sellShares(){ // sell all shares, for the end of the game
        int sum = 0;
        for(int i = 0; i < companies; i++){
            sum += this.shares[i] * marketShares[i];
        }
        sum += money;
        return sum;
    }

    public void setShares(int[] shares){
        this.shares = shares;
    }

    public ArrayList<Vote> getVotes() {
        return votes;
    }

    public void addVote(Vote vote){
        votes.add(vote);
    }

    public void setVotes(ArrayList<Vote> votes){
        this.votes = votes;
    }

    public int[] distributeShares() {
        int[] shares = new int[companies];
        for(int i = 0; i < 10; i++){
            shares[(int) (Math.random() * companies)]++;
        }
        return shares;
    }

    public boolean isEndTurn() {
        return endTurn;
    }

    public void setEndTurn(boolean endTurn) {
        this.endTurn = endTurn;
    }

    public int getMadeTransactions() {
        return madeTransactions;
    }

    public void setMadeTransactions(int madeTransactions) {
        this.madeTransactions = madeTransactions;
    }

    @Override
    public String toString() {
        String sharesList = "";
        for(Stock s: Stock.values()){
            sharesList +=  " [" + s + " - " + shares[s.ordinal()] + "]";
        }
        return "INFO: Your balance - " + money + ", your shares : " + sharesList ;
    }
}