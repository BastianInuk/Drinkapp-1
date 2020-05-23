package com.example.drinkapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    EditText etUsername, etPassword;
    Button btSubmit, btSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etUsername = findViewById(R.id.et_Username);
        etPassword = findViewById(R.id.et_Password);

        btSubmit = findViewById(R.id.bt_submit);
        btSignUp = findViewById(R.id.bt_sign_up);

        btSubmit.setOnClickListener(this);
        btSignUp.setOnClickListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View view) {
        if (view == btSubmit) {
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();

            ServerRequests sr = new ServerRequests("user/login",null, LoginType.basic(username, password), new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {

                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    Gson gson = new Gson();

                    String token = gson.fromJson(response.body().string(), Token.class).token;

                    System.out.println(token);
                }
            });

            sr.execute();
            if (etUsername.getText().toString().equals("Simon") &&
                    etPassword.getText().toString().equals("ErSej")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        MainActivity.this
                );
                builder.setIcon(R.drawable.ic_mood);
                builder.setTitle("Succesfully logged in!");
                builder.setMessage("Welcome and drink!!");

                builder.setNegativeButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        Intent mainMenu = new Intent(getApplicationContext(), MainMenu.class);
                        startActivity(mainMenu);
                        finish();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        }
        if (view == btSignUp) {
            Intent signUpIntent = new Intent(this, SignUpActivity.class);
            startActivity(signUpIntent);
            finish();
        }

    }

    class Token {
        String token;
    }
}
