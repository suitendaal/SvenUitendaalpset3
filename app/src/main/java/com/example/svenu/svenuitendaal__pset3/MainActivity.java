package com.example.svenu.svenuitendaal__pset3;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    // List to store JSON menu values
    List<String> menuList = new ArrayList<String>();

    List<String> names = new ArrayList<>();
    List<Integer> prices = new ArrayList<>();
    List<Integer> ids = new ArrayList<>();

    int orderIndex;

    // Request.
    RequestQueue queue;
    ArrayAdapter theAdapter;

    // List to store orders
    List<String> chosenItems = new ArrayList<String>();

    private TextView pageTitle;
    private ListView theListView;
    private Button editButton;
    private TextView itemDescription;
    private ImageView itemImage;
    private TextView itemPrice;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nav_menu:
                    loadCategories();
                    return true;
                case R.id.nav_your_order:
                    loadOrder();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pageTitle = findViewById(R.id.textView);
        theListView = findViewById(R.id.menu_list);
        editButton = findViewById(R.id.button_edit);
        itemDescription = findViewById(R.id.item_description);
        itemImage = findViewById(R.id.item_image);
        itemPrice = findViewById(R.id.item_price);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        queue = Volley.newRequestQueue(this);

        loadJSONArray();
        loadSharedPrefs();
        loadCategories();
    }

    private void loadJSONArray() {

        // Get menu.
        String url = "https://resto.mprog.nl/menu";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray items = response.getJSONArray("items");
                    int n = items.length();
                    for (int i = 0; i < n; i+=1) {
                        JSONObject categoriesJSON = items.getJSONObject(i);
                        names.add(categoriesJSON.getString("name"));
                        prices.add(categoriesJSON.getInt("price"));
                        ids.add(categoriesJSON.getInt("id"));
                    }
                }
                catch (JSONException exception) {
                    apology("That didn't work!");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                apology("No internet connection");
            }
        });

        // Add the request to the RequestQueue.
        queue.add(jsObjRequest);
    }


    private void loadOrder() {
        pageTitle.setText(R.string.title_yourorder);
        editButton.setVisibility(View.VISIBLE);
        editButton.setText("Submit order");
        itemDescription.setVisibility(View.GONE);
        itemImage.setVisibility(View.GONE);
        theListView.setVisibility(View.VISIBLE);
        itemPrice.setVisibility(View.GONE);

        theAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, chosenItems);
        theListView.setAdapter(theAdapter);

        theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String itemChosen = String.valueOf(adapterView.getItemAtPosition(position));
                orderIndex = position;
                loadMenuItem(itemChosen, "Delete");
            }
        });
    }

    private void loadMenu(final String categoryChosen) {
        menuList.clear();
        pageTitle.setText(R.string.title_menu);
        editButton.setVisibility(View.GONE);
        itemDescription.setVisibility(View.GONE);
        itemImage.setVisibility(View.GONE);
        theListView.setVisibility(View.VISIBLE);
        itemPrice.setVisibility(View.GONE);

        theAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, menuList);
        theListView.setAdapter(theAdapter);

        // Get menu.
        String url = "https://resto.mprog.nl/menu";

        // Request a string response from the provided URL.
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray itemsJSON = response.getJSONArray("items");
                    int n = itemsJSON.length();
                    for (int i = 0; i < n; i+=1) {
                        JSONObject categoriesJSON = itemsJSON.getJSONObject(i);
                        if (categoriesJSON.getString("category").equals(categoryChosen)){
                            menuList.add(categoriesJSON.getString("name"));
                        }
                    }
                    theAdapter.notifyDataSetChanged();
                }
                catch (JSONException exception) {
                    apology("That didn't work!");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                apology("No internet connection");
            }
        });

        // Add the request to the RequestQueue.
        queue.add(jsObjRequest);

        theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String itemChosen = String.valueOf(adapterView.getItemAtPosition(position));
                loadMenuItem(itemChosen, "Submit");
            }
        });
    }

    private void loadMenuItem(final String itemChosen, String buttonValue) {
        menuList.clear();
        pageTitle.setText(itemChosen);
        editButton.setVisibility(View.VISIBLE);
        editButton.setText(buttonValue);
        itemDescription.setVisibility(View.VISIBLE);
        itemImage.setVisibility(View.VISIBLE);
        itemImage.setImageBitmap(null);
        theListView.setVisibility(View.GONE);
        itemPrice.setVisibility(View.VISIBLE);

        // Get menu.
        String url = "https://resto.mprog.nl/menu";

        // Request a string response from the provided URL.
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray itemsJSON = response.getJSONArray("items");
                    int n = itemsJSON.length();
                    for (int i = 0; i < n; i+=1) {
                        JSONObject categoriesJSON = itemsJSON.getJSONObject(i);
                        if (categoriesJSON.getString("name").equals(itemChosen)){
                            itemDescription.setText(categoriesJSON.getString("description"));
                            itemPrice.setText("Price: €" + categoriesJSON.getInt("price"));
                            String imageUrl = categoriesJSON.getString("image_url");
                            imageRequestFunction(imageUrl);
                        }
                    }
                }
                catch (JSONException exception) {
                    apology("That didn't work!");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                apology("No internet connection");
            }
        });

        // Add the request to the RequestQueue.
        queue.add(jsObjRequest);
    }

    private void imageRequestFunction(String imageUrl) {
        // bron: https://www.programcreek.com/javi-api-examples/index.php?api=com.android.volley.toolbox.ImageRequest
        ImageRequest imageRequest = new ImageRequest(imageUrl, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap bitmap) {
                itemImage.setImageBitmap(bitmap);
            }
        }, 0, 0, null, Bitmap.Config.ALPHA_8,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        apology("Loading image failed");
                    }
                }
        );
        queue.add(imageRequest);
    }

    private void loadCategories() {
        menuList.clear();
        pageTitle.setText(R.string.restaurant);
        editButton.setVisibility(View.GONE);
        itemDescription.setVisibility(View.GONE);
        itemImage.setVisibility(View.GONE);
        theListView.setVisibility(View.VISIBLE);
        itemPrice.setVisibility(View.GONE);

        theAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, menuList);
        theListView.setAdapter(theAdapter);

        // Get categories.
        String url = "https://resto.mprog.nl/categories";

        // Request a string response from the provided URL.
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //
                            JSONArray categoriesJSON = response.getJSONArray("categories");
                            int n = categoriesJSON.length();
                            for (int i = 0; i < n; i+=1) {
                                menuList.add(categoriesJSON.getString(i));
                            }
                            theAdapter.notifyDataSetChanged();
                        }
                        catch (JSONException exception) {
                            apology("That didn't work!");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                apology("No internet connection");
            }
        });

        // Add the request to the RequestQueue.
        queue.add(jsObjRequest);

        // Add onClick listener
        theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String itemChosen = String.valueOf(adapterView.getItemAtPosition(position));
                loadMenu(itemChosen);
            }
        });
    }

    public void apology(String apologyString){
        Toast.makeText(MainActivity.this, apologyString, Toast.LENGTH_SHORT).show();
    }

    public void buttonClicked(View view) {
        Button button = (Button)view;
        String buttonText = button.getText().toString();
        String itemText = pageTitle.getText().toString();

        if (buttonText.equals("Submit")) {
            chosenItems.add(itemText);
            apology("You have ordered " + itemText);
            saveToSharedPrefs();
            loadCategories();
        }

        else if (buttonText.equals("Delete")) {
            chosenItems.remove(orderIndex);
            apology("You have removed " + itemText);
            saveToSharedPrefs();
            loadOrder();
        }

        else if (buttonText.equals("Submit order")) {
            loadSubmit();
        }

        else if (buttonText.equals("Order")) {
            chosenItems.clear();
            apology("Ordered");
            BottomNavigationView navigation = findViewById(R.id.navigation);
            navigation.setSelectedItemId(R.id.nav_menu);
        }
    }

    private void loadSubmit() {
        pageTitle.setText("Submit order");
        editButton.setVisibility(View.VISIBLE);
        editButton.setText("Order");
        itemDescription.setVisibility(View.VISIBLE);
        itemImage.setVisibility(View.GONE);
        theListView.setVisibility(View.GONE);
        itemPrice.setVisibility(View.VISIBLE);

        int price = 0;
        List<Integer> idList = new ArrayList<>();

        int n = chosenItems.size();
        for (int i = 0; i < n; i+=1) {
            int index = names.indexOf(chosenItems.get(i));
            price += prices.get(index);
            idList.add(ids.get(index));
        }

        // Estimated time
        String url = "https://resto.mprog.nl/order";

        // Request a string response from the provided URL.
        JsonObjectRequest timeRequest = new JsonObjectRequest(
                Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    //
                    String estimated_time = response.getString("preparation_time");
                    itemDescription.setText("The estimated time is " + estimated_time + " minutes.");
                }
                catch (JSONException exception) {
                    apology("No time available");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                apology("No internet connection");
            }
        });

        // Add the request to the RequestQueue.
        queue.add(timeRequest);

        itemPrice.setText("Total price: €" + price);
    }

    public void saveToSharedPrefs() {
        SharedPreferences prefs = this.getSharedPreferences("settings", this.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        int chosenItemsSize = chosenItems.size();
        editor.putInt("chosenItemsSize", chosenItemsSize);

        for (int i = 0; i < chosenItemsSize; i+=1) {
            editor.remove("chosenItem" + i);
            editor.putString("chosenItem" + i, chosenItems.get(i));
        }

        editor.commit();
    }

    public void loadSharedPrefs() {
        SharedPreferences prefs = this.getSharedPreferences("settings", this.MODE_PRIVATE);

        int chosenItemsSize = prefs.getInt("chosenItemsSize", 0);

        for (int i = 0; i < chosenItemsSize; i+=1) {
            String chosenItemRestored = prefs.getString("chosenItem" + i, null);
            if (chosenItemRestored != null) {
                chosenItems.add(chosenItemRestored);
            }
        }
    }

}