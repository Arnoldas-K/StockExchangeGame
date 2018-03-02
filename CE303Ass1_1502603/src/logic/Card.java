package logic;

public final class Card implements Comparable<Card> {

	public static int[] EFFECTS = new int[] { -20, -10, -5, +5, +10, +20 };

	public int effect;

	public Card(int effect) {
		this.effect = effect;
	}

	public Card(){ effect = 0;}

	@Override
	public String toString() {
		return "" + effect;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + effect;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return effect == ((Card) obj).effect;
	}

	@Override
	public int compareTo(Card other) {
		return Integer.compare(effect, other.effect); 
	}

	public static Card parse(String s) { 
		int effect = Integer.parseInt(s);
		return new Card(effect);
	}
}
