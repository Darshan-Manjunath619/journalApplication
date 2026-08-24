import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { FormField } from '../components/forms/FormField'
import { loginErrorMessage } from '../features/auth/authErrors'
import { loginSchema } from '../features/auth/authSchemas'
import type { LoginValues } from '../features/auth/authSchemas'
import { useAuth } from '../features/auth/useAuth'

export function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [serverError, setServerError] = useState<string | null>(null)
  const registrationComplete = Boolean((location.state as { registrationComplete?: boolean } | null)?.registrationComplete)
  const passwordChanged = Boolean((location.state as { passwordChanged?: boolean } | null)?.passwordChanged)
  const requestedPath = (location.state as { from?: string } | null)?.from ?? '/dashboard'
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<LoginValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { userName: '', password: '' },
  })

  async function onSubmit(values: LoginValues) {
    setServerError(null)
    try {
      await login(values)
      navigate(requestedPath, { replace: true })
    } catch (error) {
      setServerError(loginErrorMessage(error))
    }
  }

  return (
    <main className={'grid min-h-screen place-items-center bg-slate-50 px-5 py-10'}>
      <section className={'w-full max-w-lg rounded-2xl border border-slate-200 bg-white p-7 shadow-xl shadow-slate-200/60 sm:p-10'}>
        <p className={'mb-2 text-xs font-bold uppercase tracking-[0.16em] text-indigo-600'}>Welcome back</p>
        <h1 className={'text-3xl font-bold tracking-tight text-slate-900'}>Sign in</h1>
        {registrationComplete && (
          <p className={'mt-4 rounded-lg bg-emerald-50 p-3 text-sm font-medium text-emerald-800'} role={'status'}>
            Account created. You can now sign in.
          </p>
        )}
        {passwordChanged && (
          <p className={'mt-4 rounded-lg bg-emerald-50 p-3 text-sm font-medium text-emerald-800'} role={'status'}>
            Password changed. Sign in again with your new password.
          </p>
        )}
        {serverError && <p className={'mt-4 rounded-lg bg-rose-50 p-3 text-sm font-medium text-rose-700'} role={'alert'}>{serverError}</p>}
        <form className={'mt-6 space-y-5'} noValidate onSubmit={handleSubmit(onSubmit)}>
          <FormField id={'userName'} label={'Username'} autoComplete={'username'} error={errors.userName?.message} {...register('userName')} />
          <FormField id={'password'} label={'Password'} type={'password'} autoComplete={'current-password'} error={errors.password?.message} {...register('password')} />
          <button className={'w-full rounded-lg bg-indigo-600 px-4 py-3 font-semibold text-white transition hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-60'} type={'submit'} disabled={isSubmitting}>
            {isSubmitting ? 'Signing in...' : 'Sign in'}
          </button>
        </form>
        <p className={'mt-6 text-sm text-slate-600'}>New here? <Link className={'font-semibold text-indigo-700'} to={'/register'}>Create an account</Link></p>
      </section>
    </main>
  )
}
