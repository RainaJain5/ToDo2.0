package com.raina.todo20;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.jetbrains.annotations.NotNull;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddNewTask extends BottomSheetDialogFragment {

    public static final String TAG = "AddNewTask";

    private EditText mEditText;
    private Button mSaveButton;
    private Context mContext;
    private String tokenVal = MainActivity.token;

    public AddNewTask(Context c) {
        mContext = c;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_task, container, false);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEditText = view.findViewById(R.id.TaskText);
        mSaveButton = view.findViewById(R.id.SaveButton);

        boolean isUpdate = false;

        Bundle bundle = getArguments();
        if(bundle != null)
        {
            isUpdate = true;
            String task = bundle.getString("task");
            mEditText.setText(task);

            if(task.length() > 0)
                mSaveButton.setEnabled(false);
        }

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSaveButton.setEnabled(!s.toString().equals(""));
                mSaveButton.setBackgroundColor(getResources().getColor(R.color.royalBlue));

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        boolean finalIsUpdate = isUpdate;
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = mEditText.getText().toString();

                if(text.isEmpty())
                    Toast.makeText(getContext(), "You need to enter something",
                            Toast.LENGTH_SHORT).show();
                else if (finalIsUpdate)
                    updateTask(bundle.getInt("id"),text);
                else
                    saveTask(createRequest());

                dismiss();
            }
        });
    }

    public TaskRequest createRequest()
    {
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setTitle(mEditText.getText().toString());
        return taskRequest;
    }

    public void saveTask(TaskRequest taskRequest)
    {

        Call<ResponseBody> taskResponseCall = ApiClient.getUserService().newTask(taskRequest, tokenVal);

        taskResponseCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful())
                {
                    if(mContext instanceof UserToDo)
                    {
                        ((UserToDo)mContext).getTask();
                        dismiss();
                    }
                    else
                        Toast.makeText(mContext, "Task not added",
                                Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(mContext, "Throwable "+t.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void updateTask(int id, String task)
    {
        TaskRequest  item = new TaskRequest();
        item.setTitle(task);

        Call<TaskResponse> taskResponseCall = ApiClient.getUserService().patchTask(tokenVal, item, id);
        taskResponseCall.enqueue(new Callback<TaskResponse>() {
            @Override
            public void onResponse(Call<TaskResponse> call, Response<TaskResponse> response) {
                if(response.isSuccessful())
                {
                    if(mContext instanceof UserToDo)
                    {
                        ((UserToDo)mContext).getTask();
                        dismiss();
                    }
                    else
                        Toast.makeText(mContext, "Task not edited",
                                Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<TaskResponse> call, Throwable t) {
                Toast.makeText(mContext, "Throwable "+t.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        Activity activity = getActivity();
        if(activity instanceof OnDialogCloseListener)
        ((OnDialogCloseListener)activity).OnDialogClose(dialog);

    }
}
