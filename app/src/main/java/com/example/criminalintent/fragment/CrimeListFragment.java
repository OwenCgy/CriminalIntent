package com.example.criminalintent.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.criminalintent.java.Crime;
import com.example.criminalintent.java.CrimeLab;
import com.example.criminalintent.R;
import com.example.criminalintent.util.DateUtil;
import com.example.criminalintent.helper.TestDividerItemDecoration;

import java.util.List;
import java.util.UUID;

import static android.content.ContentValues.TAG;

/**
 * Created by sy on 2017/11/25.
 */

public class CrimeListFragment extends Fragment {

    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";

    private TextView mTextView;

    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mAdapter;
    private boolean mSubtitleVisible;
    //private int position; //设置列表位置的全局变量
    private Callbacks mCallbacks;
    private OnDeleteCrimeListener mDeleteCalLBack;

    public interface OnDeleteCrimeListener {
        void onCrimeIdSelected(UUID crimeId);
    }

    public interface Callbacks {
        void onCrimeSelected(Crime crime);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
        mDeleteCalLBack = (OnDeleteCrimeListener) context;
    }

    public void setCrimeRecyclerViewItemTouchListener() {

        ItemTouchHelper.SimpleCallback itemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Crime crime= mAdapter.mCrimes.get(position);
                Log.d(TAG, "onSwiped: " + crime.getId());
                mDeleteCalLBack.onCrimeIdSelected(crime.getId());


            }
        };

        ItemTouchHelper iteItemTouchHelper = new ItemTouchHelper(itemTouchCallback);
        iteItemTouchHelper.attachToRecyclerView(mCrimeRecyclerView);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container , false);
        mCrimeRecyclerView = (RecyclerView) view.findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCrimeRecyclerView.addItemDecoration(new TestDividerItemDecoration());
        mCrimeRecyclerView.setHasFixedSize(true);
        setCrimeRecyclerViewItemTouchListener();

        mTextView=(TextView)view.findViewById(R.id.crime_set_empty_text_view);

        //一：使app在旋转时仍能看到子标题在新建视图中依然能正确显示
        if (savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }

        updateUI();

        return view;
    }
    public void deleteCrime(UUID crimeId) {
        Crime crime = CrimeLab.get(getActivity()).getCrime(crimeId);
        CrimeLab.get(getActivity()).deleteCrime(crime);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }
    //二：使app在旋转时仍能看到子标题在新建视图中依然能正确显示
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
        mDeleteCalLBack = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);

        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
        if (mSubtitleVisible) {
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subtitleItem.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_crime:
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                updateUI();
                mCallbacks.onCrimeSelected(crime);
                return true;
            case R.id.show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //设置工具栏子标题
    private void updateSubtitle() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        //确保单复数的错误
        int crimeSize = crimeLab.getCrimes().size();
        String subtitle = getResources().getQuantityString(R.plurals.subtitle_plural,crimeSize,crimeSize);

        if (!mSubtitleVisible) {
            subtitle = null;
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    public void updateUI() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();
        if (mAdapter == null) {
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setCrimes(crimes);
           // mAdapter.notifyItemChanged(position);
            mAdapter.notifyDataSetChanged();
        }
        if(crimes.size() > 0){
            mTextView.setVisibility(View.GONE);
        }else{
            mTextView.setVisibility(View.VISIBLE);
        }
        updateSubtitle();
    }


    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Crime mCrime;
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private ImageView mSolvedImageView;


        public CrimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_crime, parent, false));
            itemView.setOnClickListener(this);
            mTitleTextView = (TextView) itemView.findViewById(R.id.crime_title);
            mDateTextView = (TextView) itemView.findViewById(R.id.crime_date);
            mSolvedImageView = (ImageView) itemView.findViewById(R.id.crime_solved);
        }

        public void bind(Crime crime){
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());
            mDateTextView.setText(new DateUtil().getNowDateTime(null));
            mSolvedImageView.setVisibility(mCrime.isSolved() ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onClick(View v) {
           // position = getPosition(v);//在点击列表中某一项的同时获取该项在列表中的位置。
            mCallbacks.onCrimeSelected(mCrime);
        }
        //获取列表的position以实现高效刷新
        /*public int getPosition(View view) {
            return ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();
        }*/
    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> //implements ItemTouchHelperAdapter {
    {

        private List<Crime> mCrimes;

        public CrimeAdapter(List<Crime> crimes){
            mCrimes = crimes;
        }
        @Override
        public CrimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new CrimeHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(CrimeHolder holder, int position) {
            Crime crime = mCrimes.get(position);
            holder.bind(crime);
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }


        public void setCrimes(List<Crime> crimes) {
            mCrimes = crimes;
        }

    }

}
