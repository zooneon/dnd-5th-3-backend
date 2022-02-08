package com.dnd5th3.dnd5th3backend.controller.dto.post;

import com.dnd5th3.dnd5th3backend.domain.member.Member;
import com.dnd5th3.dnd5th3backend.domain.posts.Posts;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PostRequestDto {

    private String title;
    private String content;
    private MultipartFile file;

    public Posts toEntity(Member member, String productImageUrl) {
        return Posts.builder()
                .member(member)
                .title(title)
                .content(content)
                .productImageUrl(productImageUrl)
                .isVoted(false)
                .isPostsEnd(false)
                .permitCount(0)
                .rejectCount(0)
                .rankCount(0)
                .voteDeadline(LocalDateTime.now().plusDays(1L))
                .postsDeadline(LocalDateTime.now().plusDays(7L))
                .build();
    }
}
