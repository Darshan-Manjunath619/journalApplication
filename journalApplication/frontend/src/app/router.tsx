import { createBrowserRouter, Navigate } from 'react-router-dom'
import type { RouteObject } from 'react-router-dom'
import { AppShell } from '../components/AppShell'
import { DashboardPage } from '../pages/DashboardPage'
import { LoginPage } from '../pages/LoginPage'
import { NotFoundPage } from '../pages/NotFoundPage'
import { ProfilePage } from '../pages/ProfilePage'
import { RegisterPage } from '../pages/RegisterPage'
import { ProtectedRoute } from '../features/auth/ProtectedRoute'
import { CreateJournalPage } from '../pages/CreateJournalPage'
import { JournalDetailPage } from '../pages/JournalDetailPage'
import { EditJournalPage } from '../pages/EditJournalPage'

export const appRoutes: RouteObject[] = [
  { path: '/', element: <Navigate to={'/dashboard'} replace /> },
  { path: '/login', element: <LoginPage /> },
  { path: '/register', element: <RegisterPage /> },
  {
    element: <ProtectedRoute />,
    children: [
      {
        element: <AppShell />,
        children: [
          { path: '/dashboard', element: <DashboardPage /> },
          { path: '/journals/new', element: <CreateJournalPage /> },
          { path: '/journals/:id', element: <JournalDetailPage /> },
          { path: '/journals/:id/edit', element: <EditJournalPage /> },
          { path: '/profile', element: <ProfilePage /> },
        ],
      },
    ],
  },
  { path: '*', element: <NotFoundPage /> },
]

export const router = createBrowserRouter(appRoutes)
