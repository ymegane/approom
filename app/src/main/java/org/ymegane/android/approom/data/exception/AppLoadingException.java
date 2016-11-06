package org.ymegane.android.approom.data.exception;

import org.ymegane.android.approom.domain.exception.ErrorBundle;

public class AppLoadingException implements ErrorBundle {
    @Override
    public Exception getException() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }
}
