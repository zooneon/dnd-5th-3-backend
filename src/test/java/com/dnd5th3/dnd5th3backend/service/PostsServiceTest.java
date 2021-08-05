package com.dnd5th3.dnd5th3backend.service;

import com.dnd5th3.dnd5th3backend.controller.dto.post.UpdateRequestDto;
import com.dnd5th3.dnd5th3backend.domain.member.Member;
import com.dnd5th3.dnd5th3backend.domain.member.Role;
import com.dnd5th3.dnd5th3backend.domain.posts.Posts;
import com.dnd5th3.dnd5th3backend.repository.PostsRepository;
import org.junit.jupiter.api.Assertions;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostsServiceTest {

    @Mock
    private PostsRepository postsRepository;

    @InjectMocks
    private PostsService postsService;

    private Member member;

    @BeforeEach
    public void setUp() {
        member = Member.builder().email("test@gmail.com").password("1234").role(Role.ROLE_USER).name("닉네임").build();
    }

    @DisplayName("post 저장 테스트")
    @Test
    public void savePostTest() throws Exception{
        //given
        Posts newPosts = Posts.builder()
                .member(member)
                .title("test")
                .productName("testProduct")
                .content("test content")
                .productImageUrl("test.jpg")
                .isVoted(false)
                .permitCount(0)
                .rejectCount(0)
                .viewCount(0)
                .isDeleted(false)
                .build();
        when(postsRepository.save(any(Posts.class))).thenReturn(newPosts);

        //when
        Posts savedPosts = postsService.savePost(newPosts.getMember(), newPosts.getTitle(), newPosts.getProductName(), newPosts.getContent(), newPosts.getProductImageUrl());

        //then
        Assertions.assertEquals(savedPosts.getId(), newPosts.getId());
        Assertions.assertEquals(savedPosts.getMember().getId(), newPosts.getMember().getId());
    }

    @DisplayName("post 상세조회 테스트")
    @Test
    public void findPostByIdTest() throws Exception{
        //given
        Posts post = Posts.builder()
                .member(member)
                .title("test")
                .productName("testProduct")
                .content("test content")
                .productImageUrl("test.jpg")
                .isVoted(false)
                .permitCount(0)
                .rejectCount(0)
                .viewCount(0)
                .isDeleted(false)
                .voteDeadline(LocalDateTime.now().plusDays(1L))
                .build();
        when(postsRepository.findById(1L)).thenReturn(Optional.of(post));

        //when
        Posts foundPost = postsService.findPostById(1L);

        //then
        Assertions.assertEquals(post.getMember().getName(), foundPost.getMember().getName());
        Assertions.assertEquals(post.getTitle(), foundPost.getTitle());
        Assertions.assertEquals(post.getViewCount(), 1);
    }

    @DisplayName("post 수정 테스트")
    @Test
    public void updatePostTest() throws Exception {
        //given
        Posts post = Posts.builder().id(1L).member(member).title("test").productName("testProduct").content("test content").build();
        UpdateRequestDto requestDto = UpdateRequestDto.builder().title("update").productName("update product").content("update contest").productImageUrl("update.jpg").build();
        when(postsRepository.findById(1L)).thenReturn(Optional.of(post));

        //when
        Posts updatedPost = postsService.updatePost(post.getId(), requestDto.getTitle(), requestDto.getProductName(), requestDto.getContent(), requestDto.getProductImageUrl());

        //then
        Assertions.assertEquals(updatedPost.getTitle(), requestDto.getTitle());
        Assertions.assertEquals(updatedPost.getContent(), requestDto.getContent());
        Assertions.assertEquals(updatedPost.getProductImageUrl(), requestDto.getProductImageUrl());
    }

    @DisplayName("post 삭제 테스트")
    @Test
    public void deletePostTest() throws Exception {
        //given
        Posts post = Posts.builder().id(1L).member(member).title("test").productName("testProduct").content("test content").build();
        when(postsRepository.findById(1L)).thenReturn(Optional.of(post));

        //when
        postsService.deletePost(post.getId());

        //then
        verify(postsRepository, times(1)).delete(eq(post));
    }

    @DisplayName("post 리스트 조회 테스트")
    @Test
    public void findPostsListTest() throws Exception {
        //given
        Posts posts1 = Posts.builder()
                .id(1L)
                .member(member)
                .title("test1")
                .productName("testProduct1")
                .content("test content1")
                .productImageUrl("test1.jpg")
                .isVoted(false)
                .permitCount(0)
                .rejectCount(0)
                .viewCount(15)
                .isDeleted(false)
                .voteDeadline(LocalDateTime.now().plusDays(1L))
                .build();
        Posts posts2 = Posts.builder()
                .id(2L)
                .member(member)
                .title("test2")
                .productName("testProduct2")
                .content("test content2")
                .productImageUrl("test2.jpg")
                .isVoted(false)
                .permitCount(0)
                .rejectCount(0)
                .viewCount(10)
                .isDeleted(false)
                .voteDeadline(LocalDateTime.now().plusDays(1L))
                .build();

        List<Posts> viewCountList = new ArrayList<>();
        viewCountList.add(posts1);
        viewCountList.add(posts2);

        List<Posts> createdDateList = new ArrayList<>();
        createdDateList.add(posts2);
        createdDateList.add(posts1);

        Posts posts3 = Posts.builder()
                .id(3L)
                .member(member)
                .title("test3")
                .productName("testProduct3")
                .content("test content3")
                .productImageUrl("test3.jpg")
                .isVoted(true)
                .permitCount(0)
                .rejectCount(0)
                .viewCount(20)
                .isDeleted(false)
                .voteDeadline(LocalDateTime.now().plusDays(1L))
                .build();
        Posts posts4 = Posts.builder()
                .id(4L)
                .member(member)
                .title("test4")
                .productName("testProduct4")
                .content("test content4")
                .productImageUrl("test4.jpg")
                .isVoted(true)
                .permitCount(0)
                .rejectCount(0)
                .viewCount(25)
                .isDeleted(false)
                .voteDeadline(LocalDateTime.now().plusDays(1L))
                .build();

        List<Posts> alreadyDoneList = new ArrayList<>();
        alreadyDoneList.add(posts3);
        alreadyDoneList.add(posts4);

        List<Posts> almostDoneList = new ArrayList<>();
        almostDoneList.add(posts1);
        alreadyDoneList.add(posts2);

        when(postsRepository.findPostsOrderByViewCount(0)).thenReturn(viewCountList);
        when(postsRepository.findPostsOrderByCreatedDate(0)).thenReturn(createdDateList);
        when(postsRepository.findPostsOrderByAlreadyDone(0)).thenReturn(alreadyDoneList);
        when(postsRepository.findPostsOrderByAlmostDone(0)).thenReturn(almostDoneList);

        //when
        List<Posts> orderByViewCountList = postsService.findAllPosts("view-count", 0);
        List<Posts> orderByCreatedDateList = postsService.findAllPosts("created-date", 0);
        List<Posts> orderByAlreadyDoneList = postsService.findAllPosts("already-done", 0);
        List<Posts> orderByAlmostDoneList = postsService.findAllPosts("almost-done", 0);

        //then
        Assertions.assertEquals(orderByViewCountList.get(0).getId(), posts1.getId());
        Assertions.assertEquals(orderByCreatedDateList.get(0).getId(), posts2.getId());
        Assertions.assertEquals(orderByAlreadyDoneList.get(0).getId(), posts3.getId());
        Assertions.assertEquals(orderByAlmostDoneList.get(0).getId(), posts1.getId());
    }

}