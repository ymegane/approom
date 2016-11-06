package org.ymegane.android.approom.domain.exception;

public interface ErrorBundle {
    Exception getException();

    String getErrorMessage();
}
