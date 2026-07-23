# Frontend Architecture Document

## AI E-commerce Product Listing Generator — Web Client

---

## 1. Executive Summary

### 1.1 Purpose
This document defines the complete frontend architecture for the AI E-commerce Product Listing Generator. It serves as the single source of truth for all frontend development decisions, patterns, and standards.

### 1.2 Goals
- Build a production-grade, performant, and accessible web application
- Provide seamless integration with the existing Spring Boot REST API
- Deliver an intuitive user experience for AI-powered product listing generation
- Ensure code maintainability through strict architectural patterns

### 1.3 Non-Goals
- Mobile native apps (iOS/Android) — future consideration
- Desktop application
- Multi-language support (i18n) — Phase 2

---

## 2. Technology Stack

### 2.1 Core

| Technology | Version | Purpose |
|------------|---------|---------|
| React | 18.3+ | UI library |
| TypeScript | 5.5+ | Type safety |
| Vite | 5.4+ | Build tool & dev server |
| React Router | 6.26+ | Client-side routing |

### 2.2 State Management

| Technology | Purpose |
|------------|---------|
| TanStack Query (React Query) | Server state, caching, mutations, optimistic updates |
| Zustand | Client state (UI state, auth state, preferences) |

**Why this split?**
- Server state (API data) is managed by TanStack Query — handles caching, refetching, pagination, background updates
- Client state (modals, sidebar, theme) is managed by Zustand — lightweight, no boilerplate

### 2.3 Forms & Validation

| Technology | Purpose |
|------------|---------|
| React Hook Form | Form management, performance (minimal re-renders) |
| Zod | Schema validation, TypeScript inference |

### 2.4 UI & Styling

| Technology | Purpose |
|------------|---------|
| Tailwind CSS | Utility-first styling |
| shadcn/ui | Pre-built accessible components (Radix UI + Tailwind) |
| Lucide React | Icon library |
| clsx | Conditional classnames |
| tailwind-merge | Tailwind class deduplication |

**Why shadcn/ui?**
- Not a component library — you own the code
- Built on Radix UI (accessible primitives)
- Fully customizable with Tailwind
- No bundle bloat from unused components

### 2.5 HTTP & API

| Technology | Purpose |
|------------|---------|
| Axios | HTTP client with interceptors |
| ky | Lighter alternative (evaluated, Axios chosen for interceptor control) |

### 2.6 Utilities

| Technology | Purpose |
|------------|---------|
| date-fns | Date formatting/manipulation |
| react-hot-toast | Toast notifications |
| react-dropzone | Drag-and-drop file uploads |
| recharts | Charts for admin analytics |
| react-helmet-async | Document head management (SEO) |

### 2.7 Testing

| Technology | Purpose |
|------------|---------|
| Vitest | Unit test runner (faster than Jest, Vite-native) |
| React Testing Library | Component testing |
| MSW (Mock Service Worker) | API mocking in tests |
| Playwright | End-to-end testing |
| Storybook | Component development & visual testing |

---

## 3. System Architecture

### 3.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Browser                              │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                    React App                          │  │
│  │  ┌──────────┐  ┌──────────┐  ┌───────────────────┐  │  │
│  │  │  Pages   │  │Components│  │   Hooks/Utils     │  │  │
│  │  └────┬─────┘  └────┬─────┘  └────────┬──────────┘  │  │
│  │       │              │                 │              │  │
│  │  ┌────▼──────────────▼─────────────────▼──────────┐  │  │
│  │  │              TanStack Query + Axios             │  │  │
│  │  │         (Server State + HTTP Client)            │  │  │
│  │  └────────────────────┬───────────────────────────┘  │  │
│  │                       │                              │  │
│  │  ┌────────────────────▼───────────────────────────┐  │  │
│  │  │              Zustand Store                     │  │  │
│  │  │     (Auth State, UI State, Preferences)        │  │  │
│  │  └───────────────────────────────────────────────┘  │  │
│  └───────────────────────────────────────────────────────┘  │
│                            │                                │
└────────────────────────────┼────────────────────────────────┘
                             │ HTTP/HTTPS
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                   Spring Boot API (:8080)                   │
│                     /api/v1/*                                │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 Request Flow

```
User Action
    │
    ▼
React Component
    │
    ▼
Custom Hook (useListings, useAuth, etc.)
    │
    ▼
TanStack Query (cache check)
    │
    ├─ Cache Hit ──► Return cached data
    │
    └─ Cache Miss ─► Axios Instance
                         │
                         ▼
                    Request Interceptor
                    (attach JWT, handle refresh)
                         │
                         ▼
                    Backend API
                         │
                         ▼
                    Response Interceptor
                    (unwrap ApiResponse, handle errors)
                         │
                         ▼
                    TanStack Query Cache
                         │
                         ▼
                    Component Re-render
```

### 3.3 Error Handling Flow

```
API Error Response
    │
    ▼
Response Interceptor
    │
    ├─ 401 Unauthorized ──► Token Refresh Attempt
    │   ├─ Refresh Success ──► Retry Original Request
    │   └─ Refresh Failure ──► Force Logout ──► Redirect to /login
    │
    ├─ 403 Forbidden ──► Show "Access Denied" toast
    │
    ├─ 404 Not Found ──► Show "Resource not found" toast
    │
    ├─ 429 Rate Limited ──► Show "Too many requests" toast + backoff
    │
    └─ 500+ Server Error ──► Show "Server error" toast + log to monitoring
```

---

## 4. Authentication Architecture

### 4.1 Token Strategy

```
┌──────────────────────────────────────────────────────────┐
│                    Token Storage                         │
├──────────────────────────────────────────────────────────┤
│  Access Token   │  In memory (React state/Zustand)       │
│  Refresh Token  │  HttpOnly, Secure, SameSite=Strict cookie │
│  User Profile   │  Zustand (persisted to localStorage)   │
└──────────────────────────────────────────────────────────┘
```

**Why this approach?**
- **Access token in memory**: Cannot be stolen by XSS (not in localStorage/cookies)
- **Refresh token in HttpOnly cookie**: Cannot be accessed by JavaScript, immune to XSS
- **User profile in localStorage**: Non-sensitive data, avoids re-fetching on page reload

### 4.2 Auth Flow

```
Login Request
    │
    ▼
POST /auth/login { username, password }
    │
    ▼
Response: { accessToken, refreshToken, user }
    │
    ├─► Store accessToken in Zustand (in-memory)
    ├─► Store user profile in Zustand (persisted to localStorage)
    └─► Set refreshToken as HttpOnly cookie (backend sets via Set-Cookie)
```

### 4.3 Token Refresh Flow

```
API Request with expired access token
    │
    ▼
401 Unauthorized
    │
    ▼
Axios Interceptor catches 401
    │
    ├─ Check if already refreshing ──► Queue request
    │
    └─ POST /auth/refresh { refreshToken } (from cookie)
         │
         ├─ 200 OK ──► New accessToken + new refreshToken
         │   │
         │   ├─► Update Zustand (new accessToken)
         │   ├─► Set new refreshToken cookie
         │   └─► Retry queued requests
         │
         └─ 401/403 ──► Force logout
             │
             └─► Clear Zustand, clear cookie, redirect to /login
```

### 4.4 Protected Routes

```tsx
// Route Guard Component
<ProtectedRoute requiredRole="USER">
  <Dashboard />
</ProtectedRoute>

// Logic:
// 1. Check if user is authenticated (Zustand auth state)
// 2. If not, redirect to /login
// 3. If yes, check role (if requiredRole provided)
// 4. If wrong role, redirect to /unauthorized
// 5. If all good, render children
```

---

## 5. Page Architecture

### 5.1 Route Structure

```
/                           → Landing Page (public)
/login                      → Login Page (public, redirect if authed)
/register                   → Register Page (public, redirect if authed)
/forgot-password            → Forgot Password (public)
/reset-password?token=xxx   → Reset Password (public)
/verify-email?token=xxx     → Email Verification (public)

/dashboard                  → Dashboard (protected)
/listings                   → Listings List (protected)
/listings/new               → Create Listing (protected)
/listings/:id               → Listing Detail (protected)
/listings/:id/edit          → Edit Listing (protected)
/listings/:id/generate      → AI Generate (protected)

/admin                      → Admin Dashboard (admin only)
/admin/users                → User Management (admin only)
/admin/analytics            → Analytics (admin only)

/settings                   → User Settings (protected)
/profile                    → User Profile (protected)

/unauthorized               → 403 Page (public)
/not-found                  → 404 Page (public)
```

### 5.2 Page Descriptions

#### Landing Page (`/`)
- Hero section with product value proposition
- Feature highlights (AI generation, multi-platform, SEO optimization)
- Call-to-action buttons (Get Started, View Demo)
- Responsive design (mobile-first)
- No auth required

#### Login Page (`/login`)
- Username/password form
- "Remember me" checkbox
- "Forgot password?" link
- "Don't have an account? Register" link
- Loading states, error handling
- Redirect to /dashboard on success

#### Register Page (`/register`)
- Username, email, full name, password, confirm password
- Client-side validation (Zod schema)
- "Already have an account? Login" link
- Email verification notice after registration
- Redirect to /dashboard on success

#### Dashboard (`/dashboard`)
- Overview cards: Total Listings, Published, Drafts, Archived
- Recent listings quick view
- AI generation quick action button
- Platform distribution chart (pie/donut)
- Activity timeline (recent actions)

#### Listings List (`/listings`)
- Data table with sorting, filtering, search
- Columns: Name, Platform, Status, Created, Actions
- Bulk actions (delete, status change)
- Pagination (10/25/50 per page)
- Filter by: Platform, Status, Date Range
- Search: Product name, brand, category
- Empty state illustration

#### Create Listing (`/listings/new`)
- Multi-step form (wizard):
  1. **Product Details**: Name, description, category, brand
  2. **Attributes**: Material, color, size
  3. **Platform Selection**: Amazon, Flipkart, Meesho, Shopify
  4. **Image Upload**: Drag-and-drop, preview
  5. **Review & Submit**: Summary before creation
- "Generate with AI" shortcut button
- Auto-save draft (debounced)

#### Listing Detail (`/listings/:id`)
- Full product information display
- AI-generated content section (SEO title, description, bullet points)
- Platform-formatted listing preview
- Action buttons: Edit, Generate AI, Duplicate, Delete
- Status badge and status change dropdown
- Image display with zoom
- Metadata (created, updated timestamps)

#### Edit Listing (`/listings/:id/edit`)
- Pre-filled form with existing data
- Same layout as Create Listing
- Save/Cancel buttons
- Unsaved changes warning on navigation

#### AI Generate (`/listings/:id/generate`)
- Product summary display
- Platform selector (if not set)
- "Generate" button with loading state
- Progress indicator (est. 2-5 seconds)
- Generated content preview with copy-to-clipboard
- "Apply to Listing" button
- "Regenerate" option
- Generation history (if available)

#### Admin Dashboard (`/admin`)
- System health overview
- User growth chart
- AI generation statistics
- Top users by activity
- System metrics (cache hit rate, API response times)

#### User Management (`/admin/users`)
- Users data table
- Columns: Username, Email, Role, Status, Created, Actions
- Toggle user enabled/disabled
- View user details (modal/drawer)
- Search and filter by role/status

#### Analytics (`/admin/analytics`)
- Generation stats (total, success rate, avg time)
- Platform usage breakdown
- User activity trends (line chart)
- Model usage distribution
- Date range picker for filtering

---

## 6. Component Architecture

### 6.1 Atomic Design Structure

```
src/
├── components/
│   ├── ui/                    # shadcn/ui primitives (Button, Input, Card, etc.)
│   ├── atoms/                 # Basic building blocks (Avatar, Badge, Icon)
│   ├── molecules/             # Composed atoms (SearchBar, FormField, StatusBadge)
│   ├── organisms/             # Complex components (DataTable, Navbar, Sidebar)
│   └── templates/             # Page layouts (DashboardLayout, AuthLayout)
```

### 6.2 Core Components

#### Layout Components
```
DashboardLayout
├── Sidebar (navigation, collapsible)
├── Header (search, notifications, user menu)
├── Main Content Area
└── Footer (optional)

AuthLayout
├── Left Panel (branding, illustration)
└── Right Panel (form content)

PublicLayout
├── Navbar (logo, nav links, CTA)
├── Main Content
└── Footer
```

#### Shared Components
```
├── DataTable          # Reusable table with sort/filter/pagination
├── SearchInput        # Debounced search input
├── StatusBadge        # Color-coded status indicator
├── PlatformBadge      # Platform-specific badge (Amazon=orange, etc.)
├── ConfirmDialog      # Reusable confirmation modal
├── EmptyState         # Empty state with illustration + CTA
├── ErrorBoundary      # React error boundary
├── LoadingSpinner     # Consistent loading indicator
├── PageHeader         # Page title + breadcrumbs + actions
├── FileUpload         # Drag-and-drop file upload
├── ImageViewer        # Image preview with zoom
├── CopyToClipboard    # Copy button with feedback
└── AiGenerationCard   # AI content display card
```

### 6.3 Component Example Pattern

```tsx
// components/molecules/StatusBadge.tsx
import { cva, type VariantProps } from "class-variance-authority";
import { cn } from "@/lib/utils";

const statusVariants = cva(
  "inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium",
  {
    variants: {
      status: {
        DRAFT: "bg-yellow-100 text-yellow-800",
        PUBLISHED: "bg-green-100 text-green-800",
        ARCHIVED: "bg-gray-100 text-gray-800",
      },
    },
    defaultVariants: { status: "DRAFT" },
  }
);

interface StatusBadgeProps extends VariantProps<typeof statusVariants> {
  label?: string;
}

export function StatusBadge({ status, label }: StatusBadgeProps) {
  return (
    <span className={cn(statusVariants({ status }))}>
      {label || status}
    </span>
  );
}
```

---

## 7. Directory Structure

```
frontend/
├── public/
│   ├── favicon.ico
│   └── images/
│       ├── logo.svg
│       └── illustrations/
│           ├── empty-listings.svg
│           ├── 404.svg
│           └── login-illustration.svg
│
├── src/
│   ├── app/                          # App entry, providers, router
│   │   ├── App.tsx
│   │   ├── providers.tsx             # QueryClient, Toaster, etc.
│   │   └── routes.tsx                # Route definitions
│   │
│   ├── api/                          # API layer
│   │   ├── client.ts                 # Axios instance, interceptors
│   │   ├── auth.api.ts               # Auth API calls
│   │   ├── listings.api.ts           # Listings API calls
│   │   ├── ai.api.ts                 # AI generation API calls
│   │   ├── admin.api.ts              # Admin API calls
│   │   ├── user.api.ts               # User API calls
│   │   └── cache.api.ts              # Cache API calls
│   │
│   ├── hooks/                        # Custom hooks
│   │   ├── use-auth.ts               # Auth hook (login, logout, register)
│   │   ├── use-listings.ts           # Listings CRUD hooks
│   │   ├── use-listing.ts            # Single listing hook
│   │   ├── use-ai-generation.ts      # AI generation hook
│   │   ├── use-users.ts              # Admin user hooks
│   │   ├── use-debounce.ts           # Debounce hook
│   │   ├── use-local-storage.ts      # LocalStorage hook
│   │   └── use-media-query.ts        # Responsive breakpoint hook
│   │
│   ├── stores/                       # Zustand stores
│   │   ├── auth.store.ts             # Auth state (user, token, isAuthenticated)
│   │   ├── ui.store.ts               # UI state (sidebar, theme, modals)
│   │   └── listing.store.ts          # Listing UI state (filters, view mode)
│   │
│   ├── lib/                          # Utilities
│   │   ├── utils.ts                  # cn() helper, general utilities
│   │   ├── constants.ts              # API URLs, platform configs, etc.
│   │   ├── validations.ts            # Zod schemas
│   │   └── formatters.ts             # Date, number, status formatters
│   │
│   ├── components/
│   │   ├── ui/                       # shadcn/ui components
│   │   │   ├── button.tsx
│   │   │   ├── input.tsx
│   │   │   ├── card.tsx
│   │   │   ├── dialog.tsx
│   │   │   ├── dropdown-menu.tsx
│   │   │   ├── table.tsx
│   │   │   ├── badge.tsx
│   │   │   ├── select.tsx
│   │   │   ├── tabs.tsx
│   │   │   ├── toast.tsx
│   │   │   ├── form.tsx
│   │   │   ├── label.tsx
│   │   │   ├── separator.tsx
│   │   │   ├── skeleton.tsx
│   │   │   ├── avatar.tsx
│   │   │   ├── tooltip.tsx
│   │   │   └── sheet.tsx
│   │   │
│   │   ├── atoms/
│   │   │   ├── logo.tsx
│   │   │   ├── loading-spinner.tsx
│   │   │   ├── error-boundary.tsx
│   │   │   └── copy-button.tsx
│   │   │
│   │   ├── molecules/
│   │   │   ├── search-input.tsx
│   │   │   ├── status-badge.tsx
│   │   │   ├── platform-badge.tsx
│   │   │   ├── confirm-dialog.tsx
│   │   │   ├── empty-state.tsx
│   │   │   ├── page-header.tsx
│   │   │   ├── file-upload.tsx
│   │   │   └── form-field.tsx
│   │   │
│   │   ├── organisms/
│   │   │   ├── data-table.tsx
│   │   │   ├── navbar.tsx
│   │   │   ├── sidebar.tsx
│   │   │   ├── user-menu.tsx
│   │   │   ├── listing-card.tsx
│   │   │   ├── listing-form.tsx
│   │   │   ├── ai-content-preview.tsx
│   │   │   ├── stats-card.tsx
│   │   │   └── pagination.tsx
│   │   │
│   │   └── templates/
│   │       ├── dashboard-layout.tsx
│   │       ├── auth-layout.tsx
│   │       └── public-layout.tsx
│   │
│   ├── pages/
│   │   ├── public/
│   │   │   ├── landing.tsx
│   │   │   ├── login.tsx
│   │   │   ├── register.tsx
│   │   │   ├── forgot-password.tsx
│   │   │   ├── reset-password.tsx
│   │   │   ├── verify-email.tsx
│   │   │   ├── unauthorized.tsx
│   │   │   └── not-found.tsx
│   │   │
│   │   ├── dashboard/
│   │   │   └── index.tsx
│   │   │
│   │   ├── listings/
│   │   │   ├── index.tsx             # List view
│   │   │   ├── new.tsx               # Create form
│   │   │   ├── [id].tsx              # Detail view
│   │   │   ├── [id]/edit.tsx         # Edit form
│   │   │   └── [id]/generate.tsx     # AI generation
│   │   │
│   │   ├── admin/
│   │   │   ├── index.tsx             # Admin dashboard
│   │   │   ├── users.tsx             # User management
│   │   │   └── analytics.tsx         # Analytics
│   │   │
│   │   └── settings/
│   │       └── index.tsx
│   │
│   ├── types/
│   │   ├── api.ts                    # API response types
│   │   ├── listing.ts                # Listing types
│   │   ├── user.ts                   # User types
│   │   └── auth.ts                   # Auth types
│   │
│   ├── styles/
│   │   └── globals.css               # Tailwind imports, custom CSS
│   │
│   └── vite-env.d.ts
│
├── .env                              # Environment variables
├── .env.example                      # Example env file
├── index.html                        # HTML entry
├── package.json
├── tsconfig.json
├── tsconfig.node.json
├── vite.config.ts
├── tailwind.config.ts
├── postcss.config.js
├── components.json                   # shadcn/ui config
└── README.md
```

---

## 8. API Integration Layer

### 8.1 Axios Instance Configuration

```typescript
// api/client.ts
import axios from "axios";

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/v1",
  timeout: 30000,
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true, // Send cookies for refresh token
});

// Request Interceptor
apiClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response Interceptor
apiClient.interceptors.response.use(
  (response) => response.data, // Unwrap ApiResponse
  async (error) => {
    const originalRequest = error.config;
    
    // Handle 401 - Token Refresh
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      try {
        const { accessToken, refreshToken } = await refreshAccessToken();
        useAuthStore.getState().setTokens(accessToken, refreshToken);
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return apiClient(originalRequest);
      } catch (refreshError) {
        useAuthStore.getState().logout();
        window.location.href = "/login";
        return Promise.reject(refreshError);
      }
    }
    
    // Handle Rate Limiting
    if (error.response?.status === 429) {
      toast.error("Too many requests. Please wait a moment.");
    }
    
    return Promise.reject(error);
  }
);
```

### 8.2 API Service Example

```typescript
// api/listings.api.ts
import { apiClient } from "./client";
import type { 
  ListingResponse, 
  ListingRequest, 
  PaginatedResponse,
  ApiResponse 
} from "@/types";

export const listingsApi = {
  getAll: (params?: ListingQueryParams) =>
    apiClient.get<any, ApiResponse<PaginatedResponse<ListingResponse>>>("/listings", { params }),

  getById: (id: number) =>
    apiClient.get<any, ApiResponse<ListingResponse>>(`/listings/${id}`),

  create: (data: ListingRequest) =>
    apiClient.post<any, ApiResponse<ListingResponse>>("/listings", data),

  update: (id: number, data: ListingRequest) =>
    apiClient.put<any, ApiResponse<ListingResponse>>(`/listings/${id}`, data),

  delete: (id: number) =>
    apiClient.delete(`/listings/${id}`),

  updateStatus: (id: number, status: ListingStatus) =>
    apiClient.patch(`/listings/${id}/status`, null, { params: { status } }),

  duplicate: (id: number) =>
    apiClient.post(`/listings/${id}/duplicate`),

  uploadImage: (id: number, file: File) => {
    const formData = new FormData();
    formData.append("file", file);
    return apiClient.post(`/listings/${id}/upload-image`, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
  },

  generateAi: (id: number) =>
    apiClient.post(`/listings/${id}/generate`),

  search: (keyword: string, params?: PaginationParams) =>
    apiClient.get("/listings/search", { params: { keyword, ...params } }),

  getStats: () =>
    apiClient.get("/listings/stats"),
};
```

### 8.3 TanStack Query Hooks

```typescript
// hooks/use-listings.ts
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { listingsApi } from "@/api/listings.api";
import { toast } from "react-hot-toast";

export function useListings(params?: ListingQueryParams) {
  return useQuery({
    queryKey: ["listings", params],
    queryFn: () => listingsApi.getAll(params),
    select: (data) => data.data,
  });
}

export function useListing(id: number) {
  return useQuery({
    queryKey: ["listing", id],
    queryFn: () => listingsApi.getById(id),
    select: (data) => data.data,
    enabled: !!id,
  });
}

export function useCreateListing() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: listingsApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["listings"] });
      toast.success("Listing created successfully");
    },
    onError: (error: ApiError) => {
      toast.error(error.response?.data?.message || "Failed to create listing");
    },
  });
}

export function useUpdateListing() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: ListingRequest }) =>
      listingsApi.update(id, data),
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: ["listings"] });
      queryClient.invalidateQueries({ queryKey: ["listing", id] });
      toast.success("Listing updated successfully");
    },
  });
}

export function useDeleteListing() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: listingsApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["listings"] });
      toast.success("Listing deleted successfully");
    },
  });
}

export function useListingStats() {
  return useQuery({
    queryKey: ["listingStats"],
    queryFn: listingsApi.getStats,
    select: (data) => data.data,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
}
```

---

## 9. State Management

### 9.1 Auth Store (Zustand)

```typescript
// stores/auth.store.ts
import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { User } from "@/types";

interface AuthState {
  accessToken: string | null;
  user: User | null;
  isAuthenticated: boolean;
  
  setAuth: (accessToken: string, user: User) => void;
  setAccessToken: (token: string) => void;
  logout: () => void;
  updateUser: (user: Partial<User>) => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      accessToken: null,
      user: null,
      isAuthenticated: false,

      setAuth: (accessToken, user) =>
        set({ accessToken, user, isAuthenticated: true }),

      setAccessToken: (token) => set({ accessToken: token }),

      logout: () =>
        set({ accessToken: null, user: null, isAuthenticated: false }),

      updateUser: (userData) =>
        set((state) => ({
          user: state.user ? { ...state.user, ...userData } : null,
        })),
    }),
    {
      name: "auth-storage",
      partialize: (state) => ({
        user: state.user,
        isAuthenticated: state.isAuthenticated,
        // Note: accessToken is NOT persisted (in memory only)
      }),
    }
  )
);
```

### 9.2 UI Store (Zustand)

```typescript
// stores/ui.store.ts
import { create } from "zustand";
import { persist } from "zustand/middleware";

interface UIState {
  sidebarOpen: boolean;
  sidebarCollapsed: boolean;
  theme: "light" | "dark" | "system";
  
  toggleSidebar: () => void;
  setSidebarCollapsed: (collapsed: boolean) => void;
  setTheme: (theme: "light" | "dark" | "system") => void;
}

export const useUIStore = create<UIState>()(
  persist(
    (set) => ({
      sidebarOpen: true,
      sidebarCollapsed: false,
      theme: "system",

      toggleSidebar: () => set((state) => ({ sidebarOpen: !state.sidebarOpen })),
      setSidebarCollapsed: (collapsed) => set({ sidebarCollapsed: collapsed }),
      setTheme: (theme) => set({ theme }),
    }),
    { name: "ui-storage" }
  )
);
```

### 9.3 Listing UI Store (Zustand)

```typescript
// stores/listing.store.ts
import { create } from "zustand";

interface ListingFilters {
  search: string;
  platform: string | null;
  status: string | null;
  dateRange: { from: Date | null; to: Date | null };
}

interface ListingState {
  filters: ListingFilters;
  viewMode: "table" | "grid";
  selectedIds: number[];
  
  setFilters: (filters: Partial<ListingFilters>) => void;
  resetFilters: () => void;
  setViewMode: (mode: "table" | "grid") => void;
  toggleSelection: (id: number) => void;
  selectAll: (ids: number[]) => void;
  clearSelection: () => void;
}

const defaultFilters: ListingFilters = {
  search: "",
  platform: null,
  status: null,
  dateRange: { from: null, to: null },
};

export const useListingStore = create<ListingState>((set) => ({
  filters: defaultFilters,
  viewMode: "table",
  selectedIds: [],

  setFilters: (newFilters) =>
    set((state) => ({ filters: { ...state.filters, ...newFilters } })),
  
  resetFilters: () => set({ filters: defaultFilters }),
  
  setViewMode: (mode) => set({ viewMode: mode }),
  
  toggleSelection: (id) =>
    set((state) => ({
      selectedIds: state.selectedIds.includes(id)
        ? state.selectedIds.filter((i) => i !== id)
        : [...state.selectedIds, id],
    })),
  
  selectAll: (ids) => set({ selectedIds: ids }),
  
  clearSelection: () => set({ selectedIds: [] }),
}));
```

---

## 10. Routing Implementation

### 10.1 Route Configuration

```typescript
// app/routes.tsx
import { createBrowserRouter, Navigate } from "react-router-dom";
import { lazy, Suspense } from "react";
import { DashboardLayout } from "@/components/templates/dashboard-layout";
import { AuthLayout } from "@/components/templates/auth-layout";
import { PublicLayout } from "@/components/templates/public-layout";
import { ProtectedRoute } from "@/components/organisms/protected-route";
import { AdminRoute } from "@/components/organisms/admin-route";
import { LoadingSpinner } from "@/components/atoms/loading-spinner";

// Lazy-loaded pages
const Landing = lazy(() => import("@/pages/public/landing"));
const Login = lazy(() => import("@/pages/public/login"));
const Register = lazy(() => import("@/pages/public/register"));
const ForgotPassword = lazy(() => import("@/pages/public/forgot-password"));
const ResetPassword = lazy(() => import("@/pages/public/reset-password"));
const VerifyEmail = lazy(() => import("@/pages/public/verify-email"));
const Dashboard = lazy(() => import("@/pages/dashboard/index"));
const Listings = lazy(() => import("@/pages/listings/index"));
const ListingDetail = lazy(() => import("@/pages/listings/[id]"));
const CreateListing = lazy(() => import("@/pages/listings/new"));
const EditListing = lazy(() => import("@/pages/listings/[id]/edit"));
const GenerateListing = lazy(() => import("@/pages/listings/[id]/generate"));
const AdminDashboard = lazy(() => import("@/pages/admin/index"));
const AdminUsers = lazy(() => import("@/pages/admin/users"));
const AdminAnalytics = lazy(() => import("@/pages/admin/analytics"));
const Settings = lazy(() => import("@/pages/settings/index"));
const Unauthorized = lazy(() => import("@/pages/public/unauthorized"));
const NotFound = lazy(() => import("@/pages/public/not-found"));

const SuspenseWrapper = ({ children }: { children: React.ReactNode }) => (
  <Suspense fallback={<LoadingSpinner className="h-screen" />}>
    {children}
  </Suspense>
);

export const router = createBrowserRouter([
  // Public routes
  {
    path: "/",
    element: <PublicLayout />,
    children: [
      { index: true, element: <SuspenseWrapper><Landing /></SuspenseWrapper> },
    ],
  },
  {
    path: "/",
    element: <AuthLayout />,
    children: [
      { path: "login", element: <SuspenseWrapper><Login /></SuspenseWrapper> },
      { path: "register", element: <SuspenseWrapper><Register /></SuspenseWrapper> },
      { path: "forgot-password", element: <SuspenseWrapper><ForgotPassword /></SuspenseWrapper> },
      { path: "reset-password", element: <SuspenseWrapper><ResetPassword /></SuspenseWrapper> },
      { path: "verify-email", element: <SuspenseWrapper><VerifyEmail /></SuspenseWrapper> },
    ],
  },

  // Protected routes
  {
    path: "/",
    element: <ProtectedRoute />,
    children: [
      {
        element: <DashboardLayout />,
        children: [
          { path: "dashboard", element: <SuspenseWrapper><Dashboard /></SuspenseWrapper> },
          { path: "listings", element: <SuspenseWrapper><Listings /></SuspenseWrapper> },
          { path: "listings/new", element: <SuspenseWrapper><CreateListing /></SuspenseWrapper> },
          { path: "listings/:id", element: <SuspenseWrapper><ListingDetail /></SuspenseWrapper> },
          { path: "listings/:id/edit", element: <SuspenseWrapper><EditListing /></SuspenseWrapper> },
          { path: "listings/:id/generate", element: <SuspenseWrapper><GenerateListing /></SuspenseWrapper> },
          { path: "settings", element: <SuspenseWrapper><Settings /></SuspenseWrapper> },
          { path: "profile", element: <SuspenseWrapper><Settings /></SuspenseWrapper> },
        ],
      },
    ],
  },

  // Admin routes
  {
    path: "/admin",
    element: <AdminRoute />,
    children: [
      {
        element: <DashboardLayout />,
        children: [
          { index: true, element: <SuspenseWrapper><AdminDashboard /></SuspenseWrapper> },
          { path: "users", element: <SuspenseWrapper><AdminUsers /></SuspenseWrapper> },
          { path: "analytics", element: <SuspenseWrapper><AdminAnalytics /></SuspenseWrapper> },
        ],
      },
    ],
  },

  // Error routes
  { path: "/unauthorized", element: <SuspenseWrapper><Unauthorized /></SuspenseWrapper> },
  { path: "*", element: <SuspenseWrapper><NotFound /></SuspenseWrapper> },
]);
```

### 10.2 Route Guards

```typescript
// components/organisms/protected-route.tsx
import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuthStore } from "@/stores/auth.store";

export function ProtectedRoute() {
  const { isAuthenticated } = useAuthStore();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return <Outlet />;
}

// components/organisms/admin-route.tsx
import { Navigate, Outlet } from "react-router-dom";
import { useAuthStore } from "@/stores/auth.store";

export function AdminRoute() {
  const { isAuthenticated, user } = useAuthStore();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (user?.role !== "ROLE_ADMIN") {
    return <Navigate to="/unauthorized" replace />;
  }

  return <Outlet />;
}
```

---

## 11. Validation Schemas

```typescript
// lib/validations.ts
import { z } from "zod";

export const loginSchema = z.object({
  username: z.string().min(3, "Username must be at least 3 characters"),
  password: z.string().min(6, "Password must be at least 6 characters"),
});

export const registerSchema = z.object({
  username: z
    .string()
    .min(3, "Username must be at least 3 characters")
    .max(50, "Username must be less than 50 characters")
    .regex(/^[a-zA-Z0-9_]+$/, "Username can only contain letters, numbers, and underscores"),
  email: z.string().email("Invalid email address"),
  fullName: z.string().min(2, "Full name is required").max(100),
  password: z
    .string()
    .min(8, "Password must be at least 8 characters")
    .regex(/[A-Z]/, "Password must contain at least one uppercase letter")
    .regex(/[a-z]/, "Password must contain at least one lowercase letter")
    .regex(/[0-9]/, "Password must contain at least one number"),
  confirmPassword: z.string(),
}).refine((data) => data.password === data.confirmPassword, {
  message: "Passwords don't match",
  path: ["confirmPassword"],
});

export const listingSchema = z.object({
  productName: z.string().min(1, "Product name is required").max(255),
  productDescription: z.string().max(5000).optional(),
  category: z.string().max(100).optional(),
  brand: z.string().max(100).optional(),
  material: z.string().max(100).optional(),
  color: z.string().max(100).optional(),
  size: z.string().max(100).optional(),
  platform: z.enum(["AMAZON", "FLIPKART", "MEESHO", "SHOPIFY"], {
    required_error: "Please select a platform",
  }),
});

export const forgotPasswordSchema = z.object({
  email: z.string().email("Invalid email address"),
});

export const resetPasswordSchema = z.object({
  password: z.string().min(8, "Password must be at least 8 characters"),
  confirmPassword: z.string(),
}).refine((data) => data.password === data.confirmPassword, {
  message: "Passwords don't match",
  path: ["confirmPassword"],
});

// Types inferred from schemas
export type LoginInput = z.infer<typeof loginSchema>;
export type RegisterInput = z.infer<typeof registerSchema>;
export type ListingInput = z.infer<typeof listingSchema>;
```

---

## 12. Type Definitions

```typescript
// types/api.ts
export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ApiError {
  response?: {
    data?: {
      success: boolean;
      message: string;
    };
    status: number;
  };
  message: string;
}

// types/user.ts
export interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
  role: "ROLE_USER" | "ROLE_ADMIN";
  enabled: boolean;
  emailVerified: boolean;
  createdAt: string;
}

// types/listing.ts
export type Platform = "AMAZON" | "FLIPKART" | "MEESHO" | "SHOPIFY";
export type ListingStatus = "DRAFT" | "PUBLISHED" | "ARCHIVED";

export interface Listing {
  id: number;
  productName: string;
  productDescription: string | null;
  category: string | null;
  brand: string | null;
  material: string | null;
  color: string | null;
  size: string | null;
  imageUrl: string | null;
  originalFileName: string | null;
  platform: Platform;
  seoTitle: string | null;
  bulletPoints: string | null;
  description: string | null;
  tags: string | null;
  keywords: string | null;
  metaDescription: string | null;
  platformFormattedListing: string | null;
  status: ListingStatus;
  createdAt: string;
  updatedAt: string;
}

export interface ListingRequest {
  productName: string;
  productDescription?: string;
  category?: string;
  brand?: string;
  material?: string;
  color?: string;
  size?: string;
  platform: Platform;
}

// types/auth.ts
export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  fullName: string;
}

// types/ai.ts
export interface AiGenerationRequest {
  productName: string;
  productDescription?: string;
  category?: string;
  brand?: string;
  material?: string;
  color?: string;
  size?: string;
  platform: Platform;
}

export interface AiGenerationResponse {
  seoTitle: string;
  bulletPoints: string;
  description: string;
  tags: string;
  keywords: string;
  metaDescription: string;
  modelUsed: string;
  generationTimeMs: number;
}

// types/admin.ts
export interface AdminStats {
  totalUsers: number;
  totalListings: number;
  activeUsers: number;
  aiGenerations: number;
}

export interface GenerationStats {
  totalGenerations: number;
  successRate: number;
  avgGenerationTimeMs: number;
  generationsByPlatform: Record<Platform, number>;
  generationsByDay: { date: string; count: number }[];
}
```

---

## 13. Environment Configuration

```env
# .env
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_APP_NAME=AI Listing Generator
VITE_APP_VERSION=1.0.0

# Optional: Analytics
VITE_GA_TRACKING_ID=

# Optional: Error Tracking
VITE_SENTRY_DSN=
```

```env
# .env.example
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_APP_NAME=AI Listing Generator
VITE_APP_VERSION=1.0.0
```

---

## 14. Performance Strategy

### 14.1 Code Splitting
- **Route-level**: All pages lazy-loaded via `React.lazy()`
- **Component-level**: Heavy components (charts, editors) lazy-loaded
- **Vendor splitting**: Separate chunks for React, TanStack Query, charts

### 14.2 Caching Strategy
- **TanStack Query**: 
  - `staleTime: 5min` for listings data
  - `cacheTime: 30min` for inactive queries
  - `refetchOnWindowFocus: false` for most queries
- **Browser**: Service worker for static assets (optional PWA)

### 14.3 Bundle Optimization
- Tree shaking via Vite
- Image optimization (WebP format, lazy loading)
- Font subsetting (only used characters)
- Compression (Brotli/Gzip in production)

### 14.4 Performance Targets
| Metric | Target |
|--------|--------|
| First Contentful Paint | < 1.5s |
| Largest Contentful Paint | < 2.5s |
| Time to Interactive | < 3.5s |
| Cumulative Layout Shift | < 0.1 |
| Total Bundle Size | < 200KB gzipped |

---

## 15. Security Measures

### 15.1 XSS Prevention
- React's default JSX escaping
- DOMPurify for any `dangerouslySetInnerHTML` usage
- Content Security Policy headers (via Nginx)

### 15.2 CSRF Protection
- SameSite cookies for refresh token
- No state-changing GET requests

### 15.3 Token Security
- Access token in memory only (not localStorage/cookies)
- Refresh token in HttpOnly, Secure, SameSite=Strict cookie
- Automatic token refresh before expiry
- Force logout on refresh failure

### 15.4 Input Validation
- Zod schemas for all form inputs
- Server-side validation (backend)
- Sanitize user inputs before display

### 15.5 Dependency Security
- `npm audit` in CI/CD
- Automated dependency updates (Dependabot/Renovate)
- Lock file committed to repo

---

## 16. Accessibility (WCAG 2.1 AA)

### 16.1 Requirements
- Semantic HTML elements (nav, main, article, section)
- ARIA labels for interactive elements
- Keyboard navigation (Tab, Enter, Escape, Arrow keys)
- Focus management (visible focus indicators)
- Color contrast ratio >= 4.5:1 (text), >= 3:1 (large text)
- Alt text for all images
- Form labels associated with inputs
- Error messages linked to form fields
- Skip navigation link

### 16.2 Testing
- Lighthouse accessibility audit (target: 95+)
- Manual keyboard testing
- Screen reader testing (VoiceOver/NVDA)
- axe-core integration in tests

---

## 17. Testing Strategy

### 17.1 Unit Tests (Vitest + React Testing Library)
- **Component tests**: All shared components
- **Hook tests**: Custom hooks
- **Utility tests**: Validation schemas, formatters, helpers
- **Target**: 80%+ coverage

### 17.2 Integration Tests
- **Form flows**: Login, register, listing creation
- **API integration**: Mocked API with MSW
- **Auth flows**: Token refresh, protected routes

### 17.3 End-to-End Tests (Playwright)
- **Critical paths**: 
  - Login → Dashboard → Create Listing → Generate AI → View Result
  - Register → Verify Email → Login
  - Admin → User Management → Toggle Status
- **Cross-browser**: Chrome, Firefox, Safari
- **Responsive**: Mobile, Tablet, Desktop viewports

### 17.4 Visual Regression (Storybook + Chromatic)
- Component snapshots
- Theme consistency checks

---

## 18. DevOps & Deployment

### 18.1 Development
```bash
npm run dev          # Start dev server (Vite)
npm run storybook    # Start Storybook
npm run test         # Run unit tests
npm run test:e2e     # Run Playwright tests
```

### 18.2 Build
```bash
npm run build        # Production build
npm run preview      # Preview production build
```

### 18.3 Docker

```dockerfile
# Dockerfile
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

### 18.4 Nginx Configuration

```nginx
server {
    listen 80;
    server_name localhost;
    root /usr/share/nginx/html;
    index index.html;

    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;
    add_header Content-Security-Policy "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline';" always;

    # Gzip compression
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;

    # Cache static assets
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # SPA fallback
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API proxy (optional - can use directly)
    location /api/ {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 18.5 CI/CD Integration

```yaml
# .github/workflows/frontend-ci.yml
name: Frontend CI

on:
  push:
    branches: [main, develop]
    paths: ['frontend/**']
  pull_request:
    branches: [main]
    paths: ['frontend/**']

jobs:
  test:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: frontend
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: 20
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json
      - run: npm ci
      - run: npm run lint
      - run: npm run typecheck
      - run: npm run test -- --coverage
      - run: npm run build

  e2e:
    needs: test
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: frontend
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: 20
      - run: npm ci
      - run: npx playwright install --with-deps
      - run: npm run test:e2e
```

---

## 19. Implementation Phases

### Phase 1: Foundation (Days 1-3)
- [ ] Project setup (Vite + React + TypeScript + Tailwind)
- [ ] shadcn/ui integration
- [ ] Axios client with interceptors
- [ ] Auth store (Zustand)
- [ ] Login/Register pages
- [ ] Protected route components

### Phase 2: Core Features (Days 4-7)
- [ ] Dashboard layout (sidebar, header)
- [ ] Listings list page with DataTable
- [ ] Create/Edit listing forms
- [ ] Listing detail page
- [ ] Platform filtering and search
- [ ] Status management

### Phase 3: AI Integration (Days 8-9)
- [ ] AI generation page
- [ ] Generation progress indicator
- [ ] Content preview with copy functionality
- [ ] Regenerate capability

### Phase 4: Admin Features (Days 10-11)
- [ ] Admin dashboard with charts
- [ ] User management page
- [ ] Analytics page
- [ ] Admin route guards

### Phase 5: Polish & Testing (Days 12-14)
- [ ] Error boundaries
- [ ] Loading states
- [ ] Empty states
- [ ] Unit tests (80%+ coverage)
- [ ] E2E tests for critical paths
- [ ] Accessibility audit
- [ ] Performance optimization

### Phase 6: Deployment (Day 15)
- [ ] Docker configuration
- [ ] CI/CD pipeline
- [ ] Production environment setup
- [ ] Monitoring integration (optional)

---

## 20. Appendix

### 20.1 Platform Color Mapping

| Platform | Primary Color | Badge Style |
|----------|---------------|-------------|
| Amazon | #FF9900 | Orange |
| FlipkART | #2874F0 | Blue |
| Meesho | #F43397 | Pink |
| Shopify | #96BF48 | Green |

### 20.2 Status Color Mapping

| Status | Color | Icon |
|--------|-------|------|
| DRAFT | Yellow | Pencil |
| PUBLISHED | Green | CheckCircle |
| ARCHIVED | Gray | Archive |

### 20.3 Responsive Breakpoints

| Breakpoint | Width | Layout |
|------------|-------|--------|
| Mobile | < 768px | Single column, bottom nav |
| Tablet | 768px - 1024px | Collapsible sidebar |
| Desktop | > 1024px | Full sidebar |

### 20.4 API Response Time Expectations

| Endpoint | Expected Response |
|----------|-------------------|
| Auth (login/register) | < 500ms |
| Listings CRUD | < 300ms |
| AI Generation | 2-10s (with loading indicator) |
| Search | < 200ms |
| Admin Analytics | < 1s |

---

*Document Version: 1.0*
*Last Updated: 2026-07-22*
*Author: AI Architect*
