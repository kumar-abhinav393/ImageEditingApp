package de.cispa.imageeditingapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun ImageEditingAppUI(){
    //State to hold the selected image URI or Bitmap
    var selectedImage by remember { mutableStateOf<Bitmap?>(null) }
    //Track if a filter has been applied
    var isFilterApplied by remember { mutableStateOf(false) }
    //State to hold the selected filter
    var selectedFilter by remember { mutableStateOf("Select Filter") }
    //State to control dropdown expansion
    var expanded by remember { mutableStateOf(false) }

    //Get the current context in the composable scope
    val context = LocalContext.current

    //Launcher for Activity Result to select an image
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val imageUri: Uri? = data?.data
            if (imageUri != null) {

                //Convert URI to Bitmap and store it in selectedImage
                selectedImage = getBitmapFromUri(context, imageUri)
            }
        }
    }

    //Column to arrange UI components vertically
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //ImageView to display the selected image
        Box(modifier = Modifier
            .size(300.dp)
            .padding(16.dp)
            .border(2.dp, Color.Gray, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center){
            if(selectedImage != null){
                //Display the selected image
                Image(
                    bitmap = selectedImage!!.asImageBitmap(),
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                //Transplant Delete Icon overlay on the top-right corner
                IconButton(onClick = { selectedImage = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(Color.Transparent)
                        .size(40.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Image", tint = Color.White)
                }
            } else {
                //Placeholder Text when no image is selected
                Text(text = "No Image Selected", modifier = Modifier.padding(16.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        //Display filter options only when an image is selected
        if(selectedImage != null){
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly, 
                verticalAlignment = Alignment.CenterVertically
            ) {
                //Dropdown for filter selection
                Box {
                    Button(onClick = { expanded = true }) {
                        Text(text = selectedFilter)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        listOf("Grayscale", "Brightness", "Contrast").forEach {filter ->
                            DropdownMenuItem(text = { Text(text = filter) },
                                onClick = {
                                    selectedFilter = filter
                                    expanded = false
                                })
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                //Apply Filter button
                Button(onClick = { 
                    if (selectedFilter != "Select Filter") {
                        //TODO: Apply the selected filter
                        isFilterApplied = true
                    }
                }) {
                    Text(text = "Apply filter")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        //Change button text based on whether the filter has been applied
        Button(onClick = {
            if (!isFilterApplied) {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                imagePickerLauncher.launch(intent)
                selectedFilter = "Apply Filter"
            } else {
                //Logic for saving the image
                //Todo: Save the processed image and the log the operation
            }
        }) {
            Text(text = if(!isFilterApplied) "Select Image" else "Save Image")
        }
    }
}

//Function to convert URI to Bitmap
fun getBitmapFromUri(context: android.content.Context, uri: Uri): Bitmap {
    return if (Build.VERSION.SDK_INT < 28) {
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    } else {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source)
    }
}


