package com.raina.todo20;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ToDoAdapterVH> implements Filterable {

    private List<TaskResponse> mList, mListAll;
    private UserToDo activity;
    private Context context;
    private String token = MainActivity.token;

    public ToDoAdapter(Context c, UserToDo activity)
    {
        this.context = c;
        this.activity = activity;
    }

    public void setData(List<TaskResponse> taskList)
    {
        this.mList = new ArrayList<>(taskList);
        this.mListAll = new ArrayList<>(mList);
        notifyDataSetChanged();
    }

    @NonNull
    @NotNull
    @Override
    public ToDoAdapter.ToDoAdapterVH onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new ToDoAdapter.ToDoAdapterVH(LayoutInflater.from(context).inflate(R.layout.task_layout,
                parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ToDoAdapter.ToDoAdapterVH holder, int position) {

        TaskResponse taskResponse = mList.get(position);

        String task = taskResponse.getTitle();

        holder.title.setText(task);

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ToDoAdapterVH extends RecyclerView.ViewHolder {

        TextView id, title;

        public ToDoAdapterVH(@NonNull @NotNull View itemView) {
            super(itemView);

            id = itemView.findViewById(R.id.taskid);
            title = itemView.findViewById(R.id.task);

        }
    }

    public void editTask(int position)
    {
        TaskResponse item = mList.get(position);

        Bundle bundle = new Bundle();
        bundle.putInt("id" , item.getId());
        bundle.putString("task" , item.getTitle());

        AddNewTask task = new AddNewTask(context);
        task.setArguments(bundle);
        task.show(activity.getSupportFragmentManager() , task.getTag());
    }


    public void DeleteTask(int position)
    {
        TaskResponse item = mList.get(position);

        Call<Void> taskResponseCall = ApiClient.getUserService().Delete(token, item.getId());

        taskResponseCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.isSuccessful())
                {
                    Toast.makeText(context, "Task Deleted Successfully", Toast.LENGTH_SHORT).show();
                    mList.remove(position);
                    mListAll.remove(position);
                    notifyItemRemoved(position);
                    if(context instanceof UserToDo)
                    ((UserToDo)context).getTask();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context,"Throwable "+t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public Context getContext(){
        return activity;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<TaskResponse> filteredList = new ArrayList<>();
            if(constraint.toString().isEmpty())
                filteredList.addAll(mListAll);
            else
            {

                for( TaskResponse model : mListAll)
                {
                    if(model.getTitle().toLowerCase().contains(constraint.toString().toLowerCase()))
                        filteredList.add(model);
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mList.clear();
            mList.addAll((Collection<? extends TaskResponse>) results.values);
            notifyDataSetChanged();
        }
    };
}
