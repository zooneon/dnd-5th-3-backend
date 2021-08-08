package com.dnd5th3.dnd5th3backend.domain.posts;

import com.dnd5th3.dnd5th3backend.domain.common.BaseTime;
import com.dnd5th3.dnd5th3backend.domain.member.Member;
import com.dnd5th3.dnd5th3backend.domain.vote.VoteType;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
public class Posts extends BaseTime {

    @Id
    @Column(name = "post_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @NotNull
    private String title;

    @NotNull
    private String productName;

    @NotNull
    private String content;

    private String productImageUrl;

    @NotNull
    private Boolean isVoted;

    @NotNull
    private Integer permitCount;

    @NotNull
    private Integer rejectCount;

    @NotNull
    private Integer rankCount;

    @NotNull
    private Boolean isDeleted;

    @NotNull
    private LocalDateTime voteDeadline;

    public void update(String title, String productName, String content, String productImageUrl) {
        if (title != null) {
            this.title = title;
        }
        if (productName != null) {
            this.productName = productName;
        }
        if (content != null) {
            this.content = content;
        }
        this.productImageUrl = productImageUrl;
    }

    public void increaseRankCount() {
        this.rankCount += 1;
    }

    public void makeVotedStatusTrue() {
        this.isVoted = true;
    }

    public void makeDeletedStatusTrue() {
        this.isDeleted = true;
    }

    public void increaseVoteCount(VoteType result) {
        if (result.equals(VoteType.PERMIT)) {
            this.permitCount += 1;
        } else if (result.equals(VoteType.REJECT)) {
            this.rejectCount += 1;
        }
    }
}
