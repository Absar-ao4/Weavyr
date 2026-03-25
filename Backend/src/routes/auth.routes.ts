import { Hono } from "hono";
import { PrismaClient } from "@prisma/client/edge";
import { withAccelerate } from "@prisma/extension-accelerate";
import { sign } from "hono/jwt";
import { loginSchema, signupSchema } from "../validators/auth.validator";
import bcrypt from "bcryptjs";

export const authRouter = new Hono<{
  Bindings: {
    ACCELERATE_URL: string;
    JWT_SECRET: string;
    RESEND_API_KEY: string; // Add this to your wrangler.toml / .env
  }
}>();

// 1. SIGNUP ROUTE (Sends Android Deep Link)
authRouter.post("/signup", async (c) => {
  const prisma = new PrismaClient({
    datasourceUrl: c.env.ACCELERATE_URL,
  }).$extends(withAccelerate());

  const body = await c.req.json();
  const { success } = signupSchema.safeParse(body);
  
  if (!success) {
    c.status(400);
    return c.json({ error: "Invalid input" });
  }

  try {
    const existingUser = await prisma.user.findUnique({
      where: { email: body.email },
    });
    if (existingUser) {
      c.status(400);
      return c.json({ error: "Email is already in use" });
    }
    const usernameExists = await prisma.user.findUnique({
      where: { username: body.username },
    });
    if (usernameExists) {
      c.status(400);
      return c.json({ error: "Username is already in use" });
    }

    const saltrounds = 10;
    const passwordHash = await bcrypt.hash(body.password, saltrounds);
    
    // Generate a secure random token
    const verificationToken = crypto.randomUUID();

    const user = await prisma.user.create({
      data: {
        email: body.email,
        passwordHash: passwordHash,
        username: body.username,
        isEmailVerified: false, 
        emailVerificationToken: verificationToken,
      }
    });

    // Configure this scheme (weavyr://) in your Android app's AndroidManifest.xml
    const deepLinkUrl = `weavyr://verify?token=${verificationToken}`;
    
    // Send email using Resend
    c.executionCtx.waitUntil(
      fetch('https://api.resend.com/emails', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${c.env.RESEND_API_KEY}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          from: 'onboarding@yourdomain.com', // Must be a verified domain in Resend
          to: user.email,
          subject: 'Verify your Account',
          html: `<h3>Welcome!</h3><p>Tap <a href="${deepLinkUrl}">here</a> on your Android device to verify your account and log in.</p>`
        })
      }).catch(err => console.error("Email failed to send", err))
    );

    return c.json({ message: "Signup successful. Please check your email to verify your account." });
  } catch (e) {
    c.status(500);
    return c.json({ error: "Internal server error" });
  }
});

// 2. LOGIN ROUTE (Blocks unverified users)
authRouter.post("/login", async (c) => {
    const prisma = new PrismaClient({
      datasourceUrl: c.env.ACCELERATE_URL,
    }).$extends(withAccelerate());

    const body = await c.req.json();
    const parsed = loginSchema.safeParse(body);
    
    if (!parsed.success) {
      c.status(400);
      return c.json({ error: "Invalid input" });
    }
    const data = parsed.data;

    try {
      const existingUser = await prisma.user.findUnique({
        where: { email: data.email },
      });
      if(!existingUser){
        c.status(400);
        return c.json({ error: "Invalid email or password" });
      }

      // Check verification status
      if (!existingUser.isEmailVerified) {
        c.status(403); 
        return c.json({ error: "Please verify your email address before logging in." });
      }

      const passwordValid = await bcrypt.compare(data.password, existingUser.passwordHash);
      if (!passwordValid) {
        c.status(400);
        return c.json({ error: "Invalid password" });
      }

      const expiry = data.rememberMe ? "30d" : "1d";
      const token = await sign({ userId: existingUser.id, expiry }, c.env.JWT_SECRET);
      
      return c.json({ token });
  } catch (e) {
    c.status(500);
    return c.json({ error: "Internal server error" });
  }
});

// 3. VERIFY EMAIL ROUTE (Called by Android App)
authRouter.post("/verify-email", async (c) => {
  const prisma = new PrismaClient({
    datasourceUrl: c.env.ACCELERATE_URL,
  }).$extends(withAccelerate());

  const { token } = await c.req.json();

  if (!token) {
    c.status(400);
    return c.json({ error: "Verification token is required" });
  }

  try {
    const user = await prisma.user.findUnique({
      where: { emailVerificationToken: token }
    });

    if (!user) {
      c.status(400);
      return c.json({ error: "Invalid or expired verification token" });
    }

    // Unlock the account and clear the token
    await prisma.user.update({
      where: { id: user.id },
      data: {
        isEmailVerified: true,
        emailVerificationToken: null, 
      }
    });

    return c.json({ message: "Email successfully verified. You can now log in." });
  } catch (e) {
    c.status(500);
    return c.json({ error: "Failed to verify email" });
  }
});