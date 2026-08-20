import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import { FormField } from '../components/forms/FormField'
import { registrationErrorMessage } from '../features/auth/authErrors'
import { registerSchema } from '../features/auth/authSchemas'
import type { RegisterValues } from '../features/auth/authSchemas'
import { useAuth } from '../features/auth/useAuth'

export function RegisterPage() {
  const { register: registerUser } = useAuth()
  const navigate = useNavigate()
  const [serverError, setServerError] = useState<string | null>(null)
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<RegisterValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: { userName: '', email: '', password: '', sentimentAnalysis: false },
  })

  async function onSubmit(values: RegisterValues) {
    setServerError(null)
    try {
      await registerUser(values)
      navigate('/login', { replace: true, state: { registrationComplete: true } })
    } catch (error) {
      setServerError(registrationErrorMessage(error))
    }
  }

  return (
    <main className={'grid min-h-screen place-items-center bg-slate-50 px-5 py-10'}>
      <section className={'w-full max-w-lg rounded-2xl border border-slate-200 bg-white p-7 shadow-xl shadow-slate-200/60 sm:p-10'}>
        <p className={'mb-2 text-xs font-bold uppercase tracking-[0.16em] text-indigo-600'}>Start writing</p>
        <h1 className={'text-3xl font-bold tracking-tight text-slate-900'}>Create an account</h1>
        {serverError && <p className={'mt-4 rounded-lg bg-rose-50 p-3 text-sm font-medium text-rose-700'} role={'alert'}>{serverError}</p>}
        <form className={'mt-6 space-y-5'} noValidate onSubmit={handleSubmit(onSubmit)}>
          <FormField id={'userName'} label={'Username'} autoComplete={'username'} error={errors.userName?.message} {...register('userName')} />
          <FormField id={'email'} label={'Email'} type={'email'} autoComplete={'email'} error={errors.email?.message} {...register('email')} />
          <FormField id={'password'} label={'Password'} type={'password'} autoComplete={'new-password'} error={errors.password?.message} {...register('password')} />
          <label className={'flex items-start gap-3 text-sm leading-6 text-slate-600'}>
            <input className={'mt-1 size-4 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500'} type={'checkbox'} {...register('sentimentAnalysis')} />
            <span>Opt in to future sentiment-analysis emails. This feature is currently disabled.</span>
          </label>
          <button className={'w-full rounded-lg bg-indigo-600 px-4 py-3 font-semibold text-white transition hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-60'} type={'submit'} disabled={isSubmitting}>
            {isSubmitting ? 'Creating account...' : 'Create account'}
          </button>
        </form>
        <p className={'mt-6 text-sm text-slate-600'}>Already registered? <Link className={'font-semibold text-indigo-700'} to={'/login'}>Sign in</Link></p>
      </section>
    </main>
  )
}
