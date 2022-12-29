package com.example.beta1;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


public class InfoFrag extends Fragment {

    EditText descEditText;
    Button upload, save, finishButton;
    String desc;
    View view;
    int[] imageViewIds;
    ImageView[] imageViews;
    Uri[] imageUris;
    String[] StringURIs;


    private static final int RESULT_OK = -1;
    private final int PICK_IMAGES_REQUEST_CODE = 1;

    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String formattedDate = dateFormat.format(calendar.getTime());

    private final String PREFS_NAME = "ParkAd" + formattedDate;
    private static final int PREFS_MODE = Context.MODE_PRIVATE;

    private SharedPreferences sharedPrefs;

    FirebaseDatabase mDb;
    FirebaseStorage mStorage;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_info, container, false);

        upload = view.findViewById(R.id.upload);
        upload.setOnClickListener(uploadButtonListener);
        save = view.findViewById(R.id.save3);
        save.setOnClickListener(saveButtonClickListener3);

        finishButton = view.findViewById(R.id.finish4);
        finishButton.setOnClickListener(finishButtonClickListener);


        descEditText = view.findViewById(R.id.description);
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(240);
        descEditText.setFilters(filters);
        imageViewIds = new int[5];
        imageViewIds[0] = R.id.imageView2;
        imageViewIds[1] = R.id.imageView3;
        imageViewIds[2] = R.id.imageView4;
        imageViewIds[3] = R.id.imageView5;
        imageViewIds[4] = R.id.imageView6;

        imageViews = new ImageView[imageViewIds.length];
        for (int i = 0; i < imageViewIds.length; i++) {
            imageViews[i] = view.findViewById(imageViewIds[i]);
        }

        StringURIs = new String[5];
        sharedPrefs = getActivity().getSharedPreferences(PREFS_NAME, PREFS_MODE);
        sharedPrefs.registerOnSharedPreferenceChangeListener(prefsListener3);

        if (sharedPrefs.contains(getString(R.string.prefs_FINISH_key))) {
            finishButton.setVisibility(View.VISIBLE);
            finishButton.setClickable(true);
        }

        if (sharedPrefs.contains(getString(R.string.prefs_URI1_key)) && !getString(R.string.prefs_URI1_key).matches("NONE")) {
            imageViews[0].setImageURI(Uri.parse(sharedPrefs.getString(getString(R.string.prefs_URI1_key), null)));
            StringURIs[0] = sharedPrefs.getString(getString(R.string.prefs_URI1_key), "NONE");
        }
        if (sharedPrefs.contains(getString(R.string.prefs_URI2_key)) && !getString(R.string.prefs_URI2_key).matches("NONE")) {
            imageViews[1].setImageURI(Uri.parse(sharedPrefs.getString(getString(R.string.prefs_URI2_key), null)));
            StringURIs[1] = sharedPrefs.getString(getString(R.string.prefs_URI2_key), "NONE");
        }
        if (sharedPrefs.contains(getString(R.string.prefs_URI3_key)) && !getString(R.string.prefs_URI3_key).matches("NONE")) {
            imageViews[2].setImageURI(Uri.parse(sharedPrefs.getString(getString(R.string.prefs_URI3_key), null)));
            StringURIs[2] = sharedPrefs.getString(getString(R.string.prefs_URI3_key), "NONE");
        }
        if (sharedPrefs.contains(getString(R.string.prefs_URI4_key)) && !getString(R.string.prefs_URI4_key).matches("NONE")) {
            imageViews[3].setImageURI(Uri.parse(sharedPrefs.getString(getString(R.string.prefs_URI4_key), null)));
            StringURIs[3] = sharedPrefs.getString(getString(R.string.prefs_URI4_key), "NONE");
        }
        if (sharedPrefs.contains(getString(R.string.prefs_URI5_key)) && !getString(R.string.prefs_URI5_key).matches("NONE")) {
            imageViews[4].setImageURI(Uri.parse(sharedPrefs.getString(getString(R.string.prefs_URI5_key), null)));
            StringURIs[4] = sharedPrefs.getString(getString(R.string.prefs_URI5_key), "NONE");

        }

        for (int i = 0; i < imageViewIds.length; i++) {
            if (imageViews[i].getDrawable() == null)
                imageViews[i].setImageResource(R.drawable.dotsquare);
        }

        mDb = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();

        return view;
    }


    private View.OnClickListener saveButtonClickListener3 = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            desc = descEditText.getText().toString();
            if (desc.isEmpty()) desc = "No description";
            if (imageUris == null || imageUris[0] == null) {
                Toast.makeText(getActivity().getApplicationContext(), "Choose at least one picture!", Toast.LENGTH_SHORT).show();
            } else {
                sharedPrefs = getActivity().getSharedPreferences(PREFS_NAME, PREFS_MODE);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(getString(R.string.prefs_description_key), desc);
                editor.putString(getString(R.string.prefs_URI1_key), StringURIs[0]);
                editor.putString(getString(R.string.prefs_URI2_key), StringURIs[1]);
                editor.putString(getString(R.string.prefs_URI3_key), StringURIs[2]);
                editor.putString(getString(R.string.prefs_URI4_key), StringURIs[3]);
                editor.putString(getString(R.string.prefs_URI5_key), StringURIs[4]);

                editor.apply();
                Toast.makeText(getActivity().getApplicationContext(), "INFO SAVED!", Toast.LENGTH_SHORT).show();

            }

        }
    };


    private View.OnClickListener finishButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            sharedPrefs = getActivity().getSharedPreferences(PREFS_NAME, PREFS_MODE);
            String latitude = sharedPrefs.getString("latitude", "0");
            String longitude = sharedPrefs.getString("longitude", "0");

            List<String> imageUris = new ArrayList<>();
            if (sharedPrefs.contains(getString(R.string.prefs_URI1_key))) {
                imageUris.add(sharedPrefs.getString("URI1", null));
                imageUris.add(sharedPrefs.getString("URI2", null));
                imageUris.add(sharedPrefs.getString("URI3", null));
                imageUris.add(sharedPrefs.getString("URI4", null));
                imageUris.add(sharedPrefs.getString("URI5", null));
            }

            String path = (latitude + longitude).replace(".", "");
            StorageReference refStorage = mStorage.getReference("ParkAdPics");
            StorageReference refPath = refStorage.child(path);
            ReCreateFolder(path);

            final AtomicInteger count = new AtomicInteger();
            ArrayList<String> imageURLS = new ArrayList<>();
            for (int i = 0; i < imageUris.size(); i++) {
                System.out.println("TEST = " + i + " --> " + imageUris.get(i));
                if ((imageUris.get(i)).matches("NONE")) {
                    imageUris.remove(i);
                    i--;
                }
            }
            System.out.println("SIZE = " + imageUris.size());
            ArrayList<UploadTask> tasks = new ArrayList<>();
            for (int i = 0; i < imageUris.size(); i++) {
                Uri imageUri = Uri.parse(imageUris.get(i));
                System.out.println("URI + = " + imageUri.toString());
                if (!((imageUri.toString()).matches("NONE"))) {
                    StorageReference imageRef = refPath.child("Image" + i);
                    imageRef.putFile(imageUri).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            System.out.println("ERROR = " + exception);
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            System.out.println("WHATSTSTS");
                            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // URL of the uploaded image was successfully retrieved
                                    String imageUrl = uri.toString();
                                    imageURLS.add(imageUrl);
                                    System.out.println("image url = " + imageUrl);

                                    int c = count.incrementAndGet();
                                    System.out.println("C = " + c);
                                    // check if all images have been uploaded
                                    if (c == imageUris.size()) {
                                        // all images have been uploaded
                                        // execute the callback function
                                        uploadAd(imageURLS);
                                    }
                                }
                            });
                        }
                    });

                }
            }
        }
    };

    public void uploadAd(ArrayList<String> imageURLS) {

        sharedPrefs = getActivity().getSharedPreferences(PREFS_NAME, PREFS_MODE);

        String latitude = sharedPrefs.getString("latitude", "0");
        String longitude = sharedPrefs.getString("longitude", "0");

        String path = (latitude + longitude).replace(".", "");

        FirebaseUser newUser = FirebaseAuth.getInstance().getCurrentUser();
        String userUid = newUser.getUid();
        int Active = 1;
        String Date = sharedPrefs.getString("Date", "0");
        String BeginHour = sharedPrefs.getString("BeginHour", "0");
        String FinishHour = sharedPrefs.getString("FinishHour", "0");
        Double HourlyRate = Double.valueOf(sharedPrefs.getString("HourlyRate", "0"));
        String Description = sharedPrefs.getString("Description", "No desc");
        String Address = sharedPrefs.getString("address", "0");

        ParkAd ad = new ParkAd(latitude, longitude, userUid, Active, Date, BeginHour, FinishHour, HourlyRate, Description, Address);
        DatabaseReference adRef = mDb.getReference("ParkAds");
        adRef.child(path).setValue(ad);
        adRef.child(path).child("IMAGE URLS").setValue(imageURLS);
        Toast.makeText(getActivity().getApplicationContext(), "AD UPLOADED!", Toast.LENGTH_SHORT).show();

    }


    public void ReCreateFolder(String path) {
        StorageReference refStorage = mStorage.getReference("ParkAdPics");
        StorageReference refPath = refStorage.child(path);

// Call listAll() to get a list of all items in the folder
        refPath.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                // Check if the folder exists by checking if it is present in the list
                if (listResult.getItems().size() > 0) {
                    // The folder exists, so you can do something here
                    // For example, you can delete its contents by calling delete() on each of the items in the list
                    for (StorageReference item : listResult.getItems()) {
                        item.delete();
                    }
                } else {
                    return;
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle errors
            }
        });

    }


    SharedPreferences.OnSharedPreferenceChangeListener prefsListener3 = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (sharedPreferences.contains("address") && sharedPreferences.contains("latitude") && sharedPreferences.contains("longitude")) {
                if (sharedPreferences.contains("Date") && sharedPreferences.contains("BeginHour") && sharedPreferences.contains("FinishHour") && sharedPreferences.contains("HourlyRate")) {
                    if (sharedPreferences.contains("Description") && sharedPreferences.contains("URI1")) {
                        finishButton.setVisibility(View.VISIBLE);
                        finishButton.setClickable(true);
                        SharedPreferences.Editor editor = sharedPrefs.edit();
                        editor.putString(getString(R.string.prefs_FINISH_key), "gogogo");
                        editor.apply();

                    } else {
                        finishButton.setVisibility(View.INVISIBLE);
                        finishButton.setClickable(false);
                    }
                } else {
                    finishButton.setVisibility(View.INVISIBLE);
                    finishButton.setClickable(false);
                }

            } else {
                finishButton.setVisibility(View.INVISIBLE);
                finishButton.setClickable(false);
            }

        }
    };


    View.OnClickListener uploadButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(intent, PICK_IMAGES_REQUEST_CODE);

        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGES_REQUEST_CODE && resultCode == RESULT_OK) {
            // Get the selected images from the intent
            if (data.getClipData() != null) {
                // Multiple images were selected
                ClipData clipData = data.getClipData();
                imageUris = new Uri[5];
                int numImages = clipData.getItemCount();
                if (numImages > 5) {
                    // Show an error message
                    Toast.makeText(getActivity().getApplicationContext(), "You can only select up to 5 images", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    imageUris[i] = (clipData.getItemAt(i).getUri());
                }
            } else {
                // Only one image was selected
                imageUris[0] = (data.getData());
            }


            // Display the images in the ImageViews
            for (int i = 0; i < imageUris.length; i++) {
                if (imageUris[i] != null) imageViews[i].setImageURI(imageUris[i]);

            }
            for (int i = 0; i < 5; i++) {
                if (imageUris[i] != null) {
                    StringURIs[i] = imageUris[i].toString();
                } else StringURIs[i] = "NONE";
            }


            Toast.makeText(getActivity().getApplicationContext(), "Upload Successful", Toast.LENGTH_SHORT).show();


        }
    }


}