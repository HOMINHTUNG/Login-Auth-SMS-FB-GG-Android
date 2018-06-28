package com.adsmove.customerlogin.Register;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.adsmove.customerlogin.MainActivity;
import com.adsmove.customerlogin.R;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputProfileActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "PhoneAuthActivity";
    private FirebaseAuth mAuth;

    private TextView txtName;
    private TextView txtEmail;
    private TextView btnNext;

    private int Flag_Login = 0; //2: login with google
    //1: login with facebook
    //0: input inform physic

    private LoginButton btnLoginFB;
    private CallbackManager callbackManager;

    private SignInButton btnLoginGG;
    private GoogleApiClient mGoogleSignInClient;
    private int RC_SIGN_IN = 115;

    private FirebaseUser currentUser;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_input_profile);
        //  Button mSignOutButton = (Button) findViewById(R.id.sign_out_button);
        txtName = (TextView) findViewById(R.id.txtName);
        txtEmail = (TextView) findViewById(R.id.txtEmail);
        btnNext = (TextView) findViewById(R.id.btnNext);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateName()) {
                    return;
                } else if (!validateEmailAddress()) {
                    return;
                } else {
                    //Flag_Login = 0 if user input infor default
                    Flag_Login = 0;

                    currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    currentUser.updateEmail(txtEmail.getText().toString());

                    Toast.makeText(InputProfileActivity.this, "Update info success!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(InputProfileActivity.this, MainActivity.class);
                    intent.putExtra("Flag_Login", Flag_Login);
                    startActivity(intent);
                    finish();
                }
            }
        });

        loginWithFacebook();
        loginWithGoogle();

    }

    //Set-up login with google
    private void loginWithGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Set the dimensions of the sign-in button.
        btnLoginGG = findViewById(R.id.btnLoginGG);
        btnLoginGG.setSize(SignInButton.SIZE_STANDARD);

        btnLoginGG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialog = new ProgressDialog(InputProfileActivity.this);
                mProgressDialog = ProgressDialog.show(InputProfileActivity.this, "",
                        "Loading...", true);

                signIn();
            }
        });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            Log.d(TAG, "Google Login success: Profile detail");
            Log.d(TAG, "id: " + account.getId());
            Log.d(TAG, "name: " + account.getDisplayName());
            Log.d(TAG, "email: " + account.getEmail());

            currentUser = FirebaseAuth.getInstance().getCurrentUser();
            currentUser.updateEmail(account.getEmail());

            //Flag_Login = 2 if user input infor with google
            Flag_Login = 2;
            Toast.makeText(InputProfileActivity.this, "Connect with google success!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(InputProfileActivity.this, MainActivity.class);
            intent.putExtra("Flag_Login", Flag_Login);
            startActivity(intent);
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            finish();
        } else {
            Log.d(TAG, "Google Login fail!");
        }
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        Toast.makeText(InputProfileActivity.this, "Disconnected!", Toast.LENGTH_LONG).show();
        Log.d(TAG, "Google Login: " + connectionResult.toString());
    }

    //Set-up login with facebook
    private void loginWithFacebook() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        KeyHash();

        callbackManager = CallbackManager.Factory.create();


        btnLoginFB = (LoginButton) findViewById(R.id.btnLoginFB);
        //"user_birthday", "user_friends"
        btnLoginFB.setReadPermissions(Arrays.asList(
                "public_profile", "email", "user_birthday", "user_friends"));
        // Callback registration
        btnLoginFB.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.v("LoginActivity", response.toString());

                                // Application code
                                try {
                                    String id = object.getString("id");
                                    String name = object.getString("name");
                                    String gender = object.getString("gender");
                                    String email = object.getString("email");
                                    String birthday = object.getString("birthday"); // 03/29/1996 format

                                    currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                    currentUser.updateEmail(email);

                                    Log.d(TAG, "Facebook Login success: Profile detail");
                                    Log.d(TAG, "id: " + object.getString("id"));
                                    Log.d(TAG, "name: " + object.getString("name"));
                                    Log.d(TAG, "gender: " + object.getString("gender"));
                                    Log.d(TAG, "email: " + object.getString("email"));
                                    Log.d(TAG, "birthday: " + object.getString("birthday"));


                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday");
                request.setParameters(parameters);
                request.executeAsync();

                //Flag_Login = 1 if user input infor with facebook
                Flag_Login = 1;
                Toast.makeText(InputProfileActivity.this, "Connect with facebook success!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(InputProfileActivity.this, MainActivity.class);
                intent.putExtra("Flag_Login", Flag_Login);
                startActivity(intent);
                finish();
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "btnLoginFB cancel");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.d(TAG, "btnLoginFB: " + exception.getMessage());
            }
        });
    }

    //Result Google + Facebook
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    //Check validate Name Syntax
    private boolean validateName() {
        String name = txtName.getText().toString();
        if (TextUtils.isEmpty(name)) {
            txtName.setError("Invalid Name!");
            return false;
        }
        return true;
    }

    //Check validate Email Syntax
    private boolean validateEmailAddress() {

        String emailAddress = txtEmail.getText().toString();

        Pattern regexPattern = Pattern.compile("^[(a-zA-Z-0-9-\\_\\+\\.)]+@[(a-z-A-z)]+\\.[(a-zA-z)]{2,3}$");
        Matcher regMatcher = regexPattern.matcher(emailAddress);
        if (!regMatcher.matches()) {
            txtEmail.setError("Invalid Email Address!");
            return false;
        }
        return true;
    }

    //Get KeyHash import Facebook API
    private void KeyHash() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo("com.adsmove.register", PackageManager.GET_SIGNATURES);
            for (Signature signature : packageInfo.signatures) {
                MessageDigest messageDigest = MessageDigest.getInstance("SHA");
                messageDigest.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(messageDigest.digest(), Base64.DEFAULT));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
