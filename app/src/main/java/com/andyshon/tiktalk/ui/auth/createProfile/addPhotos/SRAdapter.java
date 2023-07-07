package com.andyshon.tiktalk.ui.auth.createProfile.addPhotos;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public abstract class SRAdapter<T>  extends RecyclerView.Adapter<SRAdapter.SRViewHolder<T>> implements DragCallback {
    public List<T> list;
    public Context context;
    public LayoutInflater layoutInflater;
    private List<Pair<Class<? extends SRViewHolder<T>>,Integer>> holderTypeMap = new ArrayList<>();


    private List<Pair<Class<? extends SRViewHolder<T>>,Integer>> preHolders = getPreHolders();
    private List<Pair<Class<? extends SRViewHolder<T>>,Integer>> postHolders = getPostHolders();

//    private int preCount;
//    private int postCount;
    private SRListener listener;

    public RecyclerView recyclerView;


    public enum ChangeType{
        ADDED,
        MODIFIED,
        REMOVED
    }

    public static class ChangeObject<T>{
        ChangeType changeType;
        int oldPosition;
        int newPosition;
        T obj;
        public ChangeObject(ChangeType changeType,int oldPosition,int newPosition,T obj) {
            this.changeType = changeType;
            this.oldPosition = oldPosition;
            this.newPosition = newPosition;
            this.obj = obj;
        }
    }



    @Override
    public void onItemMoved(int oldPosition, int newPosition) {
        ChangeObject changes = new ChangeObject(ChangeType.MODIFIED, oldPosition, newPosition, list.get(oldPosition));
        renderChanges(changes);
    }

    public abstract Pair<Class<? extends SRViewHolder<T>>,Integer> getHolderType(T object);

    public List<Pair<Class<? extends SRViewHolder<T>>,Integer>> getPreHolders(){
     return null;
    }
    public List<Pair<Class<? extends SRViewHolder<T>>,Integer>> getPostHolders(){
     return null;
    }

    public SRAdapter(Context context, List<T> list, SRListener listener) {
        this.list = list;
        this.context = context;
        this.listener = listener;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    public void addPreholder(Pair<Class<? extends SRViewHolder<T>>,Integer> preHolder){
        if (preHolders == null)preHolders = new ArrayList<>();
        preHolders.add(preHolder);
        notifyItemInserted(preHolders.indexOf(preHolder));
    }
    public void addPostholder(Pair<Class<? extends SRViewHolder<T>>,Integer> preHolder){
        if (postHolders == null)postHolders = new ArrayList<>();
        postHolders.add(preHolder);
        notifyItemInserted(getItemCount() - 1);
    }
    public void removeAllPreholders(){
        if (preHolders != null){
            notifyItemRangeRemoved(0,preHolders.size());
            preHolders = null;
        }
    }

    public void removeAllPostholders(){
        if (postHolders!= null){
            notifyItemRangeRemoved(list.size(),list.size() + postHolders.size());
            postHolders = null;
        }
    }


    public void renderChanges(ChangeObject<T> changeObject){
        switch (changeObject.changeType){
            case ADDED:
                addObject(changeObject.newPosition,changeObject.obj);
                break;
            case REMOVED:
                removeObject(changeObject.oldPosition);
                break;
            case MODIFIED:
                if (changeObject.oldPosition != changeObject.newPosition){
                    moveObject(changeObject.oldPosition,changeObject.newPosition);
                    recyclerView.scrollToPosition(0);
                }
                updateObject(changeObject.newPosition,changeObject.obj);
                break;
        }

    }

    public void updateList(List<T> list){
        this.list = list;
        notifyDataSetChanged();
    }


    public void onItemClick(T item, int type) {
        listener.onItemClick(list.indexOf(item), item, type);
    }


    public void addObject(int position,T obj){
        if (position < 0 || position > list.size() - 1){
            list.add(obj);
            notifyItemInserted(getPreCount() + list.size() - 1);
        }else {
            list.add(position,obj);
            notifyItemInserted(getPreCount() + position);
        }
    }

    public void addObjects(int position, List<T> obj){
        if (position < 0 || position > list.size() - 1){
            int oldSize = list.size();
            list.addAll(obj);
            notifyItemRangeInserted(getPreCount() + oldSize,obj.size());
        }else {
            list.addAll(position,obj);
            notifyItemRangeInserted(getPreCount() + position,obj.size());
        }
    }

    public T removeObject(int position){
        T obj = null;
        if (position >= 0 && position < list.size()) {
            obj = list.remove(position);
            notifyItemRemoved(getPreCount() + position);
        }
        return obj;
    }

    public void removeObjects(int position,int count){
        if (position >= 0 && position < list.size()) {
            list.removeAll(list.subList(position,position + count));
            notifyItemRangeRemoved(getPreCount() + position,count);
        }
    }

    public void updateObject(int position,T obj){
        if (position >= 0 && position < list.size()) {
            if (obj != null){
                list.remove(position);
                list.add(position,obj);
            }
            notifyItemChanged(getPreCount() + position);
        }
    }

    public void moveObject(int from,int to){
        T o = list.remove(from);
        list.add(to,o);
        notifyItemMoved(from,to);
    }


    @Override
    public int getItemViewType(int position) {
        Pair<Class<? extends SRViewHolder<T>>,Integer> holderClass;
        if (position < getPreCount()){
            holderClass = preHolders.get(position);
        }else if (position >= getPreCount() + list.size()){
            holderClass = postHolders.get(position - (getPreCount() + list.size()));
        }else {
            holderClass = getHolderType(list.get(position - getPreCount()));
        }
        if (holderTypeMap.indexOf(holderClass) == -1)holderTypeMap.add(holderClass);
        return holderTypeMap.indexOf(holderClass);

    }



    @NonNull
    @Override
    public SRViewHolder<T> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Pair<Class<? extends SRViewHolder<T>>,Integer> classPair = holderTypeMap.get(viewType);
        View v = layoutInflater.inflate(classPair.second, parent, false);
        SRViewHolder<T> srViewHolder;
        try {
            srViewHolder = classPair.first.getConstructor(View.class,SRAdapter.class).newInstance(v,this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return srViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SRViewHolder<T> holder, int position) {
        if (position >= getPreCount() && position < getPreCount() + list.size()) {
            holder.bindHolder(list.get(position - getPreCount()));
        }else {
            holder.bindHolder(null);
        }
    }



    @Override
    public int getItemCount() {
        return getPreCount() + (list == null ? 0 : list.size()) + getPostCount();
    }

    public int getPreCount(){
        return preHolders == null ? 0 : preHolders.size();
    }
    public int getPostCount(){
        return postHolders == null ? 0 : postHolders.size();
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list){
        this.list = list;
    }


    public static abstract class SRViewHolder<T> extends RecyclerView.ViewHolder {
        public SRAdapter adapter;

        public SRViewHolder(View itemView, SRAdapter adapter) {
            super(itemView);
            this.adapter = adapter;


        }
        public abstract void bindHolder(T object);
    }

}
