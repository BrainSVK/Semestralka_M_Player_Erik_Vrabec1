package com.example.semestralka_m_player_erik_vrabec;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


/** Trieda NaPozadi vyuzivam BroadcastReceiver a sluzi na notifikaciu aplikacie
 * @author Erik Vrabec
 */
public class NaPozadi extends BroadcastReceiver {
    private static final String DALSIA = "DALSIA";
    private static final String PREDOSLA = "PREDOSLA";
    private static final String PLAY = "PLAY";
    private static PlayActivity playActivity;
    private Prehraj prehraj;

    public NaPozadi() {
       //playActivity = new PlayActivity();
       //prehraj = playActivity.getPlayActivity();

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null)
        {
            switch (intent.getAction())
            {
                case PREDOSLA:
                    Toast.makeText(context, "PREDOSLA", Toast.LENGTH_SHORT).show();
//                    if (prehraj != null)
//                    {
//                        prehraj.predoslaHraj();
//                    }
                    break;
                case PLAY:
                    Toast.makeText(context, "PLAY", Toast.LENGTH_SHORT).show();
//                    if (prehraj != null)
//                    {
//                        prehraj.playHraj();
//                    }
                    break;
                case DALSIA:
                    Toast.makeText(context, "DALSIA", Toast.LENGTH_SHORT).show();
//                    if (prehraj != null)
//                    {
//                        prehraj.dalsiaHraj();
//                    }
                    break;
            }
        }
    }
}
