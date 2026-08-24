import { z } from 'zod'
import { emailSchema, passwordSchema } from '../../validation/commonSchemas'

export const profileSchema = z.object({
  email: emailSchema,
  sentimentAnalysis: z.boolean(),
})

export const changePasswordSchema = z.object({
  currentPassword: passwordSchema,
  newPassword: passwordSchema,
  confirmPassword: z.string(),
})
  .refine((values) => values.newPassword !== values.currentPassword, {
    path: ['newPassword'],
    message: 'New password must be different from the current password',
  })
  .refine((values) => values.newPassword === values.confirmPassword, {
    path: ['confirmPassword'],
    message: 'Passwords do not match',
  })

export type ProfileValues = z.infer<typeof profileSchema>
export type ChangePasswordValues = z.infer<typeof changePasswordSchema>
