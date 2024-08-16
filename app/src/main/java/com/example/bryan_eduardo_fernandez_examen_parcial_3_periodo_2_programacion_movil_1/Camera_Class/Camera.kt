package com.example.bryan_eduardo_fernandez_examen_parcial_3_periodo_2_programacion_movil_1.Camera_Class

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class Camera(private val context: Context) {
    companion object {
        const val CAMERA_PERMISSION_REQUEST_CODE = 1001;
    }

    var photoURI: Uri? = null;

    fun dispatchTakePictureIntent(): Intent? {
        val takePictureIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(context.packageManager) != null) {
            // Create the File where the photo should go
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
                null
            }
            // Continue only if the File was successfully created
            photoFile?.also {
                photoURI = FileProvider.getUriForFile(
                    context, "com.example.bryan_eduardo_fernandez_examen_parcial_3_periodo_2_programacion_movil_1.fileprovider", it
                )
                takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoURI)
                return takePictureIntent
            }
        }
        return null
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    fun getBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (error: IOException) {
            error.printStackTrace();

            null;
        }
    }

    fun checkCameraPermission(activity: Activity): Boolean {
        return if (ContextCompat.checkSelfPermission(
                activity, android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            );

            false;
        } else {
            true;
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        grantResults: IntArray,
        onPermissionGranted: () -> Unit,
        onPermissionDenied: () -> Unit
    ) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted()
            } else {
                onPermissionDenied()
            }
        }
    }

    fun subirImageFirebaseStorage(
        onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit
    ) {

        photoURI?.let {
            val storageReference: StorageReference =
                FirebaseStorage.getInstance().reference.child("ImagenesDePersonas/${System.currentTimeMillis()}");

            storageReference.putFile(it).addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString());
                }
            }.addOnFailureListener { exception ->
                onFailure(exception);
                Log.e("Error", exception.toString());
            }
        }
    }

    var currentPhotoPath: String? = null

    fun getPhotoUri(): Uri? {
        return photoURI
    }
}