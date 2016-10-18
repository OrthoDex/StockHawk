package com.sam_chordas.android.stockhawk.ui;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

public class StockHawkWidgetService extends IntentService {

    public StockHawkWidgetService() {
        super("StockHawkWidgetService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, StockHawkAppWidgetProvider.class));

        // Get Stock data from Provider
        Cursor data = getContentResolver().query(
                QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE, QuoteColumns.CHANGE},
                null,
                null,
                null);
        if (data == null) {
            return;
        }
        if (!data.moveToFirst()){
            data.close();
            return;
        }

        String symbol = data.getString(data.getColumnIndex(QuoteColumns.SYMBOL));
        String bidPrice = data.getString(data.getColumnIndex(QuoteColumns.BIDPRICE));
        String change = data.getString(data.getColumnIndex(QuoteColumns.CHANGE));

        data.close();

        for (int appwidgetId : appWidgetIds) {

            RemoteViews rv = new RemoteViews(getPackageName(), R.layout.stockhawk_appwidget);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                rv.setContentDescription(R.id.app_widget_main, getString(R.string.widget_view_description));
            }
            rv.setTextViewText(R.id.widget_stock_symbol, symbol);
            rv.setTextViewText(R.id.widget_bid_price, bidPrice);
            rv.setTextViewText(R.id.widget_change, change);

            Intent detailIntent = new Intent(this, MyStocksActivity.class);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, detailIntent, 0);

            rv.setOnClickPendingIntent(R.id.app_widget_main, pendingIntent);
            rv.setContentDescription(R.id.app_widget_main, getString(R.string.app_widget_description));

            appWidgetManager.updateAppWidget(appwidgetId, rv);
        }
    }
}