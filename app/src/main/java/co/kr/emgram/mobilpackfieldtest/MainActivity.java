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
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.AttrRes;
import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import co.kr.emgram.mobilpackfieldtest.listadapter.AdjustActivity;
import co.kr.emgram.mobilpackfieldtest.network.data.UserData;
import co.kr.emgram.mobilpackfieldtest.webview.CustomView;
import co.kr.emgram.mobilpackfieldtest.webview.WebViewActivity;

public class MainActivity extends AppCompatActivity {

    private Button kakao;
    private Button naver;
    private Button path;
    private Button date_picker;
    private Button screen_shot;
    private Button drag;
    private Button email;
    private Button scheme;
    private Button seek_bar;
    private Button roc_api_test;
    private Button gc_test;
    private Button list_adapter;
    private Button btn_webview;
    private EditText name_et, email_et, username_et;
    private LinearLayout parent_layout, view_group;
    private CustomView custom_view;
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
        scheme = findViewById(R.id.scheme);
        seek_bar = findViewById(R.id.seek_bar);
        roc_api_test = findViewById(R.id.roc_api_test);
        name_et = findViewById(R.id.name_et);
        email_et = findViewById(R.id.email_et);
        username_et = findViewById(R.id.username_et);
        gc_test = findViewById(R.id.gc_test);
        list_adapter = findViewById(R.id.list_adapter);
        btn_webview = findViewById(R.id.btn_webview);
        //DateRangeCalendarView cal = findViewById(R.id.calendar);
        parent_layout = findViewById(R.id.parent_layout);
        view_group = findViewById(R.id.view_group);
        custom_view = findViewById(R.id.custom_view);

//        LayoutInflater infl = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
//        ViewGroup group = (ViewGroup) infl.inflate(R.layout.custom_layout, view_group); //1. inflate 뷰그룹에 해주면 attachToRoot true랑 동일하게 동작
//        ViewGroup group = (ViewGroup) infl.inflate(R.layout.custom_layout, view_group, false);  //2. attachToRoot false면 addView해서 붙임.
//        ViewGroup group = (ViewGroup) infl.inflate(R.layout.custom_layout, null);   //3. root null이면 attachToRoot false랑 동일하게 동작. addView해서 붙여야 함.
//        ViewGroup group = (ViewGroup) infl.inflate(R.layout.custom_layout, null, true);   //4. root null이면 attachToRoot true 줘도 안붙음. 뷰그룹이 null이니까 어디다 addView 해줘야 함
//        view_group.addView(group);
//        view_group.addView(new CustomView(this));
//        try {
//            view_group.addView(custom_view);
//        } catch (Exception e) {
//            e.printStackTrace();  //removeView() 호출 필요
//        }
//        parent_layout.addView(new CustomView(this));

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

        seek_bar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SeekBarActivity.class);
                startActivity(intent);
            }
        });

        scheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        Intent.ACTION_VIEW, Uri.parse(
                                "nmap://route/walk?" +
                                        "slat=37.4640070&slng=126.9522394&sname=%EC%84%9C%EC%9A%B8%EB%8C%80%ED%95%99%EA%B5%90" +
                                        "&v1lat=37.4690000&v1lng=126.9580000&v1name=테스트위치" +
                                        "&dlat=37.4764356&dlng=126.9618302&dname=%EB%8F%99%EC%9B%90%EB%82%99%EC%84%B1%EB%8C%80%EC%95%84%ED%8C%8C%ED%8A%B8" +
                                        "&appname=co.kr.emgram.mobilpackfieldtest"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(intent);
            }
        });

        roc_api_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TestModel model = new TestModel();
                model.userCreate(name_et.getText().toString(),
                        email_et.getText().toString(),
                        username_et.getText().toString(),
                        new TestModel.OnMainModelListener<UserData>() {
                    @Override
                    public void onSuccess(@Nullable UserData data) {
                        Log.d("UserCreate::", data.get_id()+"\n"+data.getCreateAt()+"\n"+data.getUsername());
                    }

                    @Override
                    public void onFailure(@NotNull String message) {

                    }
                });
            }
        });

        gc_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GCTest.class);
                startActivity(intent);
            }
        });

        list_adapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AdjustActivity.class);
                startActivity(intent);
            }
        });

        btn_webview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                startActivity(intent);
            }
        });

        getAppKeyHash();
//        setTest();
    }

    private void setTest() {
        test.t = new TMapManager.OnTest() {
            @Override
            public void testing() {
                Log.d("test", "onTest");
            }
        };
        TMapManager.getInstance().setTest(test);
        TMapManager.getInstance().tasking();

        TMapManager.getInstance().setTestInterface(testInterface);
        TMapManager.getInstance().tasking2();
    }

    private void testRemove() {
        test.t = null;
    }
    private void testRemove2() {
        testInterface = null;
    }

    private TMapManager.Nested test = new TMapManager.Nested();
    private TMapManager.OnTest testInterface = new TMapManager.OnTest() {
        @Override
        public void testing() {
            Log.d("test", "onTestInterface");
        }
    };

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
