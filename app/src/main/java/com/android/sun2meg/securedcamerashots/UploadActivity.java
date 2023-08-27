package com.android.sun2meg.securedcamerashots;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

public class UploadActivity extends AppCompatActivity {
    DriveClient mDriveClient;
    DriveResourceClient mDriveResourceClient;
    GoogleSignInAccount googleSignInAccount;
    String TAG = "Drive";
    private final int REQUEST_CODE_CREATOR = 2013;
    private static final int REQUEST_PERMISSIONS = 3;
    private static final int REQUEST_VIDEO_CAPTURE = 1;
    Task<DriveContents> createContentsTask;
//    String uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        // Request necessary permissions on startup

        Button btnCaptureVideo = findViewById(R.id.btnCaptureVideo);
        btnCaptureVideo.setOnClickListener(v -> startVideoCapture());
        requestPermissionsIfNeeded();
        //Fetching uri or path from previous activity.
//        uri = getIntent().getStringExtra("uriVideo");
        //Get previously signed in account.
        googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (googleSignInAccount != null) {
            mDriveClient = Drive.getDriveClient(getApplicationContext(), googleSignInAccount);
            mDriveResourceClient =
                    Drive.getDriveResourceClient(getApplicationContext(), googleSignInAccount);
        } else
            Toast.makeText(this, "Login again and retry", Toast.LENGTH_SHORT).show();
        createContentsTask = mDriveResourceClient.createContents();
        findViewById(R.id.btnCaptureVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              startVideoCapture();
            }
        });
    }

    private void createFile(Uri uri) {
        // [START create_file]
        final Task<DriveFolder> rootFolderTask = mDriveResourceClient.getRootFolder();
        final Task<DriveContents> createContentsTask = mDriveResourceClient.createContents();
        Tasks.whenAll(rootFolderTask, createContentsTask)
                .continueWithTask(new Continuation<Void, Task<DriveFile>>() {
                    @Override
                    public Task<DriveFile> then(@NonNull Task<Void> task) throws Exception {
                        DriveFolder parent = rootFolderTask.getResult();
                        DriveContents contents = createContentsTask.getResult();
                        String path = getRealPathFromURI(uri);
                        File file = new File(path);

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buf = new byte[1024];
                        FileInputStream fis = new FileInputStream(file);
                        for (int readNum; (readNum = fis.read(buf)) != -1;) {
                            baos.write(buf, 0, readNum);
                        }
                        OutputStream outputStream = contents.getOutputStream();
                        outputStream.write(baos.toByteArray());

                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle("MyVideo.mp4") // Provide you video name here
                                .setMimeType("video/mp4") // Provide you video type here
                                .build();

                        return mDriveResourceClient.createFile(parent, changeSet, contents);
                    }
                })
                .addOnSuccessListener(this,
                        new OnSuccessListener<DriveFile>() {
                            @Override
                            public void onSuccess(DriveFile driveFile) {
                                Toast.makeText(UploadActivity.this, "Upload Started", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Unable to create file", e);
                        finish();
                    }
                });
        // [END create_file]
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            // Image captured, upload to Google Drive
            Uri imageUri = data.getData();
          createFile(imageUri);
        }
    }

    private void requestPermissionsIfNeeded() {
        String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    showPermissionDeniedDialog();
                    return;
                }
            }
        }
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permission Denied")
                .setMessage("Some permissions were denied. The app may not function properly without them.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void startVideoCapture() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }



}