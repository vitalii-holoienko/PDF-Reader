package com.example.pdf;

import static com.example.pdf.MainActivity.displayPdfCover;
import static com.example.pdf.MainActivity.formatFileSize;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Favourites extends AppCompatActivity {
    private static final int REQUEST_PERMISSION = 1;
    private LinearLayout mainLayout;
    private LinearLayout mainLayout1;
    private LinearLayout footerLayout;
    private ImageView hamburgerMenu;

    private HashMap<View, File> filesToSave = new HashMap<>();
    private HashMap<View, File> containersToSave = new HashMap<>();
    private LinearLayout backToMain;
    ArrayList<File> favouritesFiles = new ArrayList<>();
    ArrayList<View> containers = new ArrayList<>();
    private LinearLayout recentFilesLayout;
    private View slidePanel;
    ArrayList<File> pdfFiles = new ArrayList<>();
    ArrayList<File> recentFiles = new ArrayList<>();
    ArrayList<RelativeLayout> viewFiles = new ArrayList<>();
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle actionBarDrawerToggle;
    private boolean isPanelOpen = false;
    boolean darkMode;
    SearchView searchView;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor spEditor;
    androidx.activity.result.ActivityResultLauncher<Intent> ActivityResultLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);
        searchView = findViewById(R.id.searchView);
        mainLayout = findViewById(R.id.mainLayout);
        hamburgerMenu = findViewById(R.id.hamburgerMenuIcon);
        footerLayout = findViewById(R.id.footer);
        backToMain = findViewById(R.id.backToMain);
        recentFilesLayout = findViewById(R.id.recentFiles);

        drawerLayout =           findViewById(R.id.drawer_layout);
        navigationView =         findViewById(R.id.nav_view);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        Switch drawerSwitch = (Switch) navigationView.getMenu().findItem(R.id.setTheme).getActionView();
        drawerSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(darkMode){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    spEditor = sharedPreferences.edit();
                    spEditor.putBoolean("dark", false);


                }else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    spEditor = sharedPreferences.edit();
                    spEditor.putBoolean("dark", true);
                }
                spEditor.apply();
            }
        });
        sharedPreferences = getSharedPreferences("Mode", 0 );
        darkMode = sharedPreferences.getBoolean("dark", false);

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
                        if (ContextCompat.checkSelfPermission(Favourites.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            // Если разрешения нет, запрашиваем у пользователя
                            ActivityCompat.requestPermissions(Favourites.this,
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

        Intent intent = getIntent();
        if(intent != null){
            pdfFiles = (ArrayList<File>) intent.getSerializableExtra("fileList");
            recentFiles = (ArrayList<File>) intent.getSerializableExtra("recentFiles");
            displayFiles(pdfFiles, pdfFiles.size());
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
        backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MainActivity.class);
                intent.putExtra("favouriteFiles", favouritesFiles);
                startActivity(intent);
            }
        });
        recentFilesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Recent.class);
                intent.putExtra("recentFiles", recentFiles);
                intent.putExtra("favouriteFiles", favouritesFiles);
                startActivity(intent);
            }
        });

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


        if(darkMode) {
            drawerSwitch.setChecked(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            applyModeChangeToSpecificView(true);
        }
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
        for(int i = 0; i < containers.size(); i++){
            RelativeLayout v = (RelativeLayout) containers.get(i);
            CardView c = (CardView) v.getChildAt(0);
            LinearLayout l = (LinearLayout) c.getChildAt(0);
            LinearLayout l1 = (LinearLayout) l.getChildAt(0);
            if(mode){
                l1.setBackgroundColor((int) ContextCompat.getColor(this, R.color.dark_el));
            }
            else{
                int color = android.R.color.transparent;
                l1.setBackgroundColor((int) ContextCompat.getColor(this, color));
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
            allStuff.setBackgroundColor(Color.parseColor("#fafafa"));
            LinearLayout information = (LinearLayout) allStuff.getChildAt(1);
            ImageView cover = (ImageView)  allStuff.getChildAt(0);
            TextView fileName = (TextView) information.getChildAt(0);
            TextView fileDescription = (TextView) information.getChildAt(1);

            LinearLayout bottomBar = (LinearLayout) information.getChildAt(2);
            TextView fileLocation = (TextView) bottomBar.getChildAt(0);
            ImageView heart =  (ImageView) bottomBar.getChildAt(1);
            Drawable filled_heart = getResources().getDrawable(R.drawable.filled_heart);
            heart.setImageDrawable(filled_heart);

            filesToSave.put(heart, allFiles.get(i));
            favouritesFiles.add(allFiles.get(i));
            containersToSave.put(container, allFiles.get(i));

            heart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isHeartIconDrawable(heart)){
                        Drawable filled_heart = getResources().getDrawable(R.drawable.filled_heart);
                        heart.setImageDrawable(filled_heart);
                        favouritesFiles.add(filesToSave.get(v));

                    } else {
                        Drawable empty_heart = getResources().getDrawable(R.drawable.heart_icon);
                        heart.setImageDrawable(empty_heart);
                        if(favouritesFiles.contains(filesToSave.get(v))) favouritesFiles.remove(filesToSave.get(v));
                    }
                }
            });
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Проверяем, что файл не равен null
                    if (containersToSave.get(v) != null) {
                        Uri fileUri = Uri.fromFile(containersToSave.get(v));
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
                        Log.e("onClick", "Файл is null");
                    }
                }
            });
            String fileLength = "";
            String dateCreated = "";
            File parentFolder = allFiles.get(i).getParentFile();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                try{
                    BasicFileAttributes attrs = Files.readAttributes(allFiles.get(i).toPath(), BasicFileAttributes.class);
                    SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
                    dateCreated = df.format(attrs.creationTime().toMillis());
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

    private void displayLanguageSelection() {
//        SelectLanguageDialogWindow customDialog = new SelectLanguageDialogWindow(this);
//        customDialog.show();
//        TextView russianLanguage = customDialog.russianLanguage;
//        TextView englishLanguage = customDialog.englishLanguage;
        RelativeLayout container = (RelativeLayout) getLayoutInflater().inflate(R.layout.select_language, null);
        LinearLayout cardView = (LinearLayout) container.getChildAt(0);
        LinearLayout content = (LinearLayout) cardView.getChildAt(0);
        TextView russianLanguage = (TextView) content.getChildAt(1);
        TextView englishLanguage = (TextView) content.getChildAt(2);
        TextView ukrainianLanguage = (TextView) content.getChildAt(3);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(container);
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);


        alertDialog.show();
        alertDialog.getWindow().setLayout(950, 1100);


        LanguageManager languageManager = new LanguageManager(this);

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
        drawerLayout.closeDrawers();

    }
    private boolean isHeartIconDrawable(ImageView heart) {
        return heart.getDrawable() != null && ((BitmapDrawable) heart.getDrawable()).getBitmap().sameAs(((BitmapDrawable) getResources().getDrawable(R.drawable.heart_icon)).getBitmap());
    }
}