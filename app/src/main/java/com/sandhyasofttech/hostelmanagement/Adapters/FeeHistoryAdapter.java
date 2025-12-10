package com.sandhyasofttech.hostelmanagement.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.sandhyasofttech.hostelmanagement.Models.StudentModel;
import com.sandhyasofttech.hostelmanagement.R;

import java.util.ArrayList;
import java.util.List;

public class FeeHistoryAdapter extends RecyclerView.Adapter<FeeHistoryAdapter.FeeVH> implements Filterable {

    public interface OnStudentPdfClick {
        void onPdfClick(StudentModel student);
    }

    private List<StudentModel> originalList;
    private List<StudentModel> filteredList;
    private OnStudentPdfClick pdfClickListener;

    // UPDATED: take listener in constructor
    public FeeHistoryAdapter(List<StudentModel> list, OnStudentPdfClick listener) {
        this.originalList = list;
        this.filteredList = new ArrayList<>(list);
        this.pdfClickListener = listener;
    }

    @Override
    public FeeVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fee_history_student, parent, false);
        return new FeeVH(v);
    }

    @Override
    public void onBindViewHolder(FeeVH h, int pos) {
        StudentModel s = filteredList.get(pos);
        h.tvNameRoom.setText(s.getName() + " (" + s.getRoom() + ")");
        h.tvClass.setText(s.getStudentClass());
        h.tvCardTotal.setText("Total: ₹ " + s.getAnnualFee());
        h.tvCardPaid.setText("Paid: ₹ " + s.getPaidFee());
        h.tvCardRemain.setText("Remain: ₹ " + s.getRemainingFee());

        h.btnStudentPdf.setOnClickListener(v -> {
            if (pdfClickListener != null) {
                pdfClickListener.onPdfClick(s);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence cs) {
                String q = cs.toString().toLowerCase().trim();
                List<StudentModel> tmp = new ArrayList<>();
                if (q.isEmpty()) {
                    tmp.addAll(originalList);
                } else {
                    for (StudentModel s : originalList) {
                        if (s.getName().toLowerCase().contains(q)
                                || s.getRoom().toLowerCase().contains(q)) {
                            tmp.add(s);
                        }
                    }
                }
                FilterResults fr = new FilterResults();
                fr.values = tmp;
                return fr;
            }

            @Override
            protected void publishResults(CharSequence cs, FilterResults results) {
                filteredList = (List<StudentModel>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public List<StudentModel> getCurrentList() {
        return filteredList;
    }

    static class FeeVH extends RecyclerView.ViewHolder {
        TextView tvNameRoom, tvClass, tvCardTotal, tvCardPaid, tvCardRemain;
        TextView btnStudentPdf; // or Button if you used <Button> in XML

        FeeVH(View v) {
            super(v);
            tvNameRoom = v.findViewById(R.id.tvNameRoom);
            tvClass = v.findViewById(R.id.tvClass);
            tvCardTotal = v.findViewById(R.id.tvCardTotal);
            tvCardPaid = v.findViewById(R.id.tvCardPaid);
            tvCardRemain = v.findViewById(R.id.tvCardRemain);
            btnStudentPdf = v.findViewById(R.id.btnStudentPdf);
        }
    }
}
