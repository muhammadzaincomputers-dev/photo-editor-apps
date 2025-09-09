package com.example.photoeditor

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.burhanrashid52.photoeditor.PhotoEditor
import com.example.photoeditor.databinding.ActivityMainBinding
import com.yalantis.ucrop.UCrop

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var photoEditor: PhotoEditor
    private var currentImageUri: Uri? = null

    // Add your stickers here (PNG files in drawable)
    private val stickerResIds = listOf(
        R.drawable.sticker1,
        R.drawable.sticker2,
        R.drawable.sticker3
    )

    // Add your colors here
    private val colors = listOf(
        Color.BLACK, Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.MAGENTA
    )

    private val colorNames = listOf(
        "Black", "Red", "Blue", "Green", "Yellow", "Magenta"
    )

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
            showTextColorPicker()
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

        binding.btnStickerPicker.setOnClickListener {
            showStickerPicker()
        }

        binding.btnDraw.setOnClickListener {
            showBrushControls()
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
                    java.io.File(cacheDir, "cropped_${System.currentTimeMillis()}.jpg")
                )
                val intent = UCrop.of(sourceUri, destinationUri)
                    .withAspectRatio(1f, 1f)
                    .withMaxResultSize(500, 500)
                    .getIntent(this)
                cropImage.launch(intent)
            }
        }
    }

    // Sticker Picker Dialog
    private fun showStickerPicker() {
        val stickerNames = stickerResIds.indices.map { "Sticker ${it + 1}" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, stickerNames)
        AlertDialog.Builder(this)
            .setTitle("Pick a Sticker")
            .setAdapter(adapter) { _, which ->
                val bitmap = BitmapFactory.decodeResource(resources, stickerResIds[which])
                photoEditor.addImage(bitmap)
            }
            .show()
    }

    // Brush Controls Dialog (color + size)
    private fun showBrushControls() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_brush_controls, null)
        val seekBar = dialogView.findViewById<SeekBar>(R.id.seekBrushSize)
        val colorListView = dialogView.findViewById<android.widget.ListView>(R.id.listColors)

        // Set up color picker
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, colorNames)
        colorListView.adapter = adapter
        var selectedColor = colors[0]
        colorListView.setOnItemClickListener { _, _, pos, _ ->
            selectedColor = colors[pos]
        }

        seekBar.progress = 10 // Default brush size

        AlertDialog.Builder(this)
            .setTitle("Brush Controls")
            .setView(dialogView)
            .setPositiveButton("Apply") { _, _ ->
                photoEditor.setBrushDrawingMode(true)
                photoEditor.brushColor = selectedColor
                photoEditor.brushSize = seekBar.progress.toFloat()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Text Color Picker Dialog
    private fun showTextColorPicker() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, colorNames)
        AlertDialog.Builder(this)
            .setTitle("Text Color")
            .setAdapter(adapter) { _, which ->
                photoEditor.addText("Hello World!", colors[which])
            }
            .show()
    }
}
