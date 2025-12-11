package com.sandhyasofttech.hostelmanagement.Adapters;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.recyclerview.widget.RecyclerView;
import com.sandhyasofttech.hostelmanagement.R;
import com.sandhyasofttech.hostelmanagement.Models.Room;
import java.util.*;
import androidx.core.content.ContextCompat;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.VH> implements Filterable {

    private Context ctx;
    private List<Room> list;
    private List<Room> filtered;
    private RoomListener listener;

    public interface RoomListener {
        void onEdit(Room room);
        void onDelete(Room room);
    }

    public RoomAdapter(Context ctx, List<Room> list, RoomListener listener) {
        this.ctx = ctx;
        this.list = list;
        this.filtered = new ArrayList<>(list);
        this.listener = listener;
    }

    public void updateList(List<Room> newList) {
        this.list = newList;
        this.filtered = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @Override public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_room, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(VH h, int pos) {
        Room r = filtered.get(pos);

        h.tvRoomNo.setText(r.roomNo);
        h.tvType.setText(r.type);
        h.tvCapacity.setText("Capacity " + r.capacity + " • Occupied " + r.occupied);

        int avail = r.available;
        h.tvAvailabilityChip.setText(avail == 0 ? "FULL" : (avail + " FREE"));

        int chipColor = ContextCompat.getColor(ctx,
                avail == 0 ? R.color.room_full : R.color.room_available);
        h.tvAvailabilityChip.getBackground().setTint(chipColor);

        // Card background subtle, no harsh red/green fill
        h.itemViewCard.setCardBackgroundColor(
                ContextCompat.getColor(ctx, android.R.color.white)
        );

        // Click – detail / future navigation
        h.itemViewCard.setOnClickListener(v ->
                Toast.makeText(ctx, "Room " + r.roomNo, Toast.LENGTH_SHORT).show()
        );

        // Long press – show edit/delete menu
        h.itemViewCard.setOnLongClickListener(v -> {
            showRoomOptions(r);
            return true;
        });
    }

    private void showRoomOptions(Room r) {
        String[] opts = {"Edit", "Delete"};
        new android.app.AlertDialog.Builder(ctx)
                .setTitle("Room " + r.roomNo)
                .setItems(opts, (dialog, which) -> {
                    if (which == 0) {
                        if (listener != null) listener.onEdit(r);
                    } else if (which == 1) {
                        if (listener != null) listener.onDelete(r);
                    }
                })
                .show();
    }

    @Override public int getItemCount() { return filtered.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvRoomNo, tvType, tvCapacity, tvAvailabilityChip;
        com.google.android.material.card.MaterialCardView itemViewCard;

        public VH(View v) {
            super(v);
            tvRoomNo = v.findViewById(R.id.tvRoomNo);
            tvType = v.findViewById(R.id.tvType);
            tvCapacity = v.findViewById(R.id.tvCapacity);
            tvAvailabilityChip = v.findViewById(R.id.tvAvailabilityChip);
            itemViewCard = (com.google.android.material.card.MaterialCardView) v;
        }
    }

    @Override public Filter getFilter() {
        return new Filter() {
            @Override protected FilterResults performFiltering(CharSequence constraint) {
                String q = constraint == null ? "" : constraint.toString().toLowerCase().trim();
                FilterResults res = new FilterResults();
                if (q.isEmpty()) {
                    res.values = list;
                    res.count = list.size();
                } else {
                    List<Room> out = new ArrayList<>();
                    for (Room r : list) {
                        if (r.roomNo.toLowerCase().contains(q) ||
                                r.type.toLowerCase().contains(q)) {
                            out.add(r);
                        }
                    }
                    res.values = out;
                    res.count = out.size();
                }
                return res;
            }

            @Override protected void publishResults(CharSequence constraint, FilterResults results) {
                filtered = (List<Room>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}
