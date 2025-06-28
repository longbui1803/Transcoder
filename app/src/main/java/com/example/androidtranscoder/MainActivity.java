package com.example.androidtranscoder;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.androidtranscoder.MediaTranscoder;
import com.example.androidtranscoder.format.MediaFormatPresetsFactory;
import com.example.androidtranscoder.utils.PermissionUtils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ng.bv";
    private static final int REQUEST_CODE_PICK = 1;
    private static final int PROGRESS_BAR_MAX = 1000;
    public static final File DIRECTORY_DCIM = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
    private Future<Void> mFuture;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        PermissionUtils.requestPermissions(this);

        findViewById(R.id.select_video_button).setOnClickListener(v ->
                startActivityForResult(
                        new Intent(Intent.ACTION_GET_CONTENT)
                                .setType("video/*"), REQUEST_CODE_PICK)
        );
        findViewById(R.id.cancel_button).setOnClickListener(view ->
                mFuture.cancel(true));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null || data.getData() == null) {
            return;
        }
        if (requestCode == REQUEST_CODE_PICK) {
            final File file;
            if (resultCode == RESULT_OK) {
                try {
                    File outputDir = new File(DIRECTORY_DCIM, "outputs");
                    //noinspection ResultOfMethodCallIgnored
                    outputDir.mkdir();
                    file = File.createTempFile("transcode_test", ".mp4", outputDir);
                } catch (IOException e) {
                    Log.e(TAG, "Failed to create temporary file.", e);
                    Toast.makeText(this, "Failed to create temporary file.", Toast.LENGTH_LONG).show();
                    return;
                }
                ContentResolver resolver = getContentResolver();
                final ParcelFileDescriptor parcelFileDescriptor;
                try {
                    parcelFileDescriptor = resolver.openFileDescriptor(data.getData(), "r");
                } catch (FileNotFoundException e) {
                    Log.w("Could not open '" + data.getDataString() + "'", e);
                    Toast.makeText(this, "File not found.", Toast.LENGTH_LONG).show();
                    return;
                }
                final FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                final ProgressBar progressBar = findViewById(R.id.progress_bar);
                progressBar.setMax(PROGRESS_BAR_MAX);
                final long startTime = SystemClock.uptimeMillis();
                MediaTranscoder.Listener listener = new MediaTranscoder.Listener() {
                    @Override
                    public void onTranscodeProgress(double progress) {
                        if (progress < 0) {
                            progressBar.setIndeterminate(true);
                        } else {
                            progressBar.setIndeterminate(false);
                            progressBar.setProgress((int) Math.round(progress * PROGRESS_BAR_MAX));
                        }
                    }

                    @Override
                    public void onTranscodeCompleted() {
                        Log.d(TAG, "transcoding took " + (SystemClock.uptimeMillis() - startTime) + "ms");
                        onTranscodeFinished(true, "transcoded file placed on " + file, parcelFileDescriptor);
                        Uri uri = Uri.parse(file.toString());
                        startActivity(new Intent(Intent.ACTION_VIEW)
                                .setDataAndType(uri, "video/mp4")
                                .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));
                        MediaScannerConnection.scanFile(
                                MainActivity.this,
                                new String[]{file.getAbsolutePath()},
                                new String[]{"video/mp4"},
                                (path, uri1) -> {
                                    Log.i("MediaScanner", "File scanned: " + path + ", uri: " + uri1);
                                }
                        );
                    }

                    @Override
                    public void onTranscodeCanceled() {
                        onTranscodeFinished(false, "Transcoder canceled.", parcelFileDescriptor);
                    }

                    @Override
                    public void onTranscodeFailed(Exception exception) {
                        onTranscodeFinished(false, "Transcoder error occurred.", parcelFileDescriptor);
                    }
                };
                Log.d(TAG, "transcoding into " + file);
                mFuture = MediaTranscoder.getInstance()
                        .transcodeVideo(
                                fileDescriptor,
                                file.getAbsolutePath(),
                                MediaFormatPresetsFactory
                                        .createVideo720pStrategy(8000 * 1000, -1, 1),
                                listener);
                switchButtonEnabled(true);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void onTranscodeFinished(boolean isSuccess, String toastMessage, ParcelFileDescriptor parcelFileDescriptor) {
        final ProgressBar progressBar = findViewById(R.id.progress_bar);
        progressBar.setIndeterminate(false);
        progressBar.setProgress(isSuccess ? PROGRESS_BAR_MAX : 0);
        switchButtonEnabled(false);
        Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
        try {
            parcelFileDescriptor.close();
        } catch (IOException e) {
            Log.w("Error while closing", e);
        }
    }

    private void switchButtonEnabled(boolean isProgress) {
        findViewById(R.id.select_video_button).setEnabled(!isProgress);
        findViewById(R.id.cancel_button).setEnabled(isProgress);
    }
}



