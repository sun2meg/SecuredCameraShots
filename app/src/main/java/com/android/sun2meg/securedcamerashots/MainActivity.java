package com.android.sun2meg.securedcamerashots;

//import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
//import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
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
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import net.steamcrafted.loadtoast.LoadToast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


//To-Do Task list
//1. Google sign in
//2. Google sign out
//3. Creation of "Example folder" in Google Drive if it does not exist, if exists then listing its content
//4. Deleting the selected file from the "Example folder" (I think radio button could be used here)
//5. Downloading the selected file from the "Example folder" onto the mobile device, into a folder called "Example Download" (I think radio button could be used here)
//        -uploading the selected file from the "Example Download" into the "Example" folder (I think radio button could be used here)
//6. Delete the file from the "Example Download" folder

//generate SHA1 for debug only
//keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_CODE_SIGN_IN = 1;
    private static final int PICK_FILE_REQUEST = 100;
    private static final int REQUEST_CODE_CAPTURE_VIDEO = 2;
    static GoogleDriveServiceHelper mDriveServiceHelper;
    static String folderId="";

    private Button signInButton;
    private Button createFolderButton;
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
    private ImageView btnResendImg;
    private ImageView btnResendVid;
    private ImageView btnSelectImage;
    private ImageView btnSelectVideo;
    private ImageView btnCaptureVideo;
    private ImageView btnCaptureImage;
    private String selectedFilePath;
    private String selectedFilePath2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
if (!hasRequiredPermissions()) {
    requestForStoragePermission();
}
        signInButton = findViewById(R.id.id_sign_in);
        createFolderButton = findViewById(R.id.id_create_folder);
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
        progressBar.setMax(100);

//        if (mDriveServiceHelper != null) {
//            createFolder();
//        } else {
//            Toast.makeText(getApplicationContext(), "Not Signed successfully", Toast.LENGTH_SHORT).show();
//
//        }

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
//                    } else {
//                        Toast.makeText(MainActivity.this, "Capture already in progress", Toast.LENGTH_SHORT).show();
//                    }
//                    captureVideo();


            }
        });



        btnResendImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (capturedImageUri != null) {
                    resendImg(capturedImageUri);
                } else {
                    Toast.makeText(MainActivity.this, "No media captured", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnResendVid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (capturedVideoUri != null) {
                    resendVid(capturedVideoUri);
                } else {
                    Toast.makeText(MainActivity.this, "No media captured", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImageFromGallery();
            }
        });

        btnSelectVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectVideoFromGallery();
            }
        });
    }
//    private boolean checkStoragePermission() {
//        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
//    }
    private boolean hasRequiredPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void captureImage() {
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
            Toast.makeText(getApplicationContext(), "Not Signed successfully", Toast.LENGTH_SHORT).show();

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
            Toast.makeText(getApplicationContext(), "Not Signed successfully", Toast.LENGTH_SHORT).show();

        }

    }

    private void resendImg(Uri uri) {

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
            Toast.makeText(this,"Cannot upload file to server",Toast.LENGTH_SHORT).show();
        }
    }

    private void resendVid(Uri uri) {

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
            Toast.makeText(this,"Cannot upload file to server",Toast.LENGTH_SHORT).show();
        }
    }


    public void sendVid(Uri uri) {


        String path = FileUtils.getPath(this, uri);
        Log.e(TAG, "Selected File Path:" + selectedFilePath2);


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
            Toast.makeText(this, "Cannot upload file to server", Toast.LENGTH_SHORT).show();
        }
    }

    public void sendImg(Uri uri){

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
            Toast.makeText(this,"Cannot upload file to server",Toast.LENGTH_SHORT).show();
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


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                if (resultCode == Activity.RESULT_OK && resultData != null) {
                    handleSignInResult(resultData);
                }
                break;

            case REQUEST_CODE_CAPTURE_VIDEO:
//            case PICK_FILE_REQUEST:
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
                    Toast.makeText(this,"Cannot upload file to server",Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_IMAGE_CAPTURE:

                loadToast.setText("Uploading file...");
                loadToast.show();

                // Get the Uri of the selected file
                Bitmap imageBitmap = (Bitmap) resultData.getExtras().get("data");
                if (imageBitmap != null) {
                    capturedImageUri = saveImageToExternalStorage(imageBitmap);

                } else {
                    Toast.makeText(MainActivity.this, "Null Bitmap", Toast.LENGTH_SHORT).show();
                }


                Log.e(TAG, "selected File Uri: "+capturedImageUri );
                // Get the path
                selectedFilePath2 = FileUtils.getPath(this, capturedImageUri);
                Log.e(TAG,"Selected File Path:" + selectedFilePath2);


                if(selectedFilePath2 != null && !selectedFilePath2.equals("")){
                    if (mDriveServiceHelper != null) {
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
                    Toast.makeText(this,"Cannot upload file to server",Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_IMAGE_GALLERY:
                if(resultData == null){
                    //no data present
                    return;
                }
                loadToast.setText("Uploading file...");
                loadToast.show();
                capturedImageUri = resultData.getData();

                Toast.makeText(MainActivity.this, "Image selected", Toast.LENGTH_SHORT).show();
                sendImg(capturedImageUri);
                break;
            case REQUEST_VIDEO_GALLERY:
                if(resultData == null){
                    //no data present
                    return;
                }
                loadToast.setText("Uploading file...");
                loadToast.show();
                capturedVideoUri = resultData.getData();
                Toast.makeText(MainActivity.this, "Video selected", Toast.LENGTH_SHORT).show();
                sendVid(capturedVideoUri);
                break;

        }

        super.onActivityResult(requestCode, resultCode, resultData);
    }

    private Uri saveImageToExternalStorage(Bitmap imageBitmap) {
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

    private void handleSignInResult(Intent result) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener(googleAccount -> {
                    Log.d(TAG, "Signed in as " + googleAccount.getEmail());

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
                    createFolderButton.setEnabled(true);
                    folderFilesButton.setEnabled(true);

                    signOutButton.setEnabled(true);
                    createFolder();
                    showMessage("Sign-In done...!!");
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception exception) {
                        Log.e(TAG, "Unable to sign in.", exception);
                        showMessage("Unable to sign in.");
                        signInButton.setEnabled(true);
                    }
                });
    }



    // This method will get call when user click on sign-in button
    public void signIn(View view) {
        requestSignIn();
    }
    public void signIn() {
        requestSignIn();
    }

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
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(Exception exception) {
                                                showMessage("Couldn't create file.");
                                                Log.e(TAG, "Couldn't create file.", exception);
                                            }
                                        });
                            }else {
                                folderId=id;
                                showMessage("Folder already present");
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception exception) {
                            showMessage("Couldn't create file..");
                            Log.e(TAG, "Couldn't create file..", exception);
                        }
                    });
        }
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
//        createFolder();
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
    }

    private void selectVideoFromGallery() {
//        createFolder();
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_VIDEO_GALLERY);
    }

    // This method will get call when user click on sign-out button
    public void signOut(View view) {
        if (googleSignInClient != null){
            googleSignInClient.signOut()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete( Task<Void> task) {
                            signInButton.setEnabled(true);
                            createFolderButton.setEnabled(false);
                            folderFilesButton.setEnabled(false);

                            signOutButton.setEnabled(false);
                            showMessage("Sign-Out is done...!!");
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
    }

    public void showMessage(String message) {
        Log.i(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}