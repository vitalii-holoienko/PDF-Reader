package com.example.pdf;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import static com.example.pdf.R.id.browsePdf;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.io.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfRenderer;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pdf.ui.theme.SelectLanguageDialogWindow;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.widget.SearchView;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_PERMISSION = 1;
    private static final int PICK_PDF_REQUEST = 3;
    private LinearLayout mainLayout;
    private LinearLayout footerLayout;
    private ImageView hamburgerMenu;
    private LinearLayout favouritesFilesLayout;
    private LinearLayout recentFilesLayout;
    private LinearLayout footer;
    private  LinearLayout selectLanguageLayout;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle actionBarDrawerToggle;
    AlertDialog alertDialog;



    private ConstraintLayout constraintLayout;
    private HashMap<View, File> filesToSave = new HashMap<>();
    private HashMap<View, File> containersToSave = new HashMap<>();
    private int favouriteFilesCounter = 0;
    private boolean darkMode = false;
    ArrayList<File> favouritesFiles = new ArrayList<>();
    ArrayList<File> pdfFiles = new ArrayList<>();
    ArrayList<File> recentFiles = new ArrayList<>();
    ArrayList<RelativeLayout> viewFiles = new ArrayList<>();
    ActivityResultLauncher<Intent> ActivityResultLauncher;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor spEditor;
    ArrayList<View> containers = new ArrayList<>();
    private static final int FILE_PICKER_REQUEST = 2;
    private boolean isDrawerOpen = false;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        footer =                 findViewById(R.id.footer);
        searchView =             findViewById(R.id.searchView);
        constraintLayout =       findViewById(R.id.appLayout);
        mainLayout =             findViewById(R.id.mainLayout);
        hamburgerMenu =          findViewById(R.id.hamburgerMenuIcon);
        footerLayout =           findViewById(R.id.footer);
        favouritesFilesLayout =  findViewById(R.id.favoritesFiles);
        recentFilesLayout =      findViewById(R.id.recentFiles);
        drawerLayout =           findViewById(R.id.drawer_layout);
        navigationView =         findViewById(R.id.nav_view);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        ActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            Uri uri = data.getData();
                            if (uri != null) {
                                String filePath = FileUtils.getPathFromUri(getBaseContext(), uri);
                                if (filePath != null) {
                                    File selectedFile = new File(filePath);
                                    Intent intent = new Intent(getBaseContext(), PdfViewer.class);
                                    Uri fileUri = Uri.fromFile(selectedFile);
                                    intent.setDataAndType(fileUri, "application/pdf");
                                    intent.putExtra("fileName", selectedFile.getName());
                                    intent.putExtra("from", "browse");
                                    startActivity(intent);
                                }
                            }
                        }
                    }
                });

        Switch drawerSwitch = (Switch) navigationView.getMenu().findItem(R.id.setTheme).getActionView();
        sharedPreferences = getSharedPreferences("Mode", 0 );
        darkMode = sharedPreferences.getBoolean("dark", false);



        drawerSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(darkMode){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    spEditor = sharedPreferences.edit();
                    spEditor.putBoolean("dark", false);
                    spEditor.apply();
                    recreate();


                }else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    spEditor = sharedPreferences.edit();
                    spEditor.putBoolean("dark", true);
                    spEditor.apply();
                    recreate();
                }
            }
        });


        RelativeLayout container = (RelativeLayout) getLayoutInflater().inflate(R.layout.select_language, null);
        LinearLayout cardView = (LinearLayout) container.getChildAt(0);
        LinearLayout content = (LinearLayout) cardView.getChildAt(0);
        TextView russianLanguage = (TextView) content.getChildAt(1);
        TextView englishLanguage = (TextView) content.getChildAt(2);
        TextView ukrainianLanguage = (TextView) content.getChildAt(3);
        Button cancelButton = (Button) content.getChildAt(4);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(container);
        alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_border);




        LanguageManager languageManager = new LanguageManager(this);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });

        ukrainianLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                languageManager.UpdateResources("uk");
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });


        russianLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                languageManager.UpdateResources("ru");
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });

        englishLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                languageManager.UpdateResources("en");
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });

        hamburgerMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    drawerLayout.openDrawer(Gravity.START);
            }
        });
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.browsePdf){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            // Если разрешения нет, запрашиваем у пользователя
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                                    REQUEST_PERMISSION);
                        } else {
                            // Разрешения уже есть
                            pickPDF();
                        }
                    } else {
                        // Версия Android ниже 6.0, разрешения не требуются
                        pickPDF();
                    }

                }
                if(item.getItemId() == R.id.setLanguage){
                    displayLanguageSelection();

                }
                return false;
            }
        });
        Intent i = getIntent();
        if(i != null){

            if(i.getSerializableExtra("favouriteFiles") != null){
                favouritesFiles = (ArrayList<File>) i.getSerializableExtra("favouriteFiles");
            }
        }
        if (SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                scanPdfFiles();
            } else { //request for the permission
                Log.i("MANAGE_EXTERNAL_STORAGE", "Permission not granted");
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        } else {
            //below android 11=======
            scanPdfFiles();
            ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        }
        if(darkMode) {
            drawerSwitch.setChecked(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            applyModeChangeToSpecificView(true);
        }else{

        }



        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterFiles(newText);
                return true;
            }
        });

        favouritesFilesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Favourites.class);
                intent.putExtra("fileList", favouritesFiles);
                intent.putExtra("recentFiles", recentFiles);
                startActivity(intent);

            }
        });
        recentFilesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Recent.class);
                Log.i("recentFiles", String.valueOf(recentFiles.size()));
                intent.putExtra("recentFiles", recentFiles);
                intent.putExtra("favouriteFiles", favouritesFiles);
                startActivity(intent);
            }
        });
    }


    private void displayLanguageSelection() {
        alertDialog.show();
        drawerLayout.closeDrawers();
    }

    private void pickPDF() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        ActivityResultLauncher.launch(intent);
    }
    private void filterFiles(String newText) {
        mainLayout.removeAllViews();
        if(newText.length() == 0){
            for(int i = 0; i < viewFiles.size();i++){
                mainLayout.addView(viewFiles.get(i));
            }
            return;
        }

        for(int i = 0; i < viewFiles.size();i++){
            RelativeLayout container = viewFiles.get(i);
            CardView temp1 =  (CardView) container.getChildAt(0);
            LinearLayout temp2 = (LinearLayout) temp1.getChildAt(0);
            LinearLayout allStuff = (LinearLayout) temp2.getChildAt(0);
            LinearLayout information = (LinearLayout) allStuff.getChildAt(1);
            TextView fileName = (TextView) information.getChildAt(0);
            if(fileName.getText().toString().toLowerCase().startsWith(newText.toLowerCase())){
                mainLayout.addView(container);
            } else if (fileName.getText().toString().toLowerCase().contains(newText.toLowerCase())) {
                mainLayout.addView(container);
            }
        }
    }
    private void applyModeChangeToSpecificView(boolean mode){
        for(int i = 0; i < containers.size();i++){
            RelativeLayout v = (RelativeLayout) containers.get(i);
            CardView c = (CardView) v.getChildAt(0);
            LinearLayout l = (LinearLayout) c.getChildAt(0);
            if(mode){
                l.setBackgroundColor((int) ContextCompat.getColor(this, R.color.dark_el));
            }else{
                int color = android.R.color.transparent;
                l.setBackgroundColor((int) ContextCompat.getColor(this, color));
            }
        }
    }
    private void scanPdfFiles() {
        Log.i("PDF scanPdfFiles", "scanPdfFiles");
        // Выполняем сканирование файловой системы устройства
        pdfFiles = new ArrayList<>();
        getAllPdfFiles(Environment.getExternalStorageDirectory(), pdfFiles);
        Log.i(String.valueOf(pdfFiles.size()), "!");

        for (File file : pdfFiles) {
            Log.i("PDF Files", "File: " + file.getAbsolutePath());
        }
        displayFiles(pdfFiles, pdfFiles.size());
        getFromSharedPreferences();

    }

    private void getAllPdfFiles(File directory, ArrayList<File> pdfFiles) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    getAllPdfFiles(file, pdfFiles);
                } else if (file.getName().endsWith(".pdf")) {
                    pdfFiles.add(file);
                }
            }
        }
    }

    private  void displayFiles( ArrayList<File> allFiles, int amount){
        for(int i = 0; i < amount;i++){

            RelativeLayout container = (RelativeLayout) getLayoutInflater().inflate(R.layout.viewfiles_layout, null);
            containers.add(container);
            CardView temp1 =  (CardView) container.getChildAt(0);
            LinearLayout temp2 = (LinearLayout) temp1.getChildAt(0);
            LinearLayout allStuff = (LinearLayout) temp2.getChildAt(0);
            LinearLayout information = (LinearLayout) allStuff.getChildAt(1);
            ImageView cover = (ImageView)  allStuff.getChildAt(0);
            TextView fileName = (TextView) information.getChildAt(0);
            TextView fileDescription = (TextView) information.getChildAt(1);

            LinearLayout bottomBar = (LinearLayout) information.getChildAt(2);
            TextView fileLocation = (TextView) bottomBar.getChildAt(0);
            ImageView heart =  (ImageView) bottomBar.getChildAt(1);

            if(favouritesFiles.contains(allFiles.get(i))){
                Drawable filled_heart = getResources().getDrawable(R.drawable.filled_heart);
                heart.setImageDrawable(filled_heart);
            }else{
                Drawable empty_heart = getResources().getDrawable(R.drawable.heart_icon);
                heart.setImageDrawable(empty_heart);
            }
            filesToSave.put(heart, allFiles.get(i));
            containersToSave.put(container, allFiles.get(i));
            heart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isHeartIconDrawable(heart)){
                        Drawable filled_heart = getResources().getDrawable(R.drawable.filled_heart);
                        heart.setImageDrawable(filled_heart);
                        favouritesFiles.add(filesToSave.get(v));
                        SharedPreferences prefs = getSharedPreferences("favourites", 0);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("fileName" + filesToSave.get(v).getName(), filesToSave.get(v).getName());
                        editor.apply();

                    } else {
                        Drawable empty_heart = getResources().getDrawable(R.drawable.heart_icon);
                        heart.setImageDrawable(empty_heart);
                        if(favouritesFiles.contains(filesToSave.get(v))) favouritesFiles.remove(filesToSave.get(v));
                        SharedPreferences prefs = getSharedPreferences("favourites", 0);
                        if(prefs.contains("fileName" + filesToSave.get(v).getName())){
                            Log.i("aaa", "bbb");
                            prefs.edit().remove("fileName" + filesToSave.get(v).getName()).commit();
                            if(prefs.contains("fileName" + filesToSave.get(v).getName())) Log.i("ccc", "ccc");
                        }

                    }
                }
            });
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Проверяем, что файл не равен null
                    if (containersToSave.get(v) != null) {
                        Uri fileUri = Uri.fromFile(containersToSave.get(v));
                        if(containersToSave.get(v) == null) Log.i("NULL", "1");

                        // Проверяем, что URI файла не равен null перед созданием Intent
                        if (fileUri != null) {
                            Intent intent = new Intent(v.getContext(), PdfViewer.class);
                            intent.setDataAndType(fileUri, "application/pdf");

                            intent.putExtra("fileName", containersToSave.get(v).getName());
                            intent.putExtra("from", "main");
                            startActivity(intent);
                        } else {
                            Log.e("onClick", "URI is null");
                        }
                    } else {
                        Log.e("onClick", "File is null");
                    }
                }
            });

            String fileLength = "";
            String dateCreated = "";
            File parentFolder = allFiles.get(i).getParentFile();
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -30);
            Date thirtyDaysAgo = calendar.getTime();

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                try{
                    BasicFileAttributes attrs = Files.readAttributes(allFiles.get(i).toPath(), BasicFileAttributes.class);
                    SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
                    Date creationDate = new Date(attrs.creationTime().toMillis());
                    dateCreated = df.format(attrs.creationTime().toMillis());
                    if (creationDate.after(thirtyDaysAgo)) {
                        recentFiles.add(allFiles.get(i));
                    }
                }catch (Exception e){
                    Log.i(e.getMessage(), "!");
                }
                finally {
                    fileLength = formatFileSize(allFiles.get(i).length());
                }
            }

            if (parentFolder != null) fileLocation.setText(parentFolder.getName()); //displaying file location
            if(dateCreated.length() != 0) fileDescription.setText(dateCreated + " " + fileLength); //displaying date of file creating
            else fileDescription.setText(fileLength);
            fileName.setText(allFiles.get(i).getName()); // displaying name of file
            displayPdfCover(allFiles.get(i), cover);
            viewFiles.add(container);
            mainLayout.addView(container);

        }
    }
    private void getFromSharedPreferences(){
        SharedPreferences prefs = getSharedPreferences("favourites", 0);
        Map<String,?> keys = prefs.getAll();

        for(Map.Entry<String,?> entry : keys.entrySet()){
            if(entry.getKey().startsWith("fileName")){
                for(Map.Entry<View, File> vf : containersToSave.entrySet()){
                    if(vf.getValue().getName().equals(entry.getKey().substring(8))){
                        RelativeLayout v = (RelativeLayout) vf.getKey();
                        CardView temp1 =  (CardView) v.getChildAt(0);
                        LinearLayout temp2 = (LinearLayout) temp1.getChildAt(0);
                        LinearLayout allStuff = (LinearLayout) temp2.getChildAt(0);
                        LinearLayout information = (LinearLayout) allStuff.getChildAt(1);
                        LinearLayout bottomBar = (LinearLayout) information.getChildAt(2);
                        ImageView heart =  (ImageView) bottomBar.getChildAt(1);
                        Drawable filled_heart = getResources().getDrawable(R.drawable.filled_heart);
                        heart.setImageDrawable(filled_heart);

                        if(!favouritesFiles.contains(vf.getValue())){
                            favouritesFiles.add(vf.getValue());
                        }
                        Log.i("favouritesFiles", String.valueOf(favouritesFiles.size()));
                    }
                }
            }
        }
    }
    private boolean isHeartIconDrawable(ImageView heart) {
        return heart.getDrawable() != null && ((BitmapDrawable) heart.getDrawable()).getBitmap().sameAs(((BitmapDrawable) getResources().getDrawable(R.drawable.heart_icon)).getBitmap());
    }
    public  static void displayPdfCover(File file, ImageView imageView) {
        try {
            ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);
            PdfRenderer.Page firstPage = pdfRenderer.openPage(0);

            // Установка цвета фона (белый, например)
            Bitmap bitmap = Bitmap.createBitmap(firstPage.getWidth(), firstPage.getHeight(), Bitmap.Config.ARGB_8888);
            firstPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);



            // Получаем Bitmap для первой страницы
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE); // Устанавливаем цвет фона

            firstPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

            // Отображаем Bitmap в ImageView
            imageView.setImageBitmap(bitmap);

            // Закрываем PdfRenderer и ParcelFileDescriptor
            firstPage.close();
            pdfRenderer.close();
            fileDescriptor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String formatFileSize(long sizeInBytes) {
        final long kiloBytes = 1024;
        final long megaBytes = kiloBytes * 1024;
        final long gigaBytes = megaBytes * 1024;

        if (sizeInBytes >= gigaBytes) {
            return String.format("%.2f GB", (double) sizeInBytes / gigaBytes);
        } else if (sizeInBytes >= megaBytes) {
            return String.format("%.2f MB", (double) sizeInBytes / megaBytes);
        } else if (sizeInBytes >= kiloBytes) {
            return String.format("%.2f KB", (double) sizeInBytes / kiloBytes);
        } else {
            return sizeInBytes + " bytes";
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i("onRequestPermissionsResult", "onRequestPermissionsResult");
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешения получены, выполняем сканирование файлов
                scanPdfFiles();
            } else {
                // Разрешения не получены, выводим сообщение об ошибке
                // Или предпринимаем другие действия в зависимости от вашей логики
            }
        }

    }
}
