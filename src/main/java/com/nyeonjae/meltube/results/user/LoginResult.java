package com.nyeonjae.meltube.results.user;


import com.nyeonjae.meltube.results.Result;

import java.io.Serializable;

public enum LoginResult implements Result {

    FAILURE_NOT_VERIFIED,
    FAILURE_SUSPENDED,
}
