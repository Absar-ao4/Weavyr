import {z} from 'zod';

export const signupSchema = z.object({
  username: z.string().trim().min(2, "Username is required"),
email: z.string().trim().email("Invalid email address"),
  password: z.string().min(8, { message: "Password must be at least 8 characters long" })
  .regex(/[a-z]/, { message: "Password must contain at least one lowercase letter" })
  .regex(/[0-9]/, { message: "Password must contain at least one number" }),
});

export const loginSchema = z.object({
  email: z.string().email("Invalid email address"),
  password: z.string().min(8, { message: "Password must be at least 8 characters long" }),
  rememberMe: z.boolean().optional(),
});

export type LoginSchema = z.infer<typeof loginSchema>;

export type SignupSchema = z.infer<typeof signupSchema>;