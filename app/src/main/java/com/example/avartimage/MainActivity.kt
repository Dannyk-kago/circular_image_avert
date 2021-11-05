package com.example.avartimage

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.avartimage.databinding.ActivityMainBinding
import com.example.avartimage.databinding.CustomDialogBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import java.nio.channels.MulticastChannel

class MainActivity : AppCompatActivity() {
    val image = "https://media.istockphoto.com/photos/portrait-of-a-girl-picture-id938709362?s=612x612"
    private lateinit var activityMainBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        Glide.with(this)
            .load(image)
            .centerCrop()
            .into(activityMainBinding.profileImage)

        activityMainBinding.profileImage.setOnClickListener{
           customDialogImageSelection()
        }

    }

    private fun customDialogImageSelection() {
        val dialog = Dialog(this)
        val binding:CustomDialogBinding = CustomDialogBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

       binding.tvCamera.setOnClickListener {
           Dexter.withContext(this).withPermissions(
               Manifest.permission.READ_EXTERNAL_STORAGE,
               Manifest.permission.CAMERA,
               Manifest.permission.INTERNET
           ).withListener(object : MultiplePermissionsListener{
               override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                   report?.let {
                       if (report.areAllPermissionsGranted()){
                           val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                           startActivityForResult(cameraIntent, CAMERA)
                       }
                   }
               }

               override fun onPermissionRationaleShouldBeShown(
                   permission: MutableList<PermissionRequest>?,
                   tokens: PermissionToken?
               ) {
                   showDialogForPermissions()
               }

           }).onSameThread().check()
           dialog.dismiss()
       }
        dialog.show()

        binding.tvGallery.setOnClickListener {
            Dexter.withContext(this).withPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE
            ).withListener(object: PermissionListener{
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    val galleryIntent = Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                    startActivityForResult(galleryIntent, GALLERY)
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Toast.makeText(this@MainActivity,
                        "You have denied the storage permission to select image",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    tokens: PermissionToken?
                ) {
                    showDialogForPermissions()
                }

            }).onSameThread().check()
            dialog.dismiss()
        }
        dialog.show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if (resultCode == CAMERA){
                data?.extras?.let {
                    val thumbnail : Bitmap = data.extras?.get("data") as Bitmap
                   //activityMainBinding.profileImage.setImageBitmap(thumbnail)

                    Glide.with(this)
                        .load(thumbnail)
                        .centerCrop()
                        .into(activityMainBinding.profileImage)

                }
            }
            if (resultCode == GALLERY){
                data?.let {
                    val selectedPhotoUri = data.data
                    //activityMainBinding.profileImage.setImageURI(selectedPhotoUri)

                    Glide.with(this)
                        .load(selectedPhotoUri)
                        .centerCrop()
                        .into(activityMainBinding.profileImage)
                }
            }
        }else if (requestCode == Activity.RESULT_CANCELED){
            Log.e("cancelled", "User cancelled Image Selection")
        }
    }

    private fun showDialogForPermissions(){
        AlertDialog.Builder(this).setMessage("It looks like you have turned off permissions " +
                "required for this feature. It can be enabled under application settings")
            .setPositiveButton("GO TO SETTINGS"){
                    _,_ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }catch (e: ActivityNotFoundException){
                    e.printStackTrace()
                }
            }

            .setNegativeButton("Cancel"){
                    dialog,_->
                dialog.dismiss()
            }.show()
    }
    companion object{
        private const val CAMERA = 1
        private const val GALLERY = 2
    }
}