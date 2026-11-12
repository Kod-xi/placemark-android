package org.setu.placemark

import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.setu.placemark.models.PlacedMark
import org.setu.placemark.AppData
import android.net.Uri
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.FlowPreview


class AddEditActivity : AppCompatActivity() {

    private lateinit var titleInput: EditText
    private lateinit var imagePreview: ImageView
    private lateinit var descriptionInput: EditText
    private lateinit var xInput: EditText
    private lateinit var yInput: EditText
    private var editingId: Long? = null
    private var selectedImageUri: Uri? = null

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) {
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: SecurityException) {
                    // Some providers don't allow persistable access.
                }
                selectedImageUri = uri
                imagePreview.setImageURI(uri)
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createUserInterface()

        editingId = intent.getLongExtra("id", -1L)

        if (editingId != -1L) {
            loadExistingMark(editingId!!)
        }
    }

    private fun createUserInterface() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 320, 32, 32)
        }

        titleInput = EditText(this).apply {
            hint = "Title"
        }

        descriptionInput = EditText(this).apply {
            hint = "Description"
        }

        xInput = EditText(this).apply {
            hint = "X coordinate"
            inputType =
                InputType.TYPE_CLASS_NUMBER or
                        InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        yInput = EditText(this).apply {
            hint = "Y coordinate"
            inputType =
                InputType.TYPE_CLASS_NUMBER or
                        InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        val heightPx = (180 * resources.displayMetrics.density).toInt()
        imagePreview = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                heightPx
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            adjustViewBounds = true
            setImageResource(android.R.drawable.ic_menu_gallery)
        }

        val pickImageButton = Button(this).apply {
            text = "Select Image"
            setOnClickListener {
                imagePicker.launch(arrayOf("image/*"))
            }
        }

        val saveButton = Button(this).apply {
            text = "Save"

            setOnClickListener {
                saveMark()
            }
        }

        val cancelButton = Button(this).apply {
            text = "Cancel"

            setOnClickListener {
                finish()
            }
        }

        root.addView(titleInput)
        root.addView(descriptionInput)
        root.addView(xInput)
        root.addView(yInput)
        root.addView(imagePreview)
        root.addView(pickImageButton)
        root.addView(saveButton)
        root.addView(cancelButton)

        setContentView(root)
    }

    private fun loadExistingMark(id: Long) {

        val mark = AppData.placedMarks.findOne(id)

        if (mark == null) {
            Toast.makeText(
                this,
                "Mark not found",
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        titleInput.setText(mark.title)
        descriptionInput.setText(mark.desc)
        xInput.setText(mark.x.toString())
        yInput.setText(mark.y.toString())

        selectedImageUri = mark.imageUri?.let { Uri.parse(it) }

        if (selectedImageUri != null) {
            imagePreview.setImageURI(selectedImageUri)
        } else {
            imagePreview.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    private fun saveMark() {

        val title = titleInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()

        if (title.isEmpty()) {
            titleInput.error = "Title is required"
            return
        }

        val x = xInput.text.toString().toDoubleOrNull()

        if (x == null) {
            xInput.error = "Enter a valid number"
            return
        }

        val y = yInput.text.toString().toDoubleOrNull()

        if (y == null) {
            yInput.error = "Enter a valid number"
            return
        }

        try{
            if (editingId == null || editingId == -1L) {

                val mark = PlacedMark(
                    title = title,
                    desc = description,
                    x = x,
                    y = y,
                    imageUri = selectedImageUri?.toString()
                )

                AppData.placedMarks.create(mark)

                Toast.makeText(
                    this,
                    "Mark created",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                val mark = PlacedMark(
                    id = editingId!!,
                    title = title,
                    desc = description,
                    x = x,
                    y = y,
                    imageUri = selectedImageUri?.toString()
                )

                AppData.placedMarks.update(mark)

                Toast.makeText(
                    this,
                    "Mark updated",
                    Toast.LENGTH_SHORT
                ).show()
            }

            finish()
        } catch (e: IllegalArgumentException) {
            // Range errors from validate(): stay on the screen
            Toast.makeText(
                this,
                e.message ?: "Please check the entered values.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}