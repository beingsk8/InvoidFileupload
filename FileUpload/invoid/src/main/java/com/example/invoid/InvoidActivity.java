package com.example.invoid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class InvoidActivity extends AppCompatActivity {

    Button selectFile, upload;
    ProgressBar progressBar;
    ImageView imageView;
    Uri imageUri;

    private  DatabaseReference root = FirebaseDatabase.getInstance().getReference().child("Images");
   private StorageReference reference = FirebaseStorage.getInstance().getReference();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoid);
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        selectFile =  findViewById(R.id.SelectFile);
        upload = findViewById(R.id.upload);
        progressBar = findViewById(R.id.progressBar3);
        imageView = findViewById(R.id.imageView);
        progressBar.setVisibility(View.INVISIBLE );


        selectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,7);
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imageUri!=null){
                    uploadToFirebase(imageUri);
                }
                else {
                    Toast.makeText(InvoidActivity.this,"please select document",Toast.LENGTH_SHORT);
                }
            }
        });
    }

    private void uploadToFirebase(Uri uri) {
        StorageReference fileRef = reference.child(System.currentTimeMillis() + " . " + getFileExtension(uri));
        fileRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                 progressBar.setVisibility(View.INVISIBLE);
                 fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                     @Override
                     public void onSuccess(Uri uri) {
                        uploadModel model = new uploadModel(uri.toString());
                        String modelID = root.push().getKey();
                        root.child(modelID).setValue(model);
                       Toast toast= Toast.makeText(InvoidActivity.this, "uploaded successfully", Toast.LENGTH_LONG);
                        View view = toast.getView();
                        TextView text = view.findViewById(android.R.id.message);
                        text.setTextColor(Color.BLACK);
                        toast.show();
                     }
                 });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                progressBar.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast toast= Toast.makeText(InvoidActivity.this, "Upload Failed", Toast.LENGTH_LONG);
                View view = toast.getView();
                TextView text = view.findViewById(android.R.id.message);
                text.setTextColor(Color.RED);
                toast.show();
            }
        });
    }

    private String getFileExtension(Uri mUri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return  mime.getExtensionFromMimeType(cr.getType(mUri));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    if(requestCode == 7 && resultCode == RESULT_OK && data != null){
      imageUri = data.getData();
        imageView.setImageURI(imageUri);
    }
    }
}