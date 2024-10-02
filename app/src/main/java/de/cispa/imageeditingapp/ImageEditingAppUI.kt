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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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

    //State to hold the selected filter
    var selectedFilter by remember { mutableStateOf<String>("Apply Filter") }

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
        //ImageView to display the selected image along with a cancel icon
        Box(modifier = Modifier.size(300.dp)){
            if(selectedImage != null){
                Image(
                    bitmap = selectedImage!!.asImageBitmap(),
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .size(300.dp)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(2.dp, Color.Gray, RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                //Cancel icon over the image to allow removing it
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Cancel Image",
                    modifier = Modifier
                        .padding(8.dp)
                        .size(24.dp)
                        .clip(
                            CircleShape
                        )
                        .clickable { selectedImage = null }
                        .background(Color.White),
                    tint = Color.Black
                )
            } else {
                //Placeholder Text when no image is selected
                Text(text = "No Image Selected", modifier = Modifier.padding(16.dp))
            }
        }

        //Show FilterOptionsDropdown only if an image is selected
        if (selectedImage != null) {
            FilterOptionsDropDown(selectedFilter) { newFilter ->
                selectedFilter = newFilter
            }
        }


        //Button to select an image
        Button(onClick = {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
            selectedFilter = "Apply Filter"
        }) {
            Text(text = "Select Image")
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

@Composable
fun FilterOptionsDropDown(selectedFilter: String, onFilterSelected: (String)-> Unit) {
    //Available filters
    val filters = listOf("Grayscale", "Brightness", "Contrast")

    //Dropdown state
    var expanded by remember { mutableStateOf(false) }

    //Box to position the dropdown
    Box {
        //Button to display the selected filter
        Button(onClick = { expanded = true }) {
            Text(text = selectedFilter)
        }

        //DropdownMenu for filter selection
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {expanded = false}
        ) {
            filters.forEach { filter ->
                DropdownMenuItem(
                    text = { Text(text = filter) },         //Provide the text for the dropdown item
                    onClick = {
                        onFilterSelected(filter)            //Update the selected filter
                        expanded = false                    //Close the dropdown
                    })
            }
        }
    }
}

