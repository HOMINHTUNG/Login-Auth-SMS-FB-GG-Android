package com.adsmove.customerlogin.Register;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.adsmove.customerlogin.Login.LoginActivity;
import com.adsmove.customerlogin.MainActivity;
import com.adsmove.customerlogin.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

/**
 * Created by HOMINHTUNG-PC on 4/16/2018.
 */

public class PhoneAuthActivity extends AppCompatActivity {

    private EditText txtPhoneNumber;
    private TextView btnNext;
    private TextView btnLogin;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseUser mCurrentUser;
    private String mPhoneNumber;
    private String mVerificationId;

    private static final String TAG = "PhoneAuthActivity";
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_phone);

        txtPhoneNumber = (EditText) findViewById(R.id.btnNumberPhone);
        txtPhoneNumber.requestFocus();

        btnNext = (TextView) findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!validatePhoneNumber()) {
                    return;
                }

                progressDialog = new ProgressDialog(PhoneAuthActivity.this);
                progressDialog = ProgressDialog.show(PhoneAuthActivity.this, "",
                        "Loading...", true);
                startPhoneNumberVerification(mPhoneNumber);
            }
        });

        btnLogin = (TextView) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PhoneAuthActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

//      call firebase auth when load verify
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {

                // send sms completed, app will send data sms for Activity verify
                Log.d(TAG, "SEND COMPLETED SMS FOR " + mPhoneNumber);

                Intent intent = new Intent(PhoneAuthActivity.this, AuthVerifyActivity.class);
                intent.putExtra("PhoneNumber", mPhoneNumber);
                Log.d(TAG, "Bundle: PhoneNumber " + mPhoneNumber);
                intent.putExtra("Verify", mVerificationId);
                Log.d(TAG, "Bundle: Verify  " + mVerificationId);
                intent.putExtra("Code", credential.getSmsCode());
                Log.d(TAG, "Bundle: Code " + credential.getSmsCode());
                intent.putExtra("Token", mResendToken);
                Log.d(TAG, "Bundle: Token " + mResendToken);

                startActivity(intent);
                Toast.makeText(PhoneAuthActivity.this,"Send SMS success!",Toast.LENGTH_LONG).show();
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                finish();
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                // send sms failed, app will show log and snackbar
                Log.d(TAG, "onVerificationFailed", e);
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    txtPhoneNumber.setError("Invalid phone number!");
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

                // support resend code in next step
                Log.d(TAG, "onCodeSent:" + verificationId);
                mVerificationId = verificationId;
                mResendToken = token;

                Intent intent = new Intent(PhoneAuthActivity.this, AuthVerifyActivity.class);
                intent.putExtra("PhoneNumber", mPhoneNumber);
                Log.d(TAG, "Bundle: PhoneNumber " + mPhoneNumber);
                intent.putExtra("Verify", mVerificationId);
                Log.d(TAG, "Bundle: Verify  " + mVerificationId);
                intent.putExtra("Token", mResendToken);
                Log.d(TAG, "Bundle: Token " + mResendToken);

                startActivity(intent);
                Toast.makeText(PhoneAuthActivity.this,"Send SMS success!",Toast.LENGTH_LONG).show();
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                finish();
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Toast.makeText(PhoneAuthActivity.this, "Time-out", Toast.LENGTH_SHORT).show();
            }


        };
    }

    private void startPhoneNumberVerification(String mPhoneNumber) {

        // setting call firebase auth
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mPhoneNumber,        // Phone number to verify
                10,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks

    }

    private boolean validatePhoneNumber() {
        // validate syntax phone number
        mPhoneNumber = txtPhoneNumber.getText().toString();
        if (TextUtils.isEmpty(mPhoneNumber)) {
            txtPhoneNumber.setError("Invalid null phone number!");
            return false;
        } else {
            if (mPhoneNumber.substring(0, 1).equals("0")) {
                mPhoneNumber = mPhoneNumber.substring(1).toString();
            }
            mPhoneNumber = "+84"+mPhoneNumber;
            if (mPhoneNumber.length() < 12 || mPhoneNumber.length() > 13) {
                txtPhoneNumber.setError("Invalid size phone number!");
                return false;
            }
            //   else if(mCurrentUser.getPhoneNumber().equals(mPhoneNumber)){
            //       txtPhoneNumber.setError("Invalid exist phone number!");
            //      return false;
            //  }
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        if ( mCurrentUser != null )
        {
            if(mCurrentUser.getEmail().toString()!=null){
                startActivity( new Intent( this, MainActivity.class ) );
                Toast.makeText(this,"Login success",Toast.LENGTH_LONG).show();
                finish();
            }else{
                startActivity( new Intent( this, InputProfileActivity.class ) );
                Toast.makeText(this,"Login success",Toast.LENGTH_LONG).show();
                finish();
            }

        }
    }

}