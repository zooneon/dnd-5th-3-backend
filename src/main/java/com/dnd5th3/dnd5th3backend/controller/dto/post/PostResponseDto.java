package com.dnd5th3.dnd5th3backend.controller.dto.post;

import com.dnd5th3.dnd5th3backend.domain.vote.VoteType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class PostResponseDto {

    private Long id;
    private String name;
    private String title;
    private String content;
    private String productImageUrl;
    private Boolean isVoted;
    private Integer permitCount;
    private Integer rejectCount;
    private LocalDateTime createdDate;
    private LocalDateTime voteDeadline;
    private VoteType currentMemberVoteType;
}
