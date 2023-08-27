package com.android.sun2meg.securedcamerashots;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

//import javax.annotation.Nullable;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import java.io.IOException;
import java.io.OutputStream;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_VIDEO_CAPTURE = 1;
    private static final int RC_SIGN_IN = 2;
    private static final int REQUEST_PERMISSIONS = 3;

    private GoogleSignInAccount googleSignInAccount;
    private DriveClient driveClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnCaptureVideo = findViewById(R.id.btnCaptureVideo);
        btnCaptureVideo.setOnClickListener(v -> startVideoCapture());


        // Request necessary permissions on startup
        requestPermissionsIfNeeded();

        // Initialize Google Sign-In options
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Drive.SCOPE_FILE)
                .build();

        // Build a GoogleSignInClient
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, signInOptions);



        // Start the sign-in process
        startActivityForResult(googleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    private void requestPermissionsIfNeeded() {
        String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.GET_ACCOUNTS
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



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            uploadVideoToDrive(videoUri);
        } else if (requestCode == RC_SIGN_IN) {
            // Handle the result of the sign-in process
            Task<GoogleSignInAccount> signInTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                googleSignInAccount = signInTask.getResult(ApiException.class);
                if (googleSignInAccount != null) {
                    driveClient = Drive.getDriveClient(this, googleSignInAccount);
                    Log.d("GoogleSignIn", "Sign-in successful: " + googleSignInAccount.getEmail());
                    showMessage("success login");
                } else {
                    Log.e("GoogleSignIn", "Sign-in failed");
                    showMessage("failure login");
                }
            } catch (ApiException e) {
                Log.e("GoogleSignIn", "Sign-in failed with error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showMessage(String message) {
        // Display the message to the user (e.g., using a Toast)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void uploadVideoToDrive(Uri videoUri) {
        if (googleSignInAccount != null) {
            Drive.getDriveResourceClient(this, googleSignInAccount)
                    .getRootFolder()
                    .continueWithTask((Continuation<DriveFolder, Task<DriveFile>>) task -> {
                        if (!task.isSuccessful()) {
                            throw new Exception("Unable to get root folder");
                        }

                        // Create a MetadataChangeSet to specify the file details
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle("MyFile.txt")
                                .setMimeType("text/plain")
                                .build();

                        // Create a new DriveContents
                        DriveContents driveContents = Drive.getDriveResourceClient(this, googleSignInAccount)
                                .createContents()
                                .getResult();

                        // Write data to DriveContents
                        OutputStream outputStream = driveContents.getOutputStream();
                        // Write your file content to outputStream
                        // ...

                        // Create the file within the root folder
                        return Drive.getDriveResourceClient(this, googleSignInAccount)
                                .createFile(task.getResult(), changeSet, driveContents);
                    })
                    .addOnSuccessListener(driveFile -> {
                        // File uploaded successfully
                        showMessage("File uploaded to Google Drive");
                    })
                    .addOnFailureListener(e -> {
                        // Handle upload failure
                        showMessage("Failed to upload file to Google Drive: " + e.getMessage());
                    });

        } else {
            // Handle authentication issue or show appropriate message to the user
            showSignInDialog();

        }
    }
    private void showSignInDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Authentication Required")
                .setMessage("Please sign in to your Google account to upload the video to Google Drive.")
                .setPositiveButton("Sign In", (dialog, which) -> {
                    startSignInProcess();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Handle cancel or show additional information to the user
                })
                .show();
    }

    private void startSignInProcess() {
        // Initialize Google Sign-In options
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Drive.SCOPE_FILE)
                .build();

        // Build a GoogleSignInClient
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, signInOptions);

        // Start the sign-in process
        startActivityForResult(googleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }
}