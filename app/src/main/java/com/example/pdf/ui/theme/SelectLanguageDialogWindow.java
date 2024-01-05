package com.example.pdf.ui.theme;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.example.pdf.R;

public class SelectLanguageDialogWindow extends AlertDialog {
    public TextView russianLanguage;
    public TextView englishLanguage;
    public SelectLanguageDialogWindow(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        RelativeLayout container = (RelativeLayout) inflater.inflate(R.layout.select_language, null);
        CardView cardView = (CardView) container.getChildAt(0);
        LinearLayout content = (LinearLayout) cardView.getChildAt(0);
        russianLanguage = (TextView) content.getChildAt(1);
        englishLanguage = (TextView) content.getChildAt(2);
        setView(container);

        // Отключаем закрытие диалога при нажатии вне его
        setCanceledOnTouchOutside(false);

        // Устанавливаем параметры для размещения в центре экрана
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.gravity = android.view.Gravity.CENTER;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT; // Устанавливаем ширину на WRAP_CONTENT
        getWindow().setAttributes(layoutParams);
    }
}
