package com.finrobotics.neyyasdk.core.exception;

import com.finrobotics.neyyasdk.error.NeyyaError;

/**
 * Created by zac on 09/10/15.
 */
public class SettingsCommandException extends Exception {

    private static final long serialVersionUID = 4664456874499611218L;
    private int error;

    public SettingsCommandException(String detailMessage, int error) {
        super(detailMessage);
        this.error = error;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " - " + NeyyaError.parseError(this.error);
    }

    public int getError() {
        return this.error;
    }

}
