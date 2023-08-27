package com.android.sun2meg.securedcamerashots;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.CreateFileActivityOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;



import android.graphics.Color;
//import android.support.design.widget.Snackbar;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.material.snackbar.Snackbar;


/*** REMEMBER TO ENABLE Google Drive API
 * https://console.developers.google.com
 * you can contact me to get a client_id.json or Client ID for testing @ jorgesys12@gmail.com
 */

public class UploadToDriveActivity extends AppCompatActivity  implements ConnectionCallbacks,
        OnConnectionFailedListener {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    private static final int REQUEST_CODE_CREATOR = 2;
    private static final int REQUEST_CODE_RESOLUTION = 3;
    private boolean isAPÏConnected;
    private GoogleApiClient mGoogleApiClient;
    private Bitmap mBitmapToSave;
        private static final int REQUEST_PERMISSIONS = 4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_to_drive);
        requestPermissionsIfNeeded();
        //Initialize Google Drive API Client!
//        connectAPIClient();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        findViewById(R.id.btnCaptureVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Start camera to take a picture
//                if(isAPÏConnected) {
                    startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_CODE_CAPTURE_IMAGE);
//                }else{
//                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Error Google API is disable or permissions are required!", Snackbar.LENGTH_LONG)
//                            .setActionTextColor(Color.RED);
//
//                    View snackbarView = snackbar.getView();
//                    snackbarView.setBackgroundColor(Color.DKGRAY);
//                    TextView textView = (TextView)findViewById(R.id.textView);
//                    textView.setTextColor(Color.RED);
//                    snackbar.show();
//
//                }
            }
        });
    }

    //Create a new file and save it to Drive.
    private void saveFileToDrive() {
        // Start by creating a new contents, and setting a callback.
        Log.i(TAG, "saveFileToDrive() Creating new content.");
        Toast.makeText(this, "saveFileToDrive() Creating new content.", Toast.LENGTH_SHORT).show();
        final Bitmap image = mBitmapToSave;
        Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(new ResultCallback<DriveContentsResult>() {
            @Override
            public void onResult(DriveContentsResult result) {
                // If the operation wasn't successful, return
                if (!result.getStatus().isSuccess()) {
                    Log.e(TAG, "Failed to create new content!.");
                    Toast.makeText(getApplicationContext(), "Failed to create new content!.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.i(TAG, "New content has been created.");
                Toast.makeText(getApplicationContext(), "New content has been created.", Toast.LENGTH_SHORT).show();
                // Get an output stream for the contents.
                OutputStream outputStream = result.getDriveContents().getOutputStream();
                // Write the bitmap data from it.
                ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream);
                try {
                    outputStream.write(bitmapStream.toByteArray());
                } catch (IOException e1) {
                    Log.i(TAG, "Unable to write file contents.");
                    Toast.makeText(getApplicationContext(), "Unable to write file contents.", Toast.LENGTH_SHORT).show();
                }
                // Create the initial metadata - MIME type and title.
                // Note that the user will be able to change the title later.
                MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                        .setMimeType("image/jpeg").setTitle("myPhoto.png").build();
                // Create an intent for the file chooser, and start it.
                IntentSender intentSender = Drive.DriveApi
                        .newCreateFileActivityBuilder()
                        .setInitialMetadata(metadataChangeSet)
                        .setInitialDriveContents(result.getDriveContents())
                        .build(mGoogleApiClient);
                try {
                    startIntentSenderForResult(
                            intentSender, REQUEST_CODE_CREATOR, null, 0, 0, 0);
                } catch (SendIntentException e) {
                    Log.i(TAG, "Failed to launch file chooser.");
                }
            }
        });
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions,  int[] grantResults) {
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



    @Override
    protected void onResume() {
        super.onResume();
    }

    //Disconnect only when the application is closed!
    @Override
    protected void onDestroy() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_CAPTURE_IMAGE:
                // Called after a photo has been taken.
                if (resultCode == Activity.RESULT_OK) {
                    // Store the image data as a bitmap for writing later.
                    mBitmapToSave = (Bitmap) data.getExtras().get("data");
                    saveFileToDrive();
                }
                break;
            case REQUEST_CODE_CREATOR:
                //Called after a file is saved to Drive.
                if (resultCode == RESULT_OK) { //succesfully saved!.
                    Log.i(TAG, "Image successfully saved.");
                    mBitmapToSave = null;
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Photo succesfully saved to Google Drive!", Snackbar.LENGTH_LONG)
                            .setActionTextColor(Color.RED);

                    View snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(Color.DKGRAY);
                    TextView textView = (TextView)findViewById(R.id.textView);
                    textView.setTextColor(Color.YELLOW);
                    snackbar.show();

                }
                break;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        isAPÏConnected = false;
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }
        // Called typically when the app is not yet authorized, and authorization dialog is displayed to the user.
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity. " + e.getMessage());
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "* API client connected !!!.");
        isAPÏConnected = true;
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended.");
    }

    private void connectAPIClient(){
        if (mGoogleApiClient == null) {
            Log.i(TAG, "connectAPIClient().");
            // Create the API client and bind it to an instance variable.
            // We use this instance as the callback for connection and connection
            // failures.
            // Since no account name is passed, the user is prompted to choose.
//            googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
//            if (googleSignInAccount != null) {
//                mDriveClient = Drive.getDriveClient(getApplicationContext(), googleSignInAccount);
//                mDriveResourceClient =
//                        Drive.getDriveResourceClient(getApplicationContext(), googleSignInAccount);
//            } else
//                Toast.makeText(this, "Login again and retry", Toast.LENGTH_SHORT).show();

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        // Connect the client. Once connected, the camera is launched.
        mGoogleApiClient.connect();
    }

}




//public class UploadToDriveActivity extends AppCompatActivity {
//
//    private static final String TAG = "UploadToDriveActivity";
//    private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
//
//    private GoogleApiClient googleApiClient;
//
//    private static final int REQUEST_PERMISSIONS = 3;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_upload_to_drive);
//        requestPermissionsIfNeeded();
//        Button btnCaptureVideo = findViewById(R.id.btnCaptureVideo);
//        btnCaptureVideo.setOnClickListener(v -> captureImage());
//        // Initialize Google API client
//        googleApiClient = new GoogleApiClient.Builder(this)
//                .addApi(Drive.API)
//                .addScope(Drive.SCOPE_FILE)
//                .addOnConnectionFailedListener(result -> {
//                    // Connection failed
//                    Log.i(TAG, "GoogleApiClient connection failed");
//                    if (!result.hasResolution()) {
//                        // Show the localized error dialog.
//                        // GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
//                        return;
//                    }
//                    try {
//                        result.startResolutionForResult(this, 0);
//                    } catch (IntentSender.SendIntentException e) {
//                        Log.e(TAG, "Exception while starting resolution activity", e);
//                    }
//                })
//                .build();
//    }
//
//
//        private void requestPermissionsIfNeeded() {
//        String[] permissions = {
//                Manifest.permission.CAMERA,
//                Manifest.permission.RECORD_AUDIO,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.GET_ACCOUNTS
//        };
//
//        boolean allPermissionsGranted = true;
//        for (String permission : permissions) {
//            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
//                allPermissionsGranted = false;
//                break;
//            }
//        }
//
//        if (!allPermissionsGranted) {
//            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_PERMISSIONS) {
//            for (int result : grantResults) {
//                if (result != PackageManager.PERMISSION_GRANTED) {
//                    showPermissionDeniedDialog();
//                    return;
//                }
//            }
//        }
//    }
//
//
//    private void showPermissionDeniedDialog() {
//        new AlertDialog.Builder(this)
//                .setTitle("Permission Denied")
//                .setMessage("Some permissions were denied. The app may not function properly without them.")
//                .setPositiveButton("OK", null)
//                .show();
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        googleApiClient.connect();
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        if (googleApiClient != null) {
//            googleApiClient.disconnect();
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_CODE_CAPTURE_IMAGE && resultCode == RESULT_OK) {
//            // Image captured, upload to Google Drive
//            Uri imageUri = data.getData();
//            uploadImageToDrive(imageUri);
//        }
//    }
//
//    private void captureImage() {
//        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivityForResult(intent, REQUEST_CODE_CAPTURE_IMAGE);
//    }
//
//
//    private void uploadImageToDrive(Uri imageUri) {
//        DriveFolder rootFolder = Drive.DriveApi.getRootFolder(googleApiClient);
//
//        // Create the metadata for the file
//        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
//                .setMimeType("image/jpeg")
//                .setTitle("MyImage.jpg") // Set your desired title here
//                .build();
//
//        // Create the file on Drive
//        rootFolder.createFile(googleApiClient, changeSet, null)
//                .setResultCallback(driveFileResult -> {
//                    if (!driveFileResult.getStatus().isSuccess()) {
//                        Log.e(TAG, "Error creating file: " + driveFileResult.getStatus());
//                        return;
//                    }
//
//                    DriveFile driveFile = driveFileResult.getDriveFile();
//
//                    driveFile.open(googleApiClient, DriveFile.MODE_WRITE_ONLY, null)
//                            .setResultCallback(driveContentsResult -> {
//                                if (!driveContentsResult.getStatus().isSuccess()) {
//                                    Log.e(TAG, "Error opening DriveContents: " + driveContentsResult.getStatus());
//                                    return;
//                                }
//
//                                DriveContents driveContents = driveContentsResult.getDriveContents();
//                                OutputStream outputStream = driveContents.getOutputStream();
//
//                                try {
//                                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
//                                    byte[] buffer = new byte[4096];
//                                    int bytesRead;
//                                    while ((bytesRead = inputStream.read(buffer)) != -1) {
//                                        outputStream.write(buffer, 0, bytesRead);
//                                    }
//                                    inputStream.close();
//                                    outputStream.close();
//                                } catch (IOException e) {
//                                    Log.e(TAG, "Error reading or writing file contents.", e);
//                                    return;
//                                }
//
//                                // Commit the contents and finish
//                                driveContents.commit(googleApiClient, null)
//                                        .setResultCallback(commitResult -> {
//                                            if (!commitResult.getStatus().isSuccess()) {
//                                                Log.e(TAG, "Error committing contents: " + commitResult.getStatus());
//                                            } else {
//                                                Log.i(TAG, "Image uploaded successfully.");
//                                            }
//                                        });
//                            });
//                });
//    }
//
//
//}

//
//public class UploadToDriveActivity extends AppCompatActivity {
//
//    private static final String TAG = "UploadToDriveActivity";
//    private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
//    private static final int REQUEST_PERMISSIONS = 3;
//    private GoogleApiClient googleApiClient;
//
//    private void requestPermissionsIfNeeded() {
//        String[] permissions = {
//                Manifest.permission.CAMERA,
//                Manifest.permission.RECORD_AUDIO,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.GET_ACCOUNTS
//        };
//
//        boolean allPermissionsGranted = true;
//        for (String permission : permissions) {
//            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
//                allPermissionsGranted = false;
//                break;
//            }
//        }
//
//        if (!allPermissionsGranted) {
//            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_PERMISSIONS) {
//            for (int result : grantResults) {
//                if (result != PackageManager.PERMISSION_GRANTED) {
//                    showPermissionDeniedDialog();
//                    return;
//                }
//            }
//        }
//    }
//
//
//    private void showPermissionDeniedDialog() {
//        new AlertDialog.Builder(this)
//                .setTitle("Permission Denied")
//                .setMessage("Some permissions were denied. The app may not function properly without them.")
//                .setPositiveButton("OK", null)
//                .show();
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_upload_to_drive);
//        // Request necessary permissions on startup
//        requestPermissionsIfNeeded();
//        Button btnCaptureVideo = findViewById(R.id.btnCaptureVideo);
//        btnCaptureVideo.setOnClickListener(v -> captureImage());
//        // Initialize Google API client
//        googleApiClient = new GoogleApiClient.Builder(this)
//                .addApi(Drive.API)
//                .addScope(Drive.SCOPE_FILE)
//                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
//                    @Override
//                    public void onConnected(@Nullable Bundle bundle) {
//                        // API client connected
////                        captureImage();
//                    }
//
//                    @Override
//                    public void onConnectionSuspended(int i) {
//                        // Connection suspended
//                        Log.i(TAG, "GoogleApiClient connection suspended");
//                    }
//                })
//                .addOnConnectionFailedListener(result -> {
//                    // Connection failed
//                    Log.i(TAG, "GoogleApiClient connection failed");
//                    if (!result.hasResolution()) {
//                        // Show the localized error dialog.
//                        // GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
//                        return;
//                    }
//                    try {
//                        result.startResolutionForResult(this, 0);
//                    } catch (IntentSender.SendIntentException e) {
//                        Log.e(TAG, "Exception while starting resolution activity", e);
//                    }
//                })
//                .build();
//    }
//
////    @Override
////    protected void onStart() {
////        super.onStart();
////        googleApiClient.connect();
////    }
////
////    @Override
////    protected void onStop() {
////        super.onStop();
////        if (googleApiClient != null) {
////            googleApiClient.disconnect();
////        }
////    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_CODE_CAPTURE_IMAGE && resultCode == RESULT_OK) {
//            // Image captured, upload to Google Drive
//            Uri imageUri = data.getData();
//            uploadImageToDrive(imageUri);
//        }
//    }
//
//    private void captureImage() {
//        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivityForResult(intent, REQUEST_CODE_CAPTURE_IMAGE);
//    }
//
////    private void uploadImageToDrive2(Uri imageUri) {
////        DriveFolder rootFolder = Drive.DriveApi.getRootFolder(googleApiClient);
////
////        // Create the metadata for the file
////        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
////                .setMimeType("image/jpeg")
////                .setTitle("MyImage.jpg") // Set your desired title here
////                .build();
////
////        // Create the file on Drive
////        rootFolder.createFile(googleApiClient, changeSet, null)
////                .addOnCompleteListener(this, task -> {
////                    if (task.isSuccessful()) {
////                        DriveContents driveContents = task.getResult().getDriveContents();
////                        OutputStream outputStream = driveContents.getOutputStream();
////
////                        try {
////                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
////                            byte[] buffer = new byte[4096];
////                            int bytesRead;
////                            while ((bytesRead = inputStream.read(buffer)) != -1) {
////                                outputStream.write(buffer, 0, bytesRead);
////                            }
////                            inputStream.close();
////                            outputStream.close();
////                        } catch (IOException e) {
////                            Log.e(TAG, "Error reading or writing file contents.", e);
////                            return;
////                        }
////
////                        // Commit the contents and finish
////                        driveContents.commit(googleApiClient, null)
////                                .addOnCompleteListener(this, commitTask -> {
////                                    if (commitTask.isSuccessful()) {
////                                        Log.i(TAG, "Image uploaded successfully.");
////                                    } else {
////                                        Log.e(TAG, "Error committing contents.", commitTask.getException());
////                                    }
////                                });
////                    } else {
////                        Log.e(TAG, "Error creating file.", task.getException());
////                    }
////                });
////    }
//
//    private void uploadImageToDrive(Uri imageUri) {
//        Drive.DriveApi.newDriveContents(googleApiClient).setResultCallback(
//                new ResultCallback<DriveApi.DriveContentsResult>() {
//                    @Override
//                    public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
//                        if (!driveContentsResult.getStatus().isSuccess()) {
//                            Log.i(TAG, "Failed to create new contents.");
//                            return;
//                        }
//
//                        DriveContents driveContents = driveContentsResult.getDriveContents();
//                        OutputStream outputStream = driveContents.getOutputStream();
//
//                        try {
//                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
//                            byte[] buffer = new byte[4096];
//                            int bytesRead;
//                            while ((bytesRead = inputStream.read(buffer)) != -1) {
//                                outputStream.write(buffer, 0, bytesRead);
//                            }
//                            inputStream.close();
//                            outputStream.close();
//                        } catch (IOException e) {
//                            Log.e(TAG, "Error reading or writing file contents.", e);
//                            Toast.makeText(getApplicationContext(), String.valueOf(e), Toast.LENGTH_SHORT).show();
//                            return;
//                        }
//
//                        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
//                                .setMimeType("image/jpeg")
//                                .setTitle("MyImage.jpg") // Set your desired title here
//                                .build();
//
//                        DriveFolder rootFolder = Drive.DriveApi.getRootFolder(googleApiClient);
//                        rootFolder.createFile(googleApiClient, metadataChangeSet, driveContents)
//                                .setResultCallback(result -> {
//                                    if (result.getStatus().isSuccess()) {
//                                        Log.i(TAG, "Image uploaded successfully: " + result.getDriveFile().getDriveId());
//                                        Toast.makeText(getApplicationContext(), "uploaded", Toast.LENGTH_SHORT).show();
//                                    } else {
//                                        Log.e(TAG, "Error uploading image: " + result.getStatus());
//                                        Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//                    }
//                });
//    }
//
//
//}
