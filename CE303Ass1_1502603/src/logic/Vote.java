package logic;

public class Vote{
    private int id;
    private Stock stock;
    private boolean yes;

    public Vote(int id, Stock stock, boolean yes){
        this.id = id;
        this.stock = stock;
        this.yes = yes;
    }

    public Stock getStock() {
        return stock;
    }

    public int getId() {
        return id;
    }

    public boolean isYes() {
        return yes;
    }

    @Override
    public String toString() {
        return stock + " - " + yes;
    }
}