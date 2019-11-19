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
    
    private String symbol;
    private double buyPrice;
    private double curPrice;
    private int shares;
    private boolean sold;
    private int id;

    public Stock(String symbol, int shares) throws IOException {
	double quote = 0.0;
	try {
	    quote = getQuote(symbol);
	} catch (MalformedURLException e) {
	    System.out.println(e);
	}
	this.symbol    = symbol;
	this.shares    = shares;
	this.buyPrice  = quote;
	this.curPrice  = quote;
	this.sold      = false;
	this.id        = 0;
    }

    public Stock(String symbol, int shares, double buyPrice) throws IOException {
	double quote = 0.0;
	try {
	    quote = getQuote(symbol);
	} catch (MalformedURLException e) {
	    System.out.println(e);
	}
	this.symbol    = symbol;
	this.shares    = shares;
	this.buyPrice  = moneyRound(buyPrice);
	this.curPrice  = quote;
	this.sold      = false;
	this.id        = 0;
    }
    
    public Stock(String symbol, int shares, double buyPrice, double sellPrice, boolean sold, int id) {
	this.symbol    = symbol;
	this.shares    = shares;
	this.buyPrice  = moneyRound(buyPrice);
	this.curPrice  = moneyRound(sellPrice);
	this.sold      = sold;
	this.id        = id;
    }

    public double update() throws IOException {
	double ret = 0.0;
	try {
	    curPrice = this.getQuote(symbol);
	    ret = curPrice;
	} catch (MalformedURLException e) {
	    System.out.println(e);
	}
	return ret;
    }

    public void update(double val) {
	curPrice = val;
    }
    
    public void sell() {
	sold = true;
    }

    public boolean getSold() {
	return sold;
    }

    public String getSymbol() {
	return symbol;
    }

    private double moneyRound(double val) {
	return (double) Math.round(val * 100d) / 100d;
    }

    private double percentRound(double val) {
	return (double) Math.round(val * 10000d) / 10000d;
    }

    public double getProfit() {
	return moneyRound(((curPrice - buyPrice) * shares) - 14.0);
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
        
	return id + "    " + STOCK_COLOR + symbol + "    " + shares + ANSI_RESET + "    "
	    + buyPrice + "    " + curPrice + "    " + MONEY_COLOR + this.getProfit() + "    "
	    + this.getExpense() + "    " + ROI_COLOR + this.getROI() + "%" + ANSI_RESET;
    }

    public String printCsv() {
	return symbol + "," + shares + "," + buyPrice + "," + curPrice + "," + this.getProfit()
	    + "," + this.getExpense() + "," + this.getROI();
    }

    public String printShort() {
	return "" + sold + "\t" + symbol + "\t" + shares + "\t" + buyPrice + "\t" + curPrice;
    }

    public int compareTo(Stock other) {
	return (int) (10000 * this.getROI()) - (int) (10000 * other.getROI());
    }

    public double getQuote(String symbol) throws IOException {
	double quote = 0.0;
	if (symbol.equals("btc")) {
	    try {
		URL btc = new URL("https://api.coindesk.com/v1/bpi/currentprice.json");
		URLConnection request = btc.openConnection();
		request.connect();
		JsonParser jsonParser = new JsonParser();
		JsonElement data = jsonParser.parse(new InputStreamReader((InputStream) request.getContent()));
		JsonObject obj = data.getAsJsonObject();
		quote = obj.getAsJsonObject("bpi").getAsJsonObject("USD").get("rate_float").getAsDouble();
	    } catch (MalformedURLException e) {
		System.out.println(e);
	    }
	} else {
	    Document doc = Jsoup.connect("https://www.marketwatch.com/investing/stock/"
					 + symbol.toLowerCase()).timeout(0).get();
	    quote = Double.parseDouble(doc.select("bg-quote.value").text());
	}
	return moneyRound(quote);
    }

    public void getZacks(String symbol) throws IOException {
	int zacks = 0;
	ArrayList<String> values = new ArrayList<String>();
	Document doc = Jsoup.connect("https://www.zacks.com/stock/quote/" + symbol.toUpperCase() + "?q=" + symbol).get();
	String one = doc.select("span.rankrect_1").text();
	String two = doc.select("span.rankrect_2").text();
	String three = doc.select("span.rankrect_3").text();
	String four = doc.select("span.rankrect_4").text();
	String five = doc.select("span.rankrect_5").text();
	values.add(one);
	values.add(two);
	values.add(three);
	values.add(four);
	values.add(five);

	for (int i = 0; i < values.size(); i++) {
	    System.out.println(values.get(i));
	}
    }
}
