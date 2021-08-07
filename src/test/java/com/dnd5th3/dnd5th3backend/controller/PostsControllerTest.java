package com.dnd5th3.dnd5th3backend.controller;

import com.dnd5th3.dnd5th3backend.config.MockSecurityFilter;
import com.dnd5th3.dnd5th3backend.controller.dto.post.SaveRequestDto;
import com.dnd5th3.dnd5th3backend.controller.dto.post.UpdateRequestDto;
import com.dnd5th3.dnd5th3backend.domain.member.Member;
import com.dnd5th3.dnd5th3backend.domain.member.Role;
import com.dnd5th3.dnd5th3backend.domain.posts.Posts;
import com.dnd5th3.dnd5th3backend.domain.vote.Vote;
import com.dnd5th3.dnd5th3backend.domain.vote.VoteType;
import com.dnd5th3.dnd5th3backend.service.PostsService;
import com.dnd5th3.dnd5th3backend.service.VoteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.dnd5th3.dnd5th3backend.domain.posts.QPosts.posts;
import static com.dnd5th3.dnd5th3backend.utils.ApiDocumentUtils.getDocumentRequest;
import static com.dnd5th3.dnd5th3backend.utils.ApiDocumentUtils.getDocumentResponse;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureRestDocs
@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class PostsControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private WebApplicationContext context;
    @MockBean
    private PostsService postsService;
    @MockBean
    private VoteService voteService;
    @Autowired
    private ObjectMapper objectMapper;

    private Member member;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation))
                .apply(springSecurity(new MockSecurityFilter()))
                .build();
        member = Member.builder().email("test@gmail.com")
                .password("1234")
                .role(Role.ROLE_USER)
                .name("name")
                .build();
    }

    @DisplayName("post 생성 api 테스트")
    @Test
    public void savePostApiTest() throws Exception {
        //given
        SaveRequestDto requestDto = SaveRequestDto.builder()
                .title("test")
                .productName("test product")
                .content("test content")
                .productImageUrl("test.jpg")
                .build();
        Posts response = Posts.builder()
                .id(1L)
                .member(member)
                .title("test")
                .productName("test product")
                .content("test content")
                .productImageUrl("test.jpg")
                .build();

        given(postsService.savePost(member, requestDto.getTitle(), requestDto.getProductName(), requestDto.getContent(), requestDto.getProductImageUrl())).willReturn(response);

        //when
        ResultActions result = mvc.perform(RestDocumentationRequestBuilders.post("/api/v1/posts")
                .principal(new UsernamePasswordAuthenticationToken(member, null))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(requestDto))
        );

        //then
        result
                .andExpect(status().isCreated())
                .andDo(print())
                .andDo(document("posts/save",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("title").description("글 제목"),
                                fieldWithPath("productName").description("상품 이름"),
                                fieldWithPath("content").description("글 내용"),
                                fieldWithPath("productImageUrl").description("상품 이미지")
                        ),
                        responseFields(
                                fieldWithPath("id").description("게시글 id").type(Long.class)
                        )
                ))
                .andExpect(jsonPath("$.id").value(1L));
    }

    @DisplayName("post 상세조회 api 테스트")
    @Test
    public void findPostByIdApiTest() throws Exception {
        //given
        Posts post = Posts.builder()
                .id(1L)
                .member(member)
                .title("test")
                .productName("test product")
                .content("content")
                .productImageUrl("test.jpg")
                .isVoted(false)
                .permitCount(2)
                .rejectCount(8)
                .rankCount(100)
                .isDeleted(false)
                .voteDeadline(LocalDateTime.of(2021, 8, 2, 12, 0, 0))
                .build();
        post.setCreatedDate(LocalDateTime.of(2021, 8, 1, 12, 0, 0));
        Vote vote = Vote.builder().id(1L).member(member).posts(post).result(VoteType.NO_RESULT).build();

        given(postsService.findPostById(1L)).willReturn(post);
        given(voteService.getVoteResult(member, post)).willReturn(vote);

        //when
        ResultActions result = mvc.perform(RestDocumentationRequestBuilders.get("/api/v1/posts/{id}", 1L));

        //then
        result
                .andDo(print())
                .andDo(document("posts/getById",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(
                                parameterWithName("id").description("게시글 id")
                        ),
                        responseFields(
                                fieldWithPath("name").description("작성자 이름"),
                                fieldWithPath("title").description("글 제목"),
                                fieldWithPath("productName").description("상품 이름"),
                                fieldWithPath("content").description("글 내용"),
                                fieldWithPath("productImageUrl").description("상품 이미지"),
                                fieldWithPath("isVoted").description("투표 종료 여부"),
                                fieldWithPath("permitRatio").description("찬성 투표 비율"),
                                fieldWithPath("rejectRatio").description("반대 투표 비율"),
                                fieldWithPath("createdDate").description("작성된 시간"),
                                fieldWithPath("voteDeadline").description("투표 종료 시간"),
                                fieldWithPath("currentMemberVoteResult").description("현재 사용자의 투표 결과")
                        )
                ))
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.title").value("test"))
                .andExpect(jsonPath("$.productName").value("test product"))
                .andExpect(jsonPath("$.content").value("content"))
                .andExpect(jsonPath("$.productImageUrl").value("test.jpg"))
                .andExpect(jsonPath("$.isVoted").value(false))
                .andExpect(jsonPath("$.permitRatio").value(20L))
                .andExpect(jsonPath("$.rejectRatio").value(80L))
                .andExpect(jsonPath("$.createdDate").value("2021-08-01T12:00:00"))
                .andExpect(jsonPath("$.voteDeadline").value("2021-08-02T12:00:00"))
                .andExpect(jsonPath("$.currentMemberVoteResult").value("NO_RESULT"));
    }

    @DisplayName("post 수정 api 테스트")
    @Test
    public void updatePostApiTest() throws Exception {
        //given
        Posts response = Posts.builder()
                .id(1L)
                .member(member)
                .title("update")
                .productName("update product")
                .content("update content")
                .productImageUrl("update.jpg")
                .isVoted(false)
                .permitCount(36)
                .rejectCount(25)
                .rankCount(70)
                .isDeleted(false)
                .voteDeadline(LocalDateTime.of(2021, 8, 3, 12, 0, 0))
                .build();
        response.setCreatedDate(LocalDateTime.of(2021, 8, 2, 12, 0, 0));
        UpdateRequestDto requestDto = UpdateRequestDto.builder()
                .title("update")
                .productName("update product")
                .content("update content")
                .productImageUrl("update.jpg")
                .build();

        given(postsService.updatePost(1L, requestDto.getTitle(), requestDto.getProductName(), requestDto.getContent(), requestDto.getProductImageUrl())).willReturn(response);

        //when
        ResultActions result = mvc.perform(RestDocumentationRequestBuilders.post("/api/v1/posts/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(requestDto))
        );

        //then
        result
                .andDo(print())
                .andDo(document("posts/update",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(
                                parameterWithName("id").description("게시글 id")
                        ),
                        requestFields(
                                fieldWithPath("title").description("수정할 제목"),
                                fieldWithPath("productName").description("수정할 상품 이름"),
                                fieldWithPath("content").description("수정할 내용"),
                                fieldWithPath("productImageUrl").description("수정할 상품 이미지")
                        ),
                        responseFields(
                                fieldWithPath("name").description("작성자 이름"),
                                fieldWithPath("title").description("수정된 제목"),
                                fieldWithPath("productName").description("수정된 상품 이름"),
                                fieldWithPath("content").description("수정된 내용"),
                                fieldWithPath("productImageUrl").description("수정된 상품 이미지"),
                                fieldWithPath("isVoted").description("투표 종료 여부"),
                                fieldWithPath("permitRatio").description("찬성 투표 비율"),
                                fieldWithPath("rejectRatio").description("반대 투표 비율"),
                                fieldWithPath("createdDate").description("작성된 시간"),
                                fieldWithPath("voteDeadline").description("투표 종료 시간")
                        )
                ))
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.title").value("update"))
                .andExpect(jsonPath("$.productName").value("update product"))
                .andExpect(jsonPath("$.content").value("update content"))
                .andExpect(jsonPath("$.productImageUrl").value("update.jpg"))
                .andExpect(jsonPath("$.isVoted").value(false))
                .andExpect(jsonPath("$.permitRatio").value(59L))
                .andExpect(jsonPath("$.rejectRatio").value(41L))
                .andExpect(jsonPath("$.createdDate").value("2021-08-02T12:00:00"))
                .andExpect(jsonPath("$.voteDeadline").value("2021-08-03T12:00:00"));
    }

    @DisplayName("post 삭제 api 테스트")
    @Test
    public void deletePostApiTest() throws Exception {
        //when
        ResultActions result  = mvc.perform(RestDocumentationRequestBuilders.delete("/api/v1/posts/{id}", 1L));

        //then
        result
                .andDo(print())
                .andDo(document("posts/delete",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(
                                parameterWithName("id").description("게시글 id")
                        ),
                        responseFields(
                                fieldWithPath("id").description("삭제된 게시글 id").type(Long.class)
                        )
                ))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("id").value(1L));
    }

    @DisplayName("post 리스트 조회 api 테스트")
    @Test
    public void findPostsListApiTest() throws Exception {
        //given
        Posts posts1 = Posts.builder()
                .id(1L)
                .member(member)
                .title("test1")
                .productImageUrl("test1.jpg")
                .isVoted(false)
                .rankCount(15)
                .permitCount(10)
                .rejectCount(25)
                .build();
        posts1.setCreatedDate(LocalDateTime.of(2021, 8, 4, 12, 0, 0));
        Posts posts2 = Posts.builder()
                .id(2L)
                .member(member)
                .title("test2")
                .productImageUrl("test2.jpg")
                .isVoted(false)
                .rankCount(10)
                .permitCount(30)
                .rejectCount(10)
                .build();
        posts2.setCreatedDate(LocalDateTime.of(2021, 8, 4, 15, 0, 0));

        List<Posts> orderByCreatedDateList = new ArrayList<>();
        orderByCreatedDateList.add(posts2);
        orderByCreatedDateList.add(posts1);

        given(postsService.findAllPosts("created-date", 0)).willReturn(orderByCreatedDateList);

        //when
        ResultActions rankCountResult = mvc.perform(RestDocumentationRequestBuilders.get("/api/v1/posts?sorted=rank-count&offset=0"));
        ResultActions createdDateResult = mvc.perform(RestDocumentationRequestBuilders.get("/api/v1/posts?sorted=created-date&offset=0"));
        ResultActions alreadyDoneResult = mvc.perform(RestDocumentationRequestBuilders.get("/api/v1/posts?sorted=already-done&offset=0"));
        ResultActions almostDoneResult = mvc.perform(RestDocumentationRequestBuilders.get("/api/v1/posts?sorted=almost-done&offset=0"));

        //then
        rankCountResult
                .andDo(document("posts/findAll/rankCount",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestParameters(
                                parameterWithName("sorted").description("정렬 방법"),
                                parameterWithName("offset").description("오프셋")
                        )
                ))
                .andExpect(status().isOk());

        createdDateResult
                .andDo(document("posts/findAll/createdDate",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestParameters(
                                parameterWithName("sorted").description("정렬 방법"),
                                parameterWithName("offset").description("오프셋")
                        ),
                        responseFields(
                                fieldWithPath("posts.[].name").description("작성자 이름"),
                                fieldWithPath("posts.[].title").description("글 제목"),
                                fieldWithPath("posts.[].productImageUrl").description("상품 이미지"),
                                fieldWithPath("posts.[].isVoted").description("투표 종료 여부"),
                                fieldWithPath("posts.[].permitRatio").description("찬성 투표 비율"),
                                fieldWithPath("posts.[].rejectRatio").description("반대 투표 비율"),
                                fieldWithPath("posts.[].createdDate").description("작성된 시간")
                        )
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts[0].name").value("name"))
                .andExpect(jsonPath("$.posts[0].title").value("test2"))
                .andExpect(jsonPath("$.posts[0].productImageUrl").value("test2.jpg"))
                .andExpect(jsonPath("$.posts[0].isVoted").value(false))
                .andExpect(jsonPath("$.posts[0].permitRatio").value(75L))
                .andExpect(jsonPath("$.posts[0].rejectRatio").value(25L))
                .andExpect(jsonPath("$.posts[0].createdDate").value("2021-08-04T15:00:00"))
                .andExpect(jsonPath("$.posts[1].name").value("name"))
                .andExpect(jsonPath("$.posts[1].title").value("test1"))
                .andExpect(jsonPath("$.posts[1].productImageUrl").value("test1.jpg"))
                .andExpect(jsonPath("$.posts[1].isVoted").value(false))
                .andExpect(jsonPath("$.posts[1].permitRatio").value(29L))
                .andExpect(jsonPath("$.posts[1].rejectRatio").value(71L))
                .andExpect(jsonPath("$.posts[1].createdDate").value("2021-08-04T12:00:00"));

        alreadyDoneResult
                .andDo(document("posts/findAll/alreadyDone",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestParameters(
                                parameterWithName("sorted").description("정렬 방법"),
                                parameterWithName("offset").description("오프셋")
                        )
                ))
                .andExpect(status().isOk());

        almostDoneResult
                .andDo(document("posts/findAll/almostDone",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestParameters(
                                parameterWithName("sorted").description("정렬 방법"),
                                parameterWithName("offset").description("오프셋")
                        )
                ))
                .andExpect(status().isOk());
    }
}