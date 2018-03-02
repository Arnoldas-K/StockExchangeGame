package logic;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class Tests {

	public Game game;

	// sample data
	public static Deck[] sampleDecks() {
		return new Deck[] { 
				new Deck(Stock.Apple, -20, 5, 10, -5, -10, 20), 
				new Deck(Stock.BP, 20, 5, 10, -5, -10, -20),
				new Deck(Stock.Cisco, 20, -5, 10, -20, -10, 5), 
				new Deck(Stock.Dell, 5, -20, 10, 20, -10, -5),
				new Deck(Stock.Ericsson, -10, 10, 20, 5, -20, -5) 
		};
	}

	public static int[][] sampleShares() {
		return new int[][] { 
			{ 3, 0, 1, 4, 2 }, 
			{ 2, 2, 5, 0, 1 }
		};
	}

	@Before
	public void setup() {
		game = new Game(Tests.sampleDecks(), Tests.sampleShares());
	}

	/* checks buy function and if player can do more than two transactions */
	@Test
	public void buy() {
		game.buy(0, Stock.Apple, 1);
		game.buy(0, Stock.Cisco, 2);
		game.buy(0, Stock.Ericsson, 1);
		Assert.assertArrayEquals(game.getShares(0), new int[] { 4, 0, 3, 4, 2 });
		Assert.assertEquals(game.getCash(0), 191);
	}

	/* checks if player can do more than two transactions
	   and if can have negative shares*/
	@Test
	public void sell() {
		game.sell(1, Stock.BP, 3);
		game.sell(1, Stock.Ericsson, 1);
		game.sell(1, Stock.Apple, 2);
		game.sell(1, Stock.Cisco, 1);
		Assert.assertArrayEquals(game.getShares(1), new int[] { 0, 2, 5, 0, 0 });
		Assert.assertEquals(game.getCash(1), 800);
	}

	/*vote function test, checks if works correctly,
	if accepts only two votes and it is impossible
	to vote for the same card twice*/
	@Test
	public void vote() {
		game.vote(0, Stock.Apple, false);
		game.vote(0, Stock.Dell, true);
		game.vote(0, Stock.Cisco, false);
		game.vote(1, Stock.BP, true);
		game.vote(1, Stock.BP, true);
		game.executeVotes();
		Assert.assertArrayEquals(game.getPrices(), new int[] { 100, 120, 100, 105, 100 });
		Card[] cards = new Card[] { new Card(5), new Card(5), new Card(20), new Card(-20), new Card(-10) };
		Assert.assertArrayEquals(game.getCards(), cards);
	}

}
