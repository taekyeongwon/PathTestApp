package com.example.maplangtest;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {

    private Button kakao;
    private Button naver;
    private Button path;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        kakao = findViewById(R.id.btn_kakao);
        naver = findViewById(R.id.btn_naver);
        path = findViewById(R.id.btn_path);

        kakao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent kIntent = new Intent(MainActivity.this, KakaoMapActivity.class);
                startActivity(kIntent);
            }
        });

        naver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent nIntent = new Intent(MainActivity.this, NaverMapActivity.class);
                startActivity(nIntent);
            }
        });

        path.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TMapManager.getInstance().getSucceeded()) {
                    Intent pIntent = new Intent(MainActivity.this, PathFindActivity.class);
                    startActivity(pIntent);
                }
            }
        });

        getAppKeyHash();

    }


    private void getAppKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.example.maplangtest", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
