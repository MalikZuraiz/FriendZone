package com.example.friendzone;

import static android.app.Activity.RESULT_OK;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;
import androidx.fragment.app.Fragment;

import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.Manifest;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    FirebaseUser user;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase database;
    DatabaseReference databaseReference;

    ImageView avatarTv,coverTV;

    FloatingActionButton FabTv;

    TextView nameTv,emailTv,phoneTv;

    ProgressDialog pd;

    View view;

    Uri image_uri;

    String photoCov;

    StorageReference storageReference;

    String storagePath = "Users_Profile_Cover_Imgs/";


    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_REQUEST_CODE = 400;

    String cameraPermission[];
    String storagePermission[];

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("Users");
        storageReference = getInstance().getReference();

        pd = new ProgressDialog(getActivity());


        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        phoneTv = view.findViewById(R.id.phoneTV);
        emailTv = view.findViewById(R.id.emailTV);
        avatarTv = view.findViewById(R.id.avatarTV);
        nameTv = view.findViewById(R.id.nameTV);
        coverTV = view.findViewById(R.id.coverTv);
        FabTv = view.findViewById(R.id.fab);

        //get data using nodes...

        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                for (DataSnapshot ds : snapshot.getChildren())
                {

                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();


                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);

                    try {
                        Picasso.get().load(image).into(avatarTv);

                    }
                    catch (Exception e)
                    {
                        Picasso.get().load(R.drawable.ic_addphoto).into(avatarTv);
                    }

                    try {
                        Picasso.get().load(cover).into(coverTV);

                    }
                    catch (Exception e)
                    {
                        Picasso.get().load(R.drawable.ic_addphoto).into(coverTV);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FabTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditProfileDialog();
            }
        });






        return view;
    }

    private boolean checkStoragePermission()
    {
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission()
    {
        requestPermissions(storagePermission,STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission()
    {
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void requestCameraPermission()
    {
        requestPermissions(cameraPermission,CAMERA_REQUEST_CODE);
    }



    private void showEditProfileDialog() {
        String option[] = {"Edit Profile Photo","Edit Cover Photo","Edit Name","Edit Phone"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Choose Action");

        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == 0)
                {
                    pd.setMessage("Updating Profile Photo");
                    photoCov = "image";
                    showImagePicDialog();

                }
                else if(i == 1)
                {
                    pd.setMessage("Updating Cover Photo");
                    photoCov = "cover";
                    showImagePicDialog();

                }
                else if(i == 2)
                {
                    pd.setMessage("Updating Name");

                    changeNameDialog("name");

                }
                else if(i == 3)
                {

                    pd.setMessage("Updating Phone");
                    changeNameDialog("phone");

                }
            }
        });

        builder.create().show();
    }

    private void changeNameDialog(final String key) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Updating" + key);

        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(linearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);

        EditText editText = new EditText(getActivity());
        editText.setHint("Enter" + key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                String value = editText.getText().toString().trim();

                if(!TextUtils.isEmpty(value))
                {
                    pd.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key,value);

                    databaseReference.child(user.getUid()).updateChildren(result).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Updated....", Toast.LENGTH_SHORT).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });


                }
                else {
                    Toast.makeText(getActivity(), "Please Enter " + key, Toast.LENGTH_SHORT).show();
                }

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

            }
        });


        builder.create().show();




    }

    private void showImagePicDialog() {

        String option[] = {"Camera","Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Pick from ");

        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == 0)
                {

                    if(!checkCameraPermission())
                    {
                        requestCameraPermission();
                    }
                    else
                    {
                        pickfromCamera();
                    }


                }
                else if(i == 1)
                {
                    if(!checkStoragePermission())
                    {
                        requestStoragePermission();
                    }
                    else
                    {
                        pickfromGallery();
                    }


                }
            }
        });

        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length >= 0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writestorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && writestorageAccepted)
                    {
                        pickfromCamera();
                    }
                    else
                    {
                        Toast.makeText(getActivity(), "PLEASE ENABLE CAMERA & STORAGE PERMISSION", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length >= 1){
                    boolean writestorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(writestorageAccepted)
                    {
                        pickfromGallery();
                    }
                    else
                    {
                        Toast.makeText(getActivity(), "PLEASE ENABLE STORAGE PERMISSION", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;


        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode == RESULT_OK)
        {
            if (requestCode == IMAGE_PICK_GALLERY_REQUEST_CODE)
            {
                image_uri = data.getData();
                uploadphoto(image_uri);
            }
            if (requestCode == IMAGE_PICK_CAMERA_REQUEST_CODE)
            {
                uploadphoto(image_uri);
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadphoto(Uri image_uri) {


        pd.show();

        String filepathname = storagePath + "" + photoCov + user.getUid();

        StorageReference storageReference2nd = storageReference.child(filepathname);

        storageReference2nd.putFile(image_uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                Uri downloaduri = uriTask.getResult();

                if(uriTask.isSuccessful()){
                    HashMap<String, Object> results = new HashMap<>();
                    results.put(photoCov, downloaduri.toString());

                    databaseReference.child(user.getUid()).updateChildren(results).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Image Updated", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Error Updating", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });


    }

    private void pickfromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,IMAGE_PICK_GALLERY_REQUEST_CODE);
    }

    private void pickfromCamera() {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");

        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_REQUEST_CODE);
        
    }
}