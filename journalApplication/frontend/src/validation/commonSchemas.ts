import { z } from 'zod'

export const usernameSchema = z
  .string()
  .trim()
  .min(3, 'Username must contain at least 3 characters')
  .max(50, 'Username cannot exceed 50 characters')
  .regex(/^[A-Za-z0-9_]+$/, 'Username can contain only letters, numbers, and underscores')

export const emailSchema = z
  .string()
  .trim()
  .min(1, 'Email is required')
  .email('Enter a valid email address')
  .max(254, 'Email cannot exceed 254 characters')

export const passwordSchema = z
  .string()
  .min(8, 'Password must contain at least 8 characters')
  .max(72, 'Password cannot exceed 72 characters')
