package uk.enfa.honkers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import kotlin.jvm.internal.Intrinsics;
import uk.enfa.honkers.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        binding.floatingActionButton.setOnClickListener( v -> {
            Intent intent = new Intent(this, NewPostActivity.class);
            startActivity(intent);
        });
        binding.swipe.setOnRefreshListener(this::updateTimeline);
    }

    @Override
    protected void onResume() {
        updateTimeline();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.gotouser:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Enter username");

                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("OK", (DialogInterface dialog, int which) -> {
                    Intent intent = new Intent(this, ProfileActivity.class);
                    intent.putExtra("username", input.getText().toString());
                    startActivity(intent);
                });

                builder.setNegativeButton("Cancel", (DialogInterface dialog, int which) -> {
                    dialog.cancel();
                });
                builder.show();
                break;
            case R.id.settings:
                Intent settings = new Intent(this, UserSettingsActivity.class);
                startActivity(settings);
                break;
            case R.id.logout:
                SharedPreferences.Editor editor = getSharedPreferences("uk.enfa.honkers", MODE_PRIVATE).edit();
                editor.remove("token");
                editor.remove("username");
                editor.remove("nickname");
                editor.apply();
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    void updateTimeline(){
        binding.swipe.setRefreshing(true);
        String jwt = getSharedPreferences("uk.enfa.honkers", MODE_PRIVATE).getString("token", "null");
        Log.d("main", "token: " + jwt);
        AppController.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.GET, getResources().getString(R.string.api_endpoint)+"/timeline/following", null, this::onTimelineReceived, this::onErrorResponse){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();

                headers.put("Authorization", "Bearer " + jwt);
                return headers;
            }
        });

    }

    void onTimelineReceived(JSONObject response){
        binding.swipe.setRefreshing(false);
        try {
            JSONArray timeline = response.getJSONArray("timeline");
            PostAdapter pa = new PostAdapter(timeline);
            binding.recyclerTimeline.setLayoutManager(new LinearLayoutManager(this));
            binding.recyclerTimeline.setAdapter(pa);
        } catch (JSONException e) {
            e.printStackTrace();
            toast("Failed to parse timeline.");
        }

    }

    void onErrorResponse(VolleyError error){

        toast("Error: "+error.networkResponse.statusCode);
        if(error instanceof AuthFailureError){
            Intent login = new Intent(this, LoginActivity.class);
            startActivity(login);
        }
    }

    void toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}