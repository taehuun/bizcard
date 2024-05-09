package com.ssafy.businesscard.privateAlbum.service;

import com.ssafy.businesscard.mycard.entity.Businesscard;
import com.ssafy.businesscard.mycard.repository.BusinesscardRepository;
import com.ssafy.businesscard.privateAlbum.dto.FilterCardResponseDto;
import com.ssafy.businesscard.privateAlbum.dto.FilterListResponseDto;
import com.ssafy.businesscard.privateAlbum.dto.PrivateAlbumResponseDto;
import com.ssafy.businesscard.privateAlbum.entity.Filter;
import com.ssafy.businesscard.privateAlbum.entity.PrivateAlbum;
import com.ssafy.businesscard.privateAlbum.entity.PrivateAlbumMember;
import com.ssafy.businesscard.privateAlbum.mapper.PrivateAlbumMapper;
import com.ssafy.businesscard.privateAlbum.repository.FilterRepository;
import com.ssafy.businesscard.privateAlbum.repository.PrivateAlbumMemberRepository;
import com.ssafy.businesscard.privateAlbum.repository.PrivateAlbumRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrivateAlbumServiceImpl implements PrivateAlbumService {

    private final PrivateAlbumRepository privateAlbumRepository;
    private final BusinesscardRepository businesscardRepository;
    private final PrivateAlbumMemberRepository privateAlbumMemberRepository;
    private final FilterRepository filterRepository;
    private final PrivateAlbumMapper privateAlbumMapper;

    //명함 지갑 목록 조회
    @Override
    public List<PrivateAlbumResponseDto> getAlbumList(Long userId, int page){
        int size = 12;
        Pageable pageable = PageRequest.of(page, size, Sort.by("businesscard.cardId").descending());
        Page<PrivateAlbum> albumPage = privateAlbumRepository.findByUser_userId(userId, pageable);
        List<Businesscard> pages = albumPage.stream().map(card -> card.getBusinesscard()).toList();
        List<PrivateAlbumResponseDto> dtos = pages.stream().map(privateAlbumMapper::toDto).toList();
        return dtos;
    }

        //명함 상세 조회
        @Override
        public PrivateAlbumResponseDto getAlbumDtail(Long userId, Long cardId){
            Optional<PrivateAlbum> optionalPrivateAlbum = privateAlbumRepository.findByUser_userIdAndBusinesscard_cardId(userId, cardId);
            if (optionalPrivateAlbum.isPresent()) {
                PrivateAlbum privateAlbum = optionalPrivateAlbum.get();
                return PrivateAlbumResponseDto.builder()
                        .cardId(privateAlbum.getBusinesscard().getCardId())
                        .name(privateAlbum.getBusinesscard().getName())
                        .company(privateAlbum.getBusinesscard().getCompany())
                        .position(privateAlbum.getBusinesscard().getPosition())
                        .rank(privateAlbum.getBusinesscard().getRank())
                        .department(privateAlbum.getBusinesscard().getDepartment())
                        .email(privateAlbum.getBusinesscard().getEmail())
                        .landlineNumber(privateAlbum.getBusinesscard().getLandlineNumber())
                        .faxNumber(privateAlbum.getBusinesscard().getFaxNumber())
                        .phoneNumber(privateAlbum.getBusinesscard().getPhoneNumber())
                        .address(privateAlbum.getBusinesscard().getAddress())
                        .realPicture(privateAlbum.getBusinesscard().getRealPicture())
                        .frontBack(privateAlbum.getBusinesscard().getFrontBack())
                        .domainUrl(privateAlbum.getBusinesscard().getDomainUrl())
                    .memo(privateAlbum.getMemo())
                    .build();
        } else {
            throw new NoSuchElementException("카드가 없음");
        }
    }

    //필터 목록 조회
    @Override
    public List<FilterListResponseDto> getFilter(Long userId){
        List<PrivateAlbumMember> privateAlbumMembers = privateAlbumMemberRepository.findByUser_userId(userId);
        List<FilterListResponseDto> filterListResponseDtoList = privateAlbumMembers.stream()
                .map(privateAlbumMember -> new FilterListResponseDto(
                        privateAlbumMember.getFilter().getFilterId(),
                        privateAlbumMember.getFilter().getFilterName()
                ))
                .collect(Collectors.toList());

        return filterListResponseDtoList;
    }

    //필터 별 명함 조회
    @Override
    public FilterCardResponseDto getFilterCard(Long userId, Long filterId){
        //filterId와 userId가 일치하는 privateAlbumId를 찾고 privateAlbumId에 해당하는 카드 Id의 해당하는 카드들을 저장
        List<PrivateAlbumMember> members = privateAlbumMemberRepository.findByFilter_FilterIdAndUser_UserId(filterId, userId);

        List<PrivateAlbumResponseDto> cards = members.stream()
                .filter(member -> member.getPrivateAlbum() != null) // null뭐시기 에러떠서 추가
                .map(member -> member.getPrivateAlbum())
                .map(album -> album.getBusinesscard())
                .filter(bc -> bc != null) // null뭐시기 에러떠서 추가
                .map(bc -> PrivateAlbumResponseDto.builder()
                        .cardId(bc.getCardId())
                        .name(bc.getName())
                        .company(bc.getCompany())
                        .position(bc.getPosition())
                        .rank(bc.getRank())
                        .department(bc.getDepartment())
                        .email(bc.getEmail())
                        .landlineNumber(bc.getLandlineNumber())
                        .faxNumber(bc.getFaxNumber())
                        .phoneNumber(bc.getPhoneNumber())
                        .address(bc.getAddress())
                        .realPicture(bc.getRealPicture())
                        .frontBack(bc.getFrontBack())
                        .domainUrl(bc.getDomainUrl())
                        .build())
                .collect(Collectors.toList());

        return FilterCardResponseDto.builder()
                .filterId(filterId)
                .cardList(cards)
                .build();
    }

    //상세보기에서 명함마다 필터 뭐있는지 조회
    @Override
    @Transactional
    public List<FilterListResponseDto> getAlbumDtailFilter(Long userId, Long cardId){
        Optional<PrivateAlbum> optionalPrivateAlbum = privateAlbumRepository.findByUser_userIdAndBusinesscard_cardId(userId, cardId);
        if (optionalPrivateAlbum.isPresent()) {
            PrivateAlbum privateAlbum = optionalPrivateAlbum.get();

            // 카드에 태그된 필터 목록 조회
            List<PrivateAlbumMember> albumMembers = privateAlbum.getPrivateAlbumMemberList();
            List<FilterListResponseDto> dtos = new ArrayList<>();
            for (PrivateAlbumMember albumMember : albumMembers) {
                Filter filter = albumMember.getFilter();
                FilterListResponseDto dto = new FilterListResponseDto(filter.getFilterId(), filter.getFilterName());
                dtos.add(dto);
            }
            return dtos;
        } else {
            return Collections.emptyList();
        }
    }



    //엑셀로 내보내기용 명함지갑목록조회
    @Override
    public List<PrivateAlbumResponseDto> getAlbumAllList(Long userId){
        List<PrivateAlbum> privateAlbums = privateAlbumRepository.findByUser_userId(userId);
        List<Businesscard> businesscards = privateAlbums.stream().map(bc -> bc.getBusinesscard()).toList();
        List<PrivateAlbumResponseDto> dtos = businesscards.stream().map(privateAlbumMapper::toDto).toList();
        return dtos;
    }
}