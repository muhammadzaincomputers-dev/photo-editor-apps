package com.example.photoeditor

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.burhanrashid52.photoeditor.PhotoEditor
import com.burhanrashid52.photoeditor.PhotoEditorView
import com.example.photoeditor.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var photoEditor: PhotoEditor

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val inputStream = contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                binding.photoEditorView.source.setImageBitmap(bitmap)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        photoEditor = PhotoEditor.Builder(this, binding.photoEditorView)
            .setPinchTextScalable(true)
            .build()

        binding.btnPickImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnAddText.setOnClickListener {
            photoEditor.addText("Hello World!", null)
        }

        binding.btnAddFilter.setOnClickListener {
            photoEditor.setFilterEffect(PhotoEditor.FilterEffect.GRAY_SCALE)
        }

        binding.btnSave.setOnClickListener {
            photoEditor.saveAsFile(
                cacheDir.absolutePath + "/edited_photo.png",
                object : PhotoEditor.OnSaveListener {
                    override fun onSuccess(imagePath: String) {
                        // Handle success
                    }
                    override fun onFailure(exception: Exception) {
                        // Handle error
                    }
                }
            )
        }
    }
}
