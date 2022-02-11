package com.herblinker.android.libraries.base.application;

import android.content.ContextWrapper;

public abstract class Communication<Before extends ContextWrapper, After extends ContextWrapper> {
    public abstract void initialize(Before before, After after);
    public abstract void work(int type, Before before, After after);
    public abstract void finalize(Before before, After after);
}
