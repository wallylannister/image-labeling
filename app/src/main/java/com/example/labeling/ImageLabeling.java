package com.example.labeling;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseLocalModel;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceImageLabelerOptions;

import java.util.List;

public class ImageLabeling extends AppCompatActivity {
    private FirebaseVisionImageLabeler labeler;
    public static final String Data = "LABELING_RESULT";
    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_labeling);
        setUpLabeler(0.7f);
        dispatchTakePictureIntent();
    }

    private void setUpLabeler(Float threshold)
    {
        FirebaseLocalModel localModel = new FirebaseLocalModel.Builder("my_local_model")
                .setAssetFilePath("manifest.json")
                .build();
        FirebaseModelManager.getInstance().registerLocalModel(localModel);

        FirebaseVisionOnDeviceImageLabelerOptions options = new FirebaseVisionOnDeviceImageLabelerOptions.Builder()
                .setConfidenceThreshold(threshold).build();
        this.labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler(options);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    private void labeling(Bitmap bitmap)
    {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        this.labeler.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                        // Task completed successfully
                        // ...
                        String label = labels.get(0).getText();
                        Log.d("Result", label);
                        Intent intent = new Intent();
                        intent.putExtra(Data, label);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        // ...
                        Log.d("Result", "Err");
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            Log.d("Message", "Processing");
            labeling(imageBitmap);

        }
    }
}
