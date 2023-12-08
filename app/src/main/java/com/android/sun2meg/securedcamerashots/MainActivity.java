package com.android.sun2meg.securedcamerashots;

//import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;


import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
//import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import net.steamcrafted.loadtoast.LoadToast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_CODE_SIGN_IN = 1;
    private static final int PICK_FILE_REQUEST = 100;
    private static final int REQUEST_CODE_CAPTURE_VIDEO = 2;
    private static final int REQUEST_AUTHORIZATION = 5;
    static GoogleDriveServiceHelper mDriveServiceHelper;
    static String folderId="";

    private Button signInButton;

    private Button folderFilesButton;

    private Button signOutButton;
    GoogleSignInClient googleSignInClient;
    LoadToast loadToast;
    private ProgressBar progressBar;
    private TextView progressText;
    private Uri capturedImageUri;
    private Uri capturedVideoUri;
    private boolean captureInProgress = false;
    private static final int REQUEST_ACCOUNT_HINT = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 1002;
    private static final int REQUEST_VIDEO_CAPTURE = 1003;
    private static final int REQUEST_IMAGE_GALLERY = 1004;
    private static final int REQUEST_VIDEO_GALLERY = 1005;
    private static final int REQUEST_PERMISSIONS = 1006;

    private CardView btnResendImg;
    private CardView btnResendVid;
    private CardView btnSelectImage;
    private CardView btnSelectVideo;
    private CardView btnCaptureVideo;
    private CardView btnCaptureImage;
//    private ImageView btnResendImg;
//    private ImageView btnResendVid;
//    private ImageView btnSelectImage;
//    private ImageView btnSelectVideo;
//    private ImageView btnCaptureVideo;
//    private ImageView btnCaptureImage;
    private String selectedFilePath;
    private String selectedFilePath2;
    private static final long NETWORK_CHECK_INTERVAL = 5000; // 5 seconds
    private static final long INITIAL_NETWORK_CHECK_DELAY = 0; // 0 milliseconds
    private static final String PREFS_NAME = "MyPrefsFile"; // SharedPreferences file name
    private static final String PREF_SIGNED_IN = "isSignedIn";
    private static final String PREF_ACCOUNT_NAME = "accountName";

    private boolean isSignedIn = false;
    private String accountName = "";
    private int backPressCount = 0;
    private GoogleApiClient googleApiClient;
    TextView userLogged;

    private ProgressDialog mProgressDialog;
    private String currentPhotoPath;
    private Uri photoURI;
//    DriveResourceClient driveResourceClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         showProgressDialog();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ConnectivityManager manager = (ConnectivityManager) getApplicationContext()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
                if (null != activeNetwork) {
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) { }
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) { }
                } else {

                    Intent intent = new Intent(MainActivity.this, NoInternetActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    finish();

                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(runnable, 1000);

        // Start checking for network connectivity with a delay
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                checkNetworkConnectivityWithTimeout();
//            }
//        }, INITIAL_NETWORK_CHECK_DELAY);


// Check for permissions

        String[] requiredPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

        List<String> missingPermissions = new ArrayList<>();

        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }

        if (!missingPermissions.isEmpty()) {
            // Request permissions if not granted
            ActivityCompat.requestPermissions(this,
                    missingPermissions.toArray(new String[0]),
                    REQUEST_PERMISSIONS);
        } else {
            // Permissions are already granted, proceed with capturing the image
            Toast.makeText(getApplicationContext(), "All permissions are granted!", Toast.LENGTH_SHORT).show();
//            requestSignIn();
        }

/////////////////////////////////perm 1
//if (!hasRequiredPermissions()) {
//    requestForStoragePermission();
//}

        signInButton = findViewById(R.id.id_sign_in);
        folderFilesButton = findViewById(R.id.id_folder_files);
        signOutButton = findViewById(R.id.id_sign_out);

        btnCaptureImage = findViewById(R.id.btn_capture_image);
        btnCaptureVideo = findViewById(R.id.btn_capture_video);
        btnResendImg = findViewById(R.id.btnResendImg);
        btnResendVid = findViewById(R.id.btnResendVid);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSelectVideo = findViewById(R.id.btnSelectVideo);

        loadToast = new LoadToast(this);
        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);
//        progressBar.setMax(100);
        userLogged = findViewById(R.id.textVw);



        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, connectionResult -> {
                    // Handle connection failure, if needed
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Check if the user was previously signed in
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        isSignedIn = settings.getBoolean(PREF_SIGNED_IN, false);
        accountName = settings.getString(PREF_ACCOUNT_NAME, "");
        userLogged.setText("Logged as: "+ accountName);

///////////////////////////////////////////

//        GoogleSignInOptions signInOptions =
//                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                        .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
//                        .requestEmail()
//                        .build();
//        googleSignInClient = GoogleSignIn.getClient(this, signInOptions);

        /////////////////////////////////
        if (isSignedIn) {
            // User was previously signed in, so you can set up your app accordingly.
            GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(this);
            if (googleAccount != null && googleAccount.getEmail().equals(accountName)) {
                // Initialize mDriveServiceHelper using the existing GoogleSignInAccount
                initializeDriveServiceHelper(googleAccount);
            } else {
                // Handle the case where the stored accountName doesn't match the signed-in account.
                // You may want to re-prompt the user to sign in.
                requestSignIn();
            }

        } else {
            // User needs to sign in. Display the sign-in button or UI.
            signInButton.setEnabled(true);
        }


        btnCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                        captureImage();

            }
        });

        btnCaptureVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                    if (!captureInProgress) {
//                        captureInProgress = true;
                        captureVideo();
            }
        });
        btnResendImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mDriveServiceHelper != null) {


                    if (currentPhotoPath != null) {
                        // Continue with your code to upload or process the captured image
                        selectedFilePath2 = currentPhotoPath;
                        resendImg();
//                    if (capturedImageUri != null) {
//                        resendImg(capturedImageUri);
                    } else {
                        Toast.makeText(MainActivity.this, "No media captured", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Not Signed", Toast.LENGTH_SHORT).show();

                }

            }
        });

        btnResendVid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDriveServiceHelper != null) {
                    if (capturedVideoUri != null) {
                        resendVid(capturedVideoUri);
                    } else {
                        Toast.makeText(MainActivity.this, "No media captured", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Not Signed", Toast.LENGTH_SHORT).show();

                }

            }
        });

        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDriveServiceHelper != null) {
                    selectImageFromGallery();
                } else {
                    Toast.makeText(getApplicationContext(), "Not Signed", Toast.LENGTH_SHORT).show();

                }

            }
        });

        btnSelectVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDriveServiceHelper != null) {
                selectVideoFromGallery();
                } else {
                    Toast.makeText(getApplicationContext(), "Not Signed", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
    private Handler handler = new Handler(Looper.getMainLooper());

    // Method to check network connectivity
    private void checkNetworkConnectivity() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting()) {
            // Network is available
//            showNetworkAvailableMessage();
        } else {
            // Network is not available
            showNetworkUnavailableMessage();
        }
    }



    private void showNetworkUnavailableMessage() {
        // Handle network unavailable case
        // This method will be called if the network is unavailable
        Intent intent = new Intent(MainActivity.this, NoInternetActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
        Toast.makeText(getApplicationContext(), "Network outage", Toast.LENGTH_SHORT).show();
    }

    private void checkNetworkConnectivityWithTimeout() {
        checkNetworkConnectivity();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkNetworkConnectivityWithTimeout();
            }
        }, NETWORK_CHECK_INTERVAL); // Schedule the next check
    }


    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS) {
            if (areAllPermissionsGranted(grantResults)) {
                // All permissions granted, proceed with capturing the image
                Toast.makeText(getApplicationContext(), "All permissions are granted!", Toast.LENGTH_SHORT).show();
                requestSignIn();
            } else {
                // Permissions denied, show a message or handle accordingly
                Toast.makeText(this, "Permissions denied. Cannot capture .", Toast.LENGTH_SHORT).show();
                hideProgressDialog();
           requestForStoragePermission();

            }
        }

    }

    // Helper method to check if all requested permissions are granted
    private boolean areAllPermissionsGranted(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    ///////perm2
//    private boolean hasRequiredPermissions() {
//        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
//                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
//                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
//    }

    private void captureImage() {
        if (mDriveServiceHelper != null) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    Toast.makeText(this, "Error while creating image file.", Toast.LENGTH_SHORT).show();
                }

                // Continue only if the File was successfully created
                if (photoFile != null) {
                  photoURI = FileProvider.getUriForFile(this,
                            "com.android.sun2meg.securedcamerashots.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), "Not Signed", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private static final String KEY_IMAGE_URI = "image_uri";


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (capturedImageUri != null) {
            outState.putString(KEY_IMAGE_URI, capturedImageUri.toString());
        }
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String savedImageUri = savedInstanceState.getString(KEY_IMAGE_URI);
        if (savedImageUri != null) {
            capturedImageUri = Uri.parse(savedImageUri);
        }
    }

    private void captureImage0() {
//        signIn();
        if (mDriveServiceHelper != null) {

//            createFolder();
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Set video quality (0 for low quality, 1 for high quality)

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
            // Continue with the rest of your code that uses driveResourceClient
        } else {
            Toast.makeText(getApplicationContext(), "Not Signed", Toast.LENGTH_SHORT).show();

        }

    }


    private void captureMedia(String action, int requestCode) {
        if (mDriveServiceHelper != null) {
            Intent intent = new Intent(action);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, requestCode);
            } else {
                Toast.makeText(this, "Not Signed successfully", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void resendMedia(String filePath) {
        if (mDriveServiceHelper != null) {
            if (filePath != null && !filePath.equals("")) {
                loadToast.setText("Uploading file...");
                loadToast.show();

                mDriveServiceHelper.uploadFileToGoogleDrive(filePath, progressBar, progressText)
                        .addOnSuccessListener(result -> {
                            loadToast.success();
                            showMessage("File uploaded ...!!");
                        })
                        .addOnFailureListener(e -> {
                            loadToast.error();
                            showMessage("Couldn't able to upload file, error: " + e);
                        });
            } else {
                Toast.makeText(this, "Cannot upload file to server", Toast.LENGTH_SHORT).show();
            }
        } else {
                Toast.makeText(this, "Not Signed successfully", Toast.LENGTH_SHORT).show();
            }
    }

    private void selectMediaFromGallery(int requestCode) {
        if (mDriveServiceHelper != null) {
        Intent intent;
        if (requestCode == REQUEST_IMAGE_GALLERY) {
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        } else {
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent, requestCode);
    }  else {
            Toast.makeText(this, "Not Signed successfully", Toast.LENGTH_SHORT).show();
        }
    }
    public void captureVideo() {
//        signIn();
        if (mDriveServiceHelper != null) {

//            createFolder();
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            // Set video quality (0 for low quality, 1 for high quality)
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_CODE_CAPTURE_VIDEO);
            }
            // Continue with the rest of your code that uses driveResourceClient
        } else {
            Toast.makeText(getApplicationContext(), "Not Signed", Toast.LENGTH_SHORT).show();

        }

    }
    private void resendImg() {
//    private void resendImg(Uri uri) {
//        if (mDriveServiceHelper != null) {
        if(selectedFilePath2 != null && !selectedFilePath2.equals("")){
            if (mDriveServiceHelper != null) {
                loadToast.setText("Uploading file...");
                loadToast.show();
                mDriveServiceHelper.uploadFileToGoogleDriveImg(selectedFilePath2,progressBar,progressText)
                        .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                            @Override
                            public void onSuccess(Boolean result) {
                                loadToast.success();
                                showMessage("File uploaded ...!!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                loadToast.error();
                                showMessage("Couldn't able to upload file, error: "+e);
                            }
                        });
            }
        }else{
            loadToast.error();
            Toast.makeText(this,"Cannot upload file to server",Toast.LENGTH_SHORT).show();
        }
    }

    private void resendVid(Uri uri) {

                loadToast.setText("Uploading file...");
                loadToast.show();
        if(selectedFilePath != null && !selectedFilePath.equals("")){
            if (mDriveServiceHelper != null) {
                loadToast.setText("Uploading file...");
                loadToast.show();
                mDriveServiceHelper.uploadFileToGoogleDrive(selectedFilePath,progressBar,progressText)
                        .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                            @Override
                            public void onSuccess(Boolean result) {
                                loadToast.success();
                                showMessage("File uploaded ...!!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                loadToast.error();
                                showMessage("Couldn't able to upload file, error: "+e);
                            }
                        });
            }
        }else{
            loadToast.error();
            Toast.makeText(this,"Cannot upload file to server",Toast.LENGTH_SHORT).show();
        }
    }


    public void sendVid(Uri uri) {

                    loadToast.setText("Uploading file...");
                    loadToast.show();
        String path = FileUtils.getPath(this, uri);
        Log.e(TAG, "Selected File Path:" + selectedFilePath);


        if (path != null && !path.equals("")) {
            if (mDriveServiceHelper != null) {
                      mDriveServiceHelper.uploadFileToGoogleDrive(path, progressBar,progressText)
                        .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                            @Override
                            public void onSuccess(Boolean result) {
                                loadToast.success();
                                showMessage("File uploaded ...!!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                loadToast.error();
                                showMessage("Couldn't able to upload file, error: " + e);
                            }
                        });
            }
        } else {
            loadToast.error();
            Toast.makeText(this, "Cannot upload file to server", Toast.LENGTH_SHORT).show();
        }

    }
    private void handleSelectedImage(Intent resultData) {
        if (resultData == null) {
            // Handle the case where resultData is null
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Result data is null", Toast.LENGTH_SHORT).show());
            return;
        }

        // Check if the resultData contains the path specified by MediaStore.EXTRA_OUTPUT
        String imagePath = resultData.getStringExtra(MediaStore.EXTRA_OUTPUT);

        if (imagePath != null && !imagePath.isEmpty()) {
            // Continue with your code to process the selected image
            loadToast.setText("Uploading file...");
            loadToast.show();

            // Use the imagePath as the selected file path
            selectedFilePath2 = imagePath;

            if (mDriveServiceHelper != null) {
                mDriveServiceHelper.uploadFileToGoogleDriveImg(selectedFilePath2, progressBar, progressText)
                        .addOnSuccessListener(result -> {
                            runOnUiThread(() -> {
                                loadToast.success();
                                showMessage("File uploaded ...!!");
                            });
                        })
                        .addOnFailureListener(e -> {
                            runOnUiThread(() -> {
                                loadToast.error();
                                showMessage("Couldn't be able to upload file, error: " + e);
                            });
                        });
            }
        } else {
            runOnUiThread(() -> {
                loadToast.error();
                Toast.makeText(MainActivity.this, "Selected image path is null or empty", Toast.LENGTH_SHORT).show();
            });
        }
    }

    // Helper method to get the file path from URI
    private String getPathFromUri(Uri uri) {
        String filePath = null;
        String[] projection = {MediaStore.Images.Media.DATA};

        try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                filePath = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return filePath;
    }



    public void sendImg(Uri uri){
                    if (mDriveServiceHelper != null) {
        String path = FileUtils.getPath(this, uri);
        Log.e(TAG,"Selected File Path:" + selectedFilePath2);


        if(path != null && !path.equals("")){
            if (mDriveServiceHelper != null) {

                mDriveServiceHelper.uploadFileToGoogleDriveImg(path,progressBar,progressText)
                        .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                            @Override
                            public void onSuccess(Boolean result) {
                                loadToast.success();
                                showMessage("File uploaded ...!!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                loadToast.error();
                                showMessage("Couldn't able to upload file, error: "+e);
                            }
                        });
            }
        }else{
            loadToast.error();
            Toast.makeText(this,"Cannot upload file to server",Toast.LENGTH_SHORT).show();
        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Not Signed", Toast.LENGTH_SHORT).show();

                    }
    }



    // Read/Write permission
    private void requestForStoragePermission() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.GET_ACCOUNTS,
                        Manifest.permission.INTERNET,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            Toast.makeText(getApplicationContext(), "All permissions are granted!", Toast.LENGTH_SHORT).show();
                            requestSignIn();
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }
    private void showSplash() {
        Intent intent = new Intent(MainActivity.this,SplashActivity.class);
        startActivity(intent);

    }

    /**
     * Showing Alert Dialog with Settings option
     * Navigates user to app settings
     * NOTE: Keep proper title and message depending on your app
     */
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }

    // navigating user to app settings
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    /**
     * Starts a sign-in activity using {@link #REQUEST_CODE_SIGN_IN}.
     */

    private void requestSignIn() {
        Log.d(TAG, "Requesting sign-in");

        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                        .requestEmail()
                        .build();
        googleSignInClient = GoogleSignIn.getClient(this, signInOptions);

        // The result of the sign-in Intent is handled in onActivityResult.
        startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Please wait!");
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }
    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (requestCode == REQUEST_CODE_SIGN_IN && resultCode == RESULT_OK) {
            if(resultData == null){
                //no data present
                return;
            }
            handleSignInResult(resultData);
        }

       else if (requestCode == REQUEST_AUTHORIZATION && resultCode == RESULT_OK) {
            // Handle the result of the authorization request

                if(resultData == null){
                    //no data present
                    return;
                }
                // User has resolved the authentication issue, retry the sign-in
                showMessage("Authentication issue resolved. Retrying sign-in...");
                requestSignIn();

        }
//        else if (requestCode == REQUEST_CODE_CAPTURE_VIDEO ) {
       else if (requestCode == REQUEST_CODE_CAPTURE_VIDEO && resultCode == RESULT_OK) {

            if(resultData == null){
                //no data present
                return;
            }

            loadToast.setText("Uploading file...");
            loadToast.show();

            // Get the Uri of the selected file
            capturedVideoUri = resultData.getData();
            Log.e(TAG, "selected File Uri: "+capturedVideoUri );
            // Get the path
            selectedFilePath = FileUtils.getPath(this, capturedVideoUri);
            Log.e(TAG,"Selected File Path:" + selectedFilePath);


            if(selectedFilePath != null && !selectedFilePath.equals("")){
                if (mDriveServiceHelper != null) {
                    mDriveServiceHelper.uploadFileToGoogleDrive(selectedFilePath,progressBar,progressText)
                            .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                                @Override
                                public void onSuccess(Boolean result) {
                                    loadToast.success();
                                    showMessage("File uploaded ...!!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(Exception e) {
                                    loadToast.error();
                                    showMessage("Couldn't able to upload file, error: "+e);
                                }
                            });
                }
            }else{
                loadToast.error();
                Toast.makeText(this,"Cannot upload file to server",Toast.LENGTH_SHORT).show();
            }
        }


//        else if (requestCode == REQUEST_IMAGE_CAPTURE) {
       else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//  RESULTDATA IS NULL FROM MY IMPLEMENTATION
//                if (resultData == null) {
////                    Toast.makeText(MainActivity.this, "User Cancelled", Toast.LENGTH_SHORT).show();
//                    // Handle the case where resultData is null (e.g., user canceled the capture).
//                    return;
//                }

            loadToast.setText("Uploading file...");
            loadToast.show();

            if (currentPhotoPath != null) {
                // Continue with your code to upload or process the captured image
                selectedFilePath2 = currentPhotoPath;
                if (mDriveServiceHelper != null) {
                    mDriveServiceHelper.uploadFileToGoogleDriveImg(currentPhotoPath, progressBar, progressText)

//                    mDriveServiceHelper.uploadFileToGoogleDriveImg(selectedFilePath2, progressBar, progressText)
                            .addOnSuccessListener(result -> {
                                runOnUiThread(() -> {
                                    loadToast.success();
                                    showMessage("File uploaded ...!!");
                                });
                            })
                            .addOnFailureListener(e -> {
                                runOnUiThread(() -> {
                                    loadToast.error();
                                    showMessage("Couldn't be able to upload file, error: " + e);
                                });
                            });
                }
            } else {
                // Handle the case where the file path is null
                runOnUiThread(() -> {
                    loadToast.error();
                    Toast.makeText(MainActivity.this, "File path is null", Toast.LENGTH_SHORT).show();
                });
            }

        }

        else  if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK) {
            if(resultData == null){
                //no data present
                return;
            }
           loadToast.setText("Uploading file...");
            loadToast.show();

            // Use the file you provided to the gallery intent
            if (currentPhotoPath != null && !currentPhotoPath.isEmpty()) {
                // Continue with your code to upload or process the selected image
                if (mDriveServiceHelper != null) {
                    mDriveServiceHelper.uploadFileToGoogleDriveImg(currentPhotoPath, progressBar, progressText)
                            .addOnSuccessListener(result -> {
                                runOnUiThread(() -> {
                                    loadToast.success();
                                    showMessage("File uploaded ...!!");
                                });
                            })
                            .addOnFailureListener(e -> {
                                runOnUiThread(() -> {
                                    loadToast.error();
                                    showMessage("Couldn't be able to upload file, error: " + e);
                                });
                            });
                }
            } else {
                runOnUiThread(() -> {
                    loadToast.error();
                    Toast.makeText(MainActivity.this, "Selected image path is null or empty", Toast.LENGTH_SHORT).show();
                });
            }
        }


      else if (requestCode == REQUEST_VIDEO_GALLERY && resultCode == RESULT_OK) {

                    if(resultData == null){
                        //no data present
                        return;
                    }
//                loadToast.setText("Uploading file...");
//                loadToast.show();
                    capturedVideoUri = resultData.getData();
                    Toast.makeText(MainActivity         .this, "Video selected", Toast.LENGTH_SHORT).show();
           sendVid(capturedVideoUri);
            } else {
            super.onActivityResult(requestCode, resultCode, resultData);
        }
    }







//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
//
////            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
////                loadToast.setText("Uploading file...");
////                loadToast.show();
////
////                if (currentPhotoPath != null) {
////                    // Continue with your code to upload or process the captured image
////                    selectedFilePath2 = currentPhotoPath;
////
////                    if (mDriveServiceHelper != null) {
////                        mDriveServiceHelper.uploadFileToGoogleDriveImg(selectedFilePath2, progressBar, progressText)
////                                .addOnSuccessListener(result -> {
////                                    runOnUiThread(() -> {
////                                        loadToast.success();
////                                        showMessage("File uploaded ...!!");
////                                    });
////                                })
////                                .addOnFailureListener(e -> {
////                                    runOnUiThread(() -> {
////                                        loadToast.error();
////                                        showMessage("Couldn't be able to upload file, error: " + e);
////                                    });
////                                });
////                    }
////                } else {
////                    // Handle the case where the file path is null
////                    runOnUiThread(() -> {
////                        loadToast.error();
////                        Toast.makeText(MainActivity.this, "File path is null", Toast.LENGTH_SHORT).show();
////                    });
////                }
////            } else {
////                // Handle the case where the user canceled the capture or there was an error
////                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Capture canceled or failed", Toast.LENGTH_SHORT).show());
////            }
////        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
////            loadToast.setText("Uploading file...");
////            loadToast.show();
////
////            capturedImageUri = resultData.getData();
////            if (capturedImageUri != null) {
////                selectedFilePath2 = FileUtils.getPath(this, capturedImageUri);
////                if (selectedFilePath2 != null && !selectedFilePath2.equals("")) {
////                    if (mDriveServiceHelper != null) {
////                        mDriveServiceHelper.uploadFileToGoogleDriveImg(selectedFilePath2, progressBar, progressText)
////                                .addOnSuccessListener(result -> {
////                                    runOnUiThread(() -> {
////                                        loadToast.success();
////                                        showMessage("File uploaded ...!!");
////                                    });
////                                })
////                                .addOnFailureListener(e -> {
////                                    runOnUiThread(() -> {
////                                        loadToast.error();
////                                        showMessage("Couldn't be able to upload file, error: " + e);
////                                    });
////                                });
////                    }
////                } else {
////                    runOnUiThread(() -> {
////                        loadToast.error();
////                        Toast.makeText(MainActivity.this, "Cannot upload file to server", Toast.LENGTH_SHORT).show();
////                    });
////                }
////            } else {
////                // Handle the case where the capturedImageUri is null
////                runOnUiThread(() -> {
////                    loadToast.error();
////                    Toast.makeText(MainActivity.this, "Captured image Uri is null", Toast.LENGTH_SHORT).show();
////                });
////            }
////
////
////            // Continue with the rest of your code to upload or process the captured image
////        }
//
//        switch (requestCode) {
//            case REQUEST_CODE_SIGN_IN:
//                if (resultCode == Activity.RESULT_OK && resultData != null) {
//                    handleSignInResult(resultData);
//                }
//                break;
//
//            case REQUEST_CODE_CAPTURE_VIDEO:
////            case PICK_FILE_REQUEST:
//                 if(resultData == null){
//                    //no data present
//                    return;
//                }
//
//                loadToast.setText("Uploading file...");
//                loadToast.show();
//
//                // Get the Uri of the selected file
//                capturedVideoUri = resultData.getData();
//                Log.e(TAG, "selected File Uri: "+capturedVideoUri );
//                // Get the path
//                 selectedFilePath = FileUtils.getPath(this, capturedVideoUri);
//                Log.e(TAG,"Selected File Path:" + selectedFilePath);
//
//
//                if(selectedFilePath != null && !selectedFilePath.equals("")){
//                    if (mDriveServiceHelper != null) {
//                        mDriveServiceHelper.uploadFileToGoogleDrive(selectedFilePath,progressBar,progressText)
//                                .addOnSuccessListener(new OnSuccessListener<Boolean>() {
//                                    @Override
//                                    public void onSuccess(Boolean result) {
//                                        loadToast.success();
//                                        showMessage("File uploaded ...!!");
//                                    }
//                                })
//                                .addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(Exception e) {
//                                        loadToast.error();
//                                        showMessage("Couldn't able to upload file, error: "+e);
//                                    }
//                                });
//                    }
//                }else{
//                    loadToast.error();
//                    Toast.makeText(this,"Cannot upload file to server",Toast.LENGTH_SHORT).show();
//                }
//                break;
//            case REQUEST_IMAGE_CAPTURE:
////                if (resultData == null) {
////                    Toast.makeText(MainActivity.this, "User Cancelled", Toast.LENGTH_SHORT).show();
////                    // Handle the case where resultData is null (e.g., user canceled the capture).
////                    return;
////                }
//                loadToast.setText("Uploading file...");
//                loadToast.show();
//
//                if (currentPhotoPath != null) {
//                    // Continue with your code to upload or process the captured image
//                    selectedFilePath2 = currentPhotoPath;
//                    if (mDriveServiceHelper != null) {
//                        mDriveServiceHelper.uploadFileToGoogleDriveImg(selectedFilePath2, progressBar, progressText)
//                                .addOnSuccessListener(result -> {
//                                    runOnUiThread(() -> {
//                                        loadToast.success();
//                                        showMessage("File uploaded ...!!");
//                                    });
//                                })
//                                .addOnFailureListener(e -> {
//                                    runOnUiThread(() -> {
//                                        loadToast.error();
//                                        showMessage("Couldn't be able to upload file, error: " + e);
//                                    });
//                                });
//                    }
//                } else {
//                    // Handle the case where the file path is null
//                    runOnUiThread(() -> {
//                        loadToast.error();
//                        Toast.makeText(MainActivity.this, "File path is null", Toast.LENGTH_SHORT).show();
//                    });
//                }
//
//////                if (resultCode == Activity.RESULT_OK && resultData != null) {
//////                    if (resultData == null) {
//////                    // Handle the case where resultData is null (e.g., user canceled the capture).
//////                    return;
//////                }
////
////                if (resultCode == Activity.RESULT_OK) {
////                    if (resultData == null) {
////                    // Handle the case where resultData is null (e.g., user canceled the capture).
////                    return;
////                }
////                    loadToast.setText("Uploading file...");
////                    loadToast.show();
////
////                    ///////////
////                    if (resultData != null && resultData.getData() != null) {
////                        // Case 1: Image data is present in the result data
////                        capturedImageUri = resultData.getData();
////                    } else if (resultData != null) {
////                        // Case 2: Image data is not present, but extras contain a thumbnail
////                        Bitmap imageBitmap = (Bitmap) resultData.getExtras().get("data");
////                        if (imageBitmap != null) {
////                            capturedImageUri = saveImageToExternalStorage(imageBitmap);
////                        } else {
////                            // Case 3: Both data and thumbnail are null (unexpected scenario)
////                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Null Bitmap", Toast.LENGTH_SHORT).show());
////                        }
////                    }
////
////                    // Continue with your code to upload or process the captured image
////                    if (capturedImageUri != null) {
////                        selectedFilePath2 = FileUtils.getPath(this, capturedImageUri);
////                        if (selectedFilePath2 != null && !selectedFilePath2.equals("")) {
////                            if (mDriveServiceHelper != null) {
////                                mDriveServiceHelper.uploadFileToGoogleDriveImg(selectedFilePath2, progressBar, progressText)
////                                        .addOnSuccessListener(result -> {
////                                            runOnUiThread(() -> {
////                                                loadToast.success();
////                                                showMessage("File uploaded ...!!");
////                                            });
////                                        })
////                                        .addOnFailureListener(e -> {
////                                            runOnUiThread(() -> {
////                                                loadToast.error();
////                                                showMessage("Couldn't be able to upload file, error: " + e);
////                                            });
////                                        });
////                            }
////                        } else {
////                            runOnUiThread(() -> {
////                                loadToast.error();
////                                Toast.makeText(MainActivity.this, "Cannot upload file to server", Toast.LENGTH_SHORT).show();
////                            });
////                        }
////                    } else {
////                        // Handle the case where the capturedImageUri is null
////                        runOnUiThread(() -> {
////                            loadToast.error();
////                            Toast.makeText(MainActivity.this, "Captured image Uri is null", Toast.LENGTH_SHORT).show();
////                        });
////                          }
////                } else {
////                    // Handle the case where the user canceled the capture or there was an error
////                    runOnUiThread(() -> {
////                        loadToast.error();
////                        Toast.makeText(MainActivity.this, "Capture canceled or failed", Toast.LENGTH_SHORT).show();
////                    });
////                        }
//                break;
////                if (resultCode == Activity.RESULT_OK && resultData != null) {
////                    runOnUiThread(new Runnable() {
////                        @Override
////                        public void run() {
////                            loadToast.setText("Uploading file...");
////                            loadToast.show();
////                        }
////                    });
////
////                    // Get the Uri of the selected file
////                    Bitmap imageBitmap = (Bitmap) resultData.getExtras().get("data");
////                    if (imageBitmap != null) {
////                        capturedImageUri = resultData.getData();
//////                    capturedImageUri = saveImageToExternalStorage(imageBitmap);
////                    } else {
////                        runOnUiThread(new Runnable() {
////                            @Override
////                            public void run() {
////                                Toast.makeText(MainActivity.this, "Null Bitmap", Toast.LENGTH_SHORT).show();
////                            }
////                        });
////                    }
////
////                    Log.e(TAG, "selected File Uri: " + capturedImageUri);
////                    // Get the path
////                    selectedFilePath2 = FileUtils.getPath(this, capturedImageUri);
////                    Log.e(TAG, "Selected File Path:" + selectedFilePath2);
////
////                    if (selectedFilePath2 != null && !selectedFilePath2.equals("")) {
////                        if (mDriveServiceHelper != null) {
////                            mDriveServiceHelper.uploadFileToGoogleDriveImg(selectedFilePath2, progressBar, progressText)
////                                    .addOnSuccessListener(new OnSuccessListener<Boolean>() {
////                                        @Override
////                                        public void onSuccess(Boolean result) {
////                                            runOnUiThread(new Runnable() {
////                                                @Override
////                                                public void run() {
////                                                    loadToast.success();
////                                                    showMessage("File uploaded ...!!");
////                                                }
////                                            });
////                                        }
////                                    })
////                                    .addOnFailureListener(new OnFailureListener() {
////                                        @Override
////                                        public void onFailure(Exception e) {
////                                            runOnUiThread(new Runnable() {
////                                                @Override
////                                                public void run() {
////                                                    loadToast.error();
////                                                    showMessage("Couldn't be able to upload file, error: " + e);
////                                                }
////                                            });
////                                        }
////                                    });
////                        }
////                    } else {
////                        runOnUiThread(new Runnable() {
////                            @Override
////                            public void run() {
////                                loadToast.error();
////                                Toast.makeText(MainActivity.this, "Cannot upload file to server", Toast.LENGTH_SHORT).show();
////                            }
////                        });
////                    }
////                } else {
////                // Handle the case where the user canceled the capture or there was an error
////                runOnUiThread(new Runnable() {
////                    @Override
////                    public void run() {
////                        Toast.makeText(MainActivity.this, "Capture canceled or failed", Toast.LENGTH_SHORT).show();
////                    }
////                });
////            }
//
////                break;
//
////            case REQUEST_IMAGE_CAPTURE:
////                loadToast.setText("Uploading file...");
////                loadToast.show();
////
////                // Get the Uri of the selected file
////                Bitmap imageBitmap = (Bitmap) resultData.getExtras().get("data");
////                if (imageBitmap != null) {
////                    // Save the captured image to a file
////                    File imageFile = saveImageToFile(imageBitmap);
////
////                    if (imageFile != null) {
////                        selectedFilePath2 = imageFile.getAbsolutePath();
////                        mDriveServiceHelper.uploadFileToGoogleDriveImg(selectedFilePath2, progressBar, progressText)
////                                .addOnSuccessListener(new OnSuccessListener<Boolean>() {
////                                    @Override
////                                    public void onSuccess(Boolean result) {
////                                        loadToast.success();
////                                        showMessage("File uploaded ...!!");
////                                    }
////                                })
////                                .addOnFailureListener(new OnFailureListener() {
////                                    @Override
////                                    public void onFailure(Exception e) {
////                                        loadToast.error();
////                                        showMessage(String.valueOf(e));
//////                                        showMessage("Couldn't able to upload file, error: " + e);
////                                    }
////                                });
////                    } else {
////                        loadToast.error();
////                        Toast.makeText(this, "Failed to save the captured image", Toast.LENGTH_SHORT).show();
////                    }
////                } else {
////                    loadToast.error();
////                    Toast.makeText(MainActivity.this, "Null Bitmap", Toast.LENGTH_SHORT).show();
////                }
////                break;
//
//            case REQUEST_IMAGE_GALLERY:
//                if(resultData == null){
//                    //no data present
//                    return;
//                }
////                loadToast.setText("Uploading file...");
////                loadToast.show();
//                capturedImageUri = resultData.getData();
//
//                Toast.makeText(MainActivity.this, "Image selected", Toast.LENGTH_SHORT).show();
//                sendImg(capturedImageUri);
//                break;
//            case REQUEST_VIDEO_GALLERY:
//                if(resultData == null){
//                    //no data present
//                    return;
//                }
////                loadToast.setText("Uploading file...");
////                loadToast.show();
//                capturedVideoUri = resultData.getData();
//                Toast.makeText(MainActivity.this, "Video selected", Toast.LENGTH_SHORT).show();
//                sendVid(capturedVideoUri);
//                break;
//
//        }
//
//        super.onActivityResult(requestCode, resultCode, resultData);
//    }

    private File saveImageToFile(Bitmap bitmap) {
        String filename = "image_" + System.currentTimeMillis() + ".jpg";
        File directory = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "your_app_image_folder");

        if (!directory.exists()) {
            directory.mkdirs();
        }

        File imageFile = new File(directory, filename);

        try {
            FileOutputStream out = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // You can adjust the image quality here
            out.flush();
            out.close();
            return imageFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null; // Return null if there was an error
        }
    }
    private Uri saveImageToExternalStorage(Bitmap imageBitmap) {
        // Ensure you have the necessary permissions to write to external storage

        // Define the directory where you want to save the image
        File directory = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "YourAppDirectoryName");

        // Create the directory if it doesn't exist
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                Log.e(TAG, "Failed to create directory");
                return null;
            }
        }

        // Generate a unique filename
        String fileName = "image_" + System.currentTimeMillis() + ".jpg";

        // Create the file
        File file = new File(directory, fileName);

        try {
            // Compress the image and save it to the file
            FileOutputStream outputStream = new FileOutputStream(file);
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            // Return the URI of the saved file
            return Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Uri saveImageToExternalStorage1(Bitmap imageBitmap) {
        // Ensure you have the necessary permissions to write to external storage

        // Define the directory where you want to save the image
        File directory = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "YourAppDirectoryName");

        // Create the directory if it doesn't exist
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                Log.e(TAG, "Failed to create directory");
                return null;
            }
        }

        // Create a file to save the image
        File file = new File(directory, "image_" + System.currentTimeMillis() + ".jpg");

        try {
            // Save the bitmap to the file
            FileOutputStream out = new FileOutputStream(file);
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            // Return the Uri of the saved file
            return Uri.fromFile(file);
        } catch (IOException e) {
            Log.e(TAG, "Error saving image to external storage", e);
            return null;
        }
    }

    private Uri saveImageToExternalStorage0(Bitmap imageBitmap) {
        String savedImageURL = MediaStore.Images.Media.insertImage(
                getContentResolver(),
                imageBitmap,
                "image_" + System.currentTimeMillis(),
                "Image captured from Camera");

        return Uri.parse(savedImageURL);
    }
    public void captureVideo(View view) {
//        signIn();
        if (mDriveServiceHelper != null) {

//            createFolder();
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            // Set video quality (0 for low quality, 1 for high quality)
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_CODE_CAPTURE_VIDEO);
            }
            // Continue with the rest of your code that uses driveResourceClient
        } else {
            Toast.makeText(getApplicationContext(), "Not Signed successfully", Toast.LENGTH_SHORT).show();

        }

    }


    /**
     * Handles the {@code result} of a completed sign-in activity initiated from {@link
     * #requestSignIn()}.
     */

    private void initializeDriveServiceHelper(GoogleSignInAccount googleAccount) {
        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(
                        this, Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(googleAccount.getAccount());

        HttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        Drive googleDriveService = new Drive.Builder(
                transport,
                jsonFactory,
                credential)
                .setApplicationName("Drive API Migration")
                .build();

        // Initialize mDriveServiceHelper with the updated Drive service
        mDriveServiceHelper = new GoogleDriveServiceHelper(googleDriveService);

        // Enable other buttons as sign-in is complete
        signInButton.setEnabled(false);
//        folderFilesButton.setEnabled(true);
        signOutButton.setEnabled(true);
        createFolder();
    }

    private void initDriveService() {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, signInOptions);
        GoogleSignInAccount signInAccount = googleSignInClient.silentSignIn().getResult();

        if (signInAccount != null) {

//            driveResourceClient = com.google.android.gms.drive.Drive.getDriveResourceClient(this, signInAccount);
            // Use the driveResourceClient to interact with Google Drive services
        }
    }
    public void handleSignInResult1(Intent resultIntent) {
        try {
            GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(resultIntent).getResult(UserRecoverableAuthIOException.class);
            if (account != null) {
                initializeDriveServiceHelper(account);
//                initDriveService();
//                Toast.makeText(this, "Signed in successfully", Toast.LENGTH_SHORT).show();
            }  else
                showMessage("Signed in Not successfully");
        } catch (UserRecoverableAuthIOException e) {
            // Exception handling for UserRecoverableAuthIOException
//            startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
        }
    }

    private void handleSignInResult(Intent result) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener(googleAccount -> {
                    Log.d(TAG, "Signed in as " + googleAccount.getEmail());


//                    // Save user's sign-in information to SharedPreferences
                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(PREF_SIGNED_IN, true);
                    editor.putString(PREF_ACCOUNT_NAME, googleAccount.getEmail());
                    editor.apply();

                    userLogged.setText("Logged as: "+ googleAccount.getEmail());
                    // Use the authenticated account to sign in to the Drive service.
                    GoogleAccountCredential credential =
                            GoogleAccountCredential.usingOAuth2(
                                    this, Collections.singleton(DriveScopes.DRIVE_FILE));
                    credential.setSelectedAccount(googleAccount.getAccount());

                    HttpTransport transport = new NetHttpTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    Drive googleDriveService = new Drive.Builder(
                            transport,
                            jsonFactory,
                            credential)
                            .setApplicationName("Drive API Migration")
                            .build();

                    // The DriveServiceHelper encapsulates all REST API and SAF functionality.
                    // Its instantiation is required before handling any onClick actions.
                    mDriveServiceHelper = new GoogleDriveServiceHelper(googleDriveService);

                    // Enable other buttons as sign-in is complete
                    signInButton.setEnabled(false);

//                    folderFilesButton.setEnabled(true);

                    signOutButton.setEnabled(true);
                    createFolder();

                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception exception) {
                        Log.e(TAG, "Unable to sign in.", exception);
                        showMessage("Unable to sign in.");
                        signInButton.setEnabled(true);
                        signOutButton.setEnabled(false);
                    }
                });
    }



    // This method will get call when user click on sign-in button
    public void signIn(View view) {
        requestSignIn();
    }
//    public void signIn() {
//        requestSignIn();
//    }

    // This method will get call when user click on create folder button

    public void createFolder() {
        if (mDriveServiceHelper != null) {

            // check folder present or not
            mDriveServiceHelper.isFolderPresent()
                    .addOnSuccessListener(new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String id) {
                            if (id.isEmpty()){
                                mDriveServiceHelper.createFolder()
                                        .addOnSuccessListener(new OnSuccessListener<String>() {
                                            @Override
                                            public void onSuccess(String fileId) {
                                                Log.e(TAG, "folder id: "+fileId );
                                                folderId=fileId;
                                                showMessage("Folder Created with id: "+fileId);
//                                                hideProgressDialog();
                                                folderFilesButton.setEnabled(true);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(Exception exception) {
                                                showMessage("Couldn't create file.");
                                                Log.e(TAG, "emptyid Couldn't create file .", exception);
                                            }
                                        });
                            }else {
                                folderId=id;
                                folderFilesButton.setEnabled(true);
                                showMessage("Ready!");
//                                hideProgressDialog();
                                showMessage("Sign-In Successfully...!!");
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception exception) {
                            showMessage("Registering ...");
//                            showMessage("exception:"+String.valueOf(exception));
                            Log.e(TAG, "Couldn't create file..", exception);

                            if (exception instanceof UserRecoverableAuthIOException) {

                                // UserRecoverableAuthIOException: Guide the user to resolve the issue
                                startActivityForResult(((UserRecoverableAuthIOException) exception).getIntent(), REQUEST_AUTHORIZATION);
                            }
                        }
                    });
        } else {
            showMessage("mDriveServiceHelper is null");
        }

        hideProgressDialog();
    }

    private void viewGoogleDriveFolder() {
        if (folderId != null && mDriveServiceHelper != null) {
            String folderUrl = "https://drive.google.com/drive/folders/" + folderId;
            Uri uri = Uri.parse(folderUrl);

            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.google.android.apps.docs"); // Open in Google Drive app if available
            intent.setDataAndType(uri, "text/html");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                // If Google Drive app is not installed, open in a web browser
                intent.setPackage(null);
                startActivity(intent);
            }
        } else {
            showMessage("Folder ID is not available or not signed in.");
        }
    }



    // This method will get call when user click on folder data button
    public void getFolderData(View view) {
        viewGoogleDriveFolder();
    }

//
//    public void getFolderData(View view) {
//        if (mDriveServiceHelper != null) {
//            Intent intent = new Intent(this, ListActivity.class);
//
//            mDriveServiceHelper.getFolderFileList()
//                    .addOnSuccessListener(new OnSuccessListener<ArrayList<GoogleDriveFileHolder>>() {
//                        @Override
//                        public void onSuccess(ArrayList<GoogleDriveFileHolder> result) {
//                            Log.e(TAG, "onSuccess: result: "+result.size() );
//                            intent.putParcelableArrayListExtra("fileList", result);
//                            startActivity(intent);
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(Exception e) {
//                            showMessage("Not able to access Folder data.");
//                            Log.e(TAG, "Not able to access Folder data.", e);
//                        }
//                    });
//        }
//    }


    private void selectImageFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // Create a file to store the selected image
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Toast.makeText(this, "Error while creating image file.", Toast.LENGTH_SHORT).show();
        }

        if (photoFile != null) {
            // Get the URI from the file
            Uri photoUri = FileProvider.getUriForFile(this, "com.android.sun2meg.securedcamerashots.fileprovider", photoFile);

            // Pass the URI as an extra to the gallery intent
            galleryIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(galleryIntent, REQUEST_IMAGE_GALLERY);
        }
    }

    private void selectImageFromGallery1() {
//        createFolder();
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

//        Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, REQUEST_IMAGE_GALLERY);

//        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
    }

    private void selectVideoFromGallery() {
//        createFolder();
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_VIDEO_GALLERY);
    }


    private void clearUserCredentials() {
        // Clear user's sign-in information from SharedPreferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(PREF_SIGNED_IN, false);
        editor.remove(PREF_ACCOUNT_NAME);
        editor.apply();
        // You may also want to clear other user-specific data as needed.
    }

    public void signOut(View view) {
        // Sign out the user
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                status -> {
                    if (status.isSuccess()) {
                        // Clear user credentials and update UI
                        clearUserCredentials();
                        // Disable UI elements, if needed
                        signInButton.setEnabled(true);
                        folderFilesButton.setEnabled(false);
                        signOutButton.setEnabled(false);
                        showMessage("Signed-Out...!!");
                        mDriveServiceHelper=null;
                        userLogged.setText("");
                        // Show a message or update UI to indicate the sign-out
                    } else {
                        // Handle sign-out failure
                        signInButton.setEnabled(false);
                        showMessage("Failed to sign out");
                    }
                }
    );
}

    // This method will get call when user click on sign-out button
    public void signOut2(View view) {
        if (googleSignInClient != null){
            googleSignInClient.signOut()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete( Task<Void> task) {
                            signInButton.setEnabled(true);

                            folderFilesButton.setEnabled(false);

                            signOutButton.setEnabled(false);
                            showMessage("Signed-Out...!!");
                            mDriveServiceHelper=null;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception exception) {
                            signInButton.setEnabled(false);
                            showMessage("Unable to sign out.");
                            Log.e(TAG, "Unable to sign out.", exception);
                        }
                    });
        }
        else
            showMessage("googleSignInClient is null.");
    }

    public void showMessage(String message) {
        Log.i(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
//    @Override
//    public void onBackPressed() {
//        if (backPressCount == 0) {
//            // Show a message to press back again
//            Toast.makeText(this, "Press back button again to exit", Toast.LENGTH_SHORT).show();
//
//            // Increment the back press count
//            backPressCount++;
//
//            // Set a delayed handler to reset the back press count
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    backPressCount = 0;
//                }
//            }, 2000); // You can adjust the time window for double press
//        } else {
//            // If back is pressed again within the time window, exit the app
////            super.onBackPressed();
//        Intent setIntent = new Intent(Intent.ACTION_MAIN);
//        setIntent.addCategory(Intent.CATEGORY_HOME);
//        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(setIntent);
//        }
//    }

@Override
public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
}
@Override
public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), Slider2.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        Log.d("CDA", "onBackPressed Called");
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    }

}