package com.nyeonjae.meltube.vos;

import com.nyeonjae.meltube.results.Result;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
                        // result나 result의 상속 관계만 허용함
public class ResultVo<TResult extends Result, TPayload> {
    // 가상의 타입을 만들어 주는것임
    private TResult result;
    private TPayload payload;

}
