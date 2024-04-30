package com.ssafy.businesscard.domain.myalbum.service.impl;

import com.ssafy.businesscard.domain.card.entity.Filter;
import com.ssafy.businesscard.domain.myalbum.dto.request.CardAddFilterRequest;
import com.ssafy.businesscard.domain.myalbum.entity.PrivateAlbum;
import com.ssafy.businesscard.domain.myalbum.entity.PrivateAlbumMember;
import com.ssafy.businesscard.domain.myalbum.repository.PrivateAlbumFilterRepository;
import com.ssafy.businesscard.domain.myalbum.repository.PrivateAlbumMemberRepository;
import com.ssafy.businesscard.domain.myalbum.repository.PrivateAlbumRepository;
import com.ssafy.businesscard.domain.myalbum.service.PrivateAlbumService;
import com.ssafy.businesscard.domain.user.entity.User;
import com.ssafy.businesscard.domain.user.repository.UserRepository;
import com.ssafy.businesscard.global.exception.GlobalExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrivateAlbumServiceImpl implements PrivateAlbumService {

    private final UserRepository userRepository;
    private final PrivateAlbumFilterRepository privateAlbumFilterRepository;
    private final PrivateAlbumRepository privateAlbumRepository;
    private final PrivateAlbumMemberRepository privateAlbumMemberRepository;

    // 명함에 필터 추가
    @Override
    @Transactional
    public void addFilter(Long userId, Long cardId, List<CardAddFilterRequest> request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalExceptionHandler.UserException(
                        GlobalExceptionHandler.UserErrorCode.NOT_EXISTS_USER
                ));

        List<Long> filterIdList = new ArrayList<>();
        for (CardAddFilterRequest cardAddFilterRequest : request) {
            filterIdList.add(cardAddFilterRequest.filterId());
        }

        for (Long filterId : filterIdList) {
            Filter filter = privateAlbumFilterRepository.findById(filterId)
                            .orElseThrow(() -> new GlobalExceptionHandler.UserException(
                                    GlobalExceptionHandler.UserErrorCode.NOT_EXISTS_FILTER
                            ));
            PrivateAlbum privateAlbum = privateAlbumRepository.findByUser_userIdAndBusinesscard_CardId(userId, cardId);
            privateAlbumMemberRepository.save(PrivateAlbumMember.builder()
                            .filter(filter)
                            .user(user)
                            .privateAlbum(privateAlbum)
                    .build());
        }
    }
}
