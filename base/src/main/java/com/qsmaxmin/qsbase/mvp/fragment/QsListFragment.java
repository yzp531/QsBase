package com.qsmaxmin.qsbase.mvp.fragment;

import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.qsmaxmin.qsbase.R;
import com.qsmaxmin.qsbase.common.aspect.ThreadPoint;
import com.qsmaxmin.qsbase.common.aspect.ThreadType;
import com.qsmaxmin.qsbase.common.log.L;
import com.qsmaxmin.qsbase.common.widget.viewpager.headerpager.base.InnerScroller;
import com.qsmaxmin.qsbase.common.widget.viewpager.headerpager.base.InnerScrollerContainer;
import com.qsmaxmin.qsbase.common.widget.viewpager.headerpager.base.OuterScroller;
import com.qsmaxmin.qsbase.mvp.adapter.QsListAdapterItem;
import com.qsmaxmin.qsbase.mvp.presenter.QsPresenter;

import java.util.ArrayList;
import java.util.List;

/**
 * @CreateBy qsmaxmin
 * @Date 17/7/2  下午4:29
 * @Description
 */
public abstract class QsListFragment<P extends QsPresenter, D> extends QsFragment<P> implements QsIListFragment<D>, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, AbsListView.OnScrollListener, InnerScrollerContainer {

    protected final List<D> mList = new ArrayList<>();

    private ListView    mListView;
    private BaseAdapter mListAdapter;
    private View        headerView;
    private View        footerView;


    @Override public int layoutId() {
        return R.layout.qs_fragment_listview;
    }

    @Override public int getHeaderLayout() {
        return 0;
    }

    @Override public int getFooterLayout() {
        return 0;
    }

    @Override public int getTopLayout() {
        return 0;
    }

    @Override public int getBottomLayout() {
        return 0;
    }

    @Override public ListView getListView() {
        return mListView;
    }

    @Override public int getViewTypeCount() {
        return 1;
    }

    @Override public int getItemViewType(int position) {
        return 0;
    }

    @Override protected View initView(LayoutInflater inflater) {
        View rootView = super.initView(inflater);
        if (getTopLayout() > 0 || getBottomLayout() > 0) initTopBottomView(rootView, inflater);
        initListView(inflater, rootView);
        return rootView;
    }


    @Override protected int rootViewLayoutId() {
        return (getTopLayout() > 0 || getBottomLayout() > 0) ? R.layout.qs_fragment_state_with_top_bottom : super.rootViewLayoutId();
    }

    protected void initTopBottomView(View rootView, LayoutInflater inflater) {
        if (rootView instanceof LinearLayout) {
            if (getTopLayout() > 0) {
                ((LinearLayout) rootView).addView(inflater.inflate(getTopLayout(), null), 0);
            }
            if (getBottomLayout() > 0) {
                int childCount = ((LinearLayout) rootView).getChildCount();
                ((LinearLayout) rootView).addView(inflater.inflate(getBottomLayout(), null), childCount);
            }
        } else {
            L.e(initTag(), "layoutId() root must be LinearLayout when getTopLayout() or getBottomLayout() is overwrite !");
        }
    }

    /**
     * 初始化ListView
     */
    protected void initListView(LayoutInflater inflater, View view) {
        if (view instanceof ListView) {
            mListView = (ListView) view;
        } else {
            mListView = (ListView) view.findViewById(android.R.id.list);
        }
        if (mListView == null) throw new RuntimeException("ListView is not exit or its id not 'android.R.id.list' in current layout!!");
        if (getHeaderLayout() != 0) {
            headerView = inflater.inflate(getHeaderLayout(), null);
            mListView.addHeaderView(headerView);
        }
        if (getFooterLayout() != 0) {
            footerView = inflater.inflate(getFooterLayout(), null);
            mListView.addFooterView(footerView);
        }
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
        BaseAdapter adapter = onCreateAdapter();
        if (adapter != null) {
            mListAdapter = adapter;
        } else {
            mListAdapter = new MyAdapter();
        }
        mListView.setAdapter(mListAdapter);
        mListView.setOnScrollListener(this);
    }


    @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        L.i(initTag(), "onItemClick()... position : " + position);
    }

    @Override public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }


    @Override public void setData(List<D> list) {
        setData(list, true);
    }

    @Override public void setData(List<D> list, boolean showEmptyView) {
        synchronized (mList) {
            mList.clear();
            if (list != null && !list.isEmpty()) mList.addAll(list);
            updateAdapter(showEmptyView);
        }
    }

    @Override public void addData(D d) {
        if (d != null) {
            synchronized (mList) {
                mList.add(d);
                updateAdapter(true);
            }
        }
    }

    @Override public void addData(List<D> list) {
        if (list != null && !list.isEmpty()) {
            synchronized (mList) {
                mList.addAll(list);
                updateAdapter(true);
            }
        }
    }

    @Override public void delete(int position) {
        synchronized (mList) {
            if (position >= 0 && position < mList.size()) {
                mList.remove(position);
                updateAdapter(true);
            }
        }
    }

    @Override public void delete(D d) {
        if (d == null) return;
        synchronized (mList) {
            mList.remove(d);
            updateAdapter(true);
        }
    }

    @Override public void deleteAll() {
        synchronized (mList) {
            mList.clear();
            updateAdapter(true);
        }
    }

    @Override public final List<D> getData() {
        return mList;
    }

    @Override public D getData(int position) {
        if (position >= 0 && position < mList.size()) {
            return mList.get(position);
        }
        return null;
    }

    @ThreadPoint(ThreadType.MAIN) @Override public void updateAdapter(boolean showEmptyView) {
        if (mListAdapter != null) {
            mListAdapter.notifyDataSetChanged();
            if (mViewAnimator != null) {
                if (mList.isEmpty() && showEmptyView) {
                    showEmptyView();
                } else {
                    showContentView();
                }
            }
        }
    }

    @Override public BaseAdapter onCreateAdapter() {
        return null;
    }

    @Override public BaseAdapter getAdapter() {
        return mListAdapter;
    }

    public View getHeaderView() {
        return headerView;
    }

    public View getFooterView() {
        return footerView;
    }

    /**
     * 自带adapter，如果需要重写{@link #onCreateAdapter()}
     */
    private final class MyAdapter extends BaseAdapter {

        @Override public int getCount() {
            return mList.size();
        }

        @Override public Object getItem(int position) {
            if (position > getCount() - 1) return null;
            return mList.get(position);
        }

        @Override public long getItemId(int position) {
            return position;
        }

        @Override public int getItemViewType(int position) {
            return QsListFragment.this.getItemViewType(position);
        }

        /**
         * 返回类型数量
         **/
        @Override public int getViewTypeCount() {
            return QsListFragment.this.getViewTypeCount();
        }

        /**
         * getView
         **/
        @Override public View getView(int position, View convertView, ViewGroup parent) {
            QsListAdapterItem item = null;
            if (convertView == null) {
                int count = getViewTypeCount();
                if (count > 1) {
                    int type = getItemViewType(position);
                    item = getListAdapterItem(type);
                } else {
                    item = getListAdapterItem(0);
                }
                convertView = LayoutInflater.from(getActivity()).inflate(item.getItemLayout(), null, false);
                item.init(convertView);
                convertView.setTag(item);
            }
            if (item == null) item = (QsListAdapterItem) convertView.getTag();
            if (item != null) item.bindData(getItem(position), position, getCount());
            return convertView;
        }
    }


    @Override public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }

    /**
     * listView是否滑动到顶部
     */
    @Override public boolean canListScrollDown() {
        return ViewCompat.canScrollVertically(getListView(), -1);
    }

    /**
     * listView是否滑动到底部
     */
    @Override public boolean canListScrollUp() {
        return ViewCompat.canScrollVertically(getListView(), 1);
    }


    /*----------------------- 以下是HeaderViewPager支持 ----------------------------*/
    @Override public void setMyOuterScroller(OuterScroller outerScroller, int myPosition) {
        if (getListView() instanceof InnerScroller) {
            L.i(initTag(), "注册调度控件：  position:" + myPosition);
            ((InnerScroller) getListView()).register2Outer(outerScroller, myPosition);
        }
    }
}
