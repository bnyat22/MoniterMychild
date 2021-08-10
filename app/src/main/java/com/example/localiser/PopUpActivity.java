package com.example.localiser;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.localiser.domains.MyEditTextDatePicker;
import com.example.localiser.domains.MyEditTextHourPicker;

public class PopUpActivity extends Dialog {

    private EditText duSamedi,aSamedi,duDimanche,aDimanche,duLundi , aLundi , duMardi , aMardi , duMercredi , aMercredi , duJeudi , aJeudi
            ,duVendredi , aVendredi , nom;


    private Button confimer , fermer;



    public PopUpActivity(@NonNull Context context) {
        super(context);
        setContentView(R.layout.ecole_popup);
        duSamedi = findViewById(R.id.duSamedi);
        aSamedi = findViewById(R.id.ASamedi);
        duDimanche = findViewById(R.id.duDimanche);
        aDimanche = findViewById(R.id.Adimanche);
        duLundi = findViewById(R.id.duLundi);
        aLundi = findViewById(R.id.ALundi);
        duMardi = findViewById(R.id.DuMardi);
        aMardi = findViewById(R.id.Amardi);
        duMercredi = findViewById(R.id.DuMercredi);
        aMercredi = findViewById(R.id.Amercredi);
        duJeudi = findViewById(R.id.duJeudi);
        aJeudi = findViewById(R.id.Ajeudi);
        duVendredi = findViewById(R.id.duVendredi);
        aVendredi = findViewById(R.id.Avendredi);
        confimer = findViewById(R.id.ecoleConfirm);
        fermer = findViewById(R.id.fermerPopup);
        nom = findViewById(R.id.endroitSpec);

        MyEditTextHourPicker myEditTextDatePickersd = new MyEditTextHourPicker(this,context , R.id.duSamedi);
        MyEditTextHourPicker myEditTextDatePickersa = new MyEditTextHourPicker(this,context , R.id.ASamedi);
        MyEditTextHourPicker myEditTextDatePickerda = new MyEditTextHourPicker(this,context , R.id.duDimanche);
        MyEditTextHourPicker myEditTextDatePickerdm = new MyEditTextHourPicker(this,context , R.id.Adimanche);
     MyEditTextHourPicker myEditTextDatePickerla = new MyEditTextHourPicker(this,context , R.id.duLundi);
        MyEditTextHourPicker myEditTextDatePickerld = new MyEditTextHourPicker(this,context , R.id.ALundi);
        MyEditTextHourPicker myEditTextHourPickerdm = new MyEditTextHourPicker(this,context , R.id.DuMardi);
        MyEditTextHourPicker myEditTextHourPickerda = new MyEditTextHourPicker(this ,context, R.id.Amardi);
        MyEditTextHourPicker myEditTextHourPickerma = new MyEditTextHourPicker(this,context , R.id.DuMercredi);
        MyEditTextHourPicker myEditTextHourPickermu = new MyEditTextHourPicker(this,context , R.id.Amercredi);
        MyEditTextHourPicker myEditTextHourPickerja = new MyEditTextHourPicker(this,context , R.id.duJeudi);
        MyEditTextHourPicker myEditTextHourPickerju = new MyEditTextHourPicker(this,context , R.id.Ajeudi);
        MyEditTextHourPicker myEditTextHourPickerva = new MyEditTextHourPicker(this,context , R.id.duVendredi);
        MyEditTextHourPicker myEditTextHourPickervu = new MyEditTextHourPicker(this,context , R.id.Avendredi);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




        confimer.setOnClickListener((t)-> {
            System.out.println("ada" + getaLundi().getText());
            this.dismiss();
        });


    }

    public EditText getDuSamedi() {
        return duSamedi;
    }

    public EditText getaSamedi() {
        return aSamedi;
    }

    public EditText getDuDimanche() {
        return duDimanche;
    }

    public EditText getaDimanche() {
        return aDimanche;
    }

    public Button getConfimer() {
        return confimer;
    }

    public Button getFermer() {
        return fermer;
    }

    public EditText getDuLundi() {
        return duLundi;
    }

    public EditText getaLundi() {
        return aLundi;
    }

    public EditText getDuMardi() {
        return duMardi;
    }

    public EditText getaMardi() {
        return aMardi;
    }

    public EditText getDuMercredi() {
        return duMercredi;
    }

    public EditText getaMercredi() {
        return aMercredi;
    }

    public EditText getDuJeudi() {
        return duJeudi;
    }

    public EditText getaJeudi() {
        return aJeudi;
    }

    public EditText getDuVendredi() {
        return duVendredi;
    }

    public EditText getaVendredi() {
        return aVendredi;
    }

    public EditText getNom() {
        return nom;
    }

    public void build()
    {
        show();
    }
}
