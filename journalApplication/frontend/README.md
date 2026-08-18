# Journal Application Frontend

React and TypeScript single-page application built with Vite.

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

Verification:

```powershell
npm.cmd run lint
npm.cmd run build
```

- `lint` checks source-code quality.
- `build` type-checks TypeScript and creates optimized static files in `dist/`.
- `node_modules/` and `dist/` are generated and intentionally ignored by Git.

The Spring Boot backend remains a separate project at the repository root and
runs at `http://localhost:8080/journal`.
