package com.example.svenu.svenuitendaal__pset3;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
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

    // Request.
    RequestQueue queue;
    ArrayAdapter theAdapter;

    // List to store orders
    List<String> chosenItems = new ArrayList<String>();

    private TextView pageTitle;
    private ListView theListView;
    private Button editButton;

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
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        theListView = findViewById(R.id.menu_list);
        editButton = findViewById(R.id.button_edit);
        queue = Volley.newRequestQueue(this);

        loadCategories();
    }


    private void loadOrder() {
        pageTitle.setText(R.string.title_yourorder);
        editButton.setVisibility(View.VISIBLE);

        String[] orders = new String[chosenItems.size()];
        chosenItems.toArray(orders);
        ListAdapter listValues = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, orders);

        theListView.setAdapter(listValues);

        theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String menuChosen = "You have chosen " + String.valueOf(adapterView.getItemAtPosition(position));

                Toast.makeText(MainActivity.this, menuChosen, Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void loadMenu(final String categoryChosen) {
        menuList.clear();
        pageTitle.setText(R.string.title_menu);
        editButton.setVisibility(View.INVISIBLE);

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
                loadMenuItem(itemChosen);
            }
        });
    }

    private void loadMenuItem(String itemChosen) {
        menuList.clear();
        pageTitle.setText(itemChosen);
        editButton.setVisibility(View.VISIBLE);
        editButton.setText("Submit");

//        editButton.setOnClickListener()
    }

    private void loadCategories() {
        menuList.clear();
        pageTitle.setText(R.string.restaurant);
        editButton.setVisibility(View.INVISIBLE);

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


}
