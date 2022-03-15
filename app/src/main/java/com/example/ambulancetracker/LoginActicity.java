package com.example.ambulancetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActicity extends AppCompatActivity {
    EditText userName,password;
    Button login;
    ProgressDialog p;
    SharedPreferences sharedpreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_acticity);
        sharedpreferences = getSharedPreferences(ConfigSetting.MyPREFERENCES, Context.MODE_PRIVATE);
        p=new ProgressDialog(this);
        userName=(EditText) findViewById(R.id.et_email);
        password=(EditText) findViewById(R.id.pwd);
        login=(Button) findViewById(R.id.btn_login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestQueue queue = Volley.newRequestQueue(LoginActicity.this);
                JSONObject data=new JSONObject();
                try {
                    data.put("uname",userName.getText().toString().trim());
                    data.put("password",password.getText().toString().trim());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                p.show();
                JsonObjectRequest request =new JsonObjectRequest(Request.Method.POST, ConfigSetting.host+"/Home/login/", data,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                p.hide();
                                try {
                                    Toast.makeText(LoginActicity.this, response.toString(), Toast.LENGTH_SHORT).show();

                                    // String sts=response.getString("status");
                                    SharedPreferences.Editor editor = sharedpreferences.edit();

                                    editor.putString(ConfigSetting.uname, userName.getText().toString());
                                    editor.putString(ConfigSetting.UserId, response.getString("id"));
                                    editor.putString(ConfigSetting.Email, response.getString("email"));
                                    editor.commit();
                                    Intent intent = new Intent(LoginActicity.this, MapsActivity.class);
                                    startActivity(intent);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        p.hide();
                        Toast.makeText(getApplicationContext(),error.getMessage().toString(),Toast.LENGTH_LONG).show();
                    }
                });
                queue.add(request);
            }
        });
    }
}