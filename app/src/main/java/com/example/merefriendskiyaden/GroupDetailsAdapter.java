package com.example.merefriendskiyaden;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GroupDetailsAdapter extends RecyclerView.Adapter<GroupDetailsAdapter.ViewHolder> {

    private List<GroupMember> groupMembers;
    Context context;

    public GroupDetailsAdapter(List<GroupMember> groupMembers, Context context) {
        this.groupMembers = groupMembers;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupMember groupMember = groupMembers.get(position);

        holder.nameTextView.setText(groupMember.getName());
        holder.emailTextView.setText(groupMember.getEmail());
        holder.mobileNoTextView.setText(groupMember.getMobileNo());

        holder.llView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                deleteItem();
                // Toast.makeText(context, "Hello", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

    }

    public void deleteItem() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Friend");
        builder.setMessage("Are you sure you want to delete this friend?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public int getItemCount() {
        return groupMembers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView emailTextView;
        public TextView mobileNoTextView;

        public LinearLayout llView;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.edtUserName);
            emailTextView = itemView.findViewById(R.id.edtUserEmail);
            mobileNoTextView = itemView.findViewById(R.id.edtMobileNo);
            llView = itemView.findViewById(R.id.llView);
        }
    }
}
