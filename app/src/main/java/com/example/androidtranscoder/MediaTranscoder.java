
package com.example.androidtranscoder;

import android.media.MediaFormat;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.androidtranscoder.engine.MediaTranscoderEngine;
import com.example.androidtranscoder.format.MediaFormatPresets;
import com.example.androidtranscoder.format.MediaFormatStrategy;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class MediaTranscoder {
    private static final String TAG = "MediaTranscoder";
    private static final int MAXIMUM_THREAD = 1; // TODO
    private static volatile MediaTranscoder sMediaTranscoder;
    private final ThreadPoolExecutor mExecutor;

    private MediaTranscoder() {
        mExecutor = new ThreadPoolExecutor(
                0, MAXIMUM_THREAD, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                r -> new Thread(r, "MediaTranscoder-Worker"));
    }

    public static MediaTranscoder getInstance() {
        if (sMediaTranscoder == null) {
            synchronized (MediaTranscoder.class) {
                if (sMediaTranscoder == null) {
                    sMediaTranscoder = new MediaTranscoder();
                }
            }
        }
        return sMediaTranscoder;
    }

    /**
     * Transcodes video file asynchronously.
     * Audio track will be kept unchanged.
     *
     * @param inPath            File path for input.
     * @param outPath           File path for output.
     * @param outFormatStrategy Strategy for output video format.
     * @param listener          Listener instance for callback.
     * @throws IOException if input file could not be read.
     */
//    public Future<Void> transcodeVideo(final String inPath, final String outPath, final MediaFormatStrategy outFormatStrategy, final Listener listener) throws IOException {
//        FileInputStream fileInputStream = null;
//        FileDescriptor inFileDescriptor;
//        try {
//            fileInputStream = new FileInputStream(inPath);
//            inFileDescriptor = fileInputStream.getFD();
//        } catch (IOException e) {
//            if (fileInputStream != null) {
//                try {
//                    fileInputStream.close();
//                } catch (IOException eClose) {
//                    Log.e(TAG, "Can't close input stream: ", eClose);
//                }
//            }
//            throw e;
//        }
//        final FileInputStream finalFileInputStream = fileInputStream;
//        return transcodeVideo(inFileDescriptor, outPath, outFormatStrategy, new Listener() {
//            @Override
//            public void onTranscodeProgress(double progress) {
//                listener.onTranscodeProgress(progress);
//            }
//
//            @Override
//            public void onTranscodeCompleted() {
//                closeStream();
//                listener.onTranscodeCompleted();
//            }
//
//            @Override
//            public void onTranscodeCanceled() {
//                closeStream();
//                listener.onTranscodeCanceled();
//            }
//
//            @Override
//            public void onTranscodeFailed(Exception exception) {
//                closeStream();
//                listener.onTranscodeFailed(exception);
//            }
//
//            private void closeStream() {
//                try {
//                    finalFileInputStream.close();
//                } catch (IOException e) {
//                    Log.e(TAG, "Can't close input stream: ", e);
//                }
//            }
//        });
//    }

    /**
     * Transcodes video file asynchronously.
     * Audio track will be kept unchanged.
     *
     * @param inFileDescriptor  FileDescriptor for input.
     * @param outPath           File path for output.
     * @param outFormatStrategy Strategy for output video format.
     * @param listener          Listener instance for callback.
     */
    public Future<Void> transcodeVideo(final FileDescriptor inFileDescriptor, final String outPath, final MediaFormatStrategy outFormatStrategy, final Listener listener) {
        Looper looper = Looper.myLooper();
        if (looper == null) looper = Looper.getMainLooper();
        final Handler handler = new Handler(looper);
        final AtomicReference<Future<Void>> futureReference = new AtomicReference<>();
        final Future<Void> createdFuture = mExecutor.submit(() -> {
            Exception caughtException = null;
            try {
                MediaTranscoderEngine engine = new MediaTranscoderEngine();
                // TODO: reuse instance
                engine.setProgressCallback(progress -> handler.post(() ->
                        listener.onTranscodeProgress(progress)));
                engine.setDataSource(inFileDescriptor);
                // MediaTranscoderEngine transcodeVideo
                engine.transcodeVideo(outPath, outFormatStrategy);
            } catch (IOException e) {
                Log.w(TAG, "Transcode failed: input file (fd: " + inFileDescriptor.toString() + ") not found"
                        + " or could not open output file ('" + outPath + "') .", e);
                caughtException = e;
            } catch (InterruptedException e) {
                Log.i(TAG, "Cancel transcode video file.", e);
                caughtException = e;
            } catch (RuntimeException e) {
                Log.e(TAG, "Fatal error while transcoding, this might be invalid format or bug in engine or Android.", e);
                caughtException = e;
            }

            final Exception exception = caughtException;
            handler.post(() -> {
                if (exception == null) {
                    listener.onTranscodeCompleted();
                } else {
                    Future<Void> future = futureReference.get();
                    if (future != null && future.isCancelled()) {
                        listener.onTranscodeCanceled();
                    } else {
                        listener.onTranscodeFailed(exception);
                    }
                }
            });

            if (exception != null) throw exception;
            return null;
        });
        futureReference.set(createdFuture);
        return createdFuture;
    }

    public interface Listener {
        /**
         * Called to notify progress.
         *
         * @param progress Progress in [0.0, 1.0] range, or negative value if progress is unknown.
         */
        void onTranscodeProgress(double progress);

        /**
         * Called when transcode completed.
         */
        void onTranscodeCompleted();

        /**
         * Called when transcode canceled.
         */
        void onTranscodeCanceled();

        /**
         * Called when transcode failed.
         *
         * @param exception Exception thrown from {@link MediaTranscoderEngine#transcodeVideo(String, MediaFormatStrategy)}.
         *                  Note that it IS NOT {@link java.lang.Throwable}. This means {@link java.lang.Error} won't be caught.
         */
        void onTranscodeFailed(Exception exception);
    }
}



