package com.contact.index;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.contact.index.sidebar.IndexBar;
import com.contact.index.sidebar.adapter.CustomAdapter;
import com.contact.index.sidebar.adapter.HeaderRecyclerAndFooterWrapperAdapter;
import com.contact.index.sidebar.model.City;
import com.contact.index.sidebar.suspension.SuspensionDecoration;
import com.contact.index.sidebar.utils.ViewHolder;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private CustomAdapter mAdapter;
    private HeaderRecyclerAndFooterWrapperAdapter mHeaderAdapter;
    private ArrayList<City> mList;

    private SuspensionDecoration mDecoration;

    /**
     * 右侧边栏导航区域
     */
    private IndexBar mIndexBar;

    /**
     * 显示指示器DialogText
     */
    private TextView mTvSideBarHint;
    private int mTitleHeight;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mTvSideBarHint = (TextView) findViewById(R.id.tvSideBarHint);
        mIndexBar = (IndexBar) findViewById(R.id.indexBar);
        initData();
    }

    private void initData() {
        mList = new ArrayList<>();
        LinearLayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(manager);
        mAdapter = new CustomAdapter(this, mList);
        mHeaderAdapter = new HeaderRecyclerAndFooterWrapperAdapter(mAdapter) {

            @Override
            protected void onBindHeaderHolder(ViewHolder holder, int headerPos, int layoutId, Object o) {
                switch (layoutId) {
                    case R.layout.layout_header:
                        LinearLayout ll_more = holder.getView(R.id.ll_more);
                        ll_more.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(MainActivity.this, "我是header", Toast.LENGTH_SHORT).show();
//                                startActivity(new Intent(MainActivity.this, CustomViewActivity.class));
                            }
                        });
                        break;
                    default:
                        break;
                }
            }
        };
        mHeaderAdapter.setHeaderView(R.layout.layout_header, "");
        mRecyclerView.setAdapter(mHeaderAdapter);
        mTitleHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
        SuspensionDecoration sd = new SuspensionDecoration(this, mList);
        sd.setHeaderViewCount(mHeaderAdapter.getHeaderViewCount());
        sd.setColorTitleBg(ContextCompat.getColor(this, R.color.color_f5f5f5));
        sd.setColorTitleFont(ContextCompat.getColor(this, R.color.color_333333));
        sd.setTitleFontSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
        sd.setmTitleHeight(mTitleHeight);
        mRecyclerView.addItemDecoration(mDecoration = sd);
        //如果add两个，那么按照先后顺序，依次渲染。
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        mIndexBar.setmPressedShowTextView(mTvSideBarHint)//设置HintTextView
                .setNeedRealIndex(true)//设置需要真实的索引
                .setmLayoutManager(manager);//设置RecyclerView的LayoutManager
        initDatas(getResources().getStringArray(R.array.provinces));
    }

    /**
     * 组织数据源
     */
    private void initDatas(final String[] data) {
        //延迟200ms 模拟加载数据中....
        getWindow().getDecorView().postDelayed(new Runnable() {
            @Override
            public void run() {
                mList = new ArrayList<>();
                for (String datum : data) {
                    City cityBean = new City();
                    cityBean.setName(datum);//设置城市名称
                    mList.add(cityBean);
                }
                //先排序
                mIndexBar.getDataHelper().sortSourceDatas(mList);
                mIndexBar.setmSourceDatas(mList)//设置数据
                        .setHeaderViewCount(mHeaderAdapter.getHeaderViewCount())//设置HeaderView数量
                        .invalidate();
                mAdapter.setDatas(mList);
                mIndexBar.requestLayout();
                mIndexBar.invalidate();
                mHeaderAdapter.notifyDataSetChanged();
                mDecoration.setmDatas(mList);
            }
        }, 200);
    }
}
