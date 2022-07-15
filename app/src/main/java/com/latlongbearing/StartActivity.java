package com.latlongbearing;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;


public class StartActivity extends AppCompatActivity  implements AdapterView.OnItemClickListener {

    List<GridViewItem> gridItems;
    Button viewImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        File folder = new File(Environment.getExternalStorageDirectory() + "/FarsiteGPS");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        setGridAdapter("/storage/emulated/0/FarsiteGPS/");
        viewImages=findViewById(R.id.viewImages);
        viewImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFile();
            }
        });

    }

    @SuppressLint("QueryPermissionsNeeded")
    public void openFile() {
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setType("image/*");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    public void gotoCaptureActivity(View view){
        startActivity(new Intent(this,CaptureImage.class));
    }

    /**
     * This will create our GridViewItems and set the adapter
     *
     * @param path
     *            The directory in which to search for images
     */
    private void setGridAdapter(String path) {
        // Create a new grid adapter
        gridItems = createGridItems(path);
        if(gridItems!=null) {
            MyGridAdapter adapter = new MyGridAdapter(this, gridItems);

            // Set the grid adapter
            GridView gridView = (GridView) findViewById(R.id.gridView);
            gridView.setAdapter(adapter);

            // Set the onClickListener
            gridView.setOnItemClickListener(this);
        }
    }


    /**
     * Go through the specified directory, and create items to display in our
     * GridView
     */
    private List<GridViewItem> createGridItems(String directoryPath) {
        List<GridViewItem> items = new ArrayList<GridViewItem>();

        if(items.size()!=0) {
            // List all the items within the folder.
            File[] files = new File(directoryPath).listFiles(new ImageFileFilter());
            for (File file : files) {

                // Add the directories containing images or sub-directories
                if (file.isDirectory()
                        && file.listFiles(new ImageFileFilter()).length > 0) {

                    items.add(new GridViewItem(file.getAbsolutePath(), true, null));
                }
                // Add the images
                else {
                    Bitmap image = BitmapHelper.decodeBitmapFromFile(file.getAbsolutePath(),
                            50,
                            50);
                    items.add(new GridViewItem(file.getAbsolutePath(), false, image));
                }
            }
        }

        return items;
    }


    /**
     * Checks the file to see if it has a compatible extension.
     */
    private boolean isImageFile(String filePath) {
        if (filePath.endsWith(".jpg") || filePath.endsWith(".png"))
        // Add other formats as desired
        {
            return true;
        }
        return false;
    }


    @Override
    public void
    onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (gridItems.get(position).isDirectory()) {
            setGridAdapter(gridItems.get(position).getPath());
        }
        else {
            // Display the image
        }

    }

    /**
     * This can be used to filter files.
     */
    private class ImageFileFilter implements FileFilter {

        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            else if (isImageFile(file.getAbsolutePath())) {
                return true;
            }
            return false;
        }
    }

}