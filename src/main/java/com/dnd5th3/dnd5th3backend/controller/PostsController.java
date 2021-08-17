package com.dnd5th3.dnd5th3backend.controller;

import com.dnd5th3.dnd5th3backend.controller.dto.post.*;
import com.dnd5th3.dnd5th3backend.domain.member.Member;
import com.dnd5th3.dnd5th3backend.domain.posts.Posts;
import com.dnd5th3.dnd5th3backend.domain.vo.VoteRatioVo;
import com.dnd5th3.dnd5th3backend.domain.vote.Vote;
import com.dnd5th3.dnd5th3backend.domain.vote.VoteType;
import com.dnd5th3.dnd5th3backend.service.PostsService;
import com.dnd5th3.dnd5th3backend.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
public class PostsController {

    private final PostsService postsService;
    private final VoteService voteService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/v1/posts")
    public IdResponseDto savePost(@RequestBody SaveRequestDto postSaveRequestDto, @AuthenticationPrincipal Member member) {
        Posts savedPosts = postsService.savePost(member, postSaveRequestDto.getTitle(), postSaveRequestDto.getContent(), postSaveRequestDto.getProductImageUrl());

        return IdResponseDto.builder().id(savedPosts.getId()).build();
    }

    @GetMapping("/api/v1/posts/{id}")
    public PostResponseDto findPostById(@PathVariable(name = "id") Long id, @AuthenticationPrincipal Member member) {
        Posts foundPost = postsService.findPostById(id);
        Vote voteResult = voteService.getVoteResult(member, foundPost);
        VoteType currentMemberVoteResult = voteResult == null ? VoteType.NO_RESULT : voteResult.getResult();
        String productImageUrl = foundPost.getProductImageUrl() == null ? "" : foundPost.getProductImageUrl();

        return PostResponseDto.builder()
                .id(foundPost.getId())
                .name(foundPost.getMember().getName())
                .title(foundPost.getTitle())
                .content(foundPost.getContent())
                .productImageUrl(productImageUrl)
                .isVoted(foundPost.getIsVoted())
                .permitCount(foundPost.getPermitCount())
                .rejectCount(foundPost.getRejectCount())
                .createdDate(foundPost.getCreatedDate())
                .voteDeadline(foundPost.getVoteDeadline())
                .currentMemberVoteResult(currentMemberVoteResult)
                .build();
    }

    @PutMapping("/api/v1/posts/{id}")
    public IdResponseDto updatePost(@PathVariable(name = "id") Long id, @RequestBody UpdateRequestDto updateRequestDto) {
        Posts updatedPost = postsService.updatePost(id, updateRequestDto.getTitle(), updateRequestDto.getContent(), updateRequestDto.getProductImageUrl());
        return IdResponseDto.builder().id(updatedPost.getId()).build();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/api/v1/posts/{id}")
    public IdResponseDto deletePost(@PathVariable(name = "id") Long id) {
        postsService.deletePost(id);
        return IdResponseDto.builder().id(id).build();
    }

    @GetMapping("/api/v1/posts")
    public AllResponseDto findPostsList(@RequestParam String sorted) {
        List<Posts> postsList = postsService.findAllPosts(sorted);
        List<PostsListDto> dtoList = postsList.stream().map(p -> {
            VoteRatioVo ratioVo = new VoteRatioVo(p);
            String productImageUrl = p.getProductImageUrl() == null ? "" : p.getProductImageUrl();
            return PostsListDto.builder()
                    .id(p.getId())
                    .name(p.getMember().getName())
                    .title(p.getTitle())
                    .productImageUrl(productImageUrl)
                    .isVoted(p.getIsVoted())
                    .permitRatio(ratioVo.getPermitRatio())
                    .rejectRatio(ratioVo.getRejectRatio())
                    .createdDate(p.getCreatedDate())
                    .voteDeadline(p.getVoteDeadline())
                    .build();
        }).collect(Collectors.toList());

        return new AllResponseDto(dtoList);
    }

    @PostMapping("/api/v1/posts/{id}/vote")
    public IdResponseDto votePost(@PathVariable(name = "id") Long id, @AuthenticationPrincipal Member member, @RequestBody VoteRequestDto requestDto) {
        Posts posts = postsService.findPostById(id);
        voteService.saveVote(member, posts, requestDto.getResult());

        return IdResponseDto.builder().id(posts.getId()).build();
    }

    @GetMapping("/api/v1/posts/main")
    public List<Map.Entry<String, MainPostDto>> mainPosts() {
        Map<String, Posts> mainPostsMap = postsService.findMainPosts();
        Map<String, MainPostDto> resultMap = new HashMap<>();
        List<Map.Entry<String, MainPostDto>> resultList = new ArrayList<>();
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

        for (Map.Entry<String, MainPostDto> resultMapEntry : resultMap.entrySet()) {
            resultList.add(resultMapEntry);
        }

        return resultList;
    }
}
