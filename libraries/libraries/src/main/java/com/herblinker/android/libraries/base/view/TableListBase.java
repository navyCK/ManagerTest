package com.herblinker.android.libraries.base.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.herblinker.android.libraries.R;
import com.herblinker.android.libraries.base.data.JsonDataException;
import com.herblinker.android.libraries.base.data.JsonObject;
import com.herblinker.android.libraries.base.data.JsonObjectArray;
import com.herblinker.android.libraries.base.net.HerbLinkerJsonGetter;
import com.herblinker.android.libraries.base.net.NameValuePair;
import com.herblinker.android.libraries.base.net.OnGetterListener;
import com.herblinker.libraries.base.data.DataException;
import com.herblinker.libraries.base.data.DataWrongFormatException;
import com.herblinker.libraries.base.data.DateType;
import com.herblinker.libraries.base.data.InputReader;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class TableListBase<DTO, ViewHolder extends RecyclerView.ViewHolder, Adapter extends RecyclerView.Adapter<ViewHolder>> extends LinearLayout {
    private static final String HERBLINKER_FROM_NAME = "from";
    private static final String HERBLINKER_TO_NAME = "to";
    private static final String HERBLINKER_NUMBER_NAME = "number";
    private static final String HERBLINKER_PAGE_NAME = "page";
    private static final String HERBLINKER_PAGE_AMOUNT_NAME = "pageAmount";
    private static final String HERBLINKER_ORDER_TYPE_NAME = "orderType";
    private static final String HERBLINKER_ORDER_VALUE_NAME = "orderValue";

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

    //view 변수
    List<DTO> items;
    List<DTO> filteredItems;
    List<DTO> itemsForList;

    //운영변수
    private TableListAdapter<DTO, ViewHolder, Adapter> tableListAdapter;
    private String url;
    private Date responseTime;
    private int from = HERBLINKER_DEFAULT_FROM;
    private int to = HERBLINKER_DEFAULT_TO;
    private int number = HERBLINKER_DEFAULT_NUMBER;
    private int page = HERBLINKER_DEFAULT_PAGE;
    private int pageAmount = HERBLINKER_DEFAULT_PAGE_AMOUNT;

    private Set<NameValuePair> parameterMap;
    private String whatForInnerSearch;


    private TextView title;
    private LinearLayout tools;
    private LinearLayout tableHeader;
    private RecyclerView tableBody;
    private LinearLayout pagination;

    private String titleString;

    private RecyclerView.Adapter<ViewHolder> adapter;

    private AlertDialog loader;
    public TableListBase(Context context) {
        super(context);
        initializeViews(context, null);
    }


    public TableListBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context, attrs);
    }

    public TableListBase(Context context, AttributeSet attrs, int defStyleAttr) {
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
        this.parameterMap = new HashSet<>();
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

    public void setAdapter(TableListAdapter<DTO, ViewHolder, Adapter> tableListAdapter) {
        this.tableListAdapter = tableListAdapter;
        tableListAdapter.tableListBase = this;
    }

    private void addToMap(NameValuePair parameter) {
        this.parameterMap.remove(parameter);
        this.parameterMap.add(parameter);
    }
    public Date getResponseTime(){
        return responseTime;
    }

    public boolean innerSearchFilter(DTO dto) {
        return true;
    }

    public void reload() {
        reload(null);
    }

    public void reload(Set<NameValuePair> additionalParameters) {
        this.movePage(this.page, additionalParameters);
    }

    public void movePage(int page) {
        movePage(page, null);
    }

    public void movePage(int page, Set<NameValuePair> additionalParameters) {
        addToMap(new NameValuePair(HERBLINKER_ORDER_TYPE_NAME, HERBLINKER_ORDER_TYPE_MOVE_PAGE));
        addToMap(new NameValuePair(HERBLINKER_ORDER_VALUE_NAME, page));
        if (additionalParameters != null)
            for (NameValuePair parameter : additionalParameters) {
                try {
                    switch (parameter.name) {
                        case HERBLINKER_FROM_NAME:
                            this.from = InputReader.getInteger(parameter.value, null).intValue();
                            break;
                        case HERBLINKER_TO_NAME:
                            this.to = InputReader.getInteger(parameter.value, null).intValue();
                            break;
                        case HERBLINKER_NUMBER_NAME:
                            this.number = InputReader.getInteger(parameter.value, null).intValue();
                            break;
                        case HERBLINKER_PAGE_NAME:
                            this.page = InputReader.getInteger(parameter.value, null).intValue();
                            addToMap(new NameValuePair(HERBLINKER_ORDER_TYPE_NAME, HERBLINKER_ORDER_TYPE_MOVE_PAGE));
                            addToMap(new NameValuePair(HERBLINKER_ORDER_VALUE_NAME, this.page));
                            break;
                        case HERBLINKER_PAGE_AMOUNT_NAME:
                            this.pageAmount = InputReader.getInteger(parameter.value, null).intValue();
                            break;
                    }
                } catch (DataWrongFormatException e) {
                    e.printStackTrace();
                }
                this.parameterMap.add(parameter);
            }
        sendOrder();
    }

    private void previousData(int amount) {
        addToMap(new NameValuePair(HERBLINKER_ORDER_TYPE_NAME, HERBLINKER_ORDER_TYPE_PREVIOUS_DATA));
        addToMap(new NameValuePair(HERBLINKER_ORDER_VALUE_NAME, amount));
        this.sendOrder();
    }

    private void nextData(int amount) {
        addToMap(new NameValuePair(HERBLINKER_ORDER_TYPE_NAME, HERBLINKER_ORDER_TYPE_NEXT_DATA));
        addToMap(new NameValuePair(HERBLINKER_ORDER_VALUE_NAME, amount));
        this.sendOrder();
    }

    protected void movePreviousAmountPage() {
        addToMap(new NameValuePair(HERBLINKER_ORDER_TYPE_NAME, HERBLINKER_ORDER_TYPE_PREVIOUS_AMOUNT_PAGE));
        this.sendOrder();
    }

    protected void moveNextAmountPage() {
        addToMap(new NameValuePair(HERBLINKER_ORDER_TYPE_NAME, HERBLINKER_ORDER_TYPE_NEXT_AMOUNT_PAGE));
        this.sendOrder();
    }

    private void sendOrder() {
        addToMap(new NameValuePair(HERBLINKER_FROM_NAME, from));
        addToMap(new NameValuePair(HERBLINKER_TO_NAME, to));
        addToMap(new NameValuePair(HERBLINKER_NUMBER_NAME, number));
        addToMap(new NameValuePair(HERBLINKER_PAGE_NAME, page));
        addToMap(new NameValuePair(HERBLINKER_PAGE_AMOUNT_NAME, pageAmount));
        this.getData(this.url, this.parameterMap);
    }

    public void getData(String url, Set<NameValuePair> parameterMap) {
        getData(url, parameterMap, null);
    }
    public void getData(String url, Set<NameValuePair> parameterMap, Runnable after) {
        if(loader!=null) {
            return;
        }
        loader = new AlertDialog.Builder(getContext()).setView(new ProgressBar(getContext()))
                .setCancelable(false)
                .setMessage("데이터를 불러오는 중입니다.")
                .create();
        loader.show();
        if (url != null)
            this.url = url;
        if (parameterMap != null)
            if (this.parameterMap != parameterMap)
                for (NameValuePair parameter : parameterMap) {
                    try {
                        switch (parameter.name) {
                            case HERBLINKER_FROM_NAME:
                                this.from = InputReader.getInteger(parameter.value, null).intValue();
                                break;
                            case HERBLINKER_TO_NAME:
                                this.to = InputReader.getInteger(parameter.value, null).intValue();
                                break;
                            case HERBLINKER_NUMBER_NAME:
                                this.number = InputReader.getInteger(parameter.value, null).intValue();
                                break;
                            case HERBLINKER_PAGE_NAME:
                                this.page = InputReader.getInteger(parameter.value, null).intValue();
                                addToMap(new NameValuePair(HERBLINKER_ORDER_TYPE_NAME, HERBLINKER_ORDER_TYPE_MOVE_PAGE));
                                addToMap(new NameValuePair(HERBLINKER_ORDER_VALUE_NAME, this.page));
                                break;
                            case HERBLINKER_PAGE_AMOUNT_NAME:
                                this.pageAmount = InputReader.getInteger(parameter.value, null).intValue();
                                break;
                        }
                    } catch (DataWrongFormatException e) {
                        e.printStackTrace();
                    }
                    if(this.parameterMap.contains(parameter))
                        this.parameterMap.remove(parameter);
                    this.parameterMap.add(parameter);
                }
        List<NameValuePair> parameters = new LinkedList<>();
        for (NameValuePair parameter : this.parameterMap)
            parameters.add(parameter);
        HerbLinkerJsonGetter herbLinkerJsonGetter = tableListAdapter.getGetter();
        if (herbLinkerJsonGetter == null)
            herbLinkerJsonGetter = new HerbLinkerJsonGetter() {
                @Override
                protected void responseTo(Object object, int code) {

                }
            };
        herbLinkerJsonGetter.execute(new TableListGetterListener(this, after), url, parameters);
    }

    boolean repaint(String responseTime, String resultCode, JsonObject jsonObject) {
        if (resultCode == null || !resultCode.equals("00"))
            return false;
        try {
            /*titleString = json.getString("title");
            if (titleString != null && !titleString.equals(""))
                title.setText(titleString);*/
            this.responseTime = InputReader.getDateBySeparator(responseTime, DateType.YEAR_TO_SENCONDS, null);
            this.from = jsonObject.getInteger("from", null).intValue();
            this.to = jsonObject.getInteger("to", null).intValue();
            this.number = jsonObject.getInteger("number", null).intValue();
            this.page = jsonObject.getInteger("page", null).intValue();
            this.pageAmount = jsonObject.getInteger("pageAmount", null).intValue();
            JsonObjectArray items = jsonObject.getJsonObjectArray("items");
            List<DTO> dtos = new LinkedList<>();
            for (int i = 0; i < items.size(); ++i)
                dtos.add(this.tableListAdapter.makeDTO(items.getJsonObject(i)));
            this.items = dtos;
            this.filteredItems.clear();
            DTO filtered;
            for (DTO dto : this.items) {
                filtered = this.tableListAdapter.filter(whatForInnerSearch, dto);
                if (filtered != null)
                    this.filteredItems.add(filtered);
            }
        } catch (DataException e) {
            e.printStackTrace();
            return false;
        }
        //페이지 검증
        int pageSeq = this.page % this.pageAmount;
        if (pageSeq == 0)
            pageSeq = this.pageAmount;
        int possiblePageAmount = this.filteredItems.size() % this.number == 0 ? this.filteredItems.size() / this.number : this.filteredItems.size() / this.number + 1;
        if (possiblePageAmount > this.pageAmount)
            possiblePageAmount = this.pageAmount;
        if (pageSeq < 0) {
            int firstPage;
            if (this.page % this.pageAmount == 0)
                firstPage = this.page - this.pageAmount + 1;
            else
                firstPage = this.page - this.page % this.pageAmount + 1;
            this.page = firstPage;
        } else if (pageSeq > possiblePageAmount) {
            int firstPage;
            if (this.page % this.pageAmount == 0)
                firstPage = this.page - this.pageAmount + 1;
            else
                firstPage = this.page - this.page % this.pageAmount + 1;
            this.page = firstPage + possiblePageAmount - 1;
        }
        if (this.page <= 0)
            this.page = 1;

        //내용물 처리
        this.repaintContent(this.page);
        return true;
    }

    protected void success(String responseTime, String resultCode, JsonObject json) {
        this.tableListAdapter.success(responseTime, resultCode, json);
    }

    protected void error(boolean networkFail, boolean invalidJson, boolean invalidHLJson, String rawJson) {
        this.tableListAdapter.error(networkFail, invalidJson, invalidHLJson, rawJson);

    }

    public void complete() {
        this.loader.dismiss();
        this.loader=null;
        this.parameterMap.remove(new NameValuePair(HERBLINKER_ORDER_TYPE_NAME, ""));
        this.parameterMap.remove(new NameValuePair(HERBLINKER_ORDER_VALUE_NAME, ""));
        tableListAdapter.complete();
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

    /**
     * For Override
     * @param tableListBase
     * @param existLeft
     * @param page
     * @param firstPage
     * @param lastPage
     * @param pageAmount
     * @param existRight
     */
    protected void makePagination(TableListBase<DTO, ViewHolder, Adapter> tableListBase, boolean existLeft, int page, int firstPage, int lastPage, int pageAmount, boolean existRight) {

    }

    public void setHeader(View view) {
        this.tableHeader.removeAllViews();
        this.tableHeader.addView(view);
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

    public void changeNumber(int afterNumber) {
        int beforeNumber = this.number;
        if (beforeNumber == afterNumber)
            return;

        int previousAmount;
        int offset = 0;
        if (this.page % this.pageAmount == 0)
            previousAmount = this.page - this.pageAmount;
        else
            previousAmount = this.page - this.page % this.pageAmount;
        previousAmount *= beforeNumber;

        int pageOffset;
        if (this.page % this.pageAmount == 0)
            pageOffset = this.pageAmount;
        else
            pageOffset = this.page % this.pageAmount;
        pageOffset = (pageOffset - 1) * this.number;

        DTO targetItem = this.filteredItems.get(pageOffset);
        if (targetItem == null)
            if (this.filteredItems.size() != 0)
                targetItem = this.filteredItems.get(this.filteredItems.size() - 1);

        if (targetItem == null)
            targetItem = this.items.get(0);

        if (targetItem != null)
            for (int i = 0; i < this.items.size(); ++i)
                if (this.items.get(i) == targetItem) {
                    offset = i;
                    break;
                }
        this.number = afterNumber;
        int targetPage = ((previousAmount + offset + 1) % this.number) == 0 ? (previousAmount + offset + 1) / this.number : (previousAmount + offset + 1) / this.number + 1;
        movePage(targetPage);
    }

    protected TextView getTitle() {
        return title;
    }

    protected LinearLayout getTools() {
        return tools;
    }

    protected LinearLayout getTableHeader() {
        return tableHeader;
    }

    protected RecyclerView getTableBody() {
        return tableBody;
    }

    protected LinearLayout getPagination() {
        return pagination;
    }

    public static abstract class TableListAdapter<DTO, ViewHolder extends RecyclerView.ViewHolder, Adapter extends RecyclerView.Adapter<ViewHolder>> extends RecyclerView.Adapter<ViewHolder> {
        private TableListBase<DTO, ViewHolder, Adapter> tableListBase;
        private boolean hasStableIds = true;
        public TableListAdapter(){
            super();
        }

        protected abstract HerbLinkerJsonGetter getGetter();

        protected abstract DTO makeDTO(JsonObject jsonObject) throws DataException;

        protected abstract void onBindViewHolder(@NonNull ViewHolder holder, int position, DTO dto);

        protected abstract void success(String responseTime, String resultCode, JsonObject json);

        protected abstract void error(boolean networkFail, boolean invalidJson, boolean invalidHLJson, String rawJson);

        protected abstract void complete();


        protected DTO filter(String what, DTO dto) {
            return dto;
        }

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

    private static class TableListGetterListener<DTO> implements OnGetterListener {
        private TableListBase<DTO, ?, ?> tableListBase;
        private Runnable after;

        private TableListGetterListener(TableListBase<DTO, ?, ?> tableListBase) {
            this(tableListBase, null);
        }
        private TableListGetterListener(TableListBase<DTO, ?, ?> tableListBase, Runnable after) {
            this.tableListBase = tableListBase;
            this.after = after;
        }

        @Override
        public void onSuccess(String responseTime, String resultCode, JsonObject json) {
            if (tableListBase.repaint(responseTime, resultCode, json))
                tableListBase.success(responseTime, resultCode, json);
            else
                tableListBase.error(false, false, false, json.toString());
        }

        @Override
        public void onError(boolean networkFail, boolean invalidJson, boolean invalidHLJson, String rawJson) {
            tableListBase.error(networkFail, invalidJson, invalidHLJson, rawJson);
        }

        @Override
        public boolean onComplete(int code) {
            tableListBase.complete();
            if(after!=null)
                after.run();
            return false;
        }
    }
}
