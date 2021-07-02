package com.raina.todo20;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AlertDialogLayout;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserToDo extends AppCompatActivity implements OnDialogCloseListener {

    private RecyclerView recyclerView;
    private ToDoAdapter toDoAdapter;
    private ImageView add;
    private String tokenValue;
    private String profileMessage;
    private char FirstLetter;
    private String first;
    private SearchView searchView;
    private List<TaskResponse> taskResponses;
    private DatabaseHandler db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_to_do);


        db = new DatabaseHandler(this);
        db.openDatabase();
        List<TokenClass> tokenModelList = db.getAllTokens();

        if (tokenModelList.size() != 0) {
            TokenClass item = tokenModelList.get(0);
            tokenValue = item.getToken();
        }
        else if (!MainActivity.keeplogged) {
            (UserToDo.this).finishAffinity();
            startActivity(new Intent(UserToDo.this, MainActivity.class));
            finish();
        }

        if (tokenModelList.size() == 0) {
            if (MainActivity.keeplogged) {
                tokenValue = MainActivity.token;
                MainActivity.keeplogged = false;
            }
        }

        recyclerView = findViewById(R.id.recyclerview);
        add = findViewById(R.id.add);


        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        toDoAdapter = new ToDoAdapter(this, UserToDo.this);
        getTask();

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNewTask task = new AddNewTask(UserToDo.this);
                task.show(getSupportFragmentManager() , AddNewTask.TAG);
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new RecyclerViewTouchHelper(toDoAdapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);

    }

    public void getUser()
    {
        Call<UserResponse> mList = ApiClient.getUserService().getAllUsers(tokenValue);

        mList.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if(response.isSuccessful())
                {
                    UserResponse user = response.body();

                    profileMessage = " Name : " + user.getName();
                    profileMessage+= "\n Email : " + user.getEmail();
                    profileMessage+= "\n Username : " + user.getUsername();

                    FirstLetter = user.getName().charAt(0);
                    first = Character.toString(FirstLetter);

                    AlertDialog.Builder builder1 = new AlertDialog.Builder(UserToDo.this);
                    builder1.setTitle("USER PROFILE");
                    builder1.setCancelable(true);
                    

                    View dialog = getLayoutInflater().inflate(R.layout.alert_layout, null);

                    builder1.setView(dialog);

                    TextView firstletter = (TextView) dialog.findViewById(R.id.firstname);
                    TextView userinfo = (TextView) dialog.findViewById(R.id.userinfo);
                    firstletter.setText(first);
                    userinfo.setText(profileMessage);

                    builder1.setPositiveButton(
                            "Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e("Failure", t.getLocalizedMessage());
            }
        });
    }

    public void getTask()
    {
        Call<List<TaskResponse>> mList = ApiClient.getUserService().getAllTasks(tokenValue);

        mList.enqueue(new Callback<List<TaskResponse>>() {
            @Override
            public void onResponse(Call<List<TaskResponse>> call, Response<List<TaskResponse>> response) {
                if(response.isSuccessful())
                {
                    taskResponses = response.body();
                    Collections.reverse(taskResponses);
                    toDoAdapter.setData(taskResponses);
                    recyclerView.setAdapter(toDoAdapter);
                }
            }

            @Override
            public void onFailure(Call<List<TaskResponse>> call, Throwable t) {
                Log.e("Failure", t.getLocalizedMessage());
            }
        });
    }

    @Override
    public void OnDialogClose(DialogInterface dialogInterface) {
        getTask();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId())
        {
            case R.id.action_search:
            {

                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        toDoAdapter.getFilter().filter(newText);

                        return false;
                    }
                });
                break;
            }

            case R.id.userprofile:
            {
                getUser();
                break;
            }
            case R.id.logout:{
                Intent intent = new Intent(UserToDo.this, MainActivity.class);
                (UserToDo.this).finishAffinity();
                startActivity(intent);
                db.delete();
            }
        }
        return true;
    }

}
