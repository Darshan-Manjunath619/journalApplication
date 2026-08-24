import { zodResolver } from '@hookform/resolvers/zod'
import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'
import { FormField } from '../components/forms/FormField'
import { useAuth } from '../features/auth/useAuth'
import { profileErrorMessage } from '../features/profile/profileErrors'
import { changePasswordSchema, profileSchema } from '../features/profile/profileSchemas'
import type { ChangePasswordValues, ProfileValues } from '../features/profile/profileSchemas'

export function ProfilePage() {
  const { user, updateProfile, changePassword } = useAuth()
  const navigate = useNavigate()
  const [profileMessage, setProfileMessage] = useState<string | null>(null)
  const [profileError, setProfileError] = useState<string | null>(null)
  const [passwordError, setPasswordError] = useState<string | null>(null)

  const profileForm = useForm<ProfileValues>({
    resolver: zodResolver(profileSchema),
    defaultValues: { email: user?.email ?? '', sentimentAnalysis: user?.sentimentAnalysis ?? false },
  })
  const passwordForm = useForm<ChangePasswordValues>({
    resolver: zodResolver(changePasswordSchema),
    defaultValues: { currentPassword: '', newPassword: '', confirmPassword: '' },
  })

  useEffect(() => {
    if (user) {
      profileForm.reset({ email: user.email, sentimentAnalysis: user.sentimentAnalysis })
    }
  }, [user, profileForm])

  async function saveProfile(values: ProfileValues) {
    setProfileError(null)
    setProfileMessage(null)
    try {
      await updateProfile(values)
      setProfileMessage('Profile updated successfully.')
    } catch (error) {
      setProfileError(profileErrorMessage(error, 'Unable to update your profile. Please try again.'))
    }
  }

  async function savePassword(values: ChangePasswordValues) {
    setPasswordError(null)
    try {
      await changePassword({ currentPassword: values.currentPassword, newPassword: values.newPassword })
      navigate('/login', { replace: true, state: { passwordChanged: true } })
    } catch (error) {
      setPasswordError(profileErrorMessage(error, 'Unable to change your password. Please try again.'))
    }
  }

  if (!user) return null

  return (
    <div className={'space-y-6'}>
      <section className={'rounded-2xl border border-slate-200 bg-white p-6 shadow-sm sm:p-10'}>
        <p className={'mb-2 text-xs font-bold uppercase tracking-[0.16em] text-indigo-600'}>Account</p>
        <h1 className={'text-3xl font-bold tracking-tight text-slate-900 sm:text-4xl'}>Profile</h1>
        <dl className={'mt-6 grid gap-4 rounded-xl bg-slate-50 p-5 sm:grid-cols-2'}>
          <div><dt className={'text-sm font-semibold text-slate-500'}>Username</dt><dd className={'mt-1 text-slate-900'}>{user.userName}</dd></div>
          <div><dt className={'text-sm font-semibold text-slate-500'}>Roles</dt><dd className={'mt-1 text-slate-900'}>{user.roles.join(', ')}</dd></div>
        </dl>

        {profileMessage && <p className={'mt-5 rounded-lg bg-emerald-50 p-3 text-sm font-medium text-emerald-800'} role={'status'}>{profileMessage}</p>}
        {profileError && <p className={'mt-5 rounded-lg bg-rose-50 p-3 text-sm font-medium text-rose-700'} role={'alert'}>{profileError}</p>}
        <form className={'mt-6 space-y-5'} noValidate onSubmit={profileForm.handleSubmit(saveProfile)}>
          <FormField id={'profile-email'} label={'Email'} type={'email'} autoComplete={'email'} error={profileForm.formState.errors.email?.message} {...profileForm.register('email')} />
          <label className={'flex items-start gap-3 text-sm leading-6 text-slate-600'}>
            <input className={'mt-1 size-4 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500'} type={'checkbox'} {...profileForm.register('sentimentAnalysis')} />
            <span>Enable sentiment-analysis emails when this optional feature becomes available.</span>
          </label>
          <button className={'rounded-lg bg-indigo-600 px-4 py-2.5 font-semibold text-white disabled:cursor-not-allowed disabled:opacity-60'} type={'submit'} disabled={profileForm.formState.isSubmitting}>
            {profileForm.formState.isSubmitting ? 'Saving...' : 'Save profile'}
          </button>
        </form>
      </section>

      <section className={'rounded-2xl border border-slate-200 bg-white p-6 shadow-sm sm:p-10'}>
        <h2 className={'text-2xl font-bold tracking-tight text-slate-900'}>Change password</h2>
        <p className={'mt-2 text-sm leading-6 text-slate-600'}>Changing your password signs you out on every device.</p>
        {passwordError && <p className={'mt-5 rounded-lg bg-rose-50 p-3 text-sm font-medium text-rose-700'} role={'alert'}>{passwordError}</p>}
        <form className={'mt-6 space-y-5'} noValidate onSubmit={passwordForm.handleSubmit(savePassword)}>
          <FormField id={'current-password'} label={'Current password'} type={'password'} autoComplete={'current-password'} error={passwordForm.formState.errors.currentPassword?.message} {...passwordForm.register('currentPassword')} />
          <FormField id={'new-password'} label={'New password'} type={'password'} autoComplete={'new-password'} error={passwordForm.formState.errors.newPassword?.message} {...passwordForm.register('newPassword')} />
          <FormField id={'confirm-password'} label={'Confirm new password'} type={'password'} autoComplete={'new-password'} error={passwordForm.formState.errors.confirmPassword?.message} {...passwordForm.register('confirmPassword')} />
          <button className={'rounded-lg bg-slate-900 px-4 py-2.5 font-semibold text-white disabled:cursor-not-allowed disabled:opacity-60'} type={'submit'} disabled={passwordForm.formState.isSubmitting}>
            {passwordForm.formState.isSubmitting ? 'Changing...' : 'Change password'}
          </button>
        </form>
      </section>
    </div>
  )
}
