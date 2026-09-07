import { z } from 'zod'
import { emailSchema, passwordSchema, usernameSchema } from '../../validation/commonSchemas'

export const loginSchema = z.object({
  userName: usernameSchema,
  password: passwordSchema,
})

export const registerSchema = z.object({
  userName: usernameSchema,
  email: emailSchema,
  password: passwordSchema,
  sentimentAnalysis: z.boolean(),
})

export type LoginValues = z.infer<typeof loginSchema>
export type RegisterValues = z.infer<typeof registerSchema>
