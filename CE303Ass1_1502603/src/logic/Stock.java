package logic;

public enum Stock {
	Apple("A"), BP("B"), Cisco("C"), Dell("D"), Ericsson("E");

	Stock(String symbol) { 
		this.symbol = symbol;
	}

	public final String symbol;

	public static Stock parse(char c) {
		switch (Character.toUpperCase(c)) {
		case 'A':
			return Apple;
		case 'B':
			return BP;
		case 'C':
			return Cisco;
		case 'D':
			return Dell;
		case 'E':
			return Ericsson;
		}
		throw new RuntimeException("Stock parsing failed"); 
	}

	public static Stock parse(String s) {
		return parse(s.charAt(0));
	} 
	
}
