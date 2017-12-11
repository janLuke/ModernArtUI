package com.example.android.modernartui;

import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.example.android.modernartui.colors.ColorSampler;
import com.example.android.modernartui.colors.GoldenRatioColorSampler;


public class ShowArtworkActivity extends AppCompatActivity {

    static final String TAG = ShowArtworkActivity.class.getSimpleName();
    static final float DEFAULT_SATURATION = 0.5f;
    static final float DEFAULT_BRIGHTNESS = 1f;
    static final int DEFAULT_DEPTH_LIMIT = 2;
    private static final float HUE_OFFSET_ON_CLICK = .13f;
    private static final int MIN_DEPTH_LIMIT = 1;
    private final int DEFAULT_GRID_SIZE_IN_DP = 30;

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
        num_saturation_levels = res.getInteger(R.integer.num_saturation_levels);
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
        depthLimitBar.setProgress(DEFAULT_DEPTH_LIMIT);

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
        }
        return true;
    }
}
