package com.android.sun2meg.securedcamerashots;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.DriveResourceClient;
//import com.google.api.client.extensions.android.http.AndroidHttp;
//import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
//import com.google.api.client.googleapis.media.MediaHttpUploader;
//import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
//import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
//import com.google.api.client.googleapis.services.CommonGoogleClientRequestInitializer;
//import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
//import com.google.api.client.googleapis.services.json.AbstractGoogleJsonClient;
//import com.google.api.client.googleapis.services.json.CommonGoogleJsonClientRequestInitializer;
//import com.google.api.client.googleapis.services.json.CommonGoogleJsonClientRequestInitializer.Builder;
//import com.google.api.client.googleapis.services.json.CommonGoogleJsonClientRequestInitializer.Builder;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
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

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class GoogleDriveHelper {

 private static final String TAG = "GoogleDriveHelper";
 public static final int REQUEST_CODE_SIGN_IN = 1;

 private Context context;
 private Drive driveService;
 private GoogleSignInClient googleSignInClient;
    DriveResourceClient driveResourceClient;
public GoogleDriveHelper(Context context) {
    this.context = context;
initDriveService();
      }

    private void initDriveService() {
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(context);

        if (signInAccount != null) {
            driveResourceClient = Drive.getDriveResourceClient(context, signInAccount);
            // Use the driveResourceClient to interact with Google Drive services
        }
    }


//    private void initDriveService() {
//        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestScopes(new Scope("https://www.googleapis.com/auth/drive.file"))
//                .build();
//        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(context, signInOptions);
//        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(context);
//
//        if (signInAccount != null) {
//            DriveResourceClient driveResourceClient = Drive.getDriveResourceClient(context, signInAccount);
//            // Use the driveResourceClient to interact with Google Drive services
//        }
//    }

//    private void initDriveService() {
//     HttpTransport transport = AndroidHttp.newCompatibleTransport();
//     JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
//
//         GoogleCredential credential = GoogleCredential.fromStream(context.getResources().openRawResource(R.raw.credentials))
//                 .createScoped(Collections.singleton("https://www.googleapis.com/auth/drive.file"));
//
//
////         GoogleCredential credential = GoogleCredential.fromStream(context.getResources().openRawResource(R.raw.credentials))
////     .createScoped(Collections.singleton(DriveScopes.DRIVE_FILE));
//
// driveService = new Drive.Builder(transport, jsonFactory, credential)
//.setApplicationName("Your Application Name")
//.setGoogleClientRequestInitializer(new CommonGoogleClientRequestInitializer("YOUR_API_KEY"))
//.build();
//}

    public GoogleSignInAccount getSignInAccount() {
        return GoogleSignIn.getLastSignedInAccount(context);
    }

public void signIn() {

    GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(new Scope("https://www.googleapis.com/auth/drive.file"))
            .build();



     googleSignInClient = GoogleSignIn.getClient(context, signInOptions);

    Intent signInIntent = googleSignInClient.getSignInIntent();
   ((Activity) context).startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN);
    }

   public void handleSignInResult(Intent resultIntent) {
                          try {
    GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(resultIntent).getResult(ApiException.class);
           if (account != null) {
      initDriveService();
      Toast.makeText(context, "Signed in successfully", Toast.LENGTH_SHORT).show();
      }
    } catch (ApiException e) {
     Log.e(TAG, "Sign-in failed", e);
     Toast.makeText(context, "Sign-in failed", Toast.LENGTH_SHORT).show();
      }
       }

    public void uploadVideo(String videoFilePath, GoogleApiClient googleApiClient) {
//    public void uploadVideo(String videoFilePath, DriveResourceClient driveResourceClient, GoogleApiClient googleApiClient) {
        java.io.File videoFile = new java.io.File(videoFilePath);

        // Create metadata for the file
        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                .setTitle("My Video")
                .setMimeType("video/mp4")
                .build();

        // Create a new empty DriveContents resource
        driveResourceClient.createContents()
                .addOnSuccessListener(driveContents -> {
                    try {
                        // Open an OutputStream for the DriveContents
                        OutputStream outputStream = driveContents.getOutputStream();

                        // Read the content of the video file and write it to the DriveContents
                        FileInputStream inputStream = new FileInputStream(videoFile);
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }

                        // Commit the DriveContents with the video content
                        driveContents.commit(googleApiClient, metadataChangeSet)
                                .setResultCallback(status -> {
                                    if (status.isSuccess()) {
                                        showToast("Uploaded successfully");
                                         // File created and content uploaded successfully
//                                        DriveFile driveFile = Drive.DriveApi.getFile(googleApiClient, status.getDriveFile().getDriveId());
//                                        String fileId = driveFile.getDriveId().encodeToString();
//                                        // Handle fileId as needed
                                    } else {
                                        // Handle failure
                                        showToast("Uploading error: " + status.getStatusMessage());

                                        Log.e(TAG, "Error committing DriveContents: " + status.getStatusMessage());
                                    }
                                });

                    } catch (IOException e) {
                        // Handle IO exception
                        showToast("Error reading file");

                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Log.e(TAG, "Error creating DriveContents", e);
                });
    }
    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

//         public void uploadVideo2(String videoFilePath) {
//         new UploadVideoTask().execute(videoFilePath);
//         }

//         private class UploadVideoTask extends AsyncTask<String, Void, String> {
//
//@Override
// protected String doInBackground(String... params) {
//String videoFilePath = params[0];
//
// try {
// File fileMetadata = new File(videoFilePath);
// fileMetadata.setName("My Video");
//fileMetadata.setMimeType("video/mp4");
//
// java.io.File videoFile = new java.io.File(videoFilePath);
// FileContent mediaContent = new FileContent("video/mp4", videoFile);
//
// File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
// .setFields("id")
// .execute();
//
//return uploadedFile.getId();
// } catch (IOException e) {
// Log.e(TAG, "Error uploading video", e);
//return null;
// }
// }
//
// @Override
// protected void onPostExecute(String fileId) {
// if (fileId != null) {
// Toast.makeText(context, "Video uploaded successfully", Toast.LENGTH_SHORT).show();
// } else {
//     Toast.makeText(context, "Failed to upload video", Toast.LENGTH_SHORT).show();
// }
// }
//}
}
