package com.example.photoeditor

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.burhanrashid52.photoeditor.PhotoEditor
import com.burhanrashid52.photoeditor.PhotoEditorView
import com.example.photoeditor.databinding.ActivityMainBinding
import com.yalantis.ucrop.UCrop

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var photoEditor: PhotoEditor
    private var currentImageUri: Uri? = null

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                currentImageUri = it
                val inputStream = contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                binding.photoEditorView.source.setImageBitmap(bitmap)
            }
        }

    private val cropImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val resultUri = UCrop.getOutput(result.data!!)
                currentImageUri = resultUri
                binding.photoEditorView.source.setImageURI(resultUri)
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

        binding.btnAddSticker.setOnClickListener {
            // Example assumes you have sticker.png in drawable
            val stickerBitmap = BitmapFactory.decodeResource(resources, R.drawable.sticker)
            photoEditor.addImage(stickerBitmap)
        }

        binding.btnDraw.setOnClickListener {
            photoEditor.setBrushDrawingMode(true)
            photoEditor.brushColor = Color.BLUE
            photoEditor.brushSize = 8f
        }

        binding.btnDisableDraw.setOnClickListener {
            photoEditor.setBrushDrawingMode(false)
        }

        binding.btnClearDrawings.setOnClickListener {
            photoEditor.clearBrushAllViews()
        }

        binding.btnCrop.setOnClickListener {
            currentImageUri?.let { sourceUri ->
                val destinationUri = Uri.fromFile(
                    File(cacheDir, "cropped_${System.currentTimeMillis()}.jpg")
                )
                val intent = UCrop.of(sourceUri, destinationUri)
                    .withAspectRatio(1f, 1f)
                    .withMaxResultSize(500, 500)
                    .getIntent(this)
                cropImage.launch(intent)
            }
        }
    }
}
