package com.company.ui.jfx.editors;

/**
 * Created by Александр on 09.04.2017.
 */
public interface EditCallback<T> {

    void onFinish(T edited);

    void onCancel();
}
