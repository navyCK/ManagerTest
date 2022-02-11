package com.herblinker.android.libraries.base.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.herblinker.android.libraries.R;
import com.herblinker.android.libraries.base.net.NameValuePair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FixedTableListBase<DTO, ViewHolder extends RecyclerView.ViewHolder, Adapter extends RecyclerView.Adapter<ViewHolder>> extends LinearLayout {
    private static final int HERBLINKER_ORDER_TYPE_PREVIOUS_AMOUNT_PAGE = -2;
    private static final int HERBLINKER_ORDER_TYPE_NEXT_AMOUNT_PAGE = 2;
    private static final int HERBLINKER_ORDER_TYPE_PREVIOUS_DATA = -3;
    private static final int HERBLINKER_ORDER_TYPE_NEXT_DATA = 3;
    private static final int HERBLINKER_ORDER_TYPE_MOVE_PAGE = 100;

    private static final int HERBLINKER_DEFAULT_FROM = 0;
    private static final int HERBLINKER_DEFAULT_TO = 0;
    private static final int HERBLINKER_DEFAULT_NUMBER = 20;
    private static final int HERBLINKER_DEFAULT_PAGE = 1;
    private static final int HERBLINKER_DEFAULT_PAGE_AMOUNT = 5;

    private TextView title;
    private LinearLayout tools;
    private LinearLayout tableHeader;
    private RecyclerView tableBody;
    private LinearLayout pagination;

    private String titleString;

    private RecyclerView.Adapter<ViewHolder> adapter;

    //view 변수
    List<DTO> items;
    List<DTO> filteredItems;
    List<DTO> itemsForList;

    //운영변수
    private FixedTableListAdapter<DTO, ViewHolder, Adapter> tableListAdapter;
    private String url;
    private int from = HERBLINKER_DEFAULT_FROM;
    private int to = HERBLINKER_DEFAULT_TO;
    private int number = HERBLINKER_DEFAULT_NUMBER;
    private int page = HERBLINKER_DEFAULT_PAGE;
    private int pageAmount = HERBLINKER_DEFAULT_PAGE_AMOUNT;

    public FixedTableListBase(Context context) {
        super(context);
        initializeViews(context, null);
    }


    public FixedTableListBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context, attrs);
    }

    public FixedTableListBase(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeViews(context, attrs);
    }

    private void initializeViews(Context context, AttributeSet attrs) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.table_list_layout, this, false);

        title = view.findViewById(R.id.title);
        tools = view.findViewById(R.id.tools);
        tableHeader = view.findViewById(R.id.tableHeader);
        tableBody = view.findViewById(R.id.tableBody);
        pagination = view.findViewById(R.id.pagination);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TableListBase);
        titleString = typedArray.getString(0);
        boolean titleVisibility = typedArray.getBoolean(R.styleable.TableListBase_titleVisibility, false);
        title.setVisibility(titleVisibility ? VISIBLE : GONE);
        typedArray.recycle();
        addView(view);
    }

    /**
     * 공통 생성자
     */
    {
        this.filteredItems = new LinkedList<>();
        this.itemsForList = new ArrayList<>(pageAmount*number+1);
    }

    protected void setup() {

    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        setup();

        if (titleString != null && !titleString.equals(""))
            title.setText(titleString);
        tableBody.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecyclerView.Adapter<ViewHolder>() {
            {
                setHasStableIds(true);
            }
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return tableListAdapter.onCreateViewHolder(parent, viewType);
            }

            @Override
            public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                tableListAdapter.onBindViewHolder(holder, position);
            }

            @Override
            public int getItemCount() {
                return itemsForList.size();
            }
        };
        tableBody.setAdapter(adapter);
    }

    public void setHeader(View view) {
        this.tableHeader.removeAllViews();
        this.tableHeader.addView(view);
    }

    public void setAdapter(FixedTableListBase.FixedTableListAdapter<DTO, ViewHolder, Adapter> tableListAdapter) {
        this.tableListAdapter = tableListAdapter;
        tableListAdapter.tableListBase = this;
    }

    public void addTool(View view) {
        this.tools.addView(view);
    }

    public void addTool(View view, int index) {
        this.tools.addView(view, index);
    }

    public void removeTool(View view) {
        this.tools.removeView(view);
    }

    public void removeTool(int index) {
        this.tools.removeViewAt(index);
    }

    public boolean innerSearchFilter(DTO dto) {
        return true;
    }

    public void reload() {
        this.movePage(this.page);
    }


    protected void movePreviousAmountPage() {
        movePage(this.page-this.pageAmount);
    }

    protected void moveNextAmountPage() {
        movePage(this.page+this.pageAmount);
    }

    public void movePage() {
        movePage(this.page);
    }
    public void movePage(int page) {
        this.page=page;
        this.filteredItems=this.items;

        repaintContent(this.page);
    }
    public void setData(List<DTO> items){
        this.items=items;
        movePage();
    }

    public void repaintPagination() {
        boolean existLeft;
        int page = this.page;
        int firstPage;
        int lastPage;
        int pageAmount = this.pageAmount;
        boolean existRight;

        int rawPossiblePageAmount = this.items.size() % this.number == 0 ? this.items.size() / this.number : this.items.size() / this.number + 1;
        int possiblePageAmount = this.filteredItems.size() % this.number == 0 ? this.filteredItems.size() / this.number : this.filteredItems.size() / this.number + 1;
        if (this.page % this.pageAmount == 0)
            firstPage = this.page - this.pageAmount + 1;
        else
            firstPage = this.page - this.page % this.pageAmount + 1;
        existLeft = firstPage != 1;
        lastPage = firstPage + possiblePageAmount - 1;
        existRight = rawPossiblePageAmount > this.pageAmount;
        makePagination(this, existLeft, page, firstPage, lastPage, pageAmount, existRight);
    }

    public void repaintContent(){
        repaintContent(this.page);
    }
    public void repaintContent(int page) {
        this.tableListAdapter.beforeRepaint(page);
        this.page = page;

        this.repaintPagination();

        int pageSeq = this.page % this.pageAmount;
        if (pageSeq == 0)
            pageSeq = this.pageAmount;
        this.itemsForList.clear();
        if (this.number > 0) {
            for (int i = (pageSeq - 1) * this.number; i < this.number * pageSeq && i < this.filteredItems.size(); ++i)
                this.itemsForList.add(this.filteredItems.get(i));
        } else {
            this.itemsForList.addAll(this.filteredItems);
        }
        adapter.notifyDataSetChanged();

        this.tableListAdapter.afterRepaint(page);
    }
    protected void makePagination(FixedTableListBase<DTO, ViewHolder, Adapter> tableListBase, boolean existLeft, int page, int firstPage, int lastPage, int pageAmount, boolean existRight) {

    }

    protected LinearLayout getPagination() {
        return pagination;
    }

    public static abstract class FixedTableListAdapter<DTO, ViewHolder extends RecyclerView.ViewHolder, Adapter extends RecyclerView.Adapter<ViewHolder>> extends RecyclerView.Adapter<ViewHolder> {
        private FixedTableListBase<DTO, ViewHolder, Adapter> tableListBase;

        protected abstract void onBindViewHolder(@NonNull ViewHolder holder, int position, DTO dto);

        List<DTO> items;
        protected void beforeRepaint(int page) {

        }

        protected void afterRepaint(int page) {

        }

        @Override
        public final int getItemCount() {
            return tableListBase.filteredItems.size();
        }

        @Override
        public final void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            onBindViewHolder(holder, position, tableListBase.itemsForList.get(position));
        }
    }
}
