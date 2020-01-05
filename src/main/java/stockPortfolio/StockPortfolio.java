package stockPortfolio;

import Stock.Stock;
import java.util.*;
import java.io.*;

public class StockPortfolio {
    public static ArrayList<Stock> portfolio = new ArrayList<Stock>();
    public static final File data = new File("/home/jpwall/stockPortfolio/src/main/java/stockPortfolio/portfolio.data");
    public static double holdingExpense = 0.0;
    public static double soldExpense    = 0.0;
    public static double holdingProfit  = 0.0;
    public static double soldProfit     = 0.0;
    
    public static void main(String[] args) throws IOException, FileNotFoundException {
	if (args[0].equals("help")) {
	    printHelp();
	} else if (args[0].equals("holding")) {
	    readPortfolio();
	    printHolding();
	} else if (args[0].equals("sold")) {
	    readPortfolio();
	    printSold();
	} else if (args[0].equals("stats")) {
	    readPortfolio();
	    printStats();
	} else if (args[0].equals("buy")) {
	    readPortfolio();
	    String symbol = args[1];
	    int shares = Integer.parseInt(args[2]);
	    portfolio.add(new Stock(symbol, shares));
	    writePortfolio();
	} else if (args[0].equals("short")) {
	    readPortfolio();
	    String symbol = args[1];
	    int shares = Integer.parseInt(args[2]);
	    portfolio.add(new Stock(symbol, shares, true));
	    writePortfolio();
	} else if (args[0].equals("sell")) {
	    readPortfolio();
	    int id = Integer.parseInt(args[1]);
	    portfolio.get(id).sell();
	    writePortfolio();
	} else if (args[0].equals("record")) {
	    readPortfolio();
	    String symbol = args[1];
	    int shares = Integer.parseInt(args[2]);
	    double buyPrice = Double.parseDouble(args[3]);
	    double sellPrice = Double.parseDouble(args[4]);
	    portfolio.add(new Stock(symbol, shares, buyPrice, sellPrice, true, false, 0));
	    writePortfolio();
	} else if (args[0].equals("part")) {
	    readPortfolio();
	    String symbol = args[1];
	    int shares = Integer.parseInt(args[2]);
	    double buyPrice = Double.parseDouble(args[3]);
	    portfolio.add(new Stock(symbol, shares, buyPrice));
	    writePortfolio();
	} else if (args[0].equals("update") || args[0].equals("u")) {
	    readPortfolio();
	    updatePortfolio();
	    writePortfolio();
	} else if (args[0].equals("updatebtc")) {
	    readPortfolio();
	    Map <String, Double> tmp = new HashMap<String, Double>();
	    for (int i = 0; i < portfolio.size(); i++) {
		if (!portfolio.get(i).getSold() && portfolio.get(i).getSymbol().equals("btc")) {
		    if (tmp.containsKey("btc")) {
			portfolio.get(i).update(tmp.get("btc"));
		    } else {
			tmp.put("btc", portfolio.get(i).update());
		    }
		}
	    }
	    writePortfolio();
	} else if (args[0].equals("quote")) {
	    Stock quote = new Stock(args[1]);
	    System.out.println(quote.getCurPrice());
	} else if (args[0].equals("export")) {
	    readPortfolio();
	    exportPortfolio();
	} else if (args[0].equals("zacks")) {
	    Stock zacks = new Stock(args[1]);
	    zacks.getZacks(args[1]);
	} else if (args[0].equals("ordered") || args[0].equals("oh")) {
	    readPortfolio();
	    if (!args[0].equals("oh")) {
		orderedPortfolio(args[1]);
	    } else {
		orderedPortfolio("holding");
	    }
	} else {
	    printHelp();
	}
    }

    public static void printHelp() {
	System.out.println("OPTIONS:");
	System.out.println("help\t- Display this help message");
	System.out.println("update (or 'u')\t- Update the current prices for all holding stocks");
	System.out.println("updatebtc\t- Update btc values for holding");
	System.out.println("quote [ticker]\t- Get current quote for ticker symbol");
	System.out.println("holding\t- Show all holdings");
	System.out.println("sold\t- Show all sold");
	System.out.println("stats\t- Show statistics of portfolio");
	System.out.println("buy [symbol] [shares]\t- Buy shares of symbol");
	System.out.println("sell [id]\t\t- Sell shares of id");
	System.out.println("record [symbol] [shares] [buy] [sell]\t- record sold stock");
	System.out.println("part [symbol] [shares] [buy]\t- record holding stock");
	System.out.println("export\t- Export portfolio and holding as csv files");
	System.out.println("ordered [type]\t- Where type is all, holding, or sold, displays stocks in order based on ROI, greatest to least. Type 'oh' for ordered holding");
    }

    public static void readPortfolio() throws FileNotFoundException {
	Scanner dataScan = new Scanner(data);

	while (dataScan.hasNextLine() && dataScan.hasNext()) {
	    int id = Integer.parseInt(dataScan.next());
	    boolean sold = Boolean.parseBoolean(dataScan.next());
	    boolean shortType = Boolean.parseBoolean(dataScan.next());
	    String symbol = dataScan.next();
	    int shares = Integer.parseInt(dataScan.next());
	    double buyPrice = Double.parseDouble(dataScan.next());
	    double curPrice = Double.parseDouble(dataScan.next());

	    //if (!shortType) {
		portfolio.add(new Stock(symbol, shares, buyPrice, curPrice, sold,
					shortType, id));
		//} else {
		//Short add = new Short(symbol, shares, buyPrice, curPrice, sold, id);
		//portfolio.add((Stock) add);
		//}

	    if (sold) {
		soldExpense += buyPrice * shares;
		if (!shortType) {
		    soldProfit += ((curPrice - buyPrice) * shares) - 14;
		} else {
		    soldProfit += ((buyPrice - curPrice) * shares) - 14;
		}
	    } else {
		holdingExpense += buyPrice * shares;
		if (!shortType) {
		    holdingProfit += ((curPrice - buyPrice) * shares) - 14;
		} else {
		    holdingProfit += ((buyPrice - curPrice) * shares) - 14;
		}
	    }
	}
    }

    public static void writePortfolio() throws FileNotFoundException {
	PrintStream output = new PrintStream(data);
	for (int i = 0; i < portfolio.size(); i++) {
	    output.print(i + "\t");
	    output.println(portfolio.get(i).printShort());
	}
    }

    public static void printHolding() {
	for (int i = 0; i < portfolio.size(); i++) {
	    if (!portfolio.get(i).getSold()) {
		System.out.println(portfolio.get(i));
	    }
	}
    }

    public static void printSold() {
	for (int i = 0; i < portfolio.size(); i++) {
	    if (portfolio.get(i).getSold()) {
		System.out.println(portfolio.get(i));
	    }
	}
    }

    public static void orderedPortfolio(String arg) {
	Collections.sort(portfolio);
	if (arg.equalsIgnoreCase("all")) {
	    for (int i = 0; i < portfolio.size(); i++) {
		System.out.println(portfolio.get(i));
	    }
	} else if (arg.equalsIgnoreCase("holding")) {
	    printHolding();
	} else if (arg.equalsIgnoreCase("sold")) {
	    printSold();
	} else {
	    System.out.println("Unknown second argument, please use all, holding, or sold");
	}
    }

    public static void updatePortfolio() throws IOException {
	Map<String, Double> tmp = new HashMap<String, Double>();
	int cSize = 0;
	int hSize = 0;
	for (int i = 0; i < portfolio.size(); i++) {
	    if (!portfolio.get(i).getSold()) {
		hSize++;
	    }
	}
	for (int i = 0; i < portfolio.size(); i++) {
	    if (!portfolio.get(i).getSold()) {
		String ticker = portfolio.get(i).getSymbol();
		double val = 0.0;
		if (!tmp.containsKey(ticker)) {
		    val = portfolio.get(i).update();
		    tmp.put(ticker, val);
		} else {
		    portfolio.get(i).update(tmp.get(ticker));
		}
		cSize++;
		//System.out.println(portfolio.get(i).getSymbol() + " updated");
		System.out.print(cSize + " / " + hSize + "\r");
	    }
	}
    }

    public static void printStats() {
	holdingExpense    = (double) Math.round(holdingExpense * 100d) / 100d;
	holdingProfit     = (double) Math.round(holdingProfit * 100d) / 100d;
	double holdingROI = (double) Math.round(((holdingProfit / holdingExpense) * 100) * 10000d)
	    / 10000d;
	soldExpense    = (double) Math.round(soldExpense * 100d) / 100d;
	soldProfit     = (double) Math.round(soldProfit * 100d) / 100d;
	double soldROI = (double) Math.round(((soldProfit / soldExpense) * 100) * 10000d) / 10000d;
	double portfolioExpense = holdingExpense + soldExpense;
	double portfolioProfit  = holdingProfit + soldProfit;
	double portfolioROI     = (double) Math.round(((portfolioProfit / portfolioExpense) * 100)
						      * 10000d) / 10000d;
	System.out.println("CTGRY    PROFIT        EXPENSE        ROI");
	System.out.println("folio    " + portfolioProfit + "    " + portfolioExpense + "    "
			   + portfolioROI + "%");
	System.out.println("sold     " + soldProfit + "    " + soldExpense + "    "
			   + soldROI + "%");
	System.out.println("hldng    " + holdingProfit + "    " + holdingExpense + "    "
			   + holdingROI + "%");
    }

    public static void exportPortfolio() throws FileNotFoundException {
	File curr = new File("/root/stockPortfolio/src/main/java/stockPortfolio/holding.csv");
	PrintStream currOut = new PrintStream(curr);
	File all = new File("/root/stockPortfolio/src/main/java/stockPortfolio/portfolio.csv");
	PrintStream allOut = new PrintStream(all);
	allOut.println("Id,Symbol,Shares,Buy Price,Sell Price,Profit,Expense,ROI");
	currOut.println("Id,Symbol,Shares,Buy Price,Sell Price,Profit,Expense,ROI");
	for (int i = 0; i < portfolio.size(); i++) {
	    allOut.print(i + ",");
	    allOut.println(portfolio.get(i).printCsv());
	    if (!portfolio.get(i).getSold()) {
		currOut.print(i + ",");
		currOut.println(portfolio.get(i).printCsv());
	    }
	}
    }
}
