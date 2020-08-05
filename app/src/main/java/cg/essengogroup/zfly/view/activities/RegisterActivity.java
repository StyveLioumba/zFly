package cg.essengogroup.zfly.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import cg.essengogroup.zfly.R;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText nom,tel,nomArtiste;
    private Switch isArtiste;
    private CardView btnIns,btnVerification;
    private ProgressBar progressBar;
    private boolean isArtisteValue=false;
    private String telValue,nomValue,genreArtistValue,nomArtisteValue;
    private CountryCodePicker codeNum;

    private Spinner spinner;
    private String[] genreList={
            "D.J","Beatmaker"
    };
    private LinearLayout linearLayout,lineEnvoie,lineVerification;
    private String codeSysteme;
    private PinView pinView;
    private FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser()!=null){
            startActivity(new Intent(RegisterActivity.this,AccueilActivity.class));
            finish();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mAuth=FirebaseAuth.getInstance();

        btnIns=findViewById(R.id.btnRegister);
        btnVerification=findViewById(R.id.btnVerification);
        progressBar=findViewById(R.id.progress);
        nom=findViewById(R.id.nom);
        nomArtiste=findViewById(R.id.nomArtiste);
        tel=findViewById(R.id.tel);
        isArtiste=findViewById(R.id.statementArtiste);
        linearLayout=findViewById(R.id.lineBottom);
        lineVerification=findViewById(R.id.lineVerification);
        lineEnvoie=findViewById(R.id.lineEnvoie);
        codeNum=findViewById(R.id.ccpCode);
        pinView=findViewById(R.id.pinView);

        spinner=findViewById(R.id.spinnerArtist);
        ArrayAdapter adapter=new ArrayAdapter(RegisterActivity.this,android.R.layout.simple_list_item_1,android.R.id.text1,genreList);
        spinner.setAdapter(adapter);

        btnIns.setOnClickListener(v->signUpMethode());
        btnVerification.setOnClickListener(v-> actionVerificationAndGoToNextActivity());//methode declancheuse

        isArtiste.setOnClickListener(v->{
            if (isArtiste.isChecked()){
                linearLayout.setVisibility(View.VISIBLE);
            }else {
                linearLayout.setVisibility(View.GONE);
            }
        });
    }

    private void signUpMethode(){
        nomValue=nom.getText().toString().trim();
        telValue="+"+codeNum.getSelectedCountryCode()+tel.getText().toString().trim();
        genreArtistValue=spinner.getSelectedItem().toString();
        nomArtisteValue=nomArtiste.getText().toString().trim();

        if (isArtiste.isChecked()){
            isArtisteValue=true;
        }

        if (TextUtils.isEmpty(nomValue)){
            nom.setError("champ requis");
            nom.requestFocus();
            return;
        }

        if (isArtiste.isChecked() && TextUtils.isEmpty(nomArtisteValue)){
            nomArtiste.setError("champ requis");
            nomArtiste.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        lineEnvoie.setVisibility(View.GONE);
        lineVerification.setVisibility(View.VISIBLE);

        sendVerificationCodeToUser(telValue);


    }

    /* Methode 1 */ private void sendVerificationCodeToUser(String numTel){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                numTel,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                TaskExecutors.MAIN_THREAD,               // Activity (for callback binding)
                mCallbacks);
    }

    /* Methode 2 */ private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =new PhoneAuthProvider
            .OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            codeSysteme=s;
        }

        @Override
        public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
            super.onCodeAutoRetrievalTimeOut(s);
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            String code=phoneAuthCredential.getSmsCode();
            if (!TextUtils.isEmpty(code)){
                pinView.setText(code);
                verificationCodeRecu(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            lineEnvoie.setVisibility(View.VISIBLE);
            lineVerification.setVisibility(View.GONE);
        }
    };

    /* Methode 3 */ private void verificationCodeRecu(String codeRecu){
        PhoneAuthCredential credential=PhoneAuthProvider.getCredential(codeSysteme,codeRecu);
        signInWithPhoneAuthCredential(credential);
    }

    /* Methode 4 */private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = task.getResult().getUser();
                            Intent intent=new Intent(getApplicationContext(),UserPhotoActivity.class);
                            intent.putExtra("nom",nomValue);
                            intent .putExtra("tel",telValue);
                            intent .putExtra("pays",codeNum.getSelectedCountryName());
                            intent .putExtra("artiste",isArtisteValue);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            if (isArtisteValue){
                                intent.putExtra("genreArtiste",genreArtistValue);
                                intent.putExtra("nomArtiste",nomArtisteValue);
                            }
                            startActivity(intent);
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                progressBar.setVisibility(View.GONE);
                                lineEnvoie.setVisibility(View.VISIBLE);
                                lineVerification.setVisibility(View.GONE);
                                Toast.makeText(RegisterActivity.this, "Connexion échoué", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    /* Methode 5 */ private void actionVerificationAndGoToNextActivity(){
        String code=pinView.getText().toString().trim();
        if (!TextUtils.isEmpty(code)){
            verificationCodeRecu(code);
        }
    }
}
