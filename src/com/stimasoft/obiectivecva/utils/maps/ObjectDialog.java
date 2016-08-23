package com.stimasoft.obiectivecva.utils.maps;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.stimasoft.obiectivecva.R;

/**
 * //TODO @Filip Documentation for the entire .utils.maps package
 * Created by filip on 24/06/2015.
 */
public class ObjectDialog extends DialogFragment implements View.OnClickListener{
    Communicator communicator;
    Button btnYes,btnNo;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        communicator = (Communicator) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setCancelable(false);

        getDialog().setTitle(R.string.new_objective_msg);
        View view = inflater.inflate(R.layout.dialog_confirm_create_object, null);

        btnYes = (Button) view.findViewById(R.id.btnYes);
        btnYes.setOnClickListener(this);

        btnNo = (Button) view.findViewById(R.id.btnNo);
        btnNo.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btnYes)
        {
            communicator.onDialogCreateObjectiveAnswer(true);
            dismiss();
        }else{
            communicator.onDialogCreateObjectiveAnswer(false);
            dismiss();
        }
    }

    public interface Communicator
    {
        void onDialogCreateObjectiveAnswer(Boolean answer);
    }

}
