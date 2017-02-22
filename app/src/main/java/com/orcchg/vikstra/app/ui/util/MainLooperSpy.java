package com.orcchg.vikstra.app.ui.util;

import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;

import com.orcchg.vikstra.domain.util.DebugSake;

import java.lang.reflect.Field;

import timber.log.Timber;

/**
 * Allows to travers MessageQueue of main looper and output it's content.
 *
 * {@see https://medium.com/square-corner-blog/a-journey-on-the-android-main-thread-lifecycle-bits-d916bc1ee6b2#.3yo8f5f4j}
 */
@DebugSake
public class MainLooperSpy {
    private final Field messagesField;
    private final Field nextField;
    private final MessageQueue mainMessageQueue;

    public MainLooperSpy() {
        try {
            Field queueField = Looper.class.getDeclaredField("mQueue");
            queueField.setAccessible(true);
            messagesField = MessageQueue.class.getDeclaredField("mMessages");
            messagesField.setAccessible(true);
            nextField = Message.class.getDeclaredField("next");
            nextField.setAccessible(true);
            Looper mainLooper = Looper.getMainLooper();
            mainMessageQueue = (MessageQueue) queueField.get(mainLooper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void dumpQueue() {
        try {
            Message nextMessage = (Message) messagesField.get(mainMessageQueue);
            Timber.v("Begin dumping queue");
            dumpMessages(nextMessage);
            Timber.v("End dumping queue");
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void dumpMessages(Message message) throws IllegalAccessException {
        if (message != null) {
            Timber.v(message.toString());
            Message next = (Message) nextField.get(message);
            dumpMessages(next);
        }
    }
}
