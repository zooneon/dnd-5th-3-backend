package com.dnd5th3.dnd5th3backend.controller;

import com.dnd5th3.dnd5th3backend.controller.dto.post.*;
import com.dnd5th3.dnd5th3backend.domain.member.Member;
import com.dnd5th3.dnd5th3backend.domain.posts.Posts;
import com.dnd5th3.dnd5th3backend.domain.vote.vo.VoteRatioVo;
import com.dnd5th3.dnd5th3backend.service.PostsService;
import com.dnd5th3.dnd5th3backend.service.VoteService;
import com.dnd5th3.dnd5th3backend.utils.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
@RestController
public class PostsController {

    private final PostsService postsService;

    @PostMapping
    public ResponseEntity<IdResponseDto> saveAPI(PostRequestDto requestDto, @AuthenticationPrincipal Member member) throws IOException {
        IdResponseDto responseDto = postsService.savePost(requestDto, member);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDto> getDetailAPI(@PathVariable(name = "id") Long id, @AuthenticationPrincipal Member member) {
        PostResponseDto responseDto = postsService.getDetailPost(id, member);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @PostMapping("/{id}")
    public ResponseEntity<IdResponseDto> updateAPI(@PathVariable(name = "id") Long id,
                                                    PostRequestDto requestDto,
                                                    @AuthenticationPrincipal Member member
                                        ) throws IOException {
        IdResponseDto responseDto = postsService.updatePost(id, requestDto, member);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<IdResponseDto> deleteAPI(@PathVariable(name = "id") Long id, @AuthenticationPrincipal Member member) {
        IdResponseDto responseDto = postsService.deletePost(id, member);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @GetMapping
    public ResponseEntity<AllPostResponseDto> getListAPI(@RequestParam(name = "sorted") String sortType) {
        AllPostResponseDto responseDto = postsService.getPostListWithSortType(sortType);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

//    @PostMapping("/{id}/vote")
//    public ResponseEntity<IdResponseDto> saveVoteAPI(@PathVariable(name = "id") Long id, @AuthenticationPrincipal Member member, @RequestBody VoteRequestDto requestDto) {
//        Posts posts = postsService.findPostById(id);
//        voteService.saveVote(member, posts, requestDto.getResult());
//        IdResponseDto responseDto = IdResponseDto.builder().id(posts.getId()).build();
//
//        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
//    }

    @GetMapping("/main")
    public ResponseEntity<AllResponseDto> getMainPostsAPI() {
        Map<String, Posts> mainPostsMap = postsService.getMainPosts();
        Map<String, MainPostDto> resultMap = new HashMap<>();
        List<MainPostDto> resultList = new ArrayList<>();
        mainPostsMap.forEach((key, value) -> {
            VoteRatioVo ratioVo = new VoteRatioVo(value);
            String productImageUrl = value.getProductImageUrl() == null ? "" : value.getProductImageUrl();
            MainPostDto postDto = MainPostDto.builder()
                    .id(value.getId())
                    .name(value.getMember().getName())
                    .title(value.getTitle())
                    .productImageUrl(productImageUrl)
                    .isVoted(value.getIsVoted())
                    .permitRatio(ratioVo.getPermitRatio())
                    .rejectRatio(ratioVo.getRejectRatio())
                    .createdDate(value.getCreatedDate())
                    .voteDeadline(value.getVoteDeadline())
                    .build();
            resultMap.put(key, postDto);
        });

        if (resultMap.get("neckAndNeckPost") == null) {
            MainPostDto mock = MainPostDto.builder()
                    .id(-1L)
                    .name("no content")
                    .title("no content")
                    .productImageUrl("no content")
                    .isVoted(true)
                    .permitRatio(-99L)
                    .rejectRatio(-99L)
                    .createdDate(LocalDateTime.now())
                    .voteDeadline(LocalDateTime.now())
                    .build();
            resultMap.put("neckAndNeckPost", mock);
        }

        if (resultMap.get("bestResponsePost") == null) {
            MainPostDto mock = MainPostDto.builder()
                    .id(-1L)
                    .name("no content")
                    .title("no content")
                    .productImageUrl("no content")
                    .isVoted(true)
                    .permitRatio(-99L)
                    .rejectRatio(-99L)
                    .createdDate(LocalDateTime.now())
                    .voteDeadline(LocalDateTime.now())
                    .build();
            resultMap.put("bestResponsePost", mock);
        }

        for (MainPostDto value : resultMap.values()) {
            resultList.add(value);
        }
        AllResponseDto responseDto = AllResponseDto.builder().posts(resultList).build();

        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

}
