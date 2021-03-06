package uk.enfa.honkers;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import uk.enfa.honkers.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        binding.imageView.setImageDrawable(getDrawable(R.mipmap.ic_launcher_round));
        binding.buttonLogin.setOnClickListener( v -> login());
        binding.buttonRegister.setOnClickListener( v -> register());
    }

    void register(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter your nickname");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", (DialogInterface dialog, int which) -> {
                String baseURL = getResources().getString(R.string.api_endpoint);
                JSONObject json = new JSONObject();
                try {
                    json.put("username", binding.editTextUsername.getText().toString());
                    json.put("password", binding.editTextPassword.getText().toString());
                    json.put("nickname", input.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        baseURL + "/auth/register",
                        json,
                        (JSONObject response) -> {
                        login();
                    }
                , this::onErrorResponse);
                AppController.getInstance().addToRequestQueue(request, "register");
            });

        builder.setNegativeButton("Cancel", (DialogInterface dialog, int which) -> {
                dialog.cancel();
            });
        builder.show();
    }

    void login(){
        String baseURL = getResources().getString(R.string.api_endpoint);
        JSONObject json = new JSONObject();
        try {
            json.put("username", binding.editTextUsername.getText().toString());
            json.put("password", binding.editTextPassword.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, baseURL + "/auth/login", json, this::onLogin, this::onErrorResponse);
        AppController.getInstance().addToRequestQueue(request, "login");
    }

    void onLogin(JSONObject response){
        SharedPreferences.Editor editor = getSharedPreferences("uk.enfa.honkers", MODE_PRIVATE).edit();
        try {
            String token = response.getString("token");
            editor.putString("token", token);
            String JWTBodyStr = new String(Base64.decode(token.split("\\.")[1], Base64.URL_SAFE), "UTF-8");
            JSONObject JWTBody = new JSONObject(JWTBodyStr);
            editor.putString("username", JWTBody.getString("username"));
            editor.putString("nickname", JWTBody.getString("nickname"));
            editor.apply();
            finish();
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
    void onErrorResponse(VolleyError error){
        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
    }
}