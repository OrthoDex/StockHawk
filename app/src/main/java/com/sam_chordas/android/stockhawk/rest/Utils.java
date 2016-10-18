package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.util.Log;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

  private static String LOG_TAG = Utils.class.getSimpleName();

  public static boolean showPercent = true;

  public static ArrayList quoteJsonToContentVals(String JSON){
    ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
    JSONObject jsonObject;
    JSONArray resultsArray;
    try{
      jsonObject = new JSONObject(JSON);
      if (jsonObject != null && jsonObject.length() != 0){
        jsonObject = jsonObject.getJSONObject("query");
        int count = Integer.parseInt(jsonObject.getString("count"));
        if (count == 1){
          jsonObject = jsonObject.getJSONObject("results")
              .getJSONObject("quote");
          batchOperations.add(buildBatchOperation(jsonObject));
        } else{
          resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

          if (resultsArray != null && resultsArray.length() != 0){
            for (int i = 0; i < resultsArray.length(); i++){
              jsonObject = resultsArray.getJSONObject(i);
              batchOperations.add(buildBatchOperation(jsonObject));
            }
          }
        }
      }
    } catch (JSONException e){
      Log.e(LOG_TAG, "String to JSON failed: " + e);
    }
    return batchOperations;
  }

  public static String truncateBidPrice(String bidPrice){
    bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
    return bidPrice;
  }

  public static String truncateChange(String change, boolean isPercentChange){
    String weight = change.substring(0,1);
    String ampersand = "";
    if (isPercentChange){
      ampersand = change.substring(change.length() - 1, change.length());
      change = change.substring(0, change.length() - 1);
    }
    change = change.substring(1, change.length());
    double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
    change = String.format("%.2f", round);
    StringBuffer changeBuffer = new StringBuffer(change);
    changeBuffer.insert(0, weight);
    changeBuffer.append(ampersand);
    change = changeBuffer.toString();
    return change;
  }

  public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject){
    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
        QuoteProvider.Quotes.CONTENT_URI);
    try {
      String change = jsonObject.getString("Change");

      if (!change.equals("null") && change != null) {
        builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
        builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
        builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
                jsonObject.getString("ChangeinPercent"), true));
        builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
        builder.withValue(QuoteColumns.ISCURRENT, 1);
        if (change.charAt(0) == '-') {
          builder.withValue(QuoteColumns.ISUP, 0);
        } else {
          builder.withValue(QuoteColumns.ISUP, 1);
        }
      }
      else {
        // if stock price doesn't exist
        return null;
      }
    } catch (JSONException e){
      e.printStackTrace();
    }
    return builder.build();
  }

  public static String[] getDateRange() {

    Calendar now = Calendar.getInstance();
    now.add(Calendar.MONTH, -4);

    return new String[]{
      new SimpleDateFormat("yyyy-MM-dd").format(now.getTime()),
      new SimpleDateFormat("yyyy-MM-dd").format(new Date())
    };
  }

  public static Float[] quoteJsontoGraphValues(String getresponse) {
    ArrayList<Float> values = new ArrayList<>();
    JSONObject jsonObject;
    JSONArray resultsArray;

    try{
      jsonObject = new JSONObject(getresponse);
      if (jsonObject != null && jsonObject.length() != 0){
        jsonObject = jsonObject.getJSONObject("query");
        int count = Integer.parseInt(jsonObject.getString("count"));

        resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

        if (resultsArray != null && resultsArray.length() != 0){
          for (int i = 0; i < resultsArray.length(); i += count/4 ){
            jsonObject = resultsArray.getJSONObject(i);
            values.add(Float.parseFloat(jsonObject.getString("Close")));
          }
        }
      }
    } catch (JSONException e){
      Log.e(LOG_TAG, "String to JSON failed: " + e);
    }

    Float[] stockValues = values.toArray(new Float[0]);
    return stockValues;
  }

  public static String[] getMonthValues() {
    Calendar now = Calendar.getInstance();
    String[] months = new String[4];
    now.add(Calendar.MONTH, +1);

    for (int i = 0; i < 4; i++) {
      now.add(Calendar.MONTH, -1);
      months[i] = new SimpleDateFormat("MMMM").format(now.getTime());
    }
    return months;
  }
}
