package com.dnd5th3.dnd5th3backend.domain.vote.vo;

import com.dnd5th3.dnd5th3backend.domain.posts.Posts;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Objects;

@Getter
@EqualsAndHashCode
public class VoteRatioVo {

    private final Long permitRatio;
    private final Long rejectRatio;

    public VoteRatioVo(Posts posts) {
        Integer permitCount = posts.getPermitCount();
        Integer rejectCount = posts.getRejectCount();
        Long permitRatio = Math.round(((double) permitCount / (permitCount + rejectCount)) * 100);
        Long rejectRatio = Math.round(((double) rejectCount / (permitCount + rejectCount)) * 100);

        this.permitRatio = permitRatio;
        this.rejectRatio = rejectRatio;
    }
}
