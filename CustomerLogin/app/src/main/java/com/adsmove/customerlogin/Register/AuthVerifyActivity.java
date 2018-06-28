package com.adsmove.customerlogin.Register;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.adsmove.customerlogin.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;


/**
 * Created by HOMINHTUNG-PC on 4/16/2018.
 */

public class AuthVerifyActivity extends AppCompatActivity {

    private static final String TAG = "AuthVerifyActivity";

    private boolean FlagResend = false;     // true: Resend
    // false: Not resend
    private EditText txtNum1;
    private EditText txtNum2;
    private EditText txtNum3;
    private EditText txtNum4;
    private EditText txtNum5;
    private EditText txtNum6;

    private TextView btnNext;
    private TextView btnResend;

    private FirebaseAuth mAuth;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    String mVerificationId;
    String mPhoneNumber;
    String mCode;
    String mInputCode;

    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_verify);

        initEditText();

        Bundle extraItem = getIntent().getExtras();
        try {
            mPhoneNumber = extraItem.getString("PhoneNumber").toString();
            Log.d(TAG, "Bundle: mPhoneNumber " + extraItem.getString("PhoneNumber").toString());

            mCode = extraItem.getString("Code").toString();
            Log.d(TAG, "Bundle: Token " + extraItem.getString("Code").toString());

            mVerificationId = extraItem.getString("Verify").toString();
            Log.d(TAG, "Bundle: mVerificationId " + extraItem.getString("Verify").toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            inputVerifyCode(mCode);
        }


        btnResend = (TextView) findViewById(R.id.btnResend);
        btnResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPhoneNumber != null) {

                    FlagResend = true;
                    progressDialog = new ProgressDialog(AuthVerifyActivity.this);
                    progressDialog = ProgressDialog.show(AuthVerifyActivity.this, "",
                            "Loading...", true);

                    resendVerificationCode(mPhoneNumber, mResendToken);

                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }
            }
        });

        btnNext = (TextView) findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkInputVerifyCode()) {
                    // validate syntax phone number
                    mInputCode = txtNum1.getText().toString() + txtNum2.getText().toString() + txtNum3.getText().toString()
                            + txtNum4.getText().toString() + txtNum5.getText().toString() + txtNum6.getText().toString();

                    FlagResend = false;
                    progressDialog = new ProgressDialog(AuthVerifyActivity.this);
                    progressDialog = ProgressDialog.show(AuthVerifyActivity.this, "",
                            "Loading...", true);
                    if (mVerificationId != null) {
                        verifyPhoneNumberWithCode(mVerificationId, mInputCode);
                    }
                }
            }
        });

//      call firebase auth verify when next verify
        mAuth = FirebaseAuth.getInstance();
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted:" + credential);

//      check FlagResend
                if (!FlagResend) {
                    signInWithPhoneAuthCredential(credential);
                } else {
                    Log.d(TAG, "RESEND SMS FOR " + mPhoneNumber);
                    inputVerifyCode(credential.getSmsCode());

                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    Toast.makeText(AuthVerifyActivity.this, "Send SMS success!", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Snackbar.make(findViewById(android.R.id.content), "Invalid phone number!",
                            Snackbar.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded!",
                            Snackbar.LENGTH_SHORT).show();
                }
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:" + verificationId);
                mVerificationId = verificationId;
                mResendToken = token;

            }

            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Toast.makeText(AuthVerifyActivity.this, "Time-out", Toast.LENGTH_SHORT).show();
            }


        };
    }

    private void initEditText() {
        txtNum1 = (EditText) findViewById(R.id.txtNum1);
        txtNum2 = (EditText) findViewById(R.id.txtNum2);
        txtNum3 = (EditText) findViewById(R.id.txtNum3);
        txtNum4 = (EditText) findViewById(R.id.txtNum4);
        txtNum5 = (EditText) findViewById(R.id.txtNum5);
        txtNum6 = (EditText) findViewById(R.id.txtNum6);

        settingEventTextChanged();
        settingEventOnKey();
    }

    private void settingEventTextChanged() {
        try {
            txtNum1.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (txtNum1.getText().toString().trim().length() >= 1) {
                        txtNum2.requestFocus();
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 0) {
                        // do your stuff ...
                    }
                }
            });

            txtNum2.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (txtNum2.getText().toString().trim().length() >= 1) {
                        txtNum3.requestFocus();
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 0) {
                        txtNum1.requestFocus(1);
                    }
                }
            });

            txtNum3.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (txtNum3.getText().toString().trim().length() >= 1) {
                        txtNum4.requestFocus();
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 0) {
                        txtNum2.requestFocus(1);
                    }
                }
            });

            txtNum4.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (txtNum4.getText().toString().trim().length() >= 1) {
                        txtNum5.requestFocus();
                    }

                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 0) {
                        txtNum3.requestFocus(1);
                    }
                }
            });

            txtNum5.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (txtNum5.getText().toString().trim().length() >= 1) {
                        txtNum6.requestFocus();
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 0) {
                        txtNum4.requestFocus(1);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void settingEventOnKey() {
        try{
            txtNum2.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
                    if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                        txtNum1.requestFocus(1);
                    }
                    return false;
                }
            });

            txtNum3.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
                    if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                        txtNum2.requestFocus(1);
                    }
                    return false;
                }
            });

            txtNum4.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
                    if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                        txtNum3.requestFocus(1);
                    }
                    return false;
                }
            });

            txtNum5.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
                    if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                        txtNum4.requestFocus(1);
                    }
                    return false;
                }
            });
            txtNum6.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
                    if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                        txtNum5.requestFocus(1);
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean checkInputVerifyCode() {
        // validate syntax InputVerifyCode
        if (txtNum1.getText().toString().equals("")) {
            txtNum1.setError("Invalid number!");
            return false;
        } else if (txtNum2.getText().toString().equals("")) {
            txtNum2.setError("Invalid number!");
            return false;
        } else if (txtNum3.getText().toString().equals("")) {
            txtNum3.setError("Invalid number!");
            return false;
        } else if (txtNum4.getText().toString().equals("")) {
            txtNum4.setError("Invalid number!");
            return false;
        } else if (txtNum5.getText().toString().equals("")) {
            txtNum5.setError("Invalid number!");
            return false;
        } else if (txtNum6.getText().toString().equals("")) {
            txtNum6.setError("Invalid number!");
            return false;
        }
        return true;
    }

    private void inputVerifyCode(String code) {
        if (code != null) {
            txtNum1.setText(code.substring(0, 1));
            txtNum2.setText(code.substring(1, 2));
            txtNum3.setText(code.substring(2, 3));
            txtNum4.setText(code.substring(3, 4));
            txtNum5.setText(code.substring(4, 5));
            txtNum6.setText(code.substring(5, 6));
            txtNum6.setSelection(1);
        }
    }

    //Check credential and signin with phone
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithPhoneAuthCredential: SUCCESS");

                            //get user and using data user if u want
                            FirebaseUser user = task.getResult().getUser();
                            startActivity(new Intent(AuthVerifyActivity.this, InputProfileActivity.class));
                            Toast.makeText(AuthVerifyActivity.this, "Input code success!", Toast.LENGTH_LONG).show();
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            finish();
                        } else {
                            Log.d(TAG, "signInWithPhoneAuthCredential: FAIL, " + task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(AuthVerifyActivity.this, "Input code fail!", Toast.LENGTH_LONG).show();
                            }
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                        }
                    }
                });
    }


    //create credential using verificationId + code
    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    //setting resend Verify
    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                10,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }
}
