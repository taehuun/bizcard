/** @jsxImportSource @emotion/react */
import { useState } from 'react'
import type { CardType } from '@/types/cardType'
import Flex from '@/components/shared/Flex'
import Text from '@/components/shared/Text'
import { css } from '@emotion/react'
import Spacing from '@/components/shared/Spacing'
import { tokens } from '@fluentui/react-components'
import {
  CheckmarkCircle24Regular,
  Circle24Regular,
  Star24Regular,
  Star24Filled,
  ShareAndroid24Filled,
  Delete24Filled,
} from '@fluentui/react-icons'
import { useNavigate } from 'react-router-dom'
import { useRecoilValue } from 'recoil'
import { userState } from '@/stores/user'
import { ExternalCardType } from '@/types/ExternalCard'
import EmptyThumbnail from '@/components/shared/EmptyThumbnail'
import { useDeleteAlbumCard } from '@/hooks/useDeleteAlbumCard'
import SmallModal from '@/components/shared/SmallModal'

interface CardThumbnailProps {
  cardInfo: CardType | ExternalCardType
  onSelect: (cardId: number) => void
  selectedCards: number[]
  forShare?: boolean
  scale?: number
  teamId? : number
}

const CardThumbnail = ({
  teamId,
  cardInfo,
  onSelect,
  selectedCards,
  forShare = false,
  scale = 1,
}: CardThumbnailProps) => {
  const [isfavorite, setIsFavorite] = useState(false)
  const [isChecked, setIsChecked] = useState(false)
  const isSelected = selectedCards.includes(cardInfo.cardId)
  const userId = useRecoilValue(userState).userId 
  // const deleteMutation = useDeleteMyAlbumMutation(selectedCards)
  console.log(teamId,userId,'teamId')
  const deletemutation = useDeleteAlbumCard()
  

  
  
  const handleCheck = (event: React.MouseEvent) => {
    event.stopPropagation()
    setIsChecked(!isChecked)
    onSelect(cardInfo.cardId)
  }

  const handleFavorite = (event: React.MouseEvent) => {
    event.stopPropagation()

    setIsFavorite(!isfavorite)
    /*  api 호출 */
    console.log('즐겨찾기 : ', cardInfo)
  }

  const handleShare = (event: React.MouseEvent) => {
    event.stopPropagation()
    console.log('공유 : ', cardInfo)
  }

  const handleDelete = (event: React.MouseEvent) => {
    event.stopPropagation()
    /*  api 호출 */
    deletemutation.mutate(cardInfo.cardId)
    console.log('삭제 : ', cardInfo.cardId)
  }
  console.log(cardInfo)
  const navigate = useNavigate()
  return (
    <div
      css={cardContainer(forShare, scale)}
      onClick={() => {
        if (forShare) {
          // 공유하기 화면 이하에서는 명함 썸네일 전체가 선택여부가 되도록
          setIsChecked(!isChecked)
          onSelect(cardInfo.cardId)
        } else {
          console.log(cardInfo, '님의 명함')
          navigate(`/myAlbum/${userId}/${cardInfo.cardId}`, {
            state: { cardInfo },
          })
        }
      }}
    >
      {isSelected ? (
        <CheckmarkCircle24Regular onClick={handleCheck} />
      ) : (
        <Circle24Regular onClick={handleCheck} />
      )}
      <Flex direction="row" justify="space-around" align="center" >
        <Flex direction="column" justify="center" align="center" css= {infoCss}>
          <Text typography="t7" bold={true}>
            {cardInfo.name.length > 3 &&
            /[\u3131-\u314e|\u314f-\u3163|\uac00-\ud7a3]/g.test(cardInfo.name)
              ? `${cardInfo.name.slice(0, 3)}...`
              : cardInfo.name.length > 6
              ? `${cardInfo.name.slice(0, 6)}...`
              : cardInfo.name.padEnd(6, ' ')}
          </Text>
          <Text typography="t9" css={infoContent} >{`${cardInfo.department} / ${cardInfo.position}`}</Text>
          <Text typography="t9" css={infoContent}>{cardInfo.company}</Text>
        </Flex>

        {cardInfo.realPicture || cardInfo.digitalPicture ? (
          <img
            src={
              cardInfo.realPicture
                ? cardInfo.realPicture
                : cardInfo.digitalPicture
            }
            alt="card"
          />
        ) : (
          <div>
            <EmptyThumbnail />
          </div>
        )}
        {!forShare && (
          <Flex direction="column" justify="space-around" align="center">
            {isfavorite ? (
              <Star24Filled css={iconCss} onClick={handleFavorite} />
            ) : (
              <Star24Regular css={i} onClick={handleFavorite} />
            )}
            <ShareAndroid24Filled css={i} onClick={handleShare} />
            <Delete24Filled css={i} onClick={handleDelete} />
            <SmallModal 
            icon={<Delete24Filled/>}
            dialogTitle='명함 삭제'
            dialogContent={`${cardInfo.name}님의 명함을 삭제하시겠습니까?`}
            onClick={() => handleDelete}
            actionButtonText='삭제'
            />
          </Flex>
        )}
      </Flex>
      <Spacing size={10} direction="vertical" />
    </div>
  )
}

export default CardThumbnail

const cardContainer = (forShare: boolean, scale: number) => css`
  border-radius: 10px;
  width: 85%;
  min-height: 120px;
  margin-bottom: 3%;
  margin-top: 1%;
  background-color: ${tokens.colorNeutralBackground1Selected};
  min-height: 100px;
  scale: ${forShare ? scale : 1};
  /* padding: 10px; */

  &:hover,
  &:active,
  &.wave {
    animation: wave 1.2s ease forwards;
    background: linear-gradient(
      90deg,
      ${tokens.colorNeutralBackground1Selected} 0%,
      ${tokens.colorNeutralBackground4Hover} 100%
    );
    background-size: 200% 200%;
  }
  @keyframes wave {
    0% {
      background-position: 10% 50%;
    }
    100% {
      background-position: 80% 50%;
      background: ${tokens.colorNeutralBackground1Selected};
    }
  }
`

const iconCss = css`
  color: yellow;
  margin-bottom: 15px;
`
const i = css`
  margin-bottom: 15px;
`

const infoContent = css`
  overflow: hidden;
  max-width: 140px;
`


const infoCss = css`
  min-width: 132px;
  max-width: 160px;
  overflow: hidden;
`