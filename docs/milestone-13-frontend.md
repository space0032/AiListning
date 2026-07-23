# Milestone 13: Frontend Application

## Overview
Production-grade React frontend for the AI E-commerce Product Listing Generator. Built with modern tooling following the architecture defined in `docs/frontend-architecture.md`.

## Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| React | 18.3+ | UI library |
| TypeScript | 5.5+ | Type safety |
| Vite | 5.4+ | Build tool & dev server |
| Tailwind CSS | 4.x | Utility-first styling |
| shadcn/ui | latest | Accessible UI components (Radix UI + Tailwind) |
| React Router | 6.26+ | Client-side routing |
| TanStack Query | 5.x | Server state management |
| Zustand | 5.x | Client state management |
| React Hook Form | 7.x | Form management |
| Zod | 3.x | Schema validation |
| Axios | 1.x | HTTP client |
| Lucide React | latest | Icons |

## Project Structure

```
frontend/
├── public/                          # Static assets
├── src/
│   ├── api/                         # API layer
│   │   ├── client.ts               # Axios instance with interceptors
│   │   ├── auth.api.ts             # Auth API calls
│   │   ├── listings.api.ts         # Listings CRUD API
│   │   ├── ai.api.ts               # AI generation API
│   │   ├── admin.api.ts            # Admin API
│   │   └── index.ts                # API exports
│   │
│   ├── app/                         # App configuration
│   │   ├── App.tsx                 # Root component
│   │   ├── providers.tsx           # QueryClient, Toaster, TooltipProvider
│   │   └── routes.tsx              # Route definitions with lazy loading
│   │
│   ├── components/
│   │   ├── ui/                      # shadcn/ui primitives (16 components)
│   │   │   ├── button.tsx          # Extended with asChild prop
│   │   │   ├── input.tsx
│   │   │   ├── card.tsx
│   │   │   ├── dialog.tsx
│   │   │   ├── dropdown-menu.tsx
│   │   │   ├── table.tsx
│   │   │   ├── badge.tsx
│   │   │   ├── select.tsx
│   │   │   ├── tabs.tsx
│   │   │   ├── sonner.tsx
│   │   │   ├── form.tsx
│   │   │   ├── separator.tsx
│   │   │   ├── skeleton.tsx
│   │   │   ├── avatar.tsx
│   │   │   ├── tooltip.tsx
│   │   │   ├── sheet.tsx
│   │   │   └── textarea.tsx
│   │   │
│   │   ├── atoms/                   # Basic building blocks
│   │   │   ├── badge.tsx           # Platform/status badge variants
│   │   │   └── loading-spinner.tsx # Consistent loading indicator
│   │   │
│   │   ├── molecules/              # Composed components
│   │   │   ├── search-input.tsx    # Debounced search with clear button
│   │   │   ├── status-badge.tsx    # Platform/status color-coded badges
│   │   │   └── empty-state.tsx     # Empty state with illustration + CTA
│   │   │
│   │   ├── organisms/              # Complex components
│   │   │   ├── sidebar.tsx         # Collapsible navigation sidebar
│   │   │   ├── header.tsx          # Top header with user menu
│   │   │   ├── protected-route.tsx # Auth route guard
│   │   │   └── admin-route.tsx     # Admin route guard
│   │   │
│   │   └── templates/              # Page layouts
│   │       ├── dashboard-layout.tsx  # Sidebar + Header + Content
│   │       ├── auth-layout.tsx       # Split layout for auth pages
│   │       └── public-layout.tsx     # Navbar + Content + Footer
│   │
│   ├── hooks/                       # Custom React hooks
│   │   ├── use-auth.ts            # Login, register, logout, forgot/reset password
│   │   ├── use-listings.ts        # Listings CRUD + stats hooks
│   │   ├── use-ai.ts              # AI generation hooks
│   │   ├── use-admin.ts           # Admin users, overview, health hooks
│   │   ├── use-debounce.ts        # Debounce hook
│   │   └── use-media-query.ts     # Responsive breakpoint hooks
│   │
│   ├── lib/                         # Utilities
│   │   ├── utils.ts               # cn() helper (clsx + tailwind-merge)
│   │   ├── validations.ts         # Zod schemas for all forms
│   │   ├── constants.ts           # Platform configs, status configs
│   │   └── formatters.ts          # Date, number, duration formatters
│   │
│   ├── pages/
│   │   ├── public/                  # Public pages (7)
│   │   │   ├── landing.tsx         # Hero + Features + CTA
│   │   │   ├── login.tsx           # Username/password form
│   │   │   ├── register.tsx        # Registration form
│   │   │   ├── forgot-password.tsx # Email input for reset
│   │   │   ├── reset-password.tsx  # New password form
│   │   │   ├── verify-email.tsx    # Email verification handler
│   │   │   ├── unauthorized.tsx    # 403 page
│   │   │   └── not-found.tsx       # 404 page
│   │   │
│   │   ├── dashboard/
│   │   │   └── index.tsx           # Stats cards + recent listings
│   │   │
│   │   ├── listings/                # Listings CRUD (5)
│   │   │   ├── index.tsx           # Data table with search/filter
│   │   │   ├── new.tsx             # Create listing form
│   │   │   └── [id]/
│   │   │       ├── index.tsx       # Listing detail view
│   │   │       ├── edit.tsx        # Edit listing form
│   │   │       └── generate.tsx    # AI content generation
│   │   │
│   │   ├── admin/                   # Admin pages (3)
│   │   │   ├── index.tsx           # Admin dashboard + health
│   │   │   ├── users.tsx           # User management table
│   │   │   └── analytics.tsx       # Generation stats + charts
│   │   │
│   │   └── settings/
│   │       └── index.tsx           # User profile settings
│   │
│   ├── stores/                      # Zustand state management
│   │   ├── auth.store.ts           # Auth state (tokens, user)
│   │   ├── ui.store.ts             # UI state (sidebar, theme)
│   │   └── listing.store.ts        # Listing filters, view mode
│   │
│   └── types/                       # TypeScript type definitions
│       ├── api.ts                  # ApiResponse, PaginatedResponse
│       ├── auth.ts                 # User, AuthResponse, LoginRequest
│       ├── listing.ts              # Listing, ListingRequest, Platform
│       ├── ai.ts                   # AiGenerationRequest/Response
│       ├── admin.ts                # AdminStats, GenerationStats
│       └── index.ts                # Type exports
│
├── .env                             # Environment variables
├── .env.example                     # Example env file
├── components.json                  # shadcn/ui config
├── index.html                       # HTML entry
├── package.json
├── tsconfig.json
├── tsconfig.app.json
├── vite.config.ts
└── tailwind.config.ts
```

## Key Architecture Decisions

### 1. State Management Split
- **TanStack Query**: Server state (API data, caching, mutations)
- **Zustand**: Client state (auth tokens, UI preferences, filters)

### 2. Authentication Strategy
- Access token stored in memory (Zustand) — immune to XSS
- Refresh token stored in HttpOnly cookie — immune to XSS
- Automatic token refresh via Axios interceptor
- Force logout on refresh failure

### 3. Route Structure
- Lazy-loaded pages via `React.lazy()` for code splitting
- Protected routes via `ProtectedRoute` component
- Admin routes via `AdminRoute` component
- Public routes with redirect if already authenticated

### 4. Form Handling
- React Hook Form for performance (minimal re-renders)
- Zod schemas for validation with TypeScript inference
- Consistent error display patterns

## Pages Implemented

| Page | Route | Auth | Description |
|------|-------|------|-------------|
| Landing | `/` | Public | Hero section, features, CTA |
| Login | `/login` | Public | Username/password auth |
| Register | `/register` | Public | Account creation |
| Forgot Password | `/forgot-password` | Public | Email for reset link |
| Reset Password | `/reset-password` | Public | New password form |
| Verify Email | `/verify-email` | Public | Email verification |
| Dashboard | `/dashboard` | Protected | Stats + recent listings |
| Listings List | `/listings` | Protected | Paginated table with filters |
| Create Listing | `/listings/new` | Protected | Multi-field form |
| Listing Detail | `/listings/:id` | Protected | Full listing view |
| Edit Listing | `/listings/:id/edit` | Protected | Edit form |
| AI Generate | `/listings/:id/generate` | Protected | AI content generation |
| Settings | `/settings` | Protected | User profile |
| Admin Dashboard | `/admin` | Admin | System overview + health |
| Admin Users | `/admin/users` | Admin | User management |
| Admin Analytics | `/admin/analytics` | Admin | Generation statistics |
| 403 Unauthorized | `/unauthorized` | Public | Access denied page |
| 404 Not Found | `*` | Public | Page not found |

## API Integration

### Axios Interceptors
- **Request**: Attaches JWT access token to headers
- **Response**: Unwraps `ApiResponse<T>`, handles 401 (token refresh), 429 (rate limit)

### TanStack Query Hooks
- `useListings(params)` — Paginated listing list with cache
- `useListing(id)` — Single listing fetch
- `useCreateListing()` — Create with cache invalidation
- `useUpdateListing()` — Update with cache invalidation
- `useDeleteListing()` — Delete with toast notification
- `useAiGeneration()` — AI content generation
- `useAdminUsers()` — Admin user list
- `useAdminOverview()` — Admin stats

## UI Components

### shadcn/ui (16 components)
Button, Input, Card, Dialog, DropdownMenu, Table, Badge, Select, Tabs, Sonner, Form, Separator, Skeleton, Avatar, Tooltip, Sheet, Textarea

### Custom Components
- `StatusBadge` — Platform (Amazon/Flipkart/Meesho/Shopify) and status (Draft/Published/Archived) badges
- `SearchInput` — Debounced search with clear button
- `EmptyState` — Empty state with illustration and CTA
- `LoadingSpinner` — Consistent loading indicator
- `ProtectedRoute` — Auth guard component
- `AdminRoute` — Admin role guard component

## Validation Schemas

```typescript
loginSchema       // username (min 3), password (min 6)
registerSchema    // username, email, fullName, password (min 8, uppercase, lowercase, number), confirmPassword
listingSchema     // productName (required), productDescription, category, brand, material, color, size, platform (required)
forgotPasswordSchema  // email
resetPasswordSchema   // password, confirmPassword
```

## Performance

| Metric | Target | Implementation |
|--------|--------|----------------|
| Code Splitting | Route-level | `React.lazy()` for all pages |
| Bundle Size | < 200KB gzipped | Vite tree shaking, manual chunks |
| Caching | 5min stale | TanStack Query default |
| Re-renders | Minimal | React Hook Form, Zustand selectors |

## Build Output

```
dist/
├── index.html
└── assets/
    ├── index-Bb-EDlvT.js           301 KB (97 KB gzipped)
    ├── chunk-KS7C4IRE-plBlQRw.js    95 KB (32 KB gzipped)
    ├── validations-C9PmCWDv.js      91 KB (27 KB gzipped)
    └── [page chunks]                3-11 KB each
```

## How to Run

```bash
cd frontend
npm install          # Install dependencies
npm run dev          # Start dev server (http://localhost:3000)
npm run build        # Production build
npm run preview      # Preview production build
```

## Next Steps

- [ ] Add unit tests (Vitest + React Testing Library)
- [ ] Add E2E tests (Playwright)
- [ ] Add Storybook for component development
- [ ] Implement dark mode toggle
- [ ] Add image drag-and-drop upload component
- [ ] Add bulk operations for listings
- [ ] Implement real-time notifications via WebSocket
- [ ] Add export to CSV/PDF for admin analytics

---

*Document Version: 1.0*
*Created: 2026-07-22*
*Milestone: 13*
