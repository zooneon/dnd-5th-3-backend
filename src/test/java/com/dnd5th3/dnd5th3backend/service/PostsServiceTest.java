package com.dnd5th3.dnd5th3backend.service;

import com.dnd5th3.dnd5th3backend.domain.member.Member;
import com.dnd5th3.dnd5th3backend.domain.member.Role;
import com.dnd5th3.dnd5th3backend.domain.posts.Posts;
import com.dnd5th3.dnd5th3backend.repository.posts.PostsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostsServiceTest {

    @Mock
    private PostsRepository postsRepository;

    @InjectMocks
    private PostsService postsService;

    private Member member;
    private Posts post;

    @BeforeEach
    void setUp() {
        member = Member.builder().email("test@gmail.com").password("1234").role(Role.ROLE_USER).name("닉네임").build();
        post = Posts.builder()
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
    }

    @DisplayName("post 저장 테스트")
    @Test
    void savePostTest() throws Exception{
        //given
        when(postsRepository.save(any(Posts.class))).thenReturn(post);

        //when
        Posts savedPosts = postsService.savePost(post.getMember(), post.getTitle(), post.getContent(), post.getProductImageUrl());

        //then
        assertEquals(savedPosts.getId(), post.getId());
        assertEquals(savedPosts.getMember().getId(), post.getMember().getId());
    }

    @DisplayName("post 상세조회 테스트")
    @Test
    void findPostByIdTest() throws Exception{
        //given
        when(postsRepository.findById(1L)).thenReturn(Optional.of(post));

        //when
        Posts foundPost = postsService.findPostById(1L);

        //then
        assertEquals(post.getMember().getName(), foundPost.getMember().getName());
        assertEquals(post.getTitle(), foundPost.getTitle());
        assertEquals(post.getRankCount(), 1);
        assertEquals(false, post.getIsVoted());
        assertEquals(false, post.getIsPostsEnd());
    }

    @DisplayName("투표 종료 여부, 메인페이지 게시 조건 종료 여부 검사 테스트")
    @Test
    void updateVoteStatusAndPostStatus() throws Exception {
        //given
        Posts voteEnd = Posts.builder().rankCount(0).isVoted(false).voteDeadline(LocalDateTime.now().minusDays(1L)).build();
        Posts postEnd = Posts.builder().rankCount(0).isPostsEnd(false).postsDeadline(LocalDateTime.now().minusDays(1L)).build();
        given(postsRepository.findById(2L)).willReturn(Optional.of(voteEnd));
        given(postsRepository.findById(3L)).willReturn(Optional.of(postEnd));

        //when
        Posts voteEndPost = postsService.findPostById(2L);
        Posts postEndPost = postsService.findPostById(3L);

        //then
        assertEquals(true, voteEndPost.getIsVoted());
        assertEquals(true, postEndPost.getIsPostsEnd());
    }

    @DisplayName("post 수정 테스트")
    @Test
    void updatePostTest() throws Exception {
        //given
        String title = "update";
        String content = "update content";
        String productImageUrl = "update.jpg";
        when(postsRepository.findById(1L)).thenReturn(Optional.of(post));

        //when
        Posts updatedPost = postsService.updatePost(post.getId(), title, content, productImageUrl);

        //then
        assertEquals(updatedPost.getTitle(), title);
        assertEquals(updatedPost.getContent(), content);
        assertEquals(updatedPost.getProductImageUrl(), productImageUrl);
    }

    @DisplayName("post 삭제 테스트")
    @Test
    void deletePostTest() throws Exception {
        //given
        when(postsRepository.findById(1L)).thenReturn(Optional.of(post));

        //when
        postsService.deletePost(post.getId(), member);

        //then
        verify(postsRepository, times(1)).delete(eq(post));
    }

    @DisplayName("post 리스트 조회 테스트")
    @Test
    void findPostsListTest() throws Exception {
        //given
        Posts posts1 = Posts.builder()
                .id(1L)
                .member(member)
                .title("test1")
                .content("test content1")
                .productImageUrl("test1.jpg")
                .isVoted(false)
                .isPostsEnd(false)
                .permitCount(0)
                .rejectCount(0)
                .rankCount(15)
                .voteDeadline(LocalDateTime.now().plusDays(1L))
                .postsDeadline(LocalDateTime.now().plusDays(7L))
                .build();
        Posts posts2 = Posts.builder()
                .id(2L)
                .member(member)
                .title("test2")
                .content("test content2")
                .productImageUrl("test2.jpg")
                .isVoted(false)
                .isPostsEnd(false)
                .permitCount(0)
                .rejectCount(0)
                .rankCount(10)
                .voteDeadline(LocalDateTime.now().plusDays(1L))
                .postsDeadline(LocalDateTime.now().plusDays(7L))
                .build();

        List<Posts> rankCountList = new ArrayList<>();
        rankCountList.add(posts1);
        rankCountList.add(posts2);

        List<Posts> createdDateList = new ArrayList<>();
        createdDateList.add(posts2);
        createdDateList.add(posts1);

        Posts posts3 = Posts.builder()
                .id(3L)
                .member(member)
                .title("test3")
                .content("test content3")
                .productImageUrl("test3.jpg")
                .isVoted(true)
                .isPostsEnd(false)
                .permitCount(0)
                .rejectCount(0)
                .rankCount(20)
                .voteDeadline(LocalDateTime.now().plusDays(1L))
                .postsDeadline(LocalDateTime.now().plusDays(7L))
                .build();
        Posts posts4 = Posts.builder()
                .id(4L)
                .member(member)
                .title("test4")
                .content("test content4")
                .productImageUrl("test4.jpg")
                .isVoted(true)
                .isPostsEnd(false)
                .permitCount(0)
                .rejectCount(0)
                .rankCount(25)
                .voteDeadline(LocalDateTime.now().plusDays(1L))
                .postsDeadline(LocalDateTime.now().plusDays(7L))
                .build();

        List<Posts> alreadyDoneList = new ArrayList<>();
        alreadyDoneList.add(posts3);
        alreadyDoneList.add(posts4);

        List<Posts> almostDoneList = new ArrayList<>();
        almostDoneList.add(posts1);
        alreadyDoneList.add(posts2);

        when(postsRepository.findPostsOrderByRankCount()).thenReturn(rankCountList);
        when(postsRepository.findPostsOrderByCreatedDate()).thenReturn(createdDateList);
        when(postsRepository.findPostsOrderByAlreadyDone()).thenReturn(alreadyDoneList);
        when(postsRepository.findPostsOrderByAlmostDone()).thenReturn(almostDoneList);

        //when
        List<Posts> orderByRankCountList = postsService.findAllPosts("rank-count");
        List<Posts> orderByCreatedDateList = postsService.findAllPosts("created-date");
        List<Posts> orderByAlreadyDoneList = postsService.findAllPosts("already-done");
        List<Posts> orderByAlmostDoneList = postsService.findAllPosts("almost-done");

        //then
        assertEquals(orderByRankCountList.get(0).getId(), posts1.getId());
        assertEquals(orderByCreatedDateList.get(0).getId(), posts2.getId());
        assertEquals(orderByAlreadyDoneList.get(0).getId(), posts3.getId());
        assertEquals(orderByAlmostDoneList.get(0).getId(), posts1.getId());
    }
}