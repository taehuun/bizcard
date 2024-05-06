package com.ssafy.businesscard.domain.myalbum.service.impl;

import com.ssafy.businesscard.domain.card.entity.Businesscard;
import com.ssafy.businesscard.domain.card.entity.Filter;
import com.ssafy.businesscard.domain.card.repository.BusinesscardRepository;
import com.ssafy.businesscard.domain.card.dto.request.CardAddFilterRequest;
import com.ssafy.businesscard.domain.card.dto.request.CardRequest;
import com.ssafy.businesscard.domain.card.dto.request.MemoRequest;
import com.ssafy.businesscard.domain.myalbum.dto.request.PrivateAlbumRequest;
import com.ssafy.businesscard.domain.myalbum.entity.PrivateAlbum;
import com.ssafy.businesscard.domain.myalbum.entity.PrivateAlbumMember;
import com.ssafy.businesscard.domain.card.mapper.BusinesscardMapper;
import com.ssafy.businesscard.domain.myalbum.repository.PrivateAlbumFilterRepository;
import com.ssafy.businesscard.domain.myalbum.repository.PrivateAlbumMemberRepository;
import com.ssafy.businesscard.domain.myalbum.repository.privateAlbum.PrivateAlbumRepository;
import com.ssafy.businesscard.domain.myalbum.service.PrivateAlbumService;
import com.ssafy.businesscard.domain.user.entity.User;
import com.ssafy.businesscard.domain.user.repository.UserRepository;
import com.ssafy.businesscard.global.exception.GlobalExceptionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrivateAlbumServiceImpl implements PrivateAlbumService {

    private final UserRepository userRepository;
    private final BusinesscardRepository businesscardRepository;
    private final BusinesscardMapper businesscardMapper;
    private final PrivateAlbumFilterRepository privateAlbumFilterRepository;
    private final PrivateAlbumRepository privateAlbumRepository;
    private final PrivateAlbumMemberRepository privateAlbumMemberRepository;

    // 명함 지갑에 명함 등록
    @Override
    @Transactional
    public String registCard(Long userId, CardRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new GlobalExceptionHandler.UserException(
                GlobalExceptionHandler.UserErrorCode.NOT_EXISTS_USER
        ));

        Businesscard businesscard = businesscardMapper.toEntity(request);

        if (isCardExist(userId, businesscard) ) {
            return "이미 등록된 명함입니다.";
        } else {
            businesscardRepository.save(businesscard);
            PrivateAlbumRequest privateAlbumRequest = PrivateAlbumRequest.builder()
                    .user(user)
                    .businesscard(businesscard)
                    .favorite(false)
                    .build();
            return addPrivateAlbum(privateAlbumRequest);
        }
    }

    // 명함 중복 검사
    private boolean isCardExist(Long userId, Businesscard businesscard) {
        List<PrivateAlbum> privateAlbumList = privateAlbumRepository.findAllByUser_userId(userId);
        for (PrivateAlbum privateAlbum : privateAlbumList) {
            if (privateAlbum.getBusinesscard().getEmail().equals(businesscard.getEmail())) {
                return true;
            }
        }
        return false;
    }

    // 명함 지갑에 명함 등록 로직
    private String addPrivateAlbum(PrivateAlbumRequest request) {
        privateAlbumRepository.save(PrivateAlbum.builder()
                .user(request.user())
                .businesscard(request.businesscard())
                .favorite(request.favorite())
                .build());
        return "명함이 등록되었습니다.";

    }

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
            PrivateAlbum privateAlbum = privateAlbumRepository.
                      findByUser_userIdAndBusinesscard_cardId(userId, cardId);
            privateAlbumMemberRepository.save(PrivateAlbumMember.builder()
                            .filter(filter)
                            .user(user)
                            .privateAlbum(privateAlbum)
                    .build());
        }
    }

    // 명함지갑 명함 수정
    @Override
    @Transactional
    public void updateCard(Long userId, Long cardId, CardRequest request) {
        PrivateAlbum privateAlbum = privateAlbumRepository.findByUser_userIdAndBusinesscard_cardId(userId, cardId);
        Businesscard card = privateAlbum.getBusinesscard();
        businesscardRepository.save(Businesscard.builder()
                .cardId(card.getCardId())
                .name(request.name())
                .company(request.company())
                .position(request.position())
                .rank(request.rank())
                .department(request.department())
                .email(request.email())
                .landlineNumber(request.landlineNumber())
                .faxNumber(request.faxNumber())
                .phoneNumber(request.phoneNumber())
                .address(request.address())
                .realPicture(request.realPicture())
                .frontBack(request.frontBack())
                .domainUrl(request.domainUrl())
                .build());

    }

    // 명함지갑 명함 삭제
    @Override
    @Transactional
    public void deleteCard(Long userId, Long cardId) {
        PrivateAlbum privateAlbum = privateAlbumRepository.findByUser_userIdAndBusinesscard_cardId(userId, cardId);

        List<PrivateAlbum> privateAlbumList = privateAlbumRepository.findByUser_userIdAndBusinesscard_email(
                privateAlbum.getUser().getUserId(), privateAlbum.getBusinesscard().getEmail());
        privateAlbumRepository.deleteAll(privateAlbumList);
    }

    // 명함에 메모 등록 및 수정
    @Override
    public String cardMemo(Long userId, Long cardId, MemoRequest request) {
        PrivateAlbum privateAlbum = privateAlbumRepository.findByUser_userIdAndBusinesscard_cardId(userId, cardId);
        log.info("[Memo] : {}", privateAlbum.getMemo());
        // 메모가 없다면 메모 등록
        if (privateAlbum.getMemo() == null) {
            privateAlbumRepository.save(PrivateAlbum.builder()
                            .privateAlbumId(privateAlbum.getPrivateAlbumId())
                            .businesscard(privateAlbum.getBusinesscard())
                            .user(privateAlbum.getUser())
                            .favorite(privateAlbum.isFavorite())
                            .memo(request.memo())
                    .build());
            return "메모가 등록되었습니다.";
        } else { // 메모가 있다면 메모 수정
            privateAlbumRepository.save(PrivateAlbum.builder()
                    .privateAlbumId(privateAlbum.getPrivateAlbumId())
                    .businesscard(privateAlbum.getBusinesscard())
                    .user(privateAlbum.getUser())
                    .favorite(privateAlbum.isFavorite())
                    .memo(request.memo())
                    .build());
          return "메모가 수정되었습니다.";
        }
    }
}
