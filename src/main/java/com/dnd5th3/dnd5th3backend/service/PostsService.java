package com.dnd5th3.dnd5th3backend.service;

import com.dnd5th3.dnd5th3backend.controller.dto.post.*;
import com.dnd5th3.dnd5th3backend.domain.member.Member;
import com.dnd5th3.dnd5th3backend.domain.posts.Posts;
import com.dnd5th3.dnd5th3backend.domain.vote.VoteType;
import com.dnd5th3.dnd5th3backend.domain.vote.vo.VoteRatioVo;
import com.dnd5th3.dnd5th3backend.exception.NoAuthorizationException;
import com.dnd5th3.dnd5th3backend.repository.posts.PostsRepository;
import com.dnd5th3.dnd5th3backend.utils.RandomNumber;
import com.dnd5th3.dnd5th3backend.utils.S3Uploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class PostsService {

    private final VoteService voteService;
    private final PostsRepository postsRepository;
    private final S3Uploader s3Uploader;

    public IdResponseDto savePost(PostRequestDto requestDto, Member member) throws IOException {
        String productImageUrl = s3Uploader.upload(requestDto.getFile(), S3Uploader.DIR_NAME);
        Posts savedPost = postsRepository.save(requestDto.toEntity(member, productImageUrl));
        return IdResponseDto.builder().id(savedPost.getId()).build();
    }

    public PostResponseDto getDetailPost(Long id, Member member) {
        Posts foundPost = postsRepository.findPostsById(id);
        foundPost.updateVoteStatusAndPostStatus();
        foundPost.increaseRankCount();
        VoteType currentMemberVoteType = voteService.getVoteType(member, foundPost);

        return PostResponseDto.builder()
                .id(foundPost.getId())
                .name(foundPost.getMember().getName())
                .title(foundPost.getTitle())
                .content(foundPost.getContent())
                .productImageUrl(foundPost.getProductImageUrl())
                .isVoted(foundPost.getIsVoted())
                .permitCount(foundPost.getPermitCount())
                .rejectCount(foundPost.getRejectCount())
                .createdDate(foundPost.getCreatedDate())
                .voteDeadline(foundPost.getVoteDeadline())
                .currentMemberVoteType(currentMemberVoteType)
                .build();
    }

    public IdResponseDto updatePost(Long id, PostRequestDto requestDto, Member member) throws IOException {
        Posts foundPost = postsRepository.findPostsById(id);
        if (foundPost.getMember().getId() != member.getId()) {
            throw new NoAuthorizationException("수정 권한 없음");
        }
        String productImageUrl = s3Uploader.upload(requestDto.getFile(), S3Uploader.DIR_NAME);
        foundPost.update(requestDto.getTitle(), requestDto.getContent(), productImageUrl);

        return IdResponseDto.builder().id(foundPost.getId()).build();
    }

    public IdResponseDto deletePost(Long id, Member member) {
        Posts foundPost = postsRepository.findPostsById(id);
        if (foundPost.getMember().getId() != member.getId()) {
            throw new NoAuthorizationException("삭제 권한 없음");
        }
        postsRepository.delete(foundPost);

        return IdResponseDto.builder().id(foundPost.getId()).build();
    }

    public AllPostResponseDto getPostListWithSortType(String sortType) {
        List<Posts> postsList = postsRepository.findPostsWithSortType(sortType);
        postsList.forEach(post -> post.updateVoteStatusAndPostStatus());
        List<PostsListDto> listDtos = PostsListDto.makePostsToListDtos(postsList);
        return AllPostResponseDto.builder().listDtos(listDtos).build();
    }

    public IdResponseDto saveVote(Long id, VoteRequestDto requestDto, Member member) {
        Posts foundPost = postsRepository.findPostsById(id);
        voteService.saveVote(member, foundPost, requestDto.getResult());
        return IdResponseDto.builder().id(foundPost.getId()).build();
    }

    public Map<String, Posts> getMainPosts() {
        Map<String, Posts> resultMap = new HashMap<>();
        //상위 50개 컨텐츠 추출
        List<Posts> top50RankedList = postsRepository.findPostsTop50Ranked();
        top50RankedList.stream().forEach(p -> {
            p.updateVoteStatusAndPostStatus();
        });
        //추출한 50개 중 반응(댓글)이 있는 것들만 필터링
        List<Posts> filterByCommentCount = new ArrayList<>();
        top50RankedList.stream().forEach(p -> {
            if (p.getComments().size() != 0) {
                filterByCommentCount.add(p);
            }
        });
        //최고의 반응글
        Posts bestResponsePost;
        if (filterByCommentCount.size() >= 2) {
            //필터링한 게시글들을 댓글수로 정렬
            filterByCommentCount.stream().sorted((p1, p2) -> p2.getComments().size() - p1.getComments().size());
            //게시글이 5개 이상이면 상위 5개 중 랜덤으로 추출
            bestResponsePost = filterByCommentCount.size() >= 5
                    ? filterByCommentCount.get(RandomNumber.startFromZeroTo(5))
                    : filterByCommentCount.get(RandomNumber.startFromZeroTo(filterByCommentCount.size()));
        } else if (filterByCommentCount.size() == 1) {
            bestResponsePost = filterByCommentCount.get(0);
        } else {
            bestResponsePost = null;
        }
        //투표비율 비교
        List<Posts> neckAndNeckPostList = new ArrayList<>();
        top50RankedList.stream().forEach(p -> {
            VoteRatioVo ratioVo = new VoteRatioVo(p);
            if ((ratioVo.getRejectRatio() != 0 && ratioVo.getPermitRatio() != 0) && Math.abs(ratioVo.getPermitRatio() - ratioVo.getRejectRatio()) <= 10) {
                neckAndNeckPostList.add(p);
            }
        });
        //막상막하 투표글
        Posts neckAndNeckPost = neckAndNeckPostList.size() == 0 ? null : neckAndNeckPostList.get(RandomNumber.startFromZeroTo(neckAndNeckPostList.size()));
        //불타고 있는글
        Posts hotPost = top50RankedList.get(RandomNumber.startFromZeroTo(50));
        //사랑 듬뿍 받은글
        Posts belovedPost = top50RankedList.get(RandomNumber.startFromZeroTo(50));
        //무물의 추천글
        Posts recommendPost = top50RankedList.get(RandomNumber.startFromZeroTo(50));

        //값이 있는 데이터만 전달
        if (bestResponsePost != null) {
            resultMap.put("bestResponsePost", bestResponsePost);
        }
        if (neckAndNeckPost != null) {
            resultMap.put("neckAndNeckPost", neckAndNeckPost);
        }
        resultMap.put("hotPost", hotPost);
        resultMap.put("belovedPost", belovedPost);
        resultMap.put("recommendPost", recommendPost);

        return resultMap;
    }
}
