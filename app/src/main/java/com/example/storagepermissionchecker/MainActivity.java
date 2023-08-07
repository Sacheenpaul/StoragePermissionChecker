package com.example.storagepermissionchecker;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 101;
    PermissionUtil permissionUtil;
    File dir_;

    private ActivityResultLauncher<Intent> launcher; // Initialise this object in Activity.onCreate()
    private Uri baseDocumentTreeUri;
    private String imgPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissionUtil = new PermissionUtil(this);
       // requestPermission();


        // Registers a photo picker activity launcher in single-select mode.
        ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        Log.d("PhotoPicker", "Selected URI: " + uri);
                        String path = uri.getPath() ;// "/mnt/sdcard/FileName.mp3
                        File file = new File(path);
                        // saveImage(path);
                        writeIntoFile(this,file.getName(),path);
                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });

// Include only one of the following calls to launch(), depending on the types
// of media that you want to let the user choose from.

// Launch the photo picker and let the user choose images and videos.
       // pickMedia.launch(new PickVisualMediaRequest.Builder()
         //       .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
         //       .build());

// Launch the photo picker and let the user choose only images.
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());


    }
    public void createDir()
    {
        dir_ = new File(getApplicationContext().getFilesDir(), "Qker");
        File mFolder = new File(String.valueOf(dir_));
        if (!mFolder.exists()) {
            mFolder.mkdir();
        }

    }

    public void writeIntoFile(Context context, String fileName, String content) {
//        File appSpecificExternalStorageDirectory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File appSpecificInternalStorageDirectory = context.getFilesDir();
        File file = new File(appSpecificInternalStorageDirectory, fileName);

        try {
            file.createNewFile();
            FileOutputStream fos = null;
            fos = new FileOutputStream(file, false);
            fos.write(content.getBytes());
            fos.close();
            openImage(file.getAbsolutePath()+"jpg");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    private void saveImage(String mPath) {
        createDir();
        OutputStream fOut;
        imgPath = dir_ + "/" +"Test" + ".jpg";
        File file = new File(mPath); // the File to save , append increasing numeric counter to prevent files from getting overwritten.
        try {
            fOut = new FileOutputStream(file);

            fOut.flush(); // Not really required
            fOut.close(); // do not forget to close the stream
            MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
            //openImage(imgPath);

        } catch (IOException e) {
            e.printStackTrace();
                Toast.makeText(this, "Error " + e, Toast.LENGTH_SHORT).show();
            Log.e("TAG", "saveImage: "+e.toString());
        }

    }

    public void launchBaseDirectoryPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        launcher.launch(intent);
    }

    private void openImage(String path) {
        File file = new File(path);
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(FileProvider.getUriForFile(this, "com.example.storagepermissionchecker"+ ".fileprovider", file), "image/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No Application Available to View Image", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
      /*  if (resultCode == Activity.RESULT_OK) {
            baseDocumentTreeUri =data.getData();
            final int takeFlags = (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            // take persistable Uri Permission for future use
            getContentResolver().takePersistableUriPermission(data.getData(), takeFlags);
            SharedPreferences preferences = getSharedPreferences("com.example.geofriend.fileutility", Context.MODE_PRIVATE);
            preferences.edit().putString("filestorageuri", data.getData().toString()).apply();
        } else {
            Log.e("FileUtility", "Some Error Occurred : " + resultCode);


        }*/
    }


    private int checkPermission() {
        int status = PackageManager.PERMISSION_DENIED;
        return status = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Thanks ", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "Permission Denied, You cannot use local drive .", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void showPermissionExplanation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("App needs to access external storage we don't collect your data ,data is store locally on your phone");
        builder.setTitle("Storage Permission Needed");
        builder.setPositiveButton("Allow", (dialogInterface, i) -> requestPermission());
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void storagePermission() {
        if (PackageManager.PERMISSION_GRANTED != checkPermission()) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showPermissionExplanation();
            } else if (!permissionUtil.checkPermissionPreference("storage")) {
                requestPermission();
                permissionUtil.updatePermissionPreferences("storage");
            } else {
                Toast.makeText(this, "Please allow  external storage permission in your app setting", Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        }
    }


}