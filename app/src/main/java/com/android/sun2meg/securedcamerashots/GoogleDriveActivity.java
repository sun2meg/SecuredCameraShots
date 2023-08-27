package com.android.sun2meg.securedcamerashots;

//import android.support.v7.app.AppCompatActivity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;



    // Add this import at the beginning of your file
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
//import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthSubIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
//import com.google.api.services.drive.Drive;
//import com.google.api.services.drive.DriveScopes;
//import com.google.api.services.drive.model.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

public class GoogleDriveActivity extends AppCompatActivity
            implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    private static final int REQUEST_CODE_PERMISSIONS = 1;
        private static final int REQUEST_CODE_CAPTURE_VIDEO = 2;
    private static final int REQUEST_PERMISSIONS = 4;
        private GoogleDriveHelper googleDriveHelper;
        private String videoFilePath;
    private DriveResourceClient driveResourceClient;
    private GoogleApiClient googleApiClient;
    private String capturedVideoFilePath; // Initialize this

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_google_drive);
            requestPermissionsIfNeeded();
            googleDriveHelper = new GoogleDriveHelper(this);
            googleDriveHelper.signIn();

            // Initialize DriveResourceClient
            GoogleSignInAccount signInAccount = googleDriveHelper.getSignInAccount();
            if (signInAccount != null) {
                driveResourceClient = Drive.getDriveResourceClient(this, signInAccount);
                // Continue with the rest of your code that uses driveResourceClient
            } else {
                Toast.makeText(getApplicationContext(), "Not Signed successfully", Toast.LENGTH_SHORT).show();
                // Handle the case where the user is not signed in
                // For example, you can prompt the user to sign in or handle it based on your app's logic
            }


            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            googleApiClient.connect();

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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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


    public void captureVideo(View view) {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_CAPTURE_VIDEO);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GoogleDriveHelper.REQUEST_CODE_SIGN_IN) {
            googleDriveHelper.handleSignInResult(data);
        } else if (requestCode == REQUEST_CODE_CAPTURE_VIDEO && resultCode == RESULT_OK) {
            Uri capturedVideoUri = data.getData();
            String capturedVideoFilePath = getRealPathFromURI(capturedVideoUri);
            googleDriveHelper.uploadVideo(capturedVideoFilePath,googleApiClient);
//            googleDriveHelper.uploadVideo(capturedVideoFilePath, driveResourceClient, googleApiClient);
        }

//
//        if (requestCode == GoogleDriveHelper.REQUEST_CODE_SIGN_IN) {
//            googleDriveHelper.handleSignInResult(data);
//
//            // Initialize DriveResourceClient
//            GoogleSignInAccount signInAccount = googleDriveHelper.getSignInAccount();
////            if (signInAccount != null) {
////                driveResourceClient = Drive.getDriveResourceClient(this, signInAccount);
////            } else
////                Toast.makeText(getApplicationContext(), "Not Signed In", Toast.LENGTH_SHORT).show();
//
//        } else if (requestCode == REQUEST_CODE_CAPTURE_VIDEO && resultCode == RESULT_OK) {
//            // Retrieve the captured video's Uri from the Intent
//            Uri capturedVideoUri = data.getData();
//            // Convert the Uri to a file path using a helper method
//            capturedVideoFilePath = getRealPathFromURI(capturedVideoUri);
//            // Call the uploadVideo() method from GoogleDriveHelper
//            googleDriveHelper.uploadVideo(capturedVideoFilePath, driveResourceClient, googleApiClient);
//        }
    }


    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Video.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        }
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {

        }
    }
