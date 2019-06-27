package org.safecoin.safeprice;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

class Exchange {
    private String exchange = "SafeTrade";
    private String pair = "safebtc";

    Exchange(String exchange, String pair) {
        if (exchange != null && !exchange.isEmpty()) {
            this.exchange = exchange;
        }
        if (pair != null && !pair.isEmpty()) {
            this.pair = pair;
        }
    }

    String getExchange() {
        return exchange;
    }

    CharSequence getTruePair() {
        Map<CharSequence, CharSequence> markets = null;
        switch (exchange) {
            case "SafeTrade":
                markets = safetrade;
                break;
            case "CREX24":
                markets = crex24;
                break;
            case "GRAVIEX":
                markets = graviex;
                break;
        }
        for (CharSequence market : markets.keySet()) {
            if (pair.contentEquals(markets.get(market))) {
                return market;
            }
        }
        return pair;
    }

    private static final Map<CharSequence, CharSequence> safetrade = new HashMap<CharSequence, CharSequence>(){{
        put("SAFE/BTC", "safebtc");
        put("SAFE/LTC", "safeltc");
        put("SAFE/DOGE", "safedoge");
    }};

    private static final Map<CharSequence, CharSequence> crex24 = new HashMap<CharSequence, CharSequence>(){{
        put("SAFE/BTC", "BTC_SAFE");
    }};

    private static final Map<CharSequence, CharSequence> graviex = new HashMap<CharSequence, CharSequence>(){{
        put("SAFE/BTC", "safebtc");
    }};

    static CharSequence[] getMarkets(String exchange){
        switch (exchange) {
            case "SafeTrade":
                return safetrade.keySet().toArray(new CharSequence[1]);
            case "CREX24":
                return crex24.keySet().toArray(new CharSequence[1]);
            case "GRAVIEX":
                return graviex.keySet().toArray(new CharSequence[1]);
        }
        return safetrade.keySet().toArray(new CharSequence[1]);
    }
    static CharSequence[] getMarketValues(String exchange){
        switch (exchange) {
            case "SafeTrade":
                return safetrade.values().toArray(new CharSequence[1]);
            case "CREX24":
                return crex24.values().toArray(new CharSequence[1]);
            case "GRAVIEX":
                return graviex.values().toArray(new CharSequence[1]);
        }
        return safetrade.values().toArray(new CharSequence[1]);
    }

    double getExchangeRate() {
        switch (exchange) {
            case "SafeTrade":
                return getSafeTradeExchangeRate();
            case "CREX24":
                return getCrex24ExchangeRate();
            case "GRAVIEX":
                return getGraviexExchangeRate();
        }
        return 0;
    }

    private double getSafeTradeExchangeRate() {
        String result = get(String.format("https://safe.trade/api/v2/tickers/%s.json", pair));
        try {
            JSONObject data = new JSONObject(result);
            return data.getJSONObject("ticker").getDouble("last");
        } catch (Exception ignore){
            return 0;
        }
    }
    private double getCrex24ExchangeRate() {
        String result = get(String.format("https://api.crex24.com/CryptoExchangeService/BotPublic/ReturnTicker?request=[NamePairs=%s]", pair));
        try {
            JSONObject data = new JSONObject(result);
            return data.getJSONArray("Tickers").getJSONObject(0).getDouble("Last");
        } catch (Exception ignore){
            return 0;
        }
    }
    private double getGraviexExchangeRate() {
        String result = get(String.format("https://graviex.net/api/v2/tickers/%s.json", pair));
        try {
            JSONObject data = new JSONObject(result);
            return data.getJSONObject("ticker").getDouble("last");
        } catch (Exception ignore){
            return 0;
        }
    }

    private String get(String URL) {
        try {
            return new API().execute(URL).get(10, TimeUnit.SECONDS);
        } catch (Exception ignore) {
            return null;
        }
    }

    private static class API extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder output = new StringBuilder();
                    String s;
                    while ((s = in.readLine()) != null) {
                        output.append(s);
                    }
                    return output.toString();
                } catch (Exception ignore) {}
                finally {
                    urlConnection.disconnect();
                }
            } catch (Exception ignore){}
            return null;
        }
    }
}
