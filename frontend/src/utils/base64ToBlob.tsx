// 인코딩 문자 => Blob 객체 변환
const base64ToBlob = (base64: string, mimeType: string) => {
  const byteCharacters = atob(base64)
  const byteNumbers = new Array(byteCharacters.length)

  for (let i = 0; i < byteCharacters.length; i++) {
    byteNumbers[i] = byteCharacters.charCodeAt(i)
  }
  const byteArray = new Uint8Array(byteNumbers)

  return new Blob([byteArray], { type: mimeType })
}

export default base64ToBlob
