package Stock;

import java.net.*;
import java.io.*;
import java.util.*;
import com.google.gson.*;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Stock implements Comparable<Stock> {
    public static final String ANSI_RESET  = "\u001B[0m";
    public static final String ANSI_RED    = "\u001B[31m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_BLUE   = "\u001B[34m";
    public static final String ANSI_GREEN  = "\u001B[32m";
    
    protected String symbol;
    protected double buyPrice;
    protected double curPrice;
    protected int shares;
    protected boolean sold;
    protected boolean shortType;
    protected int id;

    public Stock(String symbol) {
	this(symbol, 0, 0.0, getQuote(symbol), false, false, 0);
    }
    
    public Stock(String symbol, int shares) throws IOException {
	this(symbol, shares, getQuote(symbol), getQuote(symbol), false, false, 0);
    }

    public Stock(String symbol, int shares, double buyPrice) throws IOException {
	this(symbol, shares, moneyRound(buyPrice), getQuote(symbol), false, false, 0);
    }

    public Stock(String symbol, int shares, boolean shortType) {
	this(symbol, shares, getQuote(symbol), getQuote(symbol), false, shortType, 0);
    }
    
    public Stock(String symbol, int shares, double buyPrice, double sellPrice, boolean sold,
		 boolean shortType, int id) {
	this.symbol    = symbol;
	this.shares    = shares;
	this.buyPrice  = moneyRound(buyPrice);
	this.curPrice  = moneyRound(sellPrice);
	this.sold      = sold;
	this.id        = id;
	this.shortType = shortType;
    }

    public double update() {
	curPrice = getQuote(symbol);
	return curPrice;
    }

    public void update(double val) {
	curPrice = val;
    }
    
    public void sell() {
	sold = true;
    }

    public double getCurPrice() {
	return curPrice;
    }

    public boolean getSold() {
	return sold;
    }

    public String getSymbol() {
	return symbol;
    }

    protected static double moneyRound(double val) {
	return (double) Math.round(val * 100d) / 100d;
    }

    private double percentRound(double val) {
	return (double) Math.round(val * 10000d) / 10000d;
    }

    public double getProfit() {
	if (!shortType) {
	    return moneyRound(((curPrice - buyPrice) * shares) - 14.0);
	} else {
	    return moneyRound(((buyPrice - curPrice) * shares) - 14.0);
	}
    }

    public double getExpense() {
	return moneyRound(buyPrice * shares);
    }

    public double getROI() {
	return percentRound((this.getProfit() / this.getExpense()) * 100);
    }

    public String toString() {
	String STOCK_COLOR = "";
	String MONEY_COLOR = "";
	String ROI_COLOR   = "";

	if (!sold) {
	    STOCK_COLOR = ANSI_RED;
	    MONEY_COLOR = ANSI_PURPLE;
	} else {
	    STOCK_COLOR = ANSI_RESET;
	    MONEY_COLOR = ANSI_BLUE;
	}
	
	if (this.getROI() > 0.0) {
	    ROI_COLOR = ANSI_GREEN;
	} else {
	    ROI_COLOR = ANSI_RED;
	}

	String ret = "";
	ret += id + "    " + STOCK_COLOR + symbol + "    " + shares + ANSI_RESET + "    "
	    + buyPrice + "    " + curPrice + "    " + MONEY_COLOR + this.getProfit() + "    "
	    + this.getExpense() + "    " + ROI_COLOR + this.getROI() + "%" + ANSI_RESET;
	if (shortType) {
	    ret += " [short]";
	}
	return ret;
    }

    public String printCsv() {
	return symbol + "," + shares + "," + buyPrice + "," + curPrice + "," + this.getProfit()
	    + "," + this.getExpense() + "," + this.getROI();
    }

    public String printShort() {
	return "" + sold + "\t" + shortType + "\t" + symbol + "\t" + shares + "\t" + buyPrice + "\t" + curPrice;
    }

    public int compareTo(Stock other) {
	return (int) (10000 * other.getROI()) -  (int) (10000 * this.getROI());
    }

    public static double getQuote(String symbol) {
	double quote = 0.0;
	if (symbol.equals("btc")) {
	    try {
		URL btc = new URL("https://api.coindesk.com/v1/bpi/currentprice.json");
		URLConnection request = btc.openConnection();
		request.connect();
		JsonParser jsonParser = new JsonParser();
		JsonElement data = jsonParser
		    .parse(new InputStreamReader((InputStream) request.getContent()));
		JsonObject obj = data.getAsJsonObject();
		quote = obj
		    .getAsJsonObject("bpi")
		    .getAsJsonObject("USD")
		    .get("rate_float")
		    .getAsDouble();
	    } catch (MalformedURLException e) {
		System.out.println(e);
	    } catch (IOException e) {
		System.out.println(e);
	    }
	} else {
	    try {
		Document doc = Jsoup.connect("https://www.marketwatch.com/investing/stock/" + symbol.toLowerCase()).timeout(0).get();
		String[] pre = doc.select(".value").text().split(" ");
		quote = Double.parseDouble(pre[0].replaceAll(",", ""));
	    } catch (IOException e) {
		System.out.println(e);
	    }
	}
	return moneyRound(quote);
    }

    public void getZacks(String symbol) throws IOException {
	int zacks = 0;
	ArrayList<String> values = new ArrayList<String>();
	Document doc = Jsoup.connect("https://www.zacks.com/stock/quote/" + symbol.toUpperCase()
				     + "?q=" + symbol).get();
	values.add(doc.select("span.rankrect_1").text());
	values.add(doc.select("span.rankrect_2").text());
	values.add(doc.select("span.rankrect_3").text());
	values.add(doc.select("span.rankrect_4").text());
	values.add(doc.select("span.rankrect_5").text());

	for (int i = 0; i < values.size(); i++) {
	    if (values.get(i).length() > 0) {
		System.out.println(values.get(i).substring(0, 1));
	    }
	}
    }
}
