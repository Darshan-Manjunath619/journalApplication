export type AuthResponse = {
  accessToken: string
  tokenType: 'Bearer'
  expiresIn: number
}

export type LoginRequest = {
  userName: string
  password: string
}

export type RegisterRequest = {
  userName: string
  email: string
  password: string
  sentimentAnalysis: boolean
}

export type CurrentUser = {
  id: number
  userName: string
  email: string
  sentimentAnalysis: boolean
  roles: string[]
}

export type AuthStatus = 'loading' | 'authenticated' | 'unauthenticated' | 'error'
