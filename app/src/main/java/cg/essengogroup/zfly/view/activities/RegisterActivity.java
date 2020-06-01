package cg.essengogroup.zfly.view.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import cg.essengogroup.zfly.R;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText email,password,nom,prenom,pseudo,tel;
    private Switch isArtiste;
    private CardView btnIns;
    private ProgressBar progressBar;
    private boolean isArtisteValue=false;
    private String telValue=null;

    private FirebaseAuth mAuth;
    private Spinner spinner;
    private String[] genreList={
     "D.J","Beatmaker"
    };
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mAuth = FirebaseAuth.getInstance();

        btnIns=findViewById(R.id.btnRegister);
        email=findViewById(R.id.email);
        password=findViewById(R.id.password);
        progressBar=findViewById(R.id.progress);
        nom=findViewById(R.id.nom);
        prenom=findViewById(R.id.prenom);
        pseudo=findViewById(R.id.pseudo);
        tel=findViewById(R.id.tel);
        isArtiste=findViewById(R.id.statementArtiste);
        linearLayout=findViewById(R.id.lineBottom);

        spinner=findViewById(R.id.spinnerArtist);
        ArrayAdapter adapter=new ArrayAdapter(RegisterActivity.this,android.R.layout.simple_list_item_1,android.R.id.text1,genreList);
        spinner.setAdapter(adapter);

        btnIns.setOnClickListener(v->signUpMethode());
        findViewById(R.id.loginActivity).setOnClickListener(v->{
            startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
            finish();
        });
        isArtiste.setOnClickListener(v->{
            if (isArtiste.isChecked()){
                linearLayout.setVisibility(View.VISIBLE);
            }else {
                linearLayout.setVisibility(View.GONE);
            }
        });
    }

    private void signUpMethode(){
        String emailValue=email.getText().toString().trim();
        String passwordValue=password.getText().toString().trim();
        String nomValue=nom.getText().toString().trim();
        String prenomValue=prenom.getText().toString().trim();
        telValue=tel.getText().toString().trim();
        String pseudoValue=pseudo.getText().toString().trim();
        String genreArtistValue=spinner.getSelectedItem().toString();

        if (isArtiste.isChecked()){
            isArtisteValue=true;
        }

        if (TextUtils.isEmpty(nomValue)){
            nom.setError("veuillez entré votre nom");
            nom.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(prenomValue)){
            prenom.setError("veuillez entré votre prénom");
            prenom.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(pseudoValue)){
            pseudo.setError("veuillez entré votre pseudo");
            pseudo.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(emailValue)){
            email.setError("veuillez entré votre adresse mail");
            email.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()){
            email.setError("veuillez entré un adresse mail valide");
            email.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(passwordValue)){
            password.setError("veuillez entré un mot de passe");
            password.requestFocus();
            return;
        }

        if (password.length()<6){
            password.setError("veuillez entré un mot de passe superieur ou égal à 6 caracteres ");
            password.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(emailValue,passwordValue).addOnCompleteListener(task -> {

            if (progressBar.getVisibility()==View.VISIBLE){
                progressBar.setVisibility(View.GONE);
            }

            if (task.isSuccessful()){
                Intent intent=new Intent(getApplicationContext(),UserPhotoActivity.class);
                intent.putExtra("nom",nomValue);
                intent.putExtra("prenom",prenomValue);
                intent .putExtra("tel",telValue);
                intent.putExtra("pseudo",pseudoValue);
                intent .putExtra("artiste",isArtisteValue);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                if (isArtisteValue){
                    intent.putExtra("genreArtiste",genreArtistValue);
                }
                startActivity(intent);
            }else {
                if (task.getException() instanceof FirebaseAuthUserCollisionException){
                    Toast.makeText(this, "vous etes déjà ernregistre", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
