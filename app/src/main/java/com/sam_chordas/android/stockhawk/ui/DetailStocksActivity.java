package com.sam_chordas.android.stockhawk.ui;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by ishaan on 16/10/16.
 */

public class DetailStocksActivity extends AppCompatActivity {

    OkHttpClient client = new OkHttpClient();
    LineChartView lineChartView;

    private String fetchData(String urlString) throws IOException {
        Request request = new Request.Builder()
                .url(urlString)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        lineChartView = (LineChartView) findViewById(R.id.linechart);

        Bundle bundle = getIntent().getExtras();
        String symbol = bundle.getString("symbol");

        new FetchHistoricalDataTask().execute(symbol);
    }

    class FetchHistoricalDataTask extends AsyncTask<String, Void, Float[]> {
        @Override
        protected void onPostExecute(Float[] floats) {

            float[] stockValues = new float[floats.length];
            for (int i = 0; i < floats.length; i++)
                stockValues[i] = floats[i].floatValue();

            LineSet lineSet = new LineSet(Utils.getMonthValues(), stockValues);
            lineSet.setColor(Color.parseColor("#ffffff"))
                    .setDotsColor(Color.parseColor("#000000"))
                    .setThickness(4)
                    .setDashed(new float[] {10f, 10f});

            lineChartView.addData(lineSet);

            lineChartView.setBorderSpacing(Tools.fromDpToPx(15))
                    .setLabelsColor(Color.parseColor("#6a84c3"));

            lineChartView.show();

            super.onPostExecute(floats);
        }

        @Override
        protected Float[] doInBackground(String... params) {
            StringBuilder urlStringBuilder = new StringBuilder();
            urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
            try {
                urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.historicaldata where symbol "
                        + "in (", "UTF-8"));
                urlStringBuilder.append(URLEncoder.encode("\""+params[0]+"\")", "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            String[] dates = Utils.getDateRange();
            urlStringBuilder.append("and startDate='" + dates[0] + "' and endDate='" + dates[1] + "'");
            // finalize the URL for the API query.
            urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                    + "org%2Falltableswithkeys&callback=");

            String urlString = urlStringBuilder.toString();
            String getresponse = null;
            try {
                getresponse = fetchData(urlString);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Float[] values = Utils.quoteJsontoGraphValues(getresponse);
            return values;
        }
    }

}
