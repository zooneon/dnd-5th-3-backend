package com.dnd5th3.dnd5th3backend.service;

import com.dnd5th3.dnd5th3backend.controller.dto.post.*;
import com.dnd5th3.dnd5th3backend.domain.member.Member;
import com.dnd5th3.dnd5th3backend.domain.member.Role;
import com.dnd5th3.dnd5th3backend.domain.posts.Posts;
import com.dnd5th3.dnd5th3backend.domain.vote.VoteType;
import com.dnd5th3.dnd5th3backend.repository.posts.PostsRepository;
import com.dnd5th3.dnd5th3backend.repository.vote.VoteRepository;
import com.dnd5th3.dnd5th3backend.utils.S3Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
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
    @Mock
    private VoteService voteService;

    @Mock
    private S3Uploader s3Uploader;

    @InjectMocks
    private PostsService postsService;

    private Member member;
    private Posts post;
    private LocalDateTime testDate;

    @BeforeEach
    void setUp() {
        testDate = LocalDateTime.of(2022, 2, 7, 12, 0, 0);
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
                .voteDeadline(testDate.plusDays(1L))
                .postsDeadline(testDate.plusDays(7L))
                .build();
    }

    @DisplayName("post 저장 테스트")
    @Test
    void savePostTest() throws Exception{
        //given
        MockMultipartFile file = new MockMultipartFile("test file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test.jpg".getBytes(StandardCharsets.UTF_8));
        SaveRequestDto saveRequestDto = new SaveRequestDto("test", "test content", file);
        given(s3Uploader.upload(file, "static")).willReturn("test.jpg");
        given(postsRepository.save(any(Posts.class))).willReturn(post);

        //when
        IdResponseDto responseDto = postsService.savePost(saveRequestDto, member);

        //then
        assertEquals(responseDto.getId(), post.getId());
    }

    @DisplayName("post 상세조회 테스트")
    @Test
    void getPostTest() {
        //given
        given(postsRepository.findPostsById(1L)).willReturn(post);
        given(voteService.getVoteType(member, post)).willReturn(VoteType.NO_RESULT);

        //when
        PostResponseDto responseDto = postsService.getPost(1L, member);

        //then
        assertEquals(responseDto.getId(), post.getId());
        assertEquals(responseDto.getTitle(), post.getTitle());
        assertEquals(post.getRankCount(), 1);
        assertEquals(false, post.getIsVoted());
        assertEquals(false, post.getIsPostsEnd());
    }

    @DisplayName("post 수정 테스트")
    @Test
    void updatePostTest() throws Exception {
        //given
        String title = "update";
        String content = "update content";
        String productImageUrl = "update.jpg";
        when(postsRepository.findPostsById(1L)).thenReturn(post);

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
        when(postsRepository.findPostsById(1L)).thenReturn(post);

        //when
        postsService.deletePost(post.getId(), member);

        //then
        verify(postsRepository, times(1)).delete(eq(post));
    }

    @DisplayName("정렬된 전체 게시물 조회 테스트")
    @Test
    void findAllPostsWithSortType() throws Exception {
        //given
        List<Posts> postsList = new ArrayList<>();
        postsList.add(post);
        given(postsRepository.findPostsWithSortType(SortType.RANK_COUNT.getValue())).willReturn(postsList);

        //when
        AllPostResponseDto responseDto = postsService.findAllPostsWithSortType(SortType.RANK_COUNT.getValue());

        //then
        assertEquals(responseDto.getListDtos().get(0).getId(), post.getId());
    }
}