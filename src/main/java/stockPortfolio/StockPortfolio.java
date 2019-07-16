package stockPortfolio;

import Stock.Stock;
import java.util.*;
import java.io.*;

public class StockPortfolio {
    public static ArrayList<Stock> portfolio = new ArrayList<Stock>();
    public static File data = new File("/root/stockPortfolio/src/main/java/stockPortfolio/portfolio.data");
    public static final int refreshDelay = 12500;
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
	    portfolio.add(new Stock(symbol, shares, buyPrice, sellPrice, true, 0));
	    writePortfolio();
	} else if (args[0].equals("part")) {
	    readPortfolio();
	    String symbol = args[1];
	    int shares = Integer.parseInt(args[2]);
	    double buyPrice = Double.parseDouble(args[3]);
	    portfolio.add(new Stock(symbol, shares, buyPrice));
	    writePortfolio();
	} else if (args[0].equals("update")) {
	    readPortfolio();
	    updatePortfolio();
	    writePortfolio();
	} else if (args[0].equals("updatebtc")) {
	    readPortfolio();
	    for (int i = 0; i < portfolio.size(); i++) {
		if (!portfolio.get(i).getSold() && portfolio.get(i).getSymbol().equals("btc")) {
		    portfolio.get(i).update();
		}
	    }
	    writePortfolio();
	} else if (args[0].equals("quote")) {
	    Stock quote = new Stock(args[1], 0);
	    System.out.println(quote.getQuote(args[1]));
	} else if (args[0].equals("export")) {
	    readPortfolio();
	    exportPortfolio();
	} else {
	    System.out.println("Please use command line arguments. Try help.");
	}
    }

    public static void printHelp() {
	System.out.println("OPTIONS:");
	System.out.println("help\t- Display this help message");
	System.out.println("update\t- Update the current prices for all holding stocks");
	System.out.println("updatebtc\t- Update btc values for holding");
	System.out.println("quote [ticker]\t- Get current quote for ticker symbol");
	System.out.println("holding\t- Show all holdings");
	System.out.println("sold\t- Show all sold");
	System.out.println("stats\t- Show statistics of portfolio");
	System.out.println("buy [symbol] [shares]\t- Buy shares of symbol");
	System.out.println("sell [id]\t\t- Sell shares of id");
	System.out.println("record [symbol] [shares] [buy] [sell]");
	System.out.println("part [symbol] [shares] [buy]");
	System.out.println("export\t- Export portfolio and holding as csv files");
    }

    public static void readPortfolio() throws FileNotFoundException {
	Scanner dataScan = new Scanner(data);

	while (dataScan.hasNextLine() && dataScan.hasNext()) {
	    int id = Integer.parseInt(dataScan.next());
	    boolean sold = Boolean.parseBoolean(dataScan.next());
	    String symbol = dataScan.next();
	    int shares = Integer.parseInt(dataScan.next());
	    double buyPrice = Double.parseDouble(dataScan.next());
	    double curPrice = Double.parseDouble(dataScan.next());

	    portfolio.add(new Stock(symbol, shares, buyPrice, curPrice, sold, id));

	    if (sold) {
		soldExpense += buyPrice * shares;
		soldProfit += ((curPrice - buyPrice) * shares) - 14;
	    } else {
		holdingExpense += buyPrice * shares;
		holdingProfit += ((curPrice - buyPrice) * shares) - 14;
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

    public static void updatePortfolio() throws IOException {
	for (int i = 0; i < portfolio.size(); i++) {
	    if (!portfolio.get(i).getSold()) {
		if (!portfolio.get(i).getSymbol().equals("btc")) {
		    try {
			Thread.sleep(refreshDelay);
		    } catch (InterruptedException e) {
			System.out.println(e);
		    }
		}
		System.out.println(portfolio.get(i).getSymbol() + " updated");
		portfolio.get(i).update();
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
	System.out.println("CTGRY\tPROFIT\t\tEXPENSE\t\tROI");
	System.out.println("folio\t" + portfolioProfit + "\t" + portfolioExpense + "\t"
			   + portfolioROI + "%");
	System.out.println("sold\t" + soldProfit + "\t" + soldExpense + "\t"
			   + soldROI + "%");
	System.out.println("hldng\t" + holdingProfit + "\t" + holdingExpense + "\t"
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
