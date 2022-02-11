package com.herblinker.android.libraries.base.activity;

import com.herblinker.android.libraries.base.application.AbstractDataCenter;

public abstract class AbstractDataCenterNestActivity<DataCenter extends AbstractDataCenter> extends NestableActivity{

    public DataCenter getDataCenter(){
        return (DataCenter)getApplication();
    }
}
