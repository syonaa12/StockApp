package com.example.stockapp;

import androidx.appcompat.app.AppCompatActivity ;

import android.animation.ObjectAnimator;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.database.sqlite.SQLiteDatabase;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView stockPriceTextView, exchangeTextView, companyText;
    public EditText symbol;

    public TextView dataTextView;
    Button updateButton, addStockButton, viewDataButton; // Add a reference to the Add Stock button
    String symbolString;
    private RequestQueue requestQueue;
    double previousPrice = 0.0;
    private static final String API_KEY = "pk_5fb978ba9b184303b31f1dd00a933ff9";   // API key here
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Timer timer;
    private DbAdapter dbAdapter; // Declare DbAdapter
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stockPriceTextView = findViewById(R.id.stockPriceTextView);
        exchangeTextView = findViewById(R.id.exchangeTextView);
        companyText = findViewById(R.id.companyTextView);

        updateButton = findViewById(R.id.updateButton);
        addStockButton = findViewById(R.id.addStockButton); // Initialize Add Stock button
        symbol = findViewById(R.id.symbolName);
        requestQueue = Volley.newRequestQueue(this);
        dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        viewDataButton = findViewById(R.id.viewStocksButton);

        viewDataButton.setOnClickListener(this);

        dataTextView = findViewById(R.id.dataTextView);
        dbAdapter = new DbAdapter(this);
        // Initialize DbAdapter
        dbAdapter = new DbAdapter(this);
        dbAdapter.open();

//        public void onClick(View view) {
//            String data = dbAdapter.getData();
//            Message.message(this, data);
//        }

        // Set OnClickListener for Add Stock button
        addStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addStockToDatabase();
            }
        });


        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                symbolString = symbol.getText().toString();
                if(!symbolString.isEmpty()) {
                    fetchStockPrice();
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a valid symbol", Toast.LENGTH_SHORT).show();
                }
            }
        });

        timer = new Timer();
        symbolString = symbol.getText().toString();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(!symbolString.isEmpty()) {
                    fetchStockPrice();
                }
            }
        }, 0, 2*1000);
    }

    private void fetchStockPrice() {
        String symbolString = symbol.getText().toString();
        String apiUrl = "https://cloud.iexapis.com/stable/stock/" + symbolString + "/quote?token=" + API_KEY;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, apiUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String stockPrice = response.getString("latestPrice");
                            String exchange = response.getString("primaryExchange");
                            String company = response.getString("companyName");

                            double stockPriceValue = Double.parseDouble(stockPrice);
                            updateStockPrice(stockPriceValue, "Exchange: " + exchange, "Company: " + company);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("ERROR", "Error parsing JSON: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(error.networkResponse!=null&& error.networkResponse.data!=null)
                        {
                            String responeBody = new String(error.networkResponse.data);
                            Log.e("API_ERROR", "Error Response: " + responeBody);
                        }
                        Log.e("API_ERROR", "Error fetching stock price: " + error.getMessage());
                    }
                });
        requestQueue.add(request);
    }

    private void updateStockPrice(double currentPrice, String exchange, String company) {
        double instantaneousChange = currentPrice - previousPrice;
        previousPrice = currentPrice;
        handler.post(new Runnable() {
            @Override
            public void run() {
                startBlinkAnimation(stockPriceTextView);
                String formattedPrice = String.format("%2f", currentPrice);
                stockPriceTextView.setText(formattedPrice);
                int textColor = (instantaneousChange>=0.00) ? Color.GREEN : Color.RED;
                stockPriceTextView.setTextColor(textColor);
                exchangeTextView.setText(exchange);
                companyText.setText(company);
            }
        });
    }

    private void startBlinkAnimation(final TextView textView) {
        ObjectAnimator blink = ObjectAnimator.ofFloat(textView, "alpha", 1f, 0f, 1f);
        blink.setDuration(200);
        blink.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(timer!=null) {
            timer.cancel();
            timer.purge();
        }

        // Close DbAdapter
        dbAdapter.close();
    }
//    public void viewData(View view) {
//
//    }
    // Method to add stock to database
// Method to add stock to database
    private void addStockToDatabase() {
        // Get the symbol, exchange, and company from the UI
        String symbolText = symbol.getText().toString();
        String exchange = exchangeTextView.getText().toString();
        String company = companyText.getText().toString();

        // Add the stock price to the database
        if (!symbolText.isEmpty() && !exchange.isEmpty() && !company.isEmpty()) {
            double currentPrice = 0.0; // Replace this with the actual current price
            long result = dbAdapter.addStockPrice(symbolText, currentPrice, exchange, company);
            if (result != -1) {
                Toast.makeText(MainActivity.this, "Stock added to database", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Failed to add stock to database", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "Please fetch the stock price first", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        String data = dbAdapter.getData();
        Message.message(this, data);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}

