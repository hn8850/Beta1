package com.example.beta1;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Harel Navon harelnavon2710@gmail.com
 * @version 1.2
 * @since 27/12/2022
 * This Fragment is part of the UploadAd Activity.
 * This Fragment is responsible for collection of general information about the ParkAd (description
 * and images).
 */

public class InfoFrag extends Fragment {

    EditText descEditText;
    Button upload, save, finishButton, clear;
    String desc;
    View view;
    int[] imageViewIds;
    ImageView[] imageViews;
    Uri[] imageUris;
    String[] StringURIs;
    Uri imageUri;
    File photoFile;


    private static final int RESULT_OK = -1;
    private final int PICK_IMAGES_REQUEST_CODE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int MAX_IMAGES = 5;
    private int imagesCounter = 0;

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
        clear = view.findViewById(R.id.clear);
        clear.setOnClickListener(clearButtonListener);
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
        imageUris = new Uri[MAX_IMAGES];

        sharedPrefs = getActivity().getSharedPreferences(PREFS_NAME, PREFS_MODE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("TAB3", "EXISTS");
        return view;
    }


    private View.OnClickListener saveButtonClickListener3 = new View.OnClickListener() {
        /**
         * OnClickMethod for the save Button.
         * This method saves the information submitted by the user to a SharedPrefs file.
         * @param view: The save Button.
         */
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
        /**
         * OnClickMethod for the finish Button.
         * Only clickable and visible after all the necessary params for a ParkAd have been filled
         * throughout all of the Fragments.
         * Uploads all of the images selected to the Storage database and calls the UploadAd Method.
         * @param view: The finish Button.
         */
        @Override
        public void onClick(View view) {
            finishButton.setClickable(false);
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
                if ((imageUris.get(i)).matches("NONE")) {
                    imageUris.remove(i);
                    i--;
                }
            }
            for (int i = 0; i < imageUris.size(); i++) {
                Uri imageUri = Uri.parse(imageUris.get(i));
                if (!((imageUri.toString()).matches("NONE"))) {
                    StorageReference imageRef = refPath.child("Image" + i);
                    imageRef.putFile(imageUri).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // URL of the uploaded image was successfully retrieved
                                    String imageUrl = uri.toString();
                                    imageURLS.add(imageUrl);
                                    int c = count.incrementAndGet();
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

    /**
     * Creates a ParkAd Object with all of the information from the SharedPrefs file, and then
     * uploads the ParkAd to the database.
     *
     * @param imageURLS: ArrayList containing the String URL's for each image that was uploaded.
     */
    public void uploadAd(ArrayList<String> imageURLS) {
        imagesCounter = 0;

        sharedPrefs = getActivity().getSharedPreferences(PREFS_NAME, PREFS_MODE);

        String latitude = sharedPrefs.getString("latitude", "0");
        String longitude = sharedPrefs.getString("longitude", "0");

        String path = (latitude + longitude).replace(".", "");
        FirebaseUser newUser = FirebaseAuth.getInstance().getCurrentUser();
        String userUid = newUser.getUid();
        int Active = 1;
        String Date = sharedPrefs.getString("Date", "0");
        String dateParam = Services.addLeadingZerosToDate(Date, true);
        String BeginHour = sharedPrefs.getString("BeginHour", "0");
        String FinishHour = sharedPrefs.getString("FinishHour", "0");
        Double HourlyRate = Double.valueOf(sharedPrefs.getString("HourlyRate", "0"));
        String Description = sharedPrefs.getString("Description", "No desc");
        String Address = sharedPrefs.getString("address", "0");


        String beginHourKey = "B" + BeginHour.substring(0, 2) + BeginHour.substring(3);
        String endHourKey = "E" + FinishHour.substring(0, 2) + FinishHour.substring(3);
        String hourRangeKey = beginHourKey + endHourKey;
        String dateKey = "D" + Services.addLeadingZerosToDate(Date, false);
        String parkAdKey = path + dateKey + hourRangeKey;

        ParkAd ad = new ParkAd(latitude, longitude, userUid, Active, dateParam, BeginHour, FinishHour, HourlyRate, imageURLS, Description, Address);
        DatabaseReference adRef = mDb.getReference("ParkAds");
        adRef.child(parkAdKey).setValue(ad);
        DatabaseReference userAdRef = mDb.getReference("Users").child(userUid).child("ParkAds").child(parkAdKey);
        userAdRef.setValue(ad);
        Toast.makeText(getActivity().getApplicationContext(), "AD UPLOADED!", Toast.LENGTH_SHORT).show();
        sharedPrefs.edit().clear().apply();

    }


    SharedPreferences.OnSharedPreferenceChangeListener prefsListener3 = new SharedPreferences.OnSharedPreferenceChangeListener() {
        /**
         * SharedPrefsChange Listener. Used to verify that all of the required params for a ParkAd have
         * been filled out and saved by the user.
         * If so, the finish Button becomes visible and clickable.
         * @param sharedPreferences: The SharedPrefs file.
         * @param key:
         */
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


    /**
     * Used to wipe an existing images folder in the Storage database for a ParkAd in order to
     * upload new images.
     *
     * @param path: String containing the locationKey for the ParkAd.
     */
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


    /**
     * OnClickMethod for the upload Button.
     * Used to allow the user to upload images of their ParkAd from their gallery/camera.
     */
    View.OnClickListener uploadButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (imagesCounter >= 5) {
                Toast.makeText(getActivity().getApplicationContext(), "You can only select up to 5 images", Toast.LENGTH_SHORT).show();
                return;
            }

            CharSequence options[] = new CharSequence[]{"Take Photo", "Choose from Gallery"};

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Select Option");
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {
                            // Error occurred while creating the File
                        }
                        if (photoFile != null) {
                            imageUri = FileProvider.getUriForFile(getActivity(), "com.mydomain.fileprovider", photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                            //takePictureIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                        }


                    } else if (which == 1) {
                        Intent pickGalleryIntent = new Intent(Intent.ACTION_PICK);
                        pickGalleryIntent.setType("image/*");
                        pickGalleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

                        startActivityForResult(pickGalleryIntent, PICK_IMAGES_REQUEST_CODE);

                    }
                }
            });
            builder.show();
        }
    };

    /**
     * OnActivityResult Method for the UploadButtonListener.
     * Handles the processing of the images selected by the user and updates the ImageViews
     * accordingly.
     *
     * @param requestCode: The GalleryRequestCode String.
     * @param resultCode:  The GalleryResultCode String.
     * @param data:        The Intent containing the images the user has selected.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGES_REQUEST_CODE) {
                if (data.getClipData() != null) {
                    // Multiple images were selected
                    ClipData clipData = data.getClipData();
                    int numImages = clipData.getItemCount();
                    if (numImages + imagesCounter > MAX_IMAGES) {
                        // Show an error message
                        Toast.makeText(getActivity().getApplicationContext(), "You can only select up to 5 images", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    for (int i = 0; i < numImages; i++) {
                        imageUris[i + imagesCounter] = (clipData.getItemAt(i).getUri());
                    }
                    imagesCounter = imagesCounter + numImages;

                } else {
                    // Only one image was selected
                    imageUris[imagesCounter] = (data.getData());
                    imagesCounter = imagesCounter + 1;
                }
                // Display the images in the ImageViews
                for (int i = 0; i < imageUris.length; i++) {
                    if (imageUris[i] != null) imageViews[i].setImageURI(imageUris[i]);

                }
                for (int i = 0; i < MAX_IMAGES; i++) {
                    if (imageUris[i] != null) {
                        StringURIs[i] = imageUris[i].toString();
                    } else StringURIs[i] = "NONE";
                }

                Toast.makeText(getActivity().getApplicationContext(), "Upload Successful", Toast.LENGTH_SHORT).show();
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                if (imagesCounter >= 5) {
                    Toast.makeText(getActivity().getApplicationContext(), "You can only select up to 5 images", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    imageUri = Uri.fromFile(photoFile);
                    imageUris[imagesCounter] = imageUri;
                    imageViews[imagesCounter].setImageURI(imageUri);
                    imagesCounter++;

                    for (int i = 0; i < MAX_IMAGES; i++) {
                        if (imageUris[i] != null) {
                            StringURIs[i] = imageUris[i].toString();
                        } else StringURIs[i] = "NONE";
                    }
                }

            }
        }

    }

    /**
     * SubMethod for the upload Button OnClickMethod.
     * Used to create a File from any image selected (which will then be converted to a URI).
     *
     * @throws IOException
     * @return: Image File.
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }

    /**
     * OnClickMethod for the clear Button.
     * Used to reset the ImageViews and all the ArrayList's containing image data.
     */
    View.OnClickListener clearButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            imageUris = new Uri[MAX_IMAGES];
            StringURIs = new String[MAX_IMAGES];
            imagesCounter = 0;
            for (int i = 0; i < imageViews.length; i++) {
                imageViews[i].setImageResource(R.drawable.dotsquare);
            }
            sharedPrefs = getActivity().getSharedPreferences(PREFS_NAME, PREFS_MODE);
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString(getString(R.string.prefs_description_key), desc);
            editor.putString(getString(R.string.prefs_URI1_key), StringURIs[0]);
            editor.putString(getString(R.string.prefs_URI2_key), StringURIs[1]);
            editor.putString(getString(R.string.prefs_URI3_key), StringURIs[2]);
            editor.putString(getString(R.string.prefs_URI4_key), StringURIs[3]);
            editor.putString(getString(R.string.prefs_URI5_key), StringURIs[4]);

            editor.apply();

        }
    };

}