package com.example.pdf;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import com.github.barteksc.pdfviewer.PDFView;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;

import java.io.File;

public class PdfViewer extends AppCompatActivity {
    PDFView pdfView;

    LinearLayout topPanel;
    LinearLayout bottomPanel;
    private TextView fileName;
    private SeekBar brightnessSeekBar;
    private SeekBar pageSeekBar;
    private TextView pages;
    private int pageCount;
    private ImageView backToMenu;
    private ImageView swipeDirection;
    private boolean isLtoRSwipe = false;
    private boolean trakingSB= false;

    private File currentFile;
    int currentPage = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);
        pdfView = findViewById(R.id.pdfView);
        backToMenu = findViewById(R.id.back);
        fileName = findViewById(R.id.fileName);
        pages = findViewById(R.id.pages);
        brightnessSeekBar = findViewById(R.id.seekBar);
        pageSeekBar = findViewById(R.id.pageSeekBar);
        swipeDirection = findViewById(R.id.swipe_direction);

        backToMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Intent intent = getIntent();
        if (intent != null) {
            String receivedData = intent.getStringExtra("fileName");
            fileName.setText(receivedData);
            // Check if the intent contains data
            if (intent.getData() != null) {
                // Get the file URI
                Uri fileUri = intent.getData();

                // Now, you can use the fileUri to work with the file, for example:
                String filePath = fileUri.getPath();
                currentFile = new File(filePath);
                pdfView.fromFile(currentFile)
                        .onLoad(new OnLoadCompleteListener() {
                            @Override
                            public void loadComplete(int nbPages) {

                                pageCount = pdfView.getPageCount();
                                pageSeekBar.setMax(pageCount);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    pageSeekBar.setMin(1);
                                }
                            }
                        }).onPageChange(new OnPageChangeListener() {
                            @Override
                            public void onPageChanged(int page, int pageCount) {
                                pageSeekBar.setProgress(page);
                            }
                        })
                        .load();

                // Do something with the file...
            }

        }

        swipeDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLtoRSwipe = !isLtoRSwipe;
                currentPage = pdfView.getCurrentPage();

                PDFView.Configurator configurator = pdfView.fromFile(currentFile)
                        .onLoad(new OnLoadCompleteListener() {
                            @Override
                            public void loadComplete(int nbPages) {

                                pageCount = pdfView.getPageCount();
                                pageSeekBar.setMax(pageCount);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    pageSeekBar.setMin(1);
                                }
                            }
                        }).onPageChange(new OnPageChangeListener() {
                            @Override
                            public void onPageChanged(int page, int pageCount) {
                                pageSeekBar.setProgress(page);
                            }
                        })
                        .defaultPage(currentPage)
                        .enableSwipe(true);
                if (isLtoRSwipe) {
                    swipeDirection.setImageResource(R.drawable.tb_swipe);
                    configurator.swipeHorizontal(true);
                } else {
                    swipeDirection.setImageResource(R.drawable.lr_swipe);
                    configurator.swipeHorizontal(false);
                }

                configurator.load();
            }
        });
        pageSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(trakingSB){
                    pdfView.jumpTo(progress);
                }
                pages.setText(String.valueOf(progress) + " из " + String.valueOf(pageCount));
            }


            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                trakingSB = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                trakingSB = false;
            }
        });
        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Изменяем яркость приложения при изменении положения ползунка
                setAppBrightness(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Ничего не делаем при начале трекинга
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Ничего не делаем при окончании трекинга
            }
        });





        topPanel = findViewById(R.id.topBar);
        topPanel.setBackgroundColor(Color.parseColor("#0059B3"));
        bottomPanel = findViewById(R.id.bottomBar);
        bottomPanel.setBackgroundColor(Color.parseColor("#0059B3"));
        pdfView = findViewById(R.id.pdfView);
        pdfView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(topPanel.isEnabled()){

                    topPanel.setEnabled(false);
                    topPanel.setClickable(false);
                    topPanel.setFocusable(false);
                    topPanel.setVisibility(View.INVISIBLE);
                    bottomPanel.setEnabled(false);
                    bottomPanel.setClickable(false);
                    bottomPanel.setFocusable(false);
                    bottomPanel.setVisibility(View.INVISIBLE);
                }else{
                    topPanel.setEnabled(true);
                    topPanel.setClickable(true);
                    topPanel.setFocusable(true);
                    topPanel.setVisibility(View.VISIBLE);
                    bottomPanel.setEnabled(true);
                    bottomPanel.setClickable(true);
                    bottomPanel.setFocusable(true);
                    bottomPanel.setVisibility(View.VISIBLE);
                }

            }
        });



    }

    private void setPage(int progress){
        pdfView.jumpTo(progress);
        pages.setText(String.valueOf(progress) + " из " + String.valueOf(pageCount));
    }
    private void setAppBrightness(int brightness) {
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.screenBrightness = brightness / 255.0f;
        getWindow().setAttributes(layoutParams);
    }

}