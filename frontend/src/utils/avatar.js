import { fullAssetUrl } from '../api/http'

export const DEFAULT_AVATAR = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

export function resolveAvatarSrc(avatarUrl) {
  return avatarUrl ? fullAssetUrl(avatarUrl) : DEFAULT_AVATAR
}

export function onAvatarError(event) {
  if (event?.target) {
    event.target.src = DEFAULT_AVATAR
  }
}
