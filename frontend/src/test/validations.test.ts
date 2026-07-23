import { describe, it, expect } from 'vitest';
import { loginSchema, registerSchema, listingSchema } from '@/lib/validations';

describe('Validation Schemas', () => {
  describe('loginSchema', () => {
    it('accepts valid login data', () => {
      const result = loginSchema.safeParse({
        username: 'testuser',
        password: 'password123',
      });
      expect(result.success).toBe(true);
    });

    it('rejects empty username', () => {
      const result = loginSchema.safeParse({
        username: '',
        password: 'password123',
      });
      expect(result.success).toBe(false);
    });

    it('rejects empty password', () => {
      const result = loginSchema.safeParse({
        username: 'testuser',
        password: '',
      });
      expect(result.success).toBe(false);
    });
  });

  describe('registerSchema', () => {
    it('accepts valid registration data', () => {
      const result = registerSchema.safeParse({
        username: 'testuser',
        email: 'test@example.com',
        password: 'password123',
        fullName: 'Test User',
      });
      expect(result.success).toBe(true);
    });

    it('rejects invalid email', () => {
      const result = registerSchema.safeParse({
        username: 'testuser',
        email: 'not-an-email',
        password: 'password123',
        fullName: 'Test User',
      });
      expect(result.success).toBe(false);
    });

    it('rejects short password', () => {
      const result = registerSchema.safeParse({
        username: 'testuser',
        email: 'test@example.com',
        password: '123',
        fullName: 'Test User',
      });
      expect(result.success).toBe(false);
    });
  });

  describe('listingSchema', () => {
    it('accepts valid listing data', () => {
      const result = listingSchema.safeParse({
        productName: 'Wireless Headphones',
        platform: 'AMAZON',
      });
      expect(result.success).toBe(true);
    });

    it('rejects empty product name', () => {
      const result = listingSchema.safeParse({
        productName: '',
        platform: 'AMAZON',
      });
      expect(result.success).toBe(false);
    });

    it('rejects invalid platform', () => {
      const result = listingSchema.safeParse({
        productName: 'Wireless Headphones',
        platform: 'INVALID',
      });
      expect(result.success).toBe(false);
    });
  });
});
