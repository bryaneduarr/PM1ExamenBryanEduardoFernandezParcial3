package com.example.bryan_eduardo_fernandez_examen_parcial_3_periodo_2_programacion_movil_1.Camera_Class

import android.app.Activity
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class Camera_Helper(
    private val activity: AppCompatActivity, private val imageView: ImageView
) {
    private val classCamera: Camera = Camera(activity)

     val tomarFotoVal =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
            if (resultado.resultCode === Activity.RESULT_OK) {
                val photoUri = classCamera.getPhotoUri();

                photoUri?.let {
                    val bitmap = classCamera.getBitmapFromUri(it);

                    imageView.setImageBitmap(bitmap);
                }
            }
        }

    fun tomarFoto() {
        if (classCamera.checkCameraPermission(activity)) {
            val takePictureIntent = classCamera.dispatchTakePictureIntent()
            if (takePictureIntent != null) {
                tomarFotoVal.launch(takePictureIntent)
            }
        }
    }
}