package com.example.autoteach;

public interface Listener {
    void onSuccess(String result);
    void onFailure(String errorMessage);
}
