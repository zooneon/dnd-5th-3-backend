package com.dnd5th3.dnd5th3backend.service;

import com.dnd5th3.dnd5th3backend.domain.member.Member;
import com.dnd5th3.dnd5th3backend.domain.member.Role;
import com.dnd5th3.dnd5th3backend.domain.posts.Posts;
import com.dnd5th3.dnd5th3backend.domain.vote.Vote;
import com.dnd5th3.dnd5th3backend.domain.vote.VoteType;
import com.dnd5th3.dnd5th3backend.repository.posts.PostsRepository;
import com.dnd5th3.dnd5th3backend.repository.vote.VoteRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoteServiceTest {

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private PostsRepository postsRepository;

    @InjectMocks
    private VoteService voteService;

    private Member member;
    private Posts posts;
    private Vote vote;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L)
                .email("test@gmail.com")
                .password("1234")
                .role(Role.ROLE_USER)
                .name("닉네임").
                build();
        posts = Posts.builder()
                .id(1L)
                .member(member)
                .title("test")
                .content("test content")
                .productImageUrl("test.jpg")
                .isVoted(false)
                .isPostsEnd(false)
                .permitCount(0)
                .rejectCount(0)
                .rankCount(0)
                .voteDeadline(LocalDateTime.now().plusDays(1L))
                .postsDeadline(LocalDateTime.now().plusDays(7L))
                .build();
        vote = Vote.builder()
                .member(member)
                .posts(posts)
                .result(VoteType.NO_RESULT)
                .build();
    }

    @DisplayName("vote 저장 테스트")
    @Test
    void saveVoteTest() throws Exception {
        //given
        given(postsRepository.findById(1L)).willReturn(Optional.of(posts));
        given(voteRepository.save(any(Vote.class))).willReturn(vote);

        //when
        Vote savedVote = voteService.saveVote(member, posts, VoteType.NO_RESULT);

        //then
        assertEquals(savedVote.getId(), vote.getId());
        assertEquals(savedVote.getMember(), vote.getMember());
        assertEquals(savedVote.getPosts(), vote.getPosts());
    }

    @DisplayName("투표 결과 조회 테스트")
    @Test
    void getVoteResultTest() throws Exception {
        //given
        given(voteRepository.findByMemberAndPosts(member, posts)).willReturn(vote);

        //when
        VoteType voteType = voteService.getVoteType(member, posts);

        //then
        assertEquals(voteType, vote.getResult());
    }
}