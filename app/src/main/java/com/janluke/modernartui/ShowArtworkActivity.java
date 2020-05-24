package com.janluke.modernartui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.android.modernartui.R;
import com.janluke.modernartui.colors.ConstrainedColorSampler;
import com.janluke.modernartui.colors.HueOffsetColorSampler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class ShowArtworkActivity extends AppCompatActivity {

    private final static int MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT;

    static final String TAG = ShowArtworkActivity.class.getSimpleName();

    static final float INITIAL_SATURATION = 0.5f;
    static final float MIN_BRIGHTNESS = 0.8f;
    static final float MAX_BRIGHTNESS = 1f;
    static final float HUE_OFFSET_ON_TAP = .13f;

    static final int DEFAULT_DEPTH_LIMIT = 2;
    static final int MIN_DEPTH_LIMIT = 2;

    static final int DEFAULT_GRID_SIZE_IN_DP = 30;

    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 0;
    private static final Bitmap.CompressFormat IMAGE_FILE_FORMAT = Bitmap.CompressFormat.PNG;

    ArtworkGenerator artworkGenerator;
    Artwork artwork;
    ConstrainedColorSampler colorSampler;

    FrameLayout artworkFrame;
    SeekBar saturationBar;
    SeekBar depthLimitBar;
    SeekBar gridSizeBar;
    Button newArtworkButton;
    Button recolorButton;
    ImageView gridSizeImageView;

    float saturation;

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

        // Initialize seekbars and mount event handlers
        initSaturationSeekBar();
        initDepthLimitSeekBar();
        initGridSizeSeekBar();
        recolorButton.setOnClickListener(view -> recolorArtwork());
        newArtworkButton.setOnClickListener(view -> generateAndShowNewArtwork());

        // Generate the artwork
        colorSampler = HueOffsetColorSampler.withGoldenRatioOffset()
            .setSaturation(INITIAL_SATURATION)
            .setBrightnessRange(MIN_BRIGHTNESS, MAX_BRIGHTNESS);
        artworkGenerator = new ArtworkGenerator();
        artworkGenerator.setColorSampler(colorSampler);
        artworkGenerator.setStrokeWidthInDp(DEFAULT_GRID_SIZE_IN_DP);

        artworkFrame.post(this::generateAndShowNewArtwork);
    }

    void generateAndShowNewArtwork() {
        Log.i(TAG, "Create new");
        artworkFrame.removeAllViews();
        artwork = artworkGenerator.generateArtwork(
                artworkFrame.getContext(), artworkFrame.getWidth(), artworkFrame.getHeight());
        artworkFrame.addView(artwork.getView(), MATCH_PARENT, MATCH_PARENT);
        onDepthLimitChange(depthLimitBar.getProgress());
        artwork.setOnNodesClickListener(node -> {
            float newHue = node.getHue() / 360f + HUE_OFFSET_ON_TAP;
            newHue = 360f * (newHue - (int) newHue);
            node.setHue(newHue);
        });
    }

    void initSaturationSeekBar() {
        saturation = INITIAL_SATURATION;

        // The minimum saturation level is set to 1 because, for saturation 0, the color sampler
        // would make all tiles white; when changing saturation,
        saturationBar.setMax(255);
        saturationBar.setProgress((int) (INITIAL_SATURATION * saturationBar.getMax()));

        // Saturation bar listener
        saturationBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
             @Override
             public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                 onSaturationChange(i);
             }

             @Override
             public void onStartTrackingTouch(SeekBar seekBar) {}

             @Override
             public void onStopTrackingTouch(SeekBar seekBar) {}
         }
        );
    }

    void onSaturationChange(int saturationLevel) {
        saturation = (float) saturationLevel / saturationBar.getMax();
        artwork.setSaturation(saturation);
        colorSampler.setSaturation(saturation);
        Log.i(TAG, "Setting saturation to " + saturation + " (level " + saturationLevel + ")");
    }

    void initDepthLimitSeekBar() {
        depthLimitBar.setMax(ArtworkGenerator.DEFAULT_MAX_DEPTH - MIN_DEPTH_LIMIT);
        depthLimitBar.setProgress(DEFAULT_DEPTH_LIMIT - MIN_DEPTH_LIMIT);

        // Saturation bar listener
        depthLimitBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                onDepthLimitChange(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    void onDepthLimitChange(int depth) {
        artwork.setDepthLimit(depth + MIN_DEPTH_LIMIT);
        Log.i(TAG, "Setting max depth to " + depth);
    }

    void initGridSizeSeekBar() {
        // Saturation bar listener
        gridSizeBar.setProgress(DEFAULT_GRID_SIZE_IN_DP);
        gridSizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int size, boolean b) {
                onGridSizeChange(size);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

    }

    void onGridSizeChange(int marginInDp) {
        Log.i(TAG, "Setting grid size to " + marginInDp + " dp");
        gridSizeImageView.setImageResource((marginInDp == 0)
            ? R.drawable.ic_grid_off_black_36dp
            : R.drawable.ic_grid_on_black_36dp);
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
        Log.i(TAG, "Selected menu item: " + item.toString());
        switch (item.getItemId()) {
            case R.id.more_info_menu_item:
                InfoDialogFragment infoDialogFragment = InfoDialogFragment.newInstance();
                infoDialogFragment.show(getFragmentManager(), "More info");
                break;

            case R.id.save_menu_item:
                onSaveActionSelected();
        }
        return true;
    }

    private void onSaveActionSelected() {
        Log.i(TAG, "Checking permission for WRITE_EXTERNAL_STORAGE");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is missing and must be requested.
            requestWriteExternalStoragePermission();
        } else {
            // Permission granted
            Bitmap image = captureView(R.id.artwork_frame);
            String imagePath = saveImageToGallery(image, generateFileName());
            if (imagePath != null) {
                showToast(R.string.image_saved_message, Toast.LENGTH_LONG);
                scanAndOpenImageFile(imagePath);
            }
        }
    }

    private void scanAndOpenImageFile(String imagePath) {
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
                if (showImageIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(showImageIntent);
                } else {
                    showToast(R.string.no_app_for_opening_image, Toast.LENGTH_LONG);
                }
            }
        );
    }

    private void requestWriteExternalStoragePermission() {
        String writeExternalPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, writeExternalPermission)) {
            // Provide an additional rationale to the user
            showToast(R.string.external_storage_permission_rationale, Toast.LENGTH_LONG);
        }

        ActivityCompat.requestPermissions(this, new String[]{writeExternalPermission},
                PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onSaveActionSelected();
            } else {
                // Permission request was denied.
                showToast(R.string.external_storage_permission_denied, Toast.LENGTH_LONG);
            }
        }
    }

    public String saveImageToGallery(Bitmap image, String filename) {
        final String APP_NAME = getString(R.string.app_name);
        String appImagesFolderPath =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        + File.separator + APP_NAME + File.separator;

        // Create the folder if it doesn't exist
        File appImageFolder = new File(appImagesFolderPath);
        if (!appImageFolder.exists()) {
            Log.i(TAG, "Image folder (" + appImagesFolderPath + ") doesn't exist. Creating it...");
            boolean created = false;
            try {
                created = appImageFolder.mkdirs();
            } catch (SecurityException e) {
            } finally {
                if (!created)
                    showErrorDialog(R.string.unable_to_create_gallery_folder_error);
            }
            return null;
        }

        // Store the image into the folder
        File imageFile = new File(appImagesFolderPath, filename);
        boolean success = false;
        try (FileOutputStream out = new FileOutputStream(imageFile)) {
            success = image.compress(IMAGE_FILE_FORMAT, 100, out);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!success) {
            showErrorDialog(R.string.impossible_to_save_error);
            return null;
        }
        return imageFile.getAbsolutePath();
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

    String generateFileName() {
        long timeMillis = System.currentTimeMillis();
        String fileFormat = IMAGE_FILE_FORMAT.toString().toLowerCase();
        return String.format("artwork_%d.%s", timeMillis, fileFormat);
    }

    public void showErrorDialog(@StringRes int messageId) {
        String title = getString(R.string.error_dialog_title);
        showDialog(title, getString(messageId));
    }

    public void showDialog(@StringRes int titleId, @StringRes int messageId) {
        String title = getString(titleId);
        String message = getString(messageId);
        showDialog(title, message);
    }

    public void showDialog(final String title, final String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setTitle(title)
                .setNeutralButton(R.string.close_button_text, null)
                .create()
                .show();
    }

    public void showToast(@StringRes int textId, int duration) {
        Toast.makeText(this, getString(textId), duration)
                .show();
    }

}
