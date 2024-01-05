package com.example.pdf;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.documentfile.provider.DocumentFile;

public class FileUtils {

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String getPathFromUri(Context context, Uri uri) {
        String path = null;

        if (DocumentsContract.isDocumentUri(context, uri)) {
            // Для документов, предоставляемых провайдерами DocumentsProvider
            String docId = DocumentsContract.getDocumentId(uri);
            String[] split = docId.split(":");
            String type = split[0];

            if ("msf".equals(type)) {
                // Если URI предоставлен провайдером, использующим префикс "msf"
                return handleMsfProvider(context, uri);
            } else {
                // Для остальных провайдеров
                String selection = "_id=?";
                String[] selectionArgs = new String[]{split[1]};

                if ("image".equals(type)) {
                    path = getDataColumn(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
                } else if ("audio".equals(type)) {
                    path = getDataColumn(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
                } else if ("video".equals(type)) {
                    path = getDataColumn(context, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
                } else if ("download".equals(type)) {
                    path = getDataColumn(context, Uri.parse("content://downloads/public_downloads"), selection, selectionArgs);
                }
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Для контента, предоставляемого провайдерами контента
            path = getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // Для файловых URI
            path = uri.getPath();
        }

        return path;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static String handleMsfProvider(Context context, Uri uri) {
        String path = null;

        try {
            // Извлекаем числовую часть из строки "msf:1000000266"
            String documentId = uri.getLastPathSegment().substring(4);

            // Построение URI для конкретного документа
            Uri contentUri = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                contentUri = ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, Long.parseLong(documentId));
            }

            // Используем ContentResolver для получения пути
            path = getDataColumn(context, contentUri, null, null);
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        return path;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        String[] projection = {"_data"};

        try (Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow("_data");
                return cursor.getString(column_index);
            }
        } catch (Exception e) {
            Log.e("FileUtils", "Error getting data column", e);
        }

        return null;
    }
}