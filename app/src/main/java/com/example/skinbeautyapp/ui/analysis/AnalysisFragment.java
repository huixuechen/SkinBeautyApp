package com.example.skinbeautyapp.ui.analysis;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.skinbeautyapp.R;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AnalysisFragment extends Fragment {

    private static final int PICK_IMAGE = 100;
    private EditText ageInput, genderInput, heightInput, weightInput;
    private CheckBox smokingCheckbox, drinkingCheckbox;
    private Button uploadPhotoButton, submitButton;
    private Uri imageUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analysis, container, false);

        ageInput = view.findViewById(R.id.age_input);
        genderInput = view.findViewById(R.id.gender_input);
        heightInput = view.findViewById(R.id.height_input);
        weightInput = view.findViewById(R.id.weight_input);
        smokingCheckbox = view.findViewById(R.id.smoking_checkbox);
        drinkingCheckbox = view.findViewById(R.id.drinking_checkbox);
        uploadPhotoButton = view.findViewById(R.id.upload_photo_button);
        submitButton = view.findViewById(R.id.submit_button);

        uploadPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitData();
            }
        });

        return view;
    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    private void submitData() {
        String age = ageInput.getText().toString();
        String gender = genderInput.getText().toString();
        String height = heightInput.getText().toString();
        String weight = weightInput.getText().toString();
        boolean isSmoking = smokingCheckbox.isChecked();
        boolean isDrinking = drinkingCheckbox.isChecked();

        if (imageUri == null) {
            Toast.makeText(getContext(), "Please upload a photo", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File imageFile = new File(FileUtils.getPath(getContext(), imageUri));

            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("age", age)
                    .addFormDataPart("gender", gender)
                    .addFormDataPart("height", height)
                    .addFormDataPart("weight", weight)
                    .addFormDataPart("smoking", String.valueOf(isSmoking))
                    .addFormDataPart("drinking", String.valueOf(isDrinking))
                    .addFormDataPart("photo", imageFile.getName(),
                            RequestBody.create(MediaType.parse("image/jpeg"), imageFile))
                    .build();

            Request request = new Request.Builder()
                    .url("http://your_server_url/analyze") // 替换为您的服务器URL
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Submission failed", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Analysis result: " + responseBody, Toast.LENGTH_LONG).show());
                    } else {
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Submission failed", Toast.LENGTH_SHORT).show());
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error uploading photo", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == getActivity().RESULT_OK && data != null) {
            imageUri = data.getData();
            Toast.makeText(getContext(), "Photo uploaded", Toast.LENGTH_SHORT).show();
        }
    }
}
