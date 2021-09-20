/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.messenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

public class MusicPlayerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
            if (intent.getExtras() == null) {
                return;
            }
            KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
            if (keyEvent == null) {
                return;
            }
            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN)
                return;

            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    if (MediaController.getInstance().isMessagePaused()) {
                        MediaController.getInstance().playMessage(MediaController.getInstance().getPlayingMessageObject());
                    } else {
                        MediaController.getInstance().pauseMessage(MediaController.getInstance().getPlayingMessageObject());
                    }
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    MediaController.getInstance().playMessage(MediaController.getInstance().getPlayingMessageObject());
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    MediaController.getInstance().pauseMessage(MediaController.getInstance().getPlayingMessageObject());
                    break;
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    MediaController.getInstance().playNextMessage();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    MediaController.getInstance().playPreviousMessage();
                    break;
            }
        } else {
            switch (intent.getAction()) {
                case MusicPlayerService.NOTIFY_PLAY:
                    MediaController.getInstance().playMessage(MediaController.getInstance().getPlayingMessageObject());
                    break;
                case MusicPlayerService.NOTIFY_PAUSE:
                case android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                    MediaController.getInstance().pauseMessage(MediaController.getInstance().getPlayingMessageObject());
                    break;
                case MusicPlayerService.NOTIFY_NEXT:
                    MediaController.getInstance().playNextMessage();
                    break;
                case MusicPlayerService.NOTIFY_CLOSE:
                    MediaController.getInstance().cleanupPlayer(true, true);
                    break;
                case MusicPlayerService.NOTIFY_PREVIOUS:
                    MediaController.getInstance().playPreviousMessage();
                    break;
            }
        }
    }
}
