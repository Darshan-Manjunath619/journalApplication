# Journal Application Frontend

React and TypeScript single-page application built with Vite.

Frontend foundations include React Router, TanStack Query, React Hook Form,
Zod, Tailwind CSS, Vitest, and React Testing Library.

## Requirements

- Node.js 24 LTS
- npm 11 or later

## Commands

Run commands from the `frontend` directory:

```powershell
npm.cmd install
npm.cmd run dev
```

The development server runs at `http://localhost:5173`.

## Backend configuration

Create the ignored local configuration from the safe template:

```powershell
Copy-Item .env.example .env.local
```

The template configures two API destinations:

```text
VITE_API_BASE_URL=http://localhost:8080/journal/api/v1
VITE_JOURNAL_API_BASE_URL=http://localhost:8081/journal/api/v1
```

Authentication and profile requests use `VITE_API_BASE_URL`. Journal and tag
requests use `VITE_JOURNAL_API_BASE_URL`. For another environment, set both
values when building the frontend.
Only variables beginning with `VITE_` are exposed to browser code, so never
store passwords, tokens, private keys, or database credentials in them.

Verification:

```powershell
npm.cmd run lint
npm.cmd run test
npm.cmd run build
```

- `lint` checks source-code quality.
- `test` runs the frontend test suite once with Vitest.
- `test:watch` reruns affected tests while frontend code changes.
- `build` type-checks TypeScript and creates optimized static files in `dist/`.
- `node_modules/` and `dist/` are generated and intentionally ignored by Git.

Run the Identity application at `http://localhost:8080/journal` and the
extracted Journal Service at `http://localhost:8081/journal` before testing the
complete browser workflow.
