package com.janluke.modernartui;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.android.modernartui.R;
import com.janluke.modernartui.colors.ColorSampler;
import com.janluke.modernartui.colors.GoldenRatioColorSampler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class ShowArtworkActivity extends AppCompatActivity {

    static final String TAG = ShowArtworkActivity.class.getSimpleName();
    static final float DEFAULT_SATURATION = 0.5f;
    static final float DEFAULT_BRIGHTNESS = 1f;
    static final float HUE_OFFSET_ON_CLICK = .13f;

    static final int DEFAULT_DEPTH_LIMIT = 2;
    static final int MIN_DEPTH_LIMIT = 2;

    static final int DEFAULT_GRID_SIZE_IN_DP = 30;

    ModernArtworkGenerator artworkGenerator;
    ModernArtwork artwork;
    ColorSampler colorSampler;

    FrameLayout artworkFrame;
    SeekBar saturationBar;
    SeekBar depthLimitBar;
    SeekBar gridSizeBar;
    Button newArtworkButton;
    Button recolorButton;
    ImageView gridSizeImageView;

    float saturation;
    int num_saturation_levels;
    float min_saturation = 0.f;
    float max_saturation = 1.f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_artwork);

        // Find ID of relevant views
        artworkFrame = findViewById(R.id.artwork_frame);

        saturationBar = findViewById(R.id.saturation_seek_bar);
        depthLimitBar = findViewById(R.id.max_depth_seek_bar);
        gridSizeBar = findViewById(R.id.grid_size_seek_bar);

        newArtworkButton = findViewById(R.id.new_artwork_button);
        recolorButton = findViewById(R.id.recolor_button);
        gridSizeImageView = findViewById(R.id.grid_icon);

        initSaturationComponents();
        initDepthLimitChange();
        initGridSizeComponents();

        // OnClickListeners
        recolorButton.setOnClickListener( view -> recolorArtwork() );
        newArtworkButton.setOnClickListener( view -> createNewArtwork() );

        // Color sampler
        colorSampler = new GoldenRatioColorSampler.Builder()
                            .withFixedSaturation(DEFAULT_SATURATION)
                            .withFixedBrightness(DEFAULT_BRIGHTNESS)
                            .build();

        // Generate the artwork
        artworkGenerator = new ModernArtworkGenerator();
        artworkGenerator.setStrokeWidthInDp(DEFAULT_GRID_SIZE_IN_DP);
        createNewArtwork();
    }

    void createNewArtwork() {
        Log.i(TAG, "Create new");
        artwork = artworkGenerator.generateArtwork(artworkFrame, colorSampler);
        onDepthLimitChange(depthLimitBar.getProgress());
        artwork.setOnNodesClickListener(node -> {
            float newHue = node.getHue() / 360f + HUE_OFFSET_ON_CLICK;
            newHue = 360f * (newHue - (int) newHue);
            node.setHue(newHue);
        });
    }

    float scale(float x, float min, float max, float destMin, float destMax) {
        return (x - min) / (max - min) *  (destMax - destMin) + destMin;
    }

    void initSaturationComponents() {
        saturation = DEFAULT_SATURATION;
        // Set the progress
        Resources res = getResources();
        num_saturation_levels = res.getInteger(R.integer.saturation_seekbar_max);
        int saturation_level = (int) scale(DEFAULT_SATURATION,
                                             min_saturation, max_saturation,
                                            0.f, num_saturation_levels);
        saturationBar.setProgress(saturation_level);

        // Saturation bar listener
        saturationBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
             @Override
             public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                 onSaturationChange(i);
             }
             @Override public void onStartTrackingTouch(SeekBar seekBar) {}
             @Override public void onStopTrackingTouch(SeekBar seekBar) {}
         }
        );
    }

    void onSaturationChange(int level) {
        saturation = scale(level, 0.f, num_saturation_levels, min_saturation, max_saturation);
        artwork.setSaturation(saturation);
        colorSampler.keepSaturationFixedTo(saturation);
        Log.i(TAG, "Setting saturation to " + saturation + " (level " + level + ")");
    }


    void initDepthLimitChange() {
        depthLimitBar.setMax(ModernArtworkGenerator.DEFAULT_MAX_DEPTH - MIN_DEPTH_LIMIT);
        depthLimitBar.setProgress(DEFAULT_DEPTH_LIMIT - MIN_DEPTH_LIMIT);

        // Saturation bar listener
        depthLimitBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                onDepthLimitChange(i);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    void onDepthLimitChange(int depth) {
        artwork.setDepthLimit(depth + MIN_DEPTH_LIMIT);
        Log.i(TAG, "Setting max depth to " + depth);
    }

    void initGridSizeComponents() {
        // Saturation bar listener
        gridSizeBar.setProgress(DEFAULT_GRID_SIZE_IN_DP);
        gridSizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int size, boolean b) {
                onGridSizeChange(size);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

    }

    void updateGridSizeImageView(int size) {
        if (size == 0)
            gridSizeImageView.setImageResource(R.drawable.ic_grid_off_black_36dp);
        else
            gridSizeImageView.setImageResource(R.drawable.ic_grid_on_black_36dp);
    }

    void onGridSizeChange(int marginInDp) {
        Log.i(TAG, "Setting grid size to " + marginInDp + " dp");
        updateGridSizeImageView(marginInDp);
        artworkGenerator.setStrokeWidthInDp(marginInDp);
        artwork.setStrokeWidth(marginInDp);
    }

    void recolorArtwork() {
        Log.i(TAG, "Recolor");
        artwork.recolor(colorSampler);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.show_artwork_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.more_info_menu_item:
                InfoDialogFragment infoDialogFragment = InfoDialogFragment.newInstance();
                infoDialogFragment.show(getFragmentManager(), "More info");
                break;

            case R.id.save_menu_item:
                saveCurrentArtwork();
        }
        return true;
    }

    private void saveCurrentArtwork() {
        Bitmap image = captureView(R.id.artwork_frame);
        String imagePath = saveToGallery(image, generateFileName());
        if (imagePath != null) {
            String toastText = "Image successfully saved! Opening it...";
            Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT)
                    .show();

            // Tell the media scanner to register the new image and open it as soon as the image
            // has been scanned
            String[] pathsToScan = new String[]{imagePath};
            String[] mimeTypes = new String[]{"image/png"};
            MediaScannerConnection.scanFile(this, pathsToScan, mimeTypes,
                (path, uri) -> {
                    Log.i("ExternalStorage", "Scanned " + path + ":");
                    Log.i("ExternalStorage", "-> uri = " + uri);
                    Intent showImageIntent = new Intent(Intent.ACTION_VIEW);
                    showImageIntent.setDataAndType(uri, "image/png");
                    startActivity(showImageIntent);
                });
        }
    }

    public Bitmap captureView(int viewId) {
        //Find the view we are after
        View view = findViewById(viewId);
        //Create a Bitmap with the same dimensions
        Bitmap image = Bitmap.createBitmap(view.getWidth(),
                view.getHeight(),
                Bitmap.Config.RGB_565);
        //Draw the view inside the Bitmap
        view.draw(new Canvas(image));
        return image;
    }

    public String saveToGallery(Bitmap image, String filename) {
        final String APP_NAME = getString(R.string.app_name);
        String appImagesFolderPath =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    + File.separator + APP_NAME + File.separator;

        // Create the folder if it doesn't exist
        File appImageFolder = new File(appImagesFolderPath);
        if (!appImageFolder.exists()) {
            appImageFolder.mkdir();
        }

        // Store the image into the folder
        File imageFile = new File(appImagesFolderPath, filename);
        boolean success = false;
        try (FileOutputStream out = new FileOutputStream(imageFile)) {
            success = image.compress(Bitmap.CompressFormat.PNG, 100, out);
        }
        catch (FileNotFoundException e) {
            showErrorToast(R.string.impossible_to_save_error);
        }
        catch (IOException e) {
            showErrorToast(R.string.impossible_to_save_error);
        }

        return (success) ? imageFile.getAbsolutePath() : null;
    }

    void showErrorToast(int stringId) {
        String error = getString(stringId);
        Toast.makeText(this, error, Toast.LENGTH_LONG);
    }


    String generateFileName() {
        return "ModernArtwork_" + Long.toString(System.currentTimeMillis());
    }

}
