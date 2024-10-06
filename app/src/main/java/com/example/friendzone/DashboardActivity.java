package com.example.friendzone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;

    TextView loginemail;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        firebaseAuth = FirebaseAuth.getInstance();

        //loginemail = findViewById(R.id.loginEmail);

        BottomNavigationView navigationView = findViewById(R.id.bnav);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);


        HomeFragment fragment1 = new HomeFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content,fragment1,"");
        ft1.commit();


    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId())
                    {
                        case R.id.home:

                            HomeFragment fragment1 = new HomeFragment();
                            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                            ft1.replace(R.id.content,fragment1,"");
                            ft1.commit();

                            return true;
                        case R.id.profile:

                            ProfileFragment fragment2 = new ProfileFragment();
                            FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                            ft2.replace(R.id.content,fragment2,"");
                            ft2.commit();
                            return true;
                        case R.id.users:

                            UsersFragment fragment3 = new UsersFragment();
                            FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                            ft3.replace(R.id.content,fragment3,"");
                            ft3.commit();
                            return true;

                        case R.id.settings:

                            SettingsFragment fragment4 = new SettingsFragment();
                            FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                            ft4.replace(R.id.content,fragment4,"");
                            ft4.commit();
                            return true;

                    }
                    return false;
                }
            };


    private void checkUserStatus()
    {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user!=null)
        {
           // loginemail.setText(user.getEmail());

        }
        else
        {
            startActivity(new Intent(DashboardActivity.this,LoginActivity.class));
        }

    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }


    //public void Logoutuser(View view) {

       // Intent intent = new Intent(ProfileActivity.this,LoginActivity.class);
        //firebaseAuth.signOut();
       // checkUserStatus();
        //startActivity(intent);

   // }
}