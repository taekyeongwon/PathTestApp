package co.kr.emgram.mobilpackfieldtest;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;

import androidx.annotation.AttrRes;
import androidx.appcompat.app.AppCompatActivity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {

    private Button kakao;
    private Button naver;
    private Button path;
    private Button date_picker;
    private Button screen_shot;
    private Button drag;
    private Button email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        kakao = findViewById(R.id.btn_kakao);
        naver = findViewById(R.id.btn_naver);
        path = findViewById(R.id.btn_path);
        date_picker = findViewById(R.id.date_picker);
        screen_shot = findViewById(R.id.screen_shot);
        drag = findViewById(R.id.drag);
        email = findViewById(R.id.email);
        //DateRangeCalendarView cal = findViewById(R.id.calendar);

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

        date_picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomDialog dialog = new CustomDialog(MainActivity.this);
                dialog.show();
            }
        });

        screen_shot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sIntent = new Intent(MainActivity.this, ScreenshotActivity.class);
                startActivity(sIntent);
            }
        });

        drag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DragActivity.class);
                startActivity(intent);
            }
        });

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent emailSelectorIntent = new Intent( Intent.ACTION_SENDTO );
//                emailSelectorIntent.setData( Uri.parse( "mailto:" ) );
//
//                final Intent emailIntent = new Intent( Intent.ACTION_SEND );
//                emailIntent.putExtra( Intent.EXTRA_EMAIL, new String[]{ "qwesc1234@naver.com" } );
//                //emailIntent.addFlags( Intent.FLAG_GRANT_READ_URI_PERMISSION );
//                //emailIntent.addFlags( Intent.FLAG_GRANT_WRITE_URI_PERMISSION );
//                emailIntent.setSelector( emailSelectorIntent );
//
//                if ( emailIntent.resolveActivity( getPackageManager( ) ) != null ){
//                    startActivity( emailIntent );
//                }
                Intent emailSelector = new Intent(Intent.ACTION_SENDTO);
                emailSelector.setData(Uri.parse("mailto:"));
                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{ "qwesc1234@naver.com" });
                email.putExtra(Intent.EXTRA_SUBJECT, "test");
                email.putExtra(Intent.EXTRA_TEXT, "testestest");
                //email.setType("plain/text");
                email.setSelector(emailSelector);
                if(email.resolveActivity(getPackageManager()) != null)
                    startActivity(email);
            }
        });

        getAppKeyHash();

    }

    private int resolver(@AttrRes int attributeResId) {
        TypedValue value = new TypedValue();
        if(getTheme().resolveAttribute(attributeResId, value, true)) {
            return value.data;
        }
        throw new IllegalArgumentException();
    }
    private void getAppKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo("co.kr.emgram.mobilpackfieldtest", PackageManager.GET_SIGNATURES);
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
