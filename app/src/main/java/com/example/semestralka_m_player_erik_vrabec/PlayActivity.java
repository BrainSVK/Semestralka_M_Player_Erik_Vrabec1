package com.example.semestralka_m_player_erik_vrabec;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.media.Image;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;

/** Aktivita PlayActivity ma funkciu mediaPlayer rovnako ako MainActivity lenze tu vyuziva layout jednej pesnicky kde ma viacej funkcii s danou pesnickou nez v MainActivite
 * vyuziva sa tu aj seekBar, Animacie , vizualizer a celkovo tato trieda je skor spravena po dizajnovej stranke
 * @author Erik Vrabec
 */
public class PlayActivity extends AppCompatActivity implements Prehraj{
    private static Button playTlacitko, dalsiaTlacitko, predoslaTlacitko, pretocDopreduTlacitko, pretocDozaduTlacitko;
    private TextView txtStart, txtStop, txtMenoPesnicky;
    private SeekBar seekBar;
    private BarVisualizer vizualizer;
    private ImageView image;
    private NaPozadi naPozadi;

    public static String getChannelId1() {
        return CHANNEL_ID1;
    }

    public static String getChannelId2() {
        return CHANNEL_ID2;
    }

    public static String getDALSIA() {
        return DALSIA;
    }

    public static String getPREDOSLA() {
        return PREDOSLA;
    }

    public static String getPLAY() {
        return PLAY;
    }

    private String nazovPesnicky;
    private static final String CHANNEL_ID1 = "CHANNEL_1";
    private static final String CHANNEL_ID2 = "CHANNEL_2";
    private static final String DALSIA = "DALSIA";
    private static final String PREDOSLA = "PREDOSLA";
    private static final String PLAY = "PLAY";
    private static MediaPlayer mediaPlayer;
    private int pozicia;
    private static ArrayList<File> mojePesnicky;
    private Thread obnovenySeekBar;
    private int pocetPozicii;
    private static Context context;
    private static MainActivity activity;
    private int kontrola;
    private static MediaSessionCompat mediaSession;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home)
        {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (vizualizer != null)
        {
            vizualizer.release();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed()
    {
        activity.setPozicia(pozicia);
        activity.setNazovPesnicky(nazovPesnicky);
        activity.getTextMenoPesnicky().setText(nazovPesnicky);
        activity.getTextMenoPesnicky().setSelected(true);
        if (!this.getMediaPlayer().isPlaying())
        {
            activity.nastavPuasu(R.drawable.ic_play);
        }
        super.onBackPressed();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        vytvorNotifikaciu();

        getSupportActionBar().setTitle("Pocuvate");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        activity = new MainActivity();
        activity = activity.getMain();
        mediaSession = new MediaSessionCompat(this, "PrehratHudbu");
        playTlacitko = findViewById(R.id.playTlacitko);
        dalsiaTlacitko = findViewById(R.id.dalsiaTlacitko);
        predoslaTlacitko = findViewById(R.id.predoslaTlacitko);
        pretocDopreduTlacitko = findViewById(R.id.pretocDopreduTlacitko);
        pretocDozaduTlacitko = findViewById(R.id.pretocDozaduTlacitko);
        txtMenoPesnicky = findViewById(R.id.txtTw);
        txtStart = findViewById(R.id.txtStart);
        txtStop = findViewById(R.id.txtStop);
        seekBar = findViewById(R.id.seekBar);
        vizualizer = findViewById(R.id.blast);
        image = findViewById(R.id.imageView);
        PlayActivity.context = getApplicationContext();

        if (mediaPlayer != null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        Intent i = getIntent();
        Bundle bundle = i.getExtras();

        mojePesnicky = (ArrayList) bundle.getParcelableArrayList("pesnicky");
        String nazovPesnicky_ = i.getStringExtra("nazovPesnicky");
        pozicia = bundle.getInt("pozicia",0);
        txtMenoPesnicky.setSelected(true);
        Uri uri = Uri.parse(mojePesnicky.get(pozicia).toString());
        System.out.println(mojePesnicky.get(pozicia).toString());
        nazovPesnicky = mojePesnicky.get(pozicia).getName().replace(".mp3", "").replace(".wav", "");
        txtMenoPesnicky.setText(nazovPesnicky);
        pocetPozicii = mojePesnicky.size();


        mediaPlayer = MediaPlayer.create(getAppContext(), uri);
        mediaPlayer.start();

        nastavPesnicku();

        playTlacitko.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if (mediaPlayer.isPlaying())
                {
                    playTlacitko.setBackgroundResource(R.drawable.ic_play);
                    ukazNotifikaciu(R.drawable.ic_play);
                } else
                {
                    playTlacitko.setBackgroundResource(R.drawable.ic_pause);
                    ukazNotifikaciu(R.drawable.ic_pause);
                }
                playTlacitkoMetoda();
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                dalsiaTlacitko.performClick();
            }
        });

        int zvukovaStopa = mediaPlayer.getAudioSessionId();
        if (zvukovaStopa != -1)
        {
            vizualizer.setAudioSessionId(zvukovaStopa);
        }

        dalsiaTlacitko.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dalsiaTlacidloMetoda();
            }
        });

        predoslaTlacitko.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                predoslaTlacidloMetoda();
            }
        });

        pretocDopreduTlacitko.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pretocDopreduTlacitkoMetoda();
            }
        });

        pretocDozaduTlacitko.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pretocDozaduTlacitkoMetoda();
            }
        });
    }

    /**
     * void metóda vytvorNotifikaciu nam vytvorí notifikaciu
     */
    private void vytvorNotifikaciu() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID1, "CHANNEL_1",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Channel 1 desc");

            NotificationChannel notificationChannel2 = new NotificationChannel(CHANNEL_ID2, "CHANNEL_2",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationChannel2.setDescription("Channel 2 desc");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
            notificationManager.createNotificationChannel(notificationChannel2);

        }
    }

    /**
     * void metóda dalsiaTlacidloMetoda nam posunie pesnicku o 1 dalej ak je posladna tak prvu
     */
    public void dalsiaTlacidloMetoda()
    {
        mediaPlayer.stop();
        mediaPlayer.release();
        pozicia = (pozicia + 1) % getPocetMojePesnicy();
        Uri uri1 = Uri.parse(mojePesnicky.get(pozicia).toString());
        mediaPlayer = MediaPlayer.create(getAppContext(), uri1);
        nazovPesnicky = mojePesnicky.get(pozicia).getName().replace(".mp3", "").replace(".wav", "");
        txtMenoPesnicky.setText(nazovPesnicky);
        mediaPlayer.start();
        playTlacitko.setBackgroundResource(R.drawable.ic_pause);
        startAnimacie(image,1);
        int zvukovaStopa = mediaPlayer.getAudioSessionId();
        if (zvukovaStopa != -1)
        {
            vizualizer.setAudioSessionId(zvukovaStopa);
        }
        nastavPesnicku();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                dalsiaTlacitko.performClick();
            }
        });
        activity.setPozicia(pozicia);
        activity.setNazovPesnicky(nazovPesnicky);
        activity.getTextMenoPesnicky().setText(nazovPesnicky);
        activity.getTextMenoPesnicky().setSelected(true);
        ukazNotifikaciu(R.drawable.ic_pause);
    }

    /**
     * void metóda predoslaTlacidloMetoda nam posunie pesnicku o 1 dozadu ak je prva tak poslednu
     */
    public void predoslaTlacidloMetoda()
    {
        mediaPlayer.stop();
        mediaPlayer.release();
        pozicia = pozicia-1 < 0 ? mojePesnicky.size()-1 : pozicia-1;
        Uri uri1 = Uri.parse(mojePesnicky.get(pozicia).toString());
        mediaPlayer = MediaPlayer.create(getAppContext(), uri1);
        nazovPesnicky = mojePesnicky.get(pozicia).getName().replace(".mp3", "").replace(".wav", "");
        txtMenoPesnicky.setText(nazovPesnicky);
        mediaPlayer.start();
        playTlacitko.setBackgroundResource(R.drawable.ic_pause);
        startAnimacie(image,2);
        int zvukovaStopa = mediaPlayer.getAudioSessionId();
        if (zvukovaStopa != -1)
        {
            vizualizer.setAudioSessionId(zvukovaStopa);
        }
        nastavPesnicku();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                dalsiaTlacitko.performClick();
            }
        });
        ukazNotifikaciu(R.drawable.ic_pause);
    }

    /**
     * void metoda playTlacidloMetoda
     */
    public void playTlacitkoMetoda()
    {
        if (mediaPlayer.isPlaying())
        {
            mediaPlayer.pause();
        } else
        {
            mediaPlayer.start();
        }
    }

    /**
     * void metoda pretocDozaduTlacitkoMetoda pretoci pesnicko o 5 sekund naspat
     */
    public void pretocDozaduTlacitkoMetoda()
    {
        if (mediaPlayer.isPlaying())
        {
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 5000);
        }
    }

    /**
     * void metoda pretocDopreduTlacitkoMetoda pretoci pesnicko o 5 sekund dopredu
     */
    public void pretocDopreduTlacitkoMetoda()
    {
        if (mediaPlayer.isPlaying());
        {
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 5000);
        }
    }

    /**
     * void metoda startAnimacie nam pusti animacie oka podla parametru rodacia
     * @param rotacia ak 1 tak dorpedu ak inac tak dozadu
     */
    public void startAnimacie(View view, int rotacia)
    {
        ObjectAnimator animator;
        if (rotacia == 1)
        {
            animator = ObjectAnimator.ofFloat(image,"rotation",0f, 720f);
        }
        else
        {
            animator = ObjectAnimator.ofFloat(image,"rotation",720f, 0f);
        }
        animator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }

    /**
     * String metoda vytvorCas vytvori cas pesnicky
     * @return String vytvori retazec casu napr. "2:54"
     * @param dlzkaCas vladame cas v mili sekundach
     */
    public String vytvorCas(int dlzkaCas)
    {
        String cas = "";
        int min = dlzkaCas/1000/60;
        int sek = dlzkaCas/1000%60;

        cas += min + ":";
        if (sek < 10)
        {
            cas += "0";
        }
        cas += sek;

        return cas;
    }

    /**
     * void metoda nastavPesnicku nastavuje seekBar a jeho zivotnyCyklus
     */
    public void nastavPesnicku()
    {
        obnovenySeekBar = new Thread()
        {
            @Override
            public void run() {
                super.run();
                int dlzkaPesnicky = mediaPlayer.getDuration();
                int aktualnaPozicia = 0;

                while (aktualnaPozicia < dlzkaPesnicky)
                {
                    try {
                        aktualnaPozicia = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(aktualnaPozicia);
                        sleep(1000);
                    }
                    catch (InterruptedException | IllegalStateException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        };

        seekBar.setMax(mediaPlayer.getDuration());
        obnovenySeekBar.start();
        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(com.gauravk.audiovisualizer.R.color.av_red), PorterDuff.Mode.MULTIPLY);
        seekBar.getThumb().setColorFilter(getResources().getColor(com.gauravk.audiovisualizer.R.color.av_red), PorterDuff.Mode.SRC_IN);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        String koniecCasu = vytvorCas(mediaPlayer.getDuration());
        txtStop.setText(koniecCasu);

        final Handler handler = new Handler();
        final int delay = 1000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String aktualnyCas = vytvorCas(mediaPlayer.getCurrentPosition());
                txtStart.setText(aktualnyCas);
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    /**
     * void metoda ukazNotofikaciu nam vytvori aktualnu notifikaciu s parametrami, ktore do nej vkladame
     * @param i je ic_drawable a buď pause alebo play podla coho potrebujeme
     */
    public void ukazNotifikaciu(int i)
    {
        Intent intent = new Intent(this, PlayActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0, intent, 0);
        Intent predosliIntent = new Intent(this, NaPozadi.class).setAction(PREDOSLA);
        PendingIntent predosliPendingIntent = PendingIntent.getBroadcast(context,0, predosliIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Intent dalsiaIntent = new Intent(this, NaPozadi.class).setAction(DALSIA);
        PendingIntent dalsiaPendingIntent = PendingIntent.getBroadcast(context,0, dalsiaIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Intent playliIntent = new Intent(this, NaPozadi.class).setAction(PLAY);
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(context,0, playliIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Bitmap obrazok = BitmapFactory.decodeResource(context.getResources(),R.drawable.node);
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID2)
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

    /**
     * String metoda startAnimacie nam pusti animacie oka podla parametru rodacia
     * @return String
     */
    public String getNazovPesnicky() {
        return nazovPesnicky;
    }

    /**
     * MediaPlayer metoda getMediaPlayer
     * @return MediaPlayer vrati nam mediaPlayer
     */
    public MediaPlayer getMediaPlayer()
    {
        return mediaPlayer;
    }

    /**
     * void metoda setMediaPlayer
     * @param  mediaPlayer_ zadame na co chceme nastavit mediaPlayer
     */
    public void setMediaPLayer(MediaPlayer mediaPlayer_)
    {
        mediaPlayer = mediaPlayer_;
    }

    /**
     * int metoda getPozicia
     * @return int vrati nam poziciu
     */
    public int getPozicia()
    {
        return pozicia;
    }

    /**
     * void metoda setPozicia
     * @param pozicia_ nastavi nam poziciu
     */
    public void setPozicia(int pozicia_)
    {
        pozicia = pozicia_;
    }

    /**
     * int metoda getPocetMojePesnicky
     * @return int pocetPozicii
     */
    public int getPocetMojePesnicy()
    {
        return pocetPozicii;
    }

    /**
     * String metoda getMenoPesnicky
     * @return String vrati nam getMenoPesnicky
     */
    public String getMenoPesnicky()
    {
        return txtMenoPesnicky.getText().toString();
    }

    /**
     * Context metoda getAppContext
     * @return Context vrati nam context triedy/aktivity PlayActivity
     */
    public Context getAppContext()
    {
        return PlayActivity.context;
    }

    /**
     * MainActivity metoda getActivity
     * @return MainActivity vrati nam MainActivity
     */
    public MainActivity getActivity()
    {
        return activity;
    }

    /**
     * int metoda getKontrola
     * @return int vrati nam kontolu
     */
    public int getKontrola()
    {
        return kontrola;
    }

    /**
     * void metoda setKontrola
     * @param kontrola_ prepise nam kontkolu
     */
    public void setKontrola(int kontrola_)
    {
        kontrola = kontrola_;
    }

    /**
     * PlayActivity metoda getPlayActivity
     * @return PlayActivity vrati nam this cize PlayActivity
     */
    public PlayActivity getPlayActivity()
    {
        return this;
    }


    @Override
    public void playHraj() {
        if (mediaPlayer.isPlaying())
        {
            playTlacitko.setBackgroundResource(R.drawable.ic_play);
            ukazNotifikaciu(R.drawable.ic_play);
        } else
        {
            playTlacitko.setBackgroundResource(R.drawable.ic_pause);
            ukazNotifikaciu(R.drawable.ic_pause);
        }
        playTlacitkoMetoda();
    }

    @Override
    public void predoslaHraj() {
        predoslaTlacidloMetoda();
    }

    @Override
    public void dalsiaHraj() {
        dalsiaTlacidloMetoda();
    }
}