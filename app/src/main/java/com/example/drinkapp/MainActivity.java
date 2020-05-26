package com.example.drinkapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
        switch (view.getId()) {
            case R.id.bt_sign_up:
                Intent signUpIntent = new Intent(this, SignUpActivity.class);
                startActivity(signUpIntent);
                break;
            case R.id.bt_submit:
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                ServerRequests sr = new ServerRequests("user/login", null, LoginType.basic(username, password), new LoginCallBack(), HTTPRequestType.Post);
                sr.execute();
                break;
        }
    }

    class LoginCallBack implements Callback {
        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            call.cancel();
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Unable to connect to server", Toast.LENGTH_SHORT).show());
            /* Intent mainMenu = new Intent(getApplicationContext(), MainMenu.class);
            startActivity(mainMenu);
            finish();*/
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
            switch (response.code()) {
                case 200:
                    runOnUiThread(() -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setIcon(R.drawable.ic_mood);
                        builder.setTitle("Successfully logged in!");
                        builder.setMessage("Welcome and drink!!");
                        builder.setNegativeButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                Intent mainMenu = new Intent(getApplicationContext(), MainMenuActivity.class);
                                startActivity(mainMenu);
                                finish();
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    });
                    Gson gson = new Gson();
                    String token = gson.fromJson(response.body().string(), Token.class).token;
                    SharedPreferences preferences = getSharedPreferences("App", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("Token", token);
                    editor.apply();
                    System.out.println(token);
                    ServerRequests sr = new ServerRequests("machine", null, null, new MachineHandler(), HTTPRequestType.Get);
                    sr.execute();
                    break;
                default:
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Invalid password or username", Toast.LENGTH_SHORT).show());
            }
        }
    }

    class MachineHandler implements Callback {
        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
            switch (response.code()) {
                case 200:
                    Gson gson = new Gson();
                    Machine[] machines = gson.fromJson(response.body().string(), Machine[].class);
                    String id = machines[0].id;
                    SharedPreferences preferences = getSharedPreferences("App", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("MachineID", id);
                    editor.apply();
                    System.out.println(id);
                    break;
                default:
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Invalid password or username", Toast.LENGTH_SHORT).show());
            }
        }
    }

    class Machine {
        String id;
        String name;
    }

    private class Token {
        String token;
    }
}
