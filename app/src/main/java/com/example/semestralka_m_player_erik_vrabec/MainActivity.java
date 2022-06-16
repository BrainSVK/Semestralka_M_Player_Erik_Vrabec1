package com.example.semestralka_m_player_erik_vrabec;


import static com.example.semestralka_m_player_erik_vrabec.PlayActivity.getChannelId2;
import static com.example.semestralka_m_player_erik_vrabec.PlayActivity.getDALSIA;
import static com.example.semestralka_m_player_erik_vrabec.PlayActivity.getPLAY;
import static com.example.semestralka_m_player_erik_vrabec.PlayActivity.getPREDOSLA;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private static TextView textMenoPesnicky;
    private Button predosla, nasledujuca;
    private static Button zastav;
    private String[] pesnicky;
    private PlayActivity playActivity;
    private int pozicia;
    private String nazovPesnicky;
    private static MediaPlayer mediaPlayer;
    private ArrayList<File> mojePesnicky;
    private MediaSessionCompat mediaSession;
    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getSupportActionBar().setTitle("Bloody Music Player");
        predosla = findViewById(R.id.predoslaTlacitko2);
        nasledujuca = findViewById(R.id.dalsiaTlacitko2);
        zastav = findViewById(R.id.playTlacitko2);
        listView = findViewById(R.id.listViewSong);
        textMenoPesnicky = findViewById(R.id.txtMenoPesnicky2);
        context = getApplicationContext();
        playActivity = new PlayActivity();
        mediaSession = new MediaSessionCompat(this, "PrehratHudbu");
        spustaciePovolenie();


        zastav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if (playActivity.getMediaPlayer() != null) {
                    if (playActivity.getMediaPlayer().isPlaying()) {
                        zastav.setBackgroundResource(R.drawable.ic_play);
                        ukazNotifikaciu(R.drawable.ic_play);
                    } else {
                        zastav.setBackgroundResource(R.drawable.ic_pause);
                        ukazNotifikaciu(R.drawable.ic_pause);
                    }
                    playActivity.playTlacitkoMetoda();
                }
            }
        });

        nasledujuca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playActivity.getMediaPlayer() != null)
                {
                    if (playActivity.getKontrola() == 1)
                    {
                        pozicia = playActivity.getActivity().getPozicia();
                    }
                    playActivity.setKontrola(0);
                    playActivity.getMediaPlayer().stop();
                    playActivity.getMediaPlayer().release();
                    pozicia = (pozicia + 1) % pesnicky.length;
                    Uri uri1 = Uri.parse(mojePesnicky.get(pozicia).toString());
                    nazovPesnicky = pesnicky[pozicia].toString().replace(".mp3", "").replace(".wav", "");
                    textMenoPesnicky.setText(nazovPesnicky);
                    playActivity.setMediaPLayer(MediaPlayer.create(playActivity.getAppContext(),uri1));
                    playActivity.getMediaPlayer().start();
                    zastav.setBackgroundResource(R.drawable.ic_pause);
                    playActivity.getMediaPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            nasledujuca.performClick();
                        }
                    });
                    ukazNotifikaciu(R.drawable.ic_pause);
                }
            }
        });

        predosla.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playActivity.getMediaPlayer() != null)
                {
                    if (playActivity.getKontrola() == 1)
                    {
                        pozicia = playActivity.getActivity().getPozicia();
                    }
                    playActivity.setKontrola(0);
                    playActivity.getMediaPlayer().stop();
                    playActivity.getMediaPlayer().release();
                    pozicia = (pozicia - 1) < 0 ? pesnicky.length - 1 : pozicia - 1;
                    Uri uri1 = Uri.parse(mojePesnicky.get(pozicia).toString());
                    nazovPesnicky = pesnicky[pozicia].toString().replace(".mp3", "").replace(".wav", "");
                    textMenoPesnicky.setText(nazovPesnicky);
                    playActivity.setMediaPLayer(MediaPlayer.create(playActivity.getAppContext(),uri1));
                    playActivity.getMediaPlayer().start();
                    zastav.setBackgroundResource(R.drawable.ic_pause);
                    playActivity.getMediaPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            nasledujuca.performClick();
                        }
                    });
                    ukazNotifikaciu(R.drawable.ic_pause);
                }
            }
        });

    }

    public int getPozicia()
    {
        return pozicia;
    }

    public void setPozicia(int pozicia_)
    {
        pozicia = pozicia_;
    }

    public TextView getTextMenoPesnicky()
    {
        return textMenoPesnicky;
    }

    public void setNazovPesnicky(String nazovPesnicky_)
    {
        nazovPesnicky = nazovPesnicky_;
    }

    public MainActivity getMain()
    {
        return this;
    }

    public void spustaciePovolenie()
    {
        Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        zobrazPesnicky();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();

                    }
                }).check();

    }

    public ArrayList<File> najdiSkladbu(File file)  {
        ArrayList<File> arrayList = new ArrayList<>();

        File[] files = file.listFiles();
        for (File subor :files)
        {
            if (subor.isDirectory() && !subor.isHidden())
            {
                arrayList.addAll(najdiSkladbu(subor));
            } else
            {
                if (subor.getName().endsWith(".mp3") || subor.getName().endsWith(".wav"))
                {
                    arrayList.add(subor);
                }
            }
        }
        return arrayList;
    }

    void zobrazPesnicky()
    {
        mojePesnicky = najdiSkladbu(Environment.getExternalStorageDirectory());
        pesnicky = new String[mojePesnicky.size()];
        for (int i = 0; i < mojePesnicky.size();i++)
        {
            pesnicky[i] = mojePesnicky.get(i).getName().toString().replace(".mp3","").replace(".wav","");

        }
        vlastnyAdapter adapter = new vlastnyAdapter();
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String nazovPesnicky_ = (String) listView.getItemAtPosition(i);
                startActivity(new Intent(getApplicationContext(), playActivity.getClass()).putExtra("pesnicky", mojePesnicky).putExtra("nazovPesnicky", nazovPesnicky).putExtra("pozicia", i));
                pozicia = i;
                textMenoPesnicky.setSelected(true);
                nazovPesnicky = pesnicky[i].toString().replace(".mp3","").replace(".wav","");
                textMenoPesnicky.setText(nazovPesnicky);
                playActivity.setKontrola(1);
                ukazNotifikaciu(R.drawable.ic_pause);
            }
        });

    }

    public void nastavPuasu(int i)
    {
        zastav.setBackgroundResource(i);
    }

    public void ukazNotifikaciu(int i)
    {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0, intent, 0);
        Intent predosliIntent = new Intent(this, NaPozadi.class).setAction(getPREDOSLA());
        PendingIntent predosliPendingIntent = PendingIntent.getBroadcast(context,0, predosliIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Intent dalsiaIntent = new Intent(this, NaPozadi.class).setAction(getDALSIA());
        PendingIntent dalsiaPendingIntent = PendingIntent.getBroadcast(context,0, dalsiaIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Intent playliIntent = new Intent(this, NaPozadi.class).setAction(getPLAY());
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(context,0, playliIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Bitmap obrazok = BitmapFactory.decodeResource(context.getResources(),R.drawable.node);
        Notification notification = new NotificationCompat.Builder(context, getChannelId2())
                .setSmallIcon(R.drawable.node)
                .setContentTitle(mojePesnicky.get(pozicia).getName().toString())
                .addAction(R.drawable.ic_predosli, "PREDOSLA", predosliPendingIntent)
                .addAction(i, "PLAY", playPendingIntent)
                .addAction(R.drawable.ic_dalsi, "DALSIA", dalsiaPendingIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);

    }

    class vlastnyAdapter extends BaseAdapter
    {
        @Override
        public int getCount() {
            return pesnicky.length;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View mojView = getLayoutInflater().inflate(R.layout.list_pesnicka, null);
            TextView textPesnicky = mojView.findViewById(R.id.txtMenoPesnicky);
            textPesnicky.setSelected(true);
            textPesnicky.setText(pesnicky[i]);
            return mojView;
        }
    }

}